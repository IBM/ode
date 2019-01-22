#ifdef DEFAULT_SHELL_IS_VMS
#include <ssdef.h>
#endif
#include <stdlib.h>
#define _ODE_LIB_IO_UI_CPP_
#include "lib/io/ui.hpp"
#include "lib/io/path.hpp"
#ifdef __WEBMAKE__
#include "bin/make/makec.hpp"
#endif // __WEBMAKE__

/*******************************************************************************
 * Initialize all the static variables
 */
boolean Interface::AUTO  = false;
boolean Interface::DEBUG = false;
boolean Interface::INFO  = false;
#ifdef __WEBMAKE__
boolean Interface::XML   = false;
#endif // __WEBMAKE__

const String Interface::AUTO_STR   ( "auto" );
const String Interface::DEBUG_STR  ( "debug" );
const String Interface::INFO_STR   ( "info" );
const String Interface::NOAUTO_STR ( "noauto" );
const String Interface::NORMAL_STR ( "normal" );
const String Interface::QUIET_STR  ( "quiet" );
const String Interface::VERBOSE_STR( "verbose" );

int Interface::iMessagingLevel = Interface::NORMAL;


/*******************************************************************************
 *
 */
void Interface::setState( const String &messagingState )
{
  String state = messagingState;

  if (state.length() != 0)
  {
    // if there is a preceding "-", cut it off
    if (state.startsWith( "-" ))
      state.remove( STRING_FIRST_INDEX, 1 );

    if (state.equals( VERBOSE_STR ))
      iMessagingLevel = VERBOSE;
    else if (state.equals( NORMAL_STR ))
      iMessagingLevel = NORMAL;
    else if (state.equals( QUIET_STR ))
      iMessagingLevel = QUIET;
    else if (state.equals( DEBUG_STR ))
    {
      iMessagingLevel = VERBOSE;
      DEBUG = true;
    }
    else if (state.equals( INFO_STR ))
      INFO = true;
    else if (state.equals( AUTO_STR ))
      AUTO = true;
    else if (state.equals( NOAUTO_STR ))
      AUTO = false;
    else
      iMessagingLevel = NORMAL;
  }
}

/*******************************************************************************
 *
 */
String Interface::getResponse( const String &prompt, const boolean infinite )
{
  String response;  // empty string

  do
  {
    cout << prompt;
    // if auto has been specified return the empty string obj
    if (AUTO)
    {
      cout << "Using default..." << endl;
      return response;
    }
    response = gatherInput();
  } while ((response.length() == 0) && infinite);

  return response;
}



/*******************************************************************************
 *
 */
boolean Interface::getConfirmation( const String &prompt,
                                    const boolean defaultResponse )
{
  String response;

  cout << prompt;

  if (isAuto())
  {
    Interface::printAlways( "Yes" );
    return true;
  }

  response = gatherInput();

  if (response.length() == 0)
    return defaultResponse;
  else
  {
    if (response.startsWith( "Y" ) || response.startsWith( "y" ))
      return true;
    else if (response.startsWith( "N" ) || response.startsWith( "n" ))
      return false;
    else
      return defaultResponse;
  }
}



/*******************************************************************************
 *
 */
boolean Interface::getConfirmation( const String &prompt,
                                    const boolean infinite,
                                    const boolean defaultResponse )
{
  String response;

  while ((response.length() == 0) && infinite)
  {
    cout << prompt;
    if (AUTO)
    {
      Interface::printAlways( "Yes" );
      return true;
    }
    response = gatherInput();
  }

  if (response.startsWith( "Y" ) || response.startsWith( "y" ))
    return true;
  else if (response.startsWith( "N" ) || response.startsWith( "n" ))
    return false;
  else
    return defaultResponse;
}


/*******************************************************************************
 *
 */
String Interface::gatherInput()
{
  String response;
  Path::readLine( cin, &response );
  return (response);
}



/*******************************************************************************
 *
 */
void Interface::printnln( const String &message )
{
  if (isNormal())
    cout << message;
}



/*******************************************************************************
 *
 */
void Interface::print( const String &message )
{
  if (isNormal())
    cout << message << endl;
}


/*******************************************************************************
 *
 */
void Interface::printArray( const StringArray &array, const String &sep)
{
  if (array.length() == 0)
    return;

  for (int i = array.firstIndex(); i <= array.lastIndex(); i++)
  {
    cout << array[i];
    if (i < array.length())
      cout << sep;
  }

  cout << endl;
}



/*******************************************************************************
 * The "default" foolows the same convention as IString's indexing i.e the first
 * element is 1 and NOT 0. If def is 0 all elements are considered as default.
 */
