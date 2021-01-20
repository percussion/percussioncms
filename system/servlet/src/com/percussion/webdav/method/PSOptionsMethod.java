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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.PSWebdavException;

/**
 * This class implements the OPTIONS WebDAV method.
 */
public class PSOptionsMethod extends PSWebdavMethod
{

   /**
    * Constructs an instance from the given parameters.
    *
    * @param req   The servlet request, it may not be <code>null</code>.
    * @param resp  The servlet response, it may not be <code>null</code>.
    * @param servlet The webdav servlet, it may not be <code>null</code>.
    */
   public PSOptionsMethod(
      HttpServletRequest req,
      HttpServletResponse resp,
      PSWebdavServlet servlet)
   {
      super(req, resp, servlet);
   }

   // Implements PSWebdavMethod.parseRequest() 
   protected void parseRequest() throws PSWebdavException
   {
   }


   // Implements PSWebdavMethod.processRequest() 
   protected void processRequest() 
      throws PSWebdavException,
             IOException
   {
      HttpServletResponse response = getResponse();
      
      response.addHeader("MS-Author-Via", "DAV"); // needed for MS client
      
      response.addHeader("DAV", "1,2"); // use "1,2" if support lock/unlock
      response.addHeader("Allow", SUPPORTED_METHODS);
      
      setResponseStatus(PSWebdavStatus.SC_OK);
   }

   /**
    * The contants for a list of supported methods.  Fixme: build string list
    * from constant in PSWebDavServletController
    */
   private final static String SUPPORTED_METHODS = 
      "GET, HEAD, POST, PUT, DELETE, OPTIONS, PROPFIND, PROPPATCH, COPY, MOVE,"
      + " LOCK, UNLOCK";
}



