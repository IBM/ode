package com.ibm.ode.lib.util;

/**
 * <pre>
 * The <code>ExceptionError</code> class is the subclass of Error. 
 * This class can be used to throw an Exception as an Error.
 * Many a time, we may have to throw a pre-defined Exception as 
 * an Error. But Java does not let us do this. By throwing an
 * Exception as an Error, you don't have to declare/define new
 * RuntimeException class or Error class
 *	<br>
 * The benefits of this class are
 * 1: you don't need to define a new Error class or RuntimeException class
 * 2: you don't need to declare Exception in throws clause
 * 3: you can throw your Exception as an Error
 * 4: The error message will be the same as it would be when you
 *    threw an Exception.
 *	<br>
 *<br>
 * Caution: This class is recommended for use only by advanced
 *          Java programmers. Use of this class may terminate JVM
 *          in a single threaded application or stop a running thread
 *          in a multi-threaded application and it might be difficult to
 *          recover the dead thread. Use this class with caution. Do not
 *          use this class to throw any RuntimeException classes
 *          as Errors. 
 * <br>
 * For Example, you would throw ClassNotFoundException
 * as an Error in a code snippet as below.
 *	<br>
 * public static Class loadClass( String classname )
 * {
 *    try
 *    {
 *       return Class.forName( classname ); 
 *    }
 *    catch( ClassNotFoundException cnfe )
 *    {
 *       throw new ExceptionError( cnfe );
 *    }
 * }
 * <br>
 * </pre>
 * @author  Chary Lingachary
 * @version 1.2 97/09/30
 */
public final class ExceptionError extends java.lang.Error
{
	/**
	 * Constructs a new <code>ExceptionError</code> 
	 * 
	 *	@param e Exception class you want to throw as Error
	 */
	public ExceptionError( Exception e )
	{
		if( e == null )
		{
			throw new java.lang.NullPointerException();
		}

		if( e instanceof RuntimeException )
		{
			throw new IllegalArgumentException("Argument can not be an instance of RuntimeException class");
		}
		e_ = e;
	}

	/**
	 * Returns a short description of Exception class
	 *
	 * @return  a string representation of this <code>Exception</code>.
	 */
	public String toString()
	{
		String eName = e_.getClass().getName();
		String eMessage = e_.getMessage();
		return (eMessage != null) ? (eName + ": " + eMessage) : eName;
	}

	/**
	 * Returns the detail message of the exception object
	 *
	 * @return  the detail message of exception object
	 */
	public String getMessage()
	{
		return e_.getMessage();
	}

	/**
	 * returns the runtime class associated with 
	 * the Exception
	 *
	 * @return class runtime class for Exception
	 */
	public Class getExceptionClass()
	{
		return e_.getClass();
	}

	/**
	 * returns the Exception Object
	 *
	 * @param Exception object
	 */
	public Exception getExceptionObject()
	{
		return e_;
	}

	/**
	 * Exception you wanted to throw.
	 */
	private Exception e_;
}

