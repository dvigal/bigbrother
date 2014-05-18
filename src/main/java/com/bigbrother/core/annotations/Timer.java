package com.bigbrother.core.annotations;

import com.bigbrother.core.Logger;
import com.bigbrother.core.SimpleLogger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author alitvinov
 */
@Target({ElementType.METHOD/*, ElementType.TYPE*/})
@Retention(RetentionPolicy.SOURCE)
public @interface Timer {

    Class<? extends Logger> value() default SimpleLogger.class;

    // temporary solution
    TimeUnit granularity() default TimeUnit.MILLISECONDS;

    boolean enabled() default true;

}
