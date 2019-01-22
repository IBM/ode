//***********************************************************************
//* Make
//*
//***********************************************************************
#ifndef _ODE_BIN_MAKE_CMDABLE_HPP_
#define _ODE_BIN_MAKE_CMDABLE_HPP_

#include "lib/util/bitset.hpp"

#include "bin/make/commands.hpp"
#include "bin/make/passinst.hpp"
#include "bin/make/constant.hpp"

class Commandable
{

  public:
    enum
    {
      SUFFIX_PAIR_CMD  = 0,
      TARGET_NODE_CMD  = 1,
      PATTERN_PAIR_CMD = 2,
      PASS_NODE_CMD    = 3  // An invalid state, since pass nodes
                            //don't have commands
    };

    inline Commandable();
    inline Commandable( const Commandable &c );
    virtual ~Commandable() {};

    virtual int     getCmdType() const = 0;

    inline void setType( int type );
    inline void clearType( int type );
    inline boolean isSet( int type );
    inline void addCmd( const Command &c );
    inline void addPreCmd( const Command &c );
    inline void addPrePostCmd( const Command &c );
    inline void addCmds( const Vector <Command> &new_cmds );
    inline void prepareForCmds();
    inline boolean hasCmds();
    inline const Vector <Command>  &getCmds() const;
    inline void removeCmds();
    inline void setFirstCmd();
    inline void setFirstParsedCmd();
    inline const Command *getNextCmd();
    inline const Command *getNextParsedCmd();
    inline void parseCmds( const Variable &var_eval, PassInstance *pn );
    inline unsigned long cmdsLength() const;
    inline StringArray getLocalVarValue() const;
    inline StringArray getLocalVar()      const;

  protected:

    Commands             cmds;
    BitSet               typemask;   // The kind of node. See OP_ constants in
                                     // constants.hpp
};

/******************************************************************************
 */
inline Commandable::Commandable()
  : typemask( 64 )
{
  typemask.set( Constants::OP_UNKNOWN );
}

/******************************************************************************
 */
inline Commandable::Commandable( const Commandable &c )
  : cmds( c.cmds ), typemask( 64 )
{
  typemask.set( Constants::OP_UNKNOWN );

  // Only 'copy' PRE/POST CMDS setttings for now.
  if (typemask.get( Constants::OP_PRECMDS ))
    typemask.set( Constants::OP_PRECMDS );
  if (typemask.get( Constants::OP_POSTCMDS ))
    typemask.set( Constants::OP_POSTCMDS );
}

/******************************************************************************
 */
inline void Commandable::setType( int type )
{
  typemask.set( type );
}

/******************************************************************************
 */
inline void Commandable::clearType( int type )
{
  typemask.clear( type );
}

/******************************************************************************
 */
inline boolean Commandable::isSet( int type )
{
  return (typemask.get( type ));
}

/************************************************
  *       --- inline void addCmd ---
  *
  ************************************************/
inline void Commandable::addCmd( const Command &c )
{
  cmds.addCmd( c );
}

inline void Commandable::addPreCmd( const Command &c )
{
  cmds.addPreCmd( c );
}

inline void Commandable::addPrePostCmd( const Command &c )
{
  cmds.addPrePostCmd( c );
}

inline void Commandable::prepareForCmds()
{
  cmds.prepareForCmds();
}

/************************************************
  *       --- inline void addCmds ---
  *
  ************************************************/
inline void Commandable::addCmds( const Vector <Command> &new_cmds )
{
  cmds.addCmds( new_cmds );
}

/************************************************
  *     --- inline boolean hasCmds ---
  *
  ************************************************/
inline boolean Commandable::hasCmds()
{
  return( cmds.hasCmds() );
}

/************************************************
  --- inline const Vector <Command>  &Commandable::getCmds ---
  *
  ************************************************/
inline const Vector <Command>  &Commandable::getCmds() const
{
  return( cmds.getCmds() );
}

/************************************************
  *     --- inline void removeCmds ---
  *
  ************************************************/
inline void Commandable::removeCmds()
{
  cmds.removeCmds();
}

/************************************************
  *     --- inline void setFirstCmd ---
  *
  ************************************************/
inline void Commandable::setFirstCmd()
{
  cmds.setFirstCmd();
}

/************************************************
  *  --- inline void setFirstParsedCmd ---
  *
  ************************************************/
inline void Commandable::setFirstParsedCmd()
{
  cmds.setFirstParsedCmd();
}

/************************************************
  *--- inline const Command *getNextCmd ---
  *
  ************************************************/
inline const Command *Commandable::getNextCmd()
{
  return( cmds.getNextCmd() );
}

/************************************************
  * --- inline const Command *getNextParsedCmd ---
  *
  ************************************************/
inline const Command *Commandable::getNextParsedCmd()
{
  return( cmds.getNextParsedCmd() );
}

/************************************************
  *      --- inline void parseCmds ---
  *
  ************************************************/
inline void Commandable::parseCmds( const Variable &var_eval, PassInstance *pn )
{
  cmds.parseCmds( var_eval, pn );
}

/************************************************
  *   --- inline unsigned long cmdsLength ---
  *
  ************************************************/
inline unsigned long Commandable::cmdsLength() const
{
  return( cmds.length() );
}


/***************************************************
  * inline StringArray Commandable::getLocaVarValue
  *
  **************************************************/
inline StringArray Commandable::getLocalVarValue() const
{
  return( cmds.getLocalVarValue() );
}

/***************************************************
  * inline StringArray Commandable::getLocaVar
  *
  **************************************************/
inline StringArray Commandable::getLocalVar() const
{
  return( cmds.getLocalVar() );
}


#endif //_ODE_BIN_MAKE_CMDABLE_HPP_
