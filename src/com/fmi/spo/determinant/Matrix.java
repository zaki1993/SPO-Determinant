package com.fmi.spo.determinant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class Matrix {

	private static final int RECURSIVE_BOTTOM_LEVEL = 11;
	private static final int LOWEST_LEVEL = 10;
	private static final int SINGLE_THREAD_LEVEL = 9;
	private static final int OPTIMAL_THREAD_SIZE = 8;
	
	private static int count;
	private final List<Double> determinant;
	private final double[][] matrix;
	private AtomicInteger counter;
	
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
		
		double result = 0d;
		if (matrix.length == 0) {
			result = 0d;
		}
		if (matrix.length == 1) {
			result = matrix[0][0];
		}
		if (matrix.length == 2) {
			result = matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
		} else {
			try {
				int levelAddOn = ThreadPool.getThreadPoolSize() > OPTIMAL_THREAD_SIZE ? (count / 2) : 0;
				if (matrix.length > RECURSIVE_BOTTOM_LEVEL + levelAddOn ) {
					result = runInDepth();
				} else if (matrix.length > SINGLE_THREAD_LEVEL) {
					calcDeterminantMultithreaded();
					lock();
					result = determinant.stream().mapToDouble(x -> x).sum();
				} else {
					result = calcDeterminant(matrix);
				}
			} catch (Exception e) {
				throw new DeterminantCalculationException(e);
			}
		}

		return result;
	}
	
	private double runInDepth() {

		double result = 0d;
		for (int level = 0; level < matrix[0].length; level++) {
			double[][] subMatrix = buildSubMatrix(matrix, level);
			double subDet = new Matrix(subMatrix).determinant();
			result += matrix[0][level] * Math.pow (-1, level) * subDet;
		}
		return result;
	}

	private void lock() throws InterruptedException {
		while(determinant.size() != matrix.length) {
			if (matrix.length > LOWEST_LEVEL) {
				int time = 100;
				ThreadPool.processThreadInfo(Thread.currentThread(), time);
				Thread.sleep(time);
			}
		}
	}

	private void calcDeterminantMultithreaded() throws InterruptedException, ExecutionException {

		for (int level = 0; level < matrix[0].length; level++) {
			int lvl = level;
			double[][] subMatrix = buildSubMatrix(matrix, level);
			if (counter.get() > 0 && ThreadPool.hasFreeThread()) {
				counter.decrementAndGet();
				ThreadPool.execute(() -> {
					long start = System.currentTimeMillis();
					double result = new Matrix(subMatrix).determinant();
					determinant.add(matrix[0][lvl] * Math.pow (-1, lvl) * result);
					counter.incrementAndGet();
					long end = System.currentTimeMillis();
					ThreadPool.processThreadInfo(Thread.currentThread(), end - start);
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
		
		double result = 0d;
		if (matrix.length == 0) {
			result = 0d;
		}
		if (matrix.length == 1) {
			result = matrix[0][0];
		}
		if (matrix.length == 2) {
			result = matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
		} else {
			for (int level = 0; level < matrix[0].length; level++) {
				double[][] temp = buildSubMatrix(matrix, level);
				result += matrix[0][level] * Math.pow (-1, level) * calcDeterminant (temp);
			}
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
