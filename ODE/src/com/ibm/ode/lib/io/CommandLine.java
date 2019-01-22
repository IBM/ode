package com.ibm.ode.lib.io;

import java.util.Vector;

import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.io.UsagePrintable;
import com.ibm.ode.lib.string.StringTools;
import com.ibm.ode.lib.string.PlatformConstants;
import com.ibm.ode.lib.io.Version;


/**
 * This class can be used to specify the syntax of a command line and
 * then used to check if the
 * input command line matches that syntax.
 * The command line arguments have been divided into 3 types -
 *
 * 1. States - These are arguments of the type "-quiet", "-verbose", "-usage",
 * etc.  They are basically flags and are NOT followed by related variables.
 *
 * 2. Qualified Variables - These are arguments that occur in "pairs".
 * For example "-sb <sb_name>" or "-sb<sb_name>"
 *
 * 3. Unqualified Variables - This is the last kind. These variables occur
 * by themselves without any qualifiers.
 *
 * This class has two constructors - Using the first one means that you
 * have to do all the
 * processing yourself i.e calling checkSyntax, calling setMessagingState,
 * etc. This
 * is recommended only if your needs differ from the normal use.
 * The second constructor encompasses the commonl functionality for all tools.
 * Upon calling
 * the constructor it automatically checks the syntax, checks a few
 * common flags like "-usage",
 * "-version", "-quiet", "-normal", "-verbose", "-debug", and prints out
 * the usage or version
 * if needed. To use this constructor the calling class MUST implement
 * the "UsagePrintable" interface
**/
public class CommandLine
{
  private static final int SIMPLE = 1;
  private static final int COMBINATION = 2;

  private String[] arguments;
  private Vector unqualifiedVariables = new Vector();
  private String[] states;
  private String[] qualifiedVars;
  private boolean unqualifiedVars = true;


  /**
   * The constructor takes 4 arguments. The first 3 together represent the
   * correct
   * syntax and the last one is the actual input command line which has to be
   * checked. The syntax is specified using 2 string arrays and one
   * boolean variable.
   * The two arrays are used to specify the first two kinds of variables and the
   * boolean variable is used to specify if any variable of the third
   * kind is expected.
   * For the qualified variables array only the qualifier has to be specified.
   * For example - "mksb [ -debug |-quiet ] -back <xxx> -dir <yyy> <sb_name>"
   *          For the above example the arguments to the constructor are -
   *                 1. {"-debug", "-quiet"}
   *                 2. ("-back", "-dir")
   *                 3. true
   * @param states A string array containing all the states in the command line.
   * @param qualifiedVars Contains all the qualified variables' qualifiers.
   * @param unqualifiedVars Specify if unqualified variables are to be expected.
   * @param arguments The actual input command line.
  **/
  public CommandLine(String[] states, String[] qualifiedVars,
      boolean unqualifiedVars, String[] arguments)
  {
    this.states = states;
    this.qualifiedVars = qualifiedVars;
    this.unqualifiedVars = unqualifiedVars;
    this.arguments = arguments;
  }


  /**
   * When using this constructor you do not need to specify the following
   * states: "-quiet", "-normal", "-verbose", "-debug", "-info",
   * "-version", "-usage", and "-rev".
   * They are automatically assumed to be part of every command's syntax.
   * @param needArguments A boolean variable which indicates whether
   * the command needs arguments(true) to work or not (false).
   * @param callingObject A reference to the object that called this object.
  **/
  public CommandLine(String[] states, String[] qualifiedVars,
      boolean unqualifiedVars, String[] arguments,
      boolean needArguments, UsagePrintable callingObject)
  {
    this (states, qualifiedVars, unqualifiedVars, arguments);
    this.states = getStates(states);

    if (!checkSyntax() || isState("-usage") ||
       (needArguments && (getNumberOfArguments() == 0)))
    {
      callingObject.printUsage();
      Interface.quit(1);
    }

    if (isState("-version") || isState("-rev"))
    {
      printVersion(stripClassName(callingObject.getClass().getName()));
      Interface.quit(1);
    }

    setMessagingState();
  }



