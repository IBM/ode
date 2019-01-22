/* Rexx ***************************************************************/
/*                                                                    */
/*                    Licensed Materials - Property of IBM            */
/*                                                                    */
/* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.    */
/*                                                                    */
/**********************************************************************/
/*                                                                    */
/* Name: GENALIAS                                                     */
/*                                                                    */
/* Purpose: Generate aliases in the SMP/E distribution libraries.     */
/*                                                                    */
/* Input: Input is read from ddname INDD.                             */
/*        Each line of input contains the distribution library and    */
/*        member name and its alias.                                  */
/*                                                                    */
/* Return code: 0 if all aliases successfully created;                */
/*              non-zero otherwise                                    */
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

Do i = 1 To line.0
  Parse Upper Var line.i dataset alias .
  dataset = "'"dataset"'"

  "RENAME" dataset "("alias")" "ALIAS"
  If rc ^= 0 Then; Do
    Say "Create alias" alias "failed for dataset" dataset
    Return 8
  End
End

Return 0

NOVALUE:
Say "Signal on NOVALUE line" Sigl ":" Sourceline(Sigl)
Return 12
