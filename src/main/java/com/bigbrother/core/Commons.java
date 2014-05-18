package com.bigbrother.core;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.concurrent.atomic.AtomicLong;

public class Commons {

    public static final String CONFIGURATION_FILE_NAME = "configuration.xml";

    public static final String OPTION_BIG_BROTHER_ENABLED = "bigbrother.enabled";

    public static final String PACKAGE_NAME_COM = "com";
    public static final String PACKAGE_NAME_BIGBROTHER = "bigbrother";
    public static final String PACKAGE_NAME_CORE = "core";
    public static final String PACKAGE_NAME_FULL_QUALIFIED_CORE =
            Joiner.on('.')
                    .join(Lists.newArrayList(PACKAGE_NAME_COM, PACKAGE_NAME_BIGBROTHER, PACKAGE_NAME_CORE));

    public static final String CLASS_NAME_LOGGERS_ACCESS_POINT = "LoggersAccessPointGenerated";
    public static final String CLASS_NAME_LOGGER = Logger.class.getSimpleName();
    public static final String CLASS_NAME_BIGBROTHER = Bigbrother.class.getSimpleName();

    public static final String METHOD_NAME_SYSTEM_CURRENT_TIME_MILLIS = "currentTimeMillis";
    public static final String METHOD_NAME_SYSTEM_NANO_TIME = "nanoTime";

    public static final String BIGBROTHER_METHOD_NAME_GET_INSTANCE = "getInstance";
    public static final String BIGBROTHER_METHOD_NAME_GET_ACCESS_POINT = "getAccessPoint";

    private static final AtomicLong counter = new AtomicLong(0);

    public static String generateLocalVariableName(String suffix) {
        return "generate" + "__" + counter.incrementAndGet() + "__" + suffix;
    }

}
