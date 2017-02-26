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
package org.alkemy;

import org.alkemy.InstrumentClassWithLambdas.Instr;
import org.alkemy.InstrumentClassWithLambdas.InstrumentableLambdaClasses;
import org.alkemy.parse.impl.AlkemizerCTF;
import org.junit.runner.RunWith;

@RunWith(InstrumentClassWithLambdas.class)
@InstrumentableLambdaClasses(//
testClassNames = { "org.alkemy.AlkemyTest", //
        "org.alkemy.parse.impl.AlkemizerTest", //
        "org.alkemy.parse.impl.TypeParserTest",
        "org.alkemy.visitor.impl.AlkemyVisitorTests", //
        "org.alkemy.example.RandomGenerator" //
}, //
instrs = @Instr(classNames = { "org.alkemy.TestClass", //
        "org.alkemy.TestFastVisitor", //
        "org.alkemy.TestDeepCopy", //
        "org.alkemy.parse.impl.TestAlkemizer", //
        "org.alkemy.parse.impl.TestManyFields", //
        "org.alkemy.parse.impl.TestClass", //
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
        "org.alkemy.example.TestClass", //
        "org.alkemy.visitor.impl.TestClass", //
        "org.alkemy.visitor.impl.TestWriter", //
        "org.alkemy.visitor.impl.TestReader", //
        "org.alkemy.visitor.impl.TestReader$NestedA", //
        "org.alkemy.visitor.impl.TestReader$NestedB", //
        "org.alkemy.visitor.impl.TestWriter$NestedA", //
        "org.alkemy.visitor.impl.TestWriter$NestedB" }, ctf = AlkemizerCTF.class))
public class AlkemyInstrTestSuite
{
}
