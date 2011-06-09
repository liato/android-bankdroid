package se.smartrefill.exception;

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