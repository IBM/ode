using namespace std;
/**
 * GraphNode
**/
using namespace std;
#define _ODE_BIN_MAKE_GRAPHND_CPP_

#include <base/binbase.hpp>
#include "lib/portable/stack.hpp"
#include "bin/make/graphnd.hpp"


/******************************************************************************
 * Destructor.
 *   Must free the siblings.
 */
GraphNode::~GraphNode()
{
  VectorEnumeration< GraphNode * > enumsiblings( &siblings );
  while (enumsiblings.hasMoreElements())
  {
    delete *enumsiblings.nextElement();
  }
}

/******************************************************************************
 */
const String &GraphNode::hashName( ) const
{
  return (id);
}

/******************************************************************************
 */
int GraphNode::execCmds( boolean add_last )
{
  return (MADE);
}

/******************************************************************************
 */
boolean GraphNode::equals( const GraphNode &obj ) const
{
  return (id.equals( obj.id ));
}

/******************************************************************************
 */
boolean GraphNode::operator==( const GraphNode &obj ) const
{
  return (this->equals( obj ));
}

/******************************************************************************
 */
const String &GraphNode::nameOf() const
{
  return (id);
}

/******************************************************************************
 */
int GraphNode::getState()
{
  return (state);
}

/******************************************************************************
 */
void GraphNode::setState( int instate )
{
  if (instate >= UNMADE && instate <= ERROR)
    state = instate;
}

/******************************************************************************
 */
void GraphNode::orderChildren( Vector<GraphNode *> &unorderedChildren,
  Vector<GraphNode *> &orderedChildren)
  // throws ParseException
{
  GraphNode *ptr = 0, *prev_ptr = 0, *next_ptr = 0;
  Stack<GraphNode *> tmplst;
  StackEnumeration< GraphNode* > stack_enum( &tmplst );
  GraphNode **popped_element;

  if (children.isEmpty())
    return;

  boolean ordered_node;
  VectorEnumeration< GraphNode * > enumchildren( &children );
  while (enumchildren.hasMoreElements())
  {
    ptr = *enumchildren.nextElement();

    // The child is already in ordered vector, get next one
    if (orderedChildren.contains( ptr ))
      continue;

    ordered_node = false;
    tmplst.removeAll();
    // first tranverse chain to check if this is an ordered node
    prev_ptr = ptr->prev;
    while (prev_ptr != 0)
    {
      if (orderedChildren.contains( prev_ptr ))
      {
        ordered_node = true;
        break;
      }
      else
      if (children.contains( prev_ptr ))
      {
        stack_enum.setObject( &tmplst );
        while (stack_enum.hasMoreElements())
        {
          if (*stack_enum.nextElement() == prev_ptr )
          {
            short count = 0;
            GraphNode *last_ptr = 0;

            while (stack_enum.hasMoreElements())
            {
               count++;
               last_ptr = *stack_enum.nextElement();
            }
            ParseException exp( prev_ptr->mfs->getPathname(),
              prev_ptr->mfs->getLineNumber(),
              String( "Ordered target `" ) +
              ((last_ptr == ptr && count > 1) ?
              last_ptr->nameOf():prev_ptr->nameOf()) +
              "' is illegally in .ORDER statement twice" );
             Make::quit( exp.getMessage(), 1 );
          } /* end if */
        } /* end while */
        tmplst.push( prev_ptr );
        ordered_node = true;
      }
      prev_ptr = prev_ptr->prev;
    }

    if (ordered_node)
    {
      while (!tmplst.empty())
      {
        popped_element = tmplst.pop();
        addOrderedChild( orderedChildren, *popped_element );
        delete popped_element;
      }
      addOrderedChild( orderedChildren, ptr );
      continue;
    }

    // now tranverse next chain to check if this is an ordered node
    next_ptr = ptr->next;
    while (next_ptr != 0)
    {
      if (orderedChildren.contains( next_ptr ))
      {
        ParseException exp( next_ptr->mfs->getPathname(),
          next_ptr->mfs->getLineNumber(),
          String( "Ordered target `" ) + next_ptr->nameOf() +
          "' is illegally in .ORDER statement twice" );
         Make::quit( exp.getMessage(), 1 );
      }
      else
      if (children.contains( next_ptr ))
      {
        ordered_node = true;
        break;
      }
      next_ptr = next_ptr->next;
    }

    if (ordered_node)
      addOrderedChild( orderedChildren, ptr );
    else
      unorderedChildren.addElement( ptr );
  } /* end while */
} /* end orderChildren */

/************************************************
  *   --- String GraphNode::getListOf ---
  *
  ************************************************/
String GraphNode::getListOf( Vector<GraphNode *> *vect )
{
  String list = StringConstants::EMPTY_STRING;
  VectorEnumeration< GraphNode *> enumer( vect );

  while (enumer.hasMoreElements())
  {
    list += ((GraphNode *) *enumer.nextElement())->nameOf();
    if (enumer.hasMoreElements()) list += StringConstants::SPACE;
  }
  return( list );
}
