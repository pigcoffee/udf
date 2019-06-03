package com.udf.concurrent.service;

import java.util.List;

public interface IProducer<T> {
	public List<T> produce();
}
