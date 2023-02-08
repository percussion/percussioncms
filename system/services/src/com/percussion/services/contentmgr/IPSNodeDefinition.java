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
