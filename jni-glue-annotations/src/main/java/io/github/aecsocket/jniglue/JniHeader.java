package io.github.aecsocket.jniglue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds arbitrary code to the top of the generated native code file, directly after {@code jmethodID} field generation
 * but before {@code JNIInit}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface JniHeader {
    /**
     * The native code to include.
     */
    String value();
}
