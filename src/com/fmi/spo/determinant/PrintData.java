package com.fmi.spo.determinant;

import java.io.IOException;
import java.io.OutputStream;

public class PrintData {
	
	private static OutputStream out;
	
	private static boolean quiet;
	
	public static void print(Object msg) {
		
		try {
			if (out != null) {
				out.write(msg.toString().getBytes());
				out.flush();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage()); // log on STDOUT only
		}
	}
	
	public static void println() {
		print("\n");
	}
	
	public static void println(Object msg) {
		
		try {
			if (out != null) {
				out.write((msg.toString() + "\n").getBytes());
				out.flush();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage()); // log on the STDOUT only
		}
	}
	
	public static void init(boolean quiet, OutputStream output) {
		PrintData.quiet = quiet;
		PrintData.out = output;
	}
	
	public static void printQuiet(Object msg) {
		if (!quiet) {
			println(msg);
		}
	}
	
	public static void destroy() throws IOException {
		if (out != null) {
			out.close();
		}
	}
}
