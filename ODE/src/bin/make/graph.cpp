/**
 * Graph
**/
using namespace std;
#define _ODE_BIN_MAKE_GRAPH_CPP_

#include "base/binbase.hpp"
#include "bin/make/graph.hpp"

const int Graph::cachesize=20;

/******************************************************************************
 */
Graph::~Graph()
{
  HashElementEnumeration< SmartCaseString, GraphNode* > enumer( &cache );
  GraphNode * const *gn;
  while (enumer.hasMoreElements())
  {
    gn = enumer.nextElement();
    if (gn != 0)
      delete *gn;
  }
}


/******************************************************************************
 * Find a GraphNode that has an <I>id</I> matching the given <I>id</I>.
 * A new GraphNode is not create.
 */
GraphNode *Graph::find( const String &id )
{
  GraphNode *const *result=cache.get( id );
  if (result == 0)
    return ( 0 );
  return ( *result );
}  

/******************************************************************************
 * Find a GraphNode that has an <I>id</I> matching the given <I>id</I>.
 * If one is not found then the new GraphNode is inserted.
 * Returns the found GraphNode if GraphNode already exists in the graph.
 */
GraphNode *Graph::insert( GraphNode *node )
{
  GraphNode *nd = 0;
  if (node != 0)
  {
    if ((nd = find( node->hashName() )) == 0)
    {
      cache.put(node->hashName(), node);
      return ( node );
    }
    else
    {
      return ( nd );
    }
  }
  else
    return ( 0 );
}  

/******************************************************************************
 * Find a GraphNode that has an <I>id</I> matching the given <I>id</I>.
 * If one is found then the given GraphNode is inserted over and the existing
 * one is FREED!
 * Returns 0 if GraphNode already exists in the graph.
 */
GraphNode *Graph::insertReplace( GraphNode *node )
{
  GraphNode *nd = 0;
  if (node != 0)
  {
    if ((nd = find( node->hashName() )) == 0)
    {
      cache.put(node->hashName(), node);
      return ( node );
    }
    else
    {
      delete nd;
      cache.put(node->hashName(), node);
      return ( node );
    }
  }
  else
    return ( 0 );
}  

/************************************************
  *     --- void Graph::printGraph ---
  *
  ************************************************/
void Graph::printGraph( boolean print_delayed_children )
{
    GraphNode * const *gn;
    HashElementEnumeration< SmartCaseString, GraphNode* > enumer( &cache );
    while (enumer.hasMoreElements())
    {
      gn = enumer.nextElement();
      (*gn)->printNodeInfo( print_delayed_children );
    }
}

/************************************************
  *      --- void Graph::writeBOM ---
  *
  ************************************************/
void Graph::writeBOM()
{
    GraphNode * const *gn;
    HashElementEnumeration< SmartCaseString, GraphNode* > enumer( &cache );
    while (enumer.hasMoreElements())
    {
      gn = enumer.nextElement();
      (*gn)->printNodeBOM();
    }
}
