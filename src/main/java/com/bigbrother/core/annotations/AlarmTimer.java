package com.bigbrother.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author alitvinov
 */
@Target({ElementType.METHOD/*, ElementType.TYPE*/})
@Retention(RetentionPolicy.SOURCE)
public @interface AlarmTimer {
}
