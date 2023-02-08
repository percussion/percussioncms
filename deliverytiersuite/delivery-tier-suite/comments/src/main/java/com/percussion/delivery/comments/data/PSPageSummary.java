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
