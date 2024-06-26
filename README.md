[![Build](https://img.shields.io/github/actions/workflow/status/xdev-software/thread-origin-agent/check-build.yml?branch=develop)](https://github.com/xdev-software/thread-origin-agent/actions/workflows/check-build.yml?query=branch%3Adevelop)

# Thread Origin Agent

In many situations is it helpful to find out who created a [Thread](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html).

To find the origin of a thread, this project provides a [javaagent](https://docs.oracle.com/en/java/javase/21/docs/api/java.instrument/java/lang/instrument/package-summary.html) which logs the stacktrace at Thread creation.

## Installation
[Installation guide for the latest release](https://github.com/xdev-software/thread-origin-agent/releases/latest#Installation)

## Usage
Insert ``-javaagent:<pathToThread-origin-agent.jar>=<packagesToIgnore>`` into the JVM-arguments (at the beginning!)<br/>
Examples:
```bash
java -javaagent:thread-origin-agent-1.0.0.jar -jar <appToInspect>.jar
java -javaagent:"C:\temp\thread-origin-agent-1.0.0.jar"=sun/awt,sun/java2d -jar <appToInspect>.jar
```

> [!NOTE]
> Please note that it's not possible to monitor all ``Thread`` starts as the ``Thread`` class is loaded and used extremely early.<br/>
> Some static instantiations that use Threads e.g. ``ForkJoinPool#common`` are therefore not affected by changing the underlying bytecode.

<details><summary>Example output for a Spring Boot application</summary>

```
[TOA] Arg: null
[TOA] Ignoring excluded:
[TOA] Trying to retransform loaded classes
[TOA] Ignoring javassist.CtField
...
[TOA] Retransformed loaded classes; 820x successful, 150x unmodifiable
[TOA] Detected java.lang.Thread.start() id: 46 name: background-preinit
[TOA]   org.springframework.boot.autoconfigure.BackgroundPreinitializer.performPreinitialization(BackgroundPreinitializer.java:129)
[TOA]   org.springframework.boot.autoconfigure.BackgroundPreinitializer.onApplicationEvent(BackgroundPreinitializer.java:85)
[TOA]   org.springframework.boot.autoconfigure.BackgroundPreinitializer.onApplicationEvent(BackgroundPreinitializer.java:55)
...
[TOA] Detected java.lang.Thread.start() id: 47 name: Thread-0
[TOA]   org.springframework.boot.autoconfigure.condition.OnClassCondition$ThreadedOutcomesResolver.<init>(OnClassCondition.java:147)
...
```

</details>

### Additional configuration options

These options can be configured as system properties. Example:<br/>
``-DTOA_LOG_CALLER_THREADS``

| Option | Description |
| --- | --- |
| ``TOA_LOG_THREAD_JOINS`` | Also logs ``Thread#join`` calls |
| ``TOA_LOG_CALLER_THREADS`` | Displays additional information about the thread that started the new thread |
| ``TOA_DISPLAY_METHOD_INSTRUMENTATION_FAILURES`` | Displays method instrumentation failures.<br/>This happens when a method is not present or can't be instrumented due to other reasons (e.g. JDK internal method).<br/>Produces a lot of log output so it's recommended to only enable it for debugging |

## Support
If you need support as soon as possible and you can't wait for any pull request, feel free to use [our support](https://xdev.software/en/services/support).

## Contributing
See the [contributing guide](./CONTRIBUTING.md) for detailed instructions on how to get started with our project.

## Dependencies and Licenses
View the [license of the current project](LICENSE) or the [summary including all dependencies](https://xdev-software.github.io/thread-origin-agent/dependencies)

<sub>This project was inspired by [kreyssel/maven-examples](https://github.com/kreyssel/maven-examples)</sub>
