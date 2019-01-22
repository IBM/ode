:userdoc.
:docprof toc=123456.
:title.C++ Hello World Help
:h1.C++ Hello World - Application Help
:p.This file contains the help for the C++ Hello World Application.
:h2 res=100.C++ Hello World - Main Window Help
:i1.main window help
:p.This is the help panel for the main window.
The main window contains the following areas:
:ul.
:li.The title bar icon, which provides access to the system menu
:li.The window title, which displays the title of the window
:li.The menu bar, which allows the user to select specific actions
:li.A status line, which contains the current alignment
:li.A client area, which is divided into three areas:
:ul.
:li.The first area contains the static text for "Hello, World!!!".
:li.The second area is a graphic control that shows a graphic of the Earth
from space with stars.
:li.The third area contains a list box that allows the user to change the
"Hello, World!!!" text string.
:eul.
:li.Alignment push buttons, which change the alignment, and a help push
button, which is used to request help
:li.An information area, which helps the user understand the current options
of the program, including the menu bar choices
:eul.
:h2 res=102.C++ Hello World - List Box Help
:p.This is the help panel for the list box window.
Selecting any item in the list box changes the "Hello, World!!!" text.
The code that handles the list box can be found in ahellow5.cpp.
:h2 res=110.C++ Hello World - Edit Menu Help
:p.This is the help panel for the Edit menu.
:p.
This submenu (MI_EDIT) can be found under the following statement in the
resource file (ahellow5.rc):
:xmp.
    SUBMENU "~Edit", MI_EDIT                //Edit submenu
:exmp.
:p.
This help panel (id=110) was linked to the menu item (MI_EDIT)
by the following statement in the resource file (ahellow5.rc):
:xmp.
    HELPSUBITEM MI_EDIT, 110            //Edit menu item help ID
:exmp.
:h3 res=111.C++ Hello World - Alignment Menu Help
:i1 id=align.alignment help
:p.This is the help panel for the Alignment menu item.
:p.
This submenu (MI_ALIGNMENT) can be found under the following statement in the
resource file (ahellow5.rc):
:xmp.
    SUBMENU "~Alignment", MI_ALIGNMENT      //Alignment submenu
:exmp.
:p.
This help panel (id=111) was linked to the menu item (MI_ALIGNMENT)
by the following statement in the resource file (ahellow5.rc):
:xmp.
    HELPSUBITEM MI_ALIGNMENT, 111       //Alignment menu item help ID
:exmp.
:h4 res=112.C++ Hello World - Left Command Help
:i2 refid=align.left alignment
:p.This is the help panel for the Left alignment command.
Selecting the Left menu item or Left push button sets the "Hello, World!!!"
text to be left aligned.
:p.
This menu item (MI_LEFT) was created by the following statement in the
resource file (ahellow5.rc):
:xmp.
    MENUITEM "~Left\tF7",   MI_LEFT     //Left menu item - F7 key
:exmp.
:p.
The code that handles this menu item can be found in ahellow5.cpp under the
following case statement:
:xmp.
    case MI_LEFT:                    
:exmp.
:p.
This help panel (id=112) was linked to the menu item (MI_LEFT)
by the following statement in the resource file (ahellow5.rc):
:xmp.
    HELPSUBITEM MI_LEFT, 112            //Left command help ID
:exmp.
:h4 res=113.C++ Hello World - Center Command Help
:i2 refid=align.center alignment
:p.This is the help panel for the center alignment command.
Selecting the Center menu item or Center push button sets the
"Hello, World!!!" text to be center aligned.
:p.
This menu item (MI_CENTER) was created by the following statement in the
resource file (ahellow5.rc):
:xmp.
    MENUITEM "~Center\tF8", MI_CENTER   //Center menu item - F8 key
:exmp.
:p.
The code that handles this menu item can be found in ahellow5.cpp under the
following case statement:
:xmp.
    case MI_CENTER:                     
:exmp.
:p.
This help panel (id=113) was linked to the menu item (MI_CENTER)
by the following statement in the resource file (ahellow5.rc):
:xmp.
    HELPSUBITEM MI_CENTER, 113          //Center command help ID
