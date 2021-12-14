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
