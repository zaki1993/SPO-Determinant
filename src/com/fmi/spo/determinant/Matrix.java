package com.fmi.spo.determinant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Matrix {

	private static int topLevel;
	
	private final List<Double> determinant;
	private final double[][] matrix;
	
	public Matrix(double[][] matrix) {
		
		this.matrix = matrix;
		this.determinant = Collections.synchronizedList(new ArrayList<>());
	}
	
	public void setTopLevel(int level) {
		topLevel = level;
	}
	
	public double calcDeterminant() {
		
		determinant();
		while (determinant.size() != matrix.length) {
			
		}
		return determinant.stream().mapToDouble(x -> x).sum();
	}
	
	public void determinant() {
		
		if (matrix.length == 0) {
			determinant.add(0d);
		}
		if (matrix.length == 1) {
			determinant.add(matrix[0][0]);
		}
		if (matrix.length == 2) {
			determinant.add(matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]);
		} else {
			for (int level = 0; level < matrix[0].length; level++) {
				int lvl = level;
				double[][] subMatrix = buildSubMatrix(matrix, lvl);
				if(ThreadPool.getThreadPoolSize() >= topLevel && topLevel == matrix.length) {
					ThreadPool.get().execute(() -> {
						long start = System.currentTimeMillis();
						double result = new Matrix(subMatrix).calcDeterminant();
						determinant.add(matrix[0][lvl] * Math.pow (-1, lvl) * result);
						long end = System.currentTimeMillis();
						ThreadPool.processThreadInfo(Thread.currentThread(), end - start);
					});
				} else if (ThreadPool.hasFreeThread()) {
						ThreadPool.get().execute(() -> {
							long start = System.currentTimeMillis();
							double result = calcDeterminant(subMatrix);
							determinant.add(matrix[0][lvl] * Math.pow (-1, lvl) * result);
							long end = System.currentTimeMillis();
							ThreadPool.processThreadInfo(Thread.currentThread(), end - start);
						});
				} else {
					double result = calcDeterminant(subMatrix);
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
