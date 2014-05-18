package com.bigbrother.core;

/**
 * @author alitvinov
 */
public final class Bigbrother {

    private static class BigbrotherHolder {
        private static final Bigbrother instance = new Bigbrother();
    }

    private final AccessPoint accessPoint;

    private Bigbrother()  {
        try {
            this.accessPoint = (AccessPoint) ClassLoader
                    .getSystemClassLoader()
                    .loadClass(Commons.PACKAGE_NAME_FULL_QUALIFIED_CORE + '.' + Commons.CLASS_NAME_LOGGERS_ACCESS_POINT)
                    .newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Error creating an instance of the class");
        }
    }

    public static Bigbrother getInstance() {
        return BigbrotherHolder.instance;
    }

    public AccessPoint getAccessPoint() {
        return accessPoint;
    }

}
