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
package com.percussion.delivery.comments.services;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.percussion.delivery.comments.data.IPSComment;
import com.percussion.delivery.comments.data.PSCommentCriteria;
import com.percussion.delivery.comments.data.PSComments;
import com.percussion.delivery.comments.data.PSPageSummaries;
import com.percussion.delivery.comments.data.IPSComment.APPROVAL_STATE;
import com.percussion.delivery.services.IPSRestService;

/**
 * The comment service is used to store, retrieve and moderate comments. It will run in the
 * delivery tier. 
 * 
 * @author erikserating
 *
 */
public interface IPSCommentsService
{
   /**
    * Retrieves a list of comments for specified criteria.
    * 
    * @param criteria the comment criteria object that specifies the comments to be returned.
    * Cannot be <code>null</code>.
    * @param isModerator a flag indicating that the moderator is viewing these comments. If
    * <code>true</code> then any comments returned by this method call will have their "viewed"
    * flag set to <code>true</code> and persisted.
    * @return list of comments, never <code>null</code>, may be empty.
    */
   public PSComments getComments(PSCommentCriteria criteria, boolean isModerator) throws Exception;
      
   /**
    * Retrieves page summaries of all pages with comments.
    * 
    * @param site the sitename of the pages that have comments. Cannot be <code>null</code> or
    * empty.
    * @param maxResults the maximum number of results to return. If zero or less then all
    * results will be returned.
    * @param startIndex the index offset of results returned, used for paging. If zero
    * or less then start index will be zero.
    * @return a page summaries object, never <code>null</code>, may be empty.
    */
   public PSPageSummaries getPagesWithComments(String site, int maxResults, int startIndex) throws Exception;
   
   /**
    * Retrieves a list of tags found across all comments.
    * 
    * @param maxResults the maximum number of results to return. If zero or less then all
    * results will be returned.
    * @param startIndex the index offset of results returned, used for paging. If zero
    * or less then start index will be zero.
    * @return list of tags, never <code>null</code>, may be empty.
    */
   public List<String> getTags(int maxResults, int startIndex);
   
   /**
    * Adds a comment to the datastore for the specified namespace. Any existing
    * created date or id will be discarded by the service an new ones created when
    * the comment is added. pagePath and site cannot be empty in passed in comment.
    * The implementing class must ensure that both title, text and username are HTML Escaped. 
    * 
    * @param comment the comment object, cannot be <code>null</code>.
    * @return The newly added comment instance. This one has the comment ID inserted
    * in the database.
    */
   public IPSComment addComment(IPSComment comment) throws Exception;   
   
   /**
    * Adds tags to a specified comment.
    * 
    * @param id the comment id ( the persisted id), cannot be <code>null</code> or empty.
    * @param tags set of tags to be added to the comment. Cannot be <code>null</code>,
    * may be empty.
    */
   public void addCommentTags(Long id, Set<String> tags);
   
   /**
    * Approves the specified list of comment IDs. If the specified comments are
    * already approved or if there are no comment with the given IDs, the method
    * quits silently.
    * 
    * @param ids Collection of all comment IDs to be approved. Cannot be
    * <code>null</code>. Maybe empty.
    */
   public void approveComments(Collection<String> ids);
   
   /**
    * Rejects the specified list of comment IDs. If the specified comments are
    * already approved or if there are no comment with the given IDs, the method
    * quits silently.
    * 
    * @param ids Collection of all comment IDs to be rejected. Cannot be
    * <code>null</code>. Maybe empty.
    */
   public void rejectComments(Collection<String> ids);
   
   /**
    * Delete the specified list of comments.
    * @param ids list of all comment ids (persisted ids) to be deleted.
    */
   public void deleteComments(Collection<String> ids);
   
   /**
    * @param sitename the site who's default moderation state we want to retrieve. Cannot
    * be <code>null</code> or empty.
    * @return the current default, never <code>null</code>.
    */
   public APPROVAL_STATE getDefaultModerationState(String sitename);
   
   /**
    * Sets the default moderation state for the specified site. This value will be used for any
    * new comments added to the system.
    * @param sitename the site who's default moderation state we want to set. Cannot
    * be <code>null</code> or empty.
    * @param dflt the approval state default value to set. Cannot be <code>null</code>.
    */
   public void setDefaultModerationState(String sitename, APPROVAL_STATE dflt);

    /**
     * Updates comments to use the new site name after a site in CM1 is renamed.
     *
     * @param prevSiteName the old site name.
     * @param newSiteName the new site name.
     *
     * @return <code>true</code> if the update was successful or there were no updates made.
     *         <code>false</code> if there was an error.
     */
    public boolean updateCommentsForRenameSite(String prevSiteName, String newSiteName);
}
