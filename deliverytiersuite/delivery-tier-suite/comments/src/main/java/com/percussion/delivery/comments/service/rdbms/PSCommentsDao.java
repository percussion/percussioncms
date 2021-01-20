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
package com.percussion.delivery.comments.service.rdbms;

import com.percussion.delivery.comments.data.IPSComment;
import com.percussion.delivery.comments.data.IPSComment.APPROVAL_STATE;
import com.percussion.delivery.comments.data.IPSDefaultModerationState;
import com.percussion.delivery.comments.data.PSCommentCriteria;
import com.percussion.delivery.comments.data.PSCommentSort.SORTBY;
import com.percussion.delivery.comments.data.PSPageInfo;
import com.percussion.delivery.comments.services.IPSCommentsDao;
import com.percussion.delivery.comments.services.PSCommentsService;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author erikserating
 *
 */
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public class PSCommentsDao extends HibernateDaoSupport implements IPSCommentsDao
{

    /*
     * (non-Javadoc)
     *
     * @see
     * com.percussion.delivery.comments.service.rdbms.IPSCommentDao#find(com
     * .percussion.delivery.comments.data.PSCommentCriteria, boolean)
     */
    @Transactional
    public List<IPSComment> find(PSCommentCriteria criteria) throws Exception
    {
        Session session = getSession();

        try
        {
            Criteria queryCriteria = session.createCriteria(PSComment.class);
            prepareCriteria(criteria, queryCriteria);
            return queryCriteria.list();
        }
        finally
        {
           // session.close();
        }
    }


    public Set<String> findSitesForCommentIds(Collection<String> ids) throws Exception
    {
        Collection<Long> longIds = new ArrayList<Long>(ids.size());
        for(String s : ids)
            longIds.add(Long.valueOf(s));
        String selectComments = "select site from PSComment where id in (:idList)";
        List<String> siteNames = (List<String>) this.getHibernateTemplate().findByNamedParam(selectComments, "idList", longIds);
        return new HashSet<String>(siteNames);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.percussion.delivery.comments.service.rdbms.IPSCommentDao#save(com
     * .percussion.delivery.comments.data.IPSComment)
     */

    public void save(IPSComment comment) throws Exception
    {
        PSComment hComment = new PSComment(comment);
        hComment.setId(comment.getId());
        getHibernateTemplate().saveOrUpdate(hComment);
        comment.setId(hComment.getId());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.percussion.delivery.comments.service.rdbms.IPSCommentDao#delete(java
     * .util.Collection)
     */
    public void delete(Collection<String> commentIds) throws Exception
    {
        Collection<Long> longIds = new ArrayList<Long>(commentIds.size());
        for(String s : commentIds)
            longIds.add(Long.valueOf(s));
        Session session = getSession();
        try
        {
            session.createQuery("delete from PSComment where id in (:commentIds)").setParameterList("commentIds",
                    longIds).executeUpdate();
        }
        finally
        {
            //session.close();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.percussion.delivery.comments.service.rdbms.IPSCommentDao#moderate
     * (java.util.Collection,
     * com.percussion.delivery.comments.data.IPSComment.APPROVAL_STATE)
     */
    public void moderate(Collection<String> commentIds, APPROVAL_STATE newApprovalState) throws Exception
    {
        Collection<Long> longIds = new ArrayList<Long>(commentIds.size());
        for(String s : commentIds)
            longIds.add(Long.valueOf(s));
        Session session = getSession();
        try
        {
            String updateQueryString = "update PSComment com set approvalState = :newApprovalState "
                    + "where com.id in (:idList) ";

            Query updateQuery = session.createQuery(updateQueryString);
            updateQuery.setParameter("newApprovalState", newApprovalState.toString());
            updateQuery.setParameterList("idList", longIds);
            updateQuery.executeUpdate();
        }
        finally
        {
            session.flush();
           // session.close();
        }
    }

    @Transactional(readOnly = true)
    public List<PSPageInfo> findPagesWithComments(String site) throws Exception
    {
        Session session = getSession();

        try
        {
            // FIXME: this query could lead to performance issues. All comments
            // (from the specified site, of course) are loaded
            // when getPagesWithComments is executed, to get all pages and do
            // pagination.
            // Maybe another table, "Pages", should be created to solve this.
            String stringQuery = "select pagePath, approvalState, count(*), viewed " + "from PSComment "
                    + "where site = :site " + "group by pagePath, approvalState, viewed ";

            Query query = session.createQuery(stringQuery);
            query.setParameter("site", site);

            List<Object[]> result = query.list();
            List<PSPageInfo> pages = new ArrayList<PSPageInfo>();
            for(Object[] r : result)
                pages.add(new PSPageInfo((String)r[0], (String)r[1], (Long)r[2], (Boolean)r[3]));
            return pages;

        }
        finally
        {
           // session.close();
        }

    }

    public APPROVAL_STATE findDefaultModerationState(String site)
            throws Exception {
        Session session = getSession();
        try
        {
            Query query = session.createQuery("from PSDefaultModerationState where site = :site");
            query.setParameter("site", site);
            List<Object> result = query.list();
            APPROVAL_STATE state = APPROVAL_STATE.APPROVED;
            if (!result.isEmpty())
            {
                state = APPROVAL_STATE.valueOf(((IPSDefaultModerationState) result.get(0)).getDefaultState());
            }
            return state;

        }
        finally
        {
           // session.close();
        }
    }

    public void saveDefaultModerationState(String sitename, APPROVAL_STATE state)
            throws Exception {
        Session session = getSession();
        try
        {
            IPSDefaultModerationState st = new PSDefaultModerationState(sitename, state.toString());
            session.saveOrUpdate(st);
            session.flush();
        }
        finally
        {
          //  session.close();
        }

    }

    /**
     * Prepares the Hibernate Criteria object according to the settings in
     * PSCommentCriteria object.
     *
     * @param criteria The comment criteria. Must not be <code>null</code>.
     * @param queryCriteria The Hibernate Criteria object.
     */
    private void prepareCriteria(PSCommentCriteria criteria, Criteria queryCriteria)
    {
        Conjunction ands = Restrictions.conjunction();
        Conjunction ors = null;

        // Username
        if (!StringUtils.isEmpty(criteria.getUsername()))
            ands.add(Restrictions.eq("username", criteria.getUsername()).ignoreCase());

        // Pagepath
        if (!StringUtils.isEmpty(criteria.getPagepath()))
            ands.add(Restrictions.eq("pagePath", criteria.getPagepath()).ignoreCase());

        // Tag
        if (!StringUtils.isEmpty(criteria.getTag()))
        {
            queryCriteria.createAlias("commentTags", "tag");
            ands.add(Restrictions.eq("tag.name", criteria.getTag()).ignoreCase());
        }

        // Approval state
        if (criteria.getState() != null)
            ands.add(Restrictions.eq("approvalState", criteria.getState().toString()));

        // Site
        if (!StringUtils.isEmpty(criteria.getSite()))
            ands.add(Restrictions.eq("site", criteria.getSite()).ignoreCase());

        // Viewed
        if (criteria.isViewed() != null)
            ands.add(Restrictions.eq("viewed", criteria.isViewed()));

        // Moderated
        if (criteria.isModerated() != null)
            ands.add(Restrictions.eq("moderated", criteria.isModerated()));

        // Last comment Id
        if (criteria.getLastCommentId() != null)
        {
            ors = Restrictions.conjunction();
            ors.add(Restrictions.eq("id", Long.valueOf(criteria.getLastCommentId())));
            ors.add(Restrictions.eq("site", criteria.getSite()).ignoreCase());
            if (!StringUtils.isEmpty(criteria.getPagepath()))
                ors.add(Restrictions.eq("pagePath", criteria.getPagepath()).ignoreCase());
        }

        if (ors != null)
            queryCriteria.add(Restrictions.or(ands, ors));
        else
            queryCriteria.add(ands);

        // Sorting
        if (criteria.getSort() != null && criteria.getSort().getSortBy() != null)
        {
            String field = PSCommentsService.SORTBY_FIELD_MAPPING.get(criteria.getSort().getSortBy());
            boolean isAscending = criteria.getSort().isAscending();

            if (isAscending)
                queryCriteria.addOrder(Order.asc(field));
            else
                queryCriteria.addOrder(Order.desc(field));
        }
        else
        {
            // By default, sort by CREATEDATE in descending order
            queryCriteria.addOrder(Order.desc(PSCommentsService.SORTBY_FIELD_MAPPING.get(SORTBY.CREATEDDATE)));
        }

        // Max results
        if (criteria.getMaxResults() > 0)
            queryCriteria.setMaxResults(criteria.getMaxResults());

        // Start index
        if (criteria.getStartIndex() > 0)
            queryCriteria.setFirstResult(criteria.getStartIndex());

        // Unique entites
        queryCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    }

    private Session getSession(){

        return getSessionFactory().getCurrentSession();

    }


}
