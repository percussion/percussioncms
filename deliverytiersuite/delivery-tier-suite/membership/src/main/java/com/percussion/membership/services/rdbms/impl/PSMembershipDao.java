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
package com.percussion.membership.services.rdbms.impl;

import com.percussion.membership.data.IPSMembership;
import com.percussion.membership.data.IPSMembership.PSMemberStatus;
import com.percussion.membership.data.PSAccountSummary;
import com.percussion.membership.data.rdbms.impl.PSMembership;
import com.percussion.membership.services.IPSMembershipDao;
import com.percussion.membership.services.PSMemberExistsException;
import org.apache.commons.lang.Validate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author jayseletz
 *
 */
@Transactional
public class PSMembershipDao extends HibernateDaoSupport implements IPSMembershipDao
{

    private static final String ACTION_ACTIVATE = "Activate";
    private static final String ACTION_BLOCK = "Block";

    @Override
    public IPSMembership findMemberBySessionId(String sessionId) throws Exception
    {
        Validate.notEmpty(sessionId);

        Session session = getSession();
        try
        {
            IPSMembership membership = null;

            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<PSMembership> criteriaQuery = criteriaBuilder.createQuery(PSMembership.class);
            Root<PSMembership> root = criteriaQuery.from(PSMembership.class);
            criteriaQuery.select(root).where(criteriaBuilder.like(root.get("sessionId"), sessionId));
            List<PSMembership> result = session.createQuery(criteriaQuery).getResultList();

            if (!result.isEmpty())
            {
                if (result.size() > 1)
                {
                    // this would be a bug
                    throw new IllegalStateException("More than one membership entry found for sessionID: " + sessionId);
                }

                membership = (IPSMembership) result.get(0);
            }

            return membership;
        }
        finally
        {
            // session.close();
        }
    }

    private Session getSession(){

        return getSessionFactory().getCurrentSession();

    }

    @Override
    public IPSMembership findMemberByUserId(String userId)
    {
        Validate.notEmpty(userId);

        Session session = getSession();
        try
        {
            return findMember(userId, session);
        }
        finally
        {
            // session.close();
        }
    }


    @Override
    public IPSMembership findMemberByPwdResetKey(String pwdResetKey)
    {
        Validate.notEmpty(pwdResetKey);

        Session session = getSession();
        try
        {
            IPSMembership membership = null;

            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<PSMembership> criteriaQuery = criteriaBuilder.createQuery(PSMembership.class);
            Root<PSMembership> root = criteriaQuery.from(PSMembership.class);
            criteriaQuery.select(root).where(criteriaBuilder.like(root.get("pwdResetKey"), pwdResetKey));
            List<PSMembership> result = session.createQuery(criteriaQuery).getResultList();
            if (!result.isEmpty())
            {
                if (result.size() > 1)
                {
                    // this would be a bug
                    throw new IllegalStateException("More than one membership entry found for pwdResetKey: " + pwdResetKey);
                }

                membership = (IPSMembership) result.get(0);
            }

            return membership;
        }
        finally
        {
            // session.close();
        }
    }

    @Override
    public IPSMembership createMember(String userId, String password, PSMemberStatus status) throws Exception
    {
        Validate.notEmpty(userId, "userId may not be null or empty");
        Validate.notEmpty(password, "password may not be null or empty");
        Validate.notNull(status, "status must not be null");
        Session session = getSession();
        try
        {
            validateNewMember(userId, session);

        }
        finally
        {
            // session.close();
        }

        IPSMembership membership = new PSMembership();
        membership.setUserId(userId);
        membership.setPassword(password);
        membership.setStatus(status);
        membership.setLastAccessed(new Date());

        return membership;
    }

    @Override
    public void saveMember(IPSMembership member) throws Exception
    {
        Validate.notNull(member);

        Session session = getSession();
        try
        {
            if (member.getId().equals("0"))
                validateNewMember(member.getUserId(), session);
            session.saveOrUpdate(member);
            session.flush();
        }
        finally
        {
            // session.close();
        }
    }


