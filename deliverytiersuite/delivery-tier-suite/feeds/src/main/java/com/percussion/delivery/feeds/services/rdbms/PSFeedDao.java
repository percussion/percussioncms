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
package com.percussion.delivery.feeds.services.rdbms;

import com.percussion.delivery.feeds.data.IPSFeedDescriptor;
import com.percussion.delivery.feeds.services.IPSConnectionInfo;
import com.percussion.delivery.feeds.services.IPSFeedDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.QueryHints;
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
					setHint(QueryHints.HINT_CACHEABLE,true).
					setHint(QueryHints.HINT_READONLY,true).
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
					setHint(QueryHints.HINT_CACHEABLE,true).
					setHint(QueryHints.HINT_READONLY,true).
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
					setHint(QueryHints.HINT_CACHEABLE,true).
					setHint(QueryHints.HINT_READONLY,true).
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
				session.saveOrUpdate(p);
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
					setHint(QueryHints.HINT_CACHEABLE,true).
					setHint(QueryHints.HINT_READONLY,true).
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
