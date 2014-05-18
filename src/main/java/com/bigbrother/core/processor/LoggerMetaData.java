package com.bigbrother.core.processor;

import com.sun.tools.javac.tree.JCTree;

/**
 * @author alitvinov
 */
public class LoggerMetaData {
    private static int id = 0;

    public final Object value;

    public final JCTree tree;

    public final String genInstanceName;

    public final String fullQualifiedClassName;

    public final String logMethodName = "log";

    public final String enableMethodName = "enabled";

    public LoggerMetaData(Object value, JCTree tree) {
        this.value = value;
        this.tree = tree;
        this.fullQualifiedClassName = value.toString();
        this.genInstanceName = "logger_" + (++id);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
