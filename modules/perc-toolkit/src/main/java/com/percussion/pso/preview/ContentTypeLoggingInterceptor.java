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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Logging of content type and other info. 
 * 
 * This handler can be added to the URL mapping bean in the 
 * Spring configuration. 
 *
 * @author DavidBenua
 *
 */
public class ContentTypeLoggingInterceptor implements HandlerInterceptor
{

   private static final Logger log = LogManager.getLogger(ContentTypeLoggingInterceptor.class);
   
   /**
    * 
    */
   public ContentTypeLoggingInterceptor()
   {
   }
   /**
    * @see HandlerInterceptor#afterCompletion(HttpServletRequest, HttpServletResponse, Object, Exception)
    */
   public void afterCompletion(HttpServletRequest request,
         HttpServletResponse response, Object handler, Exception ex)
         throws Exception
   {
      
   }
   /**
    * @see HandlerInterceptor#postHandle(HttpServletRequest, HttpServletResponse, Object, ModelAndView)
    */
   public void postHandle(HttpServletRequest request,
         HttpServletResponse response, Object handler, ModelAndView modelAndView)
         throws Exception
   {
      
   }
   /**
    * @see HandlerInterceptor#preHandle(HttpServletRequest, HttpServletResponse, Object)
    */
   public boolean preHandle(HttpServletRequest request,
         HttpServletResponse response, Object handler) throws Exception
   {
      log.info("Request is a {}", request.getClass().getCanonicalName());
      ServletRequest innerRequest = request; 
      while(innerRequest instanceof ServletRequestWrapper)
      {
         innerRequest = ((ServletRequestWrapper)innerRequest).getRequest(); 
         log.info("   Wrapping request {}", innerRequest.getClass().getCanonicalName());
      }
      log.info("Content type is {}", request.getContentType());
      log.info("Character set is {}", request.getCharacterEncoding());
      return true; 
   }
}
