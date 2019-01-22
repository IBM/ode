#ifndef _ODE_BIN_MAKE_TARGETNODE_HPP_
#define _ODE_BIN_MAKE_TARGETNODE_HPP_

#include "base/odebase.hpp"

#include "lib/io/path.hpp"
#include "lib/io/ui.hpp"
#include "lib/portable/vector.hpp"
#include "lib/string/string.hpp"
#include "lib/string/setvars.hpp"
#include "lib/util/cachfile.hpp"

#include "bin/make/constant.hpp"
#include "bin/make/graphnd.hpp"
#include "bin/make/mkcmdln.hpp"

/******************************************************************************
 * Target graph node utility specifically for mk target definitions.
 */

class MakefileStatement;
class PassInstance;
class Job;

class TargetNode : public GraphNode
{
  friend class Job;

  public:
    enum
    {
      NEEDS_CMDS = 0,
      DONE_CMDS  = 1
    }; // used for variable cmds_states

    enum
    {
      FORCED,
      MOD,
      NOTEXIST,
      OODWRT,
      UTD
    }; // used for target make type

    inline TargetNode( const String &identifier, const MakefileStatement *mfs,
                       PassInstance *pass = 0 );

    inline virtual boolean  operator==( const TargetNode &rhs ) const;
    inline virtual boolean  operator==( const GraphNode &rhs ) const;
    int             markOODate( GraphNode *parentnode );
    int             execCmds( boolean add_last = true );
    int             finish( int job_state = MADE,
                            const MakefileStatement *errmfs = 0 );
    inline void     setCF( CachedFile *new_cf );
    inline const String &getPathname() const;
    inline const String &getPrefix() const;
    inline const String &getSuff() const;
    inline const String &getArchName() const;
    inline const String &getMembName() const;
    inline const Vector< TargetNode * > &getLinks() const;
    inline const Vector< const MakefileStatement * > &getDelayedMFS() const;
    inline void     setImpSrc( TargetNode *new_impsrc );
    inline void     setTarget();
    inline boolean  isSourceOnly();
    inline boolean  isSymbolicLink();
    inline int      getCmdsStates() const;
    inline void     setCmdsStates( int cmds_states );
    inline int      getCmdType() const;
    inline int      getNodeType() const;
    void            determineSuffix();
    inline void     addCmd( const Command &c );
    inline void     addPreCmd( const Command &c );
    inline void     addPrePostCmd( const Command &c );
    void            addLinkedTargs( Vector< TargetNode * > *tgts );
    int             update( GraphNode *parentnode,
                            PassInstance *pass_reference,
                            Vector< String > *tgts );
    virtual int     updateTarget( GraphNode *parentnode,
                            Vector< String > *tgts );
    TargetNode     *createAndInsertChild( const String &srcname,
                            const PassInstance *passnd,
                            const MakefileStatement *mfs,
                            GraphNode *srcnd = 0 );
    inline void     setTargetmkType( int tgt_mktype);
    inline int      getTargetmkType() const;
    void            printModTimeInfo();
    void            printNodeInfo( boolean print_delayed_children );
    void            printNodeBOM();
    inline void     setDefnLineSrc( const MakefileStatement *defnls );
    inline boolean  hasChildren();
    inline const String &getDelayedChildren() const;
    void            addDelayedChildren( String more_children,
                                        const MakefileStatement *mfs,
                                        boolean allLinks=true );
    void            removeChildren( boolean allLinks=true );
      // throw (ParseException)
    void            parseSources( const String &srcs);

      // throws ParseException
    inline CachedFile *getCF();
           boolean  removeTargetFile();
    inline void     setArchName( const String &name );
    inline void     setMembName( const String &name );
    inline SetVars *getLocalVars() const;
    inline void     freeLocalVars();
    const  Command *getNextParsedCmd();
    boolean         linkedTargNeedsUpdate( long youngChildTime );
    CachedFile     *getCachedFile( PassInstance *pass );
#ifdef __WEBMAKE__
#ifdef __WEBDAV__
    void            WEBDAV_autoExtract_dependency();
#endif
#endif

