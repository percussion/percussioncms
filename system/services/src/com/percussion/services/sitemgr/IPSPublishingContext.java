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

package com.percussion.services.sitemgr;

import com.percussion.utils.guid.IPSGuid;

/**
 * The publishing context controls how links are generated in the HTML documents
 * during assembly.
 * 
 * @author dougrand
 */
public interface IPSPublishingContext
{
   /**
    * Get the global unique id for this item
    * 
    * @return the globally unique id, never <code>null</code>. See
    * {@link IPSGuid}for more information.
    */
   public IPSGuid getGUID();

   /**
    * Get the description of the context, may be <code>null</code> or empty
    * 
    * @return Returns the description.
    */
   public abstract String getDescription();

   /**
    * @param description The description to set, may be <code>null</code> or
    *           empty
    */
   public abstract void setDescription(String description);

   /**
    * Get the name of the context
    * 
    * @return Returns the name, never <code>null</code> or empty
    */
   public abstract String getName();

   /**
    * @param name The name to set, never <code>null</code> or empty
    */
   public abstract void setName(String name);

   /**
    * Get the default location scheme ID.
    * 
    * @return Returns the defaultScheme ID, may be <code>null</code> if not 
    *    defined.
    */
   public abstract IPSGuid getDefaultSchemeId();

   /**
    * @param defaultSchemeId The defaultScheme ID to set, may be 
    *    <code>null</code>.
    */
   public abstract void setDefaultSchemeId(IPSGuid defaultSchemeId);

   /**
    * @return a clone of this site, never <code>null</code>.
    * @throws CloneNotSupportedException Never.
    */
   Object clone() throws CloneNotSupportedException;
   
   /**
    * Get the database id for this location scheme
    * 
    * @return Returns the id, never <code>null</code> after the scheme is
    *         persisted.
    * @deprecated Use {@link #getGUID()}
    */
   public abstract Integer getId();

   /**
    * @param id The id to set.
    * @deprecated Use {@link IPSSiteManager#createContext()} to create a new
    * context with a generated id.
    */
   public abstract void setId(Integer id);
   
   /**
    * Get the default location scheme.
    * 
    * @return Returns the defaultScheme, may be <code>null</code>. It is
    *    always <code>null</code> unless this object is returned from
    *    {@link IPSSiteManager#loadContext(IPSGuid)} and
    *    {@link #getDefaultSchemeId()} is not <code>null</code> or
    *    {@link #setDefaultScheme(IPSLocationScheme)} was called previously.
    */
   public abstract IPSLocationScheme getDefaultScheme();

   /**
    * Set the default Location Scheme.
    * Note, this can only set the ID of the default Location Scheme, but not the
    * Location Scheme itself. See {@link #setDefaultSchemeId(IPSGuid)}
    * 
    * @param defaultScheme The defaultScheme to set, may be <code>null</code>
    *           or empty
    *           
    * @deprecated use {@link #setDefaultSchemeId(IPSGuid)} instead.
    */
   public abstract void setDefaultScheme(IPSLocationScheme defaultScheme);
   
   /**
    * Copy all properties from a given Publishing Context, except its ID 
    * {@link #getGUID()} and its internal version number if there is any.
    * 
    * @param other the to be copied Publishing Context, never <code>null</code>. 
    */
   public void copy(IPSPublishingContext other);
}
