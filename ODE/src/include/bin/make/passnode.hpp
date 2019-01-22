/**
 * PassNode
 *
**/
#ifndef _ODE_BIN_MAKE_PASSNODE_HPP_
#define _ODE_BIN_MAKE_PASSNODE_HPP_


#include "base/odebase.hpp"
#include "lib/exceptn/ioexcept.hpp"
#include "lib/exceptn/mfvarexc.hpp"
#include "lib/portable/array.hpp"
#include "lib/portable/vector.hpp"
#include "lib/io/ui.hpp"
#include "lib/io/path.hpp"
#include "lib/portable/env.hpp"
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/setvars.hpp"
#include "lib/string/variable.hpp"
#include "lib/util/condeval.hpp"

#include "bin/make/graph.hpp"
#include "bin/make/graphnd.hpp"
#include "bin/make/passinst.hpp"
#include "bin/make/keyword.hpp"
#include "bin/make/constant.hpp"
#include "bin/make/makec.hpp"
#include "bin/make/mkcmdln.hpp"
#include "bin/make/makefile.hpp"
#include "bin/make/sufftrfm.hpp"
#include "bin/make/patterns.hpp"
#include "bin/make/cmdable.hpp"

class SuffixTransforms;
class Patterns;
class TargetNode;
class MakefileStatement;
class Dir;

/** A Pass is defined to be the path of subdirectories to
 *  traverse. An associated action is performed on a Pass.
 */
class PassNode : public GraphNode, public PassInstance
{
  friend class Make;

  public:
    // constructor
    PassNode(const String &initname, const String &initactionname, const String &initcwd,
             const String &initmakedir, const String &initmaketop,
             Makefile *initmakefile, Makefile *initparentmakefile,
             PassNode *initparentdirpass, const StringArray *initsearchpath);
    PassNode(const String &initname, const String &initactionname, const String &initcwd,
             const String &initmakedir, const String &initmaketop,
             Makefile *initmakefile, Makefile *initparentmakefile,
             const StringArray *initsearchpath);
    virtual ~PassNode();

    // operators overload
    inline virtual boolean operator==( const GraphNode &rhs ) const;
    inline char *toCharPtr();
    virtual const String &hashName() const;

    inline int               getNodeType() const;
    void                     doDelayedConstruction();
    inline boolean           equals( const GraphNode &obj ) const;
    inline void              setVirtualCwd( const String &newcwd );
    inline const String     &getCwd( ) const;
    inline Graph            *getTgtGraph() const;
    inline SuffixTransforms *getSuffTransforms() const;
    inline Patterns         *getPatterns() const;
    void                     updateSearchPathsbyVPATH( Suffix *suff=0 );
      // throw (ParseException)
    void                     buildNewSearchPath(
                               const StringArray &suffcorepath,
                                     Dir         *path );
    int                      update( GraphNode *parentnode, PassInstance *passnode,
                                     Vector<String> *tgts );
      // throw (ParseException)
    inline Dir              *getSearchPath() const;
    inline StringArray      *getDefaultSearchPath() const;
    inline StringArray      *getCoreSearchPath() const;
    inline Dir              *getSysSearchPath() const;
    inline SetVars          *getGlobalVars() const;
    inline SetVars          *getCmdLineVars() const;
    inline SetVars          *getRootVars()   const;
    inline void              setMakefile( Makefile *mf );
    inline SetVars          *getEnvironVars() const;
    inline StringArray      *getEnvironVarsArr() const;
    inline char            **getEnvironVarsCharStarArr() const;
    inline void              setEnvironVarsArr();
    inline const String     *findVar( const String &varname ) const;
    inline const String     &getMakeDir() const;
    inline const String     &getMakeTop() const;
    inline Makefile         *getParentMF() const;
    void                     parse();
      // throw (ParseException)
    inline StringArray      *parseUntil(const String &str, const String &until_chars,
                                     boolean allow_escaped_vars, StringArray *buf=0,
                                     boolean backslash_escape = false);
      // throw (MalformedVariable)
    static void              parse( PassNode &curpass, Makefile &makefile );
      // throw (ParseException)
    inline void              setDotPathVar();
    inline boolean           isMakeconf() const;
    inline boolean           isPrecious() const;
    void                     orderSources( StringArray &srcnames );
    void                     linkSources( StringArray &srcnames );
    void                     printNodeInfo( boolean print_delayed_children );
    void                     printNodeBOM();

    // These methods are PassInstance interface methods.
    inline Variable         *getVarEval() const;
    inline boolean           exists( const String &name ) const;
    inline boolean           isTarget( const String &tgt ) const;
    boolean                  isMainTarget( const String &tgt ) const;
    inline int               getCmdType() const;

  private:

