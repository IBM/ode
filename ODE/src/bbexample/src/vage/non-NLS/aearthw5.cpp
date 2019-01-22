/******************************************************************************
* .FILE:         aearthw5.cpp                                                 *
*                                                                             *
* .DESCRIPTION:  Hello World Sample Program Version 5: Class Implementation   *
*                                                                             *
* .CLASSES:      AEarthWindow                                                 *
*                AEarthWindowResizeHandler                                    *
*                Star                                                         *
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
#include "aearthw5.hpp"
/**************************************************************************
* Class Star::Star - Constructor for drawing a star                        *
**************************************************************************/
Star::Star(const IPoint &pt)
   : IGLine(pt,pt)
{
}

/**************************************************************************
* Class Star::setPoint - Sets the point where a star is located            *
**************************************************************************/
Star
 &Star::setPoint(const IPoint &pt)
{
  setStartingPoint(pt);
  setEndingPoint(IPoint(pt.x()+1,pt.y()+1));
  return *this;
}

/**************************************************************************
* AEarthWindow :: AEarthWindow - Constructor for the Earth window         *
**************************************************************************/
AEarthWindow :: AEarthWindow(unsigned long windowId,
                             IWindow * parowWindow,
                             const IRectangle& rect)
               :IDrawingCanvas(windowId, parowWindow, parowWindow, rect)
                ,spaceColor(IColor::black)
                ,globeColor(IColor::cyan)
                ,starColor(IColor::white)
                ,earthWindowResizeHandler(this)
{
/*------------------------------------------------------------------------|
|  Performs the initial draw of the stars and the world.  The first       |
|  elements in both starlist and earthArc are set to null so that the     |
|  objects will be created.  paintStars() and paintWorld will then        |
|  initialize the objects.                                                |
-------------------------------------------------------------------------*/
    starlist[0]=0;
    earthArc[0]=0;
    paintWorld();

/*------------------------------------------------------------------------|
|  Adds all of the objects to the glist and then attaches it to the       |
|    IDrawingCanvas.                                                      |
-------------------------------------------------------------------------*/
    graphList.addAsLast(space)
             .addAsLast(starGraphicList)
             .addAsLast(earthGraphicList);
    setGraphicList(&graphList);

/*------------------------------------------------------------------------|
|   The ResizeHandler is attached to the AEarthWindow object to begin     |
|   handling resize events.  Each time the IDrawingCanvas window needs to |
|   be resized, AEarthWindowResizeHandler::resizeWindow function will be  |
|   called.  The first painting occurs because of IWindow::show.          |
-------------------------------------------------------------------------*/

   earthWindowResizeHandler.handleEventsFor(this);
   show();
} /* end AEarthWindow :: AEarthWindow(...) */

/**************************************************************************
* AEarthWindow :: ~AEarthWindow - Destructor for Earth window             *
**************************************************************************/
AEarthWindow :: ~AEarthWindow()
{
/*------------------------------------------------------------------------|
| Tell earthWindowResizeHandler to stop handling events for AEarthWindow. |
-------------------------------------------------------------------------*/
  earthWindowResizeHandler.stopHandlingEventsFor(this);

/*------------------------------------------------------------------------|
| Delete the graphic objects in starlist and earthArc.                    |
-------------------------------------------------------------------------*/
  for (int i=0;i<stars ;i++ )
  {
     delete starlist[i];
  }
  for (i=0;i<=atmosphereLayers ; i++ )
  {
     delete earthArc[i];
  }
} /* end AEarthWindow :: ~AEarthWindow() */


