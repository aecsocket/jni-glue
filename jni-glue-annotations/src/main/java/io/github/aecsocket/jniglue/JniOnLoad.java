package io.github.aecsocket.jniglue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds arbitrary native code to run when the native library is loaded.
 * Return {@code JNI_ERR} to mark an error.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface JniOnLoad {
    /**
     * The native code to include.
     */
    String value();
}
