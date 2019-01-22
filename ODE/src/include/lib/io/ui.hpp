#ifndef _ODE_LIB_IO_UI_HPP_
#define _ODE_LIB_IO_UI_HPP_

#include <iostream.h>
#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"

class Interface
{
   public:

      // values of the enum are important since there is a test for >= NORMAL
      enum { QUIET, NORMAL, VERBOSE };

      static const String ODEDLLPORT AUTO_STR;
      static const String ODEDLLPORT DEBUG_STR;
      static const String ODEDLLPORT INFO_STR;
      static const String ODEDLLPORT NOAUTO_STR;
      static const String ODEDLLPORT NORMAL_STR;
      static const String ODEDLLPORT QUIET_STR;
      static const String ODEDLLPORT VERBOSE_STR;

      // get the "messaging" state
      inline static boolean isQuiet();
      inline static boolean isNormal();
      inline static boolean isVerbose();
      inline static boolean isDebug();
      inline static boolean isInfo();
      inline static boolean isAuto();

      // set the "messaging" state
             static void    setState( const String &messagingState );
      inline static void    setState( const String *messagingState );
      inline static void    setState( int messagingState );
#ifdef __WEBMAKE__
      inline static void    setXMLState( boolean xmlState );
      inline static boolean writeXML();
#endif

      //print based on mode
      static void printnln( const String &message );
      static void print( const String &message );
      static void printnlnToErrorStream( const String &message );
      static void printToErrorStream( const String &message );
      static void printnlnAlwaysToErrorStream( const String &message );
      static void printAlwaysToErrorStream( const String &message );
      //print in verbose mode only
      static void printnlnVerbose( const String &message );
      static void printVerbose( const String &message );
      // print regardless of mode
      static void printnlnAlways( const String &message );
      static void printAlways( const String &message );

      // print out array's - in all modes
      static void printArray( const StringArray &array,
                                     const String &sep );
      static void printArrayWithDefault( const StringArray &array,
                                                const String &sep,
                                                const int def );

      // print based on mode
      static void printnlnWarning( const String &message );
      static void printWarning( const String &message );

      // print in all modes
      static void printnlnError( const String &message );
      static void printError( const String &message );
      static void printnlnFatalError( const String &message );
      static void printFatalError( const String &message );

      // print only in debug mode
      static void printnlnDebug( const String &message );
      static void printDebug( const String &message );
      static void printDebug( const StringArray &array );

      // print only in info mode
      static boolean printnlnInfo( const String &message );
      static boolean printInfo( const String &message );

      // quit and if specified print in all modes
      static void quit( const int errorCode );
      static void quit( const String &message, const int errorCode );
      static void quitWithErrMsg( const String &message,
                                         const int errorCode );
      static void terminate( const int errorCode );

      // get input from user
      static String  getResponse( const String &prompt,
                                  const boolean infinite );
      static boolean getConfirmation( const String &prompt,
                                      const boolean defaultResponse );
      static boolean getConfirmation( const String &prompt,
                                      const boolean infinite,
                                      const boolean defaultResponse );


   private:

      static int          ODEDLLPORT iMessagingLevel;
      static boolean      ODEDLLPORT DEBUG;
      static boolean      ODEDLLPORT INFO;
      static boolean      ODEDLLPORT AUTO;
#ifdef __WEBMAKE__
      static boolean      ODEDLLPORT XML;
#endif

      static String    gatherInput();
};

inline boolean Interface::isNormal()
{
  return (iMessagingLevel >= NORMAL);
}

inline boolean Interface::isVerbose()
{
  return (iMessagingLevel == VERBOSE);
}

inline boolean Interface::isQuiet()
{
  return (iMessagingLevel == QUIET);
}

inline boolean Interface::isDebug()
{
  return (DEBUG);
}

inline boolean Interface::isInfo()
{
  return (INFO);
}

inline boolean Interface::isAuto()
{
  return (AUTO);
}
#ifdef __WEBMAKE__
inline boolean Interface::writeXML()
{
  return (XML);
}
#endif


/*******************************************************************************
 *
 */
inline void Interface::setState( const String *messagingState )
{
  if (messagingState != 0)
    Interface::setState( *messagingState );
}

/*******************************************************************************
 * Sets the level in the range of (QUIET, NORMAL, VERBOSE)
 */
inline void Interface::setState( int messagingState )
{
  if (messagingState >= QUIET && messagingState <= VERBOSE)
    iMessagingLevel = messagingState;
}

/*******************************************************************************
 * Sets the level in the range of (QUIET, NORMAL, VERBOSE)
 */
#ifdef __WEBMAKE__
inline void Interface::setXMLState( boolean xmlState )
{
    XML = xmlState;
}
#endif

#endif /* _ODE_LIB_IO_UI_HPP_ */
