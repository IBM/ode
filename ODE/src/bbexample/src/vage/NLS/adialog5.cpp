/******************************************************************************
* .FILE:         adialog5.cpp                                                 *
*                                                                             *
* .DESCRIPTION:  Hello World Sample Program Version 5: Dialog Implementation  *
*                                                                             *
* .CLASSES:      ATextDialog                                                  *
*                ADialogCommandHandler                                        *
*                AHelloWorld                                                  *
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
#include <istring.hpp>
#include <ireslib.hpp>
#include <ihelp.hpp>
#include "ahellow5.h"
#include "adialog5.hpp"

/**************************************************************************
* Class ATextDialog :: ATextDialog - Constructor for the text dialog      *
*   window                                                                *
*                                                                         *
* Construct the dialog as a frame window owned by the window passed.      *
*   Size and position of the window are calculated as an offset           *
*     from the left bottom position of the owner window.                  *
*   The style is set to match that of an OS/2 PM dialog template plus     *
*     settings for system menu and title bar.                             *
* Create the multicell canvas with this dialog frame as owner.            *
* Place the dialog's push buttons on a set canvas so that they are        *
*   evenly spaced.                                                        *
* Place the dialog controls for the static text and the entry field       *
*   along with the set canvas into a multicell canvas.                    *
* Construct the dialog command handler passing a pointer to this frame.   *
* Save the text string reference passed into the constructor.             *
**************************************************************************/
ATextDialog :: ATextDialog(IString & textString, IWindow * ownerWnd)
             : IFrameWindow(IResourceId(WND_TEXTDIALOG)
                  ,IWindow::desktopWindow()
                  ,ownerWnd
                  ,IRectangle(29,50,313,290)
                     .moveBy(ownerWnd->rect().bottomLeft())
                  ,IWindow::synchPaint
                    |IWindow::clipSiblings
                    |IWindow::saveBits
                    |dialogBackground
                    |dialogBorder
                    |systemMenu
                    |titleBar)
               ,clientCanvas(WND_MCCANVAS,this,this)
               ,buttons(WND_STCANVAS, &clientCanvas, &clientCanvas)
               ,statText(DID_STATIC,&clientCanvas,&clientCanvas)
               ,textField( DID_ENTRY,&clientCanvas,&clientCanvas)
               ,pushButton1( DID_OK,&buttons,&buttons)
               ,pushButton2(DID_CANCEL,&buttons,&buttons)
               ,dialogCommandHandler(this)
               ,saveText(textString)
{
/*------------------------------------------------------------------------|
|Set the entry field text to the string passed in on the ATextDialog      |
|  constructor and set the style.                                         |
|Set the entry field prompt and push button text strings from strings     |
|  in the resource file.                                                  |
|Set the push buttons and set canvas styles.  The buttons set canvas      |
|  pack type is set to expanded to size both push buttons to the same     |
|  size.                                                                  |
-------------------------------------------------------------------------*/
  textField.setText(saveText);
  textField.disableAutoScroll().enableMargin().enableTabStop();

  statText.setText(DID_STATIC);

  pushButton1.enableDefault().setText(IResourceId(DID_OK)).enableTabStop();
  pushButton2.setText(IResourceId(DID_CANCEL));
  buttons.setPackType(ISetCanvas::expanded).setMargin(ISize());

/*------------------------------------------------------------------------|
|  Position the dialog controls in the multicell canvas.                  |
-------------------------------------------------------------------------*/
  clientCanvas.addToCell(&statText , 2, 4);
  clientCanvas.addToCell(&textField, 2, 7);
  clientCanvas.addToCell(&buttons,   2,15);

/*------------------------------------------------------------------------|
|  Set the multicell canvas as the ATextDialog client window.             |
|  Have the command handler start handling events for the frame window.   |
|  Set the focus to the entry field.                                      |
|------------------------------------------------------------------------*/
  setClient( &clientCanvas );
  dialogCommandHandler.handleEventsFor(this);
  textField.setFocus();

/*------------------------ Use Owner's Help Window -----------------------|
|  IHelpWindow::setAssociatedWindow is called to associate the dialog with|
|    its owner's help window so that the help window is positioned        |
|    relative to this window, and so that this window is correctly        |
|    activated when the user dismisses the help window.                   |
|------------------------------------------------------------------------*/
  IHelpWindow* help = IHelpWindow::helpWindow( ownerWnd );
  if ( help )
    help->setAssociatedWindow(this);

} /* end ATextDialog :: ATextDialog(...) */

/**************************************************************************
* Class ATextDialog :: ~ATextDialog - Destructor for the dialog frame     *
*   window                                                                *
**************************************************************************/
ATextDialog :: ~ATextDialog()
{
  dialogCommandHandler.stopHandlingEventsFor(this);
} /* end ATextDialog :: ~ATextDialog() */

/**************************************************************************
* Class ATextDialog :: setTextFromEntryField - Update the reference       *
*   string with the text from the entry field.                            *
**************************************************************************/
ATextDialog &
  ATextDialog::setTextFromEntryField()
{
  saveText = textField.text();
  return (*this);                       //Return a reference to the frame
} /* end AHelloWindow :: setTextFromEntryField */

/**************************************************************************
* Class ADialogCommandHandler :: ADialogCommandHandler--Constructs the    *
*   command handler for the dialog box.                                   *
*                                                                         *
*  Store the pointer to the ATextDialog that events are handled for.      *
**************************************************************************/
ADialogCommandHandler :: ADialogCommandHandler(ATextDialog *dialogFrame)
  :frame(dialogFrame)
{
} /* end ADialogCommandHandler :: ADialogCommandHandler(...) */

/**************************************************************************
* Class ADialogCommandHandler :: command -- Handle menu commands for      *
*   dialog window                                                         *
**************************************************************************/
IBase::Boolean
  ADialogCommandHandler :: command(ICommandEvent & cmdEvent)
{
  Boolean eventProcessed(true);         //Assume event will be processed

/*------------------------------------------------------------------------|
|  Depending on the command event ID,                                     |
|    optionally update the Hello World text;                              |
|    then dismiss the text dialog passing the event ID as the result.     |
|------------------------------------------------------------------------*/
  switch (cmdEvent.commandId()) {
    case DID_OK:
      frame->setTextFromEntryField();
      frame->dismiss(DID_OK);
      break;
    case DID_CANCEL:
      frame->dismiss(DID_CANCEL);
      break;
    default:
/*------------------------------------------------------------------------|
| The event was not processed                                             |
-------------------------------------------------------------------------*/
      eventProcessed=false;
  } /* end switch */

  return(eventProcessed);
} /* end ADialogCommandHandler :: command(...) */

