package io.github.aecsocket.jniglue;

import java.lang.annotation.*;

/**
 * Determines a specific JNI version that this library must be loaded with. Defaults to {@code JNI_VERSION_1_6}.
 * You must only have up to one of these annotations in a single JNI model.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Inherited
public @interface JniVersion {
    /**
     * The JNI version that this library reuqires.
     */
    String value();
}