    String                   virtual_cwd;        // The "current working directory"
    String                   make_top;           // Relative path to top of source tree.
    String                   make_dir;           // Relative path from top of source tree.
    Makefile                *mf;                 // The current Makefile being processed.
    Makefile                *parentmf;           // The top included makefile being processed.
    Graph                   *tgtgraph;           // Target dependencies for current directory
    SetVars                 *environ_vars;       // The current set of environment variables.
    SetVars                 *global_vars;        // Variables defined in makefiles
    SetVars                 *cmdline_vars;       // Variables defined on command line
    SetVars                 *root_vars;          // Root of all "find" calls.  Used to implement -e.
    Variable                *var_eval;           // Used to evaluate variables
    char                     firstchar;          // The first char of Current makefile line being parsed
    String                   linestr;            // Current makefile line being parsed
    Array< Commandable * >   tgtlst;             // An array of target nodes just visited
    SuffixTransforms        *transforms;         // Suffix transformations
    Patterns                *patterns;           // Pattern Matching transforms
    PassNode                *parentdirpass;      // Parent directory PassNode
    Vector<PassNode *>      *subdirpasses;       // List of subdirectories to visit
    String                   actionname;         // The action for this pass.
    StringArray             *defaultsearchpath;  // Path used as initial search paths.
    StringArray             *coresearchpath;     // Path used as for VPATH appending.
    StringArray             vpaths_abs;          // Absolute vpath array
    StringArray             vpaths_rel;          // Relative vpath array
    Dir                     *searchpath;         // Path used to search for targets, sources, makefiles.
    StringArray             *environ_varsarr;    // The StringArray version of the environment
    char                   **environ_charstararr;// The char ** version of the environment
    boolean                  makeincludecompat;  // To indicate old include compatibility
    String                   passndid;           // The id of this passnode
    boolean                  is_parsed;
    boolean                  is_precious;        // All targets are precious after .PRECIOUS:


    inline void              addSubDirPass( PassNode *passnd );
    StringArray             *getPassSubDirs( StringArray *buf=0 ) ;
      // throw (ParseException)
    Vector< String >        *getPassTargets( GraphNode *parentnode );
      // throw (ParseException)
    void                     updateLocalTargets( GraphNode *parentnode,
                                                    Vector< String > *tgts );
      // throw (ParseException)
#ifdef __WEBMAKE__
#ifdef __WEBDAV__
    void                     WEBDAV_autoExtract_dependency();
#endif // __WEBDAV__
    void                     extractIncludeMakeFile(String includeMakeFile);
#endif // __WEBMAKE__

    /***************************************************************
     *  Append the subdir to the end of the default search strings.
     *  the caller should deallocate the returned pointer or pass *buf
     */
    StringArray             *createDefaultSearchPath( const String &subdir,
                                                        StringArray *buf=0 );
    int                      updateSubDirTargets( StringArray subdirs );
      // throw (ParseException)

    /***************************************************************
     * Update the given target name.  If it doesn't exist then don't complain.
     */
    int                      updateTarget( const String &tgt );
      // throw (ParseException)
    void                     initVars();
    void                     reinit( Makefile &initmakefile );
    boolean                  parseDotDirectives( boolean parseskip,
        CondEvaluator &condeval,
        VectorEnumeration< MakefileStatement > &mf_enum );
      // throw (ParseException)
    void                     parseUnDef();
      // throw (ParseException)
    void                     parseInclude( boolean tryinclude );
      // throw (ParseException)
    void                     parseDep( const String &tgts, const String &srcnms, int deptype );
      // throw (ParseException)
    StringArray             *parseSuffix( const String &tgtname, StringArray *buf=0 );
    boolean                  parseSuffixDir( String *suffix, String *suffixdir,
                                             boolean rhs=false );
    void                     parseVar( const String &varname, String val, char op );
      // throw (ParseException)
    void                     parseVarOrDep( );
      // throw (ParseException)
    void                     markAsPrecious( const StringArray &srcs );
      // throw (ParseException)

    void                     setVars( const String &lhs, const String &rhs, char op );
    int                      findDot( const String &srcs, int index ) const;
    boolean                  isSpecialSource( const String &srcs, const String &spec_src, int &index ) const;
    boolean                  startsWithDotKeyword( const String &str );
};

inline boolean PassNode::operator==( const GraphNode &rhs ) const
{
  return (this->equals( rhs ));
}

inline char *PassNode::toCharPtr()
{
  return (passndid.toCharPtr());
}

/***************************************************************
* Define equals to be if objects are both of type PassNode and there action
* and pass names match.
*/
inline boolean PassNode::equals( const GraphNode &obj ) const
{
  if (obj.getNodeType() != PASS_NODE)
    return ( false );
  const PassNode *objpn = (const PassNode *) &obj;
  return (id.equals(obj.nameOf()) && actionname.equals(objpn->actionname));
}

