/**
 * ConfigFile
 *
**/
#define _ODE_LIB_IO_CFGF_CPP_
#include "lib/io/cfgf.hpp"
#include "lib/string/strcon.hpp"

#define LINE_ARRAY_SIZE 10
#define CONTINUATION_CHAR '\\'
#define COMMENT_CHAR '#'

#ifdef __WEBMAKE__
#include "lib/io/ui.hpp"
#include "lib/util/condeval.hpp"
#include "lib/exceptn/parseexc.hpp"
#include "lib/string/setvars.hpp"
#include "lib/string/strcon.hpp"
const String ConfigFile::DOT_IFDEF     = ".ifdef";
const String ConfigFile::DOT_IFNDEF     = ".ifndef";
const String ConfigFile::DOT_IF         = ".if";
const String ConfigFile::DOT_ELSE       = ".else";
const String ConfigFile::DOT_ELIFDEF    = ".elifdef";
const String ConfigFile::DOT_ELIFNDEF   = ".elifndef";
const String ConfigFile::DOT_ELIF       = ".elif";
const String ConfigFile::DOT_ENDIF      = ".endif";
#endif // __WEBMAKE__
const char ConfigFile::MODE_READONLY = 'r';
const char ConfigFile::MODE_OVERWRITE = 'w';
const char ConfigFile::MODE_APPEND = 'a';


ConfigFile::~ConfigFile()
{
  if (file_info->copy_count <= 0)
  {
    this->close();
    delete file_info;
  }
  else
  {
    --(file_info->copy_count);
  }
}

/**
 *
**/
void ConfigFile::open( char mode ) //throws IOException, FileNotFoundException
{
  file_info->lastline = file_info->nextline = 0;

  switch (mode)
  {
    case MODE_APPEND:
    case MODE_OVERWRITE:
      if (!Path::exists( pathdir ))
        Path::createPath( pathdir );
      file_info->fdw = Path::openFileWriter( pathname,
          (mode == MODE_APPEND), false );
      file_info->current_mode = mode;
      break;
    case MODE_READONLY: // default
    default:
      file_info->fdr = Path::openFileReader( pathname );
      file_info->current_mode = MODE_READONLY;
      file_info->nextline = 1;
      break;
  }
}


/**
 * Close the file.  This can be done to ensure the
 * buffers are flushed before terminating a program,
 * or just to be polite.
**/
void ConfigFile::close()
{
  if (file_info->fdr != 0)
    Path::closeFileReader( file_info->fdr );
  if (file_info->fdw != 0)
    Path::closeFileWriter( file_info->fdw );
  file_info->fdr = 0;
  file_info->fdw = 0;
}


/**
 *
**/
boolean ConfigFile::reopen( int mode )
{
  this->close();
  try
  {
    this->open( mode );
  }
  catch (...)
  {
    return ( false );
  }
  return ( true );
}


/**
 * Write a line to the config file in the same mode
 * as was previously used (if the last access to the
 * file was with getLine(), append mode is used).
 *
 * @param line The string to output.  A newline will be
 * added to this string.
 * @return True on success, false on failure.
**/
boolean ConfigFile::putLine( const String &line )
{
  switch (file_info->current_mode)
  {
    case MODE_APPEND:
    case MODE_OVERWRITE:
      return (putLineForMode( line, file_info->current_mode ));
    default: // anything else that's not a write mode
      return (putLineForMode( line, MODE_APPEND ));
  }
}

/**
 *
**/
boolean ConfigFile::putLineForMode( const String &line, char mode )
{
  if ((file_info->fdw == 0 || mode != file_info->current_mode) &&
      reopen( mode ) == false)
    return (false);

// Yet another Monterey SDE kludge due to inlining bugs.  Ugh!
#ifdef AIX_IA64
  *(file_info->fdw) << line << endl;
#else
  if (!Path::putLine( *(file_info->fdw), line ))
    return (false);
#endif

  if (auto_flush)
    flush();

  return (true);
}

