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

package com.percussion.itemmanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotEmpty;

/**
 * Encapsulates revision information for a page or asset including revision id, last time it was modified,
 * who modified it last, and its current state
 */
@XmlRootElement(name="Comment")
public class PSComment extends PSAbstractDataObject
{
    String comment;
    @NotEmpty
    String commenter;
    @NotEmpty
    String commentType;
    @NotEmpty
    Date commentDate;

    /**
     * Default constructor. For serializers.
     */
    public PSComment()
    {
    }
    
    /**
     * Constructs an instance of the class.
     * 
     * @param comment actual comment may be blank
     * @param commenter the user who commented, never blank
     * @param commentType the operation on which user commented, never blank
     * @param commentDate date of comment, never blank
     */
    public PSComment(String comment, String commenter, String commentType, Date commentDate)
    {
    	setComment(comment);
    	setCommenter(commenter);
    	setCommentType(commentType);
    	setCommentDate(commentDate);
    }

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCommenter() {
		return commenter;
	}

	public void setCommenter(String commenter) {
		this.commenter = commenter;
	}

	public String getCommentType() {
		return commentType;
	}

	public void setCommentType(String commentType) {
		this.commentType = commentType;
	}

	public Date getCommentDate() {
		return commentDate;
	}

	public void setCommentDate(Date commentDate) {
		this.commentDate = commentDate;
	}
}
