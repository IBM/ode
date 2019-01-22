/*
 *************************************************************************
 *                                                                       *
 * Copyright 2000 Compaq Computer Corporation                            *
 *                                                                       *
 * COMPAQ Registered in U.S. Patent and Trademark Office.                *
 *                                                                       *
 *************************************************************************
 * IMPORTANT: Carefully read the License Terms below before              *
 * proceeding.  By use of these materials you agree to these terms.      *
 * If you do not agree to these terms, you may not use this software or  *
 * the accompanying documentation.                                       *
 *************************************************************************
 * LICENSE TERMS                                                         *
 * 1. GRANT                                                              *
 * Compaq Computer Corporation ("COMPAQ") grants you the right to use,   *
 * modify, and distribute the following source code (the "Software")     *
 * on any number of computers. You may use the Software as part of       *
 * creating a software program or product intended for commercial or     *
 * non-commercial distribution in machine-readable source code, binary,  *
 * or executable formats. You may distribute the Software as             *
 * machine-readable source code provided this license is not removed     *
 * from the Software and any modifications are conspicuously indicated.  *
 * 2. COPYRIGHT                                                          *
 * The Software is owned by COMPAQ and its suppliers and is protected by *
 * copyright laws and international treaties.  Your use of the Software  *
 * and associated documentation is subject to the applicable copyright   *
 * laws and the express rights and restrictions of these terms.          *
 * 3. RESTRICTIONS                                                       *
 * You may not remove any copyright, trademark, or other proprietary     *
 * notices from the Software or the associated  documentation.           *
 * You are responsible for compliance with all applicable export or      *
 * re-export control laws and regulations if you export the Software.    *
 * This license is governed by and is to be construed under the laws     *
 * of the State of Texas.                                                *
 *                                                                       *
 * DISCLAIMER OF WARRANTY AND LIABILITY                                  *
 * Compaq shall not be liable for technical or editorial errors or       *
 * omissions contained herein. The information contained herein is       *
 * subject to change without notice.                                     *
 *                                                                       *
 * THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND.       *
 * THE ENTIRE RISK ARISING OUT OF THE USE OF THIS SOFTWARE REMAINS WITH  *
 * RECIPIENT.  IN NO EVENT SHALL COMPAQ BE LIABLE FOR ANY DIRECT,        *
 * CONSEQUENTIAL, INCIDENTAL, SPECIAL, PUNITIVE OR OTHER DAMAGES         *
 * WHATSOEVER (INCLUDING WITHOUT LIMITATION DAMAGES FOR LOSS OF BUSINESS *
 * PROFITS, BUSINESS INTERRUPTION, OR LOSS OF BUSINESS INFORMATION),     *
 * EVEN IF COMPAQ HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES    *
 * AND WHETHER IN AN ACTION OF CONTRACT OR TORT INCLUDING NEGLIGENCE.    *
 *                                                                       *
 * If you have any questions concerning this license, please contact:    *
 * Compaq Computer Corporation, Software Business Practices, ZKO1-2/D22, *
 * 110 Spit Brook Road, Nashua, NH. 03062-2698.                          *
 *                                                                       *
 *************************************************************************
 */

/*
**  MODULE DESCRIPTION: a utime function for VMS
*/

#include <stdio.h>
#include <string.h>
#include <fibdef.h>
#include <atrdef.h>
#include <sbkdef.h>
#include <iodef.h>
#include <rms.h>
#include <descrip.h>
#include <starlet.h>
#include <lib$routines.h>
#include <stat.h>
#include <unixlib.h>
#include <stdlib.h>
#include <unistd.h>
#include <fabdef.h>
#include <namdef.h>
#include <fcntl.h>

#define THE_JACKETS
#include "lib/portable/native/cutime.h"

