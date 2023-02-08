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

package com.percussion.test;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.lang.StringUtils;
import org.junit.experimental.categories.Category;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Base class for servlet tests which rely on the presence of a running server.
 */
@Category(IntegrationTest.class)
public abstract class PSServletTestCase extends ServletTestCase
{
   @Override
   protected void setUp() throws Exception
   { 
      //config is an instance of 
      //org.apache.cactus.server.ServletConfigWrapper
      //which inherits from javax.servlet.ServletConfig
      ctx = WebApplicationContextUtils.getWebApplicationContext(
            config.getServletContext());
   }
   
   /**
    * Get the bean from the context for the specified name.
    * 
    * @param beanName The name of the bean to locate, may not be blank.
    * 
    * @return The specified bean as an Object.  Must be cast to the appropriate
    *  interface by the caller.
    */
   protected Object getBean(String beanName)
   {
      if (StringUtils.isBlank(beanName))
         throw new IllegalArgumentException("beanName may not be blank");
      
      return ctx.getBean(beanName);
   }
   
   /**
    * Used to access beans which are not part of the Rhythmyx context.
    * Initialized in {@link #setUp()}.
    */
   private WebApplicationContext ctx;
}
