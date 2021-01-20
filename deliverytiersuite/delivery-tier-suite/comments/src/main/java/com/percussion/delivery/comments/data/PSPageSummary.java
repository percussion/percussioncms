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
package com.percussion.delivery.comments.data;

/**
 * A simple bean class to hold basic page/comment summary info.
 * 
 * @author erikserating
 * 
 */
public class PSPageSummary
{
    private String pagePath;

    private long commentCount;

    private long approvedCount;

    private long newCommentCount;

    /**
    * 
    */
    public PSPageSummary()
    {

    }

    /**
     * @param pagePath
     * @param commentCount
     */
    public PSPageSummary(String pagePath, long commentCount, long approvedCount)
    {
        this.pagePath = pagePath;
        this.commentCount = commentCount;
        this.approvedCount = approvedCount;
    }

    /**
     * @param pagePath
     * @param commentCount
     * @param approvedCount
     * @param newCommentCount
     * 
     */
    public PSPageSummary(String pagePath, long commentCount, long approvedCount, long newCommentCount)
    {
        this.pagePath = pagePath;
        this.commentCount = commentCount;
        this.approvedCount = approvedCount;
        this.newCommentCount = newCommentCount;
    }

    /**
     * @return the pagePath
     */
    public String getPagePath()
    {
        return pagePath;
    }

    /**
     * @param pagePath the pagePath to set
     */
    public void setPagePath(String pagePath)
    {
        this.pagePath = pagePath;
    }

    /**
     * @return the commentCount
     */
    public long getCommentCount()
    {
        return commentCount;
    }

    /**
     * @param commentCount the commentCount to set
     */
    public void setCommentCount(long commentCount)
    {
        this.commentCount = commentCount;
    }

    /**
     * @return the approvedCount
     */
    public long getApprovedCount()
    {
        return approvedCount;
    }

    /**
     * @param approvedCount the approvedCount to set
     */
    public void setApprovedCount(long approvedCount)
    {
        this.approvedCount = approvedCount;
    }

    /**
     * @return the newCommentCount
     */
    public long getNewCommentCount()
    {
        return newCommentCount;
    }

    /**
     * @param newCommentCount the newCommentCount to set
     */
    public void setNewCommentCount(long newCommentCount)
    {
        this.newCommentCount = newCommentCount;
    }

}
