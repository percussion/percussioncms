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
package com.percussion.delivery.feeds.services.rdbms;

import com.percussion.delivery.feeds.data.IPSFeedDescriptor;
import com.percussion.delivery.feeds.services.IPSConnectionInfo;
import com.percussion.delivery.feeds.services.IPSFeedDao;
import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * @author erikserating
 *
 */
@Repository
public class PSFeedDao extends HibernateDaoSupport implements IPSFeedDao {

	private static final Logger log = LogManager.getLogger(PSFeedDao.class);

	public PSFeedDao(){}

	@Autowired
	public PSFeedDao(SessionFactory sessionFactory){
		super.setSessionFactory(sessionFactory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.percussion.feeds.services.IPSFeedDao#find(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	@Transactional
	public IPSFeedDescriptor find(String name, String site) {

			Session session = getSession();
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<IPSFeedDescriptor> criteriaQuery = criteriaBuilder.createQuery(IPSFeedDescriptor.class);
			Root<PSFeedDescriptor> root = criteriaQuery.from(PSFeedDescriptor.class);
			criteriaQuery.select(root).where(criteriaBuilder.and(criteriaBuilder.equal(root.get("site"), site),
					criteriaBuilder.equal(root.get("name"), name)));

			List<IPSFeedDescriptor> results = session.createQuery(criteriaQuery).
					getResultList();

			if (results.isEmpty())
				return null;
			return results.get(0);

	}

	private Session getSession(){
		return getSessionFactory().getCurrentSession();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.percussion.feeds.services.IPSFeedDao#findBySite(java.lang.String)
	 */
	@Override
	@Transactional
	public List<IPSFeedDescriptor> findBySite(String site) {

			Session session = getSession();
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<IPSFeedDescriptor> criteriaQuery = criteriaBuilder.createQuery(IPSFeedDescriptor.class);
			Root<PSFeedDescriptor> root = criteriaQuery.from(PSFeedDescriptor.class);
			criteriaQuery.where(criteriaBuilder.equal(root.get("site"),site));
			criteriaQuery.select(root);
			return  session.createQuery(criteriaQuery).
					getResultList();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.percussion.feeds.services.IPSFeedDao#getConnectionInfo()
	 */
	@Override
	@Transactional
	public IPSConnectionInfo getConnectionInfo() {

			Session session = getSession();

			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<PSConnectionInfo> criteriaQuery = criteriaBuilder.createQuery(PSConnectionInfo.class);
			Root<PSConnectionInfo> root = criteriaQuery.from(PSConnectionInfo.class);
			criteriaQuery.select(root);
			List<PSConnectionInfo> results = session.createQuery(criteriaQuery).
					getResultList();
			if (results.isEmpty())
				return null;
			return results.get(0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.percussion.feeds.services.IPSFeedDao#saveConnectionInfo(java.lang.
	 * String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	@Transactional
	public void saveConnectionInfo(String url, String user, String pass, boolean encrypted) {

			Session session = getSession();
			IPSConnectionInfo info = new PSConnectionInfo(url, user, pass, encrypted);
			session.saveOrUpdate(info);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.percussion.feeds.services.IPSFeedDao#saveDescriptors(java.util.List)
	 */
	@Transactional
	public void saveDescriptors(List<IPSFeedDescriptor> descriptors) {

			Session session = getSession();
			List<IPSFeedDescriptor> prepared = prepareDescriptors(descriptors);
			for (IPSFeedDescriptor p : prepared) {
				try {
					session.saveOrUpdate(p);
				}catch(Exception e){
					log.error("Skipping feed: {} on site {} with link: {} due to error: {} ",
							p.getName(),
							p.getSite(),
							p.getLink(),
							PSExceptionUtils.getMessageForLog(e));
				}
			}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.percussion.feeds.services.IPSFeedDao#deleteDescriptors(java.util.
	 * List)
	 */
	@Override
	@Transactional
	public void deleteDescriptors(List<IPSFeedDescriptor> descriptors) {

		Session session = getSession();
			List<IPSFeedDescriptor> prepared = prepareDescriptors(descriptors);
			for (IPSFeedDescriptor p : prepared) {
				session.delete(p);
			}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.percussion.feeds.services.IPSFeedDao#findAll()
	 */
	@Override
	@Transactional
	public List<IPSFeedDescriptor> findAll() {
		Session session = getSession();
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<IPSFeedDescriptor> criteriaQuery = criteriaBuilder.createQuery(IPSFeedDescriptor.class);
			Root<PSFeedDescriptor> root = criteriaQuery.from(PSFeedDescriptor.class);
			criteriaQuery.select(root);

		return session.createQuery(criteriaQuery).
					getResultList();

	}

	private List<IPSFeedDescriptor> prepareDescriptors(List<IPSFeedDescriptor> descriptors) {
		List<IPSFeedDescriptor> prepared = new ArrayList<>(descriptors.size());
		for (IPSFeedDescriptor d : descriptors) {
			prepared.add(new PSFeedDescriptor(d));
		}
		return prepared;
	}
}
