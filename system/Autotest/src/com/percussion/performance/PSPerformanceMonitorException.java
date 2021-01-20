/*[ PSPerformanceMonitorException.java ]***************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.performance;

/**
 * This exception is thrown when a problem occurs while trying to interact
 * with the operating system to collect statistics about the computer.
 */
public class PSPerformanceMonitorException extends Exception
{
   /**
    * Creates the exception using the supplied detail as the error message.
    *
    * @param detail The error message, may be <code>null</code>.
    */
   public PSPerformanceMonitorException( String detail )
   {
      super( detail );
   }
}