/***************************/
/********** utime **********/
/***************************/
int GENERIC_UTIME (const char *file_spec, const struct utimbuf *times)
{
   unsigned int base_time[2], temp[2], unix_timecheck;
   unsigned long unix_time;
   char tracebuf[128];
   struct tm *stupid_tm;

   struct FAB    fab;           /* File attributes block                   */
   struct NAM    nam;           /* Name block                              */
   struct fibdef fib;           /* File information block                  */

  struct FIB_DESC
                {
                   int length;          /* Length of file information block  */
                   struct fibdef *fib;  /* Address of file information block */
                }
                fib_desc =
                {
                  FIB$C_LENGTH,
                  &fib
                };

   unsigned int revdate[2];                /* 64 bit create date */

   struct atrdef  atr[2] =           /* File attributes block               */
                  {
#ifdef __ALPHA
                     {ATR$S_REVDATE, ATR$C_REVDATE, &revdate}, /* date    */
#else
/* For some reason the prototype is wrong on VAX - should be void * not uint */
                     {ATR$S_REVDATE, ATR$C_REVDATE, (unsigned int) &revdate}, /* date    */
#endif
                     {0,0,0}                               /* End of atr    */
                  };

   char full_name[NAM$C_MAXRSS]; /* Full filename returned by RMS           */
   char nam_esa[NAM$C_MAXRSS];   /* Needed by search and parse              */
   $DESCRIPTOR(fullname_desc, full_name);  /* Needed by $ASSIGN             */

   /* Used by $QIO system service... */
   unsigned long iosb[2], status, function;
   unsigned short channel;

   /*
   ** Somehow I just don't think UNIX filespecs are going to cut it,
   ** when we start doing this system service calls.
   */
   char temp_string[TEMP_STRING_SIZE];
   char *new_filename, *newVMS_filename;
   int  fnp_status;

   new_filename = GENERIC_VMS_Filename_parser((char *)file_spec,
                                        temp_string,
                                        GENERIC_K_STRING_TYPE_FILE,
                                        &fnp_status);
   newVMS_filename = GENERIC_EXTERNAL_NAME( new_filename, 0 );
   GENERIC_TRACE("utime: Mapped:", file_spec, "To:", newVMS_filename);

   /* Initialise the fab block  */
   fab = cc$rms_fab;
   fab.fab$l_nam = &nam;
   fab.fab$l_fop = FAB$M_NAM;
   fab.fab$l_fna = (char *)newVMS_filename;
   fab.fab$b_fns = strlen(newVMS_filename);

   /* Initialise the nam block */
   nam = cc$rms_nam;
   nam.nam$l_rsa = (char *)&full_name; /* Full filename wanted at this addr  */
   nam.nam$b_rss = NAM$C_MAXRSS;    /* maximum size of full filename         */
   nam.nam$l_esa = (char *)&nam_esa;/* Expanded string address               */
   nam.nam$b_ess = NAM$C_MAXRSS;    /* maximum size of expanded filename str */



   /* Use RMS to find the file and fill in the NAM and FAB blocks       */
   if ( ((status=sys$parse(&fab,0,0)) & 1) == 1)
       status = sys$search(&fab,0,0);

   if ((status & 1) == 0) return status;


   /* Initialise the fib block  */
   memset(&fib,0,FIB$C_LENGTH);

   /* Copy the File ID from the nam block to the fib block */
    memcpy( &fib.fib$r_fid_overlay , &nam.nam$w_fid[0], 6 );

   /* Fill in the length of the filename in the descriptor */
   fullname_desc.dsc$w_length = nam.nam$b_rsl;

   /* Assign a channel to the file so it can be opened     */

    status = sys$assign(
                        &fullname_desc, /* Device Name             */
                        &channel,  /* Returned Channel number */
                        0,              /* Access Mode             */
                        0               /* Mailbox Name            */
                       );

   if ((status & 1) == 0) return status;

   /*************************************************************************
    * We must now issue an ACCESS QIO to open the file.  We need to specify *
    * a file attributes block and a statistics block in order to obtain     *
    * the allocation count for the file                                     *
    *************************************************************************/

    /* Say that we intend to write */
    fib.fib$l_acctl = FIB$M_WRITE;

   function = IO$_ACCESS | IO$M_ACCESS;
   status = sys$qiow(
           0,                     /* Event Flag Number         */
           channel,               /* I/O channel               */
           function,              /* I/O function code         */
           iosb,                  /* I/O status block address  */
           0,                     /* AST Address               */
           0,                     /* AST Parameter             */
           &fib_desc,             /* P1, fib descriptor        */
           0,                     /* P2, not used              */
           0,                     /* P3, not used              */
           0,                     /* P4, not used              */
           atr,                   /* P5, Attributes block      */
           0);                    /* P6, not used              */

   if ((status & 1) == 0) return status;
   if ((iosb[0] & 1) == 0) return iosb[0];

   /*
   ** If times == NULL then we set modtime to current time.
   ** Note: on VMS actime = modtime (since actime doesn't really apply.)
   */
   if ( times )
      {
      unix_time = times->modtime;

      /*
      ** For some crazy reason, when we feed the VMS time to the QIO
      ** it gets modified into GMT !!!  This is darn darn annoying,
      ** so I going to attempt to use tm_gmtoff to compensate.
      ** NOTE: This aint ANSI89!
      */
      stupid_tm = localtime( &unix_time );
      if ( stupid_tm->tm_gmtoff ) unix_time += stupid_tm->tm_gmtoff;

      /*
      ** base_time is the length of time in 100 nanosecond intervals
      ** between 11/17/1885 & 1/1/1970 which we need to calculate unix time
      ** from VMS time (reverse of decc$fix_time function)
      */
      base_time[0] = 0x4beb4000;
      base_time[1] = 0x007c9567;
      lib$emul( &10000000, &unix_time, &0, temp );
      lib$addx( temp, base_time, revdate, &2 );

      unix_timecheck = decc$fix_time( revdate );

      sprintf(tracebuf, "utime( %s, %d )\nunix_timecheck=%d",
              newVMS_filename, unix_time, unix_timecheck );
      }
   else
      {
      sys$gettim( &revdate[0] );
      sprintf(tracebuf, "utime( %s, 0 )", newVMS_filename );
      }
   GENERIC_TRACE("VMS:", tracebuf, "", "");

   /* Now update the creation date */

   function = IO$_DEACCESS;
   status = sys$qiow(
           0,                     /* Event Flag Number         */
           channel,               /* I/O channel               */
           function,              /* I/O function code         */
           iosb,                  /* I/O status block address  */
           0,                     /* AST Address               */
           0,                     /* AST Parameter             */
           &fib_desc,             /* P1, fib descriptor        */
           0,                     /* P2, not used              */
           0,                     /* P3, not used              */
           0,                     /* P4, not used              */
           atr,                   /* P5, Attributes block      */
           0);                    /* P6, not used              */

   if ((status & 1) == 0) return status;
   if ((iosb[0] & 1) == 0) return iosb[0];

   status = sys$dassgn(
                        channel  /* Channel number */
                       );

   if ((status & 1) == 0) return status;

   return 0;
}


