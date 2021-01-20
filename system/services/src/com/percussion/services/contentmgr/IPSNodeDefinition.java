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
package com.percussion.services.contentmgr;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.utils.guid.IPSGuid;

import java.util.Set;

import javax.jcr.nodetype.NodeDefinition;

/**
 * Expanded JSR-170 node definition that allows data update, plus expanded
 * information for Rhythmyx
 * <p>
 * Node def names are returned using JSR-170 names with a prepended "rx:" and
 * embedded spaces converted to underscore characters. Those wishing to work
 * with the "raw" names need to use the get and set internal name methods.
 * 
 * @author dougrand
 */
public interface IPSNodeDefinition extends NodeDefinition, IPSCatalogItem
{
   /**
    * @return Returns the description.
    */
   String getDescription();

   /**
    * @param description The description to set.
    */
   void setDescription(String description);

   /**
    * @return Returns the hideFromMenu.
    */
   Boolean getHideFromMenu();

   /**
    * @param hideFromMenu The hideFromMenu to set, never <code>null</code>.
    */
   void setHideFromMenu(Boolean hideFromMenu);

   /**
    * @return Returns the objectType.
    */
   Integer getObjectType();

   /**
    * @param objectType The objectType to set.
    */
   void setObjectType(Integer objectType);

   /**
    * @param name The name to set.
    */
   void setName(String name);

   /**
    * Get the internal name
    * @return the internal name, does not perform any transformation,
    * never <code>null</code> or empty in a valid definition
    */
   String getInternalName();
   
   /**
    * Set the name without performing any transformation
    * @param name the new name, never <code>null</code> or empty
    */
   void setInternalName(String name);
   
   /**
    * @return Returns the template guids as a read-only set.
    */
   Set<IPSGuid> getVariantGuids();
   
   /**
    * Add a template guid to the list
    * @param guid the guid, never <code>null</code>
    */
   void addVariantGuid(IPSGuid guid);
   
   /**
    * Remove a template guid to the list
    * @param guid the guid, never <code>null</code>
    */   
   void removeVariantGuid(IPSGuid guid);
   
   /**
    * @return Returns the workflow guids as a read-only set.
    */
   Set<IPSGuid> getWorkflowGuids();
   
   /**
    * Get the label.
    * 
    * @return Returns the label.
    */
   public String getLabel();

   /**
    * Set the label.
    * 
    * @param label The label to set.
    */
   public void setLabel(String label);   
   
   /**
    * Gets the query request URL, which can be used to retrieve the content
    * of an item.
    * 
    * @return the query request URL.
    */
   public String getQueryRequest();   
}
