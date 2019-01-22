/**
 * SetVarsTemplate (SetVars)
 *
 * This file should be included by one .cpp file per
 * program/library, in which explicit instantiation(s)
 * should take place for all types of this template
 * used in that program/library.
 *
**/
#define _ODE_LIB_STRING_SETVARS_CPP_
#include "lib/string/setvars.hpp"

template< class ElemType >
SetVarsTemplate< ElemType >::~SetVarsTemplate()
{
  if (deallocate_parent)
    delete (SetVarLinkTemplate< ElemType >*)parent;
  parent = 0;
}

/**
 * Get the parent object.
 * Provides implementation for an optional
 * SetVarLinkable method.
 *
 * @return A pointer to the parent object (it is
 * unsafe to deallocate this pointer).
**/
template< class ElemType >
const SetVarLinkTemplate< ElemType >
    *SetVarsTemplate< ElemType >::getParent() const
{
  return (parent);
}

/**
 * Shallow copy of this object.
 *
 * WARNING: User must deallocate returned pointer
 * (but not the parent pointers) with the delete operator.
**/
template< class ElemType >
SetVarLinkTemplate< ElemType > *SetVarsTemplate< ElemType >::copy() const
{
  return (new SetVarsTemplate< ElemType >( *this ));
}

/**
 * Get the value of a variable.  The parent SetVars
 * objects are NOT searched.
 *
 * @param var The variable to retrieve the value of.
 * @return The variable's value.  null is returned
 * if the variable doesn't exist in this object.
**/
template< class ElemType >
const ElemType *SetVarsTemplate< ElemType >::get( const String &var ) const
{
  return (vars.get( SmartCaseString( var, case_sensitive_vars ) ));
}

/**
 * assignment
**/
template< class ElemType >
SetVarsTemplate< ElemType > &SetVarsTemplate< ElemType >::operator=(
    const SetVarsTemplate< ElemType > &src )
{
  if (this == &src)
    return (*this);
  vars = src.vars;
  setParent( src.parent ); // src is still responsible for deallocation
  case_sensitive_vars = src.case_sensitive_vars;
  return (*this);
}

/**
 * Set the parent SetVars object.
 * Provides implementation for an optional
 * SetVarLinkable method.
 *
 * @param parent A pointer to a SetVarLinkable object which
 * will be used as the parent object.
**/
template< class ElemType >
void SetVarsTemplate< ElemType >::setParent(
    const SetVarLinkTemplate< ElemType > *parent,
    boolean deallocate_parent )
{
  if (this->deallocate_parent) // might need to delete the old one
    delete (SetVarLinkTemplate< ElemType >*)this->parent;
  this->parent = (SetVarLinkTemplate< ElemType >*)parent;
  this->deallocate_parent = deallocate_parent;
}

/**
 * Set the value of a variable.  Allows control
 * over whether to replace an existing variable.
 *
 * @param var The variable name to set.
 * @param val The variable's value.  If null, the variable
 * will be unset (replace must be true).
 * @param replace If true, variable's value will be replaced
 * if it already exists.  If false, variable's original
 * value will be maintained if it already exists.
 * @return True on success, false on failure.
**/
template< class ElemType >
boolean SetVarsTemplate< ElemType >::set( const String &var,
    const ElemType &val, boolean replace )
{
  SmartCaseString locvar( var, case_sensitive_vars );

  if (!replace && vars.containsKey( locvar ))
    return (false);

  // cannot fail now, since Hashtable will always store an
  // element...it returns false only to indicate that a
  // value has been replaced.
  vars.put( locvar, val );
  return (true);
}

/**
 * Set the value of a variable.  varstr should
 * be of the form "VARIABLE=value" (however, use the
 * attribute StringConstants::VAR_SEP_STRING instead of hardcoding "=").
 * If the variable already exists in memory, the old
 * value will be overwritten with
 * the new one.  Leading StringConstants::VAR_SEP_STRING characters in the
 * value will be discarded.  If the form "VARIABLE="
 * (where "=" is StringConstants::VAR_SEP_STRING) is
 * used, the variable will be unset.
 *
 * @param varstr The variable/value pair to set, in the form
 * "VARIABLE=value" (where "=" is StringConstants::VAR_SEP_STRING).
 * @return True on success, false on failure.
**/
template< class ElemType >
boolean SetVarsTemplate< ElemType >::put( const String &varstr,
    boolean replace )
{
  StringArray *varsptr;
  boolean rc = false;
  
  if ((varsptr = separateVarFromVal( varstr )) != 0)
  {
    StringArray &vars=*varsptr;
    unsigned int len = vars.length();
    if (len < 1 || len > 2)
      rc = false;
    else if (len == 1)
      rc = set( vars[vars.firstIndex()], String( "" ), replace );
    else // len == 2
      rc = set( vars[vars.firstIndex()],
          vars[vars.firstIndex() + 1], replace );
    delete varsptr;
  }
  return (rc);
}
  
