/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any 
 * purpose with or without fee is hereby granted, provided that the above 
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES 
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALLIMPLIED WARRANTIES OF 
 * MERCHANTABILITY  AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR 
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES 
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN 
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF 
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *******************************************************************************/
package org.alkemy.instr;

import static org.objectweb.asm.Opcodes.ASM5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.alkemy.annotations.AlkemyLeaf;
import org.alkemy.annotations.Order;
import org.alkemy.instr.DefaultAlkemizer.Stop;
import org.alkemy.instr.DefaultAlkemizerWriter.OrderValueReader;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

public class DefaultAlkemizableVisitor extends AbstractAlkemizableVisitor
{
    private static final Logger log = LoggerFactory.getLogger(DefaultAlkemizer.class);

    // maintain both identified && non-identified leafs markers to speed up the process.
    private final Set<String> leafMarkers = new HashSet<>();
    private final Set<String> nonLeafMarkers = new HashSet<>();

    final List<String> orderedFields = new ArrayList<>();

    public DefaultAlkemizableVisitor(String className)
    {
        super(className);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
    {
        final FieldVisitor fv = super.visitField(access, name, desc, signature, value);
        return new FieldAnnotationVisitor(fv, name, fieldMap, leafMarkers, nonLeafMarkers, new HashSet<>(), new HasLeaves());
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible)
    {
        if (Order.class.getName().equals(AlkemizerUtils.toQualifiedNameFromDesc(desc))) { return new OrderValueReader(
                orderedFields, super.visitAnnotation(desc, visible)); }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitEnd()
    {
        final List<String> fields = fieldMap.entrySet().stream().filter(entry -> entry.getValue().alkemizable).map(
                entry -> entry.getKey()).collect(Collectors.toList());

        if (!orderedFields.isEmpty())
        {
            checkFieldNames(fields, orderedFields);
        }
        else
        {
            orderedFields.addAll(fields);
        }
    }

    private static void checkFieldNames(Collection<String> fields, List<String> orderedFields)
    {
        if (!fields.containsAll(orderedFields))
            throw new Stop("Invalid order definition (alien)."); // invalid definition
    }
    
    static class FieldProperties
    {
        final boolean isEnum;
        final boolean isStatic;
        final String desc;
        final String signature;

        boolean alkemizable;

        public FieldProperties(String desc, String signature, boolean isEnum, boolean isStatic)
        {
            this.isEnum = isEnum;
            this.isStatic = isStatic;
            this.desc = desc;
            this.signature = signature;
        }
    }

    static class MethodProperties
    {
        final boolean isStatic;
        final String desc;
        final String signature;

        boolean alkemizable;

        public MethodProperties(String desc, String signature, boolean isStatic)
        {
            this.isStatic = isStatic;
            this.desc = desc;
            this.signature = signature;
        }
    }

    static class FieldAnnotationVisitor extends FieldVisitor
    {
        protected final String name;
        protected final Map<String, FieldProperties> fields;
        protected final Set<String> leafMarkers;
        protected final Set<String> nonLeafMarkers;
        protected final Set<String> visited;
        protected final HasLeaves hasLeaves;

        FieldAnnotationVisitor(FieldVisitor fv, String name, Map<String, FieldProperties> fields, Set<String> leafMarkers,
                Set<String> nonLeafMarkers, Set<String> visited, HasLeaves hasLeaves)
        {
            super(ASM5, fv);

            this.name = name;
            this.fields = fields;
            this.leafMarkers = leafMarkers;
            this.nonLeafMarkers = nonLeafMarkers;
            this.visited = visited;
            this.hasLeaves = hasLeaves;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible)
        {
            if (visible && isAlkemizable(desc))
            {
                fields.get(name).alkemizable = true;
            }
            return super.visitAnnotation(desc, visible);
        }

        @Override
        public void visitEnd()
        {
            // Nodes are not annotated themselves, but contain leaves at some depth.
            // If a non-annotated type is found, deep search for leaves to identify it as a node.
            final FieldProperties props = fields.get(name);
            if (!props.alkemizable && !visited.contains(props.desc) && AlkemizerUtils.isType(props.desc)
                    && !AlkemizerUtils.isEnum(props.desc))
            {
                try
                {
                    if (isNode(props))
                    {
                        props.alkemizable = true;
                    }
                }
                catch (IOException e)
                {
                    log.trace("Cannot read the annotation '{}'. Ignore.", name);
                }
            }
            super.visitEnd();
        }

        private boolean isNode(FieldProperties props) throws IOException
        {
            final String desc;
            if (AlkemizerUtils.isCollection(props.desc))
                desc = AlkemizerUtils.toGenericType(props.signature, props.desc);
            else desc = props.desc;

            // Avoid cycles
            visited.add(desc);

            new ClassReader(AlkemizerUtils.toQualifiedNameFromDesc(desc)).accept(new TypeDeepLeafSearch(visited, leafMarkers,
                    nonLeafMarkers, hasLeaves), ClassReader.SKIP_CODE);
            return hasLeaves.get();
        }

        private boolean isAlkemizable(String desc)
        {
            return !nonLeafMarkers.contains(desc) && (leafMarkers.contains(desc) | isLeaf(desc));
        }

        private boolean isLeaf(String desc)
        {
            final String qualifiedName = AlkemizerUtils.toQualifiedNameFromDesc(desc);
            try
            {
                final ClassReader cr = new ClassReader(qualifiedName);
                final SearchForLeafMarker cv = new SearchForLeafMarker(nonLeafMarkers);
                cr.accept(cv, ClassReader.SKIP_CODE);

                if (cv.annotated)
                {
                    leafMarkers.add(desc);
                }
                return cv.annotated;
            }
            catch (IOException e)
            {
                nonLeafMarkers.add(desc);
                log.trace("Cannot read the annotation '{}'. Ignore.", desc);
            }
            return false;
        }
    }

    @Override
    public boolean isClassAlkemizable()
    {
        return Iterables.find(fieldMap.values(), p -> p.alkemizable) != null
                || Iterables.find(methodMap.values(), p -> p.alkemizable) != null;
    }

    static class SearchForLeafMarker extends ClassVisitor
    {
        private final Set<String> nonLeafMarkers;
        private boolean annotated = false;

        SearchForLeafMarker(Set<String> nonAlkemizableAnnotations)
        {
            super(ASM5);
            this.nonLeafMarkers = nonAlkemizableAnnotations;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible)
        {
            if (AlkemyLeaf.class.getName().equals(AlkemizerUtils.toQualifiedNameFromDesc(desc)))
            {
                annotated = true;
            }
            else
            {
                nonLeafMarkers.add(desc);
            }
            return super.visitAnnotation(desc, visible);
        }
    }

    static class TypeDeepLeafSearch extends AbstractAlkemizableVisitor
    {
        private final Set<String> visited = new HashSet<>();
        private final Set<String> leafMarkers;
        private final Set<String> nonLeafMarkers;
        private final HasLeaves hasLeaves;

        public TypeDeepLeafSearch(Set<String> visited, Set<String> alkemizableAnnotations, Set<String> nonAlkemizableAnnotations,
                HasLeaves hasLeaves)
        {
            super(null);

            // this.visited = visited;
            this.leafMarkers = alkemizableAnnotations;
            this.nonLeafMarkers = nonAlkemizableAnnotations;
            this.hasLeaves = hasLeaves;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
        {
            final FieldVisitor fv = super.visitField(access, name, desc, signature, value);
            if (visited.contains(desc))
                return fv;

            return new FieldLeafVisitor(visited, name, fieldMap, leafMarkers, nonLeafMarkers, hasLeaves);
        }

        @Override
        public boolean isClassAlkemizable()
        {
            return hasLeaves.get();
        }
    }

    static class FieldLeafVisitor extends FieldAnnotationVisitor
    {
        FieldLeafVisitor(Set<String> visited, String name, Map<String, FieldProperties> fields, Set<String> leafMarkers,
                Set<String> nonLeafMarkers, HasLeaves hasLeaves)
        {
            super(null, name, fields, leafMarkers, nonLeafMarkers, visited, hasLeaves);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible)
        {
            final AnnotationVisitor av = super.visitAnnotation(desc, visible);
            if (fields.get(name).alkemizable)
            {
                hasLeaves.leaf = true;
            }
            return av;
        }

        @Override
        public void visitEnd()
        {
            if (hasLeaves.get())
            {
                return;
            }
            else
            {
                super.visitEnd();
            }
        }
    };

    static class HasLeaves implements Supplier<Boolean>
    {
        boolean leaf;

        @Override
        public Boolean get()
        {
            return leaf;
        }
    }
}
