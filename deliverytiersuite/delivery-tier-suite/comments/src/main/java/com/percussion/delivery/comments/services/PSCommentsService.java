/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.delivery.comments.services;

import com.percussion.delivery.comments.data.IPSComment;
import com.percussion.delivery.comments.data.IPSComment.APPROVAL_STATE;
import com.percussion.delivery.comments.data.PSCommentCriteria;
import com.percussion.delivery.comments.data.PSCommentSort;
import com.percussion.delivery.comments.data.PSCommentSort.SORTBY;
import com.percussion.delivery.comments.data.PSComments;
import com.percussion.delivery.comments.data.PSPageInfo;
import com.percussion.delivery.comments.data.PSPageSummaries;
import com.percussion.delivery.comments.data.PSPageSummary;
import com.percussion.delivery.comments.service.rdbms.PSComment;
import com.percussion.delivery.listeners.IPSServiceDataChangeListener;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author erikserating
 * 
 */
public class PSCommentsService implements IPSCommentsService
{
    /**
     * Logger for this class
     */
    public static Log log = LogFactory.getLog(PSCommentsService.class);
    
    private IPSCommentsDao dao;

    private List<IPSServiceDataChangeListener> listeners = new ArrayList<IPSServiceDataChangeListener>();
    private final String[] PERC_COMMENTS_SERVICES = {"perc-comments-services"};
    
    
    @Autowired
    public PSCommentsService(IPSCommentsDao dao) {
		this.dao = dao;
	}

	/**
     * Map to get the PSComment fields given a SORTBY value.
     */
    public static final Map<SORTBY, String> SORTBY_FIELD_MAPPING = new HashMap<PSCommentSort.SORTBY, String>()
    {
        {
            put(SORTBY.CREATEDDATE, "createdDate");
            put(SORTBY.EMAIL, "email");
            put(SORTBY.USERNAME, "username");
        }
    };

    /**
     * The amount of minutes during the ones a comment recently made will remain
     * visible.
     */
    public static final int AMOUNT_MINUTES_COMMENT_VISIBLE = 1;

    private static PSProfanityFilter profanityFilter = new PSProfanityFilter();

