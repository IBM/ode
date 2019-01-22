#ifndef _ODE_LIB_IO_COMMANDLINE_HPP_
#define _ODE_LIB_IO_COMMANDLINE_HPP_

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/io/ui.hpp"
#include "lib/io/version.hpp"
#include "lib/portable/vector.hpp"
#include "lib/portable/env.hpp"
#include "lib/portable/platcon.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/string.hpp"


/*******************************************************************************
 * NOTES: The arguments variable is not made "const" cause in some cases like
 * "make" we may have to add more arguments when running.
 * The "states" private variable is not a reference cause if all we need is the
 * default states then the "client" can pass in null. The same reason applies
 * towards the "qualifiedVars" variable.
 */

class CommandLine
{
  public:

    CommandLine( const StringArray *ipStates, const StringArray *qualVars,
                 boolean unQualVars, StringArray args,
                 const Tool &program);
    CommandLine( const StringArray *ipStates, const StringArray *qualVars,
                 boolean unQualVars, StringArray args,
                 boolean needArgs, const Tool &program,
                 boolean library_mode = false,
                 boolean runtime_command = false );
    CommandLine( const StringArray *ipStates, const StringArray *qualVars,
                 boolean unQualVars, StringArray args,
                 boolean needArgs, const Vector< StringArray > *mutexVars,
                 const Vector< StringArray > *nonMutexVars,
                 const Tool &program );

    inline void         appendArgs( const StringArray &moreArgs );
    boolean             checkAltSyntax();
    inline boolean      checkSyntax();
    inline boolean      checkSyntax( const StringArray &args );
    inline boolean      checkSyntax( boolean alternateSyntax );
    boolean             checkSyntax( boolean alternateSyntax,
                                     const StringArray &args );
    inline String       getMachine() const;
    inline int          getNumberOfArguments() const;
    inline String       getQualifiedVariable( const String &qualifier ) const;
    String              getQualifiedVariable( const String &qualifier,
                                              const StringArray &args ) const;
    inline String       getQualifiedVariable( const String &qualifier,
                                              int offset ) const;
    String              getQualifiedVariable( const String &qualifier,
                                              int offset,
                                              const StringArray &args ) const;
    inline StringArray  *getQualifiedVariables( const String &qualifier,
                                                StringArray *buffer = 0 ) const;
    StringArray         *getQualifiedVariables( const String &qualifier,
                                                const StringArray &args,
                                                StringArray *buffer = 0 ) const;
    StringArray         *getOrderedQualifiedVariables( const StringArray &qualifiers,
                           StringArray *qflags = 0, StringArray *buffer = 0 ) const;
    inline String       getRCFile() const;
    inline String       getSandbox() const;
    inline StringArray  getUnqualifiedVariables();
    inline StringArray  *getUnqualifiedVariables( StringArray *buffer );
    inline boolean      isState( const String &state ) const;
    boolean             isState( const String &state,
                                 const StringArray &args ) const;
    inline const String lastState( const StringArray &fromStates ) const;
    const String        lastState( const StringArray &fromStates,
                                   const StringArray &args ) const;
           void         printVersion( const String &program ) const;
    void                process( boolean alternateSyntax = false );
    void                setMachineEnvVar() const;
    void                setMessagingState() const;
    inline void         getArgs( StringArray &args )   const;
    inline void         restoreArgs( const StringArray &newArgs );
    static inline String getProgramName();

    virtual ~CommandLine()
    {
      delete states;
      delete qualifiedVars;
    }


  private:

    StringArray                 *states;
    StringArray                 *qualifiedVars;
    const boolean               shouldHaveUnqualifiedVars;
    StringArray                 arguments;
    StringArray                 unqualifiedVars;
    const Tool                  &tool;
    const Vector< StringArray > *mutexes;
    const Vector< StringArray > *mutincs;
    boolean                     needArguments;
    boolean                     library_mode;
    boolean                     runtime_command;
    static const int            ODEDLLPORT SIMPLE_STATE;
    static const int            ODEDLLPORT COMBINATION_STATE;
    static const int            ODEDLLPORT NOT_A_STATE;
    static String               ODEDLLPORT static_program_name;