    ~TargetNode()
    {
      freeLocalVars();
    };

  protected:
    int updateChildren(
      GraphNode *parentnode,
      PassInstance *passnode, Vector< String > *tgts );

  private:
    inline long     getModTime() const;
    String          getModTimeString();
    void            setLocalVars();
    int             touchTarget();
    void            reloadCachedFile();
    inline void     setChildsModTime( TargetNode *childnd );
    void            setLinkedTargsState( int instate );

  private:
    String      suffix;          // Target suffix
    String      prefix;          // Target prefix.  prefix+suffix=targetname
    TargetNode *impsrc;          // The implied source for this target
    CachedFile  *cf;             // The associated cached file
    String delayed_children;     // Children that delayed until target nodes
                                 // creation and will need to be parsed later
    Vector< const MakefileStatement * >  child_mfs;   // Keep track of line #
                                 // delayed_children are specified.
    Vector< GraphNode * > oodate;// A list of children that are out-of-date.
    Vector< TargetNode * > linkedTargs;
    int          cmds_states;    // Whether this target need add more commands
    int          tgt_mktype;     // The reason to build the target
    String       arch;           // Archive name if this is an archive target
    String       memb;           // Member name if this is an archive target
    long         cmtime;         // The youngest child's modification time
    PassInstance *passnode;
    SetVars      *local_vars;
    SetVars      *local_runtime_vars;
    const MakefileStatement *defnmfs;  // Target definition file/line

};

inline TargetNode::TargetNode( const String &identifier,
  const MakefileStatement *mfs, PassInstance *pass )
  : GraphNode( Path::unixize( identifier ), mfs ), impsrc( 0 ),
    cf ( 0 ), cmds_states( NEEDS_CMDS ),
    cmtime( 0 ), passnode( pass ), local_vars( 0 ), local_runtime_vars( 0 ),
    defnmfs ( mfs )
{
  setType( Constants::OP_SOURCEONLY );
  determineSuffix();

  if (MkCmdLine::dTargs() && (identifier != Constants::EMPTY_TARGET))
    Interface::printAlways( "Targ: Creating target node \"" + identifier + "\"" );
}

/******************************************************************************
 *
 */
boolean TargetNode::operator==( const GraphNode &rhs ) const
{
  return GraphNode::equals(rhs);
}

/******************************************************************************
 *
 */
boolean TargetNode::operator==( const TargetNode &rhs ) const
{
  return GraphNode::equals(rhs);
}

/******************************************************************************
 *
 */
CachedFile *TargetNode::getCF()
{
  return (cf);
}


/******************************************************************************
 *
 */
void TargetNode::setCF( CachedFile *new_cf )
{
  cf = new_cf;
}


/******************************************************************************
 *
 */
const String &TargetNode::getPathname() const
{
  if (cf == 0)
    return (id);
  else
    return (cf->toString());
}


/******************************************************************************
 */
const String &TargetNode::getPrefix() const
{
  return (prefix);
}

/******************************************************************************
 */
const String &TargetNode::getSuff() const
{
  return (suffix);
}

/************************************************
  * --- const String &TargetNode::getArchName ---
  *
  ************************************************/
const String &TargetNode::getArchName() const
{
  return ( arch );
}

/************************************************
  * --- const String &TargetNode::getMembName ---
  *
  ************************************************/
const String &TargetNode::getMembName() const
{
  return ( memb );
}

/******************************************************************************
 */
void TargetNode::setImpSrc( TargetNode *new_impsrc )
{
  impsrc = new_impsrc;
}

/******************************************************************************
 */
void TargetNode::setTarget()
{
  clearType( Constants::OP_SOURCEONLY );
}

/******************************************************************************
 */
boolean TargetNode::isSourceOnly()
{
  return (isSet( Constants::OP_SOURCEONLY ));
}

/******************************************************************************
 */
boolean TargetNode::isSymbolicLink()
{
  return (isSet( Constants::OP_LINK ));
}

/******************************************************************************
 */
int TargetNode::getCmdsStates()  const
{
  return (cmds_states);
}

/******************************************************************************
 */
