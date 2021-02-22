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
package com.percussion.delivery.likes.services;

import com.percussion.delivery.comments.services.PSCommentsService;
import com.percussion.delivery.likes.data.IPSLikes;
import com.percussion.delivery.listeners.IPSServiceDataChangeListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class PSLikesService implements IPSLikesService
{
    
	private IPSLikesDao dao;

    private List<IPSServiceDataChangeListener> listeners = new ArrayList<>();
    private final String[] PERC_LIKES_SERVICES = {"perc-likes-services"};

    /**
     * Logger for this class
     */
    public static Log log = LogFactory.getLog(PSCommentsService.class);
    
       
    @Autowired
    public PSLikesService(IPSLikesDao dao)
    {
    	this.dao = dao;
    }

    /**
     * Tally of how many users have Liked a page, a comment.
     * 
     * @param site Must not be <code>null</null>.
     * @param likeId Must not be <code>null</null>.
     * @param type Must not be <code>null</null>. May be any implementation of
     *            IPSLikes interface.
     * 
     */
    public int getTotalLikes(String site, String likeId, String type)
    {
        Validate.notEmpty(site);
        Validate.notEmpty(likeId);
        Validate.notEmpty(type);

        try
        {
        	List<IPSLikes> results = dao.find(site, likeId, type);        
            if(results.isEmpty())
                return 0;
        	return results.get(0).getTotal();
        }
        catch (Exception ex)
        {
            log.error("Error in getting likes by criteria: " + ex.getMessage());
            throw new RuntimeException(ex);
        }        
    }
    
    
    /**
     * To Like a page, a comment.
     * 
     * @param site Must not be <code>null</null>.
     * @param likeId Must not be <code>null</null>.
     * @param type Must not be <code>null</null>. May be any implementation of
     *            IPSLikes interface.
     * 
     * @return int total of likes after of last like.
     */
    public int like(String site, String likeId, String type)
    {
        return likeUnlike(site, likeId, type, true);
    }
    
    /**
     * To UnLike a page, a comment.
     * 
     * @param site Must not be <code>null</null>.
     * @param likeId Must not be <code>null</null>.
     * @param type Must not be <code>null</null>. May be any implementation of
     *            IPSLikes interface.
     * @return updated count.
     */
    public int unlike(String site, String likeId, String type)
    {
    	return likeUnlike(site, likeId, type, false);
    }
    
    /**
     * Method to do the work of liking or unliking an object.
     * @param site . Must not be <code>null</null>.
     * @param likeId . Must not be <code>null</null>.
     * @param type . Must not be <code>null</null>. May be any implementation of
     *            IPSLikes interface.
     * @param isLike if <code>true</code> then this is a like operation.
     * @return updated count.
     */
    private int likeUnlike(String site, String likeId, String type, boolean isLike)
    {
        Validate.notEmpty(site);
        Validate.notEmpty(likeId);
        Validate.notEmpty(type);

        Set<String> sites = new HashSet<>(1);
        sites.add(site);
        fireDataChangeRequestedEvent(sites);

        try
        {
            List<IPSLikes> likes = dao.find(site, likeId, type);
            IPSLikes like = null;
            if (likes.isEmpty())
            {
                if (!isLike) // Cannot decrement no existent like
                    return 0;
                like = dao.create(site, likeId, type);
                like.setTotal(1);
                dao.save(like);
                return like.getTotal();
            }
            else if (isLike)
            {
                return dao.incrementTotal(site, likeId, type);
            }
            else
            {
                return dao.decrementTotal(site, likeId, type);
            }
        }
        catch (Exception ex)
        {
            log.error("Error in getting likes by criteria: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
        finally
        {
            fireDataChangedEvent(sites);
        }
    }
    

     /**
      * 
      * @param listener
      */
    public void addServicedataChangeListener(IPSServiceDataChangeListener listener)
    {
        Validate.notNull(listener, "listener cannot be null.");
        if(!listeners.contains(listener))
            listeners.add(listener);
        
    }

    /* (non-Javadoc)
     * @see com.percussion.metadata.IPSMetadataIndexerService#removeMetadataListener(com.percussion.metadata.event.IPSMetadataListener)
     */
    public void removeServicedataChangeListener(IPSServiceDataChangeListener listener)
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
            listener.dataChanged(sites, this.PERC_LIKES_SERVICES);
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
            listener.dataChangeRequested(sites, this.PERC_LIKES_SERVICES);
        }
    }

    @Override
    public void updateLikesForSiteAfterRename(String prevSiteName,
                                              String newSiteName) {
        List<IPSLikes> likes = new ArrayList<>();
        List<IPSLikes> newLikes = new ArrayList<>();
        try {
            likes = dao.findLikesForSite(prevSiteName);
            for (IPSLikes like : likes) {
                like.setSite(newSiteName);
                newLikes.add(like);
                dao.delete(Collections.singletonList(like.getId()));
            }
            dao.save(likes);
        } catch (Exception e) {
            log.error("Error retrieving likes for site: " + prevSiteName + ". "
                    + "An administrator should atttempty to update the likes table "
                    + "in the DTS database.", e);
            return;
        }

    }
}
