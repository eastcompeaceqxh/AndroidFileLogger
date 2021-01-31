package com.xinghai.log.lib.format;


import com.xinghai.log.lib.engine.LogItem;

public interface IMessageProcessorFactory {

    Processor<String, LogItem> createMessageProcessor();
}
