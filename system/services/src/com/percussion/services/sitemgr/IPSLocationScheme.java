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

package com.percussion.services.sitemgr;


import com.percussion.services.catalog.IPSCatalogIdentifier;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;


/**
 * The location scheme represents how a content item should be represented 
 * in a URL or in a directory tree. The location scheme creates the "path"
 * to the item. For the URL this is appended onto the site root, and for
 * file system publishing this is appended to the publishing root.
 * 
 * @author dougrand
 *
 */
public interface IPSLocationScheme extends IPSCatalogIdentifier
{
   /**
    * Get the content type id
    * @return Returns the contentTypeId, may not be <code>null</code> for a 
    *    valid instance; otherwise it may be <code>null</code> if it is just 
    *    created by {@link IPSSiteManager#createScheme()}.
    */
   public abstract Long getContentTypeId();

   /**
    * @param contentTypeId The contentTypeId to set, never <code>null</code>
    */
   public abstract void setContentTypeId(Long contentTypeId);

   /**
    * Get the publishing context
    * @return Returns the context. It is <code>null</code> unless this object
    *    is returned from {@link IPSSiteManager#loadScheme(IPSGuid)} or the
    *    {@link #setContext(IPSPublishingContext)} was called previously.
    * 
    * @deprecated use {@link #getContextId()} instead.
    */
   public abstract IPSPublishingContext getContext();

   /**
    * @param context The context to set.
    * 
    * @deprecated use {@link #setContextId(IPSGuid)} instead.
    */
   public abstract void setContext(IPSPublishingContext context);


   /**
    * Get the publishing context
    * @return Returns the context ID, may not be <code>null</code> for a 
    *    valid instance; otherwise it may be <code>null</code> if it is just 
    *    created by {@link IPSSiteManager#createScheme()}. 
    */
   public abstract IPSGuid getContextId();

   /**
    * @param contextId The context ID to set.
    */
   public abstract void setContextId(IPSGuid contextId);

   /**
    * Get the description
    * @return Returns the description, may be <code>null</code> or empty
    */
   public abstract String getDescription();

   /**
    * @param description The description to set.
    */
   public abstract void setDescription(String description);

   /**
    * Get the generator name. This name can be used with the extensions manager
    * to load the location scheme.
    * @return Returns the generator, never <code>null</code> or empty
    */
   public abstract String getGenerator();

   /**
    * @param generator The generator to set, never <code>null</code> or empty
    */
   public abstract void setGenerator(String generator);

   /**
    * Get the name of this scheme
    * @return Returns the name, may be <code>null</code> or empty
    */
   public abstract String getName();

   /**
    * @param name The name to set.
    */
   public abstract void setName(String name);

   /**
    * Get the parameter names
    * @return Returns the parameter names, may be empty but never
    * <code>null</code>
    */
   public abstract List<String> getParameterNames();
   
   /**
    * Get the parameter value
    * @param name the name of the parameter, never <code>null</code> or empty
    * @return the value, may be <code>null</code> if not defined
    */
   public abstract String getParameterValue(String name);
   
   /**
    * Get the parameter type
    * @param name the name of the parameter, never <code>null</code> or empty
    * @return the type, may be <code>null</code> if not defined
    */
   public abstract String getParameterType(String name);   

   /**
    * Get the parameter sequence
    * @param name the name of the parameter, never <code>null</code> or empty
    * @return the sequence, may be <code>null</code> if not defined
    */
   public abstract Integer getParameterSequence(String name);   
   
   /**
    * Adds a parameter on the location scheme. This acts as a convenience method
    * that calls {@link #addParameter(String, int, String, String)} with the
    * values of name, 0, type and value.
    * 
    * @param name the name of the parameter, never <code>null</code> or empty
    * @param type the type of the parameter, either "String" or "BackendColumn",
    * never <code>null</code> or empty
    * @param value the value, never <code>null</code> or empty
    */
   public abstract void setParameter(String name, String type, String value);
   
   /**
    * Add a parameter to the location scheme
    * 
    * @param name the name of the parameter, never <code>null</code> or empty
    * @param sequence sequence, a number <code>&gt;= 0</code>, used to order
    *           the parameter. The parameter will be inserted immediately before
    *           the nth existing parameter. If the sequence is after the
    *           existing indices then the parameter is appended. If the
    *           parameter is already known (determined by the name, case
    *           sensitive) then this method will just set the type and value,
    *           but will not move the parameter in the list.
    * @param type the type of the parameter, either "String" or "BackendColumn",
    *           never <code>null</code> or empty
    * @param value the value, never <code>null</code> or empty
    */
   public abstract void addParameter(String name, int sequence, 
         String type, String value);
   
   /**
    * Remove the named parameter on the location scheme
    * 
    * @param name the name of the parameter, never <code>null</code> or empty
    */
   public abstract void removeParameter(String name);

   /**
    * Get the id of the associated template. 
    * @return Returns the templateId, may not be <code>null</code> for a valid
    *    instance; otherwise it may be <code>null</code> if it is just 
    *    created by {@link IPSSiteManager#createScheme()}.
    */
   public abstract Long getTemplateId();

   /**
    * @param templateId The templateId to set, never <code>null</code>
    */
   public abstract void setTemplateId(Long templateId);

   /**
    * Performs a deep copy of the params, but not the context.
    * 
    * @return a clone of this scheme, never <code>null</code>.
    * @throws CloneNotSupportedException Never.
    */
   public Object clone() throws CloneNotSupportedException;
   
   /**
    * Copy all properties from a given Location Scheme, except its ID 
    * {@link #getGUID()} and its internal version number if there is any.
    * 
    * @param other the to be copied Location Scheme, never <code>null</code>. 
    */
   public void copy(IPSLocationScheme other);
}
