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

public class TestClass
{
    @Random(min = 5, max = 10)
    int i;
    
    @Random(min = 9.25, max = 11.5)
    double d;
}
```

```java
@Test
public void generateRandoms()
{
    final TestClass tc = new TestClass();
    AlkemyNodes.get(TestClass.class).stream() //
                                    .filter(f -> Random.class == f.alkemyType() && !f.isNode()) //
				    .forEach(e -> {
		                        final Random desc = e.desc().getAnnotation(Random.class);
				        final double min = desc.min();
					final double max = desc.max();
					final double rand = min + (Math.random() * ((max - min)));
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
* Creates a default constructor if not present, or makes it public if present but with less visibility. 
* Creates a public static factory for the type : ```java public static TypeClass create$$instance(Object[] args) { ... }```, where the args follow the order established in the Order annotation.
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