  /**
   * When you need to specify mutually exclusive or "must occur together"
   * args.
   * @param mutexes A set of arrays of mutually exclusive variables
   * @param nonMutexes A set of arrays of variables that must occur together
  **/
  public CommandLine(String[] states, String[] qualifiedVars,
      boolean unqualifiedVars, String[] arguments,
      boolean needArguments, Vector mutexes,
      Vector nonMutexes,UsagePrintable callingObject)
  {
    this( states, qualifiedVars, unqualifiedVars, arguments,
        needArguments, callingObject );

    if (mutexes != null && mutexes.size() > 0) // check for mutual exclusions
    {
      for (int count = 0; count < mutexes.size(); count++)
      {
        String[] mutexVars = (String[])mutexes.elementAt(count);

        int found = 0;
        // for each set of variables
        for (int i = 0; i < mutexVars.length; i++)
        {
          if (isIn(mutexVars[i], arguments) != -1)
            ++found;  // check is more than one are present
        }

        if (found > 1)
        {
          Interface.printAlways("These variables are mutually exclusive: ");
          Interface.printArray(mutexVars, ", ");
          callingObject.printUsage();
          Interface.quit(1);
        }
      }
    }
    // check for variables that must occur together
    if ((nonMutexes != null) && (nonMutexes.size() > 0))
    {
      for (int count = 0; count < nonMutexes.size(); count++)
      {
        String[] nonMutexVars = (String[])nonMutexes.elementAt(count);

        int found = 0;
        // for each set of variables
        for (int i = 0; i < nonMutexVars.length; i++)
        {
          if (isIn(nonMutexVars[i], arguments) != -1)
            ++found; // check if all of them occur
        }

        if ((found != nonMutexVars.length) && (found != 0))
        {
          Interface.printAlways(
              "These variables must always be specified together: ");
          Interface.printArray(nonMutexVars, ", ");
          callingObject.printUsage();
          Interface.quit(1);
        }
      }
    }
  }


  /**
   * @return Number of arguments in the command line.
  **/
  public int getNumberOfArguments()
  {
    return arguments.length;
  }


  /**
   * Check to see is a particular state is set in the command line.
   * @param The state to be checked.
   * @return Whether it has been set or not.
  **/
  public boolean isState (String state)
  {
   return isState(state, arguments);
  }


  /**
   * Check to see is a particular state is set in the command line.
   * @param The state to be checked.
   * @param args The arguments to process.
   * @return Whether it has been set or not.
  **/
  public boolean isState (String state, String[] args)
  {
   if ( args == null )
    args = arguments;

   // if it is a regular state, like "-quiet" or "-a"
   for (int count = 0; count < args.length; count++)
   {
     if (args[count].equals(state))
       return true;
   }

    // if it is in a combination, like "-aefg", first get the
    // combination states - they start with a "-" and are not
    // followed by any other variable.
    for (int count = 0; count < args.length; count++)
    {
      if (checkIfState(args[count], states) == COMBINATION)
        if (args[count].indexOf(state.substring(1, state.length())) != -1)
          return true;
    }

    return false;
  }


  /**
   * Returns the last occuring state, from the specified input states,
   * in the command line.
   * @param states The list of states to be considered.
   * @return The input state that last occurs in the command line.
  **/
  public String lastState(String[] states)
  {
   return lastState( states, arguments );
  }


