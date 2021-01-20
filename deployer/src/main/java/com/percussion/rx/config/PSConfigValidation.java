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
package com.percussion.rx.config;

import com.percussion.services.catalog.PSTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This class encapsulates an error or warning validation result. This the
 * a result of validating the configuration files in one package against
 * another package.
 *
 * @author YuBingChen
 */
public class PSConfigValidation
{
   /**
    * Creates a validation result.
    * 
    * @param name the name of the validated object, never <code>null</code> or
    * empty.
    * @param propName the name of the property, it may be <code>null</code> or
    * empty.
    * @param isError <code>true</code> if it is an error; otherwise it is a
    * warning.
    * @param message the message of the validation. It may not be 
    * <code>null</code> or empty.
    */
   public PSConfigValidation(String name, String propName, boolean isError, String message)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty.");
      if (StringUtils.isBlank(message))
         throw new IllegalArgumentException("message may not be null or empty.");
      
      m_objName = name;
      m_propertyName = propName;
      m_isError = isError;
      m_message = message;
      m_exception = null;
   }

   /**
    * Construct a validation result from an exception.
    * 
    * @param e the exception caught during validation process, not
    * <code>null</code>.
    */
   public PSConfigValidation(String pkgName, Exception e)
   {
      if (StringUtils.isBlank(pkgName))
         throw new IllegalArgumentException("pkgName may not be blank.");
      if (e == null)
         throw new IllegalArgumentException("Exception cannot be null.");
      
      m_pkgName = pkgName;
      m_exception = e;
   }
   
   /**
    * Gets the validation message.
    * 
    * @return the validation message, never <code>null</code> or empty.
    */
   public String getValidationMsg()
   {
      if (m_exception != null)
      {
         String expMsg = StringUtils
               .isBlank(m_exception.getLocalizedMessage()) ? m_exception
               .toString() : m_exception.getLocalizedMessage();
         return "While verifying package \"" + m_pkgName
               + "\", an error occurred: " + expMsg;
      }
      
      String msgType = m_isError ? "error" : "warning";
      String type = m_objType == null ? "" : " (type=" + m_objType.name() + ")"; 
      if (m_propertyName != null)
      {
         return "While verifying package \"" + m_pkgName
               + "\", found a conflict on property \"" + m_propertyName
               + "\" of design object \"" + m_objName + "\"" + type
               + " in package \"" + m_otherPkgName + "\". The " + msgType
               + " is: " + m_message;
      }
      else
      {
         return "While verifying package \"" + m_pkgName
               + "\", found a conflict on design object \"" + m_objName + "\""
               + type + " in package \"" + m_otherPkgName + "\". The "
               + msgType + " is: " + m_message;
      }
   }
   
   /**
    * Sets the object type.
    * 
    * @param type the object type, not <code>null</code>.
    */
   public void setObjectType(PSTypeEnum type)
   {
      if (type == null)
         throw new IllegalArgumentException("type may not be null.");
      
      m_objType = type;
   }

   /**
    * Gets the name of the validated object.
    * @return the name of the object, never <code>null</code> or empty.
    */
   public String getObjectName()
   {
      return m_objName;
   }

   /**
    * Sets the current package name
    * 
    * @param pkgName the new package name, not <code>null</code> or empty.
    */
   public void setPkgName(String pkgName)
   {
      if (StringUtils.isBlank(pkgName))
         throw new IllegalArgumentException("pkgName may not be null or empty.");
      m_pkgName = pkgName;
   }

   /**
    * Sets the package name that was used to validate against with the
    * current package. 
    * 
    * @param pkgName the other package name, not <code>null</code> or empty.
    */
   public void setOtherPkgName(String otherPkgName)
   {
      if (StringUtils.isBlank(otherPkgName))
         throw new IllegalArgumentException("otherPkgName may not be null or empty.");
      m_otherPkgName = otherPkgName;
   }
   

   /**
    * Gets the type of the validated object.
    * 
    * @return the object type, never <code>null</code>.
    */
   public PSTypeEnum getObjectType()
   {
      return m_objType;
   }
   
   /**
    * Determines if this is an error or warning.
    * 
    * @return <code>true</code> if this is an error.
    */
   public boolean isError()
   {
      return m_isError;
   }
   
   /**
    * Gets the validation message.
    * 
    * @return the message, never <code>null</code> or empty.
    */
   public String getMessage()
   {
      return m_message;
   }
   
   /**
    * Sets the message.
    * @param msg the new message, never <code> or empty.
    */
   public void setMessage(String msg)
   {
      if (StringUtils.isBlank(msg))
         throw new IllegalArgumentException("msg may not be null or empty.");

      m_message = msg;
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object b)
   {
      if (!(b instanceof PSConfigValidation))
         return false;
      
      PSConfigValidation second = (PSConfigValidation) b;
      return new EqualsBuilder()
         .append(m_objName, second.m_objName)
         .append(m_objType, second.m_objType)
         .append(m_isError, second.m_isError)
         .append(m_message, second.m_message)
         .append(m_propertyName, second.m_propertyName)
         .append(m_pkgName, second.m_pkgName)
         .append(m_otherPkgName, second.m_otherPkgName)
         .isEquals();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {

      return new HashCodeBuilder()
         .append(m_objName)
         .append(m_objType)
         .append(m_isError)
         .append(m_message)
         .append(m_propertyName)
         .append(m_pkgName)
         .append(m_otherPkgName)
         .toHashCode();
   }

   /**
    * The name of the design object, never <code>null</code> after the 
    * constructor.
    */
   private String m_objName;
   
   /**
    * The type of the design object, never <code>null</code> after the
    * constructor.
    */
   private PSTypeEnum m_objType;
   
   /**
    * Determines if the object is an error or warning. It is <code>true</code>
    * for an error, <code>false</code> if it is a warning.
    */
   private boolean m_isError;
   
   /**
    * The name of the current package.
    */
   private String m_pkgName;
   
   /**
    * The name of the package that was validated against with.
    */
   private String m_otherPkgName;
   
   /**
    * The name of the property that was detected error or warning.
    */
   private String m_propertyName;
   
   /**
    * The message of the validation.
    */
   private String m_message;
   
   /**
    * The exception caught during validation.
    */
   private Exception m_exception;
}
