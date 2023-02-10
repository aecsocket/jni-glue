<div align="center">

# JniGlue
[![License](https://img.shields.io/github/license/aecsocket/jni-glue)](LICENSE)
[![CI](https://img.shields.io/github/actions/workflow/status/aecsocket/jni-glue/build.yml)](https://github.com/aecsocket/jni-glue/actions/workflows/build.yml)
![Release](https://img.shields.io/maven-central/v/io.github.aecsocket/jni-glue-annotations?label=release)
![Snapshot](https://img.shields.io/nexus/s/io.github.aecsocket/jni-glue-annotations?label=snapshot&server=https%3A%2F%2Fs01.oss.sonatype.org)

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

### Loading the natives

Use `JniPlatform.get()` to get which platform the host is currently running. You can use this to determine the file name
of the native library to load:

```
# file structure
foobar/libfoobar-bindings.so
foobar/foobar-bindings.dll
foobar/libfoobar-bindings.dylib
```

```java
JniPlatform platform = JniPlatform.get(); // e.g. WINDOWS, LINUX
String fileName = platform.mapLibraryName("foobar-bindings"); // e.g. libfoobar-bindings.so
System.load(fileName);
```

### JNI models

Use `@JniNative` to mark a file as part of a JNI model:

```java
@JniNative(FooBarLibrary.JNI_MODEL)
public final class FooBarLibrary {
    public static final String JNI_MODEL = "foobar/FooBarJNI";
}
```

**Note:** `@JniNative` is an inherited annotation, so you do *not* need to specify it on classes which extend a class
annotated with it. A common pattern for this is:

```java
@JniNative(FooBarLibrary.JNI_MODEL)
public class FooBarNative {
    protected long address;
    
    public FooBarNative(long address) {
        this.address = address;
    }
}

// no JniNative annotation required
public class BazObject extends FooBarNative {
    public BazObject(long address) {
        super(address);
    }
}
```

Afterwards you can use any of the `@Jni-` annotations:

#### Java to C++

Annotate a method as `@JniBind` to bind a Java method to a native function - in Java 15, you can use multiline strings
to include multiple lines:

```java
@JniNative(FooBarLibrary.JNI_MODEL)
public final class FooBarLibrary {
    public static final String JNI_MODEL = "foobar/FooBarJNI";

    public static void init() { _init(); }
    @JniBind("""
            FooBar::preInit();
            FooBar::init();""")
    private static native void _init(); // note: the `_` prefix is automatically removed in generated natives 
}
```

A common situation is to wrap native objects in memory by creating a Java class which acts as a pointer, by storing
a `long address` which stores the object's address in memory. JniGlue has built-in support for this by annotating the
class with `@JniTypeMapping([native type])`, then using `@JniBindSelf` with `self` as a `[native type]*` (pointer):

```cpp
class Baz {
    public:
        int mNumber;
}
```

```java
@JniNative(FooBarLibrary.JNI_MODEL)
@JniTypeMapping("Baz")
public final class Baz {
    private long address;
    
    private Baz(long address) { this.address = address; }
    public static Baz ref(long address) { return address == 0 ? null : new Baz(address); }
    
    public int getNumber() { return _getNumber(address); }
    // `auto* self = (Baz*) _a;`
    @JniBindSelf("return self->mNumber;")
    private static native int _getNumber(long _a); // first argument must be `long _a`
}
```

#### C++ to Java

Annotate a class as `@JniReferenced` to generate a `jclass jni_[class name]` field, which is automatically found
and set to the corresponding Java class on load. You can then annotate (non-native) methods inside as `@JniCallback` to
generate `jmethodID jni_[class name]_[method name]` fields and `JNI_[class name]_[method name](JNIEnv* env, ...)`
functions to use in your code:

```java
@JniNative(FooBarLibrary.JNI_MODEL)
@JniReferenced
public abstract class LibraryCallback {
    public abstract void onError(ErrorCodeEnum errorCode);
    @JniCallback
    private void _onError(int errorCode) { onError(ErrorCodeEnum.values()[errorCode]); }
}
```

```cpp
class LibraryCallbackImpl : LibraryCallback {
    public:
        JNIEnv* env;
        jobject objectGlobalRef;
        
        LibraryCallbackImpl(JNIEnv* env, jobject obj) : env(env), objectGlobalRef(obj) {}
    
        void OnError(int errorCode) override {
            JNI_LibraryCallback_onError(env, objectGlobalRef, errorCode);
        }
}

// ...
    FooBarLibrary::setCallback(new LibraryCallbackImpl(env, object));
```

### Using the generated JNI code

A header file of the generated code will be output in the `build/generated/sources/annotationProcessor` directory. You can then
include this header file in another C++ Gradle subproject to generate a library file from the code:

```kotlin
plugins {
    id("cpp-library")
}

library {
    binaries.configureEach {
        val compileTask = compileTask.get()
        compileTask.dependsOn(":compileJava") // to generate the header file first
        compileTask.includes("${rootProject.buildDir}/generated/sources/annotationProcessor/java/main/foobar/")
    }
}
```

```cpp
#include <FooBarJNI.h>
```

## Building from source

```sh
git clone https://github.com/aecsocket/jni-glue
cd jni-glue
./gradlew build
```
