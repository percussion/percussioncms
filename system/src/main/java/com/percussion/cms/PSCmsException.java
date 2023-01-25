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
package com.percussion.cms;

import com.percussion.error.PSException;

/**
 * This class is used when an error occurs during a request for binary content.
 */
public class PSCmsException extends PSException
{

   /**
    * Construct an exception for messages taking no arguments.
    *
    * @param msgCode - the error string to load.  There is no validation on this
    *    value.
    */
   public PSCmsException(int msgCode)
   {
      super(msgCode);
   }

   public PSCmsException(int code, Throwable t){
      super(code,t);
   }

   public PSCmsException(Throwable t){
      super(t);
   }

   /**
    * Construct an exception from a class derived from PSException.
    *
    * @param ex The exception to use.  Its message code and arguments are
    *    stored.  May not be <code>null</code>.
    */
   public PSCmsException(PSException ex)
   {
      this(ex.getErrorCode(), ex.getErrorArguments(), ex);
   }

   /**
    * Construct an exception for messages taking 2 specific arguments.
    *
    * @param msgCode - the error string to load.  There is no validation on this
    * value.
    * @param contentId - the contentid on which the error occurred. May be 0.
    * @param revisionId - the revisionid on which the error occurred.  May be 0.
    */
   public PSCmsException(int msgCode, int contentId, int revisionId)
   {
      this(msgCode, new Integer[]
         {
                 contentId, revisionId
         });
   }

   /**
    * Construct an exception for messages taking multiple arguments
    *
    * @param msgCode - the error string to load.  There is no validation on this
    *    value.
    * @param singleMessage the sole of argument to use as the arguments in the
    *    error message
    */
   public PSCmsException(int msgCode, String singleMessage)
   {
      super(msgCode,singleMessage);
   }
   /**
    * Construct an exception for messages taking multiple arguments
    *
    * @param msgCode - the error string to load.  There is no validation on this
    *    value.
    * @param arrayArgs the array of arguments to use as the arguments in the
    *    error message
    */
   public PSCmsException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode,arrayArgs);
   }

   /**
    * Construct an exception for messages taking multiple arguments
    *
    * @param msgCode - the error string to load.  There is no validation on this
    *    value.
    * @param arrayArgs the array of arguments to use as the arguments in the
    *    error message
    */
   public PSCmsException(int msgCode, Object[] arrayArgs, Throwable cause)
   {
      super(msgCode,arrayArgs, cause);
   }

}


