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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.percussion.delivery.comments.data.IPSComment.APPROVAL_STATE;

/**
 * A small data class representing comment criteria to find comments
 * with.
 * @author erikserating
 *
 */
public class PSCommentCriteria
{
	private static Log log = LogFactory.getLog(PSCommentCriteria.class);
			
   private String sortby;
    private String ascending;
    private String callback;


   private String pagepath;
   
   /**
    * The user that created the comments to be returned. May be <code>null</code> or empty.
    */
   private String username;
   
   /**
    * A tag string that exists on the comments to be returned. May be <code>null</code> or empty.
    */
   private String tag;
   
  /**
   * Sort object specifying sort order for results. May be <code>null</code>. 
   */
   private PSCommentSort sort;
   
   /**
    * The approval state to filter by. May be <code>null</code> in which case comments
    * in any state may be returned. 
    */
   private APPROVAL_STATE state;
   
   /**
    * The name of the site to filter by. May be <code>null</code>, in which case comments
    * from every site will be returned.
    */
   private String site;
   
   /**
    * Flag indicating that the comment has been actively moderated. May
    * be <code>null</code>.
    */
   private Boolean moderated;
   
   /**
    * Flag indicating that the comment has been previously viewed by an admin or moderator. May
    * be <code>null</code>.
    */
   private Boolean viewed;
   
   /**
    * The maximum number of results to return. If zero or less then all
    * results will be returned.
    */
   private int maxResults;
   
   /**
    * The index offset of results returned, used for paging. If zero
    * or less then start index will be zero.
    */
   private int startIndex;
   
   /**
    * The id of the last comment added, used for returning the last comment added. If zero
    * or less then last comment Id will be zero.
    */
   private String lastCommentId;
   
   /**
    * @return the pagepath
    */
   public String getPagepath()
   {
      return pagepath;
   }
   /**
    * @param pagepath the pagepath to set
    */
   public void setPagepath(String pagepath)
   {
      this.pagepath = pagepath;
   }
   /**
    * @return the username
    */
   public String getUsername()
   {
      return username;
   }
   /**
    * @param username the username to set
    */
   public void setUsername(String username)
   {
      this.username = username;
   }
   
   /**
     * @return the tag
     */
    public String getTag() {
        return tag;
    }
    /**
     * @param tag the tag to set
     */
    public void setTag(String tag) {
        this.tag = tag;
    }
    /**
    * @return the sort
    */
   public PSCommentSort getSort()
   {
      return sort;
   }
   /**
    * @param sort the sort to set
    */
   public void setSort(PSCommentSort sort)
   {
      this.sort = sort;
   }
   /**
    * @return the state
    */
   public APPROVAL_STATE getState()
   {
      return state;
   }
   /**
    * @param state the state to set
    */
   public void setState(APPROVAL_STATE state)
   {
      this.state = state;
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
     * @return the maxResults
     */
   public int getMaxResults()
   {
      return maxResults;
   }
   /**
    * @param maxResults the maxResults to set
    */
   public void setMaxResults(int maxResults)
   {
      this.maxResults = maxResults;
   }
   /**
    * @return the startIndex
    */
   public int getStartIndex()
   {
      return startIndex;
   }
   /**
    * @param startIndex the startIndex to set
    */
   public void setStartIndex(int startIndex)
   {
      this.startIndex = startIndex;
   }
   /**
    * @return the moderated
    */
   public Boolean isModerated()
   {
      return moderated;
   }
   /**
    * @param moderated the moderated to set
    */
   public void setModerated(Boolean moderated)
   {
      this.moderated = moderated;
   }
   /**
    * @return the viewed
    */
   public Boolean isViewed()
   {
      return viewed;
   }
   /**
    * @param viewed the viewed to set
    */
   public void setViewed(Boolean viewed)
   {
      this.viewed = viewed;
   }   
   
   /**
    * @return the id of the last comment
    */
   public String getLastCommentId()
   {
       return lastCommentId;
   }

   /**
    * @param lastCommentId the id of the last comment to set
    */
   public void setLastCommentId(String lastCommentId)
   {
       this.lastCommentId = lastCommentId;
   }

   /***
    * Convenience method that returns the a JSON string representing this instance. 
    * 
    * @return A JSON formatted string, "" if the de-serialization fails. 
    */
   public String toJSON(){
	   String ret = "";
	   ObjectMapper mapper = new ObjectMapper();
	   
	   try{
	   ret = mapper.writeValueAsString(this);
	   } catch (IOException e) {
		log.warn("Error deserializing to JSON.", e);
	}
       return ret;
   }

    /**
     * The relative path of the page not including the site. May be <code>null</code> or empty.
     */
    public String getSortby() {
        return sortby;
    }

    public void setSortby(String sortby) {
        this.sortby = sortby;
    }

    public String getAscending() {
        return ascending;
    }

    public void setAscending(String ascending) {
        this.ascending = ascending;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }
}
