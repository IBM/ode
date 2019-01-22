/**
 * SetVarLinkable
 *
**/
#ifndef _ODE_LIB_STRING_SVARLINK_HPP_
#define _ODE_LIB_STRING_SVARLINK_HPP_

#include <base/odebase.hpp>

class String;
class StringArray;

/**
 * SetVarBase is the basest class of the SetVars inheritance
 * structure.  It contains only functions which are not
 * element-dependent in any way.
**/
class SetVarBase
{
  public:

    /**
     * Insure proper destruction of derived classes.
    **/
    virtual ~SetVarBase() {};
    
    /**
     * Does the variable exist?  Searches the entire hierarchy
     * only if search_parents is true.
    **/
    virtual boolean varExists( const String &var,
        boolean search_parents = true ) const = 0;
};


/**
 * Abstract class which SetVars and Env derives from.
 * Defines the element-dependent functions needed to allow
 * a hierarchical search for variables.
**/
template< class ElemType >
class SetVarLinkTemplate : public SetVarBase
{
  public:

    /**
     * Should first check if the variable exists
     * in the current object.  If so, just return a
     * copy of its value.
     * If the variable is not found, it should call its
     * parent's find() method.
    **/
    virtual const ElemType *find( const String &var ) const = 0;

    /**
     * Return a variable's value only if it exists in the
     * current object (doesn't search parents).
    **/
    virtual const ElemType *get( const String &var ) const = 0;

    /**
     * This should return all variables/values in a
     * "VAR=value" format, converting variable names
     * to uppercase if desired.
     * The user need only deallocate (with delete) the returned
     * pointer, since the parent(s) point to the same objects
     * as the original.
    **/
    virtual StringArray *get( boolean uppercase_vars,
        StringArray *buf = 0, boolean get_elements = true ) const = 0;

    /**
     * This allows variable hierarchies to be deep copied.
     * The user must deallocate the returned pointer (with
     * delete) and all of its parent pointers.
    **/
    virtual SetVarLinkTemplate< ElemType > *clone() const = 0;

    /**
     * This allows variable hierarchies to be shallow copied.
     * The user need only deallocate (with delete) the returned
     * pointer, since the parent(s) point to the same objects
     * as the original.
    **/
    virtual SetVarLinkTemplate< ElemType > *copy() const = 0;

    /**
     * Should return the parent object.
    **/
    virtual const SetVarLinkTemplate< ElemType > *getParent() const = 0;

    /**
     * Implement a typical version of varExists (declared in SetVarBase).
    **/
    inline virtual boolean varExists( const String &var,
        boolean search_parents = true ) const;
};

typedef SetVarLinkTemplate< String > SetVarLinkable;

template< class ElemType >
inline boolean SetVarLinkTemplate< ElemType >::varExists( const String &var,
    boolean search_parents ) const
{
  if (search_parents)
    return (find( var ) != 0);
  else
    return (get( var ) != 0);
}

#endif /* _ODE_LIB_STRING_SVARLINK_HPP_ */
