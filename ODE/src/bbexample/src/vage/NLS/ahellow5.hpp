/******************************************************************************
* .FILE:         ahellow5.hpp                                                 *
*                                                                             *
* .DESCRIPTION:  Hello World Sample Program Version 5: Class Header           *
*                                                                             *
* .CLASSES:      AHelloWindow                                                 *
*                ACommandHandler                                              *
*                ASelectHandler                                               *
*                AHelpHandler                                                 *
*                AEarthWindow                                                 *
*                ATextDialogWindow                                            *
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
#ifndef _AHELLOW5_
#define _AHELLOW5_

#include <iframe.hpp>
#include <istattxt.hpp>
#include <iinfoa.hpp>
#include <imenubar.hpp>
#include <icmdhdr.hpp>
#include <isetcv.hpp>
#include <ipushbut.hpp>
#include <isplitcv.hpp>
#include <ilistbox.hpp>
#include <iselhdr.hpp>
#include <ihelphdr.hpp>
#include <ihelp.hpp>

#include "aearthw5.hpp"

//Forward declarations for other classes:
class AHelloWindow;

/**************************************************************************
* Class ACommandHandler -- Handler for main frame window                  *                                                *
**************************************************************************/
class ACommandHandler : public ICommandHandler
{
  public:

/*------------------------------ Constructor -----------------------------|
| Construct the object with:                                              |
| 1) A pointer to the main frame window                                   |
-------------------------------------------------------------------------*/
    ACommandHandler(AHelloWindow *helloFrame);

/*------------------------------ Destructor ------------------------------|
| Destruct the object with:                                               |
| 1) No parameters                                                        |
-------------------------------------------------------------------------*/
    virtual
     ~ACommandHandler() { };

  protected:
/*------------------------ Override Command Function ---------------------|
| The command function is called to handle application command events.    |
-------------------------------------------------------------------------*/
    virtual Boolean
      command(ICommandEvent& cmdEvent);

  private:
    AHelloWindow
     *frame;
};

/**************************************************************************
* Class ASelectHandler -- Handles list box selections for the main window *
**************************************************************************/
class ASelectHandler : public ISelectHandler
{
  public:
/*------------------------------ Constructor -----------------------------|
| Construct the object with:                                              |
| 1) A pointer to the main frame window                                   |
-------------------------------------------------------------------------*/
    ASelectHandler(AHelloWindow *helloFrame);

/*------------------------------ Destructor ------------------------------|
| Destruct the object with:                                               |
| 1) No parameters                                                        |
-------------------------------------------------------------------------*/
    virtual
     ~ASelectHandler() { }

  protected:
/*------------------------ Override Selected Function --------------------|
| The selected() function is called to handle list box selection events.  |
|------------------------------------------------------------------------*/
    virtual Boolean
      selected(IControlEvent& ctlEvent);

  private:
    AHelloWindow
     *frame;
};

/**************************************************************************
* Clas AHelpHandler -- Provides help for keys                             *
**************************************************************************/
class AHelpHandler : public IHelpHandler
{
  public:

/*------------------------------ Destructor ------------------------------|
| Destruct the object with:                                               |
| 1) No parameters                                                        |
-------------------------------------------------------------------------*/
    virtual ~AHelpHandler() { }

  protected:
/*----------------------- Override keysHelpId Function -------------------|
| The keysHelpId() function is called to set the event result to the      |
|   ID within the Hello World help libraries for Keys Help.               |
|------------------------------------------------------------------------*/
    virtual Boolean
      keysHelpId(IEvent& evt);
};

/**************************************************************************
* Class AHelloWindow -- Main Frame Window                                 *
**************************************************************************/
class AHelloWindow : public IFrameWindow
{
  public:
/*------------------------ Hello Text Alignment --------------------------|
| The following enumeration type is used to specify the alignment of      |
| text in the hello static text window.                                   |
|------------------------------------------------------------------------*/
    enum Alignment
    {
      left, center, right
    };

/*------------------------------ Constructor -----------------------------|
| Construct the object with:                                              |
| 1) the window id                                                        |
-------------------------------------------------------------------------*/
    AHelloWindow(unsigned long windowId);

/*------------------------------ Destructor ------------------------------|
| Destruct the object with:                                               |
| 1) No parameters                                                        |
-------------------------------------------------------------------------*/
    virtual
     ~AHelloWindow();

/*---------------------- Hello Static Text Functions ---------------------|
| These functions are used to change the hello static text window.        |
|   setTextAlignment - Align the static text horizontally.  The text is   |
|           always centered vertically by design.                         |
|   editText - Use a modal dialog window to edit the text in the static   |
|           text window.                                                  |
|   setTextFromListBox -  Set the text to the first selected item in the  |
|           list box.                                                     |
|------------------------------------------------------------------------*/
    virtual AHelloWindow
     &setTextAlignment(const Alignment alignment),
     &editText(),
     &setTextFromListBox();

  private:
    IMenuBar
      menuBar;
    ISplitCanvas
      clientWindow,
      helloCanvas;
    IStaticText
      statusLine,
      hello;
    AEarthWindow
      earthWindow;
    IListBox
      listBox;
    ISetCanvas
      buttons;
    IPushButton
      leftButton,
      centerButton,
      rightButton,
      helpButton;
    IInfoArea
      infoArea;
    ACommandHandler
      commandHandler;
    ASelectHandler
      selectHandler;
    IHelpWindow
      helpWindow;
    AHelpHandler
      helpHandler;

/*------------------------------ Operators -------------------------------|
| Operators defined for this class:                                       |
|  =  -- Assignment operator                                              |
-------------------------------------------------------------------------*/
    AHelloWindow
     &operator=(const AHelloWindow&);
};
#endif
