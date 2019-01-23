using namespace std ;
#define _ODE_LIB_IO_COMMANDLINE_CPP_

#include "lib/io/cmdline.hpp"
#include "lib/exceptn/exceptn.hpp"
#include "lib/io/path.hpp"


const int CommandLine::SIMPLE_STATE = 0;
const int CommandLine::COMBINATION_STATE = 1;
const int CommandLine::NOT_A_STATE = -1;
String CommandLine::static_program_name;



/*******************************************************************************
 *
 */
CommandLine::CommandLine( const StringArray *ipStates,
                          const StringArray *qualVars,
                          boolean unQualVars, StringArray args,
                          const Tool &program)
                        : shouldHaveUnqualifiedVars( unQualVars ),
                          arguments( args ),
                          needArguments( false ), mutexes( 0 ),
                          mutincs( 0 ), tool( program ),
                          library_mode( false ), runtime_command( false )
{
  initialize( ipStates, qualVars );
}


/*******************************************************************************
 *
 */
CommandLine::CommandLine( const StringArray *ipStates,
                          const StringArray *qualVars,
                          boolean unQualVars, StringArray args,
                          boolean needArgs, const Tool &program,
                          boolean library_mode, boolean runtime_command )
                        : shouldHaveUnqualifiedVars( unQualVars ),
                          arguments( args ), needArguments( needArgs ),
                          mutexes( 0 ), mutincs( 0 ), tool( program ),
                          library_mode( library_mode ),
                          runtime_command( runtime_command )
{
  initialize( ipStates, qualVars );
}


/*******************************************************************************
 *
 */
CommandLine::CommandLine( const StringArray *ipStates,
                          const StringArray *qualVars,
                          boolean unQualVars, StringArray args,
                          boolean needArgs,
                          const Vector< StringArray > *mutexVars,
                          const Vector< StringArray > *nonMutexVars,
                          const Tool &program )
                        : shouldHaveUnqualifiedVars( unQualVars ),
                          arguments( args ), needArguments( needArgs ),
                          mutexes( mutexVars ), mutincs( nonMutexVars ),
                          tool( program ), library_mode( false ),
                          runtime_command( false )
{
  initialize( ipStates, qualVars );
}



/*******************************************************************************
 *
 */
void CommandLine::initialize( const StringArray *ipStates,
                              const StringArray *qualVars )
{
  if (static_program_name == StringConstants::EMPTY_STRING)
    static_program_name = Path::fileRoot(getFullProgramName(), true);
  if (library_mode && !runtime_command)
    arguments.prepend( getProgramName() ); // prepend equivalent of argv[0]

  // even if user entered null we still create this cause we always have
  // the default states.
  states = new StringArray();

  // if ipstates is not null copy over all the states
  if (ipStates != 0)
    *states = *ipStates;

  qualifiedVars = 0;
  if (qualVars != 0)
    qualifiedVars = new StringArray( *qualVars );
}


/**
 *
**/
void CommandLine::process( boolean altSyntax )
{
  addDefaultStates();

  if (!checkSyntax( altSyntax ))
  {
    if (library_mode)
      throw Exception( "illegal or missing argument" );
    tool.printUsage();
    Interface::quit( 1 );
  }

  if (isState( "-usage" ))
  {
    if (library_mode)
      throw Exception( "-usage is not legal" );
    tool.printUsage();
    Interface::quit( 1 );
  }

  if (isState( "-version" ) || isState( "-rev" )) // check these flags
  {
    if (library_mode)
      throw Exception( "-version/-rev is not legal" );
    printVersion( getFullProgramName() );
    Interface::quit( 1 );
  }

  if ((mutexes != 0) && (mutexes->size() > 0)) // check for mutual exclusions
  {
    for (int count = mutexes->firstIndex();
             count <= mutexes->lastIndex(); count++)
    {
      const StringArray *mutexVars = mutexes->elementAt( count );

      int found = 0;
                         // for each set of variables
      for (int i = ARRAY_FIRST_INDEX; i <= mutexVars->lastIndex(); i++)
      {
        if (isIn((*mutexVars)[i], arguments) != ELEMENT_NOTFOUND)
           ++found;  // check if more than one are present
      }

      if (found > 1)   // two or more is a crowd...bye-bye
      {
        if (library_mode)
          throw Exception( (String( "These args are mutually exclusive: " ) +
              mutexVars->join( ", " )).toCharPtr() );
        Interface::printnlnAlways( "These args are mutually exclusive: " );
        Interface::printArray( *mutexVars, ", " );
        tool.printUsage();
        Interface::quit(1);
      }
    }
  }

       // check for variables that must occur together
  if ((mutincs != 0) && (mutincs->size() > 0))
  {
    for (int count = mutincs->firstIndex();
             count <= mutincs->lastIndex(); count++)
    {
      const StringArray *mutinc = mutincs->elementAt(count);
      int found = 0;
                                // for each set of variables
      for (int i = ARRAY_FIRST_INDEX; i <= mutinc->lastIndex(); i++)
      {
        if (isIn((*mutinc)[i], arguments) != ELEMENT_NOTFOUND)
          ++found; // check if all of them occur
      }

      if ((found != mutinc->length()) && (found != 0)) // where is everybody???
      {
        if (library_mode)
          throw Exception( (String( "These args should be specified"
              "together: " ) + mutinc->join( ", " )).toCharPtr() );
        Interface::printnlnAlways( "These args should be specified "
            "together: " );
        Interface::printArray( *mutinc, ", " );
        tool.printUsage();
        Interface::quit(1);
      }
    }
  }

  setMachineEnvVar();
  setMessagingState();
}


