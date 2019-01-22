/* Rexx ***************************************************************/
/*                                                                    */
/*                    Licensed Materials - Property of IBM            */
/*                                                                    */
/* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.    */
/*                                                                    */
/**********************************************************************/
/*                                                                    */
/* Name: VPLFILES                                                     */
/*                                                                    */
/* Purpose: Process VPL listings.  Each listing data set is tersed    */
/*          and the appropriate VPL keywords are added to the tersed  */
/*          listing.  A final summary that contains the status of     */
/*          each listing is produced in data set:                     */
/*            <applid>.<function>.VPL.FILES                           */
/*                                                                    */
/* Input: Input is read from ddname INDD.                             */
/*        The first line of input contains the VPL keywords that      */
/*        are common to all the listings.  Each remaining line of     */
/*        input contains the name of the listing data set or HFS      */
/*        file, the SPA name of the part and the VPL security of      */
/*        the part.                                                   */
/*                                                                    */
/* Return code: 0 if all VPL listings were successfully created;      */
/*              non-zero if the creation of any of the listings fails */
/*                                                                    */
/**********************************************************************/
/*                                                                    */
/* Author   Defect (D) or Feature (F) and Number                      */
/* ------   ------------------------------------                      */
/* MAD      F 1463  Initial creation                                  */
/**********************************************************************/
/* CMVC File Version: 1.3 */
Trace 'O'
Address 'TSO'
Signal On NOVALUE

/* Allocate the VPL header/trailer data sets.  The header data set */
/* will be re-written for each VPL shippable; the trailer data set */
/* does not change - write its contents now.                       */
date = Substr(Date('S'), 3)
secs = Time('S')

vplHdrDsn = "'"Userid()".TEMP.VPL.HEADER.D"date".S"secs"'"
"ALLOCATE NEW CATALOG FILE(VPLHDR) DATASET("vplHdrDsn")",
  "SPACE(1) TRACKS DIR(0) RECFM(F,B) LRECL(1024) BLKSIZE(6144)"
If rc ^= 0 Then; Do
  Say "Failed to allocate VPL header dataset"
  Return 8
End

vplTrailerDsn = "'"Userid()".TEMP.VPL.TRAILER.D"date".S"secs"'"
"ALLOCATE NEW CATALOG FILE(VPLTRL) DATASET("vplTrailerDsn")",
  "SPACE(1) TRACKS DIR(0) RECFM(F,B) LRECL(1024) BLKSIZE(6144)"
If rc ^= 0 Then; Do
  Say "Failed to allocate VPL trailer dataset"
  Return 8
End

/* write the VPL trailer record */
vplTrailer.1 = "LIODATA='EOF'"
"EXECIO" 1 "DISKW VPLTRL (FINIS STEM VPLTRAILER."
If rc ^= 0 Then; Do
  Say "Failed to write the VPL trailer"
  Return 8
End

/* read input */
"EXECIO * DISKR INDD (STEM LINE."
If rc ^= 0 Then; Do
  Say "Failure reading input"
  Return 8
End

/* first line of input contains product level VPL data */
Parse Upper Var line.1 pkgClass applid function ,
                       vplAuthcode vplFromsys vplVer vplRel ,
                       vplMod vplAckn vplAvaildate terseFlag ptfNum .

/* rest of input contains individual VPL shippables */
listingStatus.0 = 0
maxCC = 0
x = 1
Do While x < line.0    /* read lines 2 to line.0 */
  x = x + 1
  theLine = line.x
  /* handle continuation */
  Do While Right(theLine, 1) = "+"
    x = x + 1
    theLine = Strip(theLine, "T", "+") || line.x
  End
  Parse Var theLine infile spaName vplSecurity vplPartqual .
  infile = "'"infile"'"

  listingDsn = "'"applid"."function"."spaName".LISTING'"
  If pkgClass = "SP" Then
    listingDsn = "'"applid"."function"."ptfNum"."spaName".LISTING'"
  tempListingDsn = "'"applid"."function"."spaName".LISTING.TEMP'"
  tersedDsn = Insert("TERSED.", tempListingDsn, ,
                                index(tempListingDsn, "."))
  status = AllocDataSets()
  If status ^= "OK" Then
    maxCC = 8

  If status = "OK" Then; Do
    status = PrepareListing()
    If status ^= "OK" Then
      maxCC = 8
  End

  If status = "OK" Then; Do
    status = CreateVpl()
    If status ^= "OK" Then
      maxCC = 8
  End

  /* clean up temp data sets... */
  Call DeleteDsn tempListingDsn
  Call DeleteDsn tersedDsn

  /* save listing status */
  L = listingStatus.0 + 1
  listingStatus.L = Strip(listingDsn,,"'") status
  listingStatus.0 = L

