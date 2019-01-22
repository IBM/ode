#ifndef _ODE_LIB_STRING_PATTERN_HPP_
#define _ODE_LIB_STRING_PATTERN_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/portable/native/regex.h"

/**
 * This class is used for pattern matching purposes.
**/                                                            
class Pattern
{
  public:      

    // constructor for "compiled" regular expression
    Pattern( String &expression, boolean extended, boolean ignoreCase,
             boolean newLine, boolean noSubstring );

    // destructor for "compiled" regular expression
    ~Pattern();

    // regular expression methods for Pattern object
    boolean match( String &str );
    boolean match( String &str,
                   unsigned long startScan,     // input
                   unsigned long &startMatch,   // output
                   unsigned long &stopMatch);   // output

    // static functions not needing a Pattern object.
    // These functions do not do regular expressions!
    static boolean isMatching( const String &pattern, 
        const String &input, boolean case_sensitive = false );
    static StringArray *isMatching( const String &pattern, 
        const StringArray &input_array, boolean case_sensitive = false ); 
    static String backslashQuotedWildcards( const String &str );


  private:
    
    ODEregex expr;
    boolean noSubstring;
    boolean compiled;

    void throwException( int retcode );

    // static functions not needing a Pattern object.
    // These functions do not do regular expressions!
    static boolean isInRange( char ch, char lo, char hi,
        boolean case_sensitive );
    static boolean isEqual( char ch1, char ch2, boolean case_sensitive );
};

#endif //_ODE_LIB_STRING_PATTERN_HPP_
