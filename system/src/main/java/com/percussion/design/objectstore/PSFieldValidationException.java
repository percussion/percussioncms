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
package com.percussion.design.objectstore;

import com.percussion.cms.PSCmsException;
import org.apache.commons.lang.StringUtils;

/**
 * This is a specific exception class derived from PSCmsException to hold the
 * field validation errors and the cached page url.
 */
public class PSFieldValidationException extends PSCmsException
{
   /**
    * Ctor
    * 
    * @param msgCode The message code of the validation error.
    * @param arrayArgs the array of arguments to use as the arguments in the
    *           error message
    * @param error The display error object, must not be <code>null</code>.
    * @param cachedPageUrl Content editor creates a cachedPage url when there is
    *           a field validation.
    */
   public PSFieldValidationException(int msgCode, Object[] arrayArgs,
         PSDisplayError error, String cachedPageUrl)
   {
      super(msgCode, arrayArgs);
      if (error == null)
         throw new IllegalArgumentException("error must not be null");
      m_displayError = error;
      m_cachedPageUrl = StringUtils.defaultString(cachedPageUrl);
   }

   /**
    * @return the display error object never <code>null</code>.
    */
   public PSDisplayError getDisplayError()
   {
      return m_displayError;
   }

   /**
    * @return the cached page url may be empty but never <code>null</code>.
    */
   public String getCachedPageUrl()
   {
      return m_cachedPageUrl;
   }

   /**
    * The display error object associated with this exception, initialized in
    * the ctor and never <code>null</code> after that.
    */
   private PSDisplayError m_displayError;

   /**
    * The cached page url that is created when there is any field validation
    * error occurs. Initialized in the ctor, never <code>null</code> may be
    * empty, if this exception is created with a blank url.
    */
   private String m_cachedPageUrl;

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

}
