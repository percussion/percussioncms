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
package com.percussion.webservices;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This exception describes errors that happend in web service calls.
 */
public class PSErrorException extends RuntimeException
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 1599438698822519375L;
   
   /**
    * The error code.
    */
   private int code;
   
   /**
    * The error message, never <code>null</code> or empty after construction.
    */
   private String errorMessage;
   
   /**
    * The stack trace from where the happened, never <code>null</code> or empty
    * after construction. 
    */
   private String stack;
   
   /**
    * Default constructor.
    */
   public PSErrorException()
   {
   }
   
   /**
    * Construct the error from an error message and exception.
    * 
    * @param message the error message. 
    * @param cause the cause of the error.
    */
   public PSErrorException(String message, Throwable cause)
   {
      super(message, cause);
      setErrorMessage(message);
   }
      
   /**
    * Construct a new error exception for the supplied parameters.
    * 
    * @param code the error code.
    * @param errorMessage the error message, may be <code>null</code> or empty.
    * @param stack the stack trace from where the error happened, not 
    *    <code>null</code> or empty.
    */
   public PSErrorException(int code, String errorMessage, String stack)
   {
      setCode(code);
      setErrorMessage(errorMessage);
      setStack(stack);
   }
   
   public PSErrorException(String errorMsg)
   {
      setErrorMessage(errorMsg);
   }
   
   /**
    * Construct a new error exception for the supplied parameters.
    * 
    * @param code the error code.
    * @param errorMessage the error message, may be <code>null</code> or empty.
    * @param stack the stack trace from where the error happened, not 
    *    <code>null</code> or empty.
    * @param e The original exception that needs to be set as cause.
    */
   public PSErrorException(int code, String errorMessage, String stack, Exception e)
   {
      super(e);
      if(errorMessage == null && e!= null)
         errorMessage = e.getLocalizedMessage();
      setCode(code);
      setErrorMessage(errorMessage);
      setStack(stack);
   }

   /**
    * Get the error code.
    * 
    * @return the error code uniquely identifies a specific error.
    */
   public int getCode()
   {
      return code;
   }
   
   /**
    * Set a new error code.
    * 
    * @param code the new error code.
    */
   public void setCode(int code)
   {
      this.code = code;
   }
   
   /**
    * Get the error message.
    * 
    * @return the error message, never <code>null</code> or empty.
    */
   public String getErrorMessage()
   {
      return errorMessage;
   }
   
   /**
    * Set a new error message.
    * 
    * @param errorMessage the new error message, not <code>null</code> or empty.
    */
   public void setErrorMessage(String errorMessage)
   {
      if (StringUtils.isBlank(errorMessage))
         throw new IllegalArgumentException(
            "errorMessage cannot be null or empty");
      
      this.errorMessage = errorMessage;
   }
   
   /*
    *  (non-Javadoc)
    * @see java.lang.Throwable#getMessage()
    */
   @Override
   public String getMessage()
   {
      return getErrorMessage();
   }
   
   /*
    *  (non-Javadoc)
    * @see java.lang.Throwable#getLocalizedMessage()
    */
   @Override
   public String getLocalizedMessage()
   {
      return getMessage();
   }
   
   /**
    * Get the stack trace where the exception happened.
    * 
    * @return the stack trace, never <code>null</code> or empty.
    */
   public String getStack()
   {
      return stack;
   }
   
   /**
    * Set a new stack trace where the error happened.
    * 
    * @param stack the new stack trace, not <code>null</code> or empty.
    */
   public void setStack(String stack)
   {
      if (StringUtils.isBlank(stack))
         throw new IllegalArgumentException("stack cannot be null or empty");
      
      this.stack = stack;
   }

   @Override
   public boolean equals(Object b)
   {
      if (!(b instanceof PSErrorException))
         return false;
      
      PSErrorException exception = (PSErrorException) b;
      EqualsBuilder builder = new EqualsBuilder();
      builder.append(code, exception.code);
      builder.append(errorMessage, exception.errorMessage);
      builder.append(stack, exception.stack);

      return builder.isEquals();
   }

   @Override
   public int hashCode()
   {
      HashCodeBuilder builder = new HashCodeBuilder();
      builder.append(code);
      builder.append(errorMessage);
      builder.append(stack);
      
      return builder.hashCode();
   }
}

