/**
 * Make
 *
**/
#ifndef _ODE_BIN_MAKE_MAKE_HPP_
#define _ODE_BIN_MAKE_MAKE_HPP_

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/io/path.hpp"
#include "lib/io/ui.hpp"
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"

#include "bin/make/dir.hpp"

class PassNode;
class TargetNode;
class MkCmdLine;

#ifdef __WEBMAKE__
#ifdef __WEBDAV__
class WebResource : public Hashable<WebResource>
{
 public:
   String href;
   String time_format;
   int file_size;
   time_t file_time;
   boolean    extracted;
   WebResource(String name, int size,time_t time,String timeformat)
   {
      href=name;
      file_size=size;
      file_time=time;
      time_format=timeformat;
      extracted=false;

   }
   ~WebResource(){};
   boolean operator==(const WebResource &in) const
   {
      if(href==in.href && file_size==in.file_size &&
         file_time==in.file_time)
         return true;
      else
         return false;
   }
   String getLastModified(){return this->time_format;}
   int compareTimeStamp(time_t time_stamp)
   {
      if(time_stamp>this->file_time)
         return 1;
      else if (time_stamp==this->file_time)
         return 0;
      else
         return -1;

   }
   String getName() {return href;}
};
#endif // __WEBDAV__
#endif // __WEBMAKE__

class Make : public Tool
{
  public:
    static Make *mk;     // Only 1 Make instance.
    PassNode *mainpass;
    TargetNode *maintgt;
    PassNode *mkconfpass;
    Dir sysSearchPath;
    Dir dirSearchPath;
    const   char      **args;
    const   char      **envs;
    static  fstream   *bf;  // BOM file

#ifdef __WEBMAKE__
#ifdef __WEBDAV__
    static Hashtable< String,WebResource* >  WebDavFilesTable;
#endif // __WEBDAV__
    static StringArray  *extractFiles;
#endif // __WEBMAKE__

    // private constructor and destructor
    // only allow 1 instance from the Make class
    Make( const char **argv, const char **envp );
    ~Make();

    void readUserMakefile( const String &usermakefile, PassNode &curpass,
                           Dir &searchpath );
    void determineMainTargets();
    void findOutOfDate( );
    void determineTop( const String &srcbase );
    void readMakeconf() ;
    void determineObjDir( String &obj_dir, PassNode *mkconfpass );
    void initSysSearchPath();
   /**
    *  Add all relative paths specified to the dirSearchPath.
    **/
    StringArray *expandSysDirs( const StringArray &arr, StringArray *buf = 0);

  /**
   * This is the public interface to the build command/class.
   * @param args The arguments to pass to mk
   * @return Returns 0 on success, and non-zero on failure.
   */
    static int classMain( const char **argv, const char **envp );
#ifdef __WEBMAKE__
#ifdef __WEBDAV__
    static void webDav_FileTable();
#endif // __WEBDAV__
#endif // __WEBMAKE__

    // access functions
    inline static void determineSubTargets(PassNode &pass, Dir &searchpath,
                   Dir &syssearchpath);
    static void quit( const int errcode );
    static void quit( const String &msg, const int errcode );
    static void error( const String &msg, boolean ignored = false );
    inline static void warning( const String &msg );
           static int  getDirDepth(const String &dir);
    virtual void handleInterrupt(); // Handle ctrl-c, ctrl-Break interrupt
                                    // stop all running threads and
                                    // remove all being updated targets

                  void runInterruptTargets();

    void printUsage() const;

    inline void setNotParallel( boolean isnotparallel = true );
    inline void setSpecialPasses( boolean isspecialpasses = true );
    inline boolean getNotParallel();
    inline boolean getSpecialPasses();
    int run()
    {
      return (0);
    };


  private:
    static volatile int interrupt_cnt;
    static  MkCmdLine  *cmdline;
            String      cur_dir;
            String      make_top;
            String      make_dir;
            String      obj_dir;

            // Used to indicate the use of .NOTPARALLEL
            boolean     not_parallel;

            // Used to indicate the use of .PASSES
            boolean     special_passes;

    static void determineDirSearchPath( const String &mkconf_subdir, 
                                        StringArray &searchpath_arr );
};

// access functions
inline void Make::determineSubTargets(PassNode &pass, Dir &searchpath,
                                Dir &syssearchpath)
{
  mk->readUserMakefile( "", pass, searchpath );
}

inline void Make::warning( const String &msg )
{
#ifdef __WEBMAKE__
  Interface::printWarning( "webmake: " + msg );
#else
  Interface::printWarning( "mk: " + msg );
#endif // __WEBMAKE__
}

inline void Make::setNotParallel( boolean isnotparallel )
{
  not_parallel = isnotparallel;
}

inline void Make::setSpecialPasses( boolean isspecialpasses )
{
  special_passes = isspecialpasses;
}

inline boolean Make::getNotParallel()
{
  return (not_parallel);
}

inline boolean Make::getSpecialPasses()
{
  return (special_passes);
}

#endif //_ODE_BIN_MAKE_MAKE_HPP_


