/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:32:53
 * Date and Time File was extracted/checked out: 06/04/13 16:44:49
 *******************************************************************************/
package com.ibm.ode.lib.string;

import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;

/**
 * Simple string manipulation functions.
 *
 * @version 1.1
 * @author
 */
public class StringTools
{
  /**
   * Replace a substring with another string.
   *
   * @param original The original string to be modified.
   * @param find The string to search for in the original
   * string.
   * @param replace The string that will replace find.
   * @param times The maximum number of times that the
   * replacement can occur (negative numbers and zero will
   * be treated as Integer.MAX_VALUE).
   * @return The resulting String after replacement.
   */
  public static String replace( String original, String find,
      String replace, int times )
  {
    int num_replaced=0, index, len=find.length();
    String new_string="", first, rest;

    if (times <= 0)
      times = Integer.MAX_VALUE;
    while ((index = original.indexOf( find )) >= 0 &&
        times > num_replaced)
    {
      first = original.substring( 0, index );
      rest = original.substring( index+len );
      new_string += first + replace;
      original = rest;
      ++num_replaced;
    }
    new_string += original; // anything left over
    return (new_string);
  }

  /**
   * Split a string an unlimited number of times.
   *
   * @param str The string to split.
   * @param delim The character to split the string by.
   * @return An array of split strings, or null on error
   * (str and delim must be non-null).
   */
  public static String[] split( String str, char delim )
  {
    return (split( str, String.valueOf( delim ), -1 ));
  }

  /**
   * Split a string, into [at most] max_strings strings.
   *
   * If the value of max_strings is zero, the original
   * string will be returned in an array of length one.
   *
   * If the value of max_strings is < zero,
   * then the string will be split at most
   * Integer.MAX_VALUE times (i.e., effectively
   * there will be no limit to the number of splits).
   *
   * @param str The string to split.
   * @param delim The character to split the string by.
   * @param max_strings The maximum number of strings to
   * split str into (this may cause delimiters to remain in
   * the contents of the last array entry).
   * @return An array of split strings, or null on error
   * (str and delim must be non-null).
   */
  public static String[] split( String str, char delim,
      int max_strings )
  {
    return (split( str, String.valueOf( delim ), max_strings ));
  }

  /**
   * Split a string an unlimited number of times.
   *
   * @param str The string to split.
   * @param delim The character(s) to split the string by.
   * @return An array of split strings, or null on error
   * (str and delim must be non-null).
  **/
  public static String[] split( String str, String delim )
  {
    return (split( str, delim, -1 ));
  }

  /**
   * Split a string, into [at most] max_strings strings.
   *
   * If the value of max_strings is zero, the original
   * string will be returned in an array of length one.
   *
   * If the value of max_strings is less than zero,
   * then the string will be split at most
   * Integer.MAX_VALUE times (i.e., effectively
   * there will be no limit to the number of splits).
   *
   * @param str The string to split.
   * @param delim The character(s) to split the string by.
   * @param max_strings The maximum number of strings to
   * split str into (this may cause delimiters to remain in
   * the contents of the last array entry).
   * @return An array of split strings, or null on error
   * (str and delim must be non-null).
   */
  public static String[] split( String str, String split_by, int max_strings )
  {
    String[] rc;
    Vector workspace;
    int old_index = 0, new_index = -1, count = 0;

    if (str == null || split_by == null || max_strings == 0)
      return (null);

    if (max_strings == 1)
    {
      rc = new String[1];
      rc[0] = str;
    }
    else
    {
      if (max_strings < 0)
        max_strings = Integer.MAX_VALUE;
      workspace = new Vector();
      while (old_index < str.length())
      {
        new_index = findIndexToSplit( str, split_by, old_index );
        if (new_index != old_index)
        {
          if (++count >= max_strings || new_index == -1)
          {
            workspace.addElement( str.substring( old_index ) );
            break; // done
          }
          else
            workspace.addElement( str.substring( old_index, new_index ) );
        }
        old_index = new_index + 1;
      }
      rc = new String[workspace.size()];
      workspace.copyInto( rc );
    }

    return rc;
  }

