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

This library allows to parse types into a directed rooted tree (this process will be referred as Alkemization) 
which provides set / get access for fields and invoke for methods.

The library comes with an in-built parser which recognises annotations qualified as AlkemyLeafs in both methods / fields. 

Once an Alkemization has taken place, the resulting tree can be traversed applying effects on each of the nodes / leafs.

There are three in-built ways to traverse a node:

1. Using Node#traverse(Consumer c) function. 
2. Using a AlkemyNodeReader to process the nodes in combination with AlkemyElementVisitors to process the leafs (preorder and postorder node readers are provided)
3. Using a AlkemyNodeHandler that will handle the whole process.

There is no limit in how many different alkemy elements a type can define, or how many different alkemy types an element visitor
can handle.

-------------
Some Examples
-------------

1. Injection.

```java
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
	// Reads TestClass, identifies the @Random elements and applies XorRandomGenerator on them.
	final TestClass tc = Alkemy.mature(TestClass.class, new XorRandomGenerator());
	
	... // asserts
}

// The visitor that works on the AlkemyElements.
static class XorRandomGenerator implements AlkemyElementVisitor<Void, RandomElement>
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
	
	... // PRNG code 
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@AlkemyLeaf
@interface Random
{
	double min();

	double max();
}
```

In this example we map the result of generating a random into a field, but there is no 
restriction regarding the source of the data. Sockets, db, service locators, ...

2. Map streams

```java
public class TestClass
{
    @Index(0)
    int a;
    
    @Index(1)
    double b;
    
    @Index(2)
    float c;
}


@Test
public void testCsvReader() throws IOException
{
	final String NEW_LINE = System.getProperty("line.separator");
	final String EXAMPLE = "0,1.2,2.3,12345678902,4" + NEW_LINE + "9,1.65,7f,12345678901,5";

	// Simulate the whole csv is a file process (although we only need an Iterator<String>)
	final BufferedReader reader = new BufferedReader(new InputStreamReader(
			new ByteArrayInputStream(EXAMPLE.getBytes("UTF-8"))));

	final CsvReader mapper = new CsvReader();

	final List<TestClass> tcs = reader.lines().map(l -> l.split(",")).map(l -> Alkemy.mature(TestClass.class, mapper, l))
			.collect(Collectors.toList());
			
	// asserts ...
}

public class CsvReader extends IndexedElementVisitor<String[]>
{
    final TypedValueFromStringArray tvfs = new TypedValueFromStringArray();

    @Override
    public void visit(IndexedElement e, Object parent, String[] parameter)
    {
        e.set(tvfs.getValue(e, parameter), parent);
    }
}
```

Resultsets and other typical stream sources follow similar fashion. Reverse mapping from the object to the stream is also fairly simple (simply reverse the mapper logic). 

3. There is no example as today, but it is possible to write visitors to support scenarios such as this...

```java
public class Foo
{
	@Property
	int foo;
	
	@Property
	int bar;
	
	@Use("foo", "bar")
	@Schedule(every = 5000, unit = TimeUnit.MILLISECONDS)
	public void method(int foo, int bar)
	{
		...
	}
}

-------
INSIDES
-------

The library instruments the classes and wrap the generated code using lambdas. 
If instrumentation is not possible, the library fallbacks to reflection (which is considerably slower).

The instrumentation happens transparently to the user through the agent-tools library. 
It is important to remark though, that due to the instrumentation restrictions,
this library should start up before any of the alkemized classes are used !! (can't modify the stack of loaded classes).

Although the library doesn't include it. An approach such as the Spring boot one is straight forward to code.

-----------
Performance 
-----------

The performance of the lambda operations is around 20 times slower than regular code (reflection is around 200 times slower). 
This measurements might vary between different machines.

The framework add significant overhead (processing 1 million of objects ranges from 200-350 ms).

Performance can be significantly improved in some cases by writing specific AlkemyNodeHandler's (processing 1 million object ranges from 50-70 ms).