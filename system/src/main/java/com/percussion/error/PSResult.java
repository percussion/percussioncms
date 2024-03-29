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
package com.percussion.error;

import com.percussion.cms.objectstore.PSKey;

/**
 * This is the base class for all test results produced by different methods in
 * the {@link IPSEffect interface}. Implements common methods to pass informtion
 * from the methods to relationship engine after execution.
 */
public class PSResult
{
   /**
    * Default constructor. Does nothing special.
    */
   public PSResult()
   {
   }

   /**
    * Is the test success?
    * @return <code>true</code> if the status indicates a not a failure,
    *  </code>false</code> otherwise.
    */
   public boolean isSuccess()
   {
      return (m_status!=STATUS_ERROR);
   }

   /**
    * Did the test produced any warning?
    * @return <code>true</code> if the status indicates a warning,
    *  </code>false</code> otherwise.
    */
   public boolean hasWarning()
   {
      return (m_status==STATUS_WARNING);
   }

   /**
    * Access method for the exception.
    * @return Exception if the status in the object is not a success.
    * <code>null</code> otherwise.
    */
   public PSException getException()
   {
      return m_ex;
   }

   /**
    * Access method for the status code.
    * @return one of the possibe status code values (STATUS_XXXX).
    */
   public int getStatus()
   {
      return m_status;
   }

   /**
    * Returns keys supplied for this result.
    * @return an array of keys, may be <code>null</code> or <code>empty</code>.
    */
   public PSKey[] getKeys()
   {
      return m_keys;
   }

   /**
    * Sets keys for this result.
    * @param keys keys to set, may be <code>null</code> or <code>empty</code>.
    */
   public void setKeys(PSKey[] keys)
   {
      m_keys = keys;
   }

   /**
    * Sets the exception for the object as error. Implicitly sets is the staus
    * code to STATUS_ERROR.
    * @param  msg message to attach to this result, never <code>null</code>.
    * @throws IllegalArgumentException if the parameter supplied is <code>null</code>.
    */
   public void setError(String msg)
   {
      if(msg == null)
         throw new IllegalArgumentException("msg must not be null");

      m_status = STATUS_ERROR;
      m_ex = new PSException(-1, msg);
   }
   
   /**
    * Sets the message for the object as error. Implicitly sets is the staus
    * code to STATUS_ERROR.
    * @param  lang language string to specify the locale for the string, must not be <code>null</code>.
    * @param msgCode message code to look in the i18n resource bundle.
    * @param args is the array of arguments required to format the error message, may be <code>null</code> if not required.
    * @throws IllegalArgumentException if the required parameter supplied is <code>null</code>.
    */
   public void setError(String lang, int msgCode, Object[] args)
   {
      if(lang == null)
         throw new IllegalArgumentException("lang must not be null");

      m_status = STATUS_ERROR;
      m_ex = new PSException(lang, msgCode, args);
   }

   /**
    * Sets status to ERROR a given message and also creates an exception object.
    * Implicitly sets is the staus code to STATUS_ERROR.
    * @param    exception exception object to be set, must not be <code>null</code>.
    * @throws IllegalArgumentException if the parameter supplied is <code>null</code>.
    */
   public void setError(PSException exception)
   {
      if(exception == null)
         throw new IllegalArgumentException("exception must not be null");

      m_status = STATUS_ERROR;
      m_ex = exception;
   }

   /**
    * Sets the message for the object as warning. Implicitly sets is the staus
    * code to STATUS_WARNING.
    * @param  msg message to attach to this result, never <code>null</code>.
    * @throws IllegalArgumentException if the parameter supplied is <code>null</code>.
    */
   public void setWarning(String msg)
   {
      if(msg == null)
         throw new IllegalArgumentException("msg must not be null");

      m_status = STATUS_WARNING;
      m_ex = new PSException(-1, msg);
   }

   /**
    * Sets the message for the object as warning. Implicitly sets is the staus
    * code to STATUS_WARNING.
    * @param  lang language string to specify the locale for the string, must not be <code>null</code>.
    * @param msgCode message code to look in the i18n resource bundle.
    * @param args is the array of arguments required to format the error message, may be <code>null</code> if not required.
    * @throws IllegalArgumentException if the required parameter supplied is <code>null</code>.
    */
   public void setWarning(String lang, int msgCode, Object[] args)
   {
      if(lang == null)
         throw new IllegalArgumentException("lang must not be null");

      m_status = STATUS_WARNING;
      m_ex = new PSException(lang, msgCode, args);
   }

   /**
    * Sets the exception for the object as warning. Implicitly sets is the staus
    * code to STATUS_WARNING.
    * @param  exception exception object to be set, must not be <code>null</code>.
    * @throws IllegalArgumentException if the parameter supplied is <code>null</code>.
    */
   public void setWarning(PSException exception)
   {
      if(exception == null)
         throw new IllegalArgumentException("exception must not be null");

      m_status = STATUS_WARNING;
      m_ex = exception;
   }

   /**
    * Sets result to success.
    */
   public void setSuccess()
   {
      m_status = STATUS_SUCCESS;
   }

   /**
    * Current value of the process status. Default value is STATUS_ERROR.
    */
   private int m_status = STATUS_ERROR;

   /**
    * Exception if the test result indicates an error or warning. Initialized
    * to <code>null</code> and can be set using <code>setException</code> method.
    */
   private PSException m_ex =  null;

   /**
    * Placeholder for the keys that client may attach to the result,
    * may be <code>null</code> or <code>empty</code>.
    */
   private PSKey[] m_keys = null;

   /**
    * Constant indicating the test result status of success
    */
   public static final int STATUS_SUCCESS = 0;

   /**
    * Constant indicating the test result status of failure
    */
   public static final int STATUS_ERROR = 1;

   /**
    * Constant indicating the test result status of warning
    */
   public static final int STATUS_WARNING = 2;
}
