# alkemy
From Al"ch"emy. "Purify, mature and perfect certain objects."

--------
Overview
--------

1. Annotate your classes
2. Build an alkemy tree
3. Traverse

-----------
Description
-----------

This library allows to parse types into a directed rooted tree (this process will be referred as Alkemization) 
which provides set / get access for fields, invoke for methods, as well as constructors.

The library comes with an in-built parser which recognises annotations qualified as AlkemyLeafs in both methods / fields. 

Once an Alkemization has taken place, the resulting tree can be traversed applying effects on each of the nodes / leafs. Nodes can be streamed, and have two in-built traversing strategies (pre-order <default>, post-order).

```java 
    AlkemyNodes.get(SomeType.class).forEach(consumer);
    AlkemyNodes.get(SomeType.class).postorder().forEach(consumer);
    AlkemyNodes.get(SomeType.class).stream().filter(filter).forEach(consumer);    
```

There is no limit in how many different alkemy elements a type can define, or how many different alkemy types an element visitor
can handle.

---------------------
A very simple example
---------------------

1. Value injection.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@AlkemyLeaf
@interface Random
{
    double min();

    double max();
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@AlkemyLeaf
@interface Uuid
{
}

public class TestClass
{
    @Random(min = 5, max = 10)
    int i;
    
    @Random(min = 9.25, max = 11.5)
    double d;
    
    @Uuid
    String s;
}

```java
public void generateRandoms()
{
    final TestClass tc = new TestClass();
    AlkemyNodes.get(TestClass.class)//
        .stream() //
        .filter(f -> Random.class == f.alkemyType() && !f.isNode()) //
		.forEach(e -> {
		    final Random desc = e.desc().getAnnotation(Random.class);
		    final double min = desc.min();
		    final double max = desc.max();
		    final double rand = min + (Math.random() * ((max - min)));
		    e.set(rand, tc);
		});
}

public void generateUuid()
{
    final TestClass tc = new TestClass();
    AlkemyNodes.get(TestClass.class)//
        .stream() //
        .filter(f -> Uuid.class == f.alkemyType() && !f.isNode()) //
		.forEach(e -> {
		    e.set(java.util.UUID.randomUUID(), tc);
		});
}
```

Commonly, if willing to object-map a large number of objects, it is undesirable to parse nodes every iteration as below.

``` java		                        
    final Random desc = e.desc().getAnnotation(Random.class);
    final double min = desc.min();
    final double max = desc.max();
```

Given that the definition is static, it can be parsed once and re-used as needed. For theses purposes, an in-built method is available to filter/transform nodes. 

```java
public class RandomElement extends AlkemyElement
{
    public final double min;
    public final double max;

    public RandomElement(AlkemyElement other)
    {
        super(other);
        final Random desc = e.desc().getAnnotation(Random.class);
        min = desc.min();
        max = desc.max();
    }
}

@Test
public void generateRandoms()
{
    final TestClass tc = new TestClass();
    final Node<RandomElement> root = AlkemyNodes.get(TestClass.class) //
                                                    , p -> Random.class == p.alkemyType() //
                                                    , f -> new RandomElement(f));
    root.stream().filter(f -> !f.isNode()) //
        .forEach(e -> {
	        final double rand = e.min + (Math.random() * ((e.max - e.min)));
	        e.set(rand, tc);
	    });
}
```

--------
Insights
--------

During the alkemization, classes are instrumented to enhance performance. If no instrumentation code is available during the parsing of the type, it fallbacks to reflection. 

The in-built alkemization in a nutshell.

* Creates a marker method : ```java public static boolean is$$instrumented() { return true; }```. This allows enabling / disabling the instr. version on runtime. 
* Creates an Order annotation with the declaration order of the fields, or leave it untouched if present. Alkemy trees are deterministically traversed.
* If no default constructor present, it creates a public static no-args factory constructor (create$$default) ```java public static TypeClass create$$default() { ... }```. 
* If default constructor is present, but not public, it changes its visibility to public.
* Creates a public static factory for the type : ```java public static TypeClass create$$args(Object[] args) { ... }```, where the args follow the order established in the Order annotation.
* Creates getters and setters if not present.
* Conversions && castings (wrapper -> primitive && String -> enum).

In the in-built parser, a type is considered alkemizable if itself, or any of its member types (including Collection component types) is alkemizable. In the following example, both Outer and Inner types are alkemizable and will be instrumented. 

```java
public class Outer 
{
    ...
    
    Collection<Inner> inners;
}

public class Inner
{
    ...
    
    @Property
    private int foo;    
}
```

------
Others
------

This library contains only the core functionality to build your own trees and traverse them. No syntax sugar, no common utilities or clients. Additionally...

* alkemy-commons : some re-usable syntax sugar like visitors
* alkemy-etc : some client examples (csv-reader, resultset-reader, settings-store, ...)
