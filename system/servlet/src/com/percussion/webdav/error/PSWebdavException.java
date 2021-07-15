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
