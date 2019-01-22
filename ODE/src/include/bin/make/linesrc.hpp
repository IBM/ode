//***********************************************************************
//* Make
//*
//***********************************************************************
#ifndef _ODE_BIN_MAKE_LINESRC_HPP_
#define _ODE_BIN_MAKE_LINESRC_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"

#include "bin/make/mfstmnt.hpp"

class LineSource {
  public:
    const MakefileStatement *mfs;  // The makefile line corresponding to this command

    LineSource() : mfs( 0 )
      { }
    LineSource( const MakefileStatement *mfline )
      : mfs( mfline )
      { }

    virtual ~LineSource()  {};

    // Public access methods.
    //
    inline const String &getPathName() const;
    inline const String *getLine();
    inline char getFirstChar();
    inline int getBeginLine();
    inline int getEndLine();

};

inline const String &LineSource::getPathName() const
{
  return (mfs->getPathname());
}

inline const String *LineSource::getLine()
{
  return mfs->getLineString();
}

inline char LineSource::getFirstChar()
{
  return mfs->getFirstChar();
}

inline int LineSource::getBeginLine()
{
  return mfs->getLineNumber();
}

inline int LineSource::getEndLine()
{
  return mfs->getEndLineNumber();
}

#endif //_ODE_BIN_MAKE_LINESRC_HPP_
