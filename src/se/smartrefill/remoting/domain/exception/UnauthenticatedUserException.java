package se.smartrefill.remoting.domain.exception;

public class UnauthenticatedUserException extends SmartrefillException {
	private static final long serialVersionUID = 1L;

	public UnauthenticatedUserException() {
	}

	public UnauthenticatedUserException(String paramString) {
		super(paramString);
	}
}
