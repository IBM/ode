/**
 * StringFindable
**/

#ifndef _ODE_LIB_STRING_STRFIND_HPP_
#define _ODE_LIB_STRING_STRFIND_HPP_

class String;

/**
 * Intended for interaction between the Variable
 * class and the make command.  When the :P modifier
 * is encountered by Variable, it will call an
 * object that implements this interface to find the
 * full path to a file.
 *
 * This object also carries a value find_dirs is set by the mk -b
 * flag, but which is needed by the :P and :F modifiers.
**/
class StringFindable
{
  public:

    StringFindable(): find_dirs(false){};

    // ensure proper destruction of derived classes
    virtual ~StringFindable() {};
    
    // return true if found and append found strings to matches
    virtual boolean stringFinder( const String &str,
                                  StringArray &matches,
                                  boolean doWildCards,
                                  boolean dirs_only ) const = 0;

    inline boolean getFindDirs() const;

    inline void setFindDirs( boolean fdirs );

  private:

    boolean find_dirs;
};

inline boolean StringFindable::getFindDirs() const
{
  return (find_dirs);
}

inline void StringFindable::setFindDirs( boolean fdirs )
{
  find_dirs = fdirs;
}

#endif /* _ODE_LIB_STRING_STRFIND_HPP_ */