End /* Do x = 2 to line.0 */

/* create the data set that contains the status of each listing */

listingStatusDsn = "'"applid"."function".VPL.FILES'"
If pkgClass = "SP" Then
  listingStatusDsn = "'"applid"."function"."ptfNum".VPL.FILES'"
Call DeleteDsn listingStatusDsn

"ALLOC FILE(VPLSTATS) DATASET("listingStatusDsn")",
  "SPACE(3,1) TRACKS DIR(0) RECFM(F,B) LRECL(80)",
  "DSORG(PS) BLKSIZE(0) NEW CATALOG RELEASE"
If rc ^= 0 Then; Do
  say "Failed to allocate status data set" listingStatusDsn
  maxCC = 8
End
Else; Do
  "EXECIO" listingStatus.0 "DISKW VPLSTATS (FINIS STEM LISTINGSTATUS."
  If rc ^= 0 Then; Do
    say "Failed to write to status data set" listingStatusDsn
    maxCC = 8
  End
End

Call DeleteDsn vplHdrDsn
Call DeleteDsn vplTrailerDsn
"FREE ALL"

Return maxCC
/* end mainline */

/*********************************************************************/
/* Procedure AllocDataSets                                           */
/* Allocates the final VPL listing data set & a temporary data set   */
/* used to terse the original listing and also a                     */
/* tersed data set to store the tersed output                        */
/*********************************************************************/
AllocDataSets:
PROCEDURE EXPOSE listingDsn tempListingDsn tersedDsn terseFlag

  /* applid.function.spa_name.listing - final vpl shippable */
  Call DeleteDsn listingDsn
  "ALLOCATE FILE(VPLLIST) REUSE DATASET("listingDsn")",
    "SPACE(4,4) CYLINDERS RELEASE DIR(0) RECFM(F,B) LRECL(1024)",
    "BLKSIZE(6144) NEW CATALOG RELEASE"
  If rc ^= 0 Then; Do
    Say "Failed to allocate" listingDsn
    Return "ALLOCATION FAILED"
  End

  /* applid.function.spa_name.listing.temp - copy of original */
  /* listing to be tersed                                     */
  Call DeleteDsn tempListingDsn
  "ALLOCATE FILE(TMPLIST) REUSE DATASET("tempListingDsn")",
    "SPACE(4,4) CYLINDERS DIR(0) RECFM(F,B) DSORG(PS)",
    "LRECL(133) BLKSIZE(0) NEW CATALOG RELEASE"
  If rc ^= 0 Then; Do
    Say "Failed to allocate" tempListingDsn
    Return "ALLOCATION FAILED"
  End

  /* applid.tersed.function.spa_name.listing.temp                        */
  /* listing of the output from trsmain                                  */
  /* the allocation for this dataset is required only if TRSMAIN is used */
  If terseFlag = "TRSMAIN" Then; Do
    Call DeleteDsn tersedDsn
    "ALLOCATE FILE(TRSLIST) REUSE DATASET("tersedDsn")",
      "SPACE(4,4) CYLINDERS RELEASE DIR(0) RECFM(F,B) DSORG(PS) LRECL(1024)",
      "BLKSIZE(0) NEW CATALOG RELEASE"
    If rc ^= 0 Then; Do
      Say "Failed to allocate" tersedDsn
      Return "ALLOCATION FAILED"
    End
  End

  Return "OK"

/* end AllocDataSets */

/*********************************************************************/
/* Procedure PrepareListing                                          */
/* Copy the original listing data set/file to the temporary listing  */
/* data set and run an oco scan if listing is unclassified.          */
/*********************************************************************/
PrepareListing:
PROCEDURE EXPOSE infile tempListingDsn crc vplSecurity

  If Index(infile, '/') ^= 0 Then
    hfs = 1
  Else
    hfs = 0

  If hfs = 0 Then
    "ALLOCATE FILE(INFILE) REUSE DATASET("infile") SHR"
  Else
    "ALLOCATE FILE(INFILE) PATH("infile") PATHOPTS(ORDONLY)"

  "OCOPY INDD(INFILE) OUTDD(TMPLIST) TEXT"
  If rc ^= 0 Then; Do
    "FREE FILE(INFILE)"
    Say "Failed to copy" infile "to" tempListingDsn
    Return "COPY FAILED"
  End
  "FREE FILE(INFILE)"

  If vplSecurity = "UNC" Then; Do
    "OCOCHECK" tempListingDsn
    If rc ^= 0 Then; Do
      Say infile "failed OCO scan"
      Return "BAD SCAN"
    End
  End

  Return "OK"
