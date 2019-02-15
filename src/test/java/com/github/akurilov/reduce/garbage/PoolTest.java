package com.github.akurilov.reduce.garbage;

import org.junit.Test;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

import static org.junit.Assert.fail;

public class PoolTest {

	static final Supplier<String> FACTORY_METHOD = () -> Long.toHexString(System.nanoTime());
	static final int PARALLELISM = Runtime.getRuntime().availableProcessors();

	@Test
	public void noRecyclingTest()
	throws Exception {
		final var counter = new LongAdder();
		final var consume = (Runnable) () -> {
			String v;
			try(final BufferedWriter writer = Files.newBufferedWriter(Paths.get("/dev/null"))) {
				while(true) {
					v = FACTORY_METHOD.get();
					writer.append(v);
					counter.increment();
				}
			} catch(final Throwable thrown) {
				fail(thrown.getMessage());
			}
		};
		final ExecutorService executor = Executors.newFixedThreadPool(PARALLELISM);
		for(int i = 0; i < PARALLELISM; i ++) {
			executor.submit(consume);
		}
		final var startTimeMillis = System.currentTimeMillis();
		try {
			while(true) {
				TimeUnit.SECONDS.sleep(10);
				System.out.println(
					"Throughput: " + (1000.0 * counter.sum() / (System.currentTimeMillis() - startTimeMillis))
				);
			}
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void concurrentPoolTest()
	throws Exception {
		final var counter = new LongAdder();
		final var pool = Pool.concurrentPool(FACTORY_METHOD);
		final var consume = (Runnable) () -> {
			String v;
			try(final BufferedWriter writer = Files.newBufferedWriter(Paths.get("/dev/null"))) {
				while(true) {
					v = pool.lease();
					writer.append(v);
					pool.release(v);
					counter.increment();
				}
			} catch(final Throwable thrown) {
				fail(thrown.getMessage());
			}
		};
		final ExecutorService executor = Executors.newFixedThreadPool(PARALLELISM);
		for(int i = 0; i < PARALLELISM; i ++) {
			executor.submit(consume);
		}
		final var startTimeMillis = System.currentTimeMillis();
		try {
			while(true) {
				TimeUnit.SECONDS.sleep(10);
				System.out.println(
					"Pool size: " + pool.size() + ", Throughput: "
						+ (1000.0 * counter.sum() / (System.currentTimeMillis() - startTimeMillis))
				);
			}
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void fixedBlockingPoolTest()
	throws Exception {
		final var counter = new LongAdder();
		final var pool = Pool.fixedBlockingPool(1_000_000, FACTORY_METHOD);
		final var consume = (Runnable) () -> {
			String v;
			try(final BufferedWriter writer = Files.newBufferedWriter(Paths.get("/dev/null"))) {
				while(true) {
					v = pool.lease();
					writer.append(v);
					pool.release(v);
					counter.increment();
				}
			} catch(final Throwable thrown) {
				fail(thrown.getMessage());
			}
		};
		final ExecutorService executor = Executors.newFixedThreadPool(PARALLELISM);
		for(int i = 0; i < PARALLELISM; i ++) {
			executor.submit(consume);
		}
		final var startTimeMillis = System.currentTimeMillis();
		try {
			while(true) {
				TimeUnit.SECONDS.sleep(10);
				System.out.println(
					"Pool size: " + pool.size() + ", Throughput: "
						+ (1000.0 * counter.sum() / (System.currentTimeMillis() - startTimeMillis))
				);
			}
		} finally {
			executor.shutdownNow();
		}
	}
}
