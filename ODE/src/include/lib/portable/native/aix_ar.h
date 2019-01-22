/* IBM_PROLOG_BEGIN_TAG                                                   */
/* This is an automatically generated prolog.                             */
/*                                                                        */
/* bos430 src/bos/usr/include/ar.h 1.10                                   */
/*                                                                        */
/* Licensed Materials - Property of IBM                                   */
/*                                                                        */
/* (C) COPYRIGHT International Business Machines Corp. 1989,1997          */
/* All Rights Reserved                                                    */
/*                                                                        */
/* US Government Users Restricted Rights - Use, duplication or            */
/* disclosure restricted by GSA ADP Schedule Contract with IBM Corp.      */
/*                                                                        */
/* IBM_PROLOG_END_TAG                                                     */
/* @(#)25	1.10  src/bos/usr/include/ar.h, cmdar, bos430, 9737A_430 2/16/97 17:13:55 */
#ifndef _H_AR
#define _H_AR
/*
 * COMPONENT_NAME: CMDAR
 *
 * STRUCTURES ar_hdr, fl_hdr, ar_hdr_big, fl_hdr_big
 *
 * ORIGINS: 27, 3
 *
 */

/*		AIX INDEXED ARCHIVE FORMAT
*
*	ARCHIVE File Organization:
*	 _____________________________________________ 
*       |__________FIXED HEADER "fl_hdr"______________|
*  +--- |					      |
*  |    |__________ARCHIVE_FILE_MEMBER_1______________|
*  +--> |					      |
*       |	Archive Member Header "ar_hdr"        |
*  +--- |.............................................| <--+
*  |    |		Member Contents		      |    |
*  |    |_____________________________________________|    |
*  |    |________ARCHIVE_FILE_MEMBER_2________________|    |
*  +--> |					      | ---+
*       |	Archive Member Header "ar_hdr"        |
*  +--- |.............................................| <--+
*  |    |		Member Contents		      |    |
*  |    |_____________________________________________|    |
*  |    |	.		.		.     |    |
*  .    |	.		.		.     |    .
*  .    |	.		.		.     |    .
*  .    |_____________________________________________|    .
*  |    |________ARCHIVE_FILE_MEMBER_n-1______________|    |
*  +--> |					      | ---+
*       |	Archive Member Header "ar_hdr"        |
*  +--- |.............................................| <--+
*  |    |	Member Contents 		      |    |
*  |    |       (Member Table, always present)        |    |
*  |    |_____________________________________________|    |
*  |    |_____________________________________________|    |
*  |    |________ARCHIVE_FILE_MEMBER_n________________|    |
*  |    |					      |    |
*  +--> |	Archive Member Header "ar_hdr"        | ---+
*       |.............................................|
*       |	Member Contents 		      |
*       |     32-bit Global Symbol Table (if present) |
*       |     64-bit Global Symbol Table (if present) |
*       |_____________________________________________|


  The archive file format has a small variant and a big variant.
  An archive from AIX 4.2 and earlier uses the small variant. The
  big variant must be used if the archive contains XCOFF64 object files.

  Define __AR_SMALL__ to get the structure definitions for the small variant.
  Define __AR_BIG__ to get the structure definitions for the big variant.
  If neither __AR_SMALL__ nor __AR_BIG__ is defined, the small variant is used.
  If both __AR_SMALL__ and __AR_BIG__ are defined, structures for both variants
  are defined, and the following macro and structure names should be used:

   =====================================================
   | Small variant name         | Big variant name     |
   =====================================================
   | struct fl_hdr              | struct fl_hdr_big    |
   | FL_HDR                     | FL_HDR_BIG           |
   | struct ar_hdr              | struct ar_hdr_big    |
   | FL_HSZ                     | FL_HSZ_BIG           |
   | AR_HSZ                     | AR_HSZ_BIG           |
   =====================================================
*/

