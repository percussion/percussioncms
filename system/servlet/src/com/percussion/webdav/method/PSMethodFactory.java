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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.webdav.method;

import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.IPSWebdavErrors;
import com.percussion.webdav.error.PSWebdavException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The factory for creating various WebDAV Method implementations.
 */
public class PSMethodFactory
{

   /**
    * Creates the method implementor for the specified method's name.
    * 
    * @param name The name of the method, it may not be <code>null</code> or
    *    empty.
    *    
    * @param req The servlet request, it may not be <code>null</code>.
    * 
    * @param resp The servlet response, it may not be <code>null</code>.
    * 
    * @param servlet The webdav servlet, it may not be <code>null</code>.
    * 
    * @return The corresponding method implementor, never <code>null</code>.
    * 
    * @throws PSWebdavException if the method is not supported.
    */
   public static PSWebdavMethod createMethod(
      String name, 
      HttpServletRequest req,
      HttpServletResponse resp,
      PSWebdavServlet servlet)
      throws PSWebdavException
   {
      if ((name == null) || (name.trim().length() == 0)) 
         throw new IllegalArgumentException("name may not be null or empty");
      
      if (name.equals("OPTIONS"))
      {
         return new PSOptionsMethod(req, resp, servlet);
      }
      else if (name.equals("PROPFIND"))
      {
         return new PSPropFindMethod(req, resp, servlet);
      }
      else if (name.equals("GET"))
      {
         return new PSGetMethod(req, resp, servlet);
      }
      else if (name.equals("PUT"))
      {
         return new PSPutMethod(req, resp, servlet);
      }
      else if (name.equals("HEAD"))
      {
         return new PSHeadMethod(req, resp, servlet);
      }
      else if (name.equals("MKCOL"))
      {
         return new PSMkcolMethod(req, resp, servlet);
      }
      else if (name.equals("LOCK"))
      {
         return new PSLockMethod(req, resp, servlet);
      }
      else if (name.equals("UNLOCK"))
      {
         return new PSUnlockMethod(req, resp, servlet);
      }
      else if (name.equals("DELETE"))
      {
         return new PSDeleteMethod(req, resp, servlet);
      }
      else if (name.equals("COPY"))
      {
         return new PSCopyMethod(req, resp, servlet);
      }
      else if (name.equals("MOVE"))
      {
         return new PSMoveMethod(req, resp, servlet);
      }
      else if (name.equals("PROPPATCH"))
      {
         return new PSPropPatchMethod(req, resp, servlet);
      }

      PSWebdavException e = new PSWebdavException(
         IPSWebdavErrors.UNSUPPORTED_METHOD, name);
      e.setStatusCode(PSWebdavStatus.SC_METHOD_NOT_ALLOWED);
      
      throw e;
   }
}

