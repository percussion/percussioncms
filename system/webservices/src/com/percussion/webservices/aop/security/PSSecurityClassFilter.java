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
package com.percussion.webservices.aop.security;

import org.springframework.aop.ClassFilter;

/**
 * Filter for WS security AOP processing, accepts classes with package names
 * that start with "com.percussion.webservices" and class names that end with
 * "Ws".
 */
public class PSSecurityClassFilter implements ClassFilter
{

   /* (non-Javadoc)
    * @see org.springframework.aop.ClassFilter#matches(java.lang.Class)
    */
   public boolean matches(Class clazz)
   {
      return (clazz.getPackage() != null && clazz.getPackage().getName()
            .startsWith("com.percussion.webservices"))
            && clazz.getName().endsWith("DesignWs");
   }

}

