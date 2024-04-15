[![Build](https://img.shields.io/github/actions/workflow/status/xdev-software/thread-origin-agent/checkBuild.yml?branch=develop)](https://github.com/xdev-software/thread-origin-agent/actions/workflows/checkBuild.yml?query=branch%3Adevelop)

# thread-origin-agent

In many situations is it helpful to find out who created a [Thread](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html).

To find the origin of a thread, this project provides a [javaagent](https://docs.oracle.com/en/java/javase/21/docs/api/java.instrument/java/lang/instrument/package-summary.html) which logs the stacktrace at Thread creation.

## Installation
[Installation guide for the latest release](https://github.com/xdev-software/thread-origin-agent/releases/latest#Installation)

## Usage
Insert ``-javaagent:<pathTothread-origin-agent.jar>=<packagesToIgnore>`` into the JVM-arguments (at the beginning!)<br/>
Examples:
```bash
java -jar <appToInspect>.jar -javaagent:thread-origin-agent-1.0.0.jar
java -jar <appToInspect>.jar -javaagent:"C:\temp\thread-origin-agent-1.0.0.jar"=sun/awt,sun/java2d
```

> [!NOTE]
> Please note that it's not possible to monitor all ``Thread`` starts as the ``Thread`` class is loaded and used extremely early.<br/>
> Some static instantiations that use Threads e.g. ``ForkJoinPool#common`` are therefore not affected by changing the underlying bytecode.

## Support
If you need support as soon as possible and you can't wait for any pull request, feel free to use [our support](https://xdev.software/en/services/support).

## Contributing
See the [contributing guide](./CONTRIBUTING.md) for detailed instructions on how to get started with our project.

## Dependencies and Licenses
View the [license of the current project](LICENSE) or the [summary including all dependencies](https://xdev-software.github.io/thread-origin-agent/dependencies)

<sub>This project was inspired by [kreyssel/maven-examples](https://github.com/kreyssel/maven-examples)</sub>
