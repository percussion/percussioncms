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


/**
 * Provides a subset of information about a catalog object. This represents an
 * object but is not an object itself. Objects implement 
 * {@link com.percussion.services.catalog.IPSCatalogItem}.
 * 
 * @author dougrand
 */
public interface IPSCatalogSummary extends IPSCatalogIdentifier
{
   /**
    * Get the object name.
    * 
    * @return the name, never <code>null</code> or empty.
    */
   String getName();
   
   /**
    * Get the display label of the object which this summary represents. 
    * Defaults to the name if the object does not specify a display label.
    * 
    * @return the objects display label, never <code>null</code> or empty.
    */
   public String getLabel();
   
   /**
    * Get a description for the object.
    * 
    * @return the description, may be <code>null</code> or empty.
    */
   String getDescription();
}
