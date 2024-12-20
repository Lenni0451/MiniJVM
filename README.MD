# GradleTemplate
A small and simple JVM written in Java made for sandboxing basic untrusted code.\
It was mainly written to run basic code snippets to make deobfuscation easier (e.g. running string decryption code).\
The project is still in a very early stage and not feature complete.

The project is written using Java 17, but should be able to run code compiled with older versions of Java (all versions ASM supports).

## Goals
- Interpret all Java bytecode instructions
- Implement the most important native methods to run simple Java code
- Provide a simple API
- Handle exceptions and errors

## Non-Goals
- Perfect performance (expect a lot of overhead)
- Full compatibility with the JVM specification
- JVM implementation specific quirks
- Running large Java applications

## Usage
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
