package io.github.aecsocket.jniglue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates that a method maps directly to a JNI method.
 * JniGlue will automatically generate a function with the specified native code
 * according to <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/design.html">the Oracle JNI documentation</a>.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface JniBind {
    /**
     * The native code to include.
     */
    String value();
}
