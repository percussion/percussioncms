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

package com.percussion.delivery.metadata.extractor.data;

import com.percussion.delivery.metadata.IPSMetadataEntry;
import com.percussion.delivery.metadata.IPSMetadataProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Administrator
 * 
 */
public class PSMetadataEntry implements IPSMetadataEntry
{
    private String pagepath;

    private String name;

    private String folder;

    private String linktext;

    private String type;

    private String site;

    private Set<IPSMetadataProperty> properties = new HashSet<>();

    public PSMetadataEntry()
    {

    }

    /**
     * Ctor
     * 
     * @param name the file name, cannot be <code>null</code> or empty.
     * @param folder the folder path of the containing folder without the site
     *            folder. Cannot be <code>null</code> or empty.
     * @param pagepath the path of the file including sitefolder. This is used
     *            as a unique key for the entry. Cannot be <code>null</code> or
     *            empty.
     * @param type
     */
    public PSMetadataEntry(String name, String folder, String pagepath, String type, String site)
    {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("name cannot be null or empty");
        if (folder == null || folder.length() == 0)
            throw new IllegalArgumentException("folder cannot be null or empty");
        if (pagepath == null || pagepath.length() == 0)
            throw new IllegalArgumentException("pagepath cannot be null or empty");
        if (site == null || site.length() == 0)
            throw new IllegalArgumentException("site cannot be null or empty");
        this.name = name;
        this.folder = folder;
        this.type = type;
        this.pagepath = pagepath;
        this.site = site;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the folder
     */
    public String getFolder()
    {
        return folder;
    }

    /**
     * @param folder the folder to set
     */
    public void setFolder(String folder)
    {
        this.folder = folder;
    }

    /**
     * @return the page path
     */
    public String getPagepath()
    {
        return pagepath;
    }

    /**
     * @param path the pagepath to set
     */
    public void setPagepath(String path)
    {
        this.pagepath = path;
    }

    /**
     * @return the linktext
     */
    public String getLinktext()
    {
        return linktext;
    }

    /**
     * @param linktext the linktext to set
     */
    public void setLinktext(String linktext)
    {
        this.linktext = linktext;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return the site
     */
    public String getSite()
    {
        return site;
    }

    /**
     * @param site the site to set
     */
    public void setSite(String site)
    {
        this.site = site;
    }

    /**
     * @return the properties
     */
    public Set<IPSMetadataProperty> getProperties()
    {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Set<IPSMetadataProperty> properties)
    {
        this.properties = properties;
    }

    public void addProperty(IPSMetadataProperty prop)
    {
        this.properties.add(prop);
    }
    
   public String getJson() throws JSONException
   {
      JSONObject json = new JSONObject();

      json.put("folder", this.getFolder());

      json.put("linktext", this.getLinktext());
      json.put("name", this.getName());
      json.put("pagepath", this.getPagepath());
      json.put("site", this.getSite());
      json.put("type", this.getType());
      JSONArray jsonArray = new JSONArray();
      json.put("properties", jsonArray);

      for (IPSMetadataProperty property : this.getProperties())
      {
         JSONObject prop = new JSONObject();
         prop.put("name", property.getName());
         prop.put("value", property.getValue());
         jsonArray.put(prop);
      }

      return json.toString();
   }

   public void clearProperties()
   {
      this.properties.clear();      
   }
}
