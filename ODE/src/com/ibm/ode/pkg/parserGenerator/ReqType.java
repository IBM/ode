/*******************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Author   Defect (D) or Feature (F) and Number
 * ------   ------------------------------------
 * BP       F 784       Initial Creation of the File
 * BP       D 1469      initialize all values to null
 ******************************************************************************/
package com.ibm.ode.pkg.parserGenerator;

import java.io.*;

/**
 * A class for representing an attribute of type reqType in the CMF.
 * The attribute in CMF is defined as attrib = ("type","value", "desc");
 * Any such attribte should be represented using this class.
 *
 * @version	1.2 97/05/02
 * @author 	Prem Bala
 */
public class ReqType implements Cloneable
{
  private String type_;
  private String value_;
  private String description_;

  /**
   * Constructor
   */
  public ReqType()
  {
    type_        = null;
    value_       = null;
    description_ = null;
  }

  /**
   * Copy constructor
   */
  public ReqType( ReqType orig )
  {
    this();
    type_        = orig.type_;
    value_       = orig.value_;
    description_ = orig.description_;
  }
  
  /**
   * Overrides the <code>clone</code> method of java.lang.Object
   */
  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
  
  /**
   * Overrides the <code>equals</code> method of java.lang.Object class
   */
  public boolean equals( Object obj )
  { 
    if (obj != null &&
        this.getClass().equals(obj.getClass()))
    {
      ReqType reqTypeObj = (ReqType)obj;
      if (this.type_!= null && this.type_.equals(reqTypeObj.getType()) &&
          this.value_ != null && this.value_.equals(reqTypeObj.getValue()) &&
          this.description_ != null && 
          this.description_.equals(reqTypeObj.getDescription()))
        return true;
    }
    return false;
  }
  
  /**
   * Overrides the <code>hashCode</code> method of java.lang.Object class
   */
  public int hashCode()
  {
    return this.type_.hashCode() + 
           this.description_.hashCode() + 
           this.value_.hashCode();
  }
  
  /**
   * Overrides the <code>toString</code> method of java.lang.Object class
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append('(');
    sb.append(this.type_);sb.append(',');
    sb.append(this.value_);sb.append(',');
    sb.append(this.description_);
    sb.append(')');
    return sb.toString();
  }
  
  public String getType()
  {
    return type_;
  }
  public String getValue()
  {
    return value_;
  }
  public String getDescription()
  {
    return description_;
  }

  public void setType( String type )
  {
    type_ = type;
  }
  public void setValue( String value )
  {
    value_ = value;
  }
  public void setDescription( String desc )
  {
    description_ = desc;
  }

  /**
   * Sets the appropriate data member based on the index value. Sets type if 
   * value of index is 1, value if the value of index is 2 and description if
   * the value of index is 3.
   *
   * @param index valid values are 1,2,3
   * @param value String 
   * @return return false if the value of index is not 1 or 2 or 3
   */
  public boolean setOneValue( int index, String value )
  {
    if (index == 1)
    {
      type_ = value;
      return true;
    }
    else if (index == 2)
    {
      value_ = value;
      return true;
    }
    else if (index == 3)
    {
      description_ = value;
      return true;
    }
    else
      return false;	
   }
 }
	