  /**
   * Returns the last occuring state, from the specified input states,
   * in the command line.
   * @param states The list of states to be considered.
   * @param args The arguments to process.
   * @return The input state that last occurs in the command line.
  **/
  public String lastState(String[] states, String[] args)
  {
    int maxIndex = -1;

    if ( args == null )
     args = arguments;

    for (int count = 0; count < states.length; count++) // for each input state
    {
      for (int index = 0; index < args.length; index++) // find where it occurs
      {
        if (args[index].equals(states[count]))
        {
          if (index >= maxIndex) // if its position is the largest
            maxIndex = index;
        }
      }
    }

    if (maxIndex == -1) // if no states found
      return null;
    else
      return (args[maxIndex]);
  }


  /**
   * Gets the variable that matches the specified qualifier.
   * The offset is used when a qualifier has more than one "attached" variable.
   * as in the case of "-src mode dir". In this case the offset of mode is 1
   * and that of dir is 2;
   * @param qualifier The name of the qualifier.
   * @param offset Used when a qualifier has more than one "attached" variable.
   * @return The variable associated with the qualifier.
  **/
  public String getQualifiedVariable (String qualifier, int offset)
  {
   return getQualifiedVariable(qualifier, offset, arguments);
  }


  /**
   * Gets the variable that matches the specified qualifier.
   * The offset is used when a qualifier has more than one "attached" variable.
   * as in the case of "-src mode dir". In this case the offset of mode is 1
   * and that of dir is 2.
   * @param qualifier The name of the qualifier.
   * @param offset Used when a qualifier has more than one "attached" variable.
   * @param args The arguments to process.
   * @return The variable associated with the qualifier.
  **/
  public String getQualifiedVariable( String qualifier, int offset,
      String[] args )
  {
   if ( args == null )
    args = arguments;

    for (int count = 0; count < args.length; count++)
    {
      if (args[count].equals(qualifier))
        if (!((count+offset) >= args.length)) // if not end of list
        {
          if (args[count+offset].startsWith("-")) // if not a variable
            return null;
          else
            return args[count + offset];
        }
    }

    return null;
  }


  /**
   * Gets the list of variables that matches the specified qualifier.
   * @param qualifier The name of the qualifier.
   * @return The variable associated with the qualifier.
  **/
  public String[] getQualifiedVariable (String qualifier)
  {
   return getQualifiedVariable(qualifier, arguments);
  }


  /**
   * Gets the list of variables that matches the specified qualifier.
   * @param qualifier The name of the qualifier.
   * @param args The arguments to process.
   * @return The variable associated with the qualifier.
  **/
  public String[] getQualifiedVariable (String qualifier, String[] args)
  {
   Vector vars = new Vector();

   if ( args == null )
     args = arguments;

    for (int count = 0; count < args.length; count++)
    {
      if (args[count].equals(qualifier))
        vars.addElement(args[++count]);
      else if (args[count].startsWith(qualifier))
        vars.addElement( args[count].substring( qualifier.length(),
            args[count].length() ) );
    }

    return (vectorToArray(vars));
  }


  /**
   * @return Returns the list (Vector) of all unqualified variables.
  **/
  public String[] getUnqualifiedVariables ()
  {
    return (vectorToArray(unqualifiedVariables));
  }


  /**
   * Checks the input command against the specified syntax.
   * @return True if the command matches the syntax, else false.
  **/
  public boolean checkSyntax()
  {
    return checkSyntax( arguments );
  }


  /**
   * Checks the input command against the specified syntax.
   * @param args The arguments to process.
   * @return True if the command matches the syntax, else false.
  **/
  public boolean checkSyntax( String[] args )
  {
    int index = 0;
    int numVars = 0;

    if ( args == null )
      args = arguments;

    for (int count = 0; count < args.length; count++)
    {
      if (checkIfState(args[count], states) != -1) // maybe this one is a state
        continue;
      else
      if ((numVars = checkIfQualifiedVariable(args[count])) != -1)
      {
        while (numVars-- != 0)
        {
          ++count;
          if (count == args.length)
          {
            System.out.println( "ERROR: <" + args[count-1] +
                "> must be followed by a variable.");
            return false;
          }
          else if (args[count].startsWith("-"))
          {
            System.out.println( "ERROR: <" + args[count] +
                "> is an invalid argument.");
            return false;
          }
        }
        continue;
      }
      else
      if (args[count].startsWith("-"))
      {
        System.out.println( "ERROR: <" + args[count] +
            "> is an invalid argument.");
        return false;
      }
      else
      if (unqualifiedVars)
      {
        unqualifiedVariables.addElement(args[count]);
      }
      else
        return false;
    }

    return true;
  }


