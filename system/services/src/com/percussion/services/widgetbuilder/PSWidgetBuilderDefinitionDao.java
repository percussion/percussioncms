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

package com.percussion.services.widgetbuilder;

import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.util.PSBaseBean;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author matthewernewein
 *
 */
@Transactional
@Repository
@PSBaseBean("sys_widgetBuilderDefinitionDao")
public class PSWidgetBuilderDefinitionDao
        implements IPSWidgetBuilderDefinitionDao
{
    
    private static final Logger log = LogManager.getLogger(PSWidgetBuilderDefinitionDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    private Session getSession(){
        return entityManager.unwrap(Session.class);
    }
    
    /**
     * Constant for the key used to generate summary ids.
     */
    private static final String USER_ITEM_KEY = "PSX_WIDGETBUILDERDEFINITIONID";
    
    private IPSGuidManager m_guidManager;


    @Transactional
    public PSWidgetBuilderDefinition save(PSWidgetBuilderDefinition definition) throws IPSGenericDao.SaveException {
        Validate.notNull(definition);

        if (definition.getWidgetBuilderDefinitionId() == -1)
        {
           definition.setWidgetBuilderDefinitionId(m_guidManager.createId(USER_ITEM_KEY));
        }

        Session session = getSession();
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
        }
        return definition;
        
    }

    public PSWidgetBuilderDefinition find(long definitionId)
    {
        PSWidgetBuilderDefinition definition = null;
        Session session = getSession();

            Query query = session.createQuery("from PSWidgetBuilderDefinition where widgetBuilderDefinitionId = :widgetBuilderDefinitionId");
            query.setParameter("widgetBuilderDefinitionId", definitionId);

            @SuppressWarnings("unchecked")
           List<PSWidgetBuilderDefinition> definitions = query.list(); 
            if(!definitions.isEmpty())
               definition = definitions.get(0);
            return definition;

    }

    @Transactional
    public void delete(long definitionId)
    {
       
        Validate.notNull(definitionId);
        PSWidgetBuilderDefinition definition = find(definitionId);
        Validate.notNull(definition);
              
        Session session = getSession();
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
      Session session = getSession();

         Criteria criteria = session.createCriteria(PSWidgetBuilderDefinition.class);
         return criteria.list();


   }
}
