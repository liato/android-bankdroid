package se.smartrefill.remoting.domain.exception;

public class InvalidLoginException extends SmartrefillException{
	private static final long serialVersionUID = 1L;

	public InvalidLoginException() {
	}

	public InvalidLoginException(String paramString) {
		super(paramString);
	}
}