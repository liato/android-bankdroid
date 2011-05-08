package se.smartrefill.exception;

public class SmartrefillException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SmartrefillException() {
		this(null);
	}

	public SmartrefillException(String paramString) {
		super(paramString);
	}
}
