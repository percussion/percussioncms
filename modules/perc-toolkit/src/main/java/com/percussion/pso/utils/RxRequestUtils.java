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
/*
 * com.percussion.pso.utils RxRequestUtils.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;
import javax.servlet.ServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.percussion.server.IPSRequestContext;

/**
 * Helper methods for accessing Percussion CMS request objects. 
 *
 * @author DavidBenua
 *
 */
public class RxRequestUtils
{
   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(RxRequestUtils.class);
   
   /**
    * Static methods only 
    */
   private RxRequestUtils()
   {
   }
   
   /**
    * Gets the Percussion CMS request for this servlet request.
    * @param req the calling servlet's request
    * @return the Percussion CMS request context or null if this request
    * did not originate in Percussion CMS. 
    */
   public static IPSRequestContext getRequest(ServletRequest req)
   {
      return (IPSRequestContext) req.getAttribute(REQUEST_ATTRIBUTE); 
   }
 
   /**
    * Convience function to get the rx username from a servlet request.
    * @param req
    * @return the user name
    */
   public static String getUserName(ServletRequest req) {
      IPSRequestContext irq = getRequest(req);
      return irq.getUserName();
   }
   
   public static String getSessionId(ServletRequest req)
   {
      IPSRequestContext irq = getRequest(req); 
      if(irq == null)
      {
         throw new IllegalStateException("Percussion CMS Request not found");
      }
      String sessionid = irq.getUserSessionId();
      log.debug("Session ID from request: " + sessionid); 

      String community = (String)irq.getSessionPrivateObject("sys_community");
      log.debug("Community is " + community); 
      return  sessionid; 
   }
   
 
   public static final String REQUEST_ATTRIBUTE = "RX_REQUEST_CONTEXT"; 
}
