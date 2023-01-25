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
package com.percussion.services.catalog;

import com.percussion.utils.exceptions.PSBaseException;

/**
 * Exception thrown on cataloging operations
 * 
 * @author dougrand
 */
public class PSCatalogException extends PSBaseException
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public PSCatalogException(int msgCode, Object... arrayArgs) {
      super(msgCode, arrayArgs);
      // TODO Auto-generated constructor stub
   }



   public PSCatalogException(int msgCode, Throwable cause, Object... arrayArgs) {
      super(msgCode, cause, arrayArgs);
      // TODO Auto-generated constructor stub
   }



   public PSCatalogException(int msgCode) {
      super(msgCode);
      // TODO Auto-generated constructor stub
   }



   @Override
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.services.catalog.PSCatalogErrorStringBundle";
   }

}