/* filesystem_parsing.c starts here */


/*
 *************************************************************************
 *                                                                       *
 * Copyright 2000 Compaq Computer Corporation                            *
 *                                                                       *
 * COMPAQ Registered in U.S. Patent and Trademark Office.                *
 *                                                                       *
 *************************************************************************
 * IMPORTANT: Carefully read the License Terms below before              *
 * proceeding.  By use of these materials you agree to these terms.      *
 * If you do not agree to these terms, you may not use this software or  *
 * the accompanying documentation.                                       *
 *************************************************************************
 * LICENSE TERMS                                                         *
 * 1. GRANT                                                              *
 * Compaq Computer Corporation ("COMPAQ") grants you the right to use,   *
 * modify, and distribute the following source code (the "Software")     *
 * on any number of computers. You may use the Software as part of       *
 * creating a software program or product intended for commercial or     *
 * non-commercial distribution in machine-readable source code, binary,  *
 * or executable formats. You may distribute the Software as             *
 * machine-readable source code provided this license is not removed     *
 * from the Software and any modifications are conspicuously indicated.  *
 * 2. COPYRIGHT                                                          *
 * The Software is owned by COMPAQ and its suppliers and is protected by *
 * copyright laws and international treaties.  Your use of the Software  *
 * and associated documentation is subject to the applicable copyright   *
 * laws and the express rights and restrictions of these terms.          *
 * 3. RESTRICTIONS                                                       *
 * You may not remove any copyright, trademark, or other proprietary     *
 * notices from the Software or the associated  documentation.           *
 * You are responsible for compliance with all applicable export or      *
 * re-export control laws and regulations if you export the Software.    *
 * This license is governed by and is to be construed under the laws     *
 * of the State of Texas.                                                *
 *                                                                       *
 * DISCLAIMER OF WARRANTY AND LIABILITY                                  *
 * Compaq shall not be liable for technical or editorial errors or       *
 * omissions contained herein. The information contained herein is       *
 * subject to change without notice.                                     *
 *                                                                       *
 * THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND.       *
 * THE ENTIRE RISK ARISING OUT OF THE USE OF THIS SOFTWARE REMAINS WITH  *
 * RECIPIENT.  IN NO EVENT SHALL COMPAQ BE LIABLE FOR ANY DIRECT,        *
 * CONSEQUENTIAL, INCIDENTAL, SPECIAL, PUNITIVE OR OTHER DAMAGES         *
 * WHATSOEVER (INCLUDING WITHOUT LIMITATION DAMAGES FOR LOSS OF BUSINESS *
 * PROFITS, BUSINESS INTERRUPTION, OR LOSS OF BUSINESS INFORMATION),     *
 * EVEN IF COMPAQ HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES    *
 * AND WHETHER IN AN ACTION OF CONTRACT OR TORT INCLUDING NEGLIGENCE.    *
 *                                                                       *
 * If you have any questions concerning this license, please contact:    *
 * Compaq Computer Corporation, Software Business Practices, ZKO1-2/D22, *
 * 110 Spit Brook Road, Nashua, NH. 03062-2698.                          *
 *                                                                       *
 *************************************************************************
 */

int GENERIC_fs_init_done =0;
int GENERIC_filename_controls[64]; /* 64 bits */
int GENERIC_dir_mapping_count = 0;

#define PARSE_UNIX_AND_VMS 3
#define DIR_IN_DIRECTORY_NAME 9
#define VALID_CHARACTERS_FILENAME 12
#define HIDDEN_FILENAME_UNDER 14
#define HIDDEN_FILENAME_DOT 15
#define DOT_IN_DIRNAME_UNDER  17
#define DOT_IN_DIRNAME_REMOVE 18
#define MULTI_DOT_IN_FILENAME_LAST  20
#define MULTI_DOT_IN_FILENAME_FIRST 21
#define FILENAME_LARGER_39_TRUNC 26
#define FILENAME_LARGER_39_MOVE  27
#define DIR_OVER_8_LEVELS_DEEP   29

typedef struct  GENERIC_dir_mapping_struct
	{
	char * dir_name;
	char * map_name;
	int  dir_name_strlen;
	int  map_name_strlen;
	} GENERIC_dir_mapping_struct_t;

GENERIC_dir_mapping_struct_t **GENERIC_dir_mapping_list;


