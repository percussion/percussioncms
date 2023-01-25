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
