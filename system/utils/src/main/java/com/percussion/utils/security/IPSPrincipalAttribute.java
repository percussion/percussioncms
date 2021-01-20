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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.utils.security;

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

