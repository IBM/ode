//***********************************************************************
//* Make
//*
//***********************************************************************
#ifndef _ODE_BIN_MAKE_GRAPH_HPP_
#define _ODE_BIN_MAKE_GRAPH_HPP_


#include "base/odebase.hpp"
#include "lib/io/path.hpp"
#include "lib/portable/hashtabl.hpp"
#include "lib/string/string.hpp"

#include "bin/make/graphnd.hpp"

/**
 * Graph utility created specifically for mk.  It handles
 * the creation of GraphNodes and locating GraphNodes.
 */
class Graph {

  // Methods
  //
  public:
    /**
     * Public constructor, it initializers root to null and creates 
     * the Hashtable used to cache GraphNode's.
     */
    Graph()
      : cache( cachesize )
    { }  

    ~Graph();

    /**
     * Find a GraphNode that has an <I>id</I> matching the given <I>id</I>.
     * A new GraphNode is not create.
     */
    GraphNode *find( const String &id );
    inline GraphNode *find( const GraphNode &id );

    /**
     * Find a GraphNode that has an <I>id</I> matching the given <I>id</I>.
     * If one is not found then the new GraphNode is inserted.
     * Returns the found GraphNode if GraphNode already exists in the graph.
     */
    GraphNode *insert( GraphNode *node );

    /**
     * Find a GraphNode that has an <I>id</I> matching the given <I>id</I>.
     * If one is found then the given GraphNode is inserted over and the
     * existing one is FREED!
     * Returns 0 if GraphNode already exists in the graph.
     */
    GraphNode *insertReplace( GraphNode *node );

    inline boolean isEmpty();

    void printGraph( boolean print_delayed_children );
    void writeBOM();

  // Data Members
  //
  protected:
    Hashtable< SmartCaseString, GraphNode * > cache;

  private:
    static const int cachesize;


};

inline GraphNode *Graph::find( const GraphNode &id )
{
  return (find( id.hashName() ));
}

inline boolean Graph::isEmpty()
{
  return (cache.isEmpty());
}

#endif //_ODE_BIN_MAKE_GRAPH_HPP_






