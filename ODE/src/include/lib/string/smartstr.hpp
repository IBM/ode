/**
 * SmartCaseString
 *
**/
#ifndef _ODE_LIB_STRING_SMARTSTR_HPP_
#define _ODE_LIB_STRING_SMARTSTR_HPP_

#include "lib/string/string.hpp"
#include "lib/portable/platcon.hpp"


/**
 * A sensitivity-aware wrapper for String objects.
 * It allows storage of a String into a Vector or
 * Hashtable so that searches on it can be case
 * insensitive.  String itself is already case-
 * aware, but defaults to being case sensitive.
 * This allows overriding that behavior easily.
**/
class SmartCaseString : public String
{
  public:

    // constructors
    SmartCaseString() :
#ifdef CASE_INSENSITIVE_OS
        String( "", PlatformConstants::onCaseSensitiveOS() ) {}
#else
        String() {}
#endif
    SmartCaseString( const char *str,
        boolean case_sensitive = PlatformConstants::onCaseSensitiveOS() ) :
#ifdef CASE_INSENSITIVE_OS
        String( str, case_sensitive ) {}
#else
        String( str ) {}
#endif
    SmartCaseString( const String &str,
        boolean case_sensitive = PlatformConstants::onCaseSensitiveOS() ) :
#ifdef CASE_INSENSITIVE_OS
        String( str, case_sensitive ) {}
#else
        String( str ) {}
#endif
    SmartCaseString( const SmartCaseString &str ) :
#ifdef CASE_INSENSITIVE_OS
        String( str, str.case_sensitive ) {}
#else
        String( str ) {}
#endif
};

#endif /* _ODE_LIB_STRING_SMARTSTR_HPP_ */
