package se.smartrefill.remoting.domain.exception;

public class ValidationException extends SmartrefillException
{
  private static final long serialVersionUID = 1L;

  public ValidationException()
  {
  }

  public ValidationException(String paramString)
  {
    super(paramString);
  }
}