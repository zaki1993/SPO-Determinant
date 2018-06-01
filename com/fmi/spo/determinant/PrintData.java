package com.fmi.spo.determinant;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PrintData {
	
	private static List<OutputStream> buffers = new ArrayList<>();
	private static boolean quite;
	
	public static void print(Object msg) throws IOException {
		for (OutputStream out : buffers) {
			out.write(msg.toString().getBytes());
			out.flush();
		}
	}
	
	public static void println() throws IOException {
		print("\n");
	}
	
	public static void println(Object msg) throws IOException {
		for (OutputStream out : buffers) {
			out.write((msg.toString() + "\n").getBytes());
			out.flush();
		}
	}
	
	public static void init(boolean quite, OutputStream ...buff) {
		PrintData.quite = quite;
		for (OutputStream out : buff) {
			buffers.add(out);
		}
	}
	
	public static void destroy() throws IOException {
		for (OutputStream out : buffers) {
			out.close();
		}
	}
}