/*******************************************************************************
 *
 */
const String CommandLine::lastState( const StringArray &states,
                                     const StringArray &args ) const
{
  for (int i = args.lastIndex(); i >= ARRAY_FIRST_INDEX; i--)
  {
    // First check for an exact match
    for (int j = ARRAY_FIRST_INDEX; j <= states.lastIndex(); j++)
    {
      if (args[i] == states[j])
        return (args[i]);
    }

    // Now check for a combinational appearance
    if (checkIfState( args[i] ) == COMBINATION_STATE)
    {
      for (int k = args[i].lastIndex(); k > STRING_FIRST_INDEX; --k)
      {
        for (int m = ARRAY_FIRST_INDEX; m <= states.lastIndex(); m++)
        {
          if (args[i].startsWith( states[m].substring(
              ARRAY_FIRST_INDEX + 1, states[m].lastIndex() + 1 ), k ) != 0)
            return (states[m]);
        }
      }
    }
  }

  return (StringConstants::EMPTY_STRING); // if nothing - return null
}


/*******************************************************************************
 *
 */
void CommandLine::setMessagingState() const
{
  static StringArray mStates;
  if (mStates.size() < 1)
  {
    mStates.add( "-quiet" );
    mStates.add( "-normal" );
    mStates.add( "-verbose" );
    mStates.add( "-debug" );
  }

  if (library_mode)
  {
    if (isState( "-info" ))
      throw Exception( "-info is not legal" );
    if (isState( "-auto" ))
      throw Exception( "-auto is not legal" );
    if (isState( "-quiet" ))
      throw Exception( "-quiet is not legal" );
    if (isState( "-normal" ))
      throw Exception( "-normal is not legal" );
    if (isState( "-verbose" ))
      throw Exception( "-verbose is not legal" );
    if (isState( "-debug" ))
      throw Exception( "-debug is not legal" );
  }
  else
  {
    Interface::setState( lastState( mStates ) );

    if (isState( "-info" ))
      Interface::setState( Interface::INFO_STR );

    if (isState( "-auto" ))
      Interface::setState( Interface::AUTO_STR );
  }
}


/*******************************************************************************
 * Checks if the "input state" is in the predefined list of states or part of
 * a combination as in "-adfgt".
 */
int CommandLine::checkIfState( const String &state ) const
{
  if (!state.startsWith( "-" ))
    return NOT_A_STATE;

  // first check if the "whole" state is just one state
  for (int j = ARRAY_FIRST_INDEX; j <= states->lastIndex(); j++)
    if ((*states)[j] == state)
      return SIMPLE_STATE;

  // now consider each character of the input and check to see if it is a state
  // all chars MUST be states. Ignore first char, since it is a "-"
  for (int i = (STRING_FIRST_INDEX + 1); i <= state.lastIndex(); i++)
  {
    int count = 0;

    for (count = ARRAY_FIRST_INDEX; count <= states->lastIndex(); count++)
      if (((*states)[count].length() == 2) && (state[i] ==
                                               ((*states)[count])[2]))
        break;

    if (count > states->lastIndex())
      return NOT_A_STATE;
  }

  return COMBINATION_STATE;
}


/*******************************************************************************
 *
 */