  /**
   *
   */
  private static int findIndexToSplit( String str, String split_by,
      int start_index )
  {
    char ch, quote = ' '; // init to something other than a quote

    for (int i = start_index; i < str.length(); ++i)
    {
      ch = str.charAt( i );
      if (ch == '\"' || ch == '\'')
      {
        if (quote == ch) // quote is now closed
        {
          quote = ' ';
          continue;
        }
        else if (quote == ' ') // quote isn't open yet, so maybe open it
        {
          if (split_by.indexOf( ch ) < 0) // make sure not splitting on quotes
          {
            quote = ch;
            continue;
          }
        }
      }

      if (quote == ' ')
      {
        if (split_by.indexOf( ch ) >= 0)
          return (i);
      }
    }

    return (-1);
  }


  /**
   * Concatenate many strings into one string.
   *
   * @param strings The array of strings to join together.
   * @return The concatenated result.  If strings was null
   * or contained no elements, the empty string is returned.
   */
  public static String join( String[] strings )
  {
    return (join( strings, "" ));
  }


  /**
   * Concatenate many strings into one string, separating
   * each one by another "join" string.
   *
   * @param strings The array of strings to join together.
   * @param joinstr The characters that will be placed
   * in between each element of strings.  If null or the
   * empty string is passed, the strings will simply be
   * concatenated together with no intervening characters.
   * @return The concatenated result.  If strings was null
   * or contained no elements, the empty string is returned.
   */
  public static String join( String[] strings, String joinstr )
  {
    String result="";

    if (joinstr == null)
      joinstr = "";
    if (strings != null && strings.length > 0)
    {
      result = strings[0];
      for (int i=1; i < strings.length; ++i)
        result += joinstr + strings[i];
    }
    return (result);
  }

  /**
   * This method checks the format entries, so they don't exceed column 72.
   *
   * @param keyword     Name of attribute
   * @param stringVal   Value of keyword
   *                    example:  filename lib1,lib2 a1
   *
   * @return cmdString  Output String containing properly fomatted SMP entry
   */
  public static String checkAndFormatString(String keyword, String stringVal)
  {
    String prefix = "  ";
    String smpStatement = prefix + keyword + "(";
    String cmdString = "";
    String newString = "";

    // stuffing spaces after prefix so as to align multiple lines below "("
    for (int tmpVar=0; tmpVar < keyword.length(); tmpVar++)
      prefix += " ";

    newString += smpStatement;
    newString += stringVal;

    if (newString.length() > 71)
    {
      while (newString.length() > 71)
      {
        String newStr = newString.substring( 0, 71);
        int lastComma = newStr.lastIndexOf(',');

        cmdString += newString.substring( 0, lastComma + 1 ) + "\n";

        newString = prefix + newString.substring( lastComma + 1 );
      } // end while

      cmdString += newString;
    } // end if
    else
    {
       cmdString += newString;
    }

    cmdString += ")";
    return cmdString;
  }

  /**
   * Returns true if the specified string is part of any string that
   * is contained in the specified Vector else false
   *
   * @param string a string to be searched
   * @param list a Vector of strings
   * @author Anil Ambati
   */
  public static boolean isPartOfAnyInTheList( String string,
                                              Vector list )
  {
    Enumeration enumer = list.elements();
    while (enumer.hasMoreElements())
    {
      if (((String)enumer.nextElement()).indexOf(string) >= 0)
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a string with spaces.
   *
   * @param numOfSpaces the length of the string
   * @return a string with specified number of spaces
   * @author Anil Ambati
   */
  public static String getStringWithSpaces( int numOfSpaces )
  {
    StringBuffer sb = new StringBuffer();
    while (numOfSpaces > 0)
    {
      sb.append(' ');
      numOfSpaces--;
    }
    return sb.toString();
  }
}
