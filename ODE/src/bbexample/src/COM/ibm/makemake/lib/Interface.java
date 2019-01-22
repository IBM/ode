package COM.ibm.makemake.lib;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * This class is used as a means to interact with the user -
 * either to display messages or get input.
 *
 * Messages are displayed based on the messaging state.
 * The messaging state i.e "quiet" or "normal" or "verbose" or "debug" should
 * be set in the main calling program - after parsing the command line. If not
 * set, the default state is the "normal" state.
**/
public class Interface
{
  public static final int QUIET = 1;
  public static final int NORMAL = 2;
  public static final int VERBOSE = 3;
  public static final int DEBUG = 4;
  public static final int MINERRORCODE = 0;
  public static final int MAXERRORCODE = 255;

  public static int messagingLevel = NORMAL;   // default is normal
  private static boolean debugState = false;
  private static boolean infoState = false;
  private static boolean auto = false;


  /**
   * Set the messaging state.
   *
   * @param messagingState The desired level of messaging - "quiet", "normal",
   * "verbose" or "debug".
  **/
  public static void setState( String messagingState )
  {
    if (messagingState != null) // only if something specified
    {
      if (messagingState.startsWith( "-" )) // eliminates the "-" as prefix
        messagingState = messagingState.substring( 1 );

      if (messagingState.equals( "verbose" ))
        messagingLevel = VERBOSE;
      else if (messagingState.equals( "normal" ) && !debugState)
        messagingLevel = NORMAL;
      else if (messagingState.equals( "quiet" ) && !debugState)
        messagingLevel = QUIET;
      else if (messagingState.equals( "debug" ))
      {
        messagingLevel = VERBOSE;
        debugState = true;
      }
      else if (messagingState.equals( "info" ))
        infoState = true;
      else if (messagingState.equals( "auto" ))
        auto = true;
      else if (messagingState.equals( "noauto" ))
        auto = false;
      else
        messagingLevel = NORMAL;
    }
  }


  /**
   * This is a general purpose print method. It prints out messages when
   * in the "normal" or the "verbose" mode. No new line char is appended.
   *
   * @param message Message to be printed.
   * @param newLine Should a new line char be added?
  **/
  public static void print( String message, boolean newLine )
  {
    if (messagingLevel >= NORMAL && message != null)
    {
      if (newLine)
        System.out.println( message );
      else
        System.out.print( message );
    }
  }


  /**
   * Print a message.
   *
   * @param message Message to be printed.
  **/
  public static void print( String message )
  {
    print( message, true );
  }


  /**
   * This prints out messages to the standard error stream.
   * in the "normal" or the "verbose" mode.
   *
   * @param message Message to be printed.
   * @param newLine Should a new line char be added?
  **/
  public static void printToErrStream( String message, boolean newLine )
  {
    if (messagingLevel >= NORMAL && message != null)
    {
      if (newLine)
        System.err.println( message );
      else
        System.err.print( message );
    }
  }


  /**
   * This prints out messages to the standard err stream.
   * in the "normal" or the "verbose" mode.
   *
   * @param message Message to be printed.
  **/
  public static void printToErrStream( String message )
  {
    printToErrStream( message, true );
  }


  /**
   * This is the main general purpose print method. It prints out messages when
   * in the "verbose" mode.
   *
   * @param message Message to be printed.
  **/
  public static void printVerbose( String message )
  {
    if (messagingLevel == VERBOSE && message != null)
      System.out.println( "> " + message );
  }


  /**
   * Prints out messages if messaging level not set or if messaging level
   * is "normal" or "verbose"
   *
   * @param message Message to be printed.
  **/
  public static void printWarning( String message )
  {
    if (messagingLevel >= NORMAL && message != null)
      System.err.println( ">> WARNING: " + message );
  }


  /**
   * Prints out messages under any messaging level.
   *
   * @param message Message to be printed.
  **/
  public static void printError( String message )
  {
    if (message != null)
      System.err.println( ">> ERROR: " + message );
  }


  /**
   * Prints out messages under any messaging level.
   *
   * @param message Message to be printed.
  **/
  public static void printFatalError( String message )
  {
    if (message != null)
      System.err.println( ">> FATAL ERROR: " + message );
  }