char *GENERIC_FileName_Parse_U_V(
	char *in_string,
	char *work_string,
	int *status)
{

	int match_found;
	char *match_str;
	char *temp_str;
	char *start_str;
	char *active_string = in_string;

	*status = 0;
        strcpy(work_string,active_string);
	if ((match_str = strchr(work_string,']')) ||
	    (match_str = strchr(work_string,'>')) ||
	    (match_str = strchr(work_string,':')))
	{
		match_str++; /* point just pass the vms stuff*/	
		*match_str = '\0';
		if (!(start_str = strrchr(work_string,'/')))
		/* not unix stuff already, point work_stringstart_str*/
			start_str = work_string;
		else
		/* unix part already, keep it, vms stuff in middle*/
			start_str++;/* point one past*/

		temp_str = decc$translate_vms (start_str);
		if ((((int )temp_str)== -1) || !temp_str)
		{    
#ifdef DEBUG
		printf("decc$translate_vms error: %s\n",start_str);
#endif
		return active_string;
		}

		if ((work_string != start_str) && !strncmp(temp_str,"./",2))
                   /* vms name in the middle check for "./" if found remove 
                      it */
		   {                   
			temp_str += 2; /* move past the ./*/
                   };

		strcpy(start_str,temp_str);
		/* ok let's get the rest of the string */
		if (!(match_str = strchr(active_string,']')))
		if (!(match_str = strchr(active_string,'>')))
		   match_str = strchr(active_string,':');
		match_str++;
		if (*match_str != '/')
			strcat(work_string,"/");
		strcat(work_string,match_str);
		*status = 1; /* return we change the context*/
		return work_string; /* return the correct pointer*/
	}
	else
	return(active_string);

}

char *GENERIC_FileName_valid_chars(
	char *in_string,
	char *work_string,
	int *status)
{
	int match_found,j,i;
	int start_search;
	*status = 0;

	/* does it begin with ~, if so leave it */
	if (in_string[0]!='~')
	   start_search =0;
	else
	   start_search =1;
        
	for (i=start_search;i<strlen(in_string);i++)
	{
		if ((in_string[i] < 'a') || (in_string[i]>'z'))
			if ((in_string[i] < 'A') || (in_string[i]>'Z'))
				if ((in_string[i] < '0') || (in_string[i]>'9'))
				if ((in_string[i]!='.') &&
				    (in_string[i]!='/') &&
				    (in_string[i]!='$') &&
				    (in_string[i]!='-')  &&
				    (in_string[i]!=';'))
				{
					*status=2;
					in_string[i] = '_';
				}
	}

	return in_string;

}
char *GENERIC_FileName_dir_in_dir(
	char *in_string,
	char *work_string,
	int *status)
{
	int match_found,j,i;
	char *match_str;
	char *first_str;
	char *last_str;
	char *temp_str;
	char *active_string = in_string;

	*status = 0;
	if (active_string == '\0')
		return active_string;
	if (!*active_string) /* null string*/
		return active_string;

	temp_str = strstr(active_string,".dir");
	if (!temp_str) /* check upcase*/
		temp_str = strstr(active_string,".DIR");
		
	if (!temp_str)
		/* no .dir */
		return active_string;

	for (i=0,j=0;i<strlen(active_string);i++)
	if (active_string[i]!='.')
		work_string[j++] = active_string[i];
	else
		/* check for .dir */
		if (!strncasecmp(&active_string[i],".dir",4))
		{
			*status = 1;
			i += 3;
		}
		else
			work_string[j++] =active_string[i];
	
	if (*status == 1)
	{
		work_string[j] = '\0';
		return work_string; /* changes */
	}
	else
		return active_string; /* no changes*/
}

char *GENERIC_FileName_Hidden_under(
        char *in_string,
        char *work_string,
        int *status)
{

        char *active_string = in_string;
        char *search_str;

        *status = 0;
        if (!in_string || !*in_string)
                return active_string; /* null pointer or null string*/

        if (active_string == '\0')
                return active_string;

        search_str = in_string;

        /*
        ** Do not replace a dot with an underscore if it's
        ** part of either "/.." or "/./"  mb 19990706
	** or a trailing "/." cb 20000602
        */
        while(search_str = strstr(search_str,"/."))
        {
                if ((search_str[2] != '.') &&
                    (search_str[2] != '/') &&
                    (search_str[2] != '\0') )
                {
                        *status = 2;
                        search_str[1] = '_'; /* change "." to underscore */
                }

		search_str+=2; /* point past /. or new /_ */
        }

        /*
        ** Check the base name.
        ** Do not replace a dot with an underscore if it's
        ** part of either "../" or "./" or "..", or if the
        ** entire string is only ".".
        */
        if (*active_string == '.')
                if (strncmp(active_string,"../",3) &&
                    strncmp(active_string,"./",2) &&
                    (strlen(active_string)>1) &&
                    strcmp(active_string,".."))
                {
                        *active_string = '_';
                        *status = 2;
                }

        return active_string;
}

char *GENERIC_FileName_Hidden_file(
	char *in_string,
	char *work_string,
	int *status)
{
	int match_found;
	char *match_str;
	char *temp_str;
	char *active_string = in_string;

	if (!in_string || !*in_string)
		return active_string; /* null pointer or null string*/
	*status = 0;
	if (active_string == '\0')
		return active_string;
	if (!strcmp(active_string,".") ||
	    !strcmp(active_string,".."))
		return active_string;

	temp_str = match_str = basename(active_string);
	if (!match_str)
	{
		printf("error basename: %s\n",active_string);
		return active_string;
	}
	if (!strcmp(match_str,".") ||
	    !strcmp(match_str,".."))
		return active_string;
	if (*match_str == '.')
	{
	   *status =2;
	   /* remove the hidden filename*/
	   match_str++;
	   strcpy(work_string,match_str);
	   strcpy(temp_str,work_string);
	}
	return active_string;
}

