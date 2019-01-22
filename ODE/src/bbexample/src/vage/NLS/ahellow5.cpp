/******************************************************************************
* .FILE:         ahellow5.cpp                                                 *
*                                                                             *
* .DESCRIPTION:  Hello World Sample Program Version 5: Class Implementation   *
*                                                                             *
* .CLASSES:      AHelloWindow                                                 *
*                ACommandHandler                                              *
*                ASelectHandler                                               *
*                AHelpHandler                                                 *
*                AEarthWindow                                                 *
*                ATextDialog                                                  *
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
#include <ibase.hpp>
#include <iapp.hpp>
#include <ifont.hpp>
#include <istring.hpp>
#include <imsgbox.hpp>
#include <icoordsy.hpp>
#include "aearthw5.hpp"
#include "ahellow5.hpp"
#include "ahellow5.h"
#include "adialog5.hpp"

/**************************************************************************
* main  - Application entry point for Hello World Version 5.              *
*                                                                         *
* Creates a new object mainWindow of class AHelloWindow                   *
* Sets Hello World window alignment                                       *
* Sets the size of mainWindow                                             *
* Sets the window focus to mainWindow                                     *
* Displays the mainWindow                                                 *
* Starts the events processing for the application                        *
**************************************************************************/
int main()
{
  ICoordinateSystem::setApplicationOrientation(
          ICoordinateSystem::originLowerLeft );
  #ifdef USE_IPF
  IHelpWindow::setDefaultStyle( IHelpWindow::defaultStyle()
                                | IHelpWindow::ipfCompatible );
  #endif
  AHelloWindow mainWindow (WND_MAIN);
  mainWindow.setTextAlignment(AHelloWindow::left);
  mainWindow.sizeTo(ISize(400,300));
  mainWindow.setFocus();
  mainWindow.show();
  IApplication::current().run();
  return 0;
} /* end main */

/**************************************************************************
* Class AHelloWindow :: AHelloWindow - Constructor for the main window    *
*                                                                         *
* Construct the IFrameWindow using the default style plus minimizedIcon,  *
*   which gets the Icon identified in the resource file and associates it *
*   with the main window.  The accelerator style causes the accelerator   *
*   table to be loaded from the resource file.                            *
* Create a menu bar object for the main window menu bar that was loaded   *
*   from the resource file.  menuBar is used by setAlignment to           *
*   manipulate check marks on the menu items.                             *
* Create a static text object for displaying the status of the            *
*   Hello World text alignment.                                           *
* Create the clientWindow split canvas to be used as the client window    *
*   of this frame and let its orientation default to vertical.            *
* Create the helloCanvas split canvas making the clientWindow its parent  *
*   window and orient it horizontally.                                    *
* Create the hello and earthWindow static text objects as children of     *
*   the helloCanvas.  hello is placed in the topmost window of the        *
*   horizontal split canvas because it is created first.  earthWindow     *
*   is placed in the bottom window of the split canvas.                   *
* Create the listBox window making the clientWindow its parent window     *
*   and enabling it for tabbing and preventing it from being resized      *
*   due to an item being too long.  helloCanvas is placed in the          *
*   leftmost window of the vertical split canvas because it is created    *
*   first.  listBox is placed in the right window of the split canvas.    *
* Create a set canvas that will contain the push buttons and will be      *
*   added to the frame as an extension.                                   *
* Create each push button with the set canvas as parent and owner.        *
* Additionally for Help, specify help style and noPointerFocus style.     *
*   noPointerFocus prevents the mouse from changing the input focus       *
*   to the Help push button when you select it using the mouse.  This     *
*   allows you to use contextual help for the control with the input      *
*   focus rather than for the Help push button itself.                    *
* Create the Hello World information area object from IInfoArea class.    *
*   The information area is automatically added as an extension below     *
*   the client window of the frame.                                       *
* Create a command handler to process command events from menu item,      *
*   push button, and accelerator key selections.                          *
* Create a select handler to process selections made in the list box.     *
* Create an IHelpWindow object that is displayed when help is requested   *
*   from a help menu item, the help push button, or the help accelerator  *
*   key, normally F1.                                                     *
* An IHelpHandler object, helpHandler is created implicitly, see class    *
*   definition for AHelloWindow in ahellow5.hpp.                          *
**************************************************************************/