boolean CommandLine::isState( const String &state,
                              const StringArray &args ) const
{
  // if it is a regular state, like "-quiet" or "-a"
  for (int j = ARRAY_FIRST_INDEX; j <= args.lastIndex(); j++)
    if (args[j] == state)
      return true;

  // if it is in a combination, like "-aefg"
  // first get the combination states - they start with a "-" and are not
  // followed by any other variable.
  for (int i = ARRAY_FIRST_INDEX; i <= args.lastIndex(); i++)
  {
    if (checkIfState( args[i] ) == COMBINATION_STATE)
      if (args[i].indexOf( state.substring(
          STRING_FIRST_INDEX + 1, state.lastIndex() + 1) ) != 0)
        return true;
  }

  return false;
}



/*******************************************************************************
 *
 */
String CommandLine::getQualifiedVariable( const String &qualifier,
                                          const StringArray &args ) const
{
  StringArray vars;

  getQualifiedVariables( qualifier, args, &vars );

  if (vars.length() != 0)
    return ( String( vars[vars.lastIndex()] )); // return last element
  else
    return String();
}


/*******************************************************************************
 *
 */
String CommandLine::getQualifiedVariable( const String &qualifier,
                                          int offset,
                                          const StringArray &args ) const
{
  for (int count = ARRAY_FIRST_INDEX; count <= args.lastIndex(); count++)
  {
    if (args[count].equals(qualifier))
      if (!((count+offset) > args.length())) // if not end of list
      {
        if (args[count+offset].startsWith("-")) // if not a variable
          return String();
        else
          return (String( args[count + offset] ));
      }
  }

  return String();
}


/*******************************************************************************
 *
 */
StringArray *CommandLine::getQualifiedVariables ( const String &qualifier,
                                                  const StringArray &args,
                                                  StringArray *buffer ) const
{
  //I need to ensure proper destruction of locally allocated vars, 
  // in case I return 0.
  StringArray *vars =0;

  if(buffer)  {
    vars= buffer;
    for (int count = ARRAY_FIRST_INDEX; count <= args.lastIndex(); count++) {
      if (args[count].equals(qualifier)) vars->add( args[++count] );
      else if (args[count].startsWith(qualifier))
        vars->add( args[count].substring( qualifier.length() + 1,
                                          args[count].lastIndex()+1) );
    }
  if (vars->length() == 0) return 0;
  } else {
   vars= new StringArray();
   for (int count = ARRAY_FIRST_INDEX; count <= args.lastIndex(); count++) {
     if (args[count].equals(qualifier)) vars->add( args[++count] );
       else if (args[count].startsWith(qualifier))
         vars->add( args[count].substring( qualifier.length() + 1,
                                           args[count].lastIndex()+1) );
   }
   if (vars->length() == 0) {
    delete vars;
    return 0;
   }
 }
 return (vars);
}

/************************************************
  * --- StringArray *CommandLine::getOrderedQualifiedVariables  ---
  *
  ************************************************/
StringArray *CommandLine::getOrderedQualifiedVariables ( const StringArray &qualifiers,
                            StringArray *qflags, StringArray *buffer ) const
{
   boolean nvars = false;
   boolean nqf = false;
   StringArray *qf = 0;
   StringArray *vars = 0;

   if (buffer && qflags)
   {
      vars = buffer;
      qf = qflags;
   }
   else if (buffer)
   {
      vars = buffer;
      nqf = true;
      qf = new StringArray();
   }
   else if (qf)
   {
      nvars = true;
      vars = new StringArray();
      qf = qflags;
   }
   else
   {
      nvars = true;
      vars = new StringArray();
      nqf = true;
      qf = new StringArray();
   }

   for (int count = ARRAY_FIRST_INDEX; count <= arguments.lastIndex(); count++)
     for (int qc = ARRAY_FIRST_INDEX; qc <= qualifiers.lastIndex(); qc++)
       if (arguments[count].equals(qualifiers[qc]))
       {
         qf->add( arguments[count] );
         vars->add( arguments[++count] );
       }
       else if (arguments[count].startsWith(qualifiers[qc]))
       {
         qf->add( arguments[count].substring( STRING_FIRST_INDEX,
             qualifiers[qc].length() + 1 ));
         vars->add( arguments[count].substring( qualifiers[qc].length() + 1,
             arguments[count].lastIndex() + 1 ) );
       }

   //I need to ensure proper destruction of locally allocated vars and qf,
   // in case I return 0.
   if (vars->length() == 0)
   {
     if (nvars)
       delete vars;
     if (nqf)
       delete qf;
     return 0;
   }

   return( vars );
}

/*******************************************************************************
 *
 */
