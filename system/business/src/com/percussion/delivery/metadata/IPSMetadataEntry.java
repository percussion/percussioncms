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

package com.percussion.delivery.metadata;

import java.util.Set;

import org.json.JSONException;

public interface IPSMetadataEntry
{

   /**
    * @return the name
    */
   public String getName();

   /**
    * @param name the name to set
    */
   public void setName(String name);

   /**
    * @return the folder
    */
   public String getFolder();

   /**
    * @param folder the folder to set
    */
   public void setFolder(String folder);

   /**
    * @return the page path
    */
   public String getPagepath();

   /**
    * @param path the pagepath to set
    */
   public void setPagepath(String path);

   /**
    * @return the linktext
    */
   public String getLinktext();

   /**
    * @param linktext the linktext to set
    */
   public void setLinktext(String linktext);

   /**
    * @return the type
    */
   public String getType();

   /**
    * @param type the type to set
    */
   public void setType(String type);

   /**
    * @return the site
    */
   public String getSite();

   /**
    * @param site the site to set
    */
   public void setSite(String site);

   /**
    * @return the properties. This returns a cloned set of properties changing
    *         the value of these directly will not affect the property values in
    *         the entry. To change property values on the entry you must passed
    *         the properties back to the entries {@link #setProperties(Set)}
    *         method.
    */
   public Set<IPSMetadataProperty> getProperties();

   /**
    * @param properties the properties to set
    */
   public void setProperties(Set<IPSMetadataProperty> properties);

   public void addProperty(IPSMetadataProperty prop);

   public void clearProperties();

   public String getJson() throws JSONException;
}