    /**
     * Adds a new comment in the database.
     * Notifies listeners of changes in comments so that cache regions can be flushed.
     * 
     * @param comment Comment to add. Must not be <code>null</null>.
     * May be any implementation of IPSComment interface.
     */
    public IPSComment addComment(IPSComment comment)
    {
        String siteName = comment.getSite();
        HashSet<String> siteSet = new HashSet<String>(1);
        siteSet.add(siteName);
        this.fireDataChangeRequestedEvent(siteSet);
        
        if (StringUtils.isBlank(comment.getPagePath()) || StringUtils.isBlank(comment.getSite()))
        {
            throw new IllegalArgumentException("pagepath and site cannot be null or empty");
        }
        log.info("Adding a new comment");

        
        comment.setTitle(StringEscapeUtils.escapeHtml(comment.getTitle()));
        comment.setText(StringEscapeUtils.escapeHtml(comment.getText()));
        comment.setUsername((StringEscapeUtils.escapeHtml(comment.getUsername())));

        APPROVAL_STATE modState = getDefaultModerationState(comment.getSite());
        if (modState == APPROVAL_STATE.APPROVED)
        {
            if (comment.getText() != null && profanityFilter.containsProfanity(comment.getText()))
            {
                modState = APPROVAL_STATE.REJECTED;
            }
        }

        if (modState == APPROVAL_STATE.APPROVED)
        {
            if (comment.getTitle() != null && profanityFilter.containsProfanity(comment.getTitle()))
            {
                modState = APPROVAL_STATE.REJECTED;
            }
        }

        comment.setApprovalState(modState);
        comment.setCreatedDate(Calendar.getInstance().getTime());

        try
        {
            PSComment comm = new PSComment(comment);
            dao.save(comm);
            comment.setId(comm.getId());
            log.info("Comment successfully added");

            return comment;
        }
        catch (Exception ex)
        {
            log.error("Error in adding a new comment: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
        finally
        {
            this.fireDataChangedEvent(siteSet);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.comments.services.IPSCommentsService#addCommentTags(java
     * .lang.String, java.util.Set)
     */
    public void addCommentTags(Long id, Set<String> tags)
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.comments.services.IPSCommentsService#approveComments(java
     * .util.List)
     */
    public void approveComments(Collection<String> commentIds)
    {
        Validate.notNull(commentIds);

        if (commentIds.isEmpty())
            return;

        log.info("Approving comments with the following IDs: " + commentIds.toString());
        moderateComments(commentIds, APPROVAL_STATE.APPROVED);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.comments.services.IPSCommentsService#rejectComments(java
     * .util.List)
     */
    public void rejectComments(Collection<String> commentIds)
    {
        Validate.notNull(commentIds);

        if (commentIds.isEmpty())
            return;

        log.info("Rejecting comments with the following IDs: " + commentIds.toString());
        moderateComments(commentIds, APPROVAL_STATE.REJECTED);
    }

    /**
     * Moderate the comments with the given IDs and approval state.
     * Notifies listeners of changes in comments so that cache regions can be flushed.
     * 
     * @param commentIds A list of comment IDs to moderate.
     * @param newApprovalState The new approval state for the given comments.
     */
    private void moderateComments(Collection<String> commentIds, APPROVAL_STATE newApprovalState)
    {
        Set<String> siteNames = new HashSet<String>();
        try
        {
        	siteNames = dao.findSitesForCommentIds(commentIds);
            this.fireDataChangeRequestedEvent(siteNames);
        	dao.moderate(commentIds, newApprovalState);
        }
        catch (Exception ex)
        {
            log.error("Error in moderating comments: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
        finally
        {
            this.fireDataChangedEvent(siteNames);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.comments.services.IPSCommentsService#deleteComments(java
     * .util.List)
     */
    public void deleteComments(Collection<String> commentIds)
    {
        Validate.notNull(commentIds);
        
        if (commentIds.size() == 0)
        {
            log.info("Comment IDs list is empty.");
            return;
        }
        
        

        log.info("Deleting comments with the following IDs: " + commentIds.toString());

        Set<String> siteNames = new HashSet<String>();
        try
        {
        	siteNames = dao.findSitesForCommentIds(commentIds);
            this.fireDataChangeRequestedEvent(siteNames);
        	dao.delete(commentIds);
        }
        catch (Exception ex)
        {
            log.error("Error in deleting comments: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
        finally
        {
            this.fireDataChangedEvent(siteNames);
        }
    }    

    /**
     * Gets all the comments according to the given criteria.
     * 
     * @param criteria The criteria object.
     * @return A PSComments object which has a comment list.
     */
    @SuppressWarnings("unchecked")
    public PSComments getComments(PSCommentCriteria criteria, boolean isModerator)
    {
        log.info("Getting all comments according to the given criteria object");

        List<IPSComment> comments = new ArrayList<IPSComment>();

       
        try
        {
            List<IPSComment> result = dao.find(criteria);

            for (IPSComment com : result)
            {
                if (!isModerator && APPROVAL_STATE.REJECTED.equals(com.getApprovalState()))
                {
                    Calendar currentDate = Calendar.getInstance();
                    Calendar commentDate = Calendar.getInstance();
                    commentDate.setTime(com.getCreatedDate());

                    // Get the represented date in milliseconds
                    long milis1 = currentDate.getTimeInMillis();
                    long milis2 = commentDate.getTimeInMillis();

                    // Calculate difference in milliseconds
                    long diff = milis1 - milis2;

                    // Calculate difference in minutes
                    long diffMinutes = diff / (60 * 1000);

                    if (diffMinutes <= AMOUNT_MINUTES_COMMENT_VISIBLE)
                    {
                        comments.add(com);
                    }
                }
                else
                {
                    comments.add(com);
                }
            }

            // If 'isModerator', update the 'viewed' flag of fetched comments
            if (isModerator)
            {
                for (IPSComment com : result)
                {
                    markAsViewed(com);
                }
            }
        }
        catch (Exception ex)
        {
            log.error("Error in getting comments by criteria: " + ex.getMessage());
            throw new RuntimeException(ex);
        }        

        return new PSComments(comments);
    }
    
    /**
     * Mark comment as viewed by moderator.
     * @param comment
     * @throws Exception
     */
    private void markAsViewed(IPSComment comment) throws Exception
    {
        if (!comment.isViewed())
        {
            comment.setViewed(true);
            dao.save(comment);
        }
    }

    

    /**
     * Get a page summary list (the ones with comments) according the site and
     * paging information (maxResult and startIndex).
     * 
     * @param site The site of the comments.
     * @param maxResults The maximum number of pages to return.
     * @param startIndex Specifies the offset of the first page to return.
     * @return A PSPageSummaries object with the list of pages with comments.
     */
    @SuppressWarnings("unchecked")    
    public PSPageSummaries getPagesWithComments(String site, int maxResults, int startIndex)
    {
        Validate.notEmpty(site);

        log.info("Getting all pages with comments");

        List<PSPageSummary> pageSummaries = new ArrayList<PSPageSummary>();

        
        try
        {
        	List<PSPageInfo> result = dao.findPagesWithComments(site);

            pageSummaries = createPageSummaries(result);

            int startIndexProcessed = startIndex > 0 ? startIndex : 0;
            int maxResultProcessed = maxResults > 0 ? maxResults : pageSummaries.size();

            int fromIndex = startIndexProcessed * maxResultProcessed;
            int toIndex = (startIndexProcessed * maxResultProcessed) + maxResultProcessed;
            if (toIndex > pageSummaries.size())
                toIndex = pageSummaries.size();

            if (pageSummaries.size() > 0)
                pageSummaries = pageSummaries.subList(fromIndex, toIndex);
        }
        catch (Exception ex)
        {
            log.error("Error in getting pages with comments: " + ex.getMessage());
            throw new RuntimeException(ex);
        }        

        return new PSPageSummaries(pageSummaries);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.comments.services.IPSCommentsService#getDefaultModerationState
     * (java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public APPROVAL_STATE getDefaultModerationState(String site)
    {
        try
        {
            APPROVAL_STATE state = dao.findDefaultModerationState(site);
            return state;

        }
        catch (Exception ex)
        {
            log.error("Error getting default moderation state: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.comments.services.IPSCommentsService#setDefaultModerationState
     * (java.lang.String,
     * com.percussion.comments.data.IPSComment.APPROVAL_STATE)
     */
    public void setDefaultModerationState(String sitename, APPROVAL_STATE dflt)
    {
        
        try
        {
            dao.saveDefaultModerationState(sitename, dflt);
        }
        catch (Exception ex)
        {
            log.error("Error setting default moderation state: " + ex.getMessage());
            throw new RuntimeException(ex);
        }       

    }

    /**
     * Given a list of Object arrays with information of a custom HQL query,
     * creates a list of PSPageSummary objects.
     * 
     * @param pagePathSummaryQuery page info object.
     * @return A List of PSPageSummary objects.
     */
    private List<PSPageSummary> createPageSummaries(List<PSPageInfo> pagePathSummaryQuery)
    {
        List<PSPageSummary> pageSummaries = new ArrayList<PSPageSummary>();

        // Create a Map with the pagepath as key, and a Long array with the
        // approved
        // comments count in the first position, and unapproved ones in the
        // second
        // position.
        Map<String, CommentCount> pagepathAndCommentsCountMap = new HashMap<String, CommentCount>();
        CommentCount pagepathWithCommentCount;

        for (PSPageInfo tmp : pagePathSummaryQuery)
        {
            String realPagepath = tmp.getPagePath();
            String lowerCasedPagepath = realPagepath.toLowerCase();
            String approvalState = tmp.getApprovalState();
            Long count = (Long) tmp.getCommentCount();
            boolean viewed = tmp.isViewed();
            if (!pagepathAndCommentsCountMap.containsKey(lowerCasedPagepath))
            {
                pagepathWithCommentCount = new CommentCount(realPagepath);
                pagepathAndCommentsCountMap.put(lowerCasedPagepath, pagepathWithCommentCount);
            }
            else
            {
                pagepathWithCommentCount = pagepathAndCommentsCountMap.get(lowerCasedPagepath);
            }

            if (approvalState.equals(APPROVAL_STATE.APPROVED.toString()))
                pagepathWithCommentCount.approvedCount += count;
            else
                pagepathWithCommentCount.unapprovedCount += count;
            if (!viewed)
                pagepathWithCommentCount.newComments += count;
        }

        // Create the PSPageSummary objects list using the Map object generated
        // above.
        long commentCount;
        long approvedCount;
        long newCommentsCount;
        for (String pagePath : pagepathAndCommentsCountMap.keySet())
        {
            pagepathWithCommentCount = pagepathAndCommentsCountMap.get(pagePath);
            commentCount = 0;
            approvedCount = 0;
            newCommentsCount = 0;

            // Check if we have information about approved comments for this
            // pagepath
            if (pagepathWithCommentCount.approvedCount != null)
            {
                commentCount += pagepathWithCommentCount.approvedCount;
                approvedCount = pagepathWithCommentCount.approvedCount;
            }

            // Check if we have information about unapproved comments for this
            // pagepath
            if (pagepathWithCommentCount.unapprovedCount != null)
            {
                commentCount += pagepathWithCommentCount.unapprovedCount;
            }

            if (pagepathWithCommentCount.newComments != null)
            {
                newCommentsCount = pagepathWithCommentCount.newComments;
            }

            pageSummaries.add(new PSPageSummary(pagepathWithCommentCount.pagepath, commentCount, approvedCount,
                    newCommentsCount));
        }

        // Sort the list by pagepath in ascending order
        Collections.sort(pageSummaries, new Comparator<PSPageSummary>()
        {
            public int compare(PSPageSummary o1, PSPageSummary o2)
            {
                return o1.getPagePath().compareTo(o2.getPagePath());
            }
        });

        return pageSummaries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.comments.services.IPSCommentsService#getTags(int,
     * int)
     */
    public List<String> getTags(int maxResults, int startIndex)
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see com.percussion.metadata.IPSMetadataIndexerService#addMetadataListener(com.percussion.metadata.event.IPSMetadataListener)
     */
    public void addMetadataListener(IPSServiceDataChangeListener listener)
    {
        Validate.notNull(listener, "listener cannot be null.");
        if(!listeners.contains(listener))
            listeners.add(listener);
        
    }

    /* (non-Javadoc)
     * @see com.percussion.metadata.IPSMetadataIndexerService#removeMetadataListener(com.percussion.metadata.event.IPSMetadataListener)
     */
    public void removeMetadataListener(IPSServiceDataChangeListener listener)
    {
        Validate.notNull(listener, "listener cannot be null.");
        if(listeners.contains(listener))
            listeners.remove(listener);
    }

    /**
     * Fire a data change event for all registered listeners.
     */
    private void fireDataChangedEvent(Set<String> sites)
    {
        if(sites == null || sites.size() == 0)
        {
            return;
        }

        for(IPSServiceDataChangeListener listener : listeners)
        {
            listener.dataChanged(sites, this.PERC_COMMENTS_SERVICES);
        }
    }

    /**
     * Fire a data change event for all registered listeners.
     */
    private void fireDataChangeRequestedEvent(Set<String> sites)
    {
        if(sites == null || sites.size() == 0)
        {
            return;
        }

        for(IPSServiceDataChangeListener listener : listeners)
        {
            listener.dataChangeRequested(sites, this.PERC_COMMENTS_SERVICES);
        }
    }

    @Override
    public boolean updateCommentsForRenameSite(String prevSiteName,
                                               String newSiteName) {
        PSCommentCriteria criteria = new PSCommentCriteria();
        criteria.setSite(prevSiteName);
        try {
            List<IPSComment> comments = dao.find(criteria);
            for (IPSComment comment : comments) {
                comment.setSite(newSiteName);
                try {
                    dao.save(comment);
                } catch (Exception e) {
                    log.error("Error updating comment with id: "
                            + comment.getId()
                            + " An administrator should attempty to update the comment manually.");
                }
            }
        } catch (Exception e) {
            log.error("Error finding comments for site: " + prevSiteName, e);
            return false;
        }
        return true;
    }
}

/**
 * Small class to represent a real pagepath (not the lowercased one) and the
 * amount of approved and unapproved comments posted there.
 * 
 * @author miltonpividori
 * 
 */
class CommentCount
{
    String pagepath;

    Long approvedCount;

    Long unapprovedCount;

    Long newComments;

    CommentCount(String pagepath)
    {
        this.pagepath = pagepath;
        this.approvedCount = 0L;
        this.unapprovedCount = 0L;
        this.newComments = 0L;
    }
}
