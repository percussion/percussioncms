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
package com.percussion.generickey.utils.services.rdbms.impl;

import com.percussion.generickey.data.IPSGenericKey;
import com.percussion.generickey.services.IPSGenericKeyDao;
import com.percussion.generickey.services.PSGenericKeyExistsException;
import com.percussion.generickey.utils.data.rdbms.impl.PSGenericKey;
import org.apache.commons.lang.Validate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Transactional
public class PSGenericKeyDao extends HibernateDaoSupport implements IPSGenericKeyDao
{
    /* (non-Javadoc)
     * @see com.percussion.generickey.services.IPSGenericKeyDao#createKey()
     */
    @Override
    public IPSGenericKey createKey()
    {
        IPSGenericKey key = new PSGenericKey();
        return key;
    }  
    
    @SuppressWarnings("unchecked")
    @Override
    public IPSGenericKey findByResetKey(String resetKey)
    {
        Validate.notEmpty(resetKey);
        Session session = getSession();
        try
        {
            IPSGenericKey genericKey = null;
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<PSGenericKey> criteriaQuery = criteriaBuilder.createQuery(PSGenericKey.class);
            Root<PSGenericKey> root = criteriaQuery.from(PSGenericKey.class);
            criteriaQuery.select(root).where(criteriaBuilder.like(root.get("genericKey"), resetKey));
            List<PSGenericKey> result = session.createQuery(criteriaQuery).getResultList();

            if (!result.isEmpty())
            {
                if (result.size() > 1)
                {
                    // this would be a bug
                    throw new IllegalStateException("More than one generic key entry found for pwdResetKey: " + resetKey);
                }
                    
                genericKey = (IPSGenericKey) result.get(0);
            }
            
            return genericKey;
        }
        finally
        {
            //session.close();
        }
    }

    @Override
    public void saveKey(IPSGenericKey resetKey) throws Exception
    {
        Validate.notNull(resetKey);
        Session session = getSession();
        try
        {
            validateNewKey(resetKey.getGenericKey(), session);
            session.saveOrUpdate(resetKey);
            session.flush();
        }
        finally
        {
            //session.close();
        }
        
    }

    @Override
    public void deleteKey(IPSGenericKey resetKey) throws Exception
    {
        Validate.notNull(resetKey);
        Session session = getSession();
        try
        {
            session.delete(resetKey);
            session.flush();
        }
        finally
        {
          // session.close();
        }
    }

    private Session getSession(){

        return getSessionFactory().getCurrentSession();

    }
    
    /**
     * Validate a generic key with the supplied resetKey does not already exist
     * 
     * @param resetKey Assumed not <code>null</code> or empty.
     * @param session The session to use, assumed not <code>null</code>.
     * 
     * @throws PSGenericKeyExistsException if a generic key with the same value already exists.
     */
    private void validateNewKey(String resetKey, Session session) throws PSGenericKeyExistsException
    {
        if (findGenericKey(resetKey, session) != null)
        {
            throw new PSGenericKeyExistsException(resetKey);
        }
    }

    /**
     * Helper method to find the key by reset key w/in a session.
     * 
     * @param resetKey Assumed not <code>null</code> or empty.
     * @param session Assumed not <code>null</code>.
     * 
     * @return The reset key, or <code>null</code> if not found.
     */
    @SuppressWarnings("unchecked")
    private IPSGenericKey findGenericKey(String resetKey, Session session)
    {
        IPSGenericKey genericKey = null;

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PSGenericKey> criteriaQuery = criteriaBuilder.createQuery(PSGenericKey.class);
        Root<PSGenericKey> root = criteriaQuery.from(PSGenericKey.class);
        criteriaQuery.select(root).where(criteriaBuilder.like(root.get("genericKey"), resetKey));
        List<PSGenericKey> result = session.createQuery(criteriaQuery).getResultList();

        if (!result.isEmpty())
        {
            if (result.size() > 1)
            {
                // this would be a bug
                throw new IllegalStateException("More than one generic key entry found for genericKey: " + resetKey);
            }
                
            genericKey = (IPSGenericKey) result.get(0);
        }
        
        return genericKey;
    }

}
