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

package com.percussion.recent.dao.impl;

import com.percussion.recent.data.PSRecent;
import com.percussion.recent.data.PSRecent.RecentType;
import com.percussion.share.dao.IPSGenericDao.SaveException;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("recentDao")
public class PSRecentDao
{

    /**
     * The hibernate session factory injected by spring
     */
    @Autowired
    private SessionFactory sessionFactory;
    
    PSRecentDao()
    {
        
    }
    /*
    PSRecentDao(SessionFactory sessionFactory)
    {
        this.setSessionFactory(sessionFactory);
    }
    */
    
    @SuppressWarnings("unchecked")
    public List<PSRecent> find(String user, String siteName, RecentType type)
    {
        Session session = getSession();
        Criteria crit = session.createCriteria(PSRecent.class);
        if (user!=null)
            crit.add(Restrictions.eq("user", user));
        if(siteName!=null)
            crit.add(Restrictions.eq("siteName", siteName));
        if(type!=null)
            crit.add(Restrictions.eq("type", type)); 
        crit.addOrder(Order.asc("order"));
        return crit.list();
    }
    

    public void saveAll(List<PSRecent> recentList)
    {
     
        for (PSRecent recent : recentList)
        {
            getSession().saveOrUpdate(recent);
        }
        
    }


    public void delete(PSRecent recent) 
    {
        getSession().delete(recent);
    }
    

    public void deleteAll(List<PSRecent> recentList)
    {
        for (PSRecent recent : recentList)
        {
            getSession().delete(recent);
        }
    }

    

    public void save(PSRecent recent) throws SaveException
    {
        getSession().saveOrUpdate(recent);
    }
    
    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }
    
    public SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
 
}

