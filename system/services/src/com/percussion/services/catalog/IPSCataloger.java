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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.catalog;

import com.percussion.utils.guid.IPSGuid;

import java.util.List;

/**
 * A cataloger enumerates types and allows queries for data. Each type is a kind
 * of object the particular service can handle. Each enumeration provides a full
 * list of identifiers for that kind of object.
 * <p>
 * The cataloger can be used in multiple contexts. It is primarily intended as
 * an interface to aid MSM in deploying objects to and from Rhythmyx
 * installations, but could also be used by import or export applications.
 * 
 * @author dougrand
 */
public interface IPSCataloger
{
   /**
    * A given service handles one or more system types. Only one service should
    * handle any specific type. This method returns an array of these types,
    * which can be used by the deployment system to decide what service handles
    * each type, especially for import.
    * 
    * @return an array of types, never <code>null</code>
    */
   PSTypeEnum[] getTypes();

   /**
    * For a given type, query what the known objects are. This can be used in
    * exporting data or by the deployment system to obtain a list of known data
    * prior to export.
    * 
    * @param type the specific type in question, never <code>null</code>
    * 
    * @return a list of summaries for items of the given type, never
    *         <code>null</code> but may be empty if the given implementation
    *         does not know about the specified type, or the set of items of
    *         that type is empty
    * @throws PSCatalogException if there is a problem while loading the summary
    *            data
    */
   List<IPSCatalogSummary> getSummaries(PSTypeEnum type)
         throws PSCatalogException;

   /**
    * Load the item specified by the given document and store into persistent
    * storage. This may overwrite an object in the persistent store.
    * 
    * @param type the type for the incoming item, never <code>null</code>
    * @param item the XML document as a string representing the item in
    *           question, never <code>null</code> or empty
    * @throws PSCatalogException if there's an error when saving the item to the
    *            repository, or the id type isn't handled by the service
    */
   void loadByType(PSTypeEnum type, String item) throws PSCatalogException;

   /**
    * Save the item specified by the guid into an XML string. The type is taken
    * from the passed guid. The item in question is retrieved from persistent
    * storage as required.
    * 
    * @param id the id of the item in question, never <code>null</code>
    * @return the XML document string representing the item, never
    *         <code>null</code> or empty
    * @throws PSCatalogException if there's an error when loading the item from
    *            the repository, or the id type isn't handled by the service
    */
   String saveByType(IPSGuid id) throws PSCatalogException;
}