  /**
   * Checks the input command against the specified syntax - an alternate form.
   * If more than the specified arguments apper on the command line, is it still
   * considered correct - this is helpful for commands where in the additional
   * arguments that are specified are simply passed to some other command.
   * @return True if the command matches the syntax, else false.
  **/
  public boolean checkAltSyntax()
  {
    int index = 0;
    int numVars = 0;

    for (int count = 0; count < arguments.length; count++)
    {
      if (checkIfState(arguments[count], states) != -1)
        continue;
      else
      if ((numVars = checkIfQualifiedVariable(arguments[count])) != -1)
      {
        // if it is of the form "-sb <sandbox_name>"
        while (numVars-- != 0)
        {
          ++count;
          if (count == arguments.length)
          {
            System.out.println( "ERROR: <" + arguments[count-1] +
                "> must be followed by a variable." );
            return false;
          }
          else if (arguments[count].startsWith("-"))
          {
            System.out.println( "ERROR: <" + arguments[count] +
                "> is an invalid argument." );
            return false;
          }
        }

        continue;
      }
      else
      if (unqualifiedVars)
      {
        unqualifiedVariables.addElement(arguments[count]);
      }
      else
        return false;
    }

    return true;
  }


  /**
   * @return Returns the sandbox name from the command line; null if
   * not specified.
  **/
  public String getSandbox()
  {
    return getQVar("-sb");
  }


  /**
   * @return Returns the rcfile name from the command line; null if
   * not specified.
  **/
  public String getRcfile()
  {
    return getQVar("-rc");
  }


  /**
   * @return Returns the set name from the command line; null if not specified.
   */
  public String getSet()
  {
    return getQVar("-set");
  }


  /**
   * Returns the variable that is preceded by a qualifier. More specific than
   * getQualifiedVariable(). Just returns the last element of the string array
   * For example if multiple sandboxes are specified "-sb sb1 -sb sb2 -sb sb3"
   * sb3 is returned.
   * @return The variable or null if not specified.
  **/
  private String getQVar(String qualifier)
  {
    String vars[];

    vars = getQualifiedVariable(qualifier);

    if (vars != null)
      return (vars[vars.length - 1]); // return last element
    else
      return null;
  }


  /**
   * Sets the messaging state for the tool. the debug state overrides
   * other states.
   * For the other states, the last occuring one on the command line is used.
   */
  public void setMessagingState()
  {
    String[] messagingStates = {"-quiet", "-normal", "-verbose"};

    if (isState("-info"))
      Interface.setState("info");
    else if (isState("-debug"))
      Interface.setState("debug");
    else
      Interface.setState(lastState(messagingStates));

    if (isState ("-auto"))
      Interface.setState("-auto");
  }


  /**
   * A method to append arguments to existing arguments.  Useful
   * for things like .MAKEFLAGS special target in make.
   * @param args The arguments to process.
  **/
  public void appendArgs( String args[] )
  {
   if ( arguments.length > 0 )
   {
    String allargs[]= new String[args.length+arguments.length];
    System.arraycopy(arguments, 0, allargs, 0, arguments.length);
    System.arraycopy(args, 0, allargs, arguments.length, args.length);
   }
   else
    arguments=args;
  }


  /**
   * A utility method. Checks to see if the key is in the array.
   */
  private int isIn (String key, String[] source)
  {
    if (source != null)
      for (int index = 0; index < source.length; index++)
      {
        if (key.equals(source[index]))
          return index;
      }

    return -1;
  }