char *GENERIC_FileName_dot_dir_u(
	char *in_string,
	char *work_string,
	int *status,
	int string_type_indicator)
{
	char *search_str;
	char *last_char;
	char *active_string = in_string;

	*status = 0;
	if (active_string == '\0')
		return active_string;
	if (!*active_string) /* null string*/
		return active_string;

	search_str = active_string;
	if (string_type_indicator == GENERIC_K_STRING_TYPE_DIRECTORY)
	{
		int str_count = strlen(active_string);
		last_char = &active_string[str_count];
		
	}	       
	else
	{
		last_char = strrchr(active_string,'/');
		if (!last_char) /* no directories to scan*/
			return (active_string);
	}
	
	while(search_str<=last_char)
	{/* scan until done*/

		while ((*search_str == '.') && (search_str<=last_char))
		{
		if (!strncmp(search_str,"../",3))
			search_str += 3;
		else
		if (!strncmp(search_str,"./",2))
			search_str += 2;
		else
		if (!strcmp(search_str,".."))
			search_str += 2;
		else
		if (!strcmp(search_str,"."))
			search_str++;
		}
		if (search_str<=last_char)
		{
			while(*search_str !='/' && (search_str <= last_char))
			{
				if (*search_str=='.')
				{
					*search_str='_';
					*status = 2;
				}
				search_str++;
			}
			search_str++; /* one pass the '/' */
		}		
	}
	return active_string;
}

char *GENERIC_FileName_dot_dir_r(
	char *in_string,
	char *work_string,
	int *status)
{
	char *active_string = in_string;

	*status = 0;
	if (active_string == '\0')
		return active_string;
	if (!*active_string) /* null string*/
		return active_string;
	return active_string;
}

char *GENERIC_FileName_Multi_dot_f(
	char *in_string,
	char *work_string,
	int *status)
{
	int match_found;
	char *match_str;
	char *temp_str;
	char *active_string = in_string;

	*status = 0;
	if (active_string == '\0')
		return active_string;

	match_str = basename(active_string);
	if (!match_str)
	{
		printf("error basename: %s\n",active_string);
		return active_string;
	}
	if (!strcmp(match_str,".") ||
	    !strcmp(match_str,".."))
		return active_string;
	
	if (!(match_str = strchr(match_str,'.')))
		/* nothing to look at */
		return active_string;
	match_str++; /* got first one, move past it*/
	while(match_str = strchr(match_str,'.'))
	{		
		*match_str= '_';
		match_str++;
		*status = 2;
	}		
	return active_string;
	
}
char *GENERIC_FileName_Multi_dot_l(
	char *in_string,
	char *work_string,
	int *status)
{
	int match_found;
	char *match_str;
	char *temp_str;
	char *search_str;
	char *active_string = in_string;

	*status = 0;
	if (active_string == '\0')
		return active_string;

	temp_str = match_str = basename(active_string);
	if (!match_str)
	{
		printf("error basename: %s\n",active_string);
		return active_string;
	}
	if (!strcmp(match_str,".") ||
	    !strcmp(match_str,".."))
		return active_string;
	
	if (!(match_str = strrchr(match_str,'.')))
		/* nothing to look at */
		return active_string;
	search_str = temp_str;
	while(search_str < match_str)
	if ((*search_str == '.') && (search_str != match_str))
	{		
		*search_str= '_';
		search_str++;
		*status = 2;
	}
	else
	search_str++;
	return active_string;
	
}

char *GENERIC_FileName_gt_39_trunc(
	char *in_string,
	char *work_string,
	int *status)
{
	int match_found;
	char *match_str;
	char *temp_str;
	char *active_string = in_string;

	*status = 0;
	if (active_string == '\0')
		return active_string;

	temp_str = match_str = basename(active_string);
	if (!match_str)
	{
		printf("error basename: %s\n",active_string);
		return active_string;
	}
	
	if (strlen(match_str)<40) /* fits don't waste cpu*/
		return active_string;

	if (!(match_str = strchr(match_str,'.')))
	{/* no . is it greater than 39?*/
		if (strlen(temp_str)>39)
			active_string[39]=0, *status = 2;
		return active_string;
	}
	if (strlen(match_str)>40) /* add one for the . */
	{/* yes trunc the end*/
		match_str[40] = '\0';
		*status = 2;
	}

	/* let's check left of the . */
	if ((match_str - temp_str ) > 39)
	{/* must be let's use work_string*/
		strncpy(work_string,
			active_string,
			(match_str - temp_str));
		work_string[(temp_str - active_string) + 39] = '\0';
		strcat(work_string, match_str);
		*status = 1;
		return work_string;
	}

	return active_string;
	
}
char *GENERIC_FileName_gt_39_move(
	char *in_string,
	char *work_string,
	int *status)
{
	int match_found;
	char *match_str;
	char *temp_str;
	char *active_string = in_string;

	*status = 0;
	if (active_string == '\0')
		return active_string;

	temp_str = match_str = basename(active_string);
	if (!match_str)
	{
		printf("error basename: %s\n",active_string);
		return active_string;
	}
	
	if (strlen(match_str)<40) /* fits don't waste cpu*/
		return active_string;

	if (!(match_str = strchr(match_str,'.')))
	{/* no . is it greater than 39?*/
		if (strlen(temp_str)>39)
		{
			int i;
			*status = 2;
			strcpy(work_string,&temp_str[39]);
			temp_str[39]='.';
			strcpy(&temp_str[40], work_string);
			temp_str[79] = '\0';
		}
		return active_string;
	}
	if ((strlen(match_str)>40) || 
		((match_str - temp_str) > 39))
	{
		int i,j;
		*status = 2;
		for (i=0,j=0;i<strlen(temp_str);i++)
		{
			if (temp_str[i]!='.')
			{
				work_string[j]=temp_str[i];
				j++;
				if (j==39)
					work_string[j++]='.';
			}
		};
		if (j > 79)
			j = 79;
                work_string[j]='\0';
		strcpy(&active_string[(temp_str - active_string)], work_string);
		return active_string;
	}


	return active_string;
	
}

