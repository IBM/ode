//***********************************************************************
//* MakefileStatement
//*
//***********************************************************************
#ifndef _ODE_BIN_MAKE_MFSTMNT_HPP_
#define _ODE_BIN_MAKE_MFSTMNT_HPP_

#include <base/odebase.hpp>

#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/strcon.hpp"
#include "lib/util/finforep.hpp"

// Can't include "bin/make/makefile.hpp" since it includes this header
//
class Makefile;

class MakefileStatement : public FileInfoReportable
{

  friend class Makefile;

  public:
    // Constructor
    inline MakefileStatement();
    inline MakefileStatement( String *initlinedata,
                       int initline,
                       int initendline,
                       Makefile *initmf);

    // Copy Constructor
    inline MakefileStatement( const MakefileStatement &rhs );

    // Destructor
    virtual ~MakefileStatement();

    /**
     * Override equal operator. We don't need to compare the contents of
     * and newlinedata according to the meaning of MakefileStatement
    **/
    inline boolean operator==( const MakefileStatement &rhs ) const;

    /**
     * Override assignment operator
    **/
    inline MakefileStatement &operator=( const MakefileStatement &rhs );
    virtual int getLineNumber() const;
    virtual const String &getPathname() const;
    inline int getEndLineNumber() const;
    inline char getFirstChar() const;
    inline const String *getLineString ( ) const;
    inline void setData( int line_num, int endline_num,
      Makefile *mf);
    /**
     * The caller shouldn't deallocate the returned Makefile pointer
    **/
    inline Makefile *getMakefile() const;

    /**
     *  Used for the compatibility with Java(Object can be NULL)
    **/
    inline boolean isNULL() const;

  protected:
    inline String *getStringPtr();

  private:
   String    newlinedata; // The line without continuation
   int       line;        // The line number of this line
   int       endline;     // The line number of the last line of the continuation
   Makefile *mf;          // The makefile that this statement was generated from

};

// Constructor
inline MakefileStatement::MakefileStatement()
  : line( 0 ), endline( 0 ), mf( 0 )
{ }

inline MakefileStatement::MakefileStatement( String *initlinedata,
                       int initline,
                       int initendline,
                       Makefile *initmf) :
  line( initline ),
  endline( initendline ),
  mf( initmf )
{
  if (initlinedata != 0)
    newlinedata = *initlinedata;
}

// Copy Constructor
inline MakefileStatement::MakefileStatement( const MakefileStatement &rhs ) :
  newlinedata( rhs.newlinedata ),
  line( rhs.line ),
  endline( rhs.endline ),
  mf( rhs.mf )
{ }

/**
 * Override equal operator. We don't need to compare the contents of
 * newlinedata according to the meaning of MakefileStatement
**/
inline boolean MakefileStatement::operator==(
  const MakefileStatement &rhs ) const
{
  return (mf == rhs.mf && line == rhs.line && endline == rhs.endline);
}

/**
 * Override assignment operator
 **/
inline MakefileStatement &MakefileStatement::operator=(
  const MakefileStatement &rhs )
{
  // Prevent copying ourself
  if (this == &rhs)
    return (*this);

  newlinedata = rhs.newlinedata;
  line = rhs.line;
  endline = rhs.endline;
  mf = rhs.mf;

  return (*this);
}

inline int MakefileStatement::getEndLineNumber() const
{
  return (endline);
}

inline char MakefileStatement::getFirstChar() const
{
  return (newlinedata[STRING_FIRST_INDEX]);
}

inline const String *MakefileStatement::getLineString ( ) const
{
  return (&newlinedata);
}

inline void MakefileStatement::setData( int line_num, int endline_num,
  Makefile *mf)
{
  line      = line_num;
  endline   = endline_num;
  this->mf  = mf;
}

/**
 * The caller shouldn't deallocate the returned Makefile pointer
**/
inline Makefile *MakefileStatement::getMakefile() const
{
  return (mf);
}

/**
 *  Used for the compatibility with Java(Object can be NULL)
**/
inline boolean MakefileStatement::isNULL() const
{
  return ((mf == 0) ? true : false);
}

inline String *MakefileStatement::getStringPtr()
{
  return (&newlinedata);
}

#endif //_ODE_BIN_MAKE_MFSTMNT_HPP_