/**************************************************************************
* AEarthWindow :: paintWorld - paint a view of Earth from space           *
**************************************************************************/
IBase::Boolean AEarthWindow :: paintWorld()
{
  Boolean
    worldPainted = false;
/*------------------------------------------------------------------------|
| Construct the graphic bundles that will be attached to each layer of    |
| atmosphere or planet.  This will only be perormed on the first call of  |
| this function.                                                          |
|------------------------------------------------------------------------*/
  IGraphicBundle
    arcbundle[4];
  if (!earthArc[0])
  {
    arcbundle[0]
     .setFillPattern(IGraphicBundle::filled)
     .setPenColor(globeColor)
     .setFillColor(globeColor);
    arcbundle[1]
     .setFillPattern(IGraphicBundle::filled)
     .setPenColor(IColor::blue)
     .setFillColor(IColor::blue);
    arcbundle[2]
     .setFillPattern(IGraphicBundle::filled)
     .setPenColor(IColor::darkCyan)
     .setFillColor(IColor::darkCyan);
    arcbundle[3]
     .setFillPattern(IGraphicBundle::filled)
     .setPenColor(IColor::darkBlue)
     .setFillColor(IColor::darkBlue);
  }


/*------------------- Construct Squares for Arc Casings ------------------|
| Construct four squares such that the leftCenter, topCenter, and         |
|   rightCenter points describe the arcs to be drawn for the Earth and    |
|   its atmosphere.  The squares are constructed from IRectangle objects  |
|   positioned initially at the center of the presentation space          |
|   rectangle and then moved 1/2 the square's width to the left and a     |
|   full square's height plus an arbitrary offset down.                   |
|------------------------------------------------------------------------*/
  const IRectangle
    psRect(rect());
  const int
    arcs=4;
  const float
    arcDropFactor[arcs] = {1.0/8,  1.0/16,  1.0/32, 0},
    arcSizeFactor[arcs] = {9.0/4, 21.0/8,  45.0/16, 3};
  const long
    psHeight=psRect.height();
  long
    arcDrop,
    arcDiameter;
  IPair
    arcOffset;
  IRectangle
    arcSquare[arcs];
  int i;

  for (i=0;i<arcs;i++ )
  {
    arcDrop = psHeight*arcDropFactor[i];
    arcDiameter = psHeight*arcSizeFactor[i];
    arcOffset = IPair(-1*arcDiameter/2,-1*arcDiameter-arcDrop);
    arcSquare[i] = IRectangle(psRect.center(),
                     ISize(arcDiameter, arcDiameter));
    arcSquare[i].moveBy(arcOffset);
  }

/*------------------------------- Color Space ----------------------------|
| Set the background color to space, which is IColor::black.              |
|------------------------------------------------------------------------*/
  IGraphicBundle spaceBundle;
  spaceBundle.setPenColor(spaceColor);
  spaceBundle.setFillColor(spaceColor);
  spaceBundle.setFillPattern(IGraphicBundle::filled);
  space.setEnclosingRect(rect());
  space.setGraphicBundle(spaceBundle);


/*-------------------- Draw the Earth and Atmosphere ---------------------|
| Draw the earth and the number of layers of atmosphere specified in      |
|   atmosphereLayers.  arcSquare[0] contains the arc dimension for the    |
|   earth and the other arcSquare objects specify each atmosphere layer.  |
| The arcs are drawn by drawing an ellipse and allowing the program to    |
|   clip the bottom half of the ellipse.  This section of code sets       |
|   the enclosing rectangle that defines the ellipse.  The graphic        |
|   bundles are also attached to set the pen color, fill color, and       |
|   the fill pattern for each ellipse.                                    |
| The ellipses are only instantiated on the first call to this function.  |
|------------------------------------------------------------------------*/
  if (earthArc[0])
     for(i=atmosphereLayers;i>=0;i--)
       earthArc[i]->setEnclosingRect(arcSquare[i]);
  else
  {
     earthGraphicList.removeAll();
     for(i=atmosphereLayers;i>=0;i--)
     {
        earthArc[i]=new IGEllipse(arcSquare[i]);
        earthArc[i]->setGraphicBundle(arcbundle[i%3+1]);
        earthGraphicList.addAsLast(*earthArc[i]);
     }
     earthArc[0]->setGraphicBundle(arcbundle[0]);
  }
/*--------------------------- Paint the Stars ----------------------------|
| Call the AEarthWindow function for painting the stars.                  |
|------------------------------------------------------------------------*/
  worldPainted=paintStars();

  return (worldPainted);

} /* end AEarthWindow :: paintWorld(..) */

