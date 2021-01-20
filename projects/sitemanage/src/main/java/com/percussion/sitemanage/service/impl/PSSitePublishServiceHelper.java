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
package com.percussion.sitemanage.service.impl;

import static com.percussion.services.utils.orm.PSDataCollectionHelper.MAX_IDS;
import static com.percussion.util.PSSqlHelper.qualifyTableName;

import static org.apache.commons.lang.StringUtils.join;

import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.pagemanagement.data.PSWidgetContentType;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.services.error.PSRuntimeException;

import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.sitemanage.service.IPSSitePublishServiceHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.percussion.util.PSSiteManageBean;
import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository("sitePublishServiceHelper")
@PSSiteManageBean
@Lazy
@Transactional
public class PSSitePublishServiceHelper implements IPSSitePublishServiceHelper
{
    private static final long MAX_RELATED_ITEMS = 1000;
    private static final long MAX_LOOPS = 10;
    
    private SessionFactory sessionFactory;
    
   
    List<Integer> publishableContentTypeIds;
    List<Integer> nonBinaryContentTypeIds;

    private IPSAssetService assetService;
    private List<String> binaryAssetTypes = Arrays.asList("percFileAsset", "percImageAsset", "percFlashAsset");
    
    @Autowired
    public PSSitePublishServiceHelper(IPSAssetService assetService)
    {
    	this.assetService = assetService;
    }

	@Override
	public Collection<Integer> findRelatedItemIds(Set<Integer> contentIds) {
		Set<Integer> results = new HashSet<Integer>();
		if (contentIds.isEmpty()) {
			return results;
		}
		Session sess = getSession();
		try {
			Set<Integer> cids = new HashSet<Integer>();
			cids.addAll(contentIds);
			int l=0;
			for (; l<MAX_LOOPS; l++) {
				Set<Integer> relatedIds = getRelatedItemIds(sess, cids);
				if (relatedIds.isEmpty()) {
					break;
				}
				relatedIds.removeAll(contentIds);
				relatedIds.removeAll(results);
				results.addAll(relatedIds);
				cids.clear();
				cids.addAll(relatedIds);
			}
			if (l == MAX_LOOPS) {
				throw new RuntimeException("Could not find the related items within the number of iterations. Looks like the items are deeply related.");
			}
		} catch (SQLException e) {
			String errMsg = "SQL error occurred while getting related content ids for incremental publishing.";
			ms_logger.error(errMsg, e);
			throw new PSRuntimeException(errMsg, e);
		}
		return results;
	}

	

    private Set<Integer> getRelatedItemIds(Session sess, Set<Integer> cids) throws SQLException {
		Set<Integer> results = new HashSet<Integer>();
		for (int i = 0; i < cids.size(); i += MAX_IDS) {
			int end = (i + MAX_IDS > cids.size()) ? cids.size() : i + MAX_IDS;
			// lets get the direct publishable related items
			results.addAll(getPublishableRelatedItemIds(sess, cids));
			// lets get the direct non-publishable related items
			Set<Integer> ncids = getNonPublishableRelatedItemIds(sess, cids);
			// now lets get the direct publishable related items
			if (!ncids.isEmpty()) {
				results.addAll(getPublishableRelatedItemIds(sess, ncids));
			}
		}
		return results;
	}
	
    private Set<Integer> getPublishableRelatedItemIds(Session sess, Set<Integer> cids) throws SQLException{
    	String sql = String.format(
        		"SELECT DISTINCT REL.DEPENDENT_ID FROM %s as CS1, %s as CS2, "
        	    		+ "%s as ST, %s as REL  WHERE REL.OWNER_ID IN (%s) AND "
        	    		+ "REL.OWNER_ID = CS1.CONTENTID AND REL.OWNER_REVISION = CS1.CURRENTREVISION AND "
        	    		+ "REL.DEPENDENT_ID = CS2.CONTENTID	AND CS2.CONTENTTYPEID in (%s) AND ST.WORKFLOWAPPID = CS2.WORKFLOWAPPID AND "
        	    		+ "ST.STATEID = CS2.CONTENTSTATEID AND ST.CONTENTVALID IN('n','i')", qualifyTableName("CONTENTSTATUS"),
        	    		qualifyTableName("CONTENTSTATUS"), qualifyTableName("STATES"), qualifyTableName("PSX_OBJECTRELATIONSHIP"),
        	    		join(cids, ","), join(getPublishableContentTypeIds(), ","));           
        SQLQuery query = sess.createSQLQuery(sql);
        return new HashSet(query.list());
    }

    private Set<Integer> getNonPublishableRelatedItemIds(Session sess, Set<Integer> cids) throws SQLException{
        String sql = String.format(
        		"SELECT DISTINCT REL.DEPENDENT_ID FROM %s as CS1, %s as CS2, "
        	    		+ "%s as ST, %s as REL  WHERE REL.OWNER_ID IN (%s) AND "
        	    		+ "REL.OWNER_ID = CS1.CONTENTID AND REL.OWNER_REVISION = CS1.CURRENTREVISION AND "
        	    		+ "REL.DEPENDENT_ID = CS2.CONTENTID AND CS2.CONTENTTYPEID in (%s)", qualifyTableName("CONTENTSTATUS"), 
        	    		qualifyTableName("CONTENTSTATUS"), qualifyTableName("STATES"), qualifyTableName("PSX_OBJECTRELATIONSHIP"),
        	    		join(cids, ","), join(getNonPublishableContentTypeIds(), ","));           
        SQLQuery query = sess.createSQLQuery(sql);
        return new HashSet(query.list());
    }
        
	private void initTypeIds() {
		try {
			PSItemDefManager defMgr = PSItemDefManager.getInstance();
			long pageContentTypeId = defMgr
					.contentTypeNameToId(IPSPageService.PAGE_CONTENT_TYPE);
			List<PSWidgetContentType> assetTypes = assetService.getAssetTypes("no");
			publishableContentTypeIds = new ArrayList<Integer>();
			nonBinaryContentTypeIds = new ArrayList<Integer>();
			for (PSWidgetContentType type : assetTypes) {
				try {
					if (binaryAssetTypes.contains(type.getContentTypeName())) {
						publishableContentTypeIds.add(Integer.valueOf(type.getContentTypeId()));
					} else {
						nonBinaryContentTypeIds.add(Integer.valueOf(type.getContentTypeId()));
					}
				} catch (Exception e) {
					String errMsg = "The supplied content type id of the widget doesn't belong to a valid content type, ignoring the widget."
							+ "widget name: " + type.getWidgetLabel() + " content type:" + type.getContentTypeName();
					ms_logger.info(errMsg, e);
				}
			}
			publishableContentTypeIds.add(Integer.valueOf("" + pageContentTypeId));
		} catch (Exception e) {
			String errMsg = "Failed to initialize content type ids while getting related content ids for incremental publishing.";
			ms_logger.error(errMsg, e);
			throw new PSRuntimeException(errMsg, e);
		}		
	}

	private List<Integer> getPublishableContentTypeIds() {
		if (publishableContentTypeIds == null) {
			initTypeIds();
		}
		return publishableContentTypeIds;
	}

	private List<Integer> getNonPublishableContentTypeIds() {
		if (nonBinaryContentTypeIds == null) {
			initTypeIds();
		}
		return nonBinaryContentTypeIds;
	}

	private Session getSession()
    {
        return sessionFactory.getCurrentSession();
    }
	
	public SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }
 
    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }


	private static Logger ms_logger = Logger.getLogger(PSSitePublishServiceHelper.class);
}
