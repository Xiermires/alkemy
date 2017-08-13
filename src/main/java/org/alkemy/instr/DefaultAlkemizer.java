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

import org.alkemy.annotations.Order;
import org.alkemy.exception.FormattedException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
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
public class DefaultAlkemizer
{
    private static final Logger log = LoggerFactory.getLogger(DefaultAlkemizer.class);

    static byte[] alkemize(String className, byte[] classBytes)
    {
        // do not instrument on the fly created classes (i.e Unsafe#define)
        if (className != null)
        {
            try
            {
                final ClassReader cr = new ClassReader(classBytes);
                final DefaultAlkemizableVisitor alkemizableVisitor = new DefaultAlkemizableVisitor(className);
                cr.accept(alkemizableVisitor, ClassReader.SKIP_FRAMES);

                if (alkemizableVisitor.isClassAlkemizable())
                {
                    final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                    final DefaultAlkemizerWriter ctorWriter = new DefaultAlkemizerWriter(cw//
                            , className//
                            , alkemizableVisitor.orderedFields//
                            , alkemizableVisitor.fieldMap//
                            , alkemizableVisitor.methodMap);
                    
                    cr.accept(ctorWriter, ClassReader.SKIP_FRAMES);
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

    static class Stop extends FormattedException
    {
        private static final long serialVersionUID = 1L;

        public Stop(String format, Object... args)
        {
            super(format, args);
        }
    }
}
