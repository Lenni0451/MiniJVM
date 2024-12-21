# MiniJVM
A small and simple JVM written in Java made for sandboxing basic untrusted code.\
It was mainly written to make deobfuscation easier by running parts of the code in a controlled environment (e.g. string decryption).\
The project is still in a very early stage and not feature complete.

Due to the nature of the average JVM being very complex, running this is not a simple "click a button, and it works" process.\
It may need some manual tweaking to get it to run the code you want it to run, like implementing missing native methods or initializing system classes.

The project is written using Java 17 but should theoretically be able to run code compiled with older versions of Java (all versions ASM supports) (no guarantees).\
Fixing support for specific Java versions is not a priority but may be done later if the project gets more mature.

If you use this project and make some improvements, I would be happy to see a pull request.

## Goals
- Interpret all Java bytecode instructions
- Implement the most important native methods to run simple Java code
- Provide a simple API
- Handle exceptions and errors
- An interface for easy conversion between Java objects and MiniJVM objects
- Breakpoints at specific instructions
- Shadow objects (reimplementing complex logic using Java objects by redirecting method calls)

## Non-Goals
- Perfectly emulating the JVM
- Performance (this is not my main focus, it gets as fast as it gets)
- Full compatibility with the JVM specification
- JVM implementation specific quirks
- Running large Java applications

## Usage
> There are no public releases yet, so you have to build the project yourself if you want to toy around with it.\
> Check the test directory for some example code.

To use `MiniJVM` using [Gradle](#gradle)/[Maven](#maven) you can add it as a dependency to your project.\
The latest version can be found in the [releases](https://github.com/Lenni0451/MiniJVM/releases).\
My libraries are hosted on [Maven Central](https://mvnrepository.com/artifact/net.lenni0451/MiniJVM) or my own [maven server](https://maven.lenni0451.net/).

### Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "net.lenni0451:MiniJVM:x.x.x"
}
```

### Maven
```xml
<dependency>
  <groupId>net.lenni0451</groupId>
  <artifactId>MiniJVM</artifactId>
  <version>x.x.x</version>
</dependency>
```

## Contribution
If you would like to contribute to the project, please follow these steps:
1. Fork the repository
2. Create a new branch for your feature or bugfix
3. Make your changes and commit them
4. Push your changes to your fork
5. Create a pull request with a description of your changes

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.
