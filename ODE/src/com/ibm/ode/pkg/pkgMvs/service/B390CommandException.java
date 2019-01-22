/*******************************************************************************
 *
 ******************************************************************************/
package com.ibm.ode.pkg.pkgMvs.service;

/**
 *
 */
public class B390CommandException extends Exception 
{
  private int returnCode = incomplete; //incomplete.
  public static final int success = 0; //Success.
  public static final int error = 1; //Process error.
  public static final int syntax = 2; //Command syntax error.
  public static final int passwd = 3; //Invalid MVS host password.
  public static final int setup = 4; //Setup information incomplete.
  public static final int mvsError = 5; //mvs host error.
  public static final int libError = 6; //library error.
  public static final int errors = 7; //the number of exceptions.
  public static final int incomplete = errors + 1; //incomplete return code.
  private static String[] exceptionText = new String[incomplete + 1];
  {
    exceptionText[success] = "Command succeded with return code=" + success;
    exceptionText[error] = "B390 error " + error;
    exceptionText[syntax] = "B390 command syntax error " + syntax;
    exceptionText[passwd] = "B390 password " + passwd;
    exceptionText[setup] = "B390 setup incomplete " + setup;
    exceptionText[mvsError] = "B390 server error " + mvsError;
    exceptionText[libError] = "CMVC error " + libError;
    exceptionText[errors] = "Unexpected return code ";
    exceptionText[incomplete] = "Command did not complete";
  }
  
  private B390CommandException( int returnCode )
  {
    super();
    this.returnCode = returnCode;
  }
  
  B390CommandException( String message )
  {
    super(message);
    this.returnCode = 0;
  }
  
  public static void handleReturnCode( int returnCode ) 
    throws B390CommandException
  {
    switch (returnCode)
    {
      case B390CommandException.success: 
      {
        return;
      }
      default:
      {
        throw new B390CommandException(returnCode);
      }
    }
  }
  
  public String getMessage() 
  {
    int returnCode = getReturnCode();
    int messageCode = returnCode;
    if (returnCode > errors || returnCode <= success)
    {
      messageCode = errors;
    }
    String message = super.getMessage();
    if (message != null)
    {
      return message + "\n" + exceptionText[messageCode] + returnCode;
    }
    else
    {
      return exceptionText[messageCode] + returnCode;
    }
  }
  
  public int getReturnCode() 
  {
    return this.returnCode;
  }
}