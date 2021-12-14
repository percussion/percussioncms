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

package com.percussion.services.publisher;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.publisher.data.PSContentList;
import com.percussion.services.publisher.data.PSEditionType;
import com.percussion.utils.guid.IPSGuid;

import java.util.Map;

/**
 * Represents a content list from the database. A content list feeds a list
 * of assembly items into the publishing system. Legacy content lists run
 * a query resource to create a content list XML. New content lists consist
 * of:
 * <ul>
 * <li>A generator that creates the initial set of content items
 * <li>A filter, which removed inappropriate items such as items that have
 * not yet reached a public state
 * <li>A template expander, which takes each qualified item and finds the 
 * templates that should be used when publishing
 * </ul>
 * The content list execution results in a set of assembly items. 
 * 
 * @author dougrand
 */
public interface IPSContentList extends IPSCatalogItem, Cloneable
{   
   /**
    * The type of content list, which dictates how it will be processed.
    */
   public enum Type
   {
      /**
       * Normal processing, pass all items from the template expander
       */
      NORMAL("Normal"), 
      /**
       * Incremental processing, only pass items that must be published 
       * due to changes or calculated content
       */
      INCREMENTAL("Incremental");
      
      private String m_label;
      
      /**
       * Create a type with the specified label.
       * @param label the label of this type.
       */
      private Type(String label)
      {
         m_label = label;
      }
      
      /**
       * Get the label of this type.
       * @return the label, never <code>null</code> or empty.
       */
      public String getLabel()
      {
         return m_label;
      }
      
      /**
       * Lookup value by ordinal
       * 
       * @param ordinal the ordinal
       * @return the matching enum value, or Normal as a default
       */
      public static Type valueOf(int ordinal)
      {
         for (Type t : values())
         {
            if (t.ordinal() == ordinal)
            {
               return t;
            }
         }
         return NORMAL;
      }
   }
   
   /**
    * Get the type of the content list. The type of the content list dictates
    * the semantics of the content list. 
    * 
    * @return the type, never <code>null</code>
    * @see Type
    */
   Type getType();
   
   /**
    * Set the type of the content list
    * @param newtype the new type, never <code>null</code>
    */
   void setType(Type newtype);
   
   /**
    * Gets the registered arguments for the generator plugin. The generator
    * is given these arguments, with appropriate overrides.
    * @return a map of parameter name to parameter value, all as strings, may be
    * empty but never <code>null</code>
    */
   Map<String, String> getGeneratorParams();

   /**
    * Set the generator arguments. This method carefully folds the new arguments
    * into the old arguments.
    * 
    * @param newargs the new arguments, never <code>null</code>
    */
   void setGeneratorParams(Map<String, String> newargs);
   
   /**
    * Gets the registered arguments for the expander plugin. The expander
    * is given these arguments, with appropriate overrides.
    * @return a map of parameter name to parameter value, all as strings, may be
    * empty but never <code>null</code>
    */
   Map<String, String> getExpanderParams();

   /**
    * Set the expander arguments. This method carefully folds the new arguments
    * into the old arguments.
    * 
    * @param newargs the new arguments, never <code>null</code>
    */
   void setExpanderParams(Map<String, String> newargs);

   /**
    * Modify the generator parameters by adding the given name and value
    * @param name the param name, never <code>null</code> or empty
    * @param value the parameter value, never <code>null</code> or empty
    */
   void addGeneratorParam(String name, String value);

   /**
    * Remove the given generator parameter
    * @param name the parameter name, never <code>null</code> or empty
    */
   void removeGeneratorParam(String name);

   /**
    * Modify the expander parameters by adding the given name and value
    * @param name the parameter name, never <code>null</code> or empty
    * @param value the parameter value, never <code>null</code> or empty
    */  
   void addExpanderParam(String name, String value);
   
   /**
    * Remove the given expander parameter
    * @param name the parameter name, never <code>null</code> or empty
    */
   void removeExpanderParam(String name);

   /**
    * @return Returns the description.
    */
   String getDescription();

   /**
    * @param description The description to set.
    */
   void setDescription(String description);

   /**
    * @return Returns the editionType.
    */
   PSEditionType getEditionType();

   /**
    * @param editionType The editionType to set.
    */
   void setEditionType(PSEditionType editionType);

   /**
    * @return Returns the expander.
    */
   String getExpander();

   /**
    * @param expander The expander to set.
    */
   void setExpander(String expander);

   /**
    * @return Returns the generator.
    */
   String getGenerator();

   /**
    * @param generator The generator to set.
    */
   void setGenerator(String generator);

   /**
    * @return Returns the name.
    */
   String getName();

   /**
    * @param name The name to set, never <code>null</code> or empty. The name
    * must be unique
    */
   void setName(String name);

   /**
    * The url to invoke the content list.
    * 
    * @return Returns the url, never <code>null</code> or empty.
    */
   String getUrl();

   /**
    * @param url The url to set, never <code>null</code> or empty.
    */
   void setUrl(String url);
   
   /**
    * Get the item filter for this content list. The filter, if present, 
    * limits the results from the generator for new style content lists.
    * <p>
    * Note, the returned object will always be <code>null</code> if the
    * Content List object is not loaded from service layer; otherwise the
    * returned object may not be <code>null</code> if the Content List object
    * is loaded from service layer and {@link #getFilterId()} is not 
    * <code>null</code>.
    * 
    * @return the filter, may be <code>null</code>
    */
   IPSItemFilter getFilter();

   /**
    * Get the ID of the item filter for this content list. The filter, if 
    * present, limits the results from the generator for new style content lists.
    * 
    * @return the item filter ID, may be <code>null</code> if the item filter is
    *    not defined for this Content List.
    */
   IPSGuid getFilterId();
   
   /**
    * Set a new item filter.
    * @param filterId the ID of the new item filter. It may be <code>null</code>
    *    if need to clear the item filter from this object.
    */
   void setFilterId(IPSGuid filterId);
   
   /**
    * Set the item filter
    * @param filter the filter, may be <code>null</code>
    * @deprecated use {@link #setFilterId(IPSGuid)} instead.
    */
   void setFilter(IPSItemFilter filter);

   /**
    * Determines if this is a legacy Content List or not. A legacy Content List
    * does not have item filter, generator or expander.
    * @return <code>true</code> if is a legacy Content List.
    */
   boolean isLegacy();
   
   /**
    * Clone the content list.
    * @return the cloned content list, never <code>null</code>.
    */
   PSContentList clone();
}
