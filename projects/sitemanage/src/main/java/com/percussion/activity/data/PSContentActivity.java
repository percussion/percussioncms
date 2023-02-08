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

package com.percussion.activity.data;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang.StringUtils;

/**
 * This object holds the activity details of the items under named site or site
 * folder or type of assets (like assets that produce resources and that do not
 * produce resources.)
 */
@JsonRootName(value = "ContentActivity")
public class PSContentActivity 
{
    public PSContentActivity()
    {}
    
    public PSContentActivity(String name, int publishedItems, int pendingItems, int newItems, int updatedItems,
	        int archivedItems)    
    {
        setName(name);
        setPublishedItems(publishedItems);
        setPendingItems(pendingItems);
        setNewItems(newItems);
        setUpdatedItems(updatedItems);
        setArchivedItems(archivedItems);
    }
	
    public PSContentActivity(String siteName, String name, int publishedItems, int pendingItems, int newItems,
            int updatedItems, int archivedItems)    
    {
        setSiteName(siteName);
        setName(name);
        setPublishedItems(publishedItems);
        setPendingItems(pendingItems);
        setNewItems(newItems);
        setUpdatedItems(updatedItems);
        setArchivedItems(archivedItems);
    }
    
    public PSContentActivity(String siteName, String path, String name, int publishedItems, int pendingItems, int newItems,
            int updatedItems, int archivedItems)    
    {
        setSiteName(siteName);
        setPath(path);
        setName(name);
        setPublishedItems(publishedItems);
        setPendingItems(pendingItems);
        setNewItems(newItems);
        setUpdatedItems(updatedItems);
        setArchivedItems(archivedItems);
    }    
    
    /**
     * @return the name of the parent site if the activity is for a folder.  May be the same as {@link #getName()} if
     * the activity details are for a site.  Will be <code>null</code> for assets.
     */
    public String getSiteName() 
    {
        return siteName;
    }

    /**
     * Sets the name of the parent site for the activity details.
     * @param siteName may be blank for assets.
     */
    public void setSiteName(String siteName) 
    {
        this.siteName = siteName;
    }
    
    /**
     * @return the name of the site or site folder or type of the assets. Never blank.
     */
    public String getName() 
    {
        return name;
    }

    /**
     * Sets the name of the object for which the activity details are provided.
     * @param name must not be blank.
     */
    public void setName(String name) 
    {
        if(StringUtils.isBlank(name))
            throw new IllegalArgumentException("name must not be blank");
        this.name = name;
    }

    /**
     * @return the total number of items published from the last publishing run under the named site or folder.
     */
    public int getPublishedItems() 
    {
        return publishedItems;
    }
    
    /**
     * @see #getPublishedItems()
     * @param publishedItems
     */
    public void setPublishedItems(int publishedItems) 
    {
        this.publishedItems = publishedItems;
    }
    
    /**
     * @return the total number of items in pending state as of now under the named site or site folder or type of assets.
     */
    public int getPendingItems() {
        return pendingItems;
    }
    
    /**
     * @see #getPendingItems()
     * @param pendingItems
     */
    public void setPendingItems(int pendingItems) 
    {
        this.pendingItems = pendingItems;
    }
    
    /**
     * @return All items under the named site or site folder or type of assets that do not have live transitions before 
     * the selected duration, but has at least one live transition during the duration.
     */
    public int getNewItems() 
    {
        return newItems;
    }
    
    /**
     * @see #getNewItems()
     * @param newItems 
     */
    public void setNewItems(int newItems) 
    {
        this.newItems = newItems;
    }
    
    /**
     * @return For all items under the named site or site folder or type of assets, number of live transitions of pages 
     * in the duration minus, new pages.
     */
    public int getUpdatedItems() 
    {
        return updatedItems;
    }
    
    /**
     * @see #getUpdatedItems()
     * @param updatedItems
     */
    public void setUpdatedItems(int updatedItems) 
    {
        this.updatedItems = updatedItems;
    }
    
    /**
     * @return For all items under the named site or site folder or type of assets, the number of take down transitions 
     * during the duration.
     */
    public int getArchivedItems() 
    {
        return archivedItems;
    }
    
    /**
     * @see #getArchivedItems()
     * @param archivedItems
     */
    public void setArchivedItems(int archivedItems) 
    {
        this.archivedItems = archivedItems;
    }

    /**
     * @return the path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    //See getters for javadoc
    private String siteName;
    private String name;
    private int publishedItems;
    private int pendingItems;
    private int newItems;
    private int updatedItems;
    private int archivedItems;
    private String path;
}
