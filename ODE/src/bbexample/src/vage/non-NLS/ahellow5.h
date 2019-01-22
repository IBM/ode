/******************************************************************************
* .FILE:         ahellow5.h                                                   *
*                                                                             *
* .DESCRIPTION:  Hello World Sample Program Version 5: Symbolic Definitions   *
*                                                                             *
* .COPYRIGHT:                                                                 *
*    Licensed Material - Program-Property of IBM                              *
*    (C) Copyright IBM Corp. 1992, 1996 - All Rights Reserved                 *
*                                                                             *
* .DISCLAIMER:                                                                *
*   The following [enclosed] code is sample code created by IBM               *
*   Corporation.  This sample code is not part of any standard IBM product    *
*   and is provided to you solely for the purpose of assisting you in the     *
*   development of your applications.  The code is provided 'AS IS',          *
*   without warranty of any kind.  IBM shall not be liable for any damages    *
*   arising out of your use of the sample code, even if they have been        *
*   advised of the possibility of such damages.                               *
*                                                                             *
* .NOTE: WE RECOMMEND USING A FIXED SPACE FONT TO LOOK AT THE SOURCE          *
*                                                                             *
******************************************************************************/
#ifndef AHELLOWINDOW_H
#define AHELLOWINDOW_H

//**************************************************************************
// window ids - used by IWindow constructors (eg IStaticText, AHelloWindow)*
//**************************************************************************
#define WND_MAIN         0x1000         //Main window window ID

#define WND_HELLO        0x1010         //Hello World window ID
#define WND_INFO         0x1012         //Information area window ID
#define WND_STATUS       0x1011         //Status line window ID
#define WND_TEXTDIALOG   0x1013         //Text dialog window ID
#define WND_EARTH        0x1014         //Earth window window ID
#define WND_CANVAS       0x8008         //Hello World client window ID
#define WND_MCCANVAS     0x8008         //Text dialog client window ID
#define WND_STCANVAS     0x1015         //Text dialog set canvas window ID
#define WND_BUTTONS      0x1021         //Button set canvas window ID
#define WND_HCANVAS      0x1040         //Hello canvas window ID
#define WND_LISTBOX      0x1050         //List box window ID

//**************************************************************************
// String IDs - used to relate resources to IStaticText and ITitle         *
//**************************************************************************
#define STR_HELLO        0x1200         //Hello World string ID
#define STR_INFO         0x1220         //Inactive text string ID
#define STR_INFODLG      0x1221         //Dialog inactive text string ID
#define STR_CENTER       0x1230         //Center alignment status string ID
#define STR_LEFT         0x1231         //Left alignment status string ID
#define STR_RIGHT        0x1232         //Right alignment status string ID
#define STR_CENTERB      0x1240         //Center button string ID
#define STR_LEFTB        0x1241         //Left button string ID
#define STR_RIGHTB       0x1242         //Right button string ID
#define STR_HELPB        0x1243         //Help button string ID
#define STR_HTITLE       0x1250         //Help window title string ID
#define STR_HELP_NOT_FOUND 0x1251       //Help not found string ID

//**************************************************************************
// Menu IDs - used to relate command ID to menu items, buttons, and keys   *
//**************************************************************************
#define MI_ALIGNMENT     0x1500         //Alignment menu item command ID
#define MI_CENTER        0x1501         //Center menu item command ID
#define MI_LEFT          0x1502         //Left menu item command ID
#define MI_RIGHT         0x1503         //Right menu item command ID
#define MI_EDIT          0x1504         //Edit menu item command ID
#define MI_TEXT          0x1505         //Text menu item command ID
#define MI_HELP          0x1510         //Help menu item command ID

//**************************************************************************
// Hello IDs - used for different language versions of Hello World text    *
//**************************************************************************
#define HI_WORLD         0x1700         //First Hello World text ID
#define HI_COUNT         8              //Number of Hello World text strings

//**************************************************************************
// Dialog IDs - window IDs used in ATextDialog class                       *
//**************************************************************************
#ifndef DID_OK                          //If not already defined by OS/2,
#define DID_OK           0x0001         //OK button command ID
#endif                                  //
#ifndef DID_CANCEL                      //If not already defined by OS/2
#define DID_CANCEL       0x0002         //Cancel button command ID
#endif                                  //
#define DID_ENTRY        0x1603         //Dialog entry field window ID
#define DID_STATIC       0x1604         //Dialog static text window ID

//**************************************************************************
// Help IDs - used to relate resources to IHelp class                      *
//**************************************************************************
#define HELP_TABLE       0x1800         //Help table ID
#define SUBTABLE_MAIN    0x1801         //Help subtable for main window
#define SUBTABLE_DIALOG  0x1802         //Help subtable for dialog window
#endif