AHelloWindow :: AHelloWindow(unsigned long windowId)
  : IFrameWindow(IFrameWindow::defaultStyle() |
                 IFrameWindow::minimizedIcon  |
                 IFrameWindow::accelerator,
                 windowId)
   ,menuBar(windowId, this)
   ,clientWindow(WND_CANVAS, this, this)
   ,helloCanvas(WND_HCANVAS,
                &clientWindow,
                &clientWindow,
                IRectangle(),
                IWindow::visible |
                ISplitCanvas::horizontal)
   ,hello(WND_HELLO,
          &helloCanvas,
          &helloCanvas)
   ,statusLine(WND_STATUS, this, this)
   ,earthWindow(WND_EARTH, &helloCanvas)
   ,listBox(WND_LISTBOX,
            &clientWindow,
            &clientWindow,
            IRectangle(),
            IListBox::defaultStyle() |
            IControl::tabStop |
            IListBox::noAdjustPosition)
   ,buttons(WND_BUTTONS, this, this)
   ,leftButton(MI_LEFT, &buttons, &buttons)
   ,centerButton(MI_CENTER, &buttons, &buttons)
   ,rightButton(MI_RIGHT, &buttons, &buttons)
   ,helpButton(MI_HELP,
               &buttons,
               &buttons, IRectangle(),
               IPushButton::defaultStyle() |
               IPushButton::help |
               IButton::noPointerFocus)
   ,infoArea(this)
   ,commandHandler(this)
   ,selectHandler(this)
   ,helpWindow(HELP_TABLE,this)
{

/*------------------------------------------------------------------------|
|  Set the clientWindow split canvas as the client window for the         |
|    AHelloWorld frame.                                                   |
|  Set the HelloCanvas object to occupy 60% of the client canvas.         |
|  Set the list box object to occupy 40% of the client canvas.            |
|------------------------------------------------------------------------*/
  setClient(&clientWindow);
  clientWindow.setSplitWindowPercentage(&helloCanvas, 60);
  clientWindow.setSplitWindowPercentage(&listBox, 40);

/*------------------------------------------------------------------------|
|  Add the status line as an extension to the frame above the client      |
|    window with the height calculated from the maximum height of a       |
|    character in the current font.                                       |
|------------------------------------------------------------------------*/
  addExtension(&statusLine, IFrameWindow::aboveClient,
                 IFont(&statusLine).maxCharHeight());

/*------------------------------------------------------------------------|
|  Add the different language versions of Hello World to the list box in  |
|    ascending order.  Hello World text strings are stored in the         |
|    resource file with IDs beginning with HI_WORLD with each subsequent  |
|    ID incremented up to HI_WORLD + HI_COUNT - 1.                        |
|  Have the select handler handle selections made in the list box.        |
|    The select handler's selected() function is called to process        |
|    list box selections.                                                 |
|------------------------------------------------------------------------*/
  for (int i=0;i<HI_COUNT;i++ )
     listBox.addAscending(HI_WORLD+i);
  selectHandler.handleEventsFor(&listBox);

/*------------------------------------------------------------------------|
|  Change the size of the information area using the maximum height of    |
|    a character in the current font.                                     |
|------------------------------------------------------------------------*/
  setExtensionSize(&infoArea, IFont(&infoArea).maxCharHeight());

/*------------------------------------------------------------------------|
|  Set the values for the text controls from strings in the resource file.|
|    The infoArea inactive text is displayed when no menu item is active. |
|------------------------------------------------------------------------*/
  hello.setText(STR_HELLO);
  leftButton.setText(STR_LEFTB);
  centerButton.setText(STR_CENTERB);
  rightButton.setText(STR_RIGHTB);
  helpButton.setText(STR_HELPB);
  infoArea.setInactiveText(STR_INFO);

/*------------------------------------------------------------------------|
|  Enable the first button as a tab stop.                                 |
|  Set the canvas margins and padding between buttons to zero.            |
|  Add the set canvas that contains the push buttons to the frame window  |
|    as an extension immediately below the client window with a height    |
|    equal to the minimum height of the push buttons.  You must set the   |
|    text in the push buttons first.                                      |
|------------------------------------------------------------------------*/
  leftButton.enableTabStop();
  buttons.setMargin(ISize());
  buttons.setPad(ISize());
  addExtension(&buttons, IFrameWindow::belowClient,
                (unsigned long)buttons.minimumSize().height());

/*------------------------------------------------------------------------|
|  Have the command handler handle commands sent from the frame window.   |
|    The command handler's command() function is called to process        |
|    menu item, push button, and accelerator key selections.              |
|------------------------------------------------------------------------*/
  commandHandler.handleEventsFor(this);

/*------------------------------------------------------------------------|
|   Add the help library to the help window using addLibraries().         |
|   Set the help window title from a string in the resource file.         |
|   Begin handling help events for the frame window.                      |
|------------------------------------------------------------------------*/
   try
   {
      helpWindow.addLibraries( "ahellow5.hlp" );
      helpWindow.setTitle(STR_HTITLE);
      helpHandler.handleEventsFor(this);
   }
   catch( ... )
   {
      IMessageBox
         msgBox( this );
      msgBox.show( STR_HELP_NOT_FOUND, IMessageBox::warning );
   }

/*------------------------------------------------------------------------|
| Align the static text, set the status line, and set the check mark in   |
|   the menu bar.                                                         |
|------------------------------------------------------------------------*/
  setTextAlignment(center);

} /* end AHelloWindow :: AHelloWindow(...) */

