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
package com.percussion.servlets;

import com.percussion.services.PSBaseServiceLocator;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Extends Spring's dispatcher servlet to automatically set the main Rhythmyx
 * application context, which must have been initialized before this servlet
 * is initialized. 
 */
public class PSDispatcherServlet extends DispatcherServlet
{
   @Override
   protected void postProcessWebApplicationContext(
      ConfigurableWebApplicationContext wac)
   {
      if (!PSBaseServiceLocator.isInitialized())
         throw new RuntimeException("Base context must be initialized");
      
      PSBaseServiceLocator.addAsParentCtx(wac);
      
      super.postProcessWebApplicationContext(wac);
   }
}

