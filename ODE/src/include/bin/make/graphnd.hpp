//***********************************************************************
//* GraphNode
//*
//***********************************************************************
#ifndef _ODE_BIN_MAKE_GRAPHND_HPP_
#define _ODE_BIN_MAKE_GRAPHND_HPP_


#include "base/odebase.hpp"
#include "lib/exceptn/parseexc.hpp"
#include "lib/portable/array.hpp"
#include "lib/portable/vector.hpp"
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"

#include "bin/make/cmdable.hpp"
#include "bin/make/linesrc.hpp"
#include "bin/make/makec.hpp"
#include "bin/make/mfstmnt.hpp"
#include "bin/make/passinst.hpp"

class Graph;
class SuffixTransforms;
class Patterns;

/**
 * A non-cyclic graph node utility created specifically for mk.  It handles
 * theadditions of parents, children, successors, and predecessors.
 *
 */
class GraphNode : public LineSource, public Commandable
{
  friend class Graph;
  friend class SuffixTransforms;
  friend class Patterns;

  public:
    enum
    {
      UNMADE,      // Not visited yet
      MAKING,      // Inprogress, used to test for cycles.
      MADE,        // Has been made
      UPTODATE,    // Doesn't need to be made
      OUTOFDATE,   // Target is out-of-date and needs to be made
      ERROR        // Error has occured
    };
    enum
    {
      TARGET_NODE,
      PASS_NODE
    };

    /**
     * Public constructor that takes a string identifier and initializes
     * all list structures.
     */
    inline GraphNode( const String &identifier );
    inline GraphNode( const String &identifier, const MakefileStatement *mfs );

    virtual ~GraphNode();
    virtual int getNodeType() const = 0;
    /**
     * An abstract update method.
     */
    virtual int update( GraphNode *parentnode,
      PassInstance *passnode, Vector<String> *tgts ) = 0;
      // throw (ParseException)

    virtual const String &hashName( ) const;

    /**
     * Define a public execCmds to do nothing.  Subclasses may redefine.
     */
    virtual int execCmds( boolean add_last = true );

    /**
     * Add a child to this GraphNode's children list.
     */
    inline GraphNode *addChild( GraphNode *child );

    /**
     * Add a parent to this GraphNode's parents list.
     */
    inline void addParent( GraphNode *parent );

    /**
     * Add a sibling to this node.
     */
    inline void addSibling( GraphNode *sib );

    /**
     * Add a predecessor to this GraphNode's next list.
     */
    inline void addNext( GraphNode *nextnd );

    /**
     * Add a successor to this GraphNode's prev list.
     */
    inline void addPrev( GraphNode *prevnd );

    inline boolean isOrdered() const;

    /**
     * Define equals to be the same as String id equals.
     */
    virtual boolean equals( const GraphNode &obj) const;

    /**
     * Define equals to be the same as String id equals.
     */
    virtual boolean operator==( const GraphNode &obj) const;

    /**
     * Return the name/id of the GraphNode.
     */
    virtual const String &nameOf() const;
    inline Vector<GraphNode *> *getChildren();
    inline Vector<GraphNode *> *getParents();
    inline Vector<GraphNode *> *getSiblings();

    /**
     *  Set/Get the state of this GraphNode
     */
    int         getState();
    void        setState( int instate );
    inline void incNumRunningChildren();
    inline void decNumRunningChildren();
    inline int  getNumRunningChildren() const;
    inline void setNumRunningChildren( int newnum );

    /**
     *  Print node information
     */
    virtual void printNodeInfo( boolean print_delayed_children ) = 0;
    virtual void printNodeBOM() = 0;
    String      getListOf( Vector<GraphNode *> *vect );

  protected:
    String               id;
    Vector<GraphNode *>  parents;    // The node of which this node is a
                                     // source.
    Vector<GraphNode *>  children;   // Sources of this node
    Vector<GraphNode *>  siblings;   // For the '::' operator
    GraphNode            *prev;      // Points to previous ordered node
    GraphNode            *next;      // Points to next ordered node
    short int            num_running_children;

