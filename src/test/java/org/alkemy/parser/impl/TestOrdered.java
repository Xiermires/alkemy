package org.alkemy.parser.impl;

import org.alkemy.annotations.Order;
import org.alkemy.general.Bar;

@Order({ "s1", "s2", "s3", "s4", "s5", "s6", "s7" })
public class TestOrdered
{
    @Bar
    String s1 = "This";

    @Bar
    String s2 = "is";

    @Bar
    String s3 = "an";

    @Bar
    String s4 = "example";

    @Bar
    String s5 = "of";

    @Bar
    String s6 = "ordered";

    @Bar
    String s7 = "alkemyElements";
}
