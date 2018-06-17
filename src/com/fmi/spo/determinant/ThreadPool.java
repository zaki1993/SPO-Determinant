package com.fmi.spo.determinant;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool {
	
	public enum Type {
		THREAD_POOL_EXECUTOR,
		FORK_JOIN,
		NEW_THREAD;
	}
	
	private static int threadPoolSize;
	private static ExecutorService threadPool;
	private static Map<String, Long> threadsInfo;
	private static Type type;
	private static AtomicInteger threadsSpawned;
	
	public static void init(int numberOfThreads, Type type) {
		
		if (numberOfThreads > 1) {
			ThreadPool.threadPoolSize = numberOfThreads;
			ThreadPool.type = type;
			threadPool = (type == Type.THREAD_POOL_EXECUTOR ? Executors.newFixedThreadPool(numberOfThreads) :
						  type == Type.FORK_JOIN ? Executors.newWorkStealingPool(numberOfThreads) : null);
			if (threadPool == null) {
				threadsSpawned = new AtomicInteger(0);
			}
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
		} else {
			threadsSpawned.incrementAndGet();
			new Thread(() -> {
				task.run();
				threadsSpawned.decrementAndGet();
			}).start();
		}
	}
	
	public static synchronized boolean hasFreeThread() {
		
		boolean result = threadPool != null && type != null;
		if (result) {
			if (type == Type.THREAD_POOL_EXECUTOR) {
				result = ((ThreadPoolExecutor) threadPool).getActiveCount() < threadPoolSize;
			} else {
				result = ((ForkJoinPool) threadPool).getActiveThreadCount() < threadPoolSize;
			}
		} else {
			result = threadsSpawned.get() < threadPoolSize;
		}
		return result;
	}
	
	public static int getThreadPoolSize() {
		return threadPoolSize;
	}
	
	public static void processThreadInfo(final Thread thread, long processTime) {
			
		String threadName = thread.getName();
		threadName = !threadName.equals("main") ? type == Type.THREAD_POOL_EXECUTOR ? 
					  threadName.substring(threadName.indexOf("thread")) :
					  "thread" + threadName.substring(threadName.lastIndexOf("-")) : threadName;
					  
		if (thread.getName().equalsIgnoreCase(threadName)) {
			threadName = "thread-" + new Random().nextInt(threadPoolSize);
		}
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
		PrintData.printQuiet("Avarage work of thread is " + (processSum / threadPoolSize) + " ms!");
	}
}
