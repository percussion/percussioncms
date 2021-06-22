/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.preview;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * A multipart resolver that fixes up the encoding.  
 * The Content Explorer Applet emits non-standard MIME headers, and this
 * can confuse JBoss/Tomcat, so to get around this, we look for any
 * charset that contains a semicolon, and strip off the junk that 
 * follows.   
 *
 * @author DavidBenua
 *
 */
public class MultipartResolverEncoding extends CommonsMultipartResolver
      implements
         MultipartResolver
{

   private static final Logger log = LogManager.getLogger(MultipartResolverEncoding.class);
   /**
    * @see CommonsMultipartResolver#determineEncoding(HttpServletRequest)
    */
   @Override
   protected String determineEncoding(HttpServletRequest request)
   {
      String encoding = super.determineEncoding(request);
      
      if(encoding.contains(";"))
      {
         encoding = StringUtils.substringBefore(encoding, ";");
         log.debug("fixed up encoding {]", encoding);
      }
      return encoding;
   }
   
   
}
