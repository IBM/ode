//***********************************************************************
//* Body
//*
//***********************************************************************
#ifndef _ODE_LIB_IO_BODY_HPP_
#define _ODE_LIB_IO_BODY_HPP_


#include <fstream>


#include "lib/portable/vector.hpp"
#include "lib/string/string.hpp"




class Body
{

  public:

    Body() {};
    Body(const String& str  );

    ~Body();

    inline void clear()
    {
      content.removeAllElements();
    }

    // Inserts a new String to the content vector
    void addElement(const String& line);

    // Prints the Body to the standard output.
    void print();

    // Writes a Body to a stream. 
    void write(fstream* fileptr );


  private:
    // Holds a Vector of Strings.
    Vector<String> content;
  
    // Don't want to copy this, as it might be too big. 
    Body( const Body& body) {;}
      
};

#endif //_ODE_LIB_IO_BODY_HPP_
