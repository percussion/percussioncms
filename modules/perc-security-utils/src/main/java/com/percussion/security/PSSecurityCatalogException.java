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

/**
 * Exception class used to indicate errors when cataloging subjects.
 */
public class PSSecurityCatalogException extends Exception
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * Calls {@link Exception#Exception(java.lang.String, java.lang.Throwable)
    * super(message, cause}.
    */
   public PSSecurityCatalogException(String message, Throwable cause)
   {
      super(message, cause);
   }

   /**
    * Calls {@link Exception#Exception(java.lang.String) super(message}.
    */
   public PSSecurityCatalogException(String message)
   {
      super(message);
   }

   /**
    * Calls {@link Exception#Exception(java.lang.Throwable) super(cause}.
    */
   public PSSecurityCatalogException(Throwable cause)
   {
      super(cause);
   }

}

