# alkemy
From Al"ch"emy. "Purify, mature and perfect certain objects."

v0.7 (still dev.)

--------
Overview
--------

1. Annotate your classes
2. Write visitors
3. Alkemize


-----------
Description
-----------

This library allows to qualify types through user defined annotations 
and parse them into a directed rooted tree (this process will be referred as Alkemization).

Once alkemized, any concrete instance of that type can be imbued with different 
properties / behaviours through visitor classes (visit node / visit element).

A type can define several different qualifiers, which will alkemize into different alkemy elements. 
Alkemy elements can be supported by one / many visitors. 


-------
Insides 
-------

This library instruments the classes and wrap the generated code using lambdas. 
If lambdas are unable to be created, the library fallbacks to reflection (which is considerably slower).

The instrumentation happens transparently to the user through the agent-tools library. 
It is important to remark though, that due to the instrumentation restrictions,
this library should start up before any of the alkemized classes are used !! (can't modify the stack of loaded classes).


-----------
Performance 
-----------

The performance of the lambda operations is around 20 times slower than regular code (reflection is around 200 times slower). 
This measurements might vary between different machines.

The framework add significant overhead (processing 1 million of simple objects ranges from 200-450 ms).

Performance can be significantly improved in some cases by writing specific visitors.

This is due to three factors:

1. A convenient AlkemyElement#newInstance(Object... args) method is included where each argument represents an alkemy element within the type. 
2. The args order of the newInstance method preserves the alkemy element declaration order / or the user specified order through the @Order annotation. 
2. Nodes include a branchDepth() method which indicates how many jumps are required from itself to the furthest children in the branch. 

Combining this three concepts, we can write node visitors that instantiate nodes at almost new() speed. 
Notice that a node with branch depth of 1 is flat (no grand children), and can handle the tree as a list and call newInstance(...) directly.


--------
Example
--------

It is probably easier to understand how-to use this library through an example. 

This particular example can be found in : 'org.alkemy.example.RandomGenerator' (other examples can be found in the test classes)

Alkemizing this the test results into : root (TestClass) -> { leaf (TestClass.i), leaf (TestClass.d) } }.

```java
public class TestClass
{
    @Random(min = 5, max = 10)
    int i;
    
    @Random(min = 9.25, max = 11.5)
    double d;
}
```

And then we can instantiate an Alkemist and imbue properties to the alkemized tree through a visitor.

The visitor, in this case a very simple RandomGenerator which uses xorshift64.

```java
// Injecting random generated values
public class RandomGenerator
{
    @Test
    public void generateRandoms()
    {
		// Build an Alkemist with a single visitor (XorRandomGenerator)
        final Alkemist alkemist = new AlkemistBuilder().visitor(new XorRandomGenerator()).build();
        
		// Create a raw class we want to alkemize
		final TestClass tc = new TestClass();

		// Visit TestClass.class alkemization and imbue XorRandomGenerator properties into tc.
        alkemist.process(tc);
        
		// No between matcher / gt, lt are left / right open.
        assertThat(tc.i, is(both(greaterThan(5)).and(lessThan(10)).or(equalTo(5)).or(equalTo(10)))); 
        assertThat(tc.d, is(both(greaterThan(9.25)).and(lessThan(11.5)).or(equalTo(9.25)).or(equalTo(11.5))));
    }
    
    // The visitor that works on the AlkemyElements. 
    static class XorRandomGenerator implements AlkemyElementVisitor<RandomElement>
    {
        @Override
        public void visit(RandomElement e, Object parent)
        {
            e.set(nextDouble(e.min, e.max), parent); // generates and sets the next random
        }

        @Override
        public RandomElement map(AlkemyElement e)
        {
            return new RandomElement(e);
        }

        protected double nextDouble(double min, double max)
        {
            return min + (nextDouble() * ((max - min)));
        }
        
        private long seed = System.nanoTime();
        
        /**
         * Return an uniformly distributed double number between (0-1).
         * <p>
         * Zero is not inclusive since we are using Xorshift.
         * <p>
         * One is excluded as well via the +1 in Long.MAX_VALUE + 1.
         */
        double nextDouble()
        {
            double d = xorshift64() / (double) (Long.MAX_VALUE + 1);
            return d < 0 ? -d : d; // xorshift64() generates values in the whole Long.MIN_VALUE to Long.MAX_VALUE. Ensure we
                                   // return positive numbers (0-1).
        }

        /**
         * Xorshift implementation 2^64 version.
         * <p>
         * Shifting triplet values selected from G. Marsaglia '<a href="https://www.jstatsoft.org/article/view/v008i14">Xorshift
         * RNGs</a>'.</a>'
         */
        long xorshift64()
        {
            seed ^= seed << 13;
            seed ^= seed >>> 7;
            seed ^= seed << 17;
            return seed;
        }
    }

    // The custom AlkemyElement built out of the Random marker.
    static class RandomElement extends AbstractAlkemyElement<RandomElement>
    {
        double min, max;

        protected RandomElement(AbstractAlkemyElement<?> other)
        {
            super(other);

            final Random a = other.desc().getAnnotation(Random.class);
            Conditions.requireNonNull(a); 

            min = a.min();
            max = a.max();
        }
    }

    // The visitor marker
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    @AlkemyLeaf(XorRandomGenerator.class)
    @interface Random
    {
        double min();

        double max();
    }
}
```
