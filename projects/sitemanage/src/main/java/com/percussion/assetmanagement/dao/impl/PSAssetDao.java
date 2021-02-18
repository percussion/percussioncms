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
package com.percussion.assetmanagement.dao.impl;

import com.percussion.assetmanagement.dao.IPSAssetDao;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSReportFailedToRunException;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.services.notification.PSNotificationHelper;
import com.percussion.share.dao.IPSContentItemDao;
import com.percussion.share.dao.PSJcrNodeFinder;
import com.percussion.share.dao.impl.PSContentItem;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSContentItemUtils;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSPropertiesValidationException;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.*;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.isTrue;

@Component("assetDao")
public class PSAssetDao implements IPSAssetDao
{

    /**
     * Logger for this service.
     */
    public static Logger log = LogManager.getLogger(PSAssetDao.class);
    
    private IPSContentItemDao contentItemDao;
    private IPSContentMgr contentMgr;
    private IPSIdMapper idMapper;
    
    @Autowired
    public PSAssetDao(IPSContentItemDao contentItemDao, IPSContentMgr contentMgr, IPSIdMapper idMapper)
    {
        super();
        this.contentItemDao = contentItemDao;
        this.contentMgr = contentMgr;
        this.idMapper = idMapper;
    }

    
    public IPSItemSummary addItemToPath(IPSItemSummary item, String folderPath) throws PSDataServiceException {
        return contentItemDao.addItemToPath(item, folderPath);
    }

    public void removeItemFromPath(IPSItemSummary item, String folderPath) throws PSDataServiceException {
        contentItemDao.removeItemFromPath(item, folderPath);
    }    

    public void delete(String id) throws com.percussion.share.dao.IPSGenericDao.DeleteException, LoadException {
        // Local content is not in a folder, also orphaned content.  Both of these types of asset should
        // be able to be deleted without validation.  Local content validation is based upon the page.
        boolean localOrOrphanedContent=false;
        String itemName = "";
        try {
            PSContentItem item = contentItemDao.find(id,true);
            itemName = item.getName();
            List<String> paths = item.getFolderPaths();
            if (paths==null || paths.isEmpty())
                localOrOrphanedContent=true;
            else
            {
                int index = paths.get(0).indexOf("/Assets");
                itemName = paths.get(0).substring(index) + "/" + itemName;
            }
                
        } catch (Exception e)
        {
          log.error("Error trying to find folder paths for item id {}",id);
        }

        contentItemDao.delete(id);
        if (StringUtils.isNotBlank(itemName))
        {
            String currentUser = (String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
            log.info( "{} has been deleted by: {} ",itemName,  currentUser);
        }
        PSNotificationHelper.notifyEvent(EventType.ASSET_DELETED, id);
    }

    public PSAsset find(String id) throws com.percussion.share.dao.IPSGenericDao.LoadException, IPSDataService.DataServiceLoadException, PSValidationException, IPSDataService.DataServiceNotFoundException {
        return find(id, false);
    }

    public PSAsset find(String id, boolean isSummary) throws com.percussion.share.dao.IPSGenericDao.LoadException, IPSDataService.DataServiceLoadException, PSValidationException, IPSDataService.DataServiceNotFoundException {
        PSContentItem contentItem = contentItemDao.find(id, isSummary);
        if (contentItem == null) return null;
        PSAsset asset = createAsset(contentItem);
        return asset;
    }

    public List<PSAsset> findAll() throws com.percussion.share.dao.IPSGenericDao.LoadException
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("findAll is not yet supported");
    }

    public synchronized PSAsset save(PSAsset object) throws com.percussion.share.dao.IPSGenericDao.SaveException, LoadException, DeleteException {
        PSContentItem contentItem = new PSContentItem();
        PSContentItemUtils.copyProperties(object, contentItem);
        contentItem = contentItemDao.save(contentItem);
        return createAsset(contentItem);
    }

    public Collection<PSAsset> findByTypeAndWf(String type, int workflowId, int stateId) throws LoadException {
        isTrue(isNotBlank(type), "type may not be blank");
        
        Map<String, String> whereFields = new HashMap<>();
        whereFields.put("sys_workflowid", String.valueOf(workflowId));
        if (stateId != -1)
        {
            whereFields.put("sys_contentstateid", String.valueOf(stateId));
        }
        
        return find(type, whereFields);
    }
    
    public Collection<PSAsset> findByTypeAndName(String type, String name) throws LoadException {
        isTrue(isNotBlank(type), "type may not be blank");
        isTrue(isNotBlank(name), "name may not be blank");
        
        Map<String, String> whereFields = new HashMap<>();
        whereFields.put("sys_title", name);
                
        return find(type, whereFields);
    }
    
    public Collection<PSAsset> findByType(String type) throws LoadException {
        isTrue(isNotBlank(type), "type may not be blank");
        
        Map<String, String> whereFields = new HashMap<>();
                
        return find(type, whereFields);
    }
    
    
    protected PSAsset createAsset(PSContentItem contentItem) {
        PSAsset asset = new PSAsset();
        PSContentItemUtils.copyProperties(contentItem, asset);
        return asset;
    }
    
