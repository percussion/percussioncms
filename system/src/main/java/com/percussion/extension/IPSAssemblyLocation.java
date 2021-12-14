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