/**************************************************************************
* Class AHelloWindow :: ~AHelloWindow - Destructor for the main window    *
*                                                                         *
* Stop handling command events for the frame.                             *
* Stop handling select events for the list box.                           *
* Stop handling help events for the frame.                                *
**************************************************************************/
AHelloWindow :: ~AHelloWindow()
{
  commandHandler.stopHandlingEventsFor(this);
  selectHandler.stopHandlingEventsFor(&listBox);
  helpHandler.stopHandlingEventsFor(this);

} /* end AHelloWindow :: ~AHelloWindow() */

/**************************************************************************
* Class AHelloWindow :: setAlignment - Align static text in client window *
**************************************************************************/
AHelloWindow &
  AHelloWindow :: setTextAlignment(const Alignment alignment)
{
/*------------------------------------------------------------------------|
|  Depending on the value passed, update the window as follows:           |
|    Set the alignment of the static text control in the client window.   |
|    Set the text of the alignment status line static text control.       |
|    Check the selected menu item; remove check marks from the other two. |
|------------------------------------------------------------------------*/
  switch(alignment)
  {
  case left:
    hello.setAlignment(IStaticText::centerLeft);
    statusLine.setText(STR_LEFT);
    menuBar.uncheckItem(MI_CENTER);
    menuBar.checkItem(MI_LEFT);
    menuBar.uncheckItem(MI_RIGHT);
    break;
  case center:
    hello.setAlignment(IStaticText::centerCenter);
    statusLine.setText(STR_CENTER);
    menuBar.checkItem(MI_CENTER);
    menuBar.uncheckItem(MI_LEFT);
    menuBar.uncheckItem(MI_RIGHT);
    break;
 case right:
    hello.setAlignment(IStaticText::centerRight);
    statusLine.setText(STR_RIGHT);
    menuBar.uncheckItem(MI_CENTER);
    menuBar.uncheckItem(MI_LEFT);
    menuBar.checkItem(MI_RIGHT);
    break;
 }
 return (*this);                        //Return a reference to the frame

} /* end AHelloWindow :: setAlignment(...) */

/**************************************************************************
* Class AHelloWindow :: editText -- Creates and shows a dialog window     *
*   for inputting text that will be used  to replace the text string      *
**************************************************************************/
AHelloWindow &
  AHelloWindow :: editText()
{
/*------------------------------------------------------------------------|
|  Store the current value of the text to be changed.                     |
|  Set the text in the information area from the dialog information       |
|    string in the resource file.                                         |
|------------------------------------------------------------------------*/
  IString textValue(hello.text());
  infoArea.setInactiveText(STR_INFODLG);

/*------------------------------------------------------------------------|
|  Create a new text dialog with textValue as the string to edit and      |
|    AHelloWindow as the owner window.                                    |
|  Show the dialog modally.  This means that the owner window cannot have |
|    the focus back until the dialog is ended.                            |
|------------------------------------------------------------------------*/
  ATextDialog textDialog(textValue,this);
  textDialog.showModally();

/*------------------------------------------------------------------------|
|  If the OK button was used to end the dialog, then the static text,     |
|    hello, is set to the textValue string.  Else, it is not changed.     |
|  Reset the information area inactive text.                              |
|------------------------------------------------------------------------*/
  if (textDialog.result() == DID_OK)
        hello.setText(textValue);
  infoArea.setInactiveText(STR_INFO);

  return (*this);                       //Return a reference to the frame
}  /* end AHelloWindow :: editText() */

