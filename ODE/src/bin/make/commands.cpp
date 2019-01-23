using namespace std;
#define _ODE_BIN_MAKE_COMMANDS_CPP_

#include <base/binbase.hpp>

#include "lib/io/cfgf.hpp"
#include "lib/portable/runcmd.hpp"

#include "bin/make/command.hpp"
#include "bin/make/commands.hpp"
#include "bin/make/mkcmdln.hpp"


/************************************************
  *    --- void Commands::parseCmds ---
  *
  ************************************************/
void Commands::parseCmds( const Variable &var_eval, PassInstance *pn )
{
  var_eval_loc = &var_eval;
  pn_loc = pn;
}


/************************************************
  * --- inline void Commands::setFirstParsedCmd ---
  *
  ************************************************/
void Commands::setFirstParsedCmd()
{
  parseskip = false;
  print_debug_msg = MkCmdLine::dCond();
  keyword = &StringConstants::EMPTY_STRING;
  errmfname = StringConstants::EMPTY_STRING;
  print_debug_msg = MkCmdLine::dCond();
  setFirstCmd();
  cmd = (Command*) getNextCmd();
}


/************************************************
  * --- Command *Commands::getNextParsedCmd ---
  *
  ************************************************/
const Command *Commands::getNextParsedCmd()
{
  static String current_cmdstr;
  Command *ret_cmd = 0;
  if ( hasCmds() )
  {
    newLocalVar.clear();
    newLocalValue.clear();

    for (; ret_cmd == 0 && cmd != 0; cmd = (Command*) getNextCmd())
    {
      lastline = cmd->mfs->getLineNumber();
      errmfname = cmd->mfs->getPathname();

      current_cmdstr = cmd->getCmdName();
      if (current_cmdstr != StringConstants::EMPTY_STRING)
      {
        current_cmdstr.trimThis();
        // Only parse runtime conditional commands.
        // Otherwise, just return the command to the caller
        if ( current_cmdstr.startsWith( Constants::DOT_RIF )    ||
             current_cmdstr.startsWith( Constants::DOT_RELSE )  ||
             current_cmdstr.startsWith( Constants::DOT_RELIF )  ||
             current_cmdstr.startsWith( Constants::DOT_RENDIF ) ||
             current_cmdstr.startsWith( Constants::DOT_RFOR )   ||
             current_cmdstr.startsWith( Constants::DOT_RENDFOR )  )
        {
          // strip off comment for all line except for command line
          ConfigFile::stripComment( current_cmdstr );
          condeval.changeFileInfo( cmd->mfs );
          condeval.changeVariable( var_eval_loc );
          condeval.changeMakeInfo( pn_loc );

          if (current_cmdstr.startsWith( Constants::DOT_RFOR ))
          {
            keyword = &Constants::DOT_RFOR;
            parseFor( current_cmdstr.substringThis(
                                       Constants::DOT_RFOR.length() + 1 ) );
          }
          else if (current_cmdstr.startsWith( Constants::DOT_RENDFOR ))
          {
            keyword = &Constants::DOT_RENDFOR;
            parseEndFor();

          }
          else if (current_cmdstr.startsWith( Constants::DOT_RIF ))
          {
            keyword = &Constants::DOT_RIF;
            parseskip = !condeval.parseIf( current_cmdstr.substringThis(
              Constants::DOT_RIF.length() + 1 ) );
          }
          else if (current_cmdstr.startsWith( Constants::DOT_RELSE ))
          {
            keyword = &Constants::DOT_RELSE;
            parseskip = !condeval.parseElse();
          }
          else if (current_cmdstr.startsWith( Constants::DOT_RELIF ))
          {
            keyword = &Constants::DOT_RELIF;
            parseskip = !condeval.parseElif( current_cmdstr.substringThis(
              Constants::DOT_RELIF.length() + 1 ) );
          }
          else if (current_cmdstr.startsWith( Constants::DOT_RENDIF ))
          {
            keyword = &Constants::DOT_RENDIF;
            parseskip = !condeval.parseEndif();
          }
          else    // something else starts with '.', probably command
          {
            ret_cmd = cmd;
            print_debug_msg = false;
          }

          if (print_debug_msg)
          {
            String cond_debug_msg( "Cond: " );
            cond_debug_msg += cmd->mfs->getPathname() + "\", line " +
                              cmd->mfs->getLineNumber() + ": " + *keyword;

            if ((!parseskip || condeval.prevCondEvaluated()) &&
                 !current_cmdstr.startsWith( Constants::DOT_RENDIF ))
            {
              if (!current_cmdstr.startsWith( Constants::DOT_RELSE ))
                cond_debug_msg += current_cmdstr;
              cond_debug_msg += " == ";
              cond_debug_msg += (!parseskip) ? "true" : "false";
            }
            else
            {
              // If conditional was not even considered (false block)
              // Indicate so, by adding a "skipped" comment to output
              if ( !condeval.prevCondEvaluated() )
              {
                if ( !current_cmdstr.startsWith( Constants::DOT_RELSE ) )
                {
                  cond_debug_msg += current_cmdstr;
                }
                cond_debug_msg +=  " == skipped";
              }
            }

            Interface::printAlways( cond_debug_msg );
          }

        }
        else  //Not runtime conditional
        {
          if (!parseskip)
          {
            ret_cmd = cmd;
          }
        }
      }
    }
    // If there is a missing .rendif, report an error.
    if (ret_cmd == 0 && !condeval.allBlocksClosed())
      throw ParseException( errmfname, lastline,
              "One or more .rif blocks are unclosed (.rendif expected)" );

    // If there is a missing .rendfor, report an error.
    if (ret_cmd == 0 && !forLoops.isEmpty())
      throw ParseException( errmfname, lastline,
              "One or more .rfor blocks are unclosed (.rendfor expected)" );

  }
  return ret_cmd;
}


