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