  /**
   * Prints out the message - regardless of the messaging state.
   * @param message Message to be printed.
  **/
  public static void printAlways( String message )
  {
    if (message != null)
      System.out.println( message );
  }


  /**
   * Prints out a Diagnostic message - only if verbose is specified
   * (verbose state is automatically set if the debug flag is set)
   * @param message Message to be printed.
  **/
  public static void printDiagnostic( String message )
  {
    if (messagingLevel == VERBOSE && message != null)
      System.out.println( ">> DIAGNOSTIC: " + message );
  }


  /**
   * If the debug state is set - then print out debug info (one message)
   * @param message Message to be printed.
  **/
  public static void printDebug(String message)
  {
    if (debugState && (message != null))
      System.out.println(">> DEBUG INFO: " + message);
  }


  /**
   * If the debug state is set print out debug info (more than one message)
   *
   * @param message Message to be printed.
  **/
  public static void printDebug(String messages[])
  {
    if (debugState && (messages != null))
    {
      System.out.println(">> DEBUG INFO: ");
      for (int count = 0; count < messages.length; count++)
      {
        System.out.println("  > " + messages[count]);
      }
    }
  }


  /**
   * When the "-info" state is specified, this method can be used to print out
   * a message and return a boolean
   * value stating whether the "-info" option was set or not.
   * One way to use this
   * is to have the following piece of code at the start of all
   * the major methods:<BR>
   * <CODE>
   *       if (Interface.printInfo( "Your info" ))<BR>
   *         return;
   * </CODE>
   * @param message The information that is to be displayed.
   * @return True is the "-info" state was set, else false.
  **/
  public static boolean printInfo(String message)
  {
    if (infoState)
      System.out.println(message);

    return infoState;
  }


  /**
   * Print an array of strings, each one separated
   * by a newline.
   *
   * @param array The array of strings.
  **/
  public static void printArray( String[] array )
  {
    if (array != null)
      for (int i=0; i < array.length; ++i)
        Interface.print( array[i] );
  }


  /**
   * Print an array of strings, each one separated
   * by a specified separator.
   *
   * @param array The array of strings.
  **/
  public static void printArray( String[] array, String separator )
  {
    if (array != null)
      for (int i=0; i < array.length; ++i)
      {
        System.out.print(array[i]);
        if (i != (array.length - 1))
           System.out.print(separator);
      }
    System.out.println();
  }


  /**
   * Print an array of strings, each one separated
   * by a specified separator.
   *
   * @param array The array of strings.
  **/
  public static void printArrayWithDefault( String[] array,
      String separator, int def )
  {
    if (array != null)
    {
      for (int i=0; i < array.length; ++i)
      {
        if (i == def)
          System.out.print("[" + array[i] + "]");
        else
          System.out.print(array[i]);

        if (i != (array.length - 1))
          System.out.print(separator);
      }
    }
    System.out.println();
  }


  /**
   * A plain simple exit with the specified status.
   *
   * @param status Upon exit return this number.
  **/
  public static void quit(int status)
  {
    System.exit(normalizeErrorCode(status));
  }


  /**
   * A exit routine which prints out a message with exit status before exiting.
   * @param message Message to be displayed before exiting.
   * @param status Upon exit return this number.
  **/
  public static void quit(String message, int status)
  {
    System.err.println("Exiting[" + status + "]: " + message);
    System.exit(normalizeErrorCode(status));
  }


  /**
   * A plain simple exit with the specified status.
   * @param status Upon exit return this number.
  **/
  public static void quitWithErrMsg(String message, int status)
  {
    printError(message);
    System.exit(normalizeErrorCode(status));
  }


  /**
   * Use this method if one needs to ask the user to input information. The
   * prompt field is what is displayed to the user and his/her response is
   * returned as a string. The boolean variable infinite is set to true if
   * you want to force the user to enter some data - basically the user is
   * prompted endlessly unless he enters some data - otherwise he is just
   * prompted once and if he enters no data null is returned.
   * @param prompt Prompt the user to respond
   * @param infinite True if a response is required, else false.
   * @return The users response or null
   * (if infinite is false and the user just hits enter).
  **/
  public static String getResponse(String prompt, boolean infinite)
  {
    StringBuffer response = new StringBuffer();

    do
    {
      System.out.print (prompt + ": ");
      if (auto)
      {
        System.out.println("Using default...");
        return null;
      }

      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      try
      {
        response = new StringBuffer( br.readLine() );
      }
      catch (IOException e)
      {
        if (!infinite)
          return null;
      }
      catch (RuntimeException e)
      {
        if (!infinite)
          return null;
      }
    } while ((response.length() == 0) && infinite);
      // if infinite - force user to respond.

    if (response.length() == 0)
      return null;
    else
      return response.toString();
  }


