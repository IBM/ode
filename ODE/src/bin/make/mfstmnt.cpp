/**
 * MakefileStatement
 *
**/
using namespace std;
#define _ODE_BIN_MAKE_MFSTMNT_CPP_

#include <base/binbase.hpp>
#include "bin/make/mfstmnt.hpp"
#include "bin/make/makefile.hpp"

// Destructor
MakefileStatement::~MakefileStatement()
{
}

// This needs to stay in the CPP file since it causes conflicts in the
// header file with "bin/make/makefile.hpp"
//
const String &MakefileStatement::getPathname() const
{
  return ((mf == 0) ? StringConstants::EMPTY_STRING : mf->getPathname());
}

int MakefileStatement::getLineNumber() const
{
  return (line);
}
