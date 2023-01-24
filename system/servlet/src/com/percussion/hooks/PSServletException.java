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
package com.percussion.hooks;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.error.PSStandaloneException;

import org.w3c.dom.Element;


/**
 * Exception class used to report general exceptions for servlets application,
 * or may be subclassed if necessary.  Handles formatting of messages stored in
 * the PSHookResources.properties resource bundle using error codes and
 * arguments. Localization is also supported.
 */
public class PSServletException extends PSStandaloneException
{
   /**
    * @see {@link com.percussion.error.PSStandaloneException(int, Object)
    */
   public PSServletException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }

   /**
    * @see {@link com.percussion.error.PSStandaloneException(int, Object[])
    */
   public PSServletException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   /**
    * @see {@link com.percussion.error.PSStandaloneException(int)
    */
   public PSServletException(int msgCode)
   {
      super(msgCode);
   }

   /**
    * @see {@link com.percussion.error.PSStandaloneException(PSException)
    */
   public PSServletException(PSException ex)
   {
      super(ex);
   }

   /**
    * @see
    * {@link com.percussion.error.PSStandaloneException(PSStandaloneException)
    */
   public PSServletException(PSStandaloneException ex)
   {
      super(ex);
   }

   /**
    * @see {@link com.percussion.error.PSStandaloneException(Element)
    */
   public PSServletException(Element source) throws PSUnknownNodeTypeException
   {
      super(source);
   }

   /**
    * @see {@link com.percussion.error.getResourceBundleBaseName()
    */
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.hooks.PSServletErrorBundle";
   }

   /**
    * @see {@link com.percussion.error.getXmlNodeName()
    */
   protected String getXmlNodeName()
   {
      return "PSXServletException";
   }

}



