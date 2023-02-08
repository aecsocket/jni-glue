package io.github.aecsocket.jniglue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <b>Requires class annotated with {@link JniReferenced}:</b> annotates that a native {@code jmethodID}
 * field and function call will be created for this method, with the field name
 * {@code jni_[class name]_[method name]} and function name {@code JNI_[class name]_[method name]}.
 * The underscore prefix ({@code _}) will be stripped from method names, unless it is a constructor.
 * For constructors, the method name is {@code _init} with <b>two</b> total underscores.
 * <b>NOTE:</b> name collisions are not handled yet!
 * <p>
 * Generated C++ code:
 * <code>
 *     jmethodID jni_[class name]_[method name];
 *
 *     [return type] JNI_[class name]_[method name]
 *       (JNIEnv* env, (jobject _obj,) [parameters]) {
 *         // function body which calls the method
 *     }
 * </code>
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
public @interface JniCallback {}
