package com.bigbrother.core;

/**
 * @author alitvinov
 */
public final class SimpleLogger implements Logger {

    @Override
    public void log(Object value) {
        System.out.println("SimpleLogger >>> " + value);
    }

    @Override
    public boolean enabled() {
        return true;
    }
}