  /**
   * Used to get a yes/no response from the user. The default response can also
   * be specified (returned when the user just hits enter).
   * Only the first letter of the input is checked.
   * @param prompt Message displayed to the user (i.e., the question).
   * @param defaultResponse The defalut value to return upon "garbage" input.
   * @return True or false based on the user's response.
  **/
  public static boolean getConfirmation(String prompt, boolean defaultResponse)
  {
    StringBuffer response = null;

    System.out.print (prompt + ": ");

    if (auto)
    {
      Interface.printAlways("Yes (\"auto\" specified.)");
      return true;
    }

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    try
    {
      response = new StringBuffer (br.readLine());
    }
    catch(IOException e)
    {}
    catch(RuntimeException r)
    {}

    if (response == null)   // if no input return default
      return defaultResponse;

    // check if response is to be classified as a "YES" or "NO"
    if (response.toString().startsWith("Y") ||
        response.toString().startsWith("y"))
      return true;
    else if (response.toString().startsWith("N") ||
        response.toString().startsWith("n"))
      return false;
    else
      return defaultResponse;
  }


  /**
   * Used to FORCE a yes/no response from the user. The default response can
   * also be specified (returned when the user just hits enter).
   * Only the first letter of the input is checked.
   *
   * @param prompt Displays a message to the user
   * @param defaultResponse The defalut value to return upon "garbage" input.
   * @return True or false based on the user's response.
  **/
  public static boolean getConfirmation(String prompt, boolean infinite,
      boolean defaultResponse)
  {
    StringBuffer response = null;

    while ((response == null) && infinite)
    {
      System.out.print (prompt + ": ");

      if (auto)
      {
        Interface.printAlways("Yes (\"auto\" specified.)");
        return true;
      }

      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      try
      {
        response = new StringBuffer (br.readLine());
      }
      catch(IOException e)
      {}
      catch(RuntimeException r)
      {}
    }
    // check if response is to be classified as a "YES" or "NO"
    if (response.toString().startsWith("Y") ||
        response.toString().startsWith("y"))
      return true;
    else if (response.toString().startsWith("N") ||
        response.toString().startsWith("n"))
      return false;
    else
      return defaultResponse;
  }


  /**
   * A small "menuing" method.
   * Prints the input String array in the form of a menu and returns the user's
   * selection - as an integer. The last item is always the Exit item
   * If the user inputs an invalid number, he is repeatedly prompted for the
   * right selection.
   * @param items A String array which represents the menu items.
   * @param title Prints a message above the menu.
   * @return Returns the choice input by the user. In case of error -1 is
   * returned.
  **/
  public static int getSelectionFromMenu(String[] items, String title)
  {
    int selection = -1; // assume the worst

    System.out.println(title);

    // print out items in a menu form.
    for (int count = 0; count < items.length; count++)
      System.out.println(count + ". " + items[count]);

    System.out.println(items.length + ". Exit");

    // try to get the user's selection
    try
    {
      selection = (new Integer(getResponse("Selection", true))).intValue();
    }
    catch(NumberFormatException e)
    {
      selection = -1;
    }

    // if user enters invalid input keep pestering him/her repeatedly
    while ((selection < 0) || (selection > items.length))
    {
      try
      {
        selection = (new Integer( getResponse(
            "Invalid selection. Please enter proper selection",
            true ) )).intValue();
      }
      catch (NumberFormatException e)
      {
        selection = -1;
      }
    }

    return selection;
  }


  /**
   * Check if the return code is within a certain boundary.
   *
   * @param code The error code to check.
   * @return The code if it's within the proper range.  Otherwise,
   * 1 is returned.
  **/
  private static int normalizeErrorCode( int code )
  {
    if (code > MAXERRORCODE || code < MINERRORCODE)
      return 1;
    return code;
  }
}
