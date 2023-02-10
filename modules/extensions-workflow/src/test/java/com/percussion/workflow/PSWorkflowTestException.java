/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