/**
 * Set the values of many variables.  Each element of
 * varstrs should
 * be of the form "VARIABLE=value" (however, use the
 * attribute StringConstants::VAR_SEP_STRING instead of hardcoding "=").
 * If a variable already exists in memory, the old
 * value will be overwritten with
 * the new one.  Leading StringConstants::VAR_SEP_STRING characters in the
 * value will be discarded.  If the form "VARIABLE="
 * (where "=" is StringConstants::VAR_SEP_STRING) is
 * used, the variable will be unset.
 *
 * @param varstr The array of variable/value pairs to set,
 * each in the form
 * "VARIABLE=value" (where "=" is StringConstants::VAR_SEP_STRING).
 * @return True on success, false on failure.
**/
template< class ElemType >
boolean SetVarsTemplate< ElemType >::put( const StringArray &varstrs,
    boolean replace )
{
  boolean rc = true;

  for (int i = varstrs.firstIndex(); i <= varstrs.lastIndex(); ++i)
  {
    if (!put( varstrs[i], replace ))
      rc = false; // but keep going anyway
  }
  return (rc);
}

template< class ElemType >
boolean SetVarsTemplate< ElemType >::put(
    const SetVarsTemplate< ElemType > &var_set, boolean replace )
{
  boolean rc = true;
  HashKeyEnumeration< SmartCaseString, ElemType > var_enum(
      &var_set.vars );

  while (var_enum.hasMoreElements())
  {
    const SmartCaseString *var = var_enum.nextElement();
    if (!set( *var, *(var_set.vars.get( *var )), replace ))
      rc = false; // but keep going anyway
  }

  return (rc);
}

/**
 * Get the value of a variable.  The parent
 * objects will also be searched until the variable
 * is found.  This is the implementation for the
 * SetVarLinkable interface.
 *
 * @param var The variable to retrieve the value of.
 * @return The variable's value.  null is returned
 * if the variable doesn't exist in this object or
 * in any of its parents' objects.
**/
template< class ElemType >
const ElemType *SetVarsTemplate< ElemType >::find( const String &var ) const
{
  const ElemType *result = get( var );

  if (result == 0 && parent != 0)
    result = parent->find( var );
  return (result);
}
  
/**
 * Get the list of all variables in memory,
 * in no particular order.  Does NOT retrieve the
 * parent's list (use getParent() to get each parent
 * object and then run their get()'s), since parents
 * may have variables with the same name.
 *
 * The element must be a class with a toCharPtr() function.
 *
 * WARNING: Users must deallocate the returned pointer
 * with the delete operator if buf was passed as null.
 *
 * @param uppercase_vars If true, variable names will be
 * converted to uppercase before returning.
 * @return The array of all variable/value pairs (in the
 * form "VARIABLE=value" [where "=" is the attribute
 * StringConstants::VAR_SEP_STRING]).  null is returned if no
 * variables exist.
**/
template< class ElemType >
StringArray *SetVarsTemplate< ElemType >::get( boolean uppercase_vars,
    StringArray *buf, boolean get_elements ) const
{
  StringArray *strings = (buf == 0) ? new StringArray() : buf;
  String var;
  HashKeyEnumeration< SmartCaseString, ElemType > keyenum( &vars );
  const SmartCaseString *locvar;
    
  while (keyenum.hasMoreElements())
  {
    locvar = keyenum.nextElement();
    var = locvar->toString();
    if (uppercase_vars)
      var.toUpperCaseThis();
    if (get_elements)
    {
      var += StringConstants::VAR_SEP_STRING;

      // stored elements must implement toCharPtr()
      var += vars.get( *locvar )->toCharPtr();
    }
    strings->add( var );
  }
  return (strings);
}

/**
 * Deep copy of this object.
 *
 * WARNING: User must deallocate returned pointer
 * and all parent pointers with the delete operator.
**/
template< class ElemType >
SetVarLinkTemplate< ElemType > *SetVarsTemplate< ElemType >::clone() const
{
  SetVarsTemplate< ElemType > *result =
      new SetVarsTemplate< ElemType >( *this );

  if (parent != 0)
    result->setParent( parent->clone(), true );

  return (result);
}

/**
 * Return a deep clone as a StringArray.  Children
 * have a higher precedence regarding duplicate
 * variable names.
 *
 * The element is assumed to be of a type able to be
 * added to a String object via String's operator+=().
 *
**/
template< class ElemType >
StringArray *SetVarsTemplate< ElemType >::getAll(
    const SetVarLinkTemplate< ElemType > *until_parent,
    boolean uppercase_vars, StringArray *buf ) const
{
  const SetVarLinkTemplate< ElemType > *parent_ptr = parent;
  SetVarsTemplate< ElemType > allvars( *this );
  StringArray temparr;
  
  while (parent_ptr != 0 && parent_ptr != until_parent)
  {
    if (parent_ptr->get( uppercase_vars, &temparr ))
      allvars.put( temparr, false );
    temparr.clear();
    parent_ptr = parent_ptr->getParent();
  }

  if (buf == 0) // acquire our own heap space
    buf = new StringArray();

  return (allvars.get( uppercase_vars, buf ));
}
