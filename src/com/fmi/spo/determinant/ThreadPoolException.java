package com.fmi.spo.determinant;

public class ThreadPoolException extends RuntimeException {
	public ThreadPoolException(Throwable e) {
		super(e);
	}
	
	public ThreadPoolException(String msg) {
		super(msg);
	}
}