void TargetNode::setCmdsStates( int cmds_states )
{
  this->cmds_states = cmds_states;
}

/******************************************************************************
 */
long TargetNode::getModTime() const
{
  // If the special source .LINK was used then return the link's modification
  // time, not the thing it is linking to.
  if (typemask.get( Constants::OP_LINK ))
    return (cf->getLinkModTime());
  else
    return (cf->getModTime());
}

/******************************************************************************
 */
int TargetNode::getNodeType() const
{
  return (TARGET_NODE);
}

/******************************************************************************
 */
int TargetNode::getCmdType() const
{
  return (TARGET_NODE_CMD);
}

/******************************************************************************
 */
inline void TargetNode::setArchName( const String &name )
{
  arch = name;
}

/******************************************************************************
 */
void inline TargetNode::setMembName( const String &name )
{
  memb = name;
}

/******************************************************************************
 */
inline void TargetNode::setChildsModTime( TargetNode *childnd )
{
  if (childnd->getModTime() > cmtime)
    cmtime = childnd->getModTime();

  // If the child is newer than the parent, add it to the parents
  // oodate list
  if (cf != 0 && childnd->getModTime() > getModTime())
    oodate.addElement( childnd );
}

/******************************************************************************
 */
inline SetVars *TargetNode::getLocalVars() const
{
  return (local_runtime_vars);
}
/******************************************************************************
 */
inline void TargetNode::freeLocalVars()
{
  if (local_vars != 0)
  {
    delete local_vars;
    local_vars = 0;
  }
  if (local_runtime_vars != 0)
  {
    delete local_runtime_vars;
    local_runtime_vars = 0;
  }
}

/************************************************
  * --- int TargetNode::getTargetmkType ---
  *
  ************************************************/
int TargetNode::getTargetmkType()  const
{
  return (tgt_mktype);
}

/************************************************
  *--- void TargetNode::setTargetmkType ---
  *
  ************************************************/
void TargetNode::setTargetmkType( int tgt_mktype )
{
  this->tgt_mktype = tgt_mktype;
}

/************************************************
  * --- void TargetNode::setDefnLineSrc ---
  *
  ************************************************/
void TargetNode::setDefnLineSrc( const MakefileStatement *defnls )
{
  this->defnmfs = defnls;
}

/************************************************
  * --- boolean TargetNode::hasChildren ---
  *
  ************************************************/
boolean TargetNode::hasChildren()
{
  return ( delayed_children.length() > 0 || children.length() > 0 );
}

/************************************************
  * --- const String &TargetNode::getDelayedChildren ---
  *
  ************************************************/
const String &TargetNode::getDelayedChildren() const
{
  return ( delayed_children );
}


inline void TargetNode::addCmd( const Command &c )
{
  Commandable::addCmd( c );

  // Add to all linked targets as well.
  VectorEnumeration< TargetNode * > enumlinks( &linkedTargs );
  while (enumlinks.hasMoreElements())
  {
    ((Commandable *)(*enumlinks.nextElement()))->addCmd( c );
  }
}

inline void TargetNode::addPreCmd( const Command &c )
{
  Commandable::addPreCmd( c );

  // Add to all linked targets as well.
  VectorEnumeration< TargetNode * > enumlinks( &linkedTargs );
  while (enumlinks.hasMoreElements())
  {
    ((Commandable *)(*enumlinks.nextElement()))->addPreCmd( c );
  }
}

inline void TargetNode::addPrePostCmd( const Command &c )
{
  Commandable::addPrePostCmd( c );

  // Add to all linked targets as well.
  VectorEnumeration< TargetNode * > enumlinks( &linkedTargs );
  while (enumlinks.hasMoreElements())
  {
    ((Commandable *)(*enumlinks.nextElement()))->addPrePostCmd( c );
  }
}

inline const Vector< TargetNode * > &TargetNode::getLinks() const
{
  return linkedTargs;
}

inline const Vector< const MakefileStatement * > &TargetNode::getDelayedMFS() const
{
  return child_mfs;
}

#endif //_ODE_BIN_MAKE_TARGETNODE_HPP_
