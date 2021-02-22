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
package com.percussion.share.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import net.sf.oval.constraint.NotEmpty;

import java.util.Collection;

/**
 * This class contains a set of known properties of an item.
 */
@JsonRootName(value = "ItemProperties")
public class PSItemProperties extends PSAbstractPersistantObject
{
    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 1L;

    /**
     * See {@link #getId()} for detail.
     */
    private String id;

    /**
     * See {@link #getName()} for detail.
     */
    private String name;

    /**
     * See {@link #getStatus()} for detail.
     */
    private String status;

    /**
     * See {@link #getWorkflow()} for detail.
     */
    private String workflow;

    /**
     * See {@link #getLastModifier()} for detail.
     */
    private String lastModifier;

    /**
     * See {@link #getLastModifiedDate()} for detail.
     */
    private String lastModifiedDate;

    /**
     * See {@link #getLastPublishedDate()} for detail.
     */
    private String lastPublishedDate;

    /**
     * See {@link #getType()} for detail.
     */
    private String type;

    /**
     * See {@link #getPath()} for detail.
     */
    @NotEmpty
    private String path;

    @NotEmpty
    private String summary;

    /**
     * See {@link #getAuthor()} for detail.
     */
    private String author;

    /**
     * See {@link #getTags()} for detail.
     */
    private Collection<String> tags;

    /**
     * See {@link #getCommentsCount()} for detail.
     */
    private int commentsCount;

    /**
     * See {@link #getNewCommentsCount()} for detail.
     */
    private int newCommentsCount;

    /**
     * See {@link #getSize()} for detail.
     */
    private String size;


    /**
     * See {@link #getPostDate()} for detail.
     */
    private String postDate;

    /**
     * See {@link #getScheduledPublishDate()} for detail.
     */
    private String scheduledPublishDate;

    /**
     * See {@link #getSheduledUnpublishDate()} for detail.
     */
    private String scheduledUnpublishDate;

    /**
     * See {@link #getThumbnailPath()} for detail.
     */
    private String thumbnailPath;

    /**
     * Gets the ID of the item.
     *
     * @return item ID, not blank for a valid item.
     */
    @Override
    public String getId()
    {
        return id;
    }

    /**
     * Sets item ID.
     *
     * @param id the new ID, not blank for a valid item.
     */
    @Override
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Gets the name of the item. It is the name of the item for an asset, but
     * it is the link text for a page.
     *
     * @return the name, not blank for a valid item.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the item name, which is the name of the item for an asset, but
     * the link text for a page.
     *
     * @param name the new name, not blank for a valid item.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the (workflow) state name.
     *
     * @return the state name. It may be blank if the item is not workflowable
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * Sets the (workflow) state name.
     *
     * @param stateName the new state name. It may be blank if the item is
     * not workflowable.
     */
    public void setStatus(String stateName)
    {
        this.status = stateName;
    }

    /**
     * Gets the workflow name.
     *
     * @return the workflow name. It may be blank if the item is not workflowable
     */
    public String getWorkflow()
    {
        return workflow;
    }

    /**
     * Sets the workflow name.
     *
     * @param workflowName the new workflow name. It may be blank if the item is
     * not workflowable.
     */
    public void setWorkflow(String workflowName)
    {
        this.workflow = workflowName;
    }

    /**
     * Gets the user name who modified the item last.
     * <p>
     * Note, this is the last user who modified the item, checked in/out the item;
     * but not the user who (workflow) transitioned the item.
     *
     * @return the user name, not blank for a valid item. It may be 
     * {@link #SYSTEM_USER} if the item was modified by the system.
     */
    public String getLastModifier()
    {
        return lastModifier;
    }

    /**
     * Sets the user name who modified the item last.
     *
     * @param user the user name, should not be blank for a valid item.
     */
    public void setLastModifier(String user)
    {
        this.lastModifier = user;
    }

    /**
     * Gets the last date/time the item has been modified.
     * <p>
     * Note, this is the last date/time the item has been modified, checked in/out;
     * but not the date/time the item was (workflow) transitioned.
     *
     * @return the modified date and time, not blank for a valid item.
     */
    public String getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    /**
     * Sets the last modified date and time.
     *
     * @param date the new date and time, not blank for a valid item.
     *
     * @see #getLastModifiedDate()
     */
    public void setLastModifiedDate(String date)
    {
        this.lastModifiedDate = date;
    }

