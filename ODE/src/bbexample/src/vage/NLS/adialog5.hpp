/******************************************************************************
* .FILE:         adialog5.hpp                                                 *
*                                                                             *
* .DESCRIPTION:  Hello World Sample Program Version 4: ATextDialog Class      *
*                                                                             *
* .CLASSES:      ADialogCommandHandler                                        *
*                ATextDialog                                                  *
*                AHellowWindow                                                *
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
#ifndef _ADIALOG5_
#define _ADIALOG5_

#include <iframe.hpp>
#include <icmdhdr.hpp>
#include <istring.hpp>
#include <imcelcv.hpp>
#include <istattxt.hpp>
#include <ientryfd.hpp>
#include <isetcv.hpp>
#include <ipushbut.hpp>

//Forward declarations for other classes:
class AHelloWindow;
class ATextDialog;

/**************************************************************************
* Class:   ADialogCommandHandler--Command handler for ADialogWindow class *
**************************************************************************/
class ADialogCommandHandler : public ICommandHandler
{
  public:
/*---------------------------- Constructor -------------------------------|
| Constructs an object with:                                              |
| 1) A pointer to the TextDialog                                          |
-------------------------------------------------------------------------*/
    ADialogCommandHandler(ATextDialog *dialogFrame);

/*----------------------------- Destructor -------------------------------|
| Destructs an object with:                                               |
| 1) No parameters                                                        |
-------------------------------------------------------------------------*/
    virtual
     ~ADialogCommandHandler() { }


  protected:
/*------------------------ Override Command Function ---------------------|
| The command function is called to handle application command events.    |
|------------------------------------------------------------------------*/
    virtual Boolean
      command(ICommandEvent& cmdEvent);

  private:
    ATextDialog
     *frame;
};

/**************************************************************************
* Class:   ATextDialog--Dialog Window class that edits  text              *
**************************************************************************/
class ATextDialog : public IFrameWindow
{
  public:
/*------------------------------ Constructor -----------------------------|
| Constructs the object with                                              |
| 1) with the editable string and parent window                           |
-------------------------------------------------------------------------*/
    ATextDialog(IString & textString,IWindow * ownerWnd);
/*------------------------------ Destructor ------------------------------|
| Destructs the object with                                               |
| 1) no parameters                                                        |
-------------------------------------------------------------------------*/
    virtual
     ~ATextDialog();

/*------------------------------------------------------------------------|
| setTextFromEntryField retrieves the text in the entry field and stores  |
|    it in saveText                                                       |
--------------------------------------------------------------------------*/
    virtual ATextDialog
     &setTextFromEntryField();

  private:
    IString
     &saveText;
    IMultiCellCanvas
      clientCanvas;
    IStaticText
      statText;
    IEntryField
      textField;
    ISetCanvas
      buttons;
    IPushButton
      pushButton1,
      pushButton2;
    ADialogCommandHandler
      dialogCommandHandler;

/*------------------------------ Operators -------------------------------|
| Operators defined for this class:                                       |
|  =  -- Assignment operator                                              |
-------------------------------------------------------------------------*/
    ATextDialog
      &operator=(const ATextDialog&);
};

#endif