:exmp.
:h4 res=114.C++ Hello World - Right Command Help
:i2 refid=align.right alignment
:p.This is the help panel for the right alignment command.
Selecting the Right menu item or Right push button sets the
"Hello, World!!!" text to be right aligned.
:p.
This menu item (MI_RIGHT) was created by the following statement in the
resource file (ahellow5.rc):
:xmp.
    MENUITEM "~Right\tF9",  MI_RIGHT    //Right menu item - F9 key
:exmp.
:p.
The code that handles this menu item can be found in ahellow5.cpp under the
following case statement:
:xmp.
    case MI_RIGHT:    
:exmp.
:p.
This help panel (id=114) was linked to the menu item (MI_RIGHT)
by the following statement in the resource file (ahellow5.rc):
:xmp.
    HELPSUBITEM MI_RIGHT, 114           //Right command help ID
:exmp.
:h3 res=199.C++ Hello World - Text... Menu Help
:p.This is the help panel for the Text... menu item.
:p.
This menu item (MI_TEXT) was created by the following statement in the
resource file (ahellow5.rc):
:xmp.
    MENUITEM "~Text...", MI_TEXT            //Text dialog menu item
:exmp.
:p.
The code that handles this menu item can be found in ahellow5.cpp under the
following case statement:
:xmp.
    case MI_TEXT:                      
:exmp.
:p.
This help panel (id=199) was linked to the menu item (MI_TEXT)
by the following statement in the resource file (ahellow5.rc):
:xmp.
    HELPSUBITEM MI_TEXT, 199            //Text command help ID    
:exmp.
:h2 res=200.C++ Hello World - Dialog Window Help
:p.This is the help panel for the text dialog.
:h3 res=201.C++ Hello World - Dialog Entry Field Help
:p.This is the help panel for the entry field in the text dialog.
:p.
The entry field is defined as textField in adialog5.hpp and
is initialized and handled by code found in adialog5.cpp.
:p.
This help panel (id=201) was linked to this entry field (DID_ENTRY)
by the following statement in the resource file (ahellow5.rc):
:xmp.
    HELPSUBITEM DID_ENTRY, 201          //Entry field help ID         
:exmp.
:h3 res=202.C++ Hello World - Dialog OK Button Help
:p.This is the help panel for the OK push button in the text dialog.
Selecting this push button closes the dialog.
Any changes made to the "Hello, World!!!" text are shown in the main
window.
:p.
The OK push button is defined as pushButton1 in adialog5.hpp and
is initialized and handled by code found in adialog5.cpp.
:p.
This help panel (id=202) was linked to this push button (DID_OK)
by the following statement in the resource file (ahellow5.rc):
:xmp.
    HELPSUBITEM DID_OK, 202             //OK command help ID          
:exmp.
:h3 res=203.C++ Hello World - Dialog Cancel Button Help
:p.This is the text for the Cancel push button in the text dialog.
Selecting this button closes the dialog without changing the
"Hello, World!!!" text.
:p.
The Cancel push button is defined as pushButton2 in adialog5.hpp and
is initialized and handled by code found in adialog5.cpp.
:p.
This help panel (id=203) was linked to this push button (DID_CANCEL)
by the following statement in the resource file (ahellow5.rc):
:xmp.
    HELPSUBITEM DID_CANCEL, 203         //Cancel command help ID      
:exmp.
:h2 res=1000.C++ Hello World - Keys Help Panel
:i1.Keys help panel
:p.This is the keys help panel.
:p.The following is a list of system-provided keys:
:dl compact tsize=10.
:dt.Alt-F4
:dd.Close window
:dt.Alt-F7
:dd.Move window
:dt.Alt-F8
:dd.Size window
:dt.Alt-F9
:dd.Minimize window
:dt.Alt-F10
:dd.Maximize window
:edl.
:p.The following is a list of application-provided keys:
:dl compact tsize=10.
:dt.F7
:dd.Left alignment
:dt.F8
:dd.Center alignment
:dt.F9
:dd.Right alignment
:edl.
:euserdoc.
