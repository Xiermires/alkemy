/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package org.alkemy.instr;

import static org.objectweb.asm.Opcodes.ACC_ENUM;
import static org.objectweb.asm.Opcodes.ASM5;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alkemy.instr.DefaultAlkemizer.Stop;
import org.alkemy.util.AlkemyUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

public class AlkemizerUtils
{
    private static final Pattern DESC = Pattern.compile("^L(.+\\/.+)+;$");

    public static boolean isDefaultCtor(String name, String desc)
    {
        return "<init>".equals(name) && "()V".equals(desc);
    }

    public static String getSetterName(String fieldName)
    {
        return "set" + camelUp(fieldName);
    }

    public static String getGetterName(String fieldName)
    {
        return "get" + camelUp(fieldName);
    }
    
    public static String toQualifiedName(String className)
    {
        return className.replace('/', '.');
    }

    // dots
    public static String toQualifiedNameFromDesc(String desc)
    {
        return toQualifiedName(toClassNameFromDesc(desc));
    }

    // slashes
    public static String toClassNameFromDesc(String desc)
    {
        final Matcher matcher = DESC.matcher(desc);
        if (matcher.matches())
            return matcher.group(1);
        return null;
    }

    public static String toDescFromClassName(String className)
    {
        return "L" + className + ";";
    }

    public static boolean isType(String desc)
    {
        return desc.startsWith("L");
    }

    public static boolean isCollection(String desc)
    {
        try
        {
            final ClassReader cr = new ClassReader(toQualifiedNameFromDesc(desc));
            final CheckImplements cv = new CheckImplements("java/util/Collection");
            cr.accept(cv, ClassReader.SKIP_CODE);
            return cv.isImplemented;
        }
        catch (IOException e)
        {
            throw new Stop("Can't read type {}", desc);
        }
    }

    public static boolean isEnum(String desc)
    {
        try
        {
            final ClassReader cr = new ClassReader(toQualifiedNameFromDesc(desc));
            final CheckFieldIsEnum cv = new CheckFieldIsEnum();
            cr.accept(cv, ClassReader.SKIP_CODE);
            return cv.isEnum;
        }
        catch (IOException e)
        {
            throw new Stop("Can't read type {}", desc);
        }
    }

    // FIXME : This pattern is invalid (nested generics fail).
    private static final Pattern nestedGeneric = Pattern.compile("L.+<(.*<.*)>;");
    private static final Pattern plainGeneric = Pattern.compile("L.+<(L.+;)>;");

    public static String toGenericType(String signature, String fallback)
    {
        final Matcher nested = nestedGeneric.matcher(signature);
        if (!nested.matches())
        {
            final Matcher plain = plainGeneric.matcher(signature);
            if (plain.matches()) { return plain.group(1); }
        }
        return fallback;
    }

    public static String camelUp(String name)
    {
        return name.substring(0, 1).toUpperCase().concat(name.substring(1));
    }

    static class CheckFieldIsEnum extends ClassVisitor
    {
        private boolean isEnum = false;

        CheckFieldIsEnum()
        {
            super(ASM5);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
        {
            isEnum = (access & ACC_ENUM) != 0;
            super.visit(version, access, name, signature, superName, interfaces);
        }
    }

    static class CheckImplements extends ClassVisitor
    {
        private final String desc;
        private boolean isImplemented = false;

        CheckImplements(String desc)
        {
            super(ASM5);
            this.desc = desc;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
        {
            isImplemented = Arrays.asList(interfaces).contains(desc);
            super.visit(version, access, name, signature, superName, interfaces);
        }
    }

    // Compile check so changing proxy'ed classes doesn't miss this class.
    // If any changes in the behaviour, change also the instrumentation accordingly.
    public static class Proxy
    {
        public static <T extends Enum<T>> T toEnum(Class<T> type, String namee)
        {
            return AlkemyUtils.toEnum(type, namee);
        }
        
        public static <T extends Enum<T>> Object toEnum(Class<T> type, Object value)
        {
            return AlkemyUtils.toEnum(type, value);
        }
    }
}
