/* Rexx ***************************************************************/
/*                                                                    */
/*                    Licensed Materials - Property of IBM            */
/*                                                                    */
/* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.    */
/*                                                                    */
/**********************************************************************/
/*                                                                    */
/* Name: OCOCHECK                                                     */
/*                                                                    */
/* Purpose: Check data set for object code only strings.              */
/*                                                                    */
/* Input: Name of data set to be checked.                             */
/*                                                                    */
/* Return code: 0 if no object code only strings are found            */
/*              non-zero if oco strings are found or if error occurs  */
/*              during processing.                                    */
/*                                                                    */
/**********************************************************************/
/*                                                                    */
/* Author   Defect (D) or Feature (F) and Number                      */
/* ------   ------------------------------------                      */
/* MAD      F 1463  Initial creation                                  */
/**********************************************************************/
/* CMVC File Version: 1.3 */

 trace o                             /*                               */
 parse arg infile                    /*                               */
 address tso                         /*                               */
 ocoline = 0;confline = 0            /* zero flags                    */
 ococol  = 0;confcol  = 0;           /* zero flags                    */
 ctr = 0                             /* loop index variable           */
 "ALLOC DD(INFILE) DSN("infile") SHR"  /*                             */
'EXECIO * DISKR INFILE ( FINI STE DATA.' /*                           */
 if rc ^= 0 then call cleanup rc     /*                               */
   do ctr = 1 to data.0              /* process the file              */
   colcnt = length(data.ctr)         /* pick up length                */
   test = data.ctr                   /* get so i can upper            */
   upper test                        /* and do it                     */
   if confline = 0 then              /* if not found allready         */
     do                              /*                               */
     loc = pos('CONFIDENTIAL',TEST)  /* is word confidential on line  */
     if loc ^= 0 then                /* if so then                    */
       do                            /*                               */
       confline = ctr                /* save line number              */
       confcol = loc                 /* save column number            */
       confdata = test               /* save actual data              */
       end                           /*                               */
     end                             /*                               */
   if ocoline = 0 then               /* if not found already          */
     do col = 1 to length(test)      /*                               */
     loc = pos(' OCO',test,col)      /*  find OCO characters          */
     if loc = 0 then loc = pos('''OCO',test,col) /*                   */
     if loc = 0 then loc = pos('"OCO',test,col) /*                    */
     if loc = 0 then loc = pos('-OCO',test,col) /*                    */
     if loc = 0 then loc = pos(' OBJECT CODE ONLY',test,col)  /*      */
     if loc = 0 then loc = pos('''OBJECT CODE ONLY',test,col) /*      */
     if loc = 0 then loc = pos('"OBJECT CODE ONLY',test,col)  /*      */
     if loc = 0 then loc = pos('-OBJECT CODE ONLY',test,col)  /*      */
     if loc = 0 then leave col       /* if not there, get out         */
     col = loc + 4                   /* bump column counter           */
     if col > colcnt then leave col  /* it's on end of line           */
     if(substr(test,loc+4,3) = 'ECT' &, /* if full spelling and       */
       (substr(test,loc+17,1) = ' '  |, /*    a space or              */
        substr(test,loc+17,1) = '-'  |, /*    a dash or               */
        substr(test,loc+17,1) = '"'  |, /*    a double quote          */
        substr(test,loc+17,1) = '''')) |,  /* a quote                 */
       (substr(test,loc+4,3)^= 'ECT' &, /* if acyronym and            */
       (substr(test,loc+4,1) = ' '  |,  /*    a space or              */
        substr(test,loc+4,1) = '-'  |,  /*    a dash or               */
        substr(test,loc+4,1) = '"'  |,  /*    a double quote          */
        substr(test,loc+4,1) = '''')) then/*  a quote                 */
       do                            /*                               */
       ocoline = ctr                 /* save line number              */
       ococol  = loc                 /* save column number            */
       ocodata = test                /* save actual data              */
       leave col                     /* and get out                   */
       end                           /*                               */
     end col                         /*                               */
   if ocoline ^= 0 | confline ^= 0 then leave /*getout if either      */
   end                               /*                               */
 drop data.                          /* release some storage          */
 if   confline ^= 0 then             /* or confidential               */
   do                                /*                               */
   say 'line' confline 'has CONFIDENTIAL in column' confcol /*        */
   say confdata                      /*                               */
   call cleanup 5                    /*                               */
   end                               /*                               */
 if   ocoline ^= 0 then              /* and has oco                   */
   do                                /*                               */
   say 'line' ocoline 'has the keyword OCO in column' ococol /*       */
   say ocodata                       /*                               */
   call cleanup 5                    /*                               */
   end                               /*                               */
call cleanup 0                       /*                               */
cleanup:                             /*                               */
 "FREE DSN("infile") "               /*                               */
exit arg(1)                          /*                               */
