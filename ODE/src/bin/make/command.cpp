using namespace std;
#define _ODE_BIN_MAKE_COMMAND_CPP_

#include "base/binbase.hpp"
#include "lib/exceptn/parseexc.hpp"

#include "bin/make/command.hpp"
#include "bin/make/mkcmdln.hpp"
#include "bin/make/graphnd.hpp"
#include "bin/make/makec.hpp"


int Command::formatCommand( const Variable &var_eval, String &cmdbuf )
{
  ignore_errors = MkCmdLine::ignoreErrors();
  boolean more_chars = true;
  be_silent  = MkCmdLine::noEcho();
  try
  {
    StringArray names;
    if (MkCmdLine::dpVars())
    {
      VarInfoPrinter ip;
      var_eval.parseUntil( name, StringConstants::EMPTY_STRING, true, false, &names, &ip );
    }
    else
      var_eval.parseUntil( name, StringConstants::EMPTY_STRING, true, false, &names );
    cmdbuf = names[ARRAY_FIRST_INDEX].trimThis();

    // If the command is only "@" then do nothing.
    //
    if (cmdbuf.equals(StringConstants::AT_SIGN))
      return (GraphNode::UNMADE);

    while (more_chars)
    {
      switch (cmdbuf.charAt( cmdbuf.firstIndex()) )
      {
        case '@':
          cmdbuf.substringThis( cmdbuf.firstIndex()+1 );
          be_silent = true;
          break;
        case '-':
          cmdbuf.substringThis( cmdbuf.firstIndex()+1 );
          ignore_errors=true;
          break;
        default:
          more_chars=false;
          break;
      } /* end switch */
    } /* end while */

    // We may need to reformat the command string so the shell
    // command interpreter takes it as we expect.
    cmdbuf.reduceWhitespaceThis();

    if (cmdbuf.length() == 0 || cmdbuf == StringConstants::EMPTY_STRING)
      return (GraphNode::UNMADE);

  }
  catch (MalformedVariable &e)
  {
    ParseException newexp( getPathName(), getBeginLine(), e.getMessage() );
    Make::error( newexp.toString() );
    return (GraphNode::ERROR);
  }
  return (GraphNode::MADE);
}

