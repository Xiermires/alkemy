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
package org.alkemy.parse.impl;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.alkemy.annotations.AlkemyLeaf;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FieldAccessorWriter extends AbstractClassFieldVisitor
{
    private static final Logger log = LoggerFactory.getLogger(FieldAlkemizer.class);

    // maintain both identified && non-identified leafs markers to speed up the process.
    private final Set<String> leafMarkers = new HashSet<>();
    private final Set<String> nonLeafMarkers = new HashSet<>();
    private boolean alkemized = false;

    public FieldAccessorWriter(ClassVisitor cv, String className)
    {
        super(cv, className);
    }

    static String getGetterName(String fieldName)
    {
        return "get" + AlkemizerUtils.camelUp(fieldName);
    }

    static String getSetterName(String fieldName)
    {
        return "set" + AlkemizerUtils.camelUp(fieldName);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value)
    {
        final FieldVisitor fv = super.visitField(access, name, desc, signature, value);
        return new FieldAnnotationVisitor(fv, name, fieldMap, leafMarkers, nonLeafMarkers, new HashSet<>(), new HasLeaves());
    }

    @Override
    public void visitEnd()
    {
        appendGetters();
        appendSetters();
        super.visitEnd();
    }

    private void appendGetters()
    {
        alkemizableFields().forEach(entry -> appendGetter(entry.getKey(), entry.getValue().desc, entry.getValue().isStatic));
    }

    private void appendSetters()
    {
        alkemizableFields().forEach(entry -> appendSetter(entry.getKey() //
                , entry.getValue().desc //
                , entry.getValue().isEnum //
                , entry.getValue().isStatic));
    }

    private Stream<Entry<String, FieldProperties>> alkemizableFields()
    {
        return fieldMap.entrySet().stream().filter(entry -> entry.getValue().alkemizable);
    }

    private void appendGetter(String name, String desc, boolean isStatic)
    {
        final String methodName = getGetterName(name);
        final MethodProperties properties = methodMap.get(methodName);
        if (properties == null || !("()" + desc).equals(properties.desc))
        {
            final MethodVisitor mv = super.visitMethod(isStatic ? ACC_PUBLIC + ACC_STATIC : ACC_PUBLIC, methodName, "()" + desc,
                    null, null);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(isStatic ? GETSTATIC : GETFIELD, className, name, desc);
            mv.visitInsn(Type.getType(desc).getOpcode(IRETURN));
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        alkemized = true;
    }

    private void appendSetter(String name, String desc, boolean isEnum, boolean isStatic)
    {
        final String methodName = getSetterName(name);
        final MethodProperties properties = methodMap.get(methodName);
        if (properties == null || !("(" + desc + ")V").equals(properties.desc))
        {
            // typified
            appendSetter(name, desc, methodName, isStatic);

            if (isEnum)
            {
                // String support
                final MethodVisitor mv = visitMethod(isStatic ? ACC_PUBLIC + ACC_STATIC : ACC_PUBLIC, methodName, "("
                        + "Ljava/lang/String;" + ")V", null, null);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitLdcInsn(Type.getType(desc));
                mv.visitVarInsn(ALOAD, 1);
                mv.visitMethodInsn(INVOKESTATIC, "org/alkemy/parse/impl/AbstractClassFieldVisitor$Proxy", "toEnum", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;", false);
                mv.visitTypeInsn(CHECKCAST, AlkemizerUtils.toClassNameFromDesc(desc));
                mv.visitFieldInsn(isStatic ? PUTSTATIC : PUTFIELD, className, name, desc);
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
            else if (!"Ljava/lang/String;".equals(desc))
            {
                // String support
                final MethodVisitor mv = visitMethod(isStatic ? ACC_PUBLIC + ACC_STATIC : ACC_PUBLIC, methodName, "("
                        + "Ljava/lang/String;" + ")V", null, null);

                mv.visitTypeInsn(NEW, "java/lang/UnsupportedOperationException");
                mv.visitInsn(DUP);
                mv.visitLdcInsn("Invalid conversion.");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/UnsupportedOperationException", "<init>", "(Ljava/lang/String;)V",
                        false);
                mv.visitInsn(ATHROW);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        alkemized = true;
    }

    private void appendSetter(String name, String desc, String methodName, boolean isStatic)
    {
        final MethodVisitor mv = visitMethod(isStatic ? //
        ACC_PUBLIC + ACC_STATIC
                : ACC_PUBLIC, methodName, "(" + desc + ")V", null, null);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(Type.getType(desc).getOpcode(ILOAD), 1);

        mv.visitFieldInsn(isStatic ? PUTSTATIC : PUTFIELD, className, name, desc);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
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
    public boolean isAlkemized()
    {
        return alkemized;
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

    static class TypeDeepLeafSearch extends AbstractClassFieldVisitor
    {
        private final Set<String> visited = new HashSet<>();
        private final Set<String> leafMarkers;
        private final Set<String> nonLeafMarkers;
        private final HasLeaves hasLeaves;

        public TypeDeepLeafSearch(Set<String> visited, Set<String> alkemizableAnnotations, Set<String> nonAlkemizableAnnotations,
                HasLeaves hasLeaves)
        {
            super(null, null);

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
        public boolean isAlkemized()
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
