#include <stdio.h>
#include "server.h"

#ifdef USE_RESOURCES

#ifdef USE_OS2_RESOURCES
#define INCL_WINWINDOWMGR
#define INCL_DOSMODULEMGR
#define INCL_DOSERRORS
#include <os2.h>
#else  /* not USE_OS2_RESOURCES */
#include "wtypes.h"
#include "winbase.h"
#include "winuser.h"
#endif /* not USE_OS2_RESOURCES */

#include "mergeres.h"

#define MSG_BUF_SIZE 100

#ifndef MODULE_NAME
#define MODULE_NAME "exa.dll"
#endif  /* no MODULE_NAME */

#ifdef USE_OS2_RESOURCES

void printmsg(char *msg) {
ULONG mrc = NO_ERROR; /* DosLoadModule return code */
CHAR LoadError[100];  /* possible error message */
HMODULE hnd = NULLHANDLE; /* exa.dll handle if using dll */
HAB hab = 0;          /* anchor block handle (seemingly not used) */
LONG len = 0;         /* length of string from resource STRINGTABLE */
char buf[MSG_BUF_SIZE+1]; /* string from STRINGTABLE */

    if ( msg ) {
        printf("%s", msg);
    } else {
#ifdef USE_SHARED_LIBRARY
      mrc = DosLoadModule( LoadError, sizeof(LoadError), MODULE_NAME,
                           &hnd );
#endif
      if (mrc == NO_ERROR) {
        len = WinLoadString(hab,
                            hnd,        /* module ID */
                            STR_ODEMSG, /* STRING id */
                            MSG_BUF_SIZE,
                            buf         /* string is put here */
                            );
      }
      if (len > 0) {
          printf("%s", buf);
      } else {
#ifdef USE_SHARED_LIBRARY
        if (mrc != NO_ERROR)
          printf("DosLoadModule LoadError code=%d: %s\n", mrc, LoadError);
#endif
        printf("%s", DEFAULTMSG);
      }
    }
}

#else /* not USE_OS2_RESOURCES */

void printmsg(char *msg) {
int rc;
int lastError = 0;
HINSTANCE hnd = 0;
char buf[MSG_BUF_SIZE+1];

    if ( msg ) {
        printf("%s", msg);
    } else {
      hnd = LoadLibrary(MODULE_NAME);
      rc = LoadString(hnd,        /* module ID */
                      STR_ODEMSG, /* STRING id */
                      buf,        /* string is put here */
                      MSG_BUF_SIZE
                      );
      if (0 < rc) {
          printf("%s", buf);
      } else {
        printf("LoadString code %d, GetLastError %d\n", rc, lastError);
        printf("%s", DEFAULTMSG);
      }
    }
}

#endif /* not USE_OS2_RESOURCES */

#else /* not use resources */

void printmsg(char *msg) {

    if ( msg ) {
        printf("%s", msg);
    } else {
        printf("%s", DEFAULTMSG);
    }
}

#endif /* not use resources */

