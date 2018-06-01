package com.fmi.spo.determinant;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
	
	private static int threadPoolSize;
	private static ExecutorService threadPool;
	
	public static void init(int threadPoolSize) {
		ThreadPool.threadPoolSize = threadPoolSize;
		threadPool = Executors.newFixedThreadPool(threadPoolSize);
		Matrix.setCounter();
	}
	
	public static void destroy() {
		
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Calculation of determinant was too long. ");
		}
	}
	
	public static void execute(Runnable task) {
		threadPool.execute(task);
	}
	
	public static synchronized boolean hasFreeThread() {
		return ((ThreadPoolExecutor) threadPool).getActiveCount() < threadPoolSize;
	}
	
	public static int getThreadPoolSize() {
		return threadPoolSize;
	}
}
