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

package com.percussion.webdav.error;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.error.PSStandaloneException;
import com.percussion.hooks.PSServletException;

import org.w3c.dom.Element;

/**
 * The WebDAV specific exception.
 */
public class PSWebdavException extends PSServletException
{
   // see com.percussion.error.PSStandaloneException(int, Object)
   public PSWebdavException(int msgCode, Object singleArg)
   {
      super(msgCode, singleArg);
   }

  /**
   * Convenient method to call {@link #PSWebdavException(int, Object)} and
   * {@link #setStatusCode(int)}.
   */
   public PSWebdavException(int msgCode, Object singleArg, int statusCode)
   {
      super(msgCode, singleArg);
      setStatusCode(statusCode);
   }

   // see com.percussion.error.PSStandaloneException(int, Object[])
   public PSWebdavException(int msgCode, Object[] arrayArgs)
   {
      super(msgCode, arrayArgs);
   }

   // see com.percussion.error.PSStandaloneException(int)
   public PSWebdavException(int msgCode)
   {
      super(msgCode);
   }

   // see com.percussion.error.PSStandaloneException(PSException)
   public PSWebdavException(PSException ex)
   {
      super(ex);
   }

   /**
    * Convenient method, calls {@link #PSWebdavException(PSException)} and
    * {@link setStatusCode(int)}.
    */
   public PSWebdavException(PSException ex, int statusCode)
   {
      super(ex);
      setStatusCode(statusCode);
   }

   // see com.percussion.error.PSStandaloneException(PSStandaloneException)
   public PSWebdavException(PSStandaloneException ex)
   {
      super(ex);
   }

   // see {@link com.percussion.error.PSStandaloneException(Element)
   public PSWebdavException(Element source) throws PSUnknownNodeTypeException
   {
      super(source);
   }
   
   /**
    * Set the status code, which may be used for the response status.
    * 
    * @param statusCode The to be set status code.
    */
   public void setStatusCode(int statusCode)
   {
      m_statusCode = statusCode;
   }
   
   /**
    * Get the status code.
    * 
    * @return The status code; <code>-1</code> if the status code has not
    *    been set for the exception.
    */
   public int getStatusCode()
   {
      return m_statusCode;
   }
   
   /**
    * The status code of this exception. Init to be <code>-1</code>.
    */
   private int m_statusCode = -1;
}
