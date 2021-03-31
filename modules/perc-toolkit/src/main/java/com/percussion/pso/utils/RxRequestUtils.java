/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * com.percussion.pso.utils RxRequestUtils.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.utils;
import javax.servlet.ServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
   private static final Log log = LogFactory.getLog(RxRequestUtils.class);
   
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
