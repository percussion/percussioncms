package com.percussion.pso.restservice.impl;

public class ArchivedException extends Exception {
	private String message;

	public ArchivedException(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
