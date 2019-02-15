package com.github.akurilov.reduce.garbage;

import java.util.Queue;
import java.util.function.Supplier;

public class QueuePoolImpl<T>
implements Pool<T> {

	protected final Queue<T> queue;
	protected final Supplier<T> factoryMethod;
	protected final boolean cleanOnClose;

	public QueuePoolImpl(final Queue<T> queue, final Supplier<T> factoryMethod) {
		this(queue, factoryMethod, false);
	}

	QueuePoolImpl(final Queue<T> queue, final Supplier<T> factoryMethod, final boolean clearOnClose) {
		this.queue = queue;
		this.factoryMethod = factoryMethod;
		this.cleanOnClose = clearOnClose;
	}

	@Override
	public T lease() {
		final T instance = queue.poll();
		return instance == null ? factoryMethod.get() : instance;
	}

	@Override
	public void release(final T o) {
		queue.offer(o);
	}

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public void close() {
		if(cleanOnClose) {
			queue.clear();
		}
	}
}
