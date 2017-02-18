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
package org.alkemy.common;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alkemy.util.Conditions;

public class DynamicLabel
{
    private DynamicLabel()
    {
    }

    public static boolean isDynamic(String raw, Pattern parameterKey)
    {
        Conditions.requireContentNotNull(raw, parameterKey);
        return parameterKey.matcher(raw).find();
    }

    /**
     * Given a parameterKey such as the following: <code>Pattern.compile("\\{&(.+?)\\}")</code>, <br>
     * the method will substitute any s = (.+?) group with the result of parameters.get(s).
     * <p>
     * If any group is left unresolved, a partially resolved string is returned.
     * <p>
     * Example:
     * <p>
     * regex: \\{&(.+?)\\} <br>
     * raw : "{&prefix}.bbb.{&infix}.ddd.{&suffix}" <br>
     * parameters : { { "prefix: "aaa" }, { "infix": "ccc" }, { "suffix": "eee" } } <br>
     * DynamicLabel.solve(raw, parameters, Pattern.compile(regex)) = "aaa.bbb.ccc.ddd.eee"
     */
    public static String replace(String raw, Map<String, String> parameters, Pattern parameterKey)
    {
        Conditions.requireContentNotNull(raw, parameterKey, parameters);

        final Matcher matcher = parameterKey.matcher(raw);
        final StringBuffer sb = new StringBuffer();
        int s = 0;
        while (matcher.find())
        {
            final String paramKey = matcher.group(1);
            final String replacement = parameters.get(paramKey);
            matcher.appendReplacement(sb, replacement == null ? paramKey : replacement);
            s = matcher.end();
        }
        sb.append(raw.substring(s));
        return sb.toString();
    }
}
