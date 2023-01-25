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
package com.percussion.services.sitemgr;

import com.percussion.utils.exceptions.PSBaseException;

/**
 * Exception for operations in the site manager
 * 
 * @author dougrand
 */
public class PSSiteManagerException extends PSBaseException
{
   private static final long serialVersionUID = 1L;

   /**
    * Ctor
    * 
    * @param msgCode code for message in properties file
    * @param arrayArgs arguments for message
    */
   public PSSiteManagerException(int msgCode, Object... arrayArgs) {
      super(msgCode, arrayArgs);
   }

   /**
    * Ctor
    * 
    * @param msgCode code for message in properties file
    * @param cause the original exception, or <code>null</code> if there was
    *           no cause exception
    * @param arrayArgs arguments for message
    */
   public PSSiteManagerException(int msgCode, Throwable cause,
         Object... arrayArgs) {
      super(msgCode, cause, arrayArgs);
   }

   /**
    * Ctor
    * @param msgCode the message code in the properties file
    */
   public PSSiteManagerException(int msgCode) {
      super(msgCode);
   }

   @Override
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.services.sitemgr.PSSiteManagerErrorStringBundle";
   }

}
