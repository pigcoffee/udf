package com.udf.concurrent.service;

public interface IConsumer<T> {
	public void consume(T obj) throws Exception;
}
