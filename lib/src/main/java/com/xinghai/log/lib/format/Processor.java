package com.xinghai.log.lib.format;

public interface Processor<T, R> {
    T process(R r);
}
