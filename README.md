<div align="center">

# JniGlue
[![Maven Central](https://img.shields.io/maven-central/v/io.github.aecsocket/jni-glue-annotations)]()

Lightweight library for generating C++ JNI code for a Java project

---

</div>

This allows you to use annotations to write C++ code directly in Java source code:

```java
@JniNative("foobar/FooBarJNI")
public class FooBarLibrary {
    public static int addTen(int value) {
        return _addTen(value);
    }
    @JniBind("return value + 10;")
    public static native int _addTen(int value);
    
    public static void testNatives() {
        int value = addTen(15);
        System.out.println("Value is " + value);
        // "Value is 25"
    }
}
```

## Usage

```kotlin
// RECOMMENDED: if using Java, set the language version to at least 15
// to use text block literals
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(15))
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.aecsocket", "jni-glue-annotations", "VERSION")
    annotationProcessor("io.github.aecsocket", "jni-glue-processor", "VERSION")
}
```

### JNI models

Use `@JniNative` to mark a file as part of a JNI model. Afterwards you can use `@JniBind` and `@JniBindSelf` to associate Java methods
with C functions. For calling Java from JNI, mark a method as `@JniCallback` to generate a `jmethodID` for the method.

To associate a Java class with a C++ class, annotate the class as `@JniType("C++ CLASS NAME")` to allow the use of `@JniBindSelf`, which
automatically generates these lines for you, with `_a` being the `long` address of the object:

```cpp
auto* self = (C++ CLASS NAME*) _a;
```

An example of using this to wrap a C++ `Baz` class or struct:

```java
@JniNative("FooBar/FooBarJNI")
@JniInclude("<FooBar/Baz.h>")
@JniType("Baz")
public final class Baz {
    public long address;

    public int getProperty() {
        return _getProperty(address);
    }
    @JniBindSelf("return self->mProperty;")
    private static native int _getProperty(long _a);
}
```

### Using the generated JNI code

A header file of the generated code will be output in the build directory's `annotationProcessors` directory. You can then
include this header file in another C++ Gradle subproject to generate a library file from the code.

## Building from source

```sh
git clone https://github.com/aecsocket/jni-glue
cd jni-glue
./gradlew build
```