    /**
     * The last published date of the item.
     *
     * @return last published date. It may be blank if the item has not been
     * published.
     */
    public String getLastPublishedDate()
    {
        return lastPublishedDate;
    }

    /**
     * Sets the last successful published date.
     *
     * @param date the new last published date, may be blank if the item has
     * not been published.
     */
    public void setLastPublishedDate(String date)
    {
        lastPublishedDate = date;
    }

    /**
     * Gets the type of the item.
     *
     * @return the content type for an asset; template name for a page, not
     * blank for a valid asset or page.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Sets the type of the item.
     *
     * @param type the new type. It is the content type for an asset;
     * or template name for a page, not blank for a valid asset or page.
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Gets the (finder) path of the item.
     *
     * @return the path. It may be <code>null</code>, never empty.
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Sets the (finder) path of the item.
     *
     * @param path the (finder) path.  It may be <code>null</code>, never empty.
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * @return the summary
     */
    public String getSummary()
    {
        return summary;
    }

    /**
     * @param summary the summary to set
     */
    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    /**
     * @return the author of the page
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * @param author the author to set for the page
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }

    /**
     * @return the page tags
     */
    public Collection<String> getTags()
    {
        return tags;
    }

    /**
     * @param tags the page tags to set
     */
    public void setTags(Collection<String> tags)
    {
        this.tags = tags;
    }

    /**
     * @return the total number of comments
     */
    public int getCommentsCount()
    {
        return commentsCount;
    }

    /**
     * @param commentsCount the total number of comments to set
     */
    public void setCommentsCount(int commentsCount)
    {
        this.commentsCount = commentsCount;
    }

    /**
     * @return the number of new comments
     */
    public int getNewCommentsCount()
    {
        return newCommentsCount;
    }

    /**
     * @param newCommentsCount the number of new comments to set
     */
    public void setNewCommentsCount(int newCommentsCount)
    {
        this.newCommentsCount = newCommentsCount;
    }

    /**
     * Gets the size of the file in the File System
     *
     * @return the size in KB.
     */
    public String getSize()
    {
        return size;
    }

    /**
     * Sets the size of the file
     *
     * @param size the size of the file expressed in KB
     */
    public void setSize(String size)
    {
        this.size = size;
    }

    /**
     * @return the post date of the item
     */
    public String getPostDate()
    {
        return postDate;
    }

    /**
     * @param postDate the post date to set for the item
     */
    public void setPostDate(String postDate)
    {
        this.postDate = postDate;
    }

    /**
     * Sets the size of the file
     *
     * @return the publishDate of the item
     */
    public String getScheduledPublishDate()
    {
        return scheduledPublishDate;
    }

    /**
     * @param the publishDate of the item
     */
    public void setScheduledPublishDate(String scheduledPublishDate)
    {
        this.scheduledPublishDate = scheduledPublishDate;
    }

    /**
     * Sets the size of the file
     *
     * @return the unpublishDate of the item
     */
    public String getScheduledUnpublishDate()
    {
        return scheduledUnpublishDate;
    }

    /**
     * @param the unpublishDate of the item
     */
    public void setScheduledUnpublishDate(String scheduledUnpublishDate)
    {
        this.scheduledUnpublishDate = scheduledUnpublishDate;
    }

    /**
     * @return the thumbnailPath
     */
    public String getThumbnailPath()
    {
        return thumbnailPath;
    }

    /**
     * @param thumbnailPath the thumbnailPath to set
     */
    public void setThumbnailPath(String thumbnailPath)
    {
        this.thumbnailPath = thumbnailPath;
    }

    /**
     * A user name of the system, which is used when an item is modified
     * by the system. 
     */
    public static final String SYSTEM_USER = "System";

    private String contentPostDateTz;

    public String getContentPostDateTz() {
        return contentPostDateTz;
    }

    public void setContentPostDateTz(String contentPostDateTz) {
        this.contentPostDateTz = contentPostDateTz;
    }
}