    /**
     * This method orders the children list and places the result in the
     * given Vector parameters.
     */
    void orderChildren( Vector<GraphNode *> &unorderedChildren,
      Vector<GraphNode *> &orderedChildren );
    inline void addOrderedChild(
      Vector< GraphNode *> &orderedChildren,
      GraphNode *new_child );
        // throws ParseException

 private:
    int                  state;      // The state of the graph node

};

/******************************************************************************
 */
inline GraphNode::GraphNode( const String &identifier )
  : LineSource(), Commandable(), id( identifier ), prev( 0 ), next( 0 ),
    state( UNMADE ),
    parents(  5, elementsEqual ),
    children( 5, elementsEqual ),
    siblings( 2, elementsEqual ),
    num_running_children( 0 )
{}

/******************************************************************************
 */
inline GraphNode::GraphNode( const String &identifier,
  const MakefileStatement *mfs )
  : LineSource( mfs ), Commandable(), id( identifier ), prev( 0 ), next( 0 ),
    state( UNMADE ),
    parents(  5, elementsEqual ),
    children( 5, elementsEqual ),
    siblings( 2, elementsEqual ),
    num_running_children( 0 )
{ }

/******************************************************************************
 */
inline void GraphNode::incNumRunningChildren()
{
  num_running_children++;
}

/******************************************************************************
 */
inline void GraphNode::decNumRunningChildren()
{
  // Attempt to revent underflow
  if (num_running_children > 0)
    num_running_children--;
}

/******************************************************************************
 */
inline int GraphNode::getNumRunningChildren() const
{
  return (num_running_children);
}

/******************************************************************************
 */
inline void GraphNode::setNumRunningChildren( int newnum )
{
  num_running_children = newnum;
}

/******************************************************************************
 * Add a child to this GraphNode's children list.
 */
inline GraphNode *GraphNode::addChild( GraphNode *child )
{
  if (child == this)
    return ( 0 );
  else
  {
    if (!children.contains( child ))
      children.addElement(child);

    return (child);
  }
}

/******************************************************************************
 */
inline void GraphNode::addParent( GraphNode *parent )
{
  // Don't need to search if parent is already a child.  This is
  // handled when the given parent was added as a child.
  if (!parents.contains( parent ))
    parents.addElement( parent );
}

/******************************************************************************
 */
inline void GraphNode::addSibling( GraphNode *sib )
{
  // Don't need to search to see if the sibling is already in
  // the siblings list.
  siblings.addElement( sib );
}

/******************************************************************************
 */
inline Vector<GraphNode *> *GraphNode::getChildren()
{
  return (&children);
}

/******************************************************************************
 */
inline Vector<GraphNode *> *GraphNode::getParents()
{
  return (&parents);
}

/******************************************************************************
 */
inline Vector<GraphNode *> *GraphNode::getSiblings()
{
  return (&siblings);
}

/******************************************************************************
 */
inline void GraphNode::addNext( GraphNode *nextnd )
{
  next = nextnd;
}

/******************************************************************************
 */
inline void GraphNode::addPrev( GraphNode *prevnd )
{
  prev = prevnd;
}

/******************************************************************************
 */
inline boolean GraphNode::isOrdered() const
{
  return (prev != 0 || next != 0);
}

/******************************************************************************
 */
inline void GraphNode::addOrderedChild(
  Vector< GraphNode *> &orderedChildren,
  GraphNode *new_child )
  // throws ParseException
{
  if (orderedChildren.contains( new_child ))
  {
     ParseException exp( new_child->mfs->getPathname(),
      new_child->mfs->getLineNumber(),
      String( "Ordered target `" ) + new_child->nameOf() +
      "' is illegally in .ORDER statement twice" );
     Make::quit( exp.getMessage(), 1 );
  }

  orderedChildren.addElement( new_child );
}

#endif //_ODE_BIN_MAKE_GRAPHND_HPP_

