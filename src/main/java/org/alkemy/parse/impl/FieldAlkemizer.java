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

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alkemy.annotations.Order;
import org.alkemy.exception.FormattedException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instrumentation class focused on the alkemization of fields.
 * <p>
 * It does the following:
 * <ul>
 * <li>Creates a marker method : 'public static boolean is$$instrumented() { return true; }' (this
 * can allow enabling / disabling the instr. version on runtime).
 * <li>Creates an {@link Order} annotation with the declaration order of the fields, or leave it
 * untouched if present.
 * <li>Makes the default constructor public if it isn't already or creates a default one if it
 * doesn't exist.
 * <li>Creates a public static factory for the type : 'public static TypeClass
 * create$$instance(Object[] args) { ... }', where the args follow the order established in the
 * {@link Order} annotation.
 * <li>Creates for each alkemized member a getter and a setter 'public fie ldType get$$fieldName() {
 * ... }' && 'public void set$$fieldName(fieldType newValue) { ... }'
 * <li>Creates for each alkemized static member a getter and a setter 'public static fieldType
 * get$$fieldName() { ... }' && 'public static void set$$fieldName(fieldType newValue) { ... }'
 * <li>Conversions && castings (wrapper -> primitive && String -> enum).
 * </ul>
 */
public class FieldAlkemizer extends Alkemizer
{
    static final String CREATE_DEFAULT = "create$$default";
    static final String INSTATIATOR = "obj$$instantiator";
    static final String IS_INSTRUMENTED = "is$$instrumented";

    private static final Logger log = LoggerFactory.getLogger(FieldAlkemizer.class);
    private final String className;
    private boolean hasDefaultCtor;

    private FieldAlkemizer(ClassVisitor cv, String className)
    {
        super(ASM5, cv);
        this.className = className;
    }

    static byte[] alkemize(String className, byte[] classBytes)
    {
        final List<AlkemizerProcessFactory> alkemizers = new ArrayList<>();

        alkemizers.add(new AlkemizerProcessFactory()
        {
            final List<Alkemizer> alkemizers = new ArrayList<>();

            @Override
            public Alkemizer create(String className, ClassWriter cw)
            {
                final FieldOrderWriter fieldOrderWriter = new FieldOrderWriter(cw, className);
                alkemizers.add(fieldOrderWriter);
                return fieldOrderWriter;
            }

            @Override
            public boolean isUpdated()
            {
                for (Alkemizer alkemizer : alkemizers)
                    if (alkemizer.isAlkemized())
                        return true;

                return false;
            }
        });
        return alkemize(className, classBytes, alkemizers);
    }

    static byte[] alkemize(String className, byte[] classBytes, List<AlkemizerProcessFactory> alkemizers)
    {
        if (className != null) // do not instrument on the fly created classes by for
                               // instance Unsafe#define...
        {
            byte[] copy = Arrays.copyOf(classBytes, classBytes.length);

            try
            {
                boolean modified = false;
                for (AlkemizerProcessFactory alkemizer : alkemizers)
                {
                    final ClassReader cr = new ClassReader(copy);
                    final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

                    cr.accept(alkemizer.create(cr.getClassName(), cw), ClassReader.SKIP_FRAMES);
                    copy = cw.toByteArray();

                    modified = modified || alkemizer.isUpdated();
                }

                // If modified, add the is$$instrumented and make ctor public.
                if (modified)
                {
                    final ClassReader cr = new ClassReader(copy);
                    final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

                    cr.accept(new FieldAlkemizer(cw, className), ClassReader.SKIP_FRAMES);
                    return cw.toByteArray();
                }
            }
            catch (Exception e) // error while alkemizing. Return the original class.
            {
                log.trace("Error while alkemizing class '{}'. Keep non modified version.", e, className);
            }
        }
        return classBytes;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        int _access = access;
        if (AlkemizerUtils.isDefaultCtor(name, desc))
        {
            hasDefaultCtor = true;
            // if not accessible, make it public
            if (_access == 0) // package default
            {
                _access += ACC_PUBLIC;
            }
            if ((_access & ACC_PRIVATE) != 0)
            {
                _access = (_access - ACC_PRIVATE) + ACC_PUBLIC;
            }
            else if ((_access & ACC_PROTECTED) != 0)
            {
                _access = (_access - ACC_PROTECTED) + ACC_PUBLIC;
            }
        }
        return super.visitMethod(_access, name, desc, signature, exceptions);
    }

    @Override
    public boolean isAlkemized()
    {
        return false;
    }

    @Override
    public void visitEnd()
    {
        appendCreateDefault();
        appendIsInstrumented();
        super.visitEnd();
    }

    private void appendCreateDefault()
    {
        if (!hasDefaultCtor)
        {
            final FieldVisitor fv = super.visitField(ACC_PRIVATE, INSTATIATOR,
                    "Lorg/objenesis/instantiator/ObjectInstantiator;", "Lorg/objenesis/instantiator/ObjectInstantiator<"
                            + AlkemizerUtils.toDescFromClassName(className) + ">;", null);
            fv.visitEnd();
        }

        final MethodVisitor mv = super.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_DEFAULT, //
                "()" + AlkemizerUtils.toDescFromClassName(className), null, null);

        mv.visitCode();
        final Label l0 = new Label();
        mv.visitLabel(l0);

        if (hasDefaultCtor)
        {
            mv.visitTypeInsn(NEW, className);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "()V", false);
            mv.visitInsn(ARETURN);
        }
        else
        {
            mv.visitFieldInsn(GETSTATIC, className, "instantiator", "Lorg/objenesis/instantiator/ObjectInstantiator;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/objenesis/instantiator/ObjectInstantiator", "newInstance",
                    "()Ljava/lang/Object;", true);
            mv.visitTypeInsn(CHECKCAST, className);
            mv.visitInsn(ARETURN);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void appendIsInstrumented()
    {
        final String methodName = IS_INSTRUMENTED;
        final MethodVisitor mv = super.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, "()Z", null, null);

        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public static interface AlkemizerProcessFactory
    {
        Alkemizer create(String className, ClassWriter cw);

        boolean isUpdated();
    }

    static class Stop extends FormattedException
    {
        private static final long serialVersionUID = 1L;

        public Stop(String format, Object... args)
        {
            super(format, args);
        }
    }
}