int GENERIC_dir_mapping_init()
{
	char *temp_getenv_string;
	int i;

	if (!(temp_getenv_string = getenv(VMS_PREFIX "DIRECTORY_MAPPING_COUNT")))
		return 0; /* not enabled */
	else 
		GENERIC_dir_mapping_count = atoi(temp_getenv_string);

	GENERIC_dir_mapping_list = malloc(sizeof (GENERIC_dir_mapping_struct_t *) *GENERIC_dir_mapping_count);

	for (i=0;i<GENERIC_dir_mapping_count;i++)
	{
		char temp_string[256];

		GENERIC_dir_mapping_list[i] = malloc(sizeof (GENERIC_dir_mapping_struct_t));
		sprintf(temp_string, VMS_PREFIX "DIRECTORY_MAPPING_%02d",i+1);
		if (!(temp_getenv_string = getenv(temp_string)))
		{
			GENERIC_dir_mapping_count =0;
			printf("error: " VMS_PREFIX "DIRECTORY_MAPPING_COUNT is set, but can't find %s\n",temp_string);
			printf("disabling " VMS_PREFIX "DIRECTORY_MAPPING_COUNT\n");
			return(0);
		};
		GENERIC_dir_mapping_list[i]->dir_name = malloc(strlen(temp_getenv_string)+1);
		strcpy(GENERIC_dir_mapping_list[i]->dir_name,temp_getenv_string);
		if (!(GENERIC_dir_mapping_list[i]->map_name = strchr(GENERIC_dir_mapping_list[i]->dir_name,'=')))
		{
			GENERIC_dir_mapping_count =0;
			printf("error: %s missing = assignment\n",temp_string);
			printf("disabling " VMS_PREFIX "DIRECTORY_MAPPING_COUNT");
			return(0);
		};
		GENERIC_dir_mapping_list[i]->map_name[0] = '\0';
		GENERIC_dir_mapping_list[i]->map_name++;
		GENERIC_dir_mapping_list[i]->dir_name_strlen = strlen(GENERIC_dir_mapping_list[i]->dir_name);
		GENERIC_dir_mapping_list[i]->map_name_strlen = strlen(GENERIC_dir_mapping_list[i]->map_name);
	};
	return 0;
}

char *GENERIC_FileName_dir_mapping(
	char *in_string,
	char *work_string,
	int *status)
{
	int match_found,j,i;
	int start_search;
	char *temp_string;
	*status = 0;

	if (in_string == '\0')
		return in_string;

	for (i=0;i<GENERIC_dir_mapping_count;i++)
		if ((strlen(in_string) >= GENERIC_dir_mapping_list[i]->dir_name_strlen) &&
		    (!strncasecmp(in_string,GENERIC_dir_mapping_list[i]->dir_name,
		   	          GENERIC_dir_mapping_list[i]->dir_name_strlen)))
		{
			strcpy(work_string,GENERIC_dir_mapping_list[i]->map_name);
			strcat(work_string,&in_string[GENERIC_dir_mapping_list[i]->dir_name_strlen]);
			*status = 1;
			return work_string;
		}
	
	return in_string;

}

void GENERIC_VMS_FileName_init()
{
  /* first time initialization routine */
  long int controls_bits;
  char *controls_logical;

  memset(GENERIC_filename_controls, -1, sizeof (GENERIC_filename_controls));

  if (controls_logical = getenv(VMS_PREFIX "FILENAME_CONTROLS"))
		{
			int i,j;
			j=0;
			controls_bits = atol(controls_logical);
		        for (i=0;i<32;i++)
				if (controls_bits & (1<<i))
				   GENERIC_filename_controls[j++]=i;
		}
  else
                {/* default to all functions enabled mph 4/26/98*/
                        int i;
                        for (i=0;i<32;i++)
                                GENERIC_filename_controls[i]=i;
                }

}

