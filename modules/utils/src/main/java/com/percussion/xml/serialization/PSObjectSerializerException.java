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
package com.percussion.xml.serialization;

/**
 * Exception that is thrown due to any irrecoverable error during serialization
 * and deserialization of objects. This is a nested exception and will have
 * information about the chain of exceptions.
 * 
 * @version 6.0
 * @created 09-Sep-2005 2:49:09 PM
 */
public class PSObjectSerializerException extends Exception
{
   /**
    * Auto generated serialization id.
    */
   private static final long serialVersionUID = 4176547846912708878L;

   /**
    * Delegates to base class version.
    */
   public PSObjectSerializerException()
   {
   }

   /**
    * Delegates to base class version.
    * 
    * @see java.lang.Exception#Exception(java.lang.String)
    */
   public PSObjectSerializerException(String msg)
   {
      super(msg);
   }

   /**
    * Delegates to base class version.
    * 
    * @see Exception#Exception(java.lang.Throwable)
    */
   public PSObjectSerializerException(Throwable nestedException)
   {
      super(nestedException);
   }

   /**
    * Delegates to base class version.
    * 
    * @see Exception#Exception(java.lang.String, java.lang.Throwable)
    */
   public PSObjectSerializerException(String msg, Throwable nestedException)
   {
      super(msg, nestedException);
   }
}
