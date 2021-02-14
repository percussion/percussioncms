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
package com.percussion.comments.data;

import static com.percussion.share.dao.PSDateUtils.getDateFromString;
import static com.percussion.share.dao.PSDateUtils.getDateToString;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.percussion.itemmanagement.data.IPSEditableItem;
import com.percussion.share.data.PSAbstractDataObject;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;

/**
 * @author wesleyhirsch
 * 
 */
@XmlRootElement(name = "comments")
public class PSComment extends PSAbstractDataObject implements IPSEditableItem {

    private static final long serialVersionUID = -6525483335618861315L;

    private String id;

    private String commentId;
    private String commentTitle;
    private String commentText;
    private Date commentCreateDate;
    private String commentApprovalState;
    private Boolean commentModerated;
    private Boolean commentViewed;
    private Set<String> commentTags;
    private Integer commentParentId;

    private String siteName;

    private String pagePath;
    private Set<String> pageTags;

    private String userName;
    private String userLinkUrl;
    private String userEmail;

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.itemmanagement.data.IPSEditableItem#getId()
     */
    @XmlElement(name = "_id")
    public String getId() {
        return this.id;
    }

    /**
     * @return The id of the comment on the delivery tier. May be
     *         <code>null</code>.
     */
    @XmlElement(name = "id")
    public String getCommentId() {
        return commentId;
    }

    /**
     * @param commentId
     *            The id of the comment on the delivery tier. Cannot be
     *            <code>null</code>.
     */
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    /**
     * @return The title of the comment. May be <code>null</code>.
     */
    @XmlElement(name = "title")
    public String getCommentTitle() {
        return commentTitle;
    }

    /**
     * @param commentTitle
     *            The title of the comment. Cannot be <code>null</code>.
     */
    public void setCommentTitle(String commentTitle) {
        this.commentTitle = commentTitle;
    }

    /**
     * @return The text from the body of the comment. May be <code>null</code>.
     */
    @XmlElement(name = "text")
    public String getCommentText() {
        return commentText;
    }

    /**
     * @param commentText
     *            The text from the body of the comment. Cannot be
     *            <code>null</code>.
     */
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    /**
     * @return The date/time the comment was received by the delivery tier
     *         server. May be <code>null</code>.
     */
    @XmlElement(name = "createdDate")
    public String getCommentCreateDate() {
        return getDateToString(this.commentCreateDate);
    }

    /**
     * @param commentCreateDate
     *            The date/time the comment was received by the delivery tier
     *            server. Cannot be <code>null</code>.
     */
    public void setCommentCreateDate(String commentCreateDate) {
        Date formattedDate;
        try {
            formattedDate = getDateFromString(commentCreateDate);
        } catch (ParseException e) {
            throw new DataServiceLoadException("Error parsing date in setCommentCreateDate(String commentCreateDate)"
                    + "in com.percussion.comments.data.PSComment", e);
        }
        this.commentCreateDate = formattedDate;
    }

    /**
     * @param commentCreateDate
     *            The date/time the comment was received by the delivery tier
     *            server. Cannot be <code>null</code>.
     */
    public void setCommentCreateDate(Date commentCreateDate) {
        this.commentCreateDate = commentCreateDate;
    }

    /**
     * @return Whether the comment is approved or rejected (or otherwise). May
     *         be <code>null</code>.
     */
    @XmlElement(name = "approvalState")
    public String getCommentApprovalState() {
        return commentApprovalState;
    }

    /**
     * @param commentApprovalState
     *            Whether the comment is approved or rejected (or otherwise).
     *            Cannot be <code>null</code>.
     */
    public void setCommentApprovalState(String commentApprovalState) {
        this.commentApprovalState = commentApprovalState;
    }

    /**
     * @return Whether or not the comment has been moderated yet. May be
     *         <code>null</code>.
     */
    @XmlElement(name = "moderated")
    public Boolean getCommentModerated() {
        return commentModerated;
    }

    /**
     * @param commentModerated
     *            Whether or not the comment has been moderated yet. Cannot be
     *            <code>null</code>.
     */
    public void setCommentModerated(Boolean commentModerated) {
        this.commentModerated = commentModerated;
    }

    /**
     * @return Whether or not the comment has been viewed by a moderator yet.
     *         May be <code>null</code>.
     */
    @XmlElement(name = "viewed")
    public Boolean getCommentViewed() {
        return commentViewed;
    }

