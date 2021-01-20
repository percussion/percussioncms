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

package com.percussion.assetmanagement.service.impl;

import static com.percussion.util.PSSqlHelper.qualifyTableName;

import static org.apache.commons.lang.StringUtils.isBlank;

import com.percussion.cms.IPSConstants;
import com.percussion.pagemanagement.service.IPSWidgetAssetRelationshipDao;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSRuntimeException;
import com.percussion.services.guidmgr.PSGuidHelper;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The service used to update Page & Asset relationships.
 * 
 * @author YuBingChen
 */
@Transactional
@Component("widgetAssetRelationshipDao")
public class PSWidgetAssetRelationshipDao implements IPSWidgetAssetRelationshipDao
{
   
    private SessionFactory sessionFactory;

    @Transactional
    public int updateWidgetNameForRelatedPages(String templateId, String widgetName, long widgetId)
    {
       
        
        widgetName = isBlank(widgetName) ? "NULL" : widgetName;
        int sortRank = PSGuidHelper.generateNext(PSTypeEnum.SORT_RANK).getUUID();
        
        Session sess = sessionFactory.getCurrentSession();
        
        // update PSX_OBJECTRELATIONSHIP set WIDGET_NAME='XXX', SORT_RANK=### where SLOT_ID = ? and WIDGET_NAME IS NULL and OWNER_ID in (select ...)
        try
        {
            String sql = "update " + qualifyTableName(IPSConstants.PSX_RELATIONSHIPS) + " set WIDGET_NAME='"
                    + widgetName + "', SORT_RANK=" + sortRank + " where SLOT_ID = " + widgetId 
                    + " and WIDGET_NAME IS NULL and OWNER_ID in (select CONTENTID from "
                    + qualifyTableName("CT_PAGE") + " where TEMPLATEID = '" + templateId + "')";
            
            SQLQuery query = sess.createSQLQuery(sql);
            int result = query.executeUpdate();
            
            ms_logger.debug("Updated " + result + " rows in " + IPSConstants.PSX_RELATIONSHIPS + " table.");
            return result;
        }
        catch (SQLException e)
        {
            String error = "Failed to update relationship table";
            ms_logger.error(error, e);
            throw new PSRuntimeException(error, e);
        }
      
    }
    
    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    public SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }



    
    
    private static Logger ms_logger = Logger.getLogger(PSWidgetAssetRelationshipDao.class);
}
