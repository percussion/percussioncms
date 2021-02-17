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
package com.percussion.pagemanagement.dao.impl;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.share.dao.IPSContentItemDao;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.dao.impl.PSContentItem;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.IPSIdMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class PSAbstractContentItemDao<T extends IPSItemSummary> implements IPSGenericDao<T, String>
{
    
    private IPSContentItemDao contentItemDao;
    
    private IPSIdMapper idMapper;

    protected abstract String getType();
    
    protected abstract T createObject();
    
    protected abstract void convertToObject(PSContentItem contentItem, T object);
    
    protected abstract void convertToItem(T page, PSContentItem contentItem);
    
    protected void postItemSave(@SuppressWarnings("unused") T object, 
            @SuppressWarnings("unused") PSContentItem contentItem) throws SaveException, LoadException {
    }

    public PSAbstractContentItemDao(IPSContentItemDao contentItemDao, IPSIdMapper idMapper)
    {
        super();
        this.contentItemDao = contentItemDao;
        this.idMapper = idMapper;
    }

    protected final IPSContentItemDao getContentItemDao()
    {
        return contentItemDao;
    }


    public void delete(String id) throws DeleteException, LoadException {
        find(id);
        contentItemDao.delete(id);
    }

    public List<T> findAll() throws LoadException
    {
        
        Collection<Integer> ids = getContentItemDao().findAllItemIdsByType(getType());
        List<T> results = new ArrayList<>();
        for (Integer id : ids)
        {
            PSLegacyGuid guid = new PSLegacyGuid(id, -1);
            String sid = idMapper.getString(guid);
            results.add(find(sid));
        }
        return results;
    }

    public T find(String id) throws LoadException
    {
        notNull(id, "id");
        PSContentItem contentItem = contentItemDao.find(id);
        if (contentItem == null) return null;
        
        if ( ! getType().equals(contentItem.getType()) ) {
           throw new LoadException("Type does not match!");
        }
        
        return getObjectFromContentItem(contentItem);
    }
    
    /**
     * Converts the specified content item to the generic object.
     * 
     * @param contentItem the Content Item, not <code>null</code>.
     * 
     * @return the generic object, not <code>null</code>.
     */
    protected T getObjectFromContentItem(PSContentItem contentItem)
    {
        notNull(contentItem, "contentItem");
        
        isTrue( isNotBlank(contentItem.getId()), "contentItem#getId() is blank");
        T object = createObject();
        object.setType(getType());
        object.setId(contentItem.getId());
        object.setFolderPaths(contentItem.getFolderPaths());
        object.setCategory(contentItem.getCategory());
        convertToObject(contentItem, object);
        return object;        
    }


    public T save(T object) throws SaveException, LoadException {
        PSContentItem item = new PSContentItem();
        item.setId(object.getId());
        item.setType(getType());
        item.setFolderPaths(object.getFolderPaths());
        convertToItem(object, item);
        item = contentItemDao.save(item);
        object.setId(item.getId());
        object.setType(item.getType());
        object.setFolderPaths(item.getFolderPaths());
        postItemSave(object, item);
        return find(item.getId());
    }
    
    protected String getFolderPath(PSContentItem contentItem) {
        List<String> paths = contentItem.getFolderPaths();
        if (paths != null && ! paths.isEmpty()) return paths.get(0);
        return null;
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSAbstractContentItemDao.class);
    

}