inline int PassNode::getNodeType() const
{
  return (PASS_NODE);
}
inline void PassNode::setVirtualCwd( const String &newcwd )
{
  virtual_cwd = newcwd;
  global_vars->set(Constants::CURDIR, newcwd, true);
  global_vars->set(Constants::DOT_CURDIR, newcwd, true); // obsolete
}

inline const String &PassNode::getCwd( ) const
{
  return (virtual_cwd);
}

inline Graph *PassNode::getTgtGraph() const
{
  return (tgtgraph);
}

inline Patterns *PassNode::getPatterns() const
{
  return patterns;
}

inline SuffixTransforms *PassNode::getSuffTransforms() const
{
  return transforms;
}

inline void PassNode::addSubDirPass( PassNode *passnd )
{
  subdirpasses->addElement(passnd);
}

inline Dir *PassNode::getSearchPath() const
{
  return (searchpath);
}

inline StringArray *PassNode::getDefaultSearchPath() const
{
  return (defaultsearchpath);
}
inline Dir *PassNode::getSysSearchPath() const
{
  return ((Dir *)&(Make::mk->sysSearchPath));
}

inline StringArray *PassNode::getCoreSearchPath() const
{
  return (coresearchpath);
}

inline SetVars *PassNode::getGlobalVars() const
{
  return (global_vars);
}

inline SetVars *PassNode::getCmdLineVars() const
{
  return (cmdline_vars);
}

inline SetVars *PassNode::getRootVars()   const
{
  return (root_vars);
}

inline void PassNode::setMakefile( Makefile *mf )
{
  global_vars->set( Constants::MAKEFILE, mf->getPathname(), true );
  parentmf = mf;
}

inline SetVars *PassNode::getEnvironVars() const
{
  return (environ_vars);
}

inline StringArray *PassNode::getEnvironVarsArr() const
{
  return (environ_varsarr);
}

inline char **PassNode::getEnvironVarsCharStarArr() const
{
  return (environ_charstararr);
}

inline void PassNode::setEnvironVarsArr()
{
  environ_varsarr = environ_vars->getAll( global_vars );
  environ_charstararr = environ_varsarr->toCharStarArray();
}

/***************************************************************
 * Used to find a variable.  It takes care of the Environment/Global variable
 * search hierarchy.  See the -e flag for details.
 */
inline const String *PassNode::findVar( const String &varname ) const
{
  return (root_vars->find( varname ));
}

inline StringArray *PassNode::parseUntil(const String &str, const String &until_chars,
                        boolean allow_escaped_vars, StringArray *buf,
                        boolean backslash_escape )
  // throw (MalformedVariable)
{
  if (MkCmdLine::dpVars())
  {
    VarInfoPrinter ip;
    return (var_eval->parseUntil( str, until_chars, allow_escaped_vars,
                                  backslash_escape, buf, &ip ));
  }
  else
    return (var_eval->parseUntil( str, until_chars, allow_escaped_vars,
                                  backslash_escape, buf ));
}

inline void PassNode::setDotPathVar()
{
  const StringArray *paths = getSearchPath()->getPath();
  if (paths == 0)
    getGlobalVars()->set( Constants::DOT_PATH, String(), true );
  else
    getGlobalVars()->set( Constants::DOT_PATH, paths->join(
        StringConstants::SPACE ), true );
}

inline boolean PassNode::isMakeconf() const
{
  return (this == Make::mk->mkconfpass);
}

inline boolean PassNode::isPrecious() const
{
  return (is_precious);
}

// These methods are PassInstance interface methods.
inline Variable *PassNode::getVarEval() const
{
  return (var_eval);
}

inline boolean PassNode::exists( const String &name ) const
{
  return (searchpath->find( name ) != 0);
}

inline boolean PassNode::isTarget( const String &tgt ) const
{
  return (getTgtGraph()->find( tgt ) != 0);
}

/************************************************
  * --- inline const String &PassNode::getMakeDir ---
  *
  ************************************************/
inline const String &PassNode::getMakeDir( ) const
{
  return ( make_dir );
}

/************************************************
  * --- inline const String &PassNode::getMakeTop ---
  *
  ************************************************/
inline const String &PassNode::getMakeTop( ) const
{
  return ( make_top );
}

/************************************************
  * --- inline const Makefile PassNode::getparentMF ---
  *
  ************************************************/
inline Makefile *PassNode::getParentMF( ) const
{
  return ( parentmf );
}

inline int PassNode::getCmdType() const
{
  return (PASS_NODE_CMD);
}

#endif //_ODE_BIN_MAKE_PASSNODE_HPP_

