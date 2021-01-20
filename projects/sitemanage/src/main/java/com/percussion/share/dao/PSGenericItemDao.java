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
package com.percussion.share.dao;

import static java.util.Arrays.*;
import static org.apache.commons.lang.StringUtils.*;
import static org.apache.commons.lang.Validate.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXB;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.share.data.PSAbstractPersistantObject;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSUnknownContentTypeException;
import com.percussion.webservices.content.IPSContentWs;

/**
 * A Generic Content Item Dao that will store a serialized object in 
 * the field {@link #getDataFieldName()}.
 * The {@link #getUniqueIdFieldName()} property is the field used to uniquely identify the content item.
 * @author adamgent
 *
 * @param <T> The object type to be serialized.
 */
public abstract class PSGenericItemDao<T extends PSAbstractPersistantObject> implements IPSGenericDao<T, String>
{

    /**
     * The type of object to be serialized to the field with name: {@link #dataFieldName}
     */
    private Class<T> type;

    private IPSContentWs contentWs;
    private PSJcrNodeFinder jcrNodeFinder;
    private IPSIdMapper idMapper;
    
    /**
     * Content type name.
     */
    private String contentType = null;
    /**
     * Unique field used for search and locating the item.
     */
    private String uniqueIdFieldName = "sys_title";
    /**
     * Field where the serialized xml is stored.
     */
    private String dataFieldName = "data";
    /**
     * Folder path where the content items will be stored.
     */
    private String folderPath = null;

    
    

    /**
     * Construct a default implementation of the item dao.
     * Use the setters to override the defaults.
     * 
     * @param contentWs never <code>null</code>.
     * @param contentMgr never <code>null</code>.
     * @param idMapper never <code>null</code>.
     * @param type never <code>null</code>.
     * @param contentType never <code>null</code>.
     * @param folderPath never <code>null</code>.
     */
    public PSGenericItemDao(
            IPSContentWs contentWs, 
            IPSContentMgr contentMgr, 
            IPSIdMapper idMapper, 
            Class<T> type, 
            String contentType, 
            String folderPath)
    {
        super();
        notNull(contentWs);
        notNull(contentMgr);
        notNull(type);
        notNull(contentType);
        notNull(idMapper);
        notNull(folderPath);
        this.folderPath = folderPath;
        this.contentWs = contentWs;
        this.idMapper = idMapper;
        this.type = type;
        this.contentType = contentType;
        this.jcrNodeFinder = new PSJcrNodeFinder(contentMgr, this.contentType, this.uniqueIdFieldName);  
    }



    public T find(String id) throws com.percussion.share.dao.IPSGenericDao.LoadException
    {
        if(log.isDebugEnabled())
            log.debug("Finding object id: " + id);
        notNull(id);
        T object = null;
        try
        {
            PSCoreItem item = findContentItem(id);
            if (item != null) {
                PSItemField field = item.getFieldByName(getDataFieldName());
                IPSFieldValue fv = field.getValue();
                String dataField = fv.getValueAsString();
                object = PSSerializerUtils.unmarshal(dataField, type);
            }
        }
        catch (PSErrorException e)
        {
            handleLoadException(id, e);
        }
        catch (PSErrorResultsException e)
        {
            handleLoadException(id, e);
        } 
        catch (PSCmsException e) {
            handleLoadException(id, e);
        }
        return object;
    }


    
    private void handleLoadException(String id, Exception e) {
        String error = errorMessage("load", id);
        throw new LoadException(error, e);
    }

    /**
     * Implementations can override this method if they need to
     * but should probably override {@link #findContentItemGuid(String)}
     * instead.
     * 
     * @param id never <code>null</code>.
     * @return maybe <code>null</code>
     * @throws PSErrorException
     * @throws PSErrorResultsException
     */
    protected PSCoreItem findContentItem(String id)
            throws PSErrorException, PSErrorResultsException {
        notNull(id);
        IPSGuid userGuid = findContentItemGuid(id);
        if (userGuid == null) return null;
        List<PSCoreItem> items = contentWs.loadItems(Arrays.asList(userGuid), 
                true, false, false, true);
        if (items.isEmpty()) return null;
        return items.get(0);
    }

    /**
     * Implementations must determine the guid from the id.
     * Implementations will most likely implement this by calling 
     * {@link #findContentItemGuidByConversion(String)}
     * or {@link #findContentItemGuidWithSearch(String)}.
     * @param id never <code>null</code>.
     * @return the guid if found, <code>null</code> otherwise.
     */
    protected abstract IPSGuid findContentItemGuid(String id);
    
    /**
     * @param id never <code>null</code>.
     * @return never <code>null</code>.
     * @see IPSIdMapper
     */
    protected IPSGuid findContentItemGuidByConversion(String id)
    {
        return idMapper.getGuid(id);
    }
    
    /**
     * Performs a search for id.
     * The search is parameterized by the properties on this class.
     * @param id never <code>null</code>.
     * @return the guid if found, <code>null</code> otherwise.
     * @see #getContentType()
     * @see #getUniqueIdFieldName()
     * @see #getFolderPath()
     */
    protected IPSGuid findContentItemGuidWithSearch(String id)
    {
        IPSNode node = jcrNodeFinder.find(folderPath, id);
        if (node == null) {
            return null;
        }
        return node.getGuid();
    }

