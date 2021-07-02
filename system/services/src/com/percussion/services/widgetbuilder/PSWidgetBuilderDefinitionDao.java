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

package com.percussion.services.widgetbuilder;

import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.share.dao.IPSGenericDao;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author matthewernewein
 *
 */
@Transactional
public class PSWidgetBuilderDefinitionDao
        implements IPSWidgetBuilderDefinitionDao
{
    
    private static final Logger log = LogManager.getLogger(PSWidgetBuilderDefinitionDao.class);


    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Constant for the key used to generate summary ids.
     */
    private static final String USER_ITEM_KEY = "PSX_WIDGETBUILDERDEFINITIONID";
    
    private IPSGuidManager m_guidManager;
    

    @SuppressWarnings("finally")
    public PSWidgetBuilderDefinition save(PSWidgetBuilderDefinition definition)
    {
        Validate.notNull(definition);

        if (definition.getWidgetBuilderDefinitionId() == -1)
        {
           definition.setWidgetBuilderDefinitionId(m_guidManager.createId(USER_ITEM_KEY));
        }

        Session session = sessionFactory.getCurrentSession();
        try
        {
            session.saveOrUpdate(definition);
        }
        catch (HibernateException e)
        {
            String msg = "database error " + e.getMessage();
            log.error(msg);
            throw new IPSGenericDao.SaveException(msg, e);
        }
        finally
        {
            session.flush();
            return definition;
        }  
        
    }

    public PSWidgetBuilderDefinition find(long definitionId)
    {
        PSWidgetBuilderDefinition definition = null;
        Session session = sessionFactory.getCurrentSession();

            Query query = session.createQuery("from PSWidgetBuilderDefinition where widgetBuilderDefinitionId = :widgetBuilderDefinitionId");
            query.setParameter("widgetBuilderDefinitionId", definitionId);

            @SuppressWarnings("unchecked")
           List<PSWidgetBuilderDefinition> definitions = query.list(); 
            if(!definitions.isEmpty())
               definition = definitions.get(0);
            return definition;

    }

    public void delete(long definitionId)
    {
       
        Validate.notNull(definitionId);
        PSWidgetBuilderDefinition definition = find(definitionId);
        Validate.notNull(definition);
              
        Session session = sessionFactory.getCurrentSession();
        try
        {
            session.delete(definition);
        }
        catch (HibernateException e)
        {
            String msg = "Failed to delete user item: " + e.getMessage();
            log.error(msg);
        }
        finally
        {
            session.flush();

        }
    }

   public void setGuidManager(IPSGuidManager guidManager)
    {
        m_guidManager = guidManager;
    }

   @SuppressWarnings("unchecked")
   public List<PSWidgetBuilderDefinition> getAll()
   {
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSWidgetBuilderDefinition.class);
         return criteria.list();


   }
}
