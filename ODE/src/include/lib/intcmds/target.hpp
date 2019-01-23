//***********************************************************************
//* Target
//*
//***********************************************************************
#ifndef _ODE_LIB_IO_TARGET_HPP_
#define _ODE_LIB_IO_TARGET_HPP_

#include <fstream>
#include "lib/intcmds/body.hpp"

#include "lib/io/ui.hpp"
#include "lib/portable/platcon.hpp"

class Target
{
    
  // Data Members
  //
  private:
    String    header;
    Body      *body;
    // are the dependents from depend.mk?  If so, the following
    // boolean is set to true (by default).  When a .u file contains
    // a target that existed in depend.mk, the old ones will be removed,
    // this boolean is then set to false, and finally the new set is added.
    // when the target isn't already in depend.mk, this flag will be
    // set to false on construction.
    boolean old_target;

  public:
    /**
     * Constructor
     */

    Target( const String& aHeader, Body *aBody, boolean from_oldfile = true )
      : header( aHeader ), body( aBody ), old_target( from_oldfile ) {;}

  private:
    // Don't allow a copy of this class since its members go
    // very deep
    Target( const Target &tgt )  // Illegal !
    { ; }

  public:

    ~Target() 
    {
      delete body;
      body = 0;
    }

    void print() const
    { 
      body->print();
    }

    inline void write( fstream *fileptr ) const
    {
      body->write( fileptr );
    }
   
    String getHeader() const 
    {
      return (header);
    }

    Body* getBody() const
    {
      return (body);
    }

    boolean isFromOldFile() const
    {
      return (old_target);
    }

    // this is used when updating a target (which existed in
    // depend.mk) for the first time (see old_target comment above).
    void purgeForUpdate()
    {
      body->clear();
      old_target = false;
    }

    /**
     * Compares two Targets by comparing their header's names.
    **/
    inline boolean operator==( const Target &tgt ) const;
};

inline boolean Target::operator==( const Target &tgt ) const
{
#ifdef CASE_INSENSITIVE_OS
  return (header.toLowerCase() == tgt.getHeader().toLowerCase());
#else
  return (header == tgt.getHeader());
#endif
}

#endif //_ODE_LIB_IO_TARGET_HPP_
