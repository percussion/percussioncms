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
package com.percussion.utils.xml;

import com.percussion.utils.exceptions.PSBaseException;

/**
 * Exception to indicate general Xml format errors.
 */
public class PSInvalidXmlException extends PSBaseException
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   /**
    *  @see PSBaseException#PSBaseException(int, Object[])
    */
   public PSInvalidXmlException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }

   /**
    *  @see PSBaseException#PSBaseException(int, Object[])
    */
   public PSInvalidXmlException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /**
    *  @see PSBaseException#PSBaseException(int)
    */
   public PSInvalidXmlException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * @see PSBaseException#PSBaseException(int, Throwable, Object[])
    */
   public PSInvalidXmlException(int msgCode, Object[] arrayArgs, 
      Throwable cause)
   {
      super(msgCode, cause, arrayArgs);
   }

   // see base class
   @Override
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.utils.xml.PSXmlErrorStringBundle";
   }
   
}

