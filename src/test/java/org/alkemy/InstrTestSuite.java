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
package org.alkemy;

import org.alkemy.InstrumentClassWithLambdas;
import org.alkemy.InstrumentClassWithLambdas.Instr;
import org.alkemy.InstrumentClassWithLambdas.InstrumentableLambdaClasses;
import org.alkemy.instr.AlkemizerCTF;
import org.junit.runner.RunWith;

@RunWith(InstrumentClassWithLambdas.class)
@InstrumentableLambdaClasses(//
testClassNames = { //
        "org.alkemy.CoreAlkemy", //
        "org.alkemy.parse.impl.AlkemizerTest", //
        "org.alkemy.parse.impl.TypeParserTest", //
        }, //
instrs = @Instr(classNames = { //
        "org.alkemy.TestClass", //
        "org.alkemy.TestTraverse", //
        "org.alkemy.TestTraverse$NestedA", //
        "org.alkemy.TestTraverse$NestedB", //
        "org.alkemy.TestTraverse$NestedC", //
        "org.alkemy.TestTraverse$NestedD", //
        "org.alkemy.TestTraverse$NestedE", //
        "org.alkemy.TestTraverse$NestedF", //
        "org.alkemy.TestStatic", //
        "org.alkemy.parse.impl.TestAlkemizer", //
        "org.alkemy.parse.impl.TestManyFields", //
        "org.alkemy.parse.impl.TestNode", //
        "org.alkemy.parse.impl.TestOrdered", //
        "org.alkemy.parse.impl.TestCreateInstanceParamPreserveOrder$FollowsOrder", //
        "org.alkemy.parse.impl.TestCreateInstanceParamPreserveOrder$FollowsDeclaration", //
        "org.alkemy.parse.impl.TestCreatedDefaultCtor", //
        "org.alkemy.parse.impl.TestDeepLeaves", //
        "org.alkemy.parse.impl.TestDeepLeaves$Nested1", //
        "org.alkemy.parse.impl.TestDeepLeaves$Nested2", //
        "org.alkemy.parse.impl.TestDeepLeaves$Nested3", //
        "org.alkemy.parse.impl.TestDeepLeaves$Nested4", //
        "org.alkemy.parse.impl.TestDeepLeaves$Nested5", //
        "org.alkemy.parse.impl.TestWithGetterSetter" //
        }, ctf = AlkemizerCTF.class))

public class InstrTestSuite
{
}
