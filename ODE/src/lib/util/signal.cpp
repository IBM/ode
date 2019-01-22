#include <signal.h>
#include <iostream.h>

#define _ODE_LIB_UTIL_SIGNAL_CPP_
#include "lib/util/signal.hpp"
#include "lib/string/string.hpp"

Signalable*       Signal::interrupt_handler = 0;
SignalFunctionPtr Signal::old_sigint_handler = SIG_DFL;
SignalFunctionPtr Signal::old_sigbreak_handler = SIG_DFL;
SignalFunctionPtr Signal::old_sigquit_handler = SIG_DFL;
SignalFunctionPtr Signal::old_sigterm_handler = SIG_DFL;
volatile boolean  Signal::interrupted = false;
int               Signal::sigType = -1;


extern "C"
{

void handleInterrupt( int signum )
{
  Signal::setInterruptFlag();
  Signal::sigType = signum;

  if (Signal::interrupt_handler != 0)
    Signal::interrupt_handler->handleInterrupt();

  Signal::resetHandler( signum );
}

} /* extern "C" */


/******************************************************************************
 * When an interrupt is received raise a flag
 */
void Signal::setInterruptFlag()
{
  interrupted = true;
}


/******************************************************************************
 *
 */
void Signal::unsetInterruptFlag()
{
  interrupted = false;
}


/******************************************************************************
 *
 */
boolean Signal::isInterrupted()
{
  return interrupted;
}


/******************************************************************************
 *
 */
int Signal::getSignalType()
{
  return sigType;
}

/**
 * Returns true if the first handler was added.
**/
boolean Signal::addHandler( Signalable *handler )
{
  boolean rc = (interrupt_handler == 0);
  interrupt_handler = handler;
  return (rc);
}

void Signal::removeHandlers()
{
  interrupt_handler = 0;
}

void Signal::saveSigint( SignalFunctionPtr sigptr )
{
  old_sigint_handler = sigptr;
}

void Signal::saveSigbreak( SignalFunctionPtr sigptr )
{
  old_sigbreak_handler = sigptr;
}

void Signal::saveSigquit( SignalFunctionPtr sigptr )
{
  old_sigquit_handler = sigptr;
}

void Signal::saveSigterm( SignalFunctionPtr sigptr )
{
  old_sigterm_handler = sigptr;
}
