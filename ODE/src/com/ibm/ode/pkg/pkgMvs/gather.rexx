/* Rexx ***************************************************************/
/*                                                                    */
/*                    Licensed Materials - Property of IBM            */
/*                                                                    */
/* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.    */
/*                                                                    */
/**********************************************************************/
/*                                                                    */
/* Name: GATHER                                                       */
/*                                                                    */
/* Purpose: Copy sequential data sets and HFS files to the            */
/*          appropriate SMP/E distribution library.                   */
/*                                                                    */
/* Input: Input is read from ddname INDD.                             */
/*        Each line of input contains the source data set or HFS      */
/*        file, the target data set/member name and an optional       */
/*        copy type (TEXT or BINARY) for HFS source files.            */
/*                                                                    */
/* Return code: 0 if all data sets/files are successfully gathered;   */
/*              otherwise non-zero                                    */
/*                                                                    */
/**********************************************************************/
/*                                                                    */
/* Author   Defect (D) or Feature (F) and Number                      */
/* ------   ------------------------------------                      */
/* MAD      F 1463  Initial creation                                  */
/**********************************************************************/
/* CMVC File Version: 1.3 */

Trace 'O'
Signal On NOVALUE
Address "TSO"

"EXECIO * DISKR INDD (STEM LINE."
If rc ^= 0 Then; Do
  Say "Failure reading input"
  Return 8
End

i = 0
Do While i < line.0
  i = i + 1
  theLine = line.i
  /* handle line continuation */
  Do While Right(theLine,1) = "+"
    i = i + 1
    theLine = Strip(theLine, "T", "+") || line.i
  End
  Parse Var theLine inputDsn outputDsn copyType .

  /* gather */
  If copyType = "BINARY" Then; Do
    "OSHELL cp -B '"inputDsn"' ""//'"outputDsn"'"""
  End
  Else If copyType = "TEXT" Then; Do
    "OSHELL cp -T '"inputDsn"' ""//'"outputDsn"'"""
  End
  Else If copyType = "PROGRAM" Then; Do
    "OSHELL cp -X '"inputDsn"' ""//'"outputDsn"'"""
  End
  Else; Do
    "OSHELL cp '"inputDsn"' ""//'"outputDsn"'"""
  End
  If rc ^= 0 Then; Do
    Say "Failed to copy" inputDsn "to" outputDsn
    Return 8
  End
End

Return 0

NOVALUE:
Say "Signal on NOVALUE line" Sigl ":" Sourceline(Sigl)
Return 12
