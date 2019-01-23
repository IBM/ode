/**
 * Pattern
 *
**/

#include <ctype.h>
#include <iostream>
using namespace std ;

#define _ODE_LIB_STRING_PATTERN_CPP_
#include "lib/string/pattern.hpp"
#include "lib/exceptn/exceptn.hpp"


Pattern::Pattern( String &expression, boolean extended, boolean ignoreCase,
                  boolean newLine, boolean noSubstring )
                  : compiled(false)
// Can throw Exception
{
  this->noSubstring = noSubstring;
  int retcode = ODEregcomp( &expr, expression.toCharPtr(), extended,
                        ignoreCase, newLine, noSubstring );
  if (retcode != 0)
  {
    throwException( retcode );
  }
  else
    compiled = true;
}


Pattern::~Pattern()
{
  if (compiled)
    ODEregfree( &expr );
}


boolean Pattern::match( String &str )
// Can throw Exception
{
  int retcode = ODEregexec( &expr, str.toCharPtr(), 0, 0, 0, 0 );
  if (retcode == 0)
    return true;
  else if (retcode != ODE_REGEX_NOMATCH)
    throwException( retcode );
  return false;
}


boolean Pattern::match( String &str,
                        unsigned long startScan,     // input
                        unsigned long &startMatch,   // output
                        unsigned long &stopMatch)    // output
// Can throw Exception
{
  if (noSubstring)
  {
    // this probably indicates a coding error in mk
    throw Exception(
      "substring match attempted but regex not compiled for substring scan" );
  }
  int notBeginLine = (startScan != STRING_FIRST_INDEX);
  unsigned long startPoint, stopPoint;
  int retcode = ODEregexec( &expr,
                            str.toCharPtr() + startScan - STRING_FIRST_INDEX,
                            notBeginLine,
                            0,
                            &startPoint,
                            &stopPoint );
  if (retcode == 0)
  {
    startMatch = startPoint + startScan;
    stopMatch = stopPoint + startScan;
    return true;
  }
  else if (retcode != ODE_REGEX_NOMATCH)
    throwException( retcode );
  return false;
}


void Pattern::throwException( int retcode )
{
  int size = ODEregerror( retcode, &expr, 0, 0 ); // get needed size of msg
  char *msg = new char[size];
  ODEregerror( retcode, &expr, msg, size );
  throw Exception( msg );
}


StringArray *Pattern::isMatching( const String &pattern, 
    const StringArray &input_array, boolean case_sensitive )
{
   if (input_array.length() == 0)
      return 0;

   StringArray *matched_strings = new StringArray();
   
   for(int i = input_array.firstIndex(); i <= input_array.lastIndex(); i++)
   {      
      if (isMatching( pattern, input_array[i], case_sensitive ))
         matched_strings->add( input_array[i] );
   }

   return (matched_strings);
}


boolean Pattern::isMatching( const String &pattern, const String &input,
    boolean case_sensitive )
{   
   int  lastStar = -1, lastIpIndex = 0, ipIndex = 0, count = 0;
   char ch, *pcPattern = pattern.toCharPtr(), *pcInput = input.toCharPtr();
   
   while (count < pattern.length())
   {    
      // if no "previous" * exists i.e string must start 
      // with a specific substring
      if (count == -1) 
         return false;
            
      ch = pcPattern[count];                     
         
      if (ch == '*') // if it is a "*"character
      {
         // this the position of the most recently encountered "*"        
         lastStar = count++;
         lastIpIndex = ipIndex;       

         // if it is last element and there is more input- absorb rest of input
         if (count == pattern.length() && (ipIndex < input.length()))
            ipIndex = input.length();                
      }   
      else if (ch == '?') // if ? - just move on to the next char
      {
         ++ipIndex;         
         ++count;
      }
      else if (ch == '[') // if it is a "[]" wildcard
      {            
          // do till you encounter ']' or you step out of bounds
         while ((ch != ']') && (count <= pattern.length())) 
         {               
            count++;
            ch = pcPattern[count];
                  
            if ((ch == ',') || (ch == ' ')) // ignore spaces and commas
            {
               ++count;
               continue;                  
            }

            if (ch == '\\')                //
               ch = pcPattern[++count];    // if an escape sequence is
                                           // specified - go to the next
            if (pcInput[ipIndex] == '\\')  // char
               ++ipIndex;    
            
            if (ch == '-') // check for range
            {
               if (isInRange( pcInput[ipIndex], pcPattern[count - 1],
                             pcPattern[count + 1], case_sensitive) )
                  break; // eureka!!!
            }
            else if (isEqual( ch, pcInput[ipIndex], case_sensitive )) //eureka!
               break;             
         }                        

         while ((ch != ']') && (count <= pattern.length()))//skip to ']'
         {
            ch = pcPattern[count++];            
            if (ch == '\\')   
               ++count;               
         }   
            
         ++ipIndex; // this input char matches the pattern - so far
      }
      else
      {                           
         
         if (ch == '\\')                //
            ch = pcPattern[++count];    // if an escape sequence is
                                        // specified - go to the next
         if (pcInput[ipIndex] == '\\')  // char
            ++ipIndex;    

         if ((ipIndex > input.length()) || (count > pattern.length()))
            break;
         
         // if not same
         if (!isEqual( pcInput[ipIndex++], pcPattern[count++], case_sensitive ))
         {
            count = lastStar;           // go back to the last star
            ipIndex = lastIpIndex + 1;  // consume just one char from input
         } //else you have taken one more step towards that perfect match                       
      }                     
         
      //  there is still more input string - go to the last "*" and
      // just consume one char from the input
      if ((count == pattern.length()) && (ipIndex < input.length()))
      {
         count = lastStar;
         ipIndex = lastIpIndex + 1;
      }  
     
      if ((ipIndex == input.length()) && (count == pattern.length()))
            break;
   }
     
   if ((ipIndex == input.length()) && (count == pattern.length()))   
      return true; // what a perfect match!!! - you two look so good together..
      
   return false; // some are just not meant to be together!!!      
}


 
/*******************************************************************************
 *
 */
boolean Pattern::isInRange( char ch, char lo, char hi, 
    boolean case_sensitive ) 
{
   if (!case_sensitive)
   {
      ch = tolower( ch );
      lo = tolower( lo );
      hi = tolower( hi );
   }

   if ((lo <= ch) && (ch <= hi))
      return true;
   else
      return false;
}


/*******************************************************************************
 *
 */
boolean Pattern::isEqual( char ch1, char ch2, boolean case_sensitive )
{
   if (!case_sensitive)
   {
      ch1 = tolower( ch1 );
      ch2 = tolower( ch2 );
   }

   if (ch1 == ch2) 
      return true;     
   else
      return false;
}


/*******************************************************************************
 * return the value of str, but with any wildcards that are in single or
 * double quotes prepended with '\'. Wildcards are *?[]\ 
 */
String Pattern::backslashQuotedWildcards( const String &str )
{
  String ret;
  String wild( "*?[]\\" );
  char quote = 0;
  for (int i = str.firstIndex(); i <= str.lastIndex(); ++i )
  {
    char ch = str[i];
    if ( ch == '\'' || ch == '\"' )
    {
      if (quote == 0)
        quote = ch;
      else if ( quote == ch )
        quote = 0;
    }
    else if (quote != 0 && wild.indexOf( ch ) != STRING_NOTFOUND)
      ret += '\\';
    ret += ch;
  }
  return (ret);
}
