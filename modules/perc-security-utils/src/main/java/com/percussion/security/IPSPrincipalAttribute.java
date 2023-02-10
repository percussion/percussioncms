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
package com.percussion.security;

import java.security.Principal;
import java.util.List;

/**
 * Repesents a named attribute of a subject that may be retrieved by a call to
 * {@link javax.security.auth.Subject#getPrincipals(Class) 
 * Subject.getPrincipals(IPSPrincipalAttribute)}. {@link Principal#getName()} is
 * called to get the name of the attribute, and {@link #getValues()} is used to
 * get the value. {@link #getAttributeType()} defines the type of attribute,
 * e.g. what the attribute defines, since the name may be arbitrary.
 * <p>
 * For example, one cataloger may provide the email address in an attribute
 * named "email", while another may provide it in an attribute name "mailto".
 * Both attributes should return {@link PrincipalAttributes#EMAIL_ADDRESS} from
 * calls to {@link #getAttributeType()}.  
 */
public interface IPSPrincipalAttribute extends Principal
{
   /**
    * Types of attributes that can be returned in a subject's collection of 
    * principals.
    */
   public enum PrincipalAttributes 
   {
      /**
       * The name used to match on the subject, can be returned to allow the
       * caller to correlate the returned subjects with the requested names. If
       * multiple values are returned, the first one will be used. If multiple
       * attributes of this type are found in the subject's principals, it is
       * undefined which will be used.
       */
      SUBJECT_NAME,
      
      /**
       * The attribute type representing the subject's email address. Support
       * for this is required for email notification. Multiple values and/or
       * multiple attributes of this type may be returned, and all will be used.
       */
      EMAIL_ADDRESS,
      
      /**
       * The attribute type representing an attribute for which no type has been
       * defined.  Used to expose attributes specific to the implementation.
       */
      ANY;
   }  
   
   /**
    * Get the values of the attribute.
    * 
    * @return The values, never <code>null</code>, may be empty.
    */
   public List<String> getValues();
   
   /**
    * Get the type of the attribute.
    * 
    * @return One of the types specified by the enum.  
    */
   public PrincipalAttributes getAttributeType();
}