    void          initialize( const StringArray *ipStates, const StringArray *qualVars );
    void          addDefaultStates();
    inline String getFullProgramName() const;
    inline int    isIn( const String &key, const StringArray &source ) const;
    int           checkIfState( const String &state ) const;
    int           checkIfQualifiedVariable( const String& arg ) const;
};


/*******************************************************************************
 *
 */
boolean CommandLine::checkSyntax()
{
  return (checkSyntax( true, arguments ));
}


/*******************************************************************************
 *
 */
boolean CommandLine::checkSyntax( const StringArray &args )
{
  return ( checkSyntax( true, args ) );
}


/*******************************************************************************
 *
 */
boolean CommandLine::checkSyntax( boolean alternateSyntax )
{
  return ( checkSyntax( alternateSyntax, arguments ) );
}


/*******************************************************************************
 *
 */
String CommandLine::getQualifiedVariable( const String &qualifier ) const
{
  return (getQualifiedVariable( qualifier, arguments ));
}


/*******************************************************************************
 *
 */
String CommandLine::getQualifiedVariable( const String &qualifier,
                                          int offset ) const
{
  return (getQualifiedVariable( qualifier, offset, arguments ));
}


/*******************************************************************************
 *
 */
StringArray *CommandLine::getQualifiedVariables( const String &qualifier,
                                                 StringArray *buffer) const
{
  return (getQualifiedVariables( qualifier, arguments, buffer ));
}


/*******************************************************************************
 *
 */
boolean CommandLine::isState( const String &state ) const
{
  return (isState( state, arguments ));
}


/*******************************************************************************
 *
 */
const String CommandLine::lastState( const StringArray &fromStates ) const
{
  return (lastState( fromStates, arguments ));
}


/*******************************************************************************
 *
 */
String CommandLine::getFullProgramName() const
{
  return( arguments[arguments.firstIndex()] );
}


/*******************************************************************************
 *
 */
String CommandLine::getProgramName()
{
  return( static_program_name );
}


/*******************************************************************************
 *
 */
int CommandLine::getNumberOfArguments() const
{
  return( arguments.length() - 1 ); // -1 since first arg is name of prog
}


/*******************************************************************************
 *
 */
StringArray CommandLine::getUnqualifiedVariables()
{
  return (unqualifiedVars);
}


/*******************************************************************************
 *
 */
StringArray *CommandLine::getUnqualifiedVariables( StringArray *buffer )
{
  StringArray *temp = (buffer == 0) ? new StringArray() : buffer;
  temp->clear();
  *temp = unqualifiedVars;;
  return temp;
}


/*******************************************************************************
 *
 */
void CommandLine::appendArgs( const StringArray &moreArgs )
{
  arguments += moreArgs;
  unqualifiedVars.clear();
  checkSyntax( true ); // so that any unqual vars are updated.
}


/*******************************************************************************
 *
 */
String CommandLine::getRCFile() const
{
  return getQualifiedVariable( "-rc" );
}


/*******************************************************************************
 *
 */
String CommandLine::getSandbox() const
{
  return getQualifiedVariable( "-sb" );
}


/*******************************************************************************
 *
 */
String CommandLine::getMachine() const
{
  return getQualifiedVariable( "-m" );
}


/*******************************************************************************
 *
 */
int CommandLine::isIn ( const String &key, const StringArray &source ) const
{
  for (int index = source.firstIndex(); index <= source.lastIndex(); index++)
    if (key == source[index] )
      return index;

  return ELEMENT_NOTFOUND;
}


/*******************************************************************************
 *
 */
//StringArray CommandLine::getArgs() const
void   CommandLine::getArgs( StringArray &args) const
{
  args = arguments;
}


/*******************************************************************************
 *
 */
void CommandLine::restoreArgs( const StringArray &newArgs )
{
  arguments.clear();
  arguments = newArgs;
}


#endif  // _ODE_LIB_IO_COMMANDLINE_HPP_
