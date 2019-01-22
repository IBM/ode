package COM.ibm.makemake.lib;

import java.util.StringTokenizer;

/**
 * Simple string manipulation functions.
**/
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
	**/
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
	**/
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
	**/
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
	**/
	public static String[] split( String str, String delim,
			int max_strings )
	{
		int index=0, numtokens;
		String[] strings=null;
		StringTokenizer toke;

		if (str != null && delim != null)
		{
			if (max_strings < 0)
				max_strings = Integer.MAX_VALUE;
			if (max_strings == 0)
			{
				strings = new String[1];
				strings[0] = str;
			}
			else
			{
				toke = new StringTokenizer( str, delim, false );
				numtokens = toke.countTokens();
				if (numtokens > 0)
				{
					if (numtokens < max_strings)
						strings = new String[numtokens];
					else
						strings = new String[max_strings];
					while (toke.hasMoreTokens() && index < strings.length)
					{
						if (index < (strings.length - 1) || numtokens == strings.length)
							strings[index] = toke.nextToken();
						else // put remainder of string in last array slot
							strings[index] = toke.nextToken( "" );
						++index;
					}
				}
			}
		}
		return (strings);
	}


	/**
	 * Concatenate many strings into one string.
	 *
	 * @param strings The array of strings to join together.
	 * @return The concatenated result.  If strings was null
	 * or contained no elements, the empty string is returned.
	**/
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
	**/
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
}
