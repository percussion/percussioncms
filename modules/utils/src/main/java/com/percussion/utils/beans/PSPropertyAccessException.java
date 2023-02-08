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
