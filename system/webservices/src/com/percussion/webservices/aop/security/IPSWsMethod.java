/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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

