//***********************************************************************
//* PassInstance
//*
//***********************************************************************
#ifndef _ODE_BIN_MAKE_PASSINST_HPP_
#define _ODE_BIN_MAKE_PASSINST_HPP_

#include <base/odebase.hpp>
#include "lib/portable/array.hpp"
#include "lib/portable/vector.hpp"
#include "lib/string/setvars.hpp"
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/variable.hpp"
#include "lib/util/minforep.hpp"

#include "bin/make/linesrc.hpp"

class Dir;
class Graph;
class SuffixTransforms;
class Patterns;

class PassInstance : public MakeInfoReportable
{
  public:

    virtual SetVars          *getGlobalVars() const = 0;
    virtual SetVars          *getRootVars() const = 0;
    virtual SetVars          *getEnvironVars() const = 0;
    virtual SetVars          *getCmdLineVars() const = 0;
    virtual StringArray      *getEnvironVarsArr() const = 0;
    virtual char            **getEnvironVarsCharStarArr() const = 0;
    virtual Variable         *getVarEval() const = 0;
    virtual Dir              *getSearchPath() const = 0;
    virtual StringArray      *getDefaultSearchPath() const = 0;
    virtual StringArray      *getCoreSearchPath() const = 0;
    virtual const String     &getCwd() const = 0;
    virtual Graph            *getTgtGraph() const = 0;
    virtual SuffixTransforms *getSuffTransforms() const = 0;
    virtual Patterns         *getPatterns() const = 0;
    virtual const String     &getMakeDir() const = 0;
    virtual const String     &getMakeTop() const = 0;
    virtual Makefile         *getParentMF() const = 0;
};

#endif //_ODE_BIN_MAKE_PASSINST_HPP_