    /**
     * @param commentViewed
     *            Whether or not the comment has been viewed by a moderator yet.
     *            Cannot be <code>null</code>.
     */
    public void setCommentViewed(Boolean commentViewed) {
        this.commentViewed = commentViewed;
    }

    /**
     * @return A collection of all tags associated with the given comment. May
     *         be empty, may be <code>null</code>.
     */
    @XmlElement(name = "commentTags")
    public Set<String> getCommentTags() {
        // @TODO Needs to have a better function to read tags on a tag level,
        // rather than on a list level.
        return commentTags;
    }

    /**
     * @param commentTags
     *            A collection of all tags associated with the given comment.
     *            May be empty, cannot be <code>null</code>.
     */
    public void setCommentTags(Set<String> commentTags) {
        // @TODO Needs to have a better function to set/remove tags on a tag
        // level, rather than on a list level.
        this.commentTags = commentTags;
    }

    /**
     * @return The hostname of the site that the comment resides upon. Will
     *         match the Site Name in CMS. May be <code>null</code>.
     */
    @XmlElement(name = "site")
    public String getSiteName() {
        return siteName;
    }

    /**
     * @param siteName
     *            The hostname of the site that the comment resides upon. Must
     *            match the Site Name in CMS. Cannot be <code>null</code>.
     */
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    /**
     * @return The url that the comment is tied to. Relative to the site root,
     *         includes filename, etc.. May be <code>null</code>.
     */
    @XmlElement(name = "pagePath")
    public String getPagePath() {
        return pagePath;
    }

    /**
     * @param pagePath
     *            The url that the comment is tied to. Relative to the site
     *            root, includes filename, etc.. Cannot be <code>null</code>.
     */
    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }

    /**
     * @return A collection of all tags associated with the page that the
     *         comment is on. May be empty, may be <code>null</code>.
     */
    @XmlElement(name = "tags")
    public Set<String> getPageTags() {
        // @TODO Needs to have a better function to read tags on a tag level,
        // rather than on a list level.
        return pageTags;
    }

    /**
     * @param pageTags
     *            A collection of all tags associated with the page that the
     *            comment is on. May be empty, cannot be <code>null</code>.
     */
    public void setPageTags(Set<String> pageTags) {
        // @TODO Needs to have a better function to set/remove tags on a tag
        // level, rather than on a list level.
        this.pageTags = pageTags;
    }

    /**
     * @return The delivery tier's id of the parent comment of this comment. Can
     *         be used for hierarchical threading of comments. May be
     *         <code>null</code>.
     */
    @XmlElement(name = "parent")
    public Integer getCommentParentId() {
        return commentParentId;
    }

    /**
     * @param commentParentId
     *            The delivery tier's id of the parent comment of this comment.
     *            Can be used for hierarchical threading of comments. Cannot be
     *            <code>null</code>.
     */
    public void setCommentParentId(Integer commentParentId) {
        this.commentParentId = commentParentId;
    }

    /**
     * @return The username of the person posting the comment. May be
     *         <code>null</code>.
     */
    @XmlElement(name = "username")
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName
     *            The username of the person posting the comment. Cannot be
     *            <code>null</code>.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return The URL that the poster of the comment has chosen to include.
     *         Should be valid and fully qualified, may be <code>null</code>.
     */
    @XmlElement(name = "url")
    public String getUserLinkUrl() {
        return userLinkUrl;
    }

    /**
     * @param userLinkUrl
     *            The URL that the poster of the comment has chosen to include.
     *            Must be valid and fully qualified, cannot be <code>null</code>
     *            .
     */
    public void setUserLinkUrl(String userLinkUrl) {
        this.userLinkUrl = userLinkUrl;
    }

    /**
     * @return The email of the user who posted the comment. May be
     *         <code>null</code>.
     */
    @XmlElement(name = "email")
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * @param userEmail
     *            The email of the user who posted the comment. Cannot be
     *            <code>null</code>.
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.itemmanagement.data.IPSEditableItem#getType()
     */
    public String getType() {
        // TODO ASSET_TYPE is wrong. Need to find out what type this is, and
        // report it appropriately.
        return IPSEditableItem.ASSET_TYPE;
    }

}