    /**
     * Finds assets of the specified type which satisfy the specified where-clause fields in a jcr query.
     * 
     * @param type content type, assumed not <code>null</code>.
     * @param whereFields map of where field -> value, assumed not <code>null</code>.
     * @return collection of <code>PSAsset</code> objects, never <code>null</code>.
     */
    private Collection<PSAsset> find(String type, Map<String, String> whereFields) throws LoadException {
    	
    	List<PSAsset> assets = new ArrayList<>();
        
        PSJcrNodeFinder jcrNodeFinder = new PSJcrNodeFinder(contentMgr, type, "sys_title");
        List<IPSNode> nodes = jcrNodeFinder.find(whereFields);
        for (IPSNode node : nodes)
        {
            try {
                assets.add(find(idMapper.getString(node.getGuid())));
            } catch (IPSDataService.DataServiceLoadException | IPSDataService.DataServiceNotFoundException | PSValidationException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(),e);
                //continue processing
            }
        }        
        
        return assets;
    }


    @Override
    public void revisionControlOn(String id) throws LoadException {
        contentItemDao.revisionControlOn(id);
    }

        private static final String NONCOMPLIANT_IMAGE_ASSET_QUERY="select rx:sys_contentid from rx:percImageAsset WHERE rx:alttext is null or rx:displaytitle is null or rx:alttext like '%.%' or rx:displaytitle like '%.%' or rx:resource_link_title is null or rx:resource_link_title like '%.%' order by jcr:path asc";
	private static final String NONCOMPLIANT_IMAGE_ASSET_REPORT="Non Compliant Image Assets";
	@Override
	public List<PSAsset> findAllNonADACompliantImageAssets() throws PSReportFailedToRunException {
		return runReport(NONCOMPLIANT_IMAGE_ASSET_QUERY,NONCOMPLIANT_IMAGE_ASSET_REPORT);		
	}
    // Added check for AltText is blank or filename | CMS-3216
	private static final String NONCOMPLIANT_FILE_ASSET_QUERY="select rx:sys_contentid from rx:percFileAsset WHERE rx:displaytitle is null or rx:displaytitle like '%.%' or rx:alttext is null or rx:alttext like '%.%' order by jcr:path asc";
	private static final String NONCOMPLIANT_FILE_ASSET_REPORT="Non Compliant File Assets";

	@Override
	public List<PSAsset> findAllNonADACompliantFileAssets() throws PSReportFailedToRunException {
		return runReport(NONCOMPLIANT_FILE_ASSET_QUERY,NONCOMPLIANT_FILE_ASSET_REPORT);		
	}
	
	private static final String ALL_FILE_ASSET_QUERY="select rx:sys_contentid from rx:percFileAsset";
	private static final String ALL_FILE_ASSET_REPORT="All File Assets";

	@Override
	public List<PSAsset> findAllFileAssets() throws PSReportFailedToRunException {
		return runReport(ALL_FILE_ASSET_QUERY,ALL_FILE_ASSET_REPORT);		
	}
	
	private static final String ALL_IMAGE_ASSET_QUERY="select rx:sys_contentid from rx:percImageAsset order by jcr:path asc";
	private static final String ALL_IMAGE_ASSET_REPORT="All Image Assets";

	@Override
	public List<PSAsset> findAllImageAssets() throws PSReportFailedToRunException {
		return runReport(ALL_IMAGE_ASSET_QUERY,ALL_IMAGE_ASSET_REPORT);		
	}
	
    private List<PSAsset> runReport(String query, String reportName) throws PSReportFailedToRunException{
	ArrayList<PSAsset> ret = new ArrayList<>();
		
		Query q = null;
		try {
			q = contentMgr.createQuery(query, Query.SQL);
		} catch (RepositoryException e) {
			log.error("An error occurred executing the query for the " + reportName + " report from the Content Repository.", e);
			throw new PSReportFailedToRunException(e);
		}
		
		QueryResult results = null;
		try {
			results = contentMgr.executeQuery(q, -1, null, null);
		} catch (RepositoryException e) {
			log.error("An error occurred executing the query for the " + reportName + " report from the Content Repository.", e);
			throw new PSReportFailedToRunException(e);
		}
	
		NodeIterator it;
		try {
			it = results.getNodes();
		} catch (RepositoryException e) {
			log.error("An error occurred retrieving the {} report from the Content Repository. Error: {}",  reportName,
                    e.getMessage());
			log.debug(e.getMessage(),e);
			return ret;
		}
		
         while (it.hasNext())
         {
             IPSNode node = (IPSNode) it.nextNode();
             PSContentItem contentItem=null;
             
			try {
				contentItem = contentItemDao.find(idMapper.getString(node.getGuid()), true);
			} catch (LoadException | IPSDataService.DataServiceLoadException | PSValidationException | IPSDataService.DataServiceNotFoundException e) {
				log.error("An error occurred retrieving an Image Asset for the  {} report from the Content Repository. Error: {}",reportName,
                        e.getMessage());
				log.debug(e.getMessage(),e);
			}
            
			if(null != contentItem ){
            	ret.add(createAsset(contentItem));
            }
         }
        
         return ret;
		
    }
}
