package com.ibm.ode.lib.util;

import java.lang.Error;

/**
 * <pre>
 * Thrown when the common interface
 * can not create a process for executing the command.
 * </pre>
 *
 * @author  Chary Lingachary
 * @version     1.4 98/01/22
 * @since   SDWB 1.3.1
 */
public class ProcessCreationError extends Error 
{
   /**
    * Constructs a <code>ProcessCreationError</code>
    *
    * @since   SDWB 1.3.1
    */
   public ProcessCreationError()
   {
      super();
   }

   /**
    * Constructs a <code>ProcessCreationError</code> with the
    * command to execute
    *
    * @param   <code>command</code> command to execute
    * @since   SDWB 1.3.1
    */
   public ProcessCreationError( String command )
   {
      super();
      detailMessage_ = detailMessage_ + " Command: " + command;
   }

   /**
    * detailed custom message
    *
    * @since   SDWB 1.3.1
    */
   public String getMessage()
   {
      return detailMessage_;
   }

   /**
    * String representation of the Error object
    */
   public String toString()
   {
      return (detailMessage_ != null) ? (getClass().getName() + ": " + detailMessage_) : getClass().getName();
   }

   /**
    * detailed message
    *
    * @since   SDWB 1.3.1
    */
   private String detailMessage_ = "Creating process..."; 
}


