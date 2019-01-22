#ifndef _ODE_LIB_STRING_SETVARS_HPP_
#define _ODE_LIB_STRING_SETVARS_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/svarlink.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/string/strcon.hpp"
#include "lib/portable/hashtabl.hpp"

/**
 * Non-environment variable storage/retrieval functionality.
 * Allows chaining of other SetVars objects so that a search
 * for a variable (using find() instead of get()) can apply
 * to a complete hierarchy instead of just a single object.
 * Instead of using a hardcoded "=" (for VARIABLE=value
 * specifications), use the attribute StringConstants::VAR_SEP_STRING instead.
 * For example: "VARIABLE" + StringConstants::VAR_SEP_STRING + "value".
 * The parent can be set to any SetVarLinkable object.
 *
**/
template< class ElemType >
class SetVarsTemplate : public SetVarLinkTemplate< ElemType >
{
  public:

    // constructors
    SetVarsTemplate( const SetVarsTemplate< ElemType > &set_vars ) :
        vars( set_vars.vars ), parent( set_vars.parent ),
        case_sensitive_vars( set_vars.case_sensitive_vars ),
        deallocate_parent( false ) {};
    SetVarsTemplate( boolean case_sensitive_vars = true ) :
        parent( 0 ),
        case_sensitive_vars( case_sensitive_vars ),
        deallocate_parent( false ) {};
    SetVarsTemplate( const SetVarLinkTemplate< ElemType > *parent,
        boolean case_sensitive_vars = true,
        boolean deallocate_parent = false ) :
        parent( parent ),
        case_sensitive_vars( case_sensitive_vars ),
        deallocate_parent( deallocate_parent ) {};

    virtual ~SetVarsTemplate();

    SetVarsTemplate< ElemType > &operator=(
        const SetVarsTemplate< ElemType > &src );
    void setParent( const SetVarLinkTemplate< ElemType > *parent,
        boolean deallocate_parent = false );
    inline void unsetParent();
    virtual const SetVarLinkTemplate< ElemType > *getParent() const;
    inline boolean isCaseSensitive() const;
    boolean set( const String &var, const ElemType &val,
        boolean replace );
    inline boolean unset( const String &var );
    boolean put( const String &varstr, boolean replace = true );
    boolean put( const StringArray &varstrs, boolean replace = true );
    boolean put( const SetVarsTemplate< ElemType > &var_set,
        boolean replace = true );
    virtual const ElemType *find( const String &var ) const;
    virtual SetVarLinkTemplate< ElemType > *clone() const;
    virtual SetVarLinkTemplate< ElemType > *copy() const;
    virtual const ElemType *get( const String &var ) const;
    virtual StringArray *get( boolean uppercase_vars = false,
        StringArray *buf = 0, boolean get_elements = true ) const;
    inline static StringArray *separateVarFromVal( const String &str,
        StringArray *buf = 0 );
    inline void clear();
    StringArray *getAll( const SetVarLinkTemplate< ElemType > *until_parent,
        boolean uppercase_vars = false, StringArray *buf = 0 ) const;


  private:

    typedef Hashtable< SmartCaseString, ElemType > SetVarHash;
    SetVarHash vars;
    const SetVarLinkTemplate< ElemType > *parent;
    boolean case_sensitive_vars, deallocate_parent;
};


typedef SetVarsTemplate< String > SetVars;

/**
 * Remove the parent SetVars object (so that this
 * object has no parent).
 * Provides implementation for an optional
 * SetVarLinkable method.
**/
template< class ElemType >
inline void SetVarsTemplate< ElemType >::unsetParent()
{
  setParent( 0 );
}

/**
 * Return whether or not the variable names are
 * treated as case sensitive or not.
**/
template< class ElemType >
inline boolean SetVarsTemplate< ElemType >::isCaseSensitive() const
{
  return (case_sensitive_vars);
}

/**
 * Takes a string of the form "VARIABLE=value" (where
 * "=" is StringConstants::VAR_SEP_STRING), and returns the two elements
 * separately.  Any leading "=" characters in value will be
 * removed.
 *
 * WARNING: User must deallocate returned pointer
 * with the delete operator.
 *
 * @param str A string of the form "VARIABLE=value"
 * (where "=" is StringConstants::VAR_SEP_STRING).
 * @return A pointer to a two-element StringArray in which
 * the first element
 * is the variable, and the second is its value.  The caller
 * must deallocate this pointer with delete.
**/
template< class ElemType >
inline StringArray *SetVarsTemplate< ElemType >::separateVarFromVal(
    const String &str, StringArray *buf )
{
  buf = str.split( StringConstants::VAR_SEP_STRING, 2, buf );
  if (buf->size() < 1)
  {
    buf->add( StringConstants::EMPTY_STRING );
    buf->add( StringConstants::EMPTY_STRING );
  }
  else if (buf->size() < 2)
    buf->add( StringConstants::EMPTY_STRING );
  return (buf);
}

/**
 * Remove all variables from this object (does not alter
 * parent structures).
**/
template< class ElemType >
inline void SetVarsTemplate< ElemType >::clear()
{
  vars.clear();
}

/**
 * Removes a variable from memory.
 *
 * @param var The variable to unset.
 * @return True on success, false on failure.
**/
template< class ElemType >
inline boolean SetVarsTemplate< ElemType >::unset( const String &var )
{
  return (vars.remove( SmartCaseString( var, case_sensitive_vars ) ));
}

#endif /* _ODE_LIB_STRING_SETVARS_HPP_ */
