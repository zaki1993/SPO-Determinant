package com.fmi.spo.determinant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class Parser {
	
	private static final Set allowedCommands = new HashSet();
	
	static {
		allowedCommands.add("-n");
		allowedCommands.add("-i");
		allowedCommands.add("-o");
		allowedCommands.add("-q");
		allowedCommands.add("-t");
	}
	
	public static void main(String[] args) {
		
		try {
			Parser d = new Parser();
			Map commands = d.parseInput(args);
			d.evaluateCommands(commands);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				PrintData.destroy();
			} catch (IOException e) {;}
		}
	}

	private Map parseInput(String[] args) throws IOException {
		
		if (args.length < 2) {
			throw new IllegalArgumentException("Incorrect number of parameters provided. Expected 2, but got " + args.length);
		}
		String command = args[0];
		Map commands = new HashMap();

		for (int i = 0; i < args.length; i += 2) {	
			if (i >= args.length) {
				if (i + 1 >= args.length) {
					throw new IllegalArgumentException("Invalid command provided..!");
				} else {
					commands.put(args[i], null);
					continue;
				}
			}
			commands.put(args[i], args[i + 1]);
		}
		return commands;
	}

	private void evaluateCommands(Map<String, String> commands) throws IOException {
		
		double[][] matrix = new MatrixBuilder().buildMatrix(commands);
		int maxThreadCount = getMaxThreadCount(commands);
		boolean isQuietMode = getQuietMode(commands);
		String outputFile = commands.get("-o");
		
		if (outputFile != null) {
			PrintStream filePs = new PrintStream(new File(outputFile));
			PrintData.init(isQuietMode, filePs, System.out);
		} else {
			PrintData.init(isQuietMode, System.out);
		}
		
		long start = System.currentTimeMillis();
		double result = calcDeterminant(matrix, maxThreadCount);
		long end = System.currentTimeMillis();
		printMatrix(matrix, result, end - start);
	}

	private double calcDeterminant(double[][] matrix, int maxThreadCount) {
		
		double result = 0d;
		try {
			ThreadPool.init(maxThreadCount);
			Test m = new Test(matrix);
			if (maxThreadCount == 1) {
				result = m.calcDeterminant(matrix);
			} else {
				result = m.determinant();
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

	private void printMatrix(double[][] matrix, double determinant, long executionTime) throws IOException {
		
		for (int row = 0; row < matrix.length; row++ ) {
			for (int col = 0; col < matrix	.length; col++) {
				PrintData.print(matrix[row][col]);
				if (col < matrix.length - 1) {
					PrintData.print(" ");
				}
			}
			PrintData.println();
		}
		PrintData.println("Determinant: " + determinant);
		PrintData.println("Total execution time for current run(millis) " + executionTime);
	} 
}