/**************************************************************************
* AEarthWindow :: paintStars - paint the stars in the Earth window        *
**************************************************************************/
IBase::Boolean AEarthWindow :: paintStars()
{
  Boolean starsPainted = false;

/*------------------- Get Presentation Space Objects ---------------------|
| Get the presentation space handle (called "graphics context" in AIX)    |
|   and the rectangle of the area that needs to be painted.               |
|------------------------------------------------------------------------*/
  const IRectangle
    psRect(rect());

/*------------------- Construct Stars from IPoints -----------------------|
| Construct a star array where each star is a point within the            |
|   presentation space rectangle.  Each point is computed as a fraction   |
|   of the psRect size offset from the origin of the psRect.              |
|------------------------------------------------------------------------*/
  const int
    stars=13;
  const IPair
    psOrigin(psRect.bottomLeft()),
    psSize(psRect.size());
  int
    i, j;

  IPoint
    star[stars];
  star[0] =IPoint(psOrigin+psSize.scaledBy(0.98,0.43));
  star[1] =IPoint(psOrigin+psSize.scaledBy(0.70,0.69));
  star[2] =IPoint(psOrigin+psSize.scaledBy(0.20,0.50));
  star[3] =IPoint(psOrigin+psSize.scaledBy(0.80,0.63));
  star[4] =IPoint(psOrigin+psSize.scaledBy(0.05,0.41));
  star[5] =IPoint(psOrigin+psSize.scaledBy(0.50,0.69));
  star[6] =IPoint(psOrigin+psSize.scaledBy(0.60,0.94));
  star[7] =IPoint(psOrigin+psSize.scaledBy(0.10,0.87));
  star[8] =IPoint(psOrigin+psSize.scaledBy(0.40,0.81));
  star[9] =IPoint(psOrigin+psSize.scaledBy(0.25,0.69));
  star[10]=IPoint(psOrigin+psSize.scaledBy(0.75,0.63));
  star[11]=IPoint(psOrigin+psSize.scaledBy(0.30,0.87));
  star[12]=IPoint(psOrigin+psSize.scaledBy(0.95,0.87));


/*------------------------------------------------------------------------|
| Draw the stars by setting the starBundle to white and by setting the    |
| position of that the star will be drawn at.  On the first call to this  |
| function, each star will instantiated while setting the location and    |
| the graphic bundle will be set to starBundle.                           |
|------------------------------------------------------------------------*/
  IGraphicBundle starBundle;
  starBundle.setPenColor(starColor);

  if (starlist[0])
  {
    for (i=0;i<stars;i++)
      starlist[i]->setPoint(star[i]);
  }
  else
  {
    starGraphicList.removeAll();
    for (int i=0;i<stars ;i++ )
    {
      starlist[i]=new Star(star[i]);
      starlist[i]->setGraphicBundle(starBundle);
      starGraphicList.addAsLast(*starlist[i]);
    }
  }

  starsPainted = true;
  return (starsPainted);

} /* end AEarthWindow :: paintStars(...) */

/******************************************************************************
* Class AEarthWIndowResizeHandler :: AEarthWindowResizeHandler - Constructor  *
*   for the timecard's pie chart                                              *
*                                                                             *
* Define yourself as an IResizeHandler                                        *
* Store a pointer to the picture                                              *
******************************************************************************/
AEarthWindowResizeHandler::AEarthWindowResizeHandler( AEarthWindow * aew )
     :IResizeHandler()
     , earthWindow ( aew )
{
}


/******************************************************************************
* Class AEarthWindowResizeHandler :: ~AEarthWindowResizeHandler - Destructor  *
******************************************************************************/
AEarthWindowResizeHandler::~AEarthWindowResizeHandler( )
{
}


/******************************************************************************
* Class AEarthWindowResizeHandler :: windowResize() - Called when a resize    *
*   event occurs for the picture.                                             *
******************************************************************************/
IBase::Boolean AEarthWindowResizeHandler::windowResize( IResizeEvent& event )
{
/*-----------------------------------------------------------------------------
| Call our own resizing function and repaint the drawing canvas.              |
-----------------------------------------------------------------------------*/
   earthWindow->paintWorld();
   earthWindow->refresh();
   return false;
}