/**
 * Get a line (up to but not including the newline) of
 * text from the config file.  If the last action was
 * a putLine(), this will start reading from the
 * beginning of the file.  If the last action was another
 * getLine(), this will continue where it left off.  The
 * line will be pre-parsed to the following extent: (1)
 * the line may be trimmed of leading and trailing whitespace
 * (with String.trim()), (2) lines starting with a comment
 * character (#) will be skipped, and (3) lines ending with
 * a continuation character (\) will be placed in separate
 * array entries (that character will be stripped).
 *
 * @param trim If true, all lines will be trimmed of leading
 * and trailing whitespace.  If false, no trimming will occur.
 * @param backslash_escape IGNORED
 * @return The string(s) read from the file (does not include
 * the newline).  If EOF was reached, 0 is returned.  If the file
 * could not be opened, no elements will be added to the array.
**/
String *ConfigFile::getLine( boolean trim, boolean backslash_escape,
          String *buf, boolean ignore_comment, boolean replCont )
{
  static String   str;
         boolean  added_lines = false;
         String  *result = 0;

  if (file_info->fdr == 0 && reopen( MODE_READONLY ) == false)
    return (0);

  result = (buf == 0) ? new String() : buf;

  file_info->lastline = file_info->nextline;

  str     = StringConstants::EMPTY_STRING;
  *result = StringConstants::EMPTY_STRING;

  while (Path::readLine( *(file_info->fdr), &str ))
  {
    ++file_info->nextline;

    // If we know the string is empty, no need to process any further
    if (str.length() == 0)
    {
      return (result);
    }
    else if (ignore_comment && ConfigFile::isCommentLine( str ))
    {
      if (!added_lines)
        ++file_info->lastline;
    }
    else
    {
      if (!ConfigFile::cleanLine( str, trim, replCont ))
      {
        *result += str;
        return (result);
      }

      *result += str;
      added_lines = true;
    }
  }

  if (!added_lines)
  {
    if (buf != result)
      delete result;
    return (0);
  }

  return (result);
}

#ifdef __WEBMAKE__
String *ConfigFile::getLine( CondEvaluator *condeval,boolean trim, boolean backslash_escape, String  *buf, boolean ignore_comment)
{
  static String   str;
         boolean  added_lines = false;
         String  *result = 0;
  //------------------------------------------------------
  //add this for controlling parsing the ifdef block
  boolean parseskip=false;
  boolean skipUntilENDIF=false;
  //------------------------------------------------------

  if (file_info->fdr == 0 && reopen( MODE_READONLY ) == false)
    return (0);

  result = (buf == 0) ? new String() : buf;

  file_info->lastline = file_info->nextline;

  str     = StringConstants::EMPTY_STRING;
  *result = StringConstants::EMPTY_STRING;

  while (Path::readLine( *(file_info->fdr), &str ))
  {
    ++file_info->nextline;
    /*------------------------------------------------------------+
    | add this part for converting nmake directives to ode style  |
    +-------------------------------------------------------------*/
    if(str.startsWith("!IFDEF"))
       str=str.replace("!IFDEF",".ifdef");
    else if(str.startsWith("!IFNDEF"))
       str=str.replace("!IFNDEF",".ifndef");
    else if(str.startsWith("!IF"))
       str=str.replace("!IF",".if");
    else if(str.startsWith("!ELSEIFNDEF"))
       str=str.replace("!ELSEIFNDEF",".elifndef");
    else if(str.startsWith("!ELSE IFNDEF"))
       str=str.replace("!ELSE IFNDEF",".elifndef");
    else if(str.startsWith("!ELSEIFDEF"))
       str=str.replace("!ELSEIFDEF",".elifdef");
    else if(str.startsWith("!ELIFDEF"))
       str=str.replace("!ELIFDEF",".elifdef");
    else if(str.startsWith("!ELSE IFDEF"))
       str=str.replace("!ELSE IFDEF",".elifdef");
    else if(str.startsWith("!ELSEIF"))
       str=str.replace("!ELSEIF",".elif");
    else if(str.startsWith("!ELSE IF"))
       str=str.replace("!ELSE IF",".elif");
    else if(str.startsWith("!ELSE"))
       str=str.replace("!ELSE",".else");
    else if(str.startsWith("!ENDIF"))
       str=str.replace("!ENDIF",".endif");
    else if(str.startsWith("!ERROR"))
       str=str.replace("!ERROR",".error");
    else if(str.startsWith("!UNDEF"))
       str=str.replace("!UNDEF",".undefvariable");
    else if(str.startsWith("!INCLUDE"))
       str=str.replace("!INCLUDE",".include");
    else if (str.startsWith("include ")) //for unix platform
       str=str.replace("include",".include");
    if (str.indexOf("$**")!=STRING_NOTFOUND)
       str=str.replace("$**","$>");
    /*--------------------------------------------------------------+
    | This logic is added to take care of the if condition block    |
    | after the continuation line (\).                              |
    | Examples:                                                     |
    | hello.exe: hello.obj \                                        |
    | .ifdef NT                                                     |
    |     bar.obj \                                                 |
    | .endif                                                        |
    |     hello2.obj                                                |
    |    link $** /OUT:hello.exe                                    |
    | The logic is that if the string contains some elements which  |
    | indicates that we have encountered continuation line before.  |
    | process the dot directive which will work as trigger point for|
    | whether we need to process the following block or skip them   |
    +---------------------------------------------------------------*/

    if (result->length()!=0 && str.startsWith(".")&& condeval!=0)
    {
       if (str.startsWith( DOT_IFNDEF ))
          {
            skipUntilENDIF=true;
            parseskip = !condeval->parseIfndef( str.substringThis(
               ConfigFile::DOT_IFNDEF.length() + 1 ) );
          }
       else if (str.startsWith( DOT_IFDEF))
          {
            skipUntilENDIF=true;
            parseskip = !condeval->parseIfdef( str.substringThis(
               ConfigFile::DOT_IFDEF.length()+1 ) );
          }
       else if (str.startsWith( DOT_IF ))
          {
            skipUntilENDIF=true;
            parseskip = !condeval->parseIf( str.substringThis(
               ConfigFile::DOT_IF.length() + 1 ) );
          }
       else if (str.startsWith( DOT_ELSE ))
          parseskip = !condeval->parseElse();
       else if (str.startsWith( DOT_ELIFDEF ))
          parseskip = !condeval->parseElifdef( str.substringThis(
          ConfigFile::DOT_ELIFDEF.length() + 1 ) );
       else if (str.startsWith( DOT_ELIFNDEF ))
          parseskip = !condeval->parseElifndef( str.substringThis(
          ConfigFile::DOT_ELIFNDEF.length()+ 1 ) );
       else if (str.startsWith( DOT_ELIF ))
          parseskip = !condeval->parseElif( str.substringThis(
          ConfigFile::DOT_ELIF.length()+ 1 ) );
       else if (str.startsWith( DOT_ENDIF ))
          {
             skipUntilENDIF=false;
             parseskip = !condeval->parseEndif();

          }
       continue; //go one to the next line for the coditional block
       //end of converting directives and parsing ifdef block
    }

    if (parseskip)
       continue;

    // If we know the string is empty, no need to process any further
    if (str.length() == 0)
    {
      return (result);
    }
    else if (ignore_comment && ConfigFile::isCommentLine( str ))
    {
      if (!added_lines)
        ++file_info->lastline;
    }
    else
    {
      if (!ConfigFile::cleanLine( str, trim ))
      {
      //add this part for skip the lines base on the ifdef block
      /*-------------------------------------------------------+
      |  so pop the lines until we reach .endif                |
      |  this can take care of the following situation         |
      |  hello.exe:  hello1.obj \                              |
      |  .ifdef BAR                                            |
      |       bar.obj                                          |
      |  .elifdef FOO                                          |
      |       foo.obj                                          |
      |  .else                                                 |
      |       hello2.obj                                       |
      |  .endif                                                |
      |     link $** /OUT:hello.exe                            |
      +--------------------------------------------------------*/
      String tempstr;
      if (skipUntilENDIF==true)
      {
         do{
         Path::readLine( *(file_info->fdr), &tempstr );
         ++file_info->nextline;
         }while (!tempstr.startsWith(DOT_ENDIF));
      }
      //
      //--------------------------------------------------------
        if (result->length() > 0)
          *result += ' ';
        *result += str;
        return (result);
      }

      if (result->length() > 0)
        *result += ' ';
      *result += str;

      added_lines = true;
    }
  }

  if (!added_lines)
  {
    if (buf != result)
      delete result;
    return (0);
  }

  return (result);
}
#endif // __WEBMAKE__

