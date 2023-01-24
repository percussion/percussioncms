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