#ifdef __cplusplus
extern "C" {
#endif

#define SAIAMAG	8
#define AIAFMAG	"`\n"
#define AIAMAG		"<aiaff>\n"
#define AIAMAGBIG	"<bigaf>\n"

#if defined(__AR_SMALL__) && defined (__AR_BIG__)
#define _A_(name) name ## _BIG
#define _a_(name) name ## _big
#define FL_HSZ_BIG sizeof(FL_HDR_BIG)
#define AR_HSZ_BIG sizeof(AR_HDR_BIG)
#else
#define _A_(name) name
#define _a_(name) name
#endif

#if defined(__AR_SMALL__) || ! defined (__AR_BIG__)
typedef struct fl_hdr	/* archive fixed length header - printable ascii */
{
	char	fl_magic[SAIAMAG];	/* Archive file magic string */
	char	fl_memoff[12];		/* Offset to member table */
	char	fl_gstoff[12];		/* Offset to global symbol table */
	char	fl_fstmoff[12];		/* Offset to first archive member */
	char	fl_lstmoff[12];		/* Offset to last archive member */
	char	fl_freeoff[12];		/* Offset to first mem on free list */
} FL_HDR;

typedef struct ar_hdr	/* archive file member header - printable ascii */
{
	char	ar_size[12];	/* file member size - decimal */
	char	ar_nxtmem[12];	/* pointer to next member -  decimal */
	char	ar_prvmem[12];	/* pointer to previous member -  decimal */
	char	ar_date[12];	/* file member date - decimal */
	char	ar_uid[12];	/* file member user id - decimal */
	char	ar_gid[12];	/* file member group id - decimal */
	char	ar_mode[12];	/* file member mode - octal */
	char	ar_namlen[4];	/* file member name length - decimal */
	union
	{
		char	ar_name[2];	/* variable length member name */
		char	ar_fmag[2];	/* AIAFMAG - string to end header */
	}	_ar_name;		/*      and variable length name */
} AR_HDR;
#endif /* defined(__AR_SMALL__) || ! defined (__AR_BIG__) */

#ifdef __AR_BIG__

typedef struct _a_(fl_hdr) /* archive fixed length header - printable ascii */
{
	char	fl_magic[SAIAMAG];	/* Archive file magic string */
	char	fl_memoff[20];		/* Offset to member table */
	char	fl_gstoff[20];		/* Offset to 32-bit global sym table */
	char	fl_gst64off[20];	/* Offset to 64-bit global sym table */
	char	fl_fstmoff[20];		/* Offset to first archive member */
	char	fl_lstmoff[20];		/* Offset to last archive member */
	char	fl_freeoff[20];		/* Offset to first mem on free list */
} _A_(FL_HDR);

typedef struct _a_(ar_hdr) /* archive file member header - printable ascii */
{
	char	ar_size[20];	/* file member size - decimal */
	char	ar_nxtmem[20];	/* pointer to next member -  decimal */
	char	ar_prvmem[20];	/* pointer to previous member -  decimal */
	char	ar_date[12];	/* file member date - decimal */
	char	ar_uid[12];	/* file member user id - decimal */
	char	ar_gid[12];	/* file member group id - decimal */
	char	ar_mode[12];	/* file member mode - octal */
	char	ar_namlen[4];	/* file member name length - decimal */
	union
	{
		char	ar_name[2];	/* variable length member name */
		char	ar_fmag[2];	/* AIAFMAG - string to end header */
	}	_ar_name;		/*      and variable length name */
} _A_(AR_HDR);

#endif /* __AR_BIG__ */

#define FL_HSZ sizeof(FL_HDR)
#define AR_HSZ sizeof(AR_HDR)

/*
*	Note: 	'ar_namlen' contains the length of the member name which
*		may be up to 255 chars.  The character string containing
*		the name begins at '_ar_name.ar_name'.  The terminating
*		string AIAFMAG, is only cosmetic. File member contents begin
*		at the first even byte boundary past 'header position + 
*		sizeof(struct ar_hdr) + ar_namlen',  and continue for
*		'ar_size' bytes.
*/

#ifdef __cplusplus
}
#endif

#endif /* _H_AR */
