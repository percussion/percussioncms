/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.workflow;
/**
 * This is the only checked exception that may be thrown by the method
 * <CODE>ExecuteTest</CODE> in any class that implements
 * <CODE>PSWorkflowTest</CODE>, it is a wrapper for the original exception. 
 */

public class PSWorkflowTestException extends Exception 
{
   public PSWorkflowTestException ()
   {
      super();
   }
   
   public PSWorkflowTestException (String s)
   {
      super(s);
   }

   /**
    * Constructor specifying a <CODE>Throwable</CODE>
    *
    * @param throwable The original <CODE>Exception</CODE> or
    *                  <CODE>Error</CODE> being wrapped by this exception
    */
   public PSWorkflowTestException (Throwable throwable)
   {
      super();
      m_Throwable = throwable;
   }

   /**
    * Constructor specifying a <CODE>Throwable</CODE>
    *
    * @param s         error string            
    * @param throwable The original <CODE>Exception</CODE> or
    *                  <CODE>Error</CODE> being wrapped by this exception
    */
   public PSWorkflowTestException (String s,
                                   Throwable throwable)
   {
      super(s);
      m_Throwable = throwable;
   }


   /**
    * Gets the value of the wrapped <CODE>Throwable</CODE>
    *
    * @return the value of wrapped <CODE>Throwable</CODE>
    */
   public Throwable getThrowable() 
   {
      return m_Throwable;
   }

   /*
    * The original <CODE>Exception</CODE> or <CODE>Error</CODE> being wrapped
    * by this exception.
    */
   private Throwable m_Throwable = null;
}
