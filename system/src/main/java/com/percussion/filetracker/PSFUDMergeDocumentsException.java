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

package com.percussion.filetracker;

/**
 * This exception is thrown if the configuartion document is invalid because of,
 * for example, there are no server aliases defined at all.
 */
public class PSFUDMergeDocumentsException extends Exception
{
   /**
    * Default constructor
    */
   public PSFUDMergeDocumentsException()
   {
      super();
   }
   /**
    * Constructor that takes the error message as parameter
    *
    * @param msg as String
    *
    */
   public PSFUDMergeDocumentsException(String msg)
   {
      super(msg);
   }

   public  PSFUDMergeDocumentsException(Throwable t){
      super(t);

   }
}
