package com.fmi.spo.determinant;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Matrix {
	
	private static int count;
	private final List<Double> determinant;
	private final double[][] matrix;
	private AtomicInteger counter;
	
	private static final int LOWEST_LEVEL = 10;
	
	public Matrix(double[][] matrix) {
		
		this.matrix = matrix;
		this.determinant = Collections.synchronizedList(new ArrayList<>());
		this.counter = new AtomicInteger(count);
	}
	
	public static void setCounter() {
		count = (int) Math.ceil(Math.log(ThreadPool.getThreadPoolSize())) + 1;
		if (count == 0) {
			count = 1;
		}
	}
	
	public double determinant() {
		
		if (matrix.length == 0) {
			return 0d;
		}
		if (matrix.length == 1) {
			return matrix[0][0];
		}
		if (matrix.length == 2) {
			return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
		}
		
		try {
			if (matrix.length > (LOWEST_LEVEL - 1) && ThreadPool.hasFreeThread()) {
				calcDeterminantMultythreaded();
				lock();
			} else {
				return calcDeterminant(matrix);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return determinant.stream().mapToDouble(x -> x).sum();
	}
	
	private void lock() throws InterruptedException {
		while(determinant.size() != matrix.length) {
			int diff = matrix.length - determinant.size();
			if (matrix.length > LOWEST_LEVEL) {
				int time = 1000 / Math.abs(diff - determinant.size());
				System.out.println("Sleeping for: " + time);
				Thread.sleep(time);
			}
		}
	}

	private void calcDeterminantMultythreaded() throws InterruptedException, ExecutionException {
		
		for (int level = 0; level < matrix[0].length; level++) {
			int lvl = level;
			double[][] subMatrix = buildSubMatrix(matrix, level);
			if (counter.get() > 0) {
				counter.decrementAndGet();
				ThreadPool.execute(() -> {
					Thread.yield();
					double result = new Matrix(subMatrix).determinant();
					determinant.add(matrix[0][lvl] * Math.pow (-1, lvl) * result);
					counter.incrementAndGet();
				});
			} else {
				processSubDeterminant(matrix, lvl);
			}
		}
	}

	private void processSubDeterminant(double[][] matrix, int level) {
		
		double[][] temp = buildSubMatrix(matrix, level);
		double subDeterminant = calcDeterminant (temp);
		determinant.add(matrix[0][level] * Math.pow (-1, level) * subDeterminant);
	}
	
	public double calcDeterminant(double[][] matrix) {
		
		if (matrix.length == 0) {
			return 0d;
		}
		if (matrix.length == 1) {
			return matrix[0][0];
		}
		if (matrix.length == 2) {
			return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
		}
		
		double result = 0d;
		for (int level = 0; level < matrix[0].length; level++) {
			double[][] temp = buildSubMatrix(matrix, level);
			result += matrix[0][level] * Math.pow (-1, level) * calcDeterminant (temp);
		}
		return result;
	}
	
	private double[][] buildSubMatrix(double[][] parent, int level) {
		
		double[][] temp = new double[parent.length - 1][parent[0].length - 1];
		for (int j = 1; j < parent.length; j++) {
			for (int k = 0; k < parent[0].length; k++) {
				if (k < level) {
					temp[j - 1][k] = parent[j][k];
				} else if (k > level) {
					temp[j - 1][k - 1] = parent[j][k];
				}
			}
		}
		return temp;
	}
}