boolean CommandLine::checkSyntax( boolean altSyntax, const StringArray &args )
{
  int numVars = 0;

  // start from second arg - the first is the prog name
  for (int count = ARRAY_FIRST_INDEX + 1; count <= args.lastIndex(); count++)
  {
    if ((numVars = checkIfQualifiedVariable( args[count]) ) != -1)
    {
      while (numVars-- != 0)
      {
        ++count;
        if (count > args.lastIndex())
        {
          if (library_mode)
            throw Exception( (args[count - 1] + 
                             " must be followed by an argument").toCharPtr() );
          Interface::printError( CommandLine::getProgramName() +
                                 ": <" + args[count-1] +
                                 "> must be followed by an argument." );
          return false;
        }
        else if (args[count].startsWith("-"))
        {
          if (library_mode)
            throw Exception( (args[count] +
                " is an invalid argument").toCharPtr() );
          Interface::printError( CommandLine::getProgramName() +
                                 ": <" + args[count] +
                                 "> is an invalid argument." );
          return false;
        }
      }

      continue;
    }
    else if(checkIfState( args[count] ) != NOT_A_STATE ) // if state
      continue;
    else if (!altSyntax && args[count].startsWith("-"))//skip if altsyntax
    {
      if (library_mode)
        throw Exception( (args[count] +
            " is an invalid argument").toCharPtr() );
      Interface::printError( CommandLine::getProgramName() +
                             ": <" + args[count] +
                             "> is an invalid argument." );
      return false;
    }
    else if (shouldHaveUnqualifiedVars)
      unqualifiedVars.add( args[count] );
    else
    {
      if (library_mode)
        throw Exception( (args[count] +
            " is an invalid argument").toCharPtr() );
      Interface::printError( CommandLine::getProgramName() +
                             ": <" + args[count] +
                             "> is an invalid argument." );
      return false;
    }
  }

  if (needArguments && getNumberOfArguments() == 0)
    return (false);

  return true;
}


/*******************************************************************************
 *
 */
int CommandLine::checkIfQualifiedVariable( const String &arg ) const
{
  long numVars = -1;

  if (qualifiedVars != 0)
  {
    for (int i = ARRAY_FIRST_INDEX;
             i <= qualifiedVars->lastIndex(); i++)
    {
      StringArray *actArg = (*qualifiedVars)[i].split( "^" );

      if (arg.startsWith( (*actArg)[ARRAY_FIRST_INDEX] ))
      {
        if (actArg->length() > 1)
          numVars = ((*actArg)[ARRAY_FIRST_INDEX + 1]).asInt();
        else
          numVars = 1;

               // the args can be of the type -<flag><var>
               // instead of -<flag> <var>
        if (!arg.equals( (*actArg)[ARRAY_FIRST_INDEX] ))
          numVars = 0;
      }

      delete actArg;
    }
  }

  return numVars;
}



/*******************************************************************************
 *
 */
void CommandLine::setMachineEnvVar() const
{
  String machine = getMachine();

  if (machine == StringConstants::EMPTY_STRING)
  {
    if (Env::getenv( StringConstants::MACHINE_VAR ) != 0)
      Env::setenv( SandboxConstants::CONTEXT_VAR,
                   *(Env::getenv( StringConstants::MACHINE_VAR )), false );
    else
      Env::setenv( SandboxConstants::CONTEXT_VAR,
                   PlatformConstants::CURRENT_MACHINE, false );
  }
  else
  {
    // need to add this here since if it isn't set Buildconf sets these vars
    // equal to the current machine - even if a different machine has been
    // specified on the command line.
    //
    Env::setenv( SandboxConstants::CONTEXT_VAR, machine, true );
  }
}



/*******************************************************************************
 * This is implemented in this rather cumbersome way so that it is a little
 * faster.
 */
void CommandLine::addDefaultStates()
{
  states->add( "-quiet" );
  states->add( "-normal" );
  states->add( "-verbose" );
  states->add( "-debug" );
  states->add( "-info" );
  states->add( "-version" );
  states->add( "-rev" );
  states->add( "-usage" );
  states->add( "-auto" );
  states->add( "-noauto" );
}

/*******************************************************************************
 *
 */
void CommandLine::printVersion( const String &program ) const
{
  Interface::printAlways( "program    : " + program );
  Interface::printAlways( "release    : " + Version::VERSION );
  Interface::printAlways( "build date : " + Version::BUILD_DATE );
  Interface::printAlways( "machine    : " +
                          PlatformConstants::CURRENT_MACHINE );
}
