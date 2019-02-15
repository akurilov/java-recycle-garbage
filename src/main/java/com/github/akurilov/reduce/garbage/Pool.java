package com.github.akurilov.reduce.garbage;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public interface Pool<T>
extends AutoCloseable {

	T lease();

	void release(final T o);

	int size();

	static <T> Pool<T> concurrentPool(final Supplier<T> factoryMethod) {
		return new QueuePoolImpl<>(new ConcurrentLinkedQueue<>(), factoryMethod, true);
	}

	static <T> Pool<T> fixedBlockingPool(final int limit, final Supplier<T> factoryMethod) {
		return new QueuePoolImpl<>(new ArrayBlockingQueue<>(limit), factoryMethod, true);
	}
}