/************************************************
  * --- void Commands::parseFor ---
  *
  ************************************************/
void  Commands::parseFor( const String &line )
{
  // Look for required = sign.  Quit now if it doesn't exist.
  // Everything to left of = is variable name.
  // Space separated arguments to the right of =

  String line_eval = line.trim();
  StringArray eval_array;
  if (var_eval_loc)
  {
    var_eval_loc->parseUntil( line_eval, StringConstants::EMPTY_STRING, true,
        false, &eval_array );
    line_eval = eval_array[ARRAY_FIRST_INDEX];
  }
  int  equalIndex =  line_eval.indexOf( StringConstants::EQUAL_SIGN );

  if ( equalIndex == STRING_NOTFOUND )
  {
    throw ParseException( errmfname, cmd->mfs->getLineNumber(),
          "Illegal .rfor statement syntax ( equal sign '=' expected)" );
  }

  if ((equalIndex == line_eval.lastIndex()) ||
      (equalIndex == line_eval.firstIndex()))
  {
    throw ParseException( errmfname, cmd->mfs->getLineNumber(),
                          "Illegal .rfor statement syntax.");
  }

  // Create a new for loop instance and initialize it.
  RunTimeForLoop  *newLoop = new  RunTimeForLoop();
  newLoop->initialize( line_eval, equalIndex, &newLocalVar, &newLocalValue );

  // Save the index of .rfor command.
  newLoop->setCommandIndex( currIndex );

  // Add the newly created for loop to the maintained vector of loops.
  forLoops.addElement( newLoop );
}


/************************************************
  * --- void Commands::parseEndFor ---
  *
  ************************************************/
void  Commands::parseEndFor()
{
  // Hey,  we are not expecting any .rendfor at this point.
  if (forLoops.isEmpty())
  {
    throw ParseException( errmfname, cmd->mfs->getLineNumber(),
                          "Unexpected .rendfor statement.");
  }

  // Get for loop we are currently processing.
  RunTimeForLoop  *currForLoop = *forLoops.lastElement();

  // Move to next argument for the for loop.
  if (currForLoop->incrementArgIndex( &newLocalVar, &newLocalValue ))
  {
    // This for loop is still valid,  we need to reset the command enumerator
    // so that it points to the first command following the .rfor statement.
    resetCmdEnumerator( currForLoop->getCommandIndex() );
  }
  else
  {
    // We are finished with this loop, remove it and delete it.
    forLoops.removeElement( currForLoop );
    delete currForLoop;
  }
}




/************************************************
  *  - RunTimForLoop::initialize -
  *
  ************************************************/
void RunTimeForLoop::initialize( String      line,
                                 int         equalIndex,
                                 StringArray *newLocalVar,
                                 StringArray *newLocalValue )
{
  // get variable name
  variable = line.substring( STRING_FIRST_INDEX, equalIndex );
  variable.trimThis();

  // split arguments to right of = sign
  line.substringThis( equalIndex + 1 );
  line.trimThis();

  line.split( StringConstants::SPACE_TAB, UINT_MAX, &args );
  argIndex =  ARRAY_FIRST_INDEX;

  int  beginIdx = 0,  endIdx = 0;

  if ((args.lastIndex() - ARRAY_FIRST_INDEX == 2) &&
      (args[ARRAY_FIRST_INDEX + 1].trim() == "-") &&
      (args[ARRAY_FIRST_INDEX].isDigits())        &&
      (args[ARRAY_FIRST_INDEX + 2].isDigits()))
  {
    beginIdx = args[ARRAY_FIRST_INDEX].asInt();
    endIdx   = args[ARRAY_FIRST_INDEX + 2].asInt();

    // Make sure that beginIdx is <= endIdx
    // If not, treat as non-numeric loop.
    if (beginIdx <= endIdx)
    {
      args.clear();
      for (int i = beginIdx; i <= endIdx; i++)
      {
        args.add( String( i ) );
      }
    }
  }

  // Set variable value
  newLocalVar->add  ( variable );
  newLocalValue->add( args[ARRAY_FIRST_INDEX] );
}


/************************************************
  *  - RunTimForLoop::incrementArgIndex -
  *
  ************************************************/
boolean RunTimeForLoop::incrementArgIndex( StringArray *newLocalVar,
                                           StringArray *newLocalValue )
{
  argIndex++;
  newLocalVar->add( variable );

  if (argIndex <= args.lastIndex())
  {
    // Set variable value
    newLocalValue->add( args[argIndex] );
    return( true );
  }
  else
  {
    // also need to clear variable out
    newLocalValue->add( StringConstants::EMPTY_STRING );
    return( false );
  }
}

