package com.fmi.spo.determinant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Matrix {

	private static int topLevel;
	private static AtomicInteger counter = new AtomicInteger(0);
	
	private final List<Double> determinant;
	private final double[][] matrix;
	private final AtomicInteger parentCounter = new AtomicInteger(0);
	private final AtomicInteger childCounter = new AtomicInteger(0);
	
	public Matrix(double[][] matrix) {
		
		this.matrix = matrix;
		this.determinant = Collections.synchronizedList(new ArrayList<>());
	}
	
	public void setTopLevel(int level) {
		topLevel = level;
	}
	
	public double calcDeterminant() {
		
		determinant();
		while (determinant.size() != matrix.length && matrix.length > 2) {}
		return determinant.stream().mapToDouble(x -> x).sum();
	}
	
	public void determinant() {
		
		if (matrix.length == 0) {
			determinant.add(0d);
		} else if (matrix.length == 1) {
			determinant.add(matrix[0][0]);
		} if (matrix.length == 2) {
			determinant.add(matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]);
		} else {
			for (int level = 0; level < matrix[0].length; level++) {
				int lvl = level;
				double[][] subMatrix = buildSubMatrix(matrix, lvl);
				boolean isTopLevel = topLevel == matrix.length;
				boolean isLowerLevel = counter.get() == topLevel && matrix.length > topLevel - 2 && ThreadPool.hasFreeThread();
				if ((isTopLevel && parentCounter.get() <= ThreadPool.getThreadPoolSize() / 2) ||
					(isLowerLevel && childCounter.get() <= ThreadPool.getThreadPoolSize() / 2)) {
					ThreadPool.execute(() -> {
						if (isTopLevel) {
							counter.incrementAndGet();
							parentCounter.incrementAndGet();
						} else {
							childCounter.incrementAndGet();
						}
						long start = System.currentTimeMillis();
						double result = new Matrix(subMatrix).calcDeterminant();
						long end = System.currentTimeMillis();
						ThreadPool.processThreadInfo(Thread.currentThread(), end - start);
						determinant.add(matrix[0][lvl] * Math.pow (-1, lvl) * result);
						if (isTopLevel) {
							parentCounter.decrementAndGet();
						} else {
							childCounter.decrementAndGet();
						}
					});
				} else {
					double result = subMatrix.length > 11 ? new Matrix(subMatrix).calcDeterminant() : calcDeterminant(subMatrix);
					determinant.add(matrix[0][lvl] * Math.pow (-1, lvl) * result);	
				}
			}
		}
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
