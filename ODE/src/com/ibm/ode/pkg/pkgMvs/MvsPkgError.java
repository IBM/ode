package com.ibm.ode.pkg.pkgMvs;

import java.text.MessageFormat;

/**
 * Exception class for MVS ipp and ptf packaging.
 * @version 1.4 97/09/30
 * @author  Mark DeBiase
**/

public class MvsPkgError extends Exception
{
  //***************************************************************************
  // Throws MvsPkgError exception with specified text.
  //***************************************************************************
  public MvsPkgError(String text)
  {
    super(text);
  }

  //***************************************************************************
  // Throws MvsPkgError exception with formatted message text.
  // 
  // Parameters:  msgId - the message id to format
  //              args  - the values to be substituted into the message
  //
  // See: java.Text.MessageFormat
  //
  //***************************************************************************
  public MvsPkgError(int msgId, Object [] args)
  {
    super( MessageFormat.format( msgArray_[msgId], args) );
  }

  //***************************************************************************
  // The message ids for formatted messages.  Each message id is an index
  // into an array of strings which holds the messages to be formatted.  
  // Each message id name is followed by an integer indicating the 
  // expected number of arguments.
  // NOTE: If new message ids are addded, be sure to increment maxMsgId_
  // to the total number of messages in the message array.
  //***************************************************************************
  public final static int requiredProductTag1      = 0;
  public final static int requiredShipTag2         = 1;
  public final static int duplicateDistlib1        = 2;
  public final static int incorrectAllocationInfo1 = 3;
  public final static int errorParsingDistlib0     = 4;
  public final static int distlibNotFound1         = 5;
  public final static int invalidForParttype3      = 6;
  public final static int mutuallyExclusive3       = 7;
  public final static int jclinDistname3           = 8;
  public final static int onlyOneJclinAllowed1     = 9;
  public final static int invalidPartType2         = 10;
  public final static int invalidJobcard1          = 11;
  public final static int fileNotFound2            = 12;
  public final static int ioException3             = 13;
  public final static int invalidProductTag1       = 14;
  public final static int invalidPartTag2          = 15;   
  public final static int invalidMcsKeywords3      = 16;
  public final static int invalidVerStatement      = 17;
  public final static int invalidIfReq             = 18;
  public final static int invalidProductTag2       = 19;
  public final static int invalidDistlibFormat     = 20;
  public final static int linkEditPartNotAllowed   = 21;
  public final static int requiredVariable         = 22;

  private static int      maxMsgId_                = 23;  // last msgId + 1 !!
  private static String[] msgArray_                = new String[maxMsgId_];

  //***************************************************************************
  // initialize the array of messages
  //***************************************************************************
  static 
  {
    String NL_ = System.getProperty("line.separator");

    try
    {
      msgArray_[requiredProductTag1] =
        "Error: Required tag <{0}> not found in product data.";

      msgArray_[requiredShipTag2] =
        "Error: Required tag <{0}> not found in ship data for {1}.";

      msgArray_[duplicateDistlib1] =
        "Error: Duplicate specification of distlib {0} in <DISTLIBS> tag.";

      msgArray_[incorrectAllocationInfo1] =
        "Error: Allocation information for distlib {0} is incorrect.";

      msgArray_[errorParsingDistlib0] =
        "Error: Error parsing <DISTLIBS> value.";

      msgArray_[distlibNotFound1] =
        "Error: Distlib {0} was not found in <DISTLIBS> tag.";

      msgArray_[invalidForParttype3] =
        "Error: {0}: <{1}> tag is invalid for part type {2}.";

      msgArray_[mutuallyExclusive3] =
        "Error: {0}: <{1}> and <{2}> tags are mutually exclusive.";

      msgArray_[jclinDistname3] =
        "Error: {0}: DISTNAME for JCLIN must match FUNCTION." + NL_ +
        "       DISTNAME is {1} and FUNCTION is {2}.";

      msgArray_[onlyOneJclinAllowed1] =
        "Error: {0}: There can be only one JCLIN part.";

      msgArray_[invalidPartType2] =
        "Error: {0}: {1} is not a valid SMP part type.";

      msgArray_[invalidJobcard1] =
        "Error: {0} contains an invalid jobcard.";

      msgArray_[fileNotFound2] = 
        "Error: Could not find {0} file {1}.";

      msgArray_[ioException3] =
        "Error: Caught IOException {0} file {1}:" + NL_ +
        "       {2}";

      msgArray_[invalidProductTag1] = 
        "Error: Invalid tag <{0}> found in product data.";

      msgArray_[invalidPartTag2] =
        "Error: Invalid tag <{0}> found in ship data for {1}.";

      msgArray_[invalidMcsKeywords3] =
        "Error: {0}: The following tags are invalid for PARTTYPE {1}:" + NL_ +
        "       {2}";

      msgArray_[invalidVerStatement] =
        "Error: The following value for {0} is formatted incorrectly" + NL_ +
        " for use in a CTLDEF VER statment:" + NL_ + "{1}";

      msgArray_[invalidIfReq] =
        "Error: The following ifreq value does not contain two tokens" + NL_ +
        "{0}";

      msgArray_[invalidProductTag2] =
        "Error: {0}: {1} is not in a valid format.";

      msgArray_[invalidDistlibFormat] =
        "Error: The following distlib specification was formatted incorrectly" 
        + NL_ + "{0}";

      msgArray_[linkEditPartNotAllowed] =
        "Error: The following part cannot be link-edited because a" +
        " RECFM U distribution library was not allocated:" + NL_ + "   {0}";

      msgArray_[requiredVariable] =
        "Error: Required variable not found in input list:" + NL_ + "{0}";

    }
    catch( ArrayIndexOutOfBoundsException e )
    {
      System.err.println("MvsPkgError: caught ArrayIndexOutOfBoundsException");
    }
  } // static
}
