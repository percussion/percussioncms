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
