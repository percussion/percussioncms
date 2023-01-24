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

package com.percussion.recent.service.impl;

import com.percussion.recent.dao.IPSRecentDao;
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
    @Autowired
    private IPSRecentDao recentDao;

    public PSRecentServiceBase(IPSRecentDao recentDao) {
        this.recentDao = recentDao;
    }

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
        if (existingRecents.size() > 0 && existingRecents.get(0).getValue().equals(value)) {
            return;
        }

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

        if (CollectionUtils.isNotEmpty(existingRecents)) {
            recentDao.deleteAll(existingRecents);
        }
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
        if (!toDelete.isEmpty()) {
            recentDao.deleteAll(toDelete);
        }
        return recents;
    }

    @Override
    public void deleteRecent(String user, String siteName, RecentType type, List<String> toDelete)
    {
        List<PSRecent> existingRecents = recentDao.find(user, siteName, type);
        Iterator<PSRecent> iterator = existingRecents.iterator();
        while (iterator.hasNext())
        {
            if (!toDelete.contains(iterator.next().getValue())) {
                iterator.remove();
            }
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

    public IPSRecentDao getRecentDao() {
        return recentDao;
    }

    public void setRecentDao(PSRecentDao recentDao) {
        this.recentDao = recentDao;
    }
}
