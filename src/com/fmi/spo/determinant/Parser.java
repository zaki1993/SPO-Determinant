package com.fmi.spo.determinant;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fmi.spo.determinant.ThreadPool.Type;

public class Parser {
	
	private static final Set<String> allowedCommands = new HashSet<>();
	
	static {
		allowedCommands.add("-n");
		allowedCommands.add("-i");
		allowedCommands.add("-o");
		allowedCommands.add("-q");
		allowedCommands.add("-t");
		allowedCommands.add("-p");
	}
	
	public void parse(String[] args) {
		
		try {
			Map<String, String> commands = parseInput(args);
			evaluateCommands(commands);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		} finally {
			try {
				PrintData.destroy();
			} catch (IOException e) {;}
		}
	}

	private Map<String, String> parseInput(String[] args) throws IOException {
		
		if (args.length < 2) {
			throw new IllegalArgumentException("Incorrect number of parameters provided. Expected at least 2, but got " + args.length);
		}
		Map<String, String> commands = new HashMap<>();

		for (int i = 0; i < args.length; i += 2) {	
			if (i >= args.length - 1) {
				if (args[i].equals("-q")) {
					commands.put(args[i], null);
					continue;
				} else {
					throw new IllegalArgumentException("Invalid command provided..!");
				}
			}
			if (args[i].equals("-q")) {
				commands.put(args[i], null);
				if (i != args.length - 1) {
					i--;
				}
			} else {
				commands.put(args[i], args[i + 1]);
			}
		}
		return commands;
	}

	private void evaluateCommands(Map<String, String> commands) throws IOException {

		int maxThreadCount = getMaxThreadCount(commands);
		boolean isQuietMode = getQuietMode(commands);
		String outputFile = commands.get("-o");
		
		OutputStream out = System.out;
		if (outputFile != null) {
			out = new PrintStream(new File(outputFile));
		}
		PrintData.init(isQuietMode, out);
		
		double[][] matrix = new MatrixBuilder().buildMatrix(commands);
		printMatrix(matrix);
		
		ThreadPool.Type type = findParallelismType(commands);
		
		long start = System.currentTimeMillis();
		double result = calcDeterminant(matrix, maxThreadCount, type);
		long end = System.currentTimeMillis();
		PrintData.println("Total execution time for current run " + (end - start) + " ms");
		PrintData.println("Determinant: " + result);
		if (outputFile != null) {
			System.out.println("Please check " + outputFile + " for more information..!");
		}
	}

	private Type findParallelismType(Map<String, String> commands) {
		
		ThreadPool.Type type = Type.THREAD_POOL_EXECUTOR;
		
		String typeValue = commands.get("-p");
		if (typeValue != null) {
			try {
				type = ThreadPool.Type.valueOf(typeValue);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Invalid parallelism type " + typeValue);
			}
		}
		
		return type;
	}

	private double calcDeterminant(double[][] matrix, int maxThreadCount, ThreadPool.Type type) {
		
		double result = 0d;
		try {
			ThreadPool.init(maxThreadCount, type);
			Matrix m = new Matrix(matrix);
			if (maxThreadCount == 1) {
				result = m.calcDeterminant(matrix);
			} else {
				m.setTopLevel(matrix.length);
				result = m.calcDeterminant();
				ThreadPool.printThreadInfo();
			}
		} finally {
			ThreadPool.destroy();
		}
		return result;
	}

	private boolean getQuietMode(Map<String, String> commands) {
		return commands.containsKey("-q");
	}

	private int getMaxThreadCount(Map<String, String> commands) {
		
		int maxThreadCount = 1;
		String threadCommand = commands.get("-t");
		if (threadCommand != null && !"".equals(threadCommand)) {
			try {
				maxThreadCount = Integer.parseInt(threadCommand);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Invalid numeric value for command -t " + threadCommand);	
			}
			if (maxThreadCount < 0) {
				throw new IllegalArgumentException("Thread count cannot be negative number!");
			}
		}
		return maxThreadCount;
	}

	private void printMatrix(double[][] matrix) {
		
		PrintData.println("Matrix is: ");
		for (int row = 0; row < matrix.length; row++ ) {
			for (int col = 0; col < matrix	.length; col++) {
				PrintData.print(String.format("%-6s", matrix[row][col]));
				if (col < matrix.length - 1) {
					PrintData.print(" ");
				}
			}
			PrintData.println();
		}
	} 
}