char * GENERIC_VMS_Filename_parser(char *in_string, 
                                   char *out_string, 
                                   int string_type_indicator,
                                   int *status)
{

	int i;
	int work_status;
	char *active_string;
	char *new_string;
	char *work_string;
	char temp_string[TEMP_STRING_SIZE];
	int  switch_work_and_temp=0;

	/* copy the in_string and put it in the out_string*/
	active_string=out_string;
	strcpy(active_string,in_string);
	work_string = temp_string;

	if (GENERIC_K_STRING_TYPE_UNKNOWN==string_type_indicator)
	{/* check last char, if / it is a directory*/
		int temp_len = strlen(active_string);
		if ((active_string[temp_len-1]=='/') ||
		    (active_string[temp_len-1]==']') ||
		    (active_string[temp_len-1]=='>'))
			string_type_indicator=GENERIC_K_STRING_TYPE_DIRECTORY;
	}
	work_status =0;
	*status = 0;
	if (!GENERIC_fs_init_done)
		{
		GENERIC_VMS_FileName_init();
		GENERIC_fs_init_done=1;
		}

	i=0;
	while(GENERIC_filename_controls[i]!= -1)
		{
		/* cpu saving, we have two work areas,
			the output string and our tempstring,
		 keep  switching the roles to save recopy the string each
		pass*/
		if (switch_work_and_temp)
			work_string = out_string;
		else
			work_string = temp_string;

		switch (GENERIC_filename_controls[i])
			{
		case PARSE_UNIX_AND_VMS :
				active_string = 
				   GENERIC_FileName_Parse_U_V(
					active_string,
                                  	work_string,
					&work_status);
				if (work_status==1) /* do it again
							there might be  
							[-][something]  */
					i--;
                              	break;
		case VALID_CHARACTERS_FILENAME :
				active_string = 
				   GENERIC_FileName_valid_chars(
					active_string,
                                  	work_string,
					&work_status);
		case DIR_IN_DIRECTORY_NAME :
				active_string =
				   GENERIC_FileName_dir_in_dir(
					active_string,
                                  	work_string,
					&work_status);
                              	break;
		case HIDDEN_FILENAME_UNDER :
				active_string =
				   GENERIC_FileName_Hidden_under(
					active_string,
                                  	work_string,
					&work_status);
                              	break;
		case HIDDEN_FILENAME_DOT :
				active_string =
				   GENERIC_FileName_Hidden_file(
					active_string,
                                  	work_string,
					&work_status);
                              	break;
		case DOT_IN_DIRNAME_UNDER:
				active_string =
				   GENERIC_FileName_dot_dir_u(
					active_string,
                                  	work_string,
					&work_status,
					string_type_indicator);
                              	break;
		case DOT_IN_DIRNAME_REMOVE:
				active_string =
				   GENERIC_FileName_dot_dir_r(
					active_string,
                                  	work_string,
					&work_status);
                              	break;
		case MULTI_DOT_IN_FILENAME_LAST :
				active_string = 
				   GENERIC_FileName_Multi_dot_l(
					active_string,
					work_string,
					&work_status);
				break;
		case MULTI_DOT_IN_FILENAME_FIRST :
				active_string = 
				   GENERIC_FileName_Multi_dot_f(
					active_string,
					work_string,
					&work_status);
				break;

		case FILENAME_LARGER_39_TRUNC:
				active_string = 
				   GENERIC_FileName_gt_39_trunc(
					active_string,
					work_string,
					&work_status);
				break;
		case FILENAME_LARGER_39_MOVE:
				active_string = 
				   GENERIC_FileName_gt_39_move(
					active_string,
					work_string,
					&work_status);
				break;
		case DIR_OVER_8_LEVELS_DEEP:
				active_string = 
			       	   GENERIC_FileName_dir_mapping(
					active_string,
					work_string,
					&work_status);
				break;

		default:
			break;
			}

		if (work_status==1)
			if (!switch_work_and_temp)
				switch_work_and_temp =1;
			else 
				switch_work_and_temp = 0;
		work_status = 0;
		i++;
		}

	/* did in_string have a '/' at the end, if so put it back on
           sometimes basename removes the '/'*/
        if (in_string && in_string[0] && 
		(in_string[strlen(in_string)-1]=='/') &&
		active_string[0] &&
		(active_string[strlen(active_string)-1]!='/'))
		strcat(active_string,"/");
	if (switch_work_and_temp)
		/* we're in the temp directory move it to output*/
		{
		strcpy(out_string,active_string);
		return(out_string);
		}
	return active_string;

}
#ifdef TESTING_MAIN
int main(int argc,char **argv)
{

	char *temp_string;
	char out_filename[TEMP_STRING_SIZE];
	int return_status;
	int i;

	GENERIC_dir_mapping_init();

	for (i=0;i<10;i++) /* you need to loop a few times to find the problem*/
	temp_string =  GENERIC_VMS_Filename_parser(
		argv[1], out_filename, GENERIC_K_STRING_TYPE_UNKNOWN, &return_status);
	printf("string returned %s, status %d\n",
			temp_string,return_status);
  
}
#endif


/* unix2vms.c starts here */



