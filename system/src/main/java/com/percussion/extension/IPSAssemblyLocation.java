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
package com.percussion.extension;

import com.percussion.server.IPSRequestContext;

/**
 * This interface specifies the functionality to be implemented by all
 * content assembler location generators.
 * The implementing classes should not be called directly, instead they will
 * be called from the PSGeneratePubLocation for the current context and 
 * contenttype.
 */
public interface IPSAssemblyLocation extends IPSExtension
{
   /**
    * This method creates a new publishing location string for the provided
    * parameters.
    *
    * @param params an array of parameters as specified in the 
    *    RXLOCATIONSCHEME table for the current context and contenttype, nut
    *    <code>null</code>, might be empty.
    * @param request the request for which the location needs to be created,
    *    not <code>null</code>.
    * @throws PSExtensionException to report any error in the execution of
    *    the implementing class.
    */
   public String createLocation(Object[] params, IPSRequestContext request)
           throws PSExtensionException;
   
   /**
    * Supported parameter type String, never <code>null</code>.
    */
   public static final String TYPE_STRING = "String";
   
   /**
    * Supported parameter type BackendColumn, never <code>null</code>.
    */
   public static final String TYPE_BACKEND_COLUMN = "BackendColumn";
   
   /**
    * Supported parameter type Passthrough, never <code>null</code>.
    */
   public static final String TYPE_PASSTHROUGH = "Passthrough";
}