void Interface::printArrayWithDefault( const StringArray &array,
                                              const String &sep,
                                              const int def )
{
  if ((array.length() == 0) || (def < 0) || (def > array.length()))
    return;

  if (def == 0)
    cout << "[";

  for (int i = array.firstIndex(); i <= array.lastIndex(); i++)
  {
    if (i == def)
      cout << "[" << array[i] << "]";
    else
      cout << array[i];
    if (i < array.length())
      cout << sep;
  }

  if (def == 0)
    cout << "]";

  cout << endl;
}


/*******************************************************************************
 *
 */
void Interface::printnlnToErrorStream( const String &message )
{
  if (isNormal())
    cerr << message;
}


/*******************************************************************************
 *
 */
void Interface::printToErrorStream( const String &message )
{
  if (isNormal())
    cerr << message << endl;
}


/*******************************************************************************
 *
 */
void Interface::printnlnAlwaysToErrorStream( const String &message )
{
  cerr << message;
}


/*******************************************************************************
 *
 */
void Interface::printAlwaysToErrorStream( const String &message )
{
  cerr << message << endl;
}


/*******************************************************************************
 *
 */
void Interface::printnlnVerbose( const String &message )
{
  if (isVerbose())
    cout << message;
}



/*******************************************************************************
 *
 */
void Interface::printVerbose( const String &message )
{
  if (isVerbose())
    cout << message << endl;
}



/*******************************************************************************
 *
 */
void Interface::printnlnAlways( const String &message )
{
  cout << message;
}


/*******************************************************************************
 *
 */
void Interface::printAlways( const String &message )
{
  cout << message << endl;
}


/*******************************************************************************
 *
 */
void Interface::printnlnWarning( const String &message )
{
  if (isNormal())
    cout << "> WARNING: " << message;
}


/*******************************************************************************
 *
 */
void Interface::printWarning( const String &message )
{
  if (isNormal())
    cout << "> WARNING: " << message << endl;
}


/*******************************************************************************
 *
 */
void Interface::printnlnError( const String &message )
{
  cerr << ">> ERROR: " << message;
}


/*******************************************************************************
 *
 */
void Interface::printError( const String &message )
{
  cerr << ">> ERROR: " << message << endl;
}


/*******************************************************************************
 *
 */
void Interface::printnlnFatalError( const String &message )
{
  cerr << ">>> FATAL ERROR: " << message;
}


/*******************************************************************************
 *
 */
void Interface::printFatalError( const String &message )
{
  cerr << ">>> FATAL ERROR: " << message << endl;
}


/*******************************************************************************
 *
 */
void Interface::printnlnDebug( const String &message )
{
  if (isDebug())
    cout << "* DEBUG: " << message;
}


/*******************************************************************************
 *
 */
void Interface::printDebug( const String &message )
{
  if (isDebug())
    cout << "* DEBUG: " << message << endl;
}


/*******************************************************************************
 *
 */
void Interface::printDebug(const StringArray &array)
{
  if (isDebug() && (array.length() > 0))
  {
    cout << "* DEBUG: " << array[array.firstIndex()] << endl;
    for (int i = array.firstIndex() + 1; i <= array.lastIndex(); i++)
      cout << "         " << array[i] << endl;
  }
}


/*******************************************************************************
 *
 */
boolean Interface::printnlnInfo( const String &message )
{
  if (isInfo())
    cout << "** " << message;

  return (isInfo( ));
}


/*******************************************************************************
 *
 */
boolean Interface::printInfo( const String &message )
{
  if (isInfo())
    cout << "** " << message << endl;

  return (isInfo( ));
}

/*******************************************************************************
 *
 */
void Interface::quit( const int errorCode )
{
#ifdef __WEBMAKE__
  if (writeXML())
     Interface::printAlways( "</WebMake>" );
#endif // __WEBMAKE__
  Interface::terminate( errorCode );
}


/*******************************************************************************
 *
 */
void Interface::quit( const String &message, const int errorCode )
{
  cout << message << endl;
#ifdef __WEBMAKE__
  if (writeXML())
     Interface::printAlways( "</WebMake>" );
#endif // __WEBMAKE__
  Interface::terminate ( errorCode );
}


/*******************************************************************************
 *
 */
void Interface::quitWithErrMsg( const String &message,
                                       const int errorCode )
{
  printError( message );
#ifdef __WEBMAKE__
  if (writeXML())
     Interface::printAlways( "</WebMake>" );
#endif // __WEBMAKE__
  Interface::terminate( errorCode );
}


void Interface::terminate( const int errorCode )
{
#ifdef DEFAULT_SHELL_IS_VMS
  if (errorCode)
    exit( SS$_ABORT );
  else
    exit( SS$_NORMAL );
#else
  exit( errorCode );
#endif
}
