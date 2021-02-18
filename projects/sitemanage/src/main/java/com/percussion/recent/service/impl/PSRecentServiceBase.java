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

package com.percussion.recent.service.impl;

import com.percussion.recent.dao.impl.PSRecentDao;
import com.percussion.recent.data.PSRecent;
import com.percussion.recent.data.PSRecent.RecentType;
import com.percussion.recent.service.IPSRecentServiceBase;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A CM1 independent implementation of the recent service.
 * This class controls the number of entries stored, but does not
 * clean up or validate the values that are stored in the table.
 * 
 * @author stephenbolton
 *
 */
@Transactional(propagation = Propagation.REQUIRED)
@Component("recentServiceBase")
@Lazy
public class PSRecentServiceBase implements IPSRecentServiceBase
{
    private PSRecentDao recentDao;

    /** (non-Javadoc)
     * @see com.percussion.recent.service.IPSRecentServiceBase#findRecent(java.lang.String, java.lang.String, com.percussion.recent.data.PSRecent.RecentType)
     */
    @Override
    public List<String> findRecent(String user, String siteName, RecentType type)
    {
        List<PSRecent> returnRecents = recentDao.find(user, siteName, type);
        
        List<String> returnValues = new ArrayList<>();

        if (returnRecents != null)
        {
            for (PSRecent recent : returnRecents)
            {
                returnValues.add(recent.getValue());
            }
        }
        return returnValues;
    }

    /* (non-Javadoc)
     * @see com.percussion.recent.service.IPSRecentServiceBase#addRecent(java.lang.String, java.lang.String, com.percussion.recent.data.PSRecent.RecentType, java.lang.String)
     */
    @Override
    public void addRecent(String user, String siteName, RecentType type, String value)
    {
        List<PSRecent> existingRecents = recentDao.find(user, siteName, type);

        // If the most recent is the same as value do not have to update
        // anything.
        if (existingRecents.size() > 0 && existingRecents.get(0).getValue().equals(value))
            return;

        existingRecents = deleteExtraRecents(type, existingRecents, value, true);

        // Add new item with index 0;
        PSRecent newRecent = new PSRecent(user, siteName, type, 0, value);
        List<PSRecent> updatedRecents = new ArrayList<>();
        updatedRecents.add(newRecent);

        // update index of existing values
        for (int i = 0; i < existingRecents.size(); i++)
        {
            PSRecent existing = existingRecents.get(i);
            existing.setOrder(i + 1);
            updatedRecents.add(existing);
        }
        recentDao.saveAll(updatedRecents);
    }

    /* (non-Javadoc)
     * @see com.percussion.recent.service.IPSRecentServiceBase#deleteRecent(java.lang.String, java.lang.String, com.percussion.recent.data.PSRecent.RecentType)
     */
    @Override
    public void deleteRecent(String user, String siteName, RecentType type)
    {
        List<PSRecent> existingRecents = recentDao.find(user, siteName, type);

        if (CollectionUtils.isNotEmpty(existingRecents))
            recentDao.deleteAll(existingRecents);
    }

    /**
     * This method will remove any extra items found in the list of recent items returned so we maintain 
     * the correct number.  It will clean up if we change the maximum values for the recent type.
     * 
     * @param type the type we are working with, to get maximum size
     * @param recents List of recents
     * @param value current value to check if it is already in list
     * @param forAdd Are we adding an item to the list, therefore we need one less item.
     * @return cleaned up list of recent items
     */
    private List<PSRecent> deleteExtraRecents(RecentType type, List<PSRecent> recents, String value, boolean forAdd)
    {
        int numOfElementsToKeep = forAdd ? type.MaxSize() - 1 : type.MaxSize();
        List<PSRecent> toDelete = new ArrayList<>();
        // remove other entries of
        Iterator<PSRecent> it = recents.iterator();
        if (value != null)
        {
            while (it.hasNext())
            {
                PSRecent del = it.next();
                if (del.getValue().equals(value))
                {
                    toDelete.add(del);
                    it.remove();
                }
            }
        }
        if (recents.size() > numOfElementsToKeep)
        {
            toDelete.addAll(recents.subList(numOfElementsToKeep, recents.size()));
            recents = recents.subList(0, numOfElementsToKeep);
        }
        if (!toDelete.isEmpty())
            recentDao.deleteAll(toDelete);
        return recents;
    }

    @Override
    public void deleteRecent(String user, String siteName, RecentType type, List<String> toDelete)
    {
        List<PSRecent> existingRecents = recentDao.find(user, siteName, type);
        Iterator<PSRecent> iterator = existingRecents.iterator();
        while (iterator.hasNext())
        {
            if (!toDelete.contains(iterator.next().getValue()))
                iterator.remove();
        }
        recentDao.deleteAll(existingRecents);
    }

    @Override
    public void renameSiteRecent(String oldSiteName, String newSiteName) {
        List<PSRecent> siteRecents = recentDao.find(null, oldSiteName, null);
        for (PSRecent recent : siteRecents) {
            if (recent.getSiteName().equals(oldSiteName)) {
                recent.setSiteName(newSiteName);
                if (recent.getValue().contains(oldSiteName)) {
                    recent.setValue(recent.getValue().replaceAll(oldSiteName, newSiteName));
                }
            }
        }
        recentDao.saveAll(siteRecents);
    }

    public PSRecentDao getRecentDao() {
        return recentDao;
    }

    @Autowired
    public void setRecentDao(PSRecentDao recentDao) {
        this.recentDao = recentDao;
    }
}
