/******************************************************************************
 *
 * [ PSPublisherRunnerException.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.publisher.runner;

/**
 * This exception is thrown by publisher runner if publish HTTP request fails
 * for any reason. This is just same as its base class except for the name.
 */
public class PSPublisherRunnerException extends Exception
{
   /**
    * Empty constructor
    */
   public PSPublisherRunnerException()
   {
      super();
   }

   /**
    * Constructor that takes the error message.
    */
   public PSPublisherRunnerException(String msg)
   {
      super(msg);
   }
}
