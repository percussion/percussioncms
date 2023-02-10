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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to allow customization of Webserice method behavior
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface IPSWsMethod
{
   /**
    * Indicates that this method be ignored by the web service security 
    * processing.  If ignored, all other properties of this annotation are 
    * ignored as well.
    * 
    * @return <code>true</code> to ignore it, <code>false</code> (the default)
    * to be processed.
    */
   boolean ignore() default(false);
   
   /**
    * Indicates that this method is ignored by the web service authorization
    * processing.
    * 
    * @return <code>true</code> to ignore it, <code>false</code> (the default)
    *    to be processed.
    */
   boolean ignoreAuthorization() default(false);
   
   /**
    * Indicates that any objects filtered from the results should be unlocked
    * if a lock is found.
    * 
    * @return <code>true</code> to unlock the object, <code>false</code> 
    * otherwise.
    */
   boolean unlockOnError() default(true);
}