    public List<T> findAll() throws com.percussion.share.dao.IPSGenericDao.LoadException
    {
        throw new UnsupportedOperationException("Find all not supported");
    }

    public void delete(String id) throws com.percussion.share.dao.IPSGenericDao.DeleteException
    {
        if (log.isDebugEnabled())
            log.debug("Deleting " + type.getSimpleName() + " : " + id);
            
        notNull(id);
        IPSGuid guid = findContentItemGuid(id);
        if (guid == null) throw new DeleteException("#findContentItemGuid returned null.");
        try
        {
            contentWs.deleteItems(asList(guid));
        }
        catch (PSErrorsException e)
        {
            String error = errorMessage("delete", id);
            throw new DeleteException(error, e);
        }
        catch (PSErrorException e)
        {
            String error = errorMessage("delete", id);
            throw new DeleteException(error,e);
        }
        
    }
    
    private String errorMessage(String action, String id) {
        return "Failed to " + action + " item with id: " + id + " for type " + type.getSimpleName();
    }
    
    private String errorMessage(String action, T item) {
        return "Failed to " + action + " item: " + item + " for type " + type.getSimpleName();
    }

    public T save(T object) throws SaveException
    {
        if(log.isDebugEnabled())
            log.debug("Save " +  type.getSimpleName() +  ":" + object);
        
        notNull(object);
        try {
            String id = object.getId();
            PSCoreItem coreItem = null;
            if (id != null)
                coreItem = findContentItem(id);
            /*
             * Create the content item.
             */
            if (coreItem == null) {
                if(log.isDebugEnabled())
                    log.debug("Creating content item for object: " + object);
                coreItem = contentWs.createItems(getContentType(), 1).get(0);
                if (id == null) {
                    String fakeId = createSurrogateId(coreItem, object);
                    isTrue(isNotBlank(fakeId), "Surrogate id cannot be blank");
                    id = fakeId;
                }
            }
            String data = marshal(object);
            List<String> paths = getFolderPaths(object);
            notEmpty(paths);
            coreItem.setFolderPaths(paths);
            coreItem.setTextField("sys_title", id);
            coreItem.setTextField(getUniqueIdFieldName(), id);
            coreItem.setTextField(getDataFieldName(), data);
            IPSGuid guid = contentWs.saveItems(Arrays.asList(coreItem), false, false).get(0);
            /*
             * If they didn't supply an id we will use the guid.
             */
            if (object.getId() == null) {
                object.setId(idMapper.getString(guid));
            }
            return object;
        } catch (PSUnknownContentTypeException e) {
            String err = "Could not save because content type does not exist";
            throw new SaveException(err,e);
        } catch (PSErrorException e) {
            String error = errorMessage("save", object);
            throw new SaveException(error, e);
        } catch (PSErrorResultsException e) {
            String error = errorMessage("save", object);
            throw new SaveException(error, e);
        }
    }
    
    /**
     * When an item is created, right before its saved
     * a unique id is needed. Sometimes the object does not
     * specify its id on {@link #save(Object)} and a surrogate
     * must be used till the item is updated.
     *  
     * @param item never <code>null</code>.
     * @param object never <code>null</code>.
     * @return never <code>null</code>, empty, or blank.
     */
    //TODO: Fix me on getting the guid. The coreitem should have the guid.
    protected String createSurrogateId(PSCoreItem item, T object) 
    {
        if(log.isDebugEnabled())
            log.debug("Creating surrogate id for object: " + object);
        String surrogateId = RandomStringUtils.randomAlphabetic(20);
        if(log.isDebugEnabled())
            log.debug("Surrogate id: " + surrogateId);
        return surrogateId;
    }
    
    /**
     * Implementations need to override this method as to where
     * the items will be stored.
     * All items should be in a folder so returning empty is bad. 
     * @param object never <code>null</code>.
     * @return the folder paths of where the object should be stored, never <code>null</code> or empty.
     */
    protected abstract List<String> getFolderPaths(T object);
    
    protected String marshal(T object) {
       return PSSerializerUtils.marshal(object);
    }
    
    protected T unmarshal(String dataField)
    {
    	T object = null;
        return (T) PSSerializerUtils.unmarshal(dataField, type);
    }
    
    
    
    public String getUniqueIdFieldName()
    {
        return uniqueIdFieldName;
    }



    public void setUniqueIdFieldName(String uniqueIdFieldName)
    {
        this.uniqueIdFieldName = uniqueIdFieldName;
    }
    

    public String getFolderPath()
    {
        return folderPath;
    }



    public void setFolderPath(String folderPath)
    {
        this.folderPath = folderPath;
    }





    public String getContentType()
    {
        return contentType;
    }



    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }


    

    public String getDataFieldName()
    {
        return dataFieldName;
    }



    public void setDataFieldName(String dataFieldName)
    {
        this.dataFieldName = dataFieldName;
    }



    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    protected final Log log = LogFactory.getLog(getClass());

}
