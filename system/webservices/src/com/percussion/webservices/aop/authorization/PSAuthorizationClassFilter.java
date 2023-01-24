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
package com.percussion.webservices.aop.authorization;

import org.springframework.aop.ClassFilter;

/**
 * Class filter which filters out all webservice design java API implementation 
 * classes.
 */
public class PSAuthorizationClassFilter implements ClassFilter
{
   /* (non-Javadoc)
    * @see org.springframework.aop.ClassFilter#matches(java.lang.Class)
    */
   public boolean matches(Class clazz)
   {
      Package p = clazz.getPackage();
      return p != null && 
         p.getName().startsWith("com.percussion.webservices") && 
         clazz.getName().endsWith("DesignWs");
   }
}

