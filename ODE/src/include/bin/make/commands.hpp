/**
 * Commands container class
 *
**/
#ifndef _ODE_BIN_MAKE_COMMANDS_HPP_
#define _ODE_BIN_MAKE_COMMANDS_HPP_

#include "base/odebase.hpp"
#include "lib/string/string.hpp"
#include "lib/string/variable.hpp"

#include "bin/make/command.hpp"
#include "bin/make/passinst.hpp"
#include "lib/util/condeval.hpp"

class RunTimeForLoop;


/**
 * Note that the pre_count index exists exclusively so that
 * command sets can be prepended instead of appended.  This
 * is to support the .PRECMDS special source.  The index
 * is repeatedly increased until someone calls one of the
 * "append" functions for the cmds Vector (e.g., addCmd),
 * at which point it is reset to ARRAY_FIRST_INDEX.  The
 * function prepareForCmds() exists so that the caller can
 * be sure that the next call to addPreCmd() or addPrePostCmd()
 * will start at ARRAY_FIRST_INDEX (as opposed to where it
 * left off from the last call, which is necessary if two
 * occurrences of .PRECMDS appear sequentially).
**/
class Commands
{

  public:
    inline Commands();
    inline Commands( const Vector <Command> &new_cmds );

    inline void addCmd( const Command &c );
    inline void addPreCmd( const Command &c );
    inline void addPrePostCmd( const Command &c );
    inline void addCmds( const Vector <Command> &new_cmds );
    inline void prepareForCmds();
    inline boolean hasCmds();
    inline const Vector< Command > &getCmds() const;
    inline void removeCmds();
    inline void setFirstCmd();
    inline void resetCmdEnumerator( int destIndex );
    void setFirstParsedCmd();
    inline const Command *getNextCmd();
    const Command *getNextParsedCmd();
    void parseCmds( const Variable &var_eval, PassInstance *pn );
    inline unsigned long length() const;
    inline StringArray  getLocalVarValue() const;
    inline StringArray  getLocalVar() const;
    ~Commands() {};


  private:
    int pre_count;
    Vector <Command> cmds;
    VectorEnumeration <Command> enum_cmds;
    const Variable *var_eval_loc;
    PassInstance *pn_loc;
    String errmfname;
    CondEvaluator condeval;
    boolean parseskip;
    boolean print_debug_msg;
    const String *keyword;
    Command *cmd;
    int lastline;
    int          currIndex;
    StringArray  newLocalValue;
    StringArray  newLocalVar;
    Vector <RunTimeForLoop *>  forLoops;
    void  parseFor   ( const String &line );
    void  parseEndFor();
};

/************************************************
  *         --- inline Commands::Commands ---
  *
  ************************************************/
inline Commands::Commands() :
    pre_count( ARRAY_FIRST_INDEX )
{
}

inline Commands::Commands( const Vector <Command> &new_cmds ) :
    pre_count( ARRAY_FIRST_INDEX )
{
  addCmds( new_cmds );
}

/************************************************
  *      --- inline void Commands::addCmd ---
  *
  ************************************************/
inline void Commands::addCmd( const Command &c )
{
  cmds.addElement( c );
  pre_count = ARRAY_FIRST_INDEX;
}

inline void Commands::addPreCmd( const Command &c )
{
  cmds.insertElementAt( c, pre_count++ );
}

inline void Commands::addPrePostCmd( const Command &c )
{
  cmds.insertElementAt( c, pre_count++ );
  cmds.addElement( c );
}

/************************************************
  *     --- inline void Commands::addCmds ---
  *
  ************************************************/
inline void Commands::addCmds( const Vector <Command> &new_cmds )
{
  cmds = new_cmds;
  pre_count = ARRAY_FIRST_INDEX;
}

inline void Commands::prepareForCmds()
{
  pre_count = ARRAY_FIRST_INDEX;
}

/************************************************
  *--- inline boolean Commands::hasCmds ---
  *
  ************************************************/
inline boolean Commands::hasCmds()
{
  return (!cmds.isEmpty());
}

/************************************************
  --- inline const Vector< Command > &Commands::getCmds ---
  *
  ************************************************/
inline const Vector< Command > &Commands::getCmds() const
{
  return cmds;
}

/************************************************
  *--- inline void Commands::removeCmds ---
  *
  ************************************************/
inline void Commands::removeCmds()
{
  cmds.removeAllElements();
}

/************************************************
  * --- inline void Commands::setFirstCmd ---
  *
  ************************************************/
inline void Commands::setFirstCmd()
{
  enum_cmds.setObject( &cmds );
  currIndex = ARRAY_FIRST_INDEX;
}

/************************************************
  * --- inline Command *Commands::getNextCmd ---
  *
  ************************************************/
inline const Command *Commands::getNextCmd()
{
  if (enum_cmds.hasMoreElements())
  {
    currIndex++;
    return( enum_cmds.nextElement() );
  }
  else
    return( 0 );
}

/*************************************************
  * inline Command *Commands::resetCmdEnumerator
  *
  ************************************************/
inline void Commands::resetCmdEnumerator( int destIndex )
{
  setFirstCmd();
  while (enum_cmds.hasMoreElements() && (currIndex < destIndex ))
  {
    enum_cmds.nextElement();
    currIndex++;
  }
}

/*************************************************
  * --- inline unsigned long Commands::length ---
  *
  ************************************************/
inline unsigned long Commands::length() const
{
  return cmds.length();
}

/****************************************************
  * inline unsigned long Commands::getLocalVarValue
  *
  ***************************************************/
inline StringArray Commands::getLocalVarValue() const
{
  return( newLocalValue );
}

/****************************************************
  * inline unsigned long Commands::getLocalVar
  *
  ***************************************************/
inline StringArray Commands::getLocalVar() const
{
  return( newLocalVar );
}




class RunTimeForLoop
{

  public:
    inline RunTimeForLoop();
    ~RunTimeForLoop() {};
    inline void    setCommandIndex ( int idx );
    inline int     getCommandIndex ();
    void    initialize( String      line,
                        int         equalIndex,
                        StringArray *newLocalVar,
                        StringArray *newLocalValue );
    boolean incrementArgIndex( StringArray *newLocalVar,
                               StringArray *newLocalValue );

  private:
    int          argIndex;
    int          cmdIndex;
    StringArray  args;
    String       variable;

};

/************************************************
  *  --- inline RunTimForLoop::RunTimeForLoop ---
  *
  ************************************************/
inline RunTimeForLoop::RunTimeForLoop() :
    cmdIndex( 0 ), argIndex( 0 )
{
}

/************************************************
  *  --- inline RunTimForLoop::setCommandIndex --
  *
  ************************************************/
inline void RunTimeForLoop::setCommandIndex( int idx )
{
  cmdIndex = idx;
}

/************************************************
  *  --- inline RunTimForLoop::getCommandIndex --
  *
  ************************************************/
inline int RunTimeForLoop::getCommandIndex()
{
  return( cmdIndex );
}


#endif //_ODE_BIN_MAKE_COMMANDS_HPP_
