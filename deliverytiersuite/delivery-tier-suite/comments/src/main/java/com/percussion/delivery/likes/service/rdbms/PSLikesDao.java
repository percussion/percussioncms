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

package com.percussion.delivery.likes.service.rdbms;

import com.percussion.delivery.likes.data.IPSLikes;
import com.percussion.delivery.likes.services.IPSLikesDao;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
@Scope("singleton")
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class PSLikesDao implements IPSLikesDao {

    private SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    public void delete(Collection<String> ids) throws Exception
	{
		Collection<Long> longIds = new ArrayList<>(ids.size());
    	for(String s : ids)
    		longIds.add(Long.valueOf(s));
    	Session session = getSession();
        try
        {
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaDelete<PSLikes> deleteQuery = builder.createCriteriaDelete(PSLikes.class);
			Root<PSLikes> root = deleteQuery.from(PSLikes.class);
			root.get("id").in(longIds);
			session.createQuery(deleteQuery).executeUpdate();

        }
        finally
        {
            //session.close();
        }

	}

	private Session getSession(){
		return sessionFactory.getCurrentSession();
	}

    public List<IPSLikes> findLikesForSite(String siteName) throws Exception {
        Session session = getSession();
		try {
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<IPSLikes> criteriaQuery = criteriaBuilder.createQuery(IPSLikes.class);
			Root<PSLikes> root = criteriaQuery.from(PSLikes.class);
			criteriaQuery.select(root).where(criteriaBuilder.like(root.get("site"), siteName));

			List<IPSLikes> results = session.createQuery(criteriaQuery).getResultList();
            return results;
        } finally {
            //session.close();
        }
    }

    public void save(List<IPSLikes> likes) throws Exception
    {
        Session session = getSession();
        try {
			int i = 0;
			for (IPSLikes like : likes) {
				session.saveOrUpdate(like);
				if (++i % 50 == 0) {
					session.flush();
					session.clear();
				}
			}
		}finally {
        	//session.close();
		}
    }

	public List<IPSLikes> find(String site, String likeId, String type)
			throws Exception 
	{
		Session session = getSession();

		try
		{
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<IPSLikes> criteriaQuery = criteriaBuilder.createQuery(IPSLikes.class);
			Root<PSLikes> root = criteriaQuery.from(PSLikes.class);
			criteriaQuery.select(root).where(
					criteriaBuilder.and(criteriaBuilder.like(root.get("site"), site),
							criteriaBuilder.equal(root.get("type"), type),
							criteriaBuilder.equal(root.get("likeId"), likeId)));
			List<IPSLikes> results =session.createQuery(criteriaQuery).getResultList();
			return results;
			
		}
		finally
		{
			//session.close();
		}
	}
    

	public void save(IPSLikes like) throws Exception
	{
		PSLikes hlike = new PSLikes(like);
		hlike.setLikeId(like.getLikeId());
        getSession().saveOrUpdate(hlike);
		like.setLikeId(hlike.getLikeId());
	}

	public IPSLikes create(String site, String likeId, String type)
			throws Exception {
		return new PSLikes(site, likeId, type);
	}
	
	public int decrementTotal(String site, String likeId, String type)
			throws Exception 
	{
		return incDecTotal(site, likeId, type, false);		
	}
    
	public int incrementTotal(String site, String likeId, String type)
			throws Exception 
	{
		return incDecTotal(site, likeId, type, true);		
	}

	int incDecTotal(String site, String likeId, String type, boolean isInc)
			throws Exception {
		String query = "update PSLikes set total = :total where likeid = :likeId and site = :site and type = :type";

		List<IPSLikes> existing = find(site, likeId, type);
		if (!existing.isEmpty()) {
            IPSLikes like = existing.get(0);
		    int count = like.getTotal();
			if (isInc || count > 0) {
				Session session = getSession();
				
				int newTotal = isInc ? count + 1 : count - 1;
                like.setTotal(newTotal);
                session.saveOrUpdate(like);

                return newTotal;
			}
		}
		return 0;
	}
	
	

}
