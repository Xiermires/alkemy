package org.alkemy.alkemizer;

import org.alkemy.alkemizer.InstrumentableLambdaClassSuite.Instr;
import org.alkemy.alkemizer.InstrumentableLambdaClassSuite.InstrumentableLambdaClasses;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(InstrumentableLambdaClassSuite.class)
@InstrumentableLambdaClasses(testClassNames = "org.alkemy.alkemizer.AlkemizerTest", //
                                instrs = @Instr(classNames = "org.alkemy.alkemizer.TestClass", ctf = AlkemizerCTF.class))
public class AlkemizerTestSuite
{
    @BeforeClass
    public static void pre() throws Exception
    {
        Thread.sleep(3);
    }
}