    @Override
    public List<IPSMembership> findMembers() throws Exception
    {
        Session session = getSession();
        try
        {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<IPSMembership> criteriaQuery = criteriaBuilder.createQuery(IPSMembership.class);
            Root<PSMembership> root = criteriaQuery.from(PSMembership.class);
            criteriaQuery.select(root).orderBy(criteriaBuilder.asc(root.get("userId")));

            List<IPSMembership> resultList = session.createQuery(criteriaQuery).getResultList();

            return resultList;

        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
        finally
        {
            // session.close();
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.membership.services.IPSMembershipDao#changeStatusAccount(
     * com.percussion.membership.data.PSAccountSummary)
     */
    @Override
    public void changeStatusAccount(PSAccountSummary account) throws Exception
    {
        // Validates the parameters
        Validate.notEmpty(account.getEmail(), "User email may not be null or empty");
        Validate.notEmpty(account.getAction(), "Action may not be null or empty");

        Session session = getSession();
        try
        {
            IPSMembership member = findMemberByUserId(account.getEmail());

            if (member == null)
            {
                throw new Exception("Member not found.");
            }

            // Sets the new status
            if (account.getAction().equalsIgnoreCase(ACTION_ACTIVATE))
            {
                member.setStatus(PSMemberStatus.Active);
            }
            else if (account.getAction().equalsIgnoreCase(ACTION_BLOCK))
            {
                member.setStatus(PSMemberStatus.Blocked);
            }
            else
            {
                throw new Exception("Action not allowed.");
            }

            session.saveOrUpdate(member);
            session.flush();
        }
        finally
        {
            //  session.close();
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.membership.services.IPSMembershipDao#deleteAccount(java.lang.String)
     */
    @Override
    public void deleteAccount(String email) throws Exception
    {
        // Validates the parameters
        Validate.notEmpty(email, "User email may not be null or empty");

        Session session = getSession();
        try
        {
            IPSMembership member = findMemberByUserId(email);
            if (member == null)
            {
                throw new Exception("Member not found.");
            }

            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaDelete<PSMembership> deleteQuery = builder.createCriteriaDelete(PSMembership.class);
            Root<PSMembership> root = deleteQuery.from(PSMembership.class);
            deleteQuery.where(root.get("id").in(Long.valueOf(member.getId())));
            session.createQuery(deleteQuery).executeUpdate();

        }
        finally
        {
            //  session.close();
        }
    }

    /**
     * Validate a member with the supplied userId does not already exist
     *
     * @param userId Assumed not <code>null</code> or empty.
     * @param session The session to use, assumed not <code>null</code>.
     *
     * @throws PSMemberExistsException if a member with the same name already exists.
     */
    private void validateNewMember(String userId, Session session) throws PSMemberExistsException
    {
        if (findMember(userId, session) != null)
        {
            throw new PSMemberExistsException(userId);
        }
    }

    /**
     * Helper method to find the member by user id w/in a session.
     *
     * @param userId Assumed not <code>null</code> or empty.
     * @param session Assumed not <code>null</code>.
     *
     * @return The member, or <code>null</code> if not found.
     */
    private IPSMembership findMember(String userId, Session session)
    {
        IPSMembership membership = null;

        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<PSMembership> criteriaQuery = criteriaBuilder.createQuery(PSMembership.class);
        Root<PSMembership> root = criteriaQuery.from(PSMembership.class);

        Expression<String> uid = root.get("userId");
        Expression<String> upper = criteriaBuilder.upper(uid);
        Predicate ctfPredicate = criteriaBuilder.like(upper,userId.toUpperCase());
        criteriaQuery.select(root).where(criteriaBuilder.and(ctfPredicate));

        // criteriaQuery.select(root).where(criteriaBuilder.like(root.get("userId"), userId));
        List<PSMembership> result = session.createQuery(criteriaQuery).getResultList();

        if (!result.isEmpty())
        {
            if (result.size() > 1)
            {
                // this would be a bug
                throw new IllegalStateException("More than one membership entry found for userId: " + userId);
            }

            membership = (IPSMembership) result.get(0);
//            //because we need to do case matching
//            if(!membership.getUserId().equals(userId)){
//                return null;
//            }
        }

        return membership;
    }


}
