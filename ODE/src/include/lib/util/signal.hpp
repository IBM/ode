/**
 * Signalable
 * Signal
**/

#ifndef _ODE_LIB_UTIL_SIGNAL_HPP_
#define _ODE_LIB_UTIL_SIGNAL_HPP_

#include <signal.h>

#include <base/odebase.hpp>

extern "C"
{

void handleInterrupt( int signum );

} /* extern "C" */

class Signalable
{
  public:
  
    virtual ~Signalable() {};

    // when implementing this, do NOT call exit() or abort().
    // just do your cleanup work and return normally.
    // the Signal class will ensure the program terminates
    // after all registered handlers have been called.
    virtual void handleInterrupt() = 0;
};

typedef Signalable* SignalablePtr;

class Signal
{
  public:

    inline static void registerInterruptHandler( Signalable *handler );
    inline static void ignoreInterrupts();
    inline static void restoreInterrupts();
    static int getSignalType();
    static void setInterruptFlag();
    static void unsetInterruptFlag();
    static boolean isInterrupted();

  private:

    static volatile boolean ODEDLLPORT interrupted;
    static int ODEDLLPORT sigType;
    static SignalablePtr ODEDLLPORT interrupt_handler;
    static SignalFunctionPtr ODEDLLPORT old_sigint_handler;
    static SignalFunctionPtr ODEDLLPORT old_sigbreak_handler;
    static SignalFunctionPtr ODEDLLPORT old_sigquit_handler;
    static SignalFunctionPtr ODEDLLPORT old_sigterm_handler;

    inline static void resetHandler( int signnum );
    inline static SignalFunctionPtr setSignal( int signum,
        SignalFunctionPtr handler );
    static boolean addHandler( Signalable *handler );
    static void removeHandlers();
    static void saveSigint( SignalFunctionPtr sigptr );
    static void saveSigbreak( SignalFunctionPtr sigptr );
    static void saveSigquit( SignalFunctionPtr sigptr );
    static void saveSigterm( SignalFunctionPtr sigptr );


  friend void handleInterrupt( int signum );
};

inline void Signal::registerInterruptHandler( Signalable *handler )
{
  if (addHandler( handler )) // was it the first handler added?
  {
#ifdef SIGINT
    saveSigint( setSignal( SIGINT, (SignalFunctionPtr)handleInterrupt ) );
#endif
#ifdef SIGBREAK
    saveSigbreak( setSignal( SIGBREAK, (SignalFunctionPtr)handleInterrupt ) );
#endif
#ifdef SIGQUIT
    saveSigquit( setSignal( SIGQUIT, (SignalFunctionPtr)handleInterrupt ) );
#endif
#ifdef SIGTERM
    saveSigterm( setSignal( SIGTERM, (SignalFunctionPtr)handleInterrupt ) );
#endif
  }
}

inline void Signal::resetHandler( int signum )
{
  signal( signum, (SignalFunctionPtr)handleInterrupt );
}

inline void Signal::ignoreInterrupts()
{
  removeHandlers();
#ifdef SIGINT
  saveSigint( setSignal( SIGINT, (SignalFunctionPtr)SIG_IGN ) );
#endif
#ifdef SIGBREAK
  saveSigbreak( setSignal( SIGBREAK, (SignalFunctionPtr)SIG_IGN ) );
#endif
#ifdef SIGQUIT
  saveSigquit( setSignal( SIGQUIT, (SignalFunctionPtr)SIG_IGN ) );
#endif
#ifdef SIGTERM
  saveSigterm( setSignal( SIGTERM, (SignalFunctionPtr)SIG_IGN ) );
#endif
}

/**
 * Removes all registered handlers and restores the
 * signal settings.
**/
inline void Signal::restoreInterrupts()
{
  removeHandlers();
#ifdef SIGINT
  setSignal( SIGINT, old_sigint_handler );
#endif
#ifdef SIGBREAK
  setSignal( SIGBREAK, old_sigbreak_handler );
#endif
#ifdef SIGQUIT
  setSignal( SIGQUIT, old_sigquit_handler );
#endif
#ifdef SIGTERM
  setSignal( SIGTERM, old_sigterm_handler );
#endif
}

inline SignalFunctionPtr Signal::setSignal( int signum,
    SignalFunctionPtr handler )
{
#ifdef USE_SIGACTION_FOR_SIGS
  struct sigaction new_act, old_act;
  new_act.sa_handler = handler;
  sigemptyset( &new_act.sa_mask );
  new_act.sa_flags = SA_NODEFER | SA_RESETHAND;
  new_act.sa_sigaction = 0;
  if (sigaction( signum, &new_act, &old_act ) != 0)
    return ((SignalFunctionPtr)SIG_DFL);
  return ((SignalFunctionPtr)old_act.sa_handler);
#else
  return ((SignalFunctionPtr)signal( signum, handler ));
#endif
}

#endif /* _ODE_LIB_UTIL_SIGNAL_HPP_ */
