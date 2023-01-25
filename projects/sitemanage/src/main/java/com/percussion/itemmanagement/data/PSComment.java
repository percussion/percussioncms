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
