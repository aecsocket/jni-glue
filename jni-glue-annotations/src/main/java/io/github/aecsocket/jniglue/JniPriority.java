package io.github.aecsocket.jniglue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Determines at what point, relative to other classes, this class's Jni- annotations will be processed.
 * Any "base" classes should typically specify a lower {@link NativePriority}, like {@link NativePriority#EARLY}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface JniPriority {
    /**
     * The priority.
     */
    int value();
}
