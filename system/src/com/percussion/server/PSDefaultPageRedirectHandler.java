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
package com.percussion.server;

import java.io.IOException;

/**
 * Handles a request for the default page of an application by generating
 * a HTTP_MOVED_TEMPORARILY response that redirects to the default page
 * 
 * @author James Schultz
 * @since 4.0
 */
public class PSDefaultPageRedirectHandler implements IPSRequestHandler
{
   /**
    * Creates a new PSDefaultPageRedirectHandler
    * 
    * @param fullRequestRoot includes the server and application root (don't
    *    include a trailing slash); must not be <code>null</code> or empty; 
    *    for example, "/Rhythmyx/sys_ca" (uses the same format as the output 
    *    from <code>PSServer.makeRequestRoot</code>)
    * @param defaultPage the resource (page) within an application to use;
    *    must not be <code>null</code> or empty; for example, 
    *    "camain.html?sys_componentname=ca_inbox&sys_pagename=ca_inbox"
    */ 
   public PSDefaultPageRedirectHandler(String fullRequestRoot, 
                                       String defaultPage)
   {
      if (null == fullRequestRoot || null == defaultPage || 
            fullRequestRoot.trim().length() == 0 || 
            defaultPage.trim().length() == 0)
         throw new IllegalArgumentException
               ("Cannot provide empty parameters to constructor");
      
      m_url = fullRequestRoot + "/" + defaultPage;
   }
   
   // see IPSRequestHandler
   public void processRequest(PSRequest request)
   {
      try
      {
         request.getResponse().sendRedirect(m_url, request);
      }
      catch (IOException e)
      {
         PSConsole.printMsg("Server", 
               "Could not process default page redirect request",
               new String[] { e.toString() } );
      }      
   }

   // see IPSRequestHandler
   public void shutdown()
   {
      // nothing to do
   }
   
  
   /**
    * Stores the redirection URL to which this handler will send requests.
    * Never <code>null</code> or empty.
    */ 
   private String m_url = "/index.htm"; 
}