/**************************************************************************
* Class AHelloWindow :: setTextFromListBox -- Public function used by     *
*   non-AHelloWindow functions to set the Hello World text from the first *
*   selected item in the AHelloWindow listBox.                            *
**************************************************************************/
AHelloWindow &
  AHelloWindow :: setTextFromListBox()
{

/*------------------------------------------------------------------------|
|  Create a cursor to the list box.  Using the default filter for a       |
|    list box cursor, selectedItems, causes the setToFirst() function     |
|    to position the cursor to the first selected item.                   |
|  Set the hello IStaticText control text value.                          |
|------------------------------------------------------------------------*/
  IListBox::Cursor lbCursor(listBox);
  lbCursor.setToFirst();
  hello.setText(listBox.elementAt(lbCursor));

  return (*this);                       //Return a reference to the frame
}  /* end AHelloWindow :: setTextFromListBox() */

/**************************************************************************
* Class ACommandHandler :: ACommandHandler - constructor for the command  *
*   handler                                                               *
*                                                                         *
* Construct the command handler from a pointer to the AHelloWindow that   *
*   events will be handled for.                                           *
**************************************************************************/
ACommandHandler :: ACommandHandler(AHelloWindow *helloFrame)
   : frame(helloFrame)
{
} /* end ACommandHandler :: ACommandHandler(...) */

/**************************************************************************
* Class ACommandHandler :: command - Handle menu and button commands      *
**************************************************************************/
IBase::Boolean
  ACommandHandler :: command(ICommandEvent & cmdEvent)
{
  Boolean eventProcessed(true);         //Assume event will be processed

/*------------------------------------------------------------------------|
|  Depending on the command event ID, call the AHelloWindow::setAlignment |
|    function with the appropriate AHelloWorld::Alignment value.          |
|  Do this except when the Text menu item is selected;                    |
|    then call the AHelloWindow::editText function for changing the       |
|    Hello World text using a dialog.                                     |
|------------------------------------------------------------------------*/
  switch (cmdEvent.commandId()) {
    case MI_CENTER:
      frame->setTextAlignment(AHelloWindow::center);
      break;
    case MI_LEFT:
      frame->setTextAlignment(AHelloWindow::left);
      break;
    case MI_RIGHT:
      frame->setTextAlignment(AHelloWindow::right);
      break;
    case MI_TEXT:
      frame->editText();
      break;
    default:
/*------------------------------------------------------------------------|
| The event was not processed                                             |
-------------------------------------------------------------------------*/
      eventProcessed=false;
  } /* end switch */

  return(eventProcessed);
} /* end ACommandHandler :: command(...) */

/**************************************************************************
* Class ASelectHandler :: ASelectHandler - constructor for the select     *
*   handler                                                               *
*                                                                         *
* Construct the select handler from a pointer to the AHelloWindow         *
*   that will be changed as a result of the selection.                    *
**************************************************************************/
ASelectHandler :: ASelectHandler(AHelloWindow *helloFrame)
   :frame(helloFrame)
{
} /* end ASelectHandler :: ASelectHandler(...) */


/**************************************************************************
* Class ASelectHandler :: selected - Handle items selected within the     *
*   list box.                                                             *
**************************************************************************/
IBase::Boolean
  ASelectHandler :: selected(IControlEvent & evt)
{

/*------------------------------------------------------------------------|
|  Call the AHelloWindow::setTextFromListBox function for the frame used  |
|    to construct this select handler.                                    |
|------------------------------------------------------------------------*/
  frame->setTextFromListBox();

  return (true);                        //Event is always processed
} /* end ASelectHandler :: selected(...) */

/**************************************************************************
* Class AHelpHandler :: keysHelpId - Handle the keys help request event   *
**************************************************************************/
IBase::Boolean
  AHelpHandler :: keysHelpId(IEvent& evt)
{
  evt.setResult(1000);                  //1000=keys help ID in
                                        //  ahellow5.ipf file

  return (true);                        //Event is always processed
} /* end AHelpHandler :: keysHelpId(...) */
