/* Rexx ***************************************************************/
/*                                                                    */
/*                    Licensed Materials - Property of IBM            */
/*                                                                    */
/* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.    */
/*                                                                    */
/**********************************************************************/
/*                                                                    */
/* Name: ALLOCLIB                                                     */
/*                                                                    */
/* Purpose: Allocate SMP/E distribution libraries and other data sets */
/*          needed for MVS packaging.                                 */
/*                                                                    */
/* Input: Input is read from ddname INDD.                             */
/*        Each line of input contains the name of the data set to be  */
/*        allocated and its allocation information.                   */
/*                                                                    */
/* Return code: 0 if all data sets are successfully allocated;        */
/*              non-zero otherwise.                                   */
/*                                                                    */
/**********************************************************************/
/*                                                                    */
/* Author   Defect (D) or Feature (F) and Number                      */
/* ------   ------------------------------------                      */
/* MAD      F 1463  Initial creation                                  */
/* MAD      F 1615  Allow specification of allocation units (TRACKS   */
/*                  or BLOCKS) & added code for line continuation.    */ 
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
  thisLine = line.i
  /* handle line continuation */
  Do While Right(thisLine, 1) = "+"
    i = i + 1
    thisLine = Strip(thisLine, "T", "+") || line.i
  End
  thisLine = Strip(thisLine)

  Parse Upper Var thisLine dataset allocunits primary secondary ,
                dirblks recfm lrecl blksize volser unit .

  If allocunits = "BLOCKS" Then
    allocunits = "BLOCK("blksize")"

  If Length(recfm) = 1 Then
    newRecfm = recfm
  Else; Do
    /* put spaces between each character of record format */
    newRecfm = ""
    Do j = 1 To Length(recfm)
      newRecfm = newRecfm" "Substr(recfm,j,1)
    End
    newRecfm = Strip(newRecfm)
  End

  If dirblks = 0 Then
    dsorg = "PS"
  Else
    dsorg = "PO"

  /* delete the dataset if it already exists */
  If SYSDSN("'"dataset"'") = "OK" Then; Do
    "DELETE '"dataset"'"
  End

  /* volume and unit are optional */
  volUnit = ""
  If volser ^= "" Then
    volUnit = "VOLUME("volser")"
  If unit ^= "" Then
    volUnit = volUnit "UNIT("unit")"

  noDirBlks = ""
  If dirblks = "U" Then
    noDirBlks = "DSNTYPE(LIBRARY)"
  Else
    noDirBlks = "DIR("dirblks")"

  "ALLOCATE FILE(ALLOC1) DATASET('"dataset"')",
    "SPACE("primary","secondary")" allocunits noDirBlks,
    "RECFM("newRecfm") LRECL("lrecl") BLKSIZE("blksize")",
    "DSORG("dsorg") NEW CATALOG " || volUnit

  If rc ^= 0 Then; Do
    Say "Allocation failed for dataset" dataset
    Return 8
  End
  "FREE FILE(ALLOC1)"
End

Return 0

NOVALUE:
Say "Signal on NOVALUE line" Sigl ":" Sourceline(Sigl)
Return 12
