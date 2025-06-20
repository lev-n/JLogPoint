# JLogPoint

Dynamic log points for JVM.

This project provides a simple Java agent that allows adding or removing
log points at runtime via an HTTP API. It relies on Byte Buddy to
instrument methods and intercept calls.

This project targets **Java 17**. Ensure JDK 17 or higher is available for
building and running the agent.

## Building

Use Maven to build the agent jar:

```bash
mvn package
```

The resulting jar `target/jlogpoint.jar` contains all dependencies and can
be used as a `-javaagent` argument.

## Usage

Start your application with the agent:

```bash
java -javaagent:path/to/jlogpoint.jar=classPattern=com.example.*,methodPattern=get.* -jar yourapp.jar
```

The agent instruments all classes and methods by default if no patterns are
provided. Visit `http://localhost:8081/` for the list of HTTP API endpoints that
allow managing log points at runtime.