/**
 * Replaces the continuation character (if it is the last
 * non-whitespace char in the string) with a space.
 *
 * Afterwards (if trim is true), the string is trimmed.
 *
 * Returns true if a continuation char was found (and removed).
 *
**/
boolean ConfigFile::cleanLine( String &str, boolean trim, boolean replCont )
{
  static int i;
  static boolean rc;

  rc = false; // assume continuation will not be found

  for (i = str.lastIndex(); i >= STRING_FIRST_INDEX; --i)
  {
    if (str[i] == CONTINUATION_CHAR)
    {
      str[i] = ' ';
      rc = true;
      break;
    }
    if (str[i] != ' ' && str[i] != '\t')
      break;
  }

  if (trim)
  {
    str.trimThis();
    if (rc)
      str += " ";
  }
  else if (rc && !replCont)
  {
    // Remove the space that we added for the continuation char above.
    str.substringThis( STRING_FIRST_INDEX, i );
  }

  return (rc);
}

boolean ConfigFile::isCommentLine( const String &str )
{
  static const char *ptr;

  ptr = str.toCharPtr();
  while (*ptr == ' ' || *ptr == '\t')
    ++ptr;
  return (*ptr == COMMENT_CHAR);
}

/**
 * Returns true if a comment was removed, or false if
 * no comment was found in the string.
**/
boolean ConfigFile::stripComment( String &str )
{
  static int i;
  for (i = STRING_FIRST_INDEX; i <= str.lastIndex(); ++i)
  {
    if (str[i] == COMMENT_CHAR)
    {
      str.substringThis( STRING_FIRST_INDEX, i );
      return (true);
    }
  }

  return (false);
}


const String &ConfigFile::getPathname() const
{
  return (pathname);
}

int ConfigFile::getLineNumber() const
{
  return (getLastLineNumber());
}
