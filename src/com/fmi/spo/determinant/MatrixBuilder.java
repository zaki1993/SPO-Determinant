package com.fmi.spo.determinant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Properties;

public class MatrixBuilder {
	
	private double upperBound;
	private double lowerBound;
	private DecimalFormat df;
	
	public MatrixBuilder() {
		loadProperties();
	}
	
	private void loadProperties() {
		
		Properties prop = new Properties();
		try (InputStream in = new FileInputStream("matrix.properties")) {
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
		df = new DecimalFormat(prop.getProperty("decimal.format", "#.00"));
		upperBound = Double.parseDouble(prop.getProperty("upper.bound", "10"));
		lowerBound = Double.parseDouble(prop.getProperty("lower.bound", "-10"));
	}
	
	public double[][] buildMatrix(Map<String, String> commands) throws IOException {
		
		long start = System.currentTimeMillis();
		double[][] matrix;
		String randomMatrixSize = commands.get("-n");
		if (randomMatrixSize != null && !"".equals(randomMatrixSize)) {
			int matrixSize = 0;
			try {
				matrixSize = Integer.parseInt(randomMatrixSize);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Invalid number " + randomMatrixSize);
			}
			matrix = generateRandomMatrix(matrixSize);
		} else {
			String matrixFile = commands.get("-i");
			if (matrixFile != null && !"".equals(matrixFile)) {
				matrix = readMatrixFile(matrixFile);
			} else {
				throw new IllegalArgumentException("Please provide valid command -n [Number] or -i [Filename]");
			}
		}
		long end = System.currentTimeMillis();
		PrintData.println("Building the matrix took " + (end - start) + " ms");
		return matrix;
	}

	private double[][] readMatrixFile(String matrixFile) throws IOException {
		
		double[][] matrix;
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(matrixFile)))) {
			int matrixSize = Integer.parseInt(reader.readLine());
			matrix = new double[matrixSize][matrixSize];
			String matrixRow = null;
			int currentRow = 0;
			while ((matrixRow = reader.readLine()) != null) {
				String[] rowValues = matrixRow.split(" ");
				if (rowValues.length != matrixSize) {
					throw new IOException("Matrix in file " + matrixFile + " on row " + matrixRow + " has invalid size!");
				}
				for (int col = 0; col < matrixSize; col++) {
					matrix[currentRow][col] = Double.parseDouble(rowValues[col]);
				}
				currentRow++;
			}
			if (currentRow != matrixSize) {
				throw new IOException("Matrix in file " + matrixFile + " is invalid!");
			}
		} catch (NumberFormatException nfe) {
			String msg = nfe.getMessage();
			throw new IOException("Error while reading matrix from file " + matrixFile + ". Number was expected but got: " + msg.substring(msg.indexOf("\"")));
		}
		return matrix;
	}
	
	// В условието на задачата е казано да се изчисли детерминантата паралелно, но не става
	// ясно дали и генерирането на матрицата трябва да е по такъв начин
	// затова ползвам Math.random() вместо ThreadLocalRandom
	// Изчислих, че е по - бързо когато се генерират с една нишка
	private double[][] generateRandomMatrix(int matrixSize) {
		
		double[][] matrix = new double[matrixSize][matrixSize];
		for (int row = 0; row < matrixSize; row++) {
			for (int col = 0; col < matrixSize; col++) {
				matrix[row][col] = Double.parseDouble(df.format(Math.random() * (upperBound - lowerBound) + lowerBound));
			}
			//matrix[row] = ThreadLocalRandom.current().doubles().limit(matrixSize).toArray();
		}
		return matrix;
	}
}
