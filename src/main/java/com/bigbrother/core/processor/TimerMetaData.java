package com.bigbrother.core.processor;

import com.bigbrother.core.Commons;

import javax.lang.model.element.Element;

/**
 * @author alitvinov
 */
public class TimerMetaData {

    public final Element element;

    public final String fullQualifiedConsumerName;

    public final String startTimeVariableName;

    public final String loggerVariableName;

    protected TimerMetaData(Element element, String fullQualifiedConsumerName) {
        this.element = element;
        this.fullQualifiedConsumerName = fullQualifiedConsumerName;
        this.startTimeVariableName = Commons.generateLocalVariableName("start");
        this.loggerVariableName = Commons.generateLocalVariableName("logger");
    }
}