/*
 *************************************************************************
 *                                                                       *
 * Copyright 2000 Compaq Computer Corporation                            *
 *                                                                       *
 * COMPAQ Registered in U.S. Patent and Trademark Office.                *
 *                                                                       *
 *************************************************************************
 * IMPORTANT: Carefully read the License Terms below before              *
 * proceeding.  By use of these materials you agree to these terms.      *
 * If you do not agree to these terms, you may not use this software or  *
 * the accompanying documentation.                                       *
 *************************************************************************
 * LICENSE TERMS                                                         *
 * 1. GRANT                                                              *
 * Compaq Computer Corporation ("COMPAQ") grants you the right to use,   *
 * modify, and distribute the following source code (the "Software")     *
 * on any number of computers. You may use the Software as part of       *
 * creating a software program or product intended for commercial or     *
 * non-commercial distribution in machine-readable source code, binary,  *
 * or executable formats. You may distribute the Software as             *
 * machine-readable source code provided this license is not removed     *
 * from the Software and any modifications are conspicuously indicated.  *
 * 2. COPYRIGHT                                                          *
 * The Software is owned by COMPAQ and its suppliers and is protected by *
 * copyright laws and international treaties.  Your use of the Software  *
 * and associated documentation is subject to the applicable copyright   *
 * laws and the express rights and restrictions of these terms.          *
 * 3. RESTRICTIONS                                                       *
 * You may not remove any copyright, trademark, or other proprietary     *
 * notices from the Software or the associated  documentation.           *
 * You are responsible for compliance with all applicable export or      *
 * re-export control laws and regulations if you export the Software.    *
 * This license is governed by and is to be construed under the laws     *
 * of the State of Texas.                                                *
 *                                                                       *
 * DISCLAIMER OF WARRANTY AND LIABILITY                                  *
 * Compaq shall not be liable for technical or editorial errors or       *
 * omissions contained herein. The information contained herein is       *
 * subject to change without notice.                                     *
 *                                                                       *
 * THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND.       *
 * THE ENTIRE RISK ARISING OUT OF THE USE OF THIS SOFTWARE REMAINS WITH  *
 * RECIPIENT.  IN NO EVENT SHALL COMPAQ BE LIABLE FOR ANY DIRECT,        *
 * CONSEQUENTIAL, INCIDENTAL, SPECIAL, PUNITIVE OR OTHER DAMAGES         *
 * WHATSOEVER (INCLUDING WITHOUT LIMITATION DAMAGES FOR LOSS OF BUSINESS *
 * PROFITS, BUSINESS INTERRUPTION, OR LOSS OF BUSINESS INFORMATION),     *
 * EVEN IF COMPAQ HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES    *
 * AND WHETHER IN AN ACTION OF CONTRACT OR TORT INCLUDING NEGLIGENCE.    *
 *                                                                       *
 * If you have any questions concerning this license, please contact:    *
 * Compaq Computer Corporation, Software Business Practices, ZKO1-2/D22, *
 * 110 Spit Brook Road, Nashua, NH. 03062-2698.                          *
 *                                                                       *
 *************************************************************************
 */

/*
** Routine to determine if a filespec is VMS format or not
*/
int GENERIC_IS_VALID_VMS_FILE_SPEC(char *file_name)
{

struct NAM nam;
struct FAB fab;
int stat;

    fab = cc$rms_fab;
    nam = cc$rms_nam;
    fab.fab$l_fna = file_name;
    fab.fab$b_fns = strlen( file_name );
    nam.nam$b_nop = NAM$M_SYNCHK; /* This means we do not want to I/O
                                     to verify file exist. only parse name */
    fab.fab$l_nam = & nam;

    stat = sys$parse( &fab );
    if ( ! (stat&1 ) )
	return 0;
    else
	return 1;

}


/*
**  Action routine which creates a dynamic memory copy of the callback name
*/
static char *action_buffer = NULL;

static int action_routine (char *string, int unused) {

   if (action_buffer) 
   {
       strcpy(action_buffer,string);
   } 
   else 
   {
       action_buffer = strdup(string);
   }

   return (0);
}


char * GENERIC_EXTERNAL_NAME (const char *unixPtr, char *optBuffer) 
{
   char local_unix [NAM$C_MAXRSS+1];
   size_t offset = strlen(unixPtr) - 1;

   if( GENERIC_IS_VALID_VMS_FILE_SPEC( (char *)unixPtr ) )
   {  
       if( optBuffer )
       {
           strcpy( optBuffer, unixPtr ); /* hopefully user given enough space*/
           return optBuffer;
       }
       else
       {
           action_buffer = (char *)malloc( strlen(unixPtr) + 1 );
           strcpy( action_buffer, unixPtr );
           return action_buffer;
       }
   }
   else
   {
       strcpy (local_unix, unixPtr);
       while (local_unix[offset] == '/') local_unix[offset--] = '\0';

       if (optBuffer) 
       {
          action_buffer = optBuffer;
          optBuffer[0]='\0'; /* in case no translations */
       } 
       else 
       {
       action_buffer = NULL;
       }

       decc$to_vms(local_unix, action_routine, -1, 1);
       /* decc$to_vms(local_unix, action_routine, 0, 1); */
      return (action_buffer);
   }
}


char * GENERIC_EXTERNAL_DIR_NAME (const char *unixPtr, char *optBuffer) 
{

   char local_unix [NAM$C_MAXRSS+1];
   size_t offset = strlen(unixPtr) - 1;

   if( GENERIC_IS_VALID_VMS_FILE_SPEC( (char *)unixPtr ) )
   {  
       if( optBuffer )
       {
           strcpy( optBuffer, unixPtr ); /* hopefully user given enough space*/
           return optBuffer;
       }
       else
       {
           action_buffer = (char *)malloc( strlen(unixPtr) + 1 );
           strcpy( action_buffer, unixPtr );
           return action_buffer;
       }
   }
   else
   {
      strcpy (local_unix, unixPtr);
      while (local_unix[offset] == '/') local_unix[offset--] = '\0';

      if (optBuffer) 
      {
          action_buffer = optBuffer;
      } 
      else 
      {
          action_buffer = NULL;
      }

      decc$to_vms(local_unix, action_routine, -1, 2);
      /* decc$to_vms(local_unix, action_routine, 0, 2); */
      return (action_buffer);
   }
}


