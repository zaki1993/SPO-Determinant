package com.fmi.spo.determinant;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
	
	private static int threadPoolSize;
	private static ExecutorService threadPool;
	private static Map<String, Long> threadsInfo;
	
	public static void init(int numberOfThreads) {
		
		if (numberOfThreads > 1) {
			ThreadPool.threadPoolSize = numberOfThreads;
			threadPool = Executors.newFixedThreadPool(numberOfThreads);
			threadsInfo = new HashMap<>();
			Matrix.setCounter();
		}
	}
	
	public static void destroy() {
		
		if (threadPool != null) {
			threadPool.shutdown();
			try {
				threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new ThreadPoolException("Calculation of determinant was too long. ");
			}
		}
	}
	
	public static void execute(Runnable task) {
		
		if (threadPool != null) {
			threadPool.execute(task);
		}
	}
	
	public static synchronized boolean hasFreeThread() {
		return threadPool == null ? false : ((ThreadPoolExecutor) threadPool).getActiveCount() < threadPoolSize;
	}
	
	public static int getThreadPoolSize() {
		return threadPoolSize;
	}
	
	public static void processThreadInfo(final Thread thread, long processTime) {
			
		String threadName = Thread.currentThread().getName();
		threadName = threadName.substring(threadName.indexOf("thread"));
		PrintData.printQuiet(threadName + " has worked " + processTime + " ms on a task!");		
		Long currentTime = threadsInfo.get(threadName);
		if (currentTime == null) {
			threadsInfo.put(threadName, processTime);
		} else {
			threadsInfo.put(threadName, currentTime + processTime);
		}
	}
	
	public static void printThreadInfo() {
		
		long processSum = 0l;
		for (Map.Entry<String, Long> threadInfo : threadsInfo.entrySet()) {
			PrintData.printQuiet(threadInfo.getKey() + " has worked for total " + threadInfo.getValue() + " ms!");
			processSum += threadInfo.getValue();
		}
		PrintData.printQuiet("Avarage work of thread is " + (processSum/threadPoolSize) + " ms!");
	}
}