  /**
   * Concatenates the default list of states with the additional
   * sepcified states.
  **/
  private String[] getStates(String[] inputStates)
  {                                                // the default set
    String[] defaultStates = {"-quiet", "-normal", "-verbose", "-debug",
        "-info", "-version", "-usage", "-rev", "-auto"};
    String[] actualStates;

    if (inputStates == null)  // create the combined set
      actualStates = new String[defaultStates.length];
    else
      actualStates = new String[defaultStates.length + inputStates.length];

    int count;      // add the defaluts
    for (count = 0; count < defaultStates.length; count++)
      actualStates[count] = new String(defaultStates[count]);

    if (inputStates != null)   // add the extra, if any
      for (int index = 0; index < inputStates.length; index++)
        actualStates[index + count] = new String(inputStates[index]);

    return actualStates;
  }


  /**
   * Checks if the input state is actually in the predefined set of states.
   * Also determines if the input state is a simple state or a
   * combination of simple states.
   * @param state Check if this state is valid
   * @param states The list of valis states
   * @return -1 if input state is invalid else returns SIMPLE or COMBINATION.
   */
  private int checkIfState(String state, String[] states)
  {
    if (!state.startsWith("-"))   // check to see if follows the format
      return -1;

    if (states != null)
    {
      // if "state" represents one state as in "-quiet"
      for (int index = 0; index < states.length; index++)
      {
        if (state.equals(states[index]))
          return SIMPLE;
      }

      // else if "state" is a combination. (e.g., "-arde")
      int index;
      for (index = 1; index < state.length(); index++)
      {
        int index1;
        for (index1 = 0; index1 < states.length; index1++)
        {
          // only works for states with single char
          if (states[index1].length() == 2 &&
              state.charAt(index) == states[index1].charAt(1))
            break;
        }

        if (index1 == states.length)
          return -1;
      }

      if (index == state.length()) // if all states processed successfully
        return COMBINATION;
    }

    return -1;
  }


  /**
   * Checks to see if any of the strings in the array start with
   * the specified key.
   *
  **/
  private int checkIfQualifiedVariable( String arg )
  {
    int numVars = -1;

    if (qualifiedVars != null)
      for (int i = 0; i < qualifiedVars.length; i++)
      {
        String[] actArg = StringTools.split(qualifiedVars[i], "^");

        if (arg.startsWith(actArg[0]))
        {
          if (actArg.length > 1)
            numVars = Integer.parseInt(actArg[1]);
          else
            numVars = 1;

          // the args can be of the type -<flag><var>
          // instead of -<flag> <var>
          if (!arg.equals(actArg[0]))
            numVars = 0;
        }
      }

    return numVars;
  }


  /**
   * Converts a Vector into an array of strings
  **/
  private String[] vectorToArray(Vector input)
  {
    if ((input != null) && (input.size() != 0))
    {
      String[] mapped = new String[input.size()];

      for (int count = 0; count < input.size(); count++)
        mapped[count] = new String ((String)input.elementAt(count));

      return mapped;
    }
    else
      return null;
  }


  /**
   * Strips the long form of a class name (package.classname) and just returns
   * class name.
  **/
  private String stripClassName(String longName)
  {
    if (longName.indexOf('.') == -1)
      return longName;
    else
      return (longName.substring(longName.lastIndexOf('.') + 1,
          longName.length()));
  }

  public static void printVersion( String program )
  {
    Interface.printAlways( "program    : " + program );
    Interface.printAlways( "release    : " + Version.getOdeVersionNumber() +
        " (Build " + Version.getOdeLevelName() + ")" );
    Interface.printAlways( "build date : " + Version.getOdeBuildTimestamp() );
    Interface.printAlways( "machine    : " +
        PlatformConstants.CURRENT_MACHINE );
  }
}
