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
package com.percussion.utils.spring;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * Provides access to the matching path pattern used by the
 * {@link PSWebDavServletController} to delegate to the
 * {@link PSPassThruDispatcherServlet}. Capture the mapping of the path pattern
 * to a dispatch handler during initialization and stores it in a static member.
 * At runtime, when the Spring framework uses the mapping to lookup the request
 * dispatcher, the path that was matched on is stored in thread local storage,
 * and the {@link #getUrlPath()} method then provides access to that path. This
 * way the <code>PSWebdavServlet</code> can call this method to determine the
 * base path to which the request was matched, and that can be compared to the
 * request URL from the servlet request to determine the non-base portion of
 * that path.
 * <p>
 * The special handler <q>[default]</q> is used for non-matching paths when
 * matching in {@link #lookupHandler(String, HttpServletRequest)}. 
 */
public class PSUrlHandlerMapping extends SimpleUrlHandlerMapping
{
   /**
    * Delegates to base class method and then builds a map of handlers to 
    * url path pattern.  See base class method for more info.
    */
   @Override
   protected void registerHandler(String urlPath, Object handler) 
      throws BeansException
   {
      super.registerHandler(urlPath, handler);
      if (handler instanceof String)
      {
         String handlerName = (String) handler;
         if (getApplicationContext().isSingleton(handlerName))
         {
            // handler is instance of PSWebDavServletController
            handler = getApplicationContext().getBean(handlerName);
         }
      }
      ms_handlerMap.put(handler, urlPath);
   }

   /**
    * Delegates to the base class method and then uses the resulting handler to
    * get the url path pattern from the map built during
    * {@link #registerHandler(String, Object)}. Stores the result in thread
    * local storage. See base class details for more info.
    */
   @Override
   protected Object lookupHandler(String urlPath, HttpServletRequest req)
      throws Exception
   {
      Object handler = super.lookupHandler(urlPath, req);
      if (handler instanceof HandlerExecutionChain)
      {
         // get the handler object to execute, which should be an instance of 
         // PSWebDavServletController. See registerHandler(String, Object)
         handler = ((HandlerExecutionChain) handler).getHandler();
      }
      ms_urlPath.set(ms_handlerMap.get(handler));
      
      if (handler == null)
      {
         handler = super.lookupHandler("/[default]", req);
      }
      
      return handler;
   }
   
   /**
    * Provides access to the path stored for the current thread by the last call
    * on this thread to {@link #lookupHandler(String, HttpServletRequest)}.
    * 
    * @return The path, could be <code>null</code> or empty if
    *         {@link #lookupHandler(String, HttpServletRequest)} has not been
    *         called, or if somehow no handler was registered for the specified
    *         path, but if the system is configured properly and this method is
    *         called by invocation of the <code>PSWebdavServlet</code> through
    *         this framework, then it cannot be <code>null</code> or empty.
    */
   public static String getUrlPath()
   {
      return ms_urlPath.get();
   }
   
   /**
    * Map of handlers to url path patterns, never <code>null</code>.
    * The map key is an instance of {@link PSWebDavServletController}
    * The map value is the URL of the (servlet) handler (that is the map key).
    */
   private static Map<Object, String> ms_handlerMap = 
      new HashMap<>();
   
   /**
    * Stores most recently match url path pattern for a thread of execution,
    * never <code>null</code>.
    */
   private static ThreadLocal<String> ms_urlPath = new ThreadLocal<>();

}

