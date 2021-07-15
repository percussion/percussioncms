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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
