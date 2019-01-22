//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* Version: 1.4
//*
//* Date and Time File was last checked in:       97/09/30 10:32:03
//* Date and Time File was extracted/checked out: 99/04/25 09:14:39
//*
//*****************************************************************************

package com.ibm.ode.pkg.pkgMvs;

import java.util.*;

/**
 * Class for parsing MVS product metadata read from package control file.
 * @version 1.4 97/09/30
 * @author  Mark DeBiase
**/
class MvsProductData
{

  //***************************************************************************
  // Parse the product data string and return a Hashtable that contains
  // the product metadata tag value pairs.
  //***************************************************************************
  public static Hashtable parseProductDataString(String productDataString)
                          throws MvsPkgError
  {
    StringTokenizer    st = new StringTokenizer(productDataString, "<>");
    Hashtable productData = new Hashtable();

    String tag, val;
    ArrayList ifStmts = new ArrayList();
    ArrayList delStmts = new ArrayList();

    // parse each product data tag
    while ( st.hasMoreTokens() )
    {
      tag = st.nextToken().trim().toUpperCase();

      if (!MvsValidation.validateProductDataTag(tag))  // MAD-F1615
      {
        throw new MvsPkgError(MvsPkgError.invalidProductTag1,
                              new Object[] {tag});
      }

      val = upperCase(tag, st.nextToken().trim());
      
      // "IF" and "DEL" statements need to go into an array because multiple IFs and
      // DELs are allowed in a CMF which can't be stored in a hash table
      if (tag.equals("IF"))
        ifStmts.add( val );
      else if (tag.equals("DEL"))
        delStmts.add( val );      
      else
      {
        String s = (String) productData.put(tag, val);
        if (s != null)
        {
          System.out.println("Warning: Duplicate " + tag +
                             " tag found in product data");
          System.out.println("         Keeping current value    : " + val);
          System.out.println("         Discarding previous value: " + s);
        }
      }
    }
    // enter the array containing IF statements in to the hash table with
    // the tag "IF"
    if (!ifStmts.isEmpty())
    {
      tag = "IF";
      productData.put(tag, ifStmts);
    }

    // enter the array containing DEL statements into the hash table with
    // the tag "DEL"
    if (!delStmts.isEmpty())
    {
      tag = "DEL";
      productData.put(tag, delStmts);
    }

    return productData;
  }

  //***************************************************************************
  // Conditionally convert values to uppercase.
  //***************************************************************************
  private static String upperCase(String tag, String val)
  {
    // don't uppercase the following tags....
    // COPYRIGHT    - can be a hfs file
    // EXTRASMPEFILE - can be a hfs file
    // DESCRIPTION - can be mixed case
    if ( tag.equals("COPYRIGHT") || tag.equals("EXTRASMPEFILE")
         || tag.equals("DESCRIPTION") )
      return val;
    else
      return val.toUpperCase();
  }

}