/* end PrepareListing */

/*********************************************************************/
/* Procedure CreateVpl                                               */
/* Write out the contents of the VPL header dataset; terse the       */
/* temporary listing data set; merge the VPL header, the tersed      */
/* listing data set and the VPL trailer to create the final VPL      */
/* listing.                                                          */
/*********************************************************************/
CreateVpl:
PROCEDURE EXPOSE tempListingDsn listingDsn tersedDsn,
                 vplHdrDsn vplTrailerDsn,
                 applid function spaName,
                 vplAuthcode vplFromsys vplVer vplRel,
                 vplMod vplAckn vplAvaildate,
                 vplSecurity vplPartqual terseFlag ptfNum,
                 pkgClass

  /* create the VPL header */
  timeLong = Time('L')
  Parse Var timeLong hh':'mm':'ss'.'ms
  th = Left(ms, 2)

  vplHdr.1  = "LIODATA='REQUIRED'"
  vplHdr.2  = "TRANNAME='VPL_LISTING'"
  vplHdr.3  = "TRANID='"Date('S')||hh||mm||ss||th"'"
  vplHdr.4  = "AUTHCODE='"vplAuthcode"'"
  vplHdr.5  = "FROMSYS='"vplFromsys"'"
  vplHdr.6  = "PRODQUAL='"function"'"
  vplHdr.7  = "PART='"spaName"'"
  vplHdr.8  = "VPLSECURITY='"vplSecurity"'"

  n = 8
  If pkgClass = "IPP" Then; Do
    n = n + 1; vplHdr.n = "VER='"vplVer"'"
    n = n + 1; vplHdr.n = "REL='"vplRel"'"
    n = n + 1; vplHdr.n = "MOD='"vplMod"'"
    If vplAvaildate ^= "NONE" Then; Do
      n = n + 1; vplHdr.n = "AVAILDATE='"vplAvaildate"'"
    End
  End
  Else; Do
    n = n + 1; vplHdr.n = "PTF='"ptfNum"'"
  End

  n = n + 1; vplHdr.n = "ACKN='"vplAckn"'"
  If vplPartqual ^= "NONE" Then; Do
    n = n + 1; vplHdr.n = "PARTQUAL='"vplPartqual"'"
  End

  n = n + 1; vplHdr.n = "LIODATA='BULK'"
  "EXECIO" n "DISKW VPLHDR (FINIS STEM VPLHDR."
  If rc ^= 0 Then; Do
    Say "Failed to write VPL header"
    Return "TERSE FAILED"
  End

  /* TRSMAIN the listing data set if terseFlag is "TRSMAIN" */
  If terseFlag = "TRSMAIN" Then; Do
    "ALLOCATE FILE(INFILE) REUSE DATASET("tempListingDsn") SHR"
    "ALLOCATE FILE(OUTFILE) REUSE DATASET("tersedDsn") OLD"
    "INVOKE TRSMAIN 'SPACK'"
    If rc ^= 0 Then; Do
      Say "TRSMAIN" tempListingDsn "failed"
      Return "TRSMAIN FAILED"
    End
    "FREE FI(INFILE)"
    "FREE FI(OUTFILE)"
  End
  /* TERSE the listing data set if terseFlag is "TERSE" */
  Else; Do
    "TERSE" tempListingDsn "SPACK"
    If rc ^= 0 Then; Do
      Say "TERSE" tempListingDsn "failed"
      Return "TERSE FAILED"
    End
  End

  /* merge the header and trailer with the tersed listing */
  "ALLOCATE FILE(MERGED) REUSE SHR",
    "DATASET("vplHdrDsn tersedDsn vplTrailerDsn")"

  "OCOPY INDD(MERGED) OUTDD(VPLLIST)"
  If rc ^= 0 Then; Do
    Say "Copy merged VPL files to" listingDsn "failed"
    Return "TERSE FAILED"
  End
  "FREE FI(MERGED)"

  Return "OK"
/* end CreateVpl */

/*********************************************************************/
/* Procedure DeleteDsn                                               */
/*********************************************************************/
DeleteDsn:
PROCEDURE
Parse Arg dataset .
If SYSDSN(dataset) = "OK" Then; Do
  "ALLOCATE FILE(DELETE) REUSE DATASET("dataset") SHR DELETE"
  "FREE FI(DELETE)"
End
Return
/* end Delete */

NOVALUE:
Say "Signal on NOVALUE line" Sigl ":" Sourceline(Sigl)
Exit 12
