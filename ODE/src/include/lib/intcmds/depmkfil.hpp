//***********************************************************************
//* DepMkFile
//*
//***********************************************************************
#ifndef _ODE_LIB_IO_DEPMKFIL_HPP_
#define _ODE_LIB_IO_DEPMKFIL_HPP_

#include "lib/string/strarray.hpp"
#include "lib/portable/hashtabl.hpp"
#include "lib/io/path.hpp"

#include "lib/intcmds/target.hpp"




class DepMkFile
{

  private:
    typedef Hashtable< String, Target* > TargetCollection;
    TargetCollection targets;

    String         name;
    String         callerName;

  //Hold static information from depend.mk files.
    static const  String ODEDLLPORT LINE1;
    static const  String ODEDLLPORT LINE2;



  public:

    DepMkFile( const String& name, String cname );

    ~DepMkFile();


    // Reads target information from given 'depend.mk' file
    void load( const String &sandboxbase, const String &relcurdir,
               const StringArray &backeddirs );

    // Adds a new Target object to the TargetCollection
    void appendTarget( Target *target );


    // Replaces a Target* from the TargetCollection.
    void replaceTarget( Target *target );

    // Finds a Target from the TargetCollection using the key.
    Target* const* get(const String& key);

    // Writes the TargetCollection to a file with the name 'name'.
    void save();

    // Sees, if TargetCollection contains a specific key or not.
    boolean containsKey( const String& name );

    // Writes the TargetCollection to the screen.
    void print();



  private:
    void readTargets( const File& depmkpath );

};

#endif //_ODE_LIB_IO_DEPMKFIL_HPP_
