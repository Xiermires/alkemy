package org.alkemy.example;

import org.alkemy.example.RandomGenerator.Random;

public class TestClass
{
    @Random(min = 5, max = 10)
    int i;
    
    @Random(min = 9.25, max = 11.5)
    double d;
}
