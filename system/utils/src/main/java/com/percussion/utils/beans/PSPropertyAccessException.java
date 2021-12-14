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
package com.percussion.utils.beans;

import org.apache.commons.lang.StringUtils;

/**
 * This exception represents an error with a property access. As it is not
 * an expected error, it derives from runtime so it will not be treated as
 * a checked exception.
 * 
 * @author dougrand
 */
public class PSPropertyAccessException extends RuntimeException
{
   /**
    * 
    */
   private static final long serialVersionUID = -6270051152589740818L;

   /**
    * No-args ctor
    */
   public PSPropertyAccessException() {
      super();
      // TODO Auto-generated constructor stub
   }
   
   /**
    * Ctor
    * @param message message string, never <code>null</code> or empty
    * @param cause the cause, may be <code>null</code>
    */
   public PSPropertyAccessException(String message, Throwable cause) {
      super(message, cause);
      if (StringUtils.isBlank(message))
      {
         throw new IllegalArgumentException("message may not be null or empty");
      }
   }

   /**
    * Ctor
    * @param message message string, never <code>null</code> or empty
    */
   public PSPropertyAccessException(String message) {
      super(message);
      if (StringUtils.isBlank(message))
      {
         throw new IllegalArgumentException("message may not be null or empty");
      }
   }

   /**
    * Ctor
    * @param cause the cause, may be <code>null</code>
    */
   public PSPropertyAccessException(Throwable cause) {
      super(cause);
   }

}
