package se.smartrefill.remoting.domain.exception;

public class InvalidSecurityCodeException extends ValidationException
{
  private static final long serialVersionUID = 1L;

  public InvalidSecurityCodeException()
  {
  }

  public InvalidSecurityCodeException(String paramString)
  {
    super(paramString);
  }
}