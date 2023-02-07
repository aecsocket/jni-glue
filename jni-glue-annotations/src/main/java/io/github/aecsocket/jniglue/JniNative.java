package io.github.aecsocket.jniglue;

import java.lang.annotation.*;

/**
 * Determines that a class is part of a JNI native project model.
 * Any class using any Jni- annotation <b>must</b> also be annotated with this.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Inherited
public @interface JniNative {
    /**
     * The model this class is a part of.
     */
    String value();
}
