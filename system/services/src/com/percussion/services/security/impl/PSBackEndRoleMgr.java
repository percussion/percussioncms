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
package com.percussion.services.security.impl;

import com.percussion.cms.objectstore.PSActionVisibilityContext;
import com.percussion.data.utils.PSHibernateEvictionTableUpdateHandler;
import com.percussion.design.objectstore.*;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.design.objectstore.server.PSXmlObjectStoreLockerId;
import com.percussion.error.PSException;
import com.percussion.security.PSBackendCataloger;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.IPSSecurityErrors;
import com.percussion.services.security.PSJaasUtils;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.data.PSBackEndRole;
import com.percussion.services.security.data.PSBackEndSubject;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.security.data.PSCommunityRoleAssociation;
import com.percussion.util.PSBaseBean;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.security.IPSPrincipalAttribute;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.*;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NamingException;
import javax.security.auth.Subject;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Implementation of the {@link IPSBackEndRoleMgr} service.
 */
@PSBaseBean("sys_backEndRoleMgr")
@Transactional
public class PSBackEndRoleMgr implements IPSBackEndRoleMgr {
    /**
     * Used for logging of all messages, never <code>null</code>.
     */
    private static final Logger ms_log = LogManager.getLogger(PSBackEndRoleMgr.class);

    /**
     * Used to set/get email on the subject
     */
    private static String EMAIL_ATTRIBUTE_NAME = "sys_email";
    private SessionFactory sessionFactory;

    /**
     * Default ctor, sets up necessary eviction handler.
     */
    public PSBackEndRoleMgr() {
        String[] tables = {
                "PSX_ROLES", "PSX_SUBJECTS", "RXCOMMUNITY", "RXCOMMUNITYROLE"
            };
        String[] pks = { "ID", "ID", "COMMUNITYID", null };
        Class[] clazz = {
                PSBackEndRole.class, PSBackEndSubject.class, PSCommunity.class,
                PSCommunityRoleAssociation.class
            };

        PSServer.addInitListener(new PSHibernateEvictionTableUpdateHandler(
                tables, pks, clazz));
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // see IPSBackendRoleMgr interface
    public List<String> getRhythmyxRoles() {
        List<String> roleList = new ArrayList<>();

        Iterator roles = loadRoleList(null).iterator();

        while (roles.hasNext()) {
            PSBackEndRole role = (PSBackEndRole) roles.next();
            roleList.add(role.getName());
        }

        return roleList;
    }

    // see IPSBackendRoleMgr interface
    public List<String> getRhythmyxRoles(String subjectName, int subjectType) {
        List<String> roleNames = new ArrayList<>();

        Session session = getSessionFactory().getCurrentSession();

        Criteria criteria = session.createCriteria(PSBackEndSubject.class);

        if (!StringUtils.isBlank(subjectName)) {
            criteria.add(Restrictions.ilike("name", subjectName));
        }

        if (subjectType != 0) {
            criteria.add(Restrictions.eq("type", subjectType));
        }

        Iterator subjects = criteria.list().iterator();

        while (subjects.hasNext()) {
            PSBackEndSubject sub = (PSBackEndSubject) subjects.next();
            Iterator roles = sub.getRoles().iterator();

            while (roles.hasNext()) {
                PSBackEndRole role = (PSBackEndRole) roles.next();
                roleNames.add(role.getName());
            }
        }

        return roleNames;
    }

    /*
     * //see base interface method for details
     */
    public void setRhythmyxRoles(String subjectName, int subjectType,
        Collection<String> roleNames) {
        RoleConfig config = null;

        try {
            config = getRoleConfig();
            setSubjectRoles(subjectName, subjectType, roleNames, config.roleCfg);
            config.objectStore.saveRoleConfiguration(config.roleCfg,
                config.lockId, config.securityToken);
        } catch (Exception e) {
            String msg = "Failed to set subject (name=" + subjectName +
                ", type=" + subjectType + ") to roles (names = " + roleNames +
                ").";
            ms_log.error(msg);
            throw new RuntimeException(msg, e);
        } finally {
            if (config != null) {
                releaseConfigLock(config.objectStore, config.lockId);
            }
        }
    }

    /*
     * //see base interface method for details
     */
    public void setRhythmyxRoles(Collection<String> subjectNames,
        int subjectType, Collection<String> roleNames) {
        RoleConfig config = null;

        try {
            // Getting role config just once as it is very costfull
            config = getRoleConfig();

            // Iterate to add every subject to the roles
            Iterator<String> subjectNamesIterator = subjectNames.iterator();

            while (subjectNamesIterator.hasNext()) {
                String subjectName = (String) subjectNamesIterator.next();
                setSubjectRoles(subjectName, subjectType, roleNames,
                    config.roleCfg);
            }

            // Saving role config just once as it is very costfull
            config.objectStore.saveRoleConfiguration(config.roleCfg,
                config.lockId, config.securityToken);
        } catch (Exception e) {
            String msg = "Failed to set subjects of type=" + subjectType +
                " to roles (names = " + roleNames + ").";
            ms_log.error(msg);
            throw new RuntimeException(msg, e);
        } finally {
            if (config != null) {
                releaseConfigLock(config.objectStore, config.lockId);
            }
        }
    }

    //see base interface method for details
    public void setSubjectEmail(String subjectName, String subjectEmail) {
        RoleConfig config = null;

        try {
            config = getRoleConfig();
            setUserEmail(subjectName, subjectEmail, config.roleCfg);
            config.objectStore.saveRoleConfiguration(config.roleCfg,
                config.lockId, config.securityToken);
        } catch (Exception e) {
            String msg = "Failed to set the email: " + subjectEmail +
                " to the user: " + subjectName;
            ms_log.error(msg);
        } finally {
            if (config != null) {
                releaseConfigLock(config.objectStore, config.lockId);
            }
        }
    }

    /**
     * See {@link IPSBackEndRoleMgr#update(String, String)}.
     */
    public PSBackEndRole update(String roleName, String description) {
        List<PSBackEndRole> roleList = findRolesByName(roleName);
        PSBackEndRole role = new PSBackEndRole();

        if (!roleList.isEmpty()) {
            role = roleList.get(0);
            role.setDescription(description);
        }

        // Persist the role
        getSessionFactory().getCurrentSession().saveOrUpdate(role);
        ms_log.info("Role description is updated for the role: " + roleName);

        return role;
    }

    /**
     * Sets the provided email on the provided subject.
     *
     * @param subjectName  never <code>null</code> or empty.
     * @param subjectEmail never <code>null</code> might be empty.
     * @param roleCfg      never <code>null</code>
     */
    private void setUserEmail(String subjectName, String subjectEmail,
        PSRoleConfiguration roleCfg) {
        PSRelativeSubject relativeSub = new PSRelativeSubject(subjectName,
                PSSubject.SUBJECT_TYPE_USER, null);
        PSSubject subject = roleCfg.getGlobalSubject(relativeSub, true);

        if (subject != null) {
            PSAttributeList attrs = subject.getAttributes();
            PSAttribute emailAttribute = attrs.getAttribute(EMAIL_ATTRIBUTE_NAME);

            //email attribute is not already set up on this user, so set the attribute and value
            if (emailAttribute == null) {
                attrs.setAttribute(EMAIL_ATTRIBUTE_NAME,
                    Arrays.asList(subjectEmail));
            } else {
                emailAttribute.setValues(Arrays.asList(subjectEmail));
            }
        }
    }

    /**
     * Updates the specified role configuration, to add the specified subject
     * to the specified roles if needed and remove the subject from roles that
     * are not in the specified roles.
     *
     * @param subjectName the name of the subject, assumed not blank.
     * @param subjectType the type of the subject.
     * @param roleNames   the names of the list of roles, assumed not
     *                    <code>null</code>, but may be empty.
     * @param roleCfg     the to be updated role configuration, assumed not
     *                    <code>null</code>.
     */
    private void setSubjectRoles(String subjectName, int subjectType,
        Collection<String> roleNames, PSRoleConfiguration roleCfg) {
        PSRelativeSubject relativeSub = new PSRelativeSubject(subjectName,
                subjectType, null);
        PSSubject sub = roleCfg.getGlobalSubject(relativeSub, true);

        Iterator roles = roleCfg.getRoles().iterator();

        while (roles.hasNext()) {
            PSRole role = (PSRole) roles.next();

            // if role is in "roleNames", add subject into it if needed
            if (roleNames.contains(role.getName())) {
                if (!role.containsCorrespondingSubject(sub)) {
                    role.getSubjects().add(relativeSub);
                }
            }
            // if role is not in "roleNames", then remove subject from the role
            else if (role.containsCorrespondingSubject(sub)) {
                removeSubjectFromRole(role, relativeSub);
            }
        }
    }

    /**
     * Removes the specified subject from the given role.
     *
     * @param role        the role that contains the specified subject, assumed not
     *                    <code>null</code>.
     * @param relativeSub the to be removed subject, assumed not <code>null</code>.
     */
    private void removeSubjectFromRole(PSRole role,
        PSRelativeSubject relativeSub) {
        PSRelativeSubject tgtSub = null;
        Iterator subs = role.getSubjects().iterator();

        while (subs.hasNext()) {
            PSRelativeSubject sub = (PSRelativeSubject) subs.next();

            if (sub.isMatch(relativeSub)) {
                tgtSub = sub;

                break;
            }
        }

        if (tgtSub != null) {
            role.getSubjects().remove(tgtSub);
        }
    }

    /**
     * Releases the specified lock.
     *
     * @param objectStore object store, used to release the lock, assumed not
     *                    <code>null</code>.
     * @param lockId      the to be released lock, assumed not <code>null</code>.
     */
    private void releaseConfigLock(PSServerXmlObjectStore objectStore,
        PSXmlObjectStoreLockerId lockId) {
        try {
            objectStore.releaseServerConfigLock(lockId);
        } catch (Exception e) {
            ms_log.warn(e); // ignore
        }
    }

    /**
     * Gets the back-end role configuration (along with other related objects,
     * which will be used to manage role configuration later).
     *
     * @return the role configuration (along with other related objects),
     * never <code>null</code>.
     */
    private RoleConfig getRoleConfig() {
        // get the security token
        PSRequest request = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
        PSSecurityToken tok = request.getSecurityToken();

        PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
        PSXmlObjectStoreLockerId lockId = null;
        RoleConfig config = null;

        try {
            String user = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
            lockId = new PSXmlObjectStoreLockerId(user, true, true,
                    tok.getUserSessionId());
            os.getServerConfigLock(lockId, 10);

            PSRoleConfiguration roleCfg = os.getRoleConfigurationObject(tok);

            config = new RoleConfig();
            config.lockId = lockId;
            config.objectStore = os;
            config.securityToken = tok;
            config.roleCfg = roleCfg;

            return config;
        } catch (PSException e) {
            String msg = "Failed to get role configuration";
            ms_log.warn(msg, e);
            throw new RuntimeException(msg, e);
        } finally {
            // release the lock in case of failure
            if ((config == null) && (lockId != null)) {
                releaseConfigLock(os, lockId);
            }
        }
    }

    // see IPSBackendRoleMgr interface
    public Set<IPSPrincipalAttribute> getRoleAttributes(String roleName) {
        if (StringUtils.isBlank(roleName)) {
            throw new IllegalArgumentException(
                "roleName may not be null or empty");
        }

        Set<IPSPrincipalAttribute> attrSet = new HashSet<>();

        Iterator attrs = PSBackendCataloger.getRoleAttributes(roleName)
                                           .iterator();

        while (attrs.hasNext()) {
            PSAttribute attr = (PSAttribute) attrs.next();
            attrSet.add(PSJaasUtils.convertAttribute(attr));
        }

        return attrSet;
    }

    // see IPSBackendRoleMgr interface
    public Set<Subject> getRoleSubjectAttributes(String roleName,
        String subjectNameFilter) {
        if (StringUtils.isBlank(roleName)) {
            throw new IllegalArgumentException(
                "roleName may not be null or empty");
        }

        Set<Subject> subSet = new HashSet<>();

        Iterator subs = PSBackendCataloger.getSubjectRoleAttributes(subjectNameFilter,
                0, roleName, null).iterator();

        while (subs.hasNext()) {
            PSSubject psSub = (PSSubject) subs.next();
            subSet.add(PSJaasUtils.convertSubject(psSub));
        }

        return subSet;
    }

    // see IPSBackendRoleMgr interface
    public Set<Subject> getGlobalSubjectAttributes(String subjectNameFilter,
        String attributeNameFilter, boolean includeEmptySubjects) {
        Set<Subject> subSet = new HashSet<>();

        Iterator subs = PSBackendCataloger.getSubjectGlobalAttributes(subjectNameFilter,
                0, null, attributeNameFilter, includeEmptySubjects).iterator();

        while (subs.hasNext()) {
            PSSubject psSub = (PSSubject) subs.next();
            subSet.add(PSJaasUtils.convertSubject(psSub));
        }

        return subSet;
    }

    // see IPSBackendRoleMgr interface
    public List<String> getCommunityRoles(int communityId) {
        List<String> roleNames = new ArrayList<>();

        PSGuid guid = new PSGuid(PSTypeEnum.COMMUNITY_DEF, communityId);
        PSCommunity[] comms = loadCommunities(new IPSGuid[] { guid });

        if (comms.length > 0) {
            PSCommunity comm = comms[0];
            Collection<IPSGuid> roleGuids = comm.getRoleAssociations();
            PSBackEndRole[] roles = loadRoles(roleGuids.toArray(
                        new IPSGuid[roleGuids.size()]));

            for (int i = 0; i < roles.length; i++) {
                if(roles != null && roles[i] != null) {
                    roleNames.add(roles[i].getName());
                }
            }
        }

        return roleNames;
    }

    /*
     * (non-Javadoc)
     *
     * @see IPSBackendRoleMgr#loadCommunities(IPSGuid[])
     */
    public PSCommunity[] loadCommunities(IPSGuid[] ids) {
        if (PSGuidUtils.isBlank(ids)) {
            throw new IllegalArgumentException("ids cannot be null or empty");
        }

        List communities = loadCommunityList(ids);

        return (PSCommunity[]) communities.toArray(new PSCommunity[communities.size()]);
    }

    public PSBackEndRole[] loadRoles(IPSGuid[] ids) {
        if (PSGuidUtils.isBlank(ids)) {
            throw new IllegalArgumentException("ids cannot be null or empty");
        }

        List roles = loadRoleList(ids);

        return (PSBackEndRole[]) roles.toArray(new PSBackEndRole[roles.size()]);
    }

    /**
     * Load all roles for the supplied ids.
     *
     * @param ids the ids for which to load the roles, may be <code>null</code>
     *            to load all roles, assumed not empty.
     * @return a list with all loaded roles in the same order as requested, never
     * <code>null</code> or empty.
     */
    private List loadRoleList(IPSGuid[] ids) {
        Session session = getSessionFactory().getCurrentSession();
        if (ids==null)
            return session.createCriteria(PSBackEndRole.class).setCacheable(true).list();
        else
            return Arrays.stream(ids)
                    .map(id -> session.get(PSBackEndRole.class,id.longValue()))
                    .collect(Collectors.toList());
    }

    /**
     * Load all communities for the supplied ids.
     *
     * @param ids the ids for which to load the communities, assumed not
     *            <code>null</code> or empty.
     * @return a list with all loaded communities in the same order as requested,
     * never <code>null</code> or empty.
     */
   @SuppressWarnings("unchecked")
   private List loadCommunityList(IPSGuid[] ids)
   {
        Session session = getSessionFactory().getCurrentSession();
       if (ids==null)
           return session.createCriteria(PSCommunity.class).setCacheable(true).list();
       else
           return Arrays.stream(ids)
                   .map(id -> session.get(PSCommunity.class,id.longValue()))
                   .collect(Collectors.toList());
    }

    /*
     * (non-Javadoc)
     *
     * @see IPSBackendRoleMgr#createRole(String)
     */
    public PSBackEndRole createRole(String name) {
        return createRole(name, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see IPSBackendRoleMgr#createRole(String, String)
     */
    public PSBackEndRole createRole(String name, String description) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }

        PSBackEndRole role = null;

        List<PSBackEndRole> beRoleList = findRolesByName(name);

        // Look for exact match
        boolean match = false;

        for (PSBackEndRole ber : beRoleList) {
            String lName = ber.getNormalizedName();

            if (lName.equals(name.toLowerCase())) {
                match = true;

                break;
            }
        }

        // Add role name if no match
        if (!match) {
            // Create guid and convert to id
            IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
            IPSGuid guid = guidMgr.createGuid(PSTypeEnum.ROLE);
            long id = guid.getUUID();

            // Build the new role
            role = new PSBackEndRole();
            role.setId(id);
            role.setName(name);
            role.setNormalizedName(StringUtils.lowerCase(name));

            if (description != null) {
                role.setDescription(description);
            }

            // Persist the role
            getSessionFactory().getCurrentSession().saveOrUpdate(role);
            ms_log.info("Role '" + name + "' added to Role Configuration.");
        }

        return role;
    }

    /*
     * (non-Javadoc)
     *
     * @see IPSBackendRoleMgr#deleteRole(String)
     */
    public void deleteRole(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }

        PSBackEndRole role = null;

        List<PSBackEndRole> beRoleList = findRolesByName(name);

        // Look for exact match
        for (PSBackEndRole ber : beRoleList) {
            String lName = ber.getNormalizedName();

            if (lName.equals(name.toLowerCase())) {
                role = ber;

                break;
            }
        }

        // Delete role name if found
        if (role != null) {
            // Delete the role
            getSessionFactory().getCurrentSession().delete(role);
            ms_log.info("Role '" + name + "' removed from Role Configuration.");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see IPSBackendRoleMgr#createCommunity(String, String)
     */
    public PSCommunity createCommunity(String name, String description) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }

        List<PSCommunity> communities = findCommunitiesByName(name);

        if (!communities.isEmpty()) {
            throw new IllegalArgumentException(
                "name must be unique across all existing communities");
        }

        return new PSCommunity(name, description);
    }

    /*
     * (non-Javadoc)
     *
     * @see IPSBackendRoleMgr#deleteCommunity(IPSGuid)
     */
    @SuppressWarnings("deprecation")
    public void deleteCommunity(IPSGuid id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

        Session s = getSessionFactory().getCurrentSession();

        try {
            PSCommunity community = loadCommunity(id);
            s.delete(community);

            // cleanup community compontents
            StringBuilder b = new StringBuilder();
            b.append("DELETE FROM ");
            b.append(qualify("RXCOMPONENTCOMMUNITY"));
            b.append(" WHERE COMMUNITYID = ?");

            SQLQuery st = s.createSQLQuery(b.toString());

            st.setLong(1, id.longValue());
            st.executeUpdate();

            // cleanup menu visibilities
            b = new StringBuilder();
            b.append("DELETE FROM ");
            b.append(qualify("RXMENUVISIBILITY"));
            b.append(" WHERE VISIBILITYCONTEXT = ?");
            b.append(" AND VALUE = ?");
            st = s.createSQLQuery(b.toString());
            st.setString(1, PSActionVisibilityContext.VIS_CONTEXT_COMMUNITY);
            st.setString(2, Long.toString(id.longValue()));
            st.executeUpdate();

            // cleanup autotranslations
            b = new StringBuilder();
            b.append("DELETE FROM ");
            b.append(qualify("PSX_AUTOTRANSLATION"));
            b.append(" WHERE COMMUNITYID = ?");
            st = s.createSQLQuery(b.toString());
            st.setLong(1, id.longValue());
            st.executeUpdate();
        } catch (SQLException e) {
            ms_log.error("Couldn't save community", e);
        } catch (NamingException e) {
            ms_log.error("Couldn't save community", e);
        } catch (PSSecurityException e) {
            // ignore non existing community
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see IPSBackendRoleMgr#findCommunitiesByName(String)
     */
   @SuppressWarnings("unchecked")
   public List<PSCommunity> findCommunitiesByName(String name)
   {
      Session session = sessionFactory.getCurrentSession();
      List<PSCommunity> communities;

         if (StringUtils.isBlank(name) || name.equals("%"))
         {
            communities = session.createCriteria(PSCommunity.class).setCacheable(true).list();
        }
         else
         {
        Criteria criteria = session.createCriteria(PSCommunity.class);
        criteria.add(Restrictions.ilike("name", name));
        criteria.addOrder(Order.asc("name"));
            criteria.setCacheable(true);
            communities = criteria.list();
         }

         return communities;
    }

    /*
     * (non-Javadoc)
     *
     * @see IPSBackendRoleMgr#loadCommunity(IPSGuid)
     */
    public PSCommunity loadCommunity(IPSGuid id) throws PSSecurityException {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

      Session session = sessionFactory.getCurrentSession();

         PSCommunity community = (PSCommunity)session.get(PSCommunity.class,id.longValue());

         if (community==null)
            throw new PSSecurityException(IPSSecurityErrors.MISSING_COMMUNITY,
                id);

         return community;
    }

    /*
     * (non-Javadoc)
     *
     * @see IPSBackendRoleMgr#saveCommunity(PSCommunity)
     */
    @SuppressWarnings("deprecation")
    public void saveCommunity(PSCommunity community) {
        if (community == null) {
            throw new IllegalArgumentException("community cannot be null");
        }

        Session session = getSessionFactory().getCurrentSession();

        try {
            if (community.getVersion() == null) {
                session.save(community);

                // This code has been duplicated in PSCommunityDefDependencyHandler
                // class to get the same functionality. As this code is a hack for
                // legacy functionality, this has not been moved to a new method and
                // exposed. When modifying this code update code in
                // PSCommunityDefDependencyHandler also.
                StringBuilder b = new StringBuilder();
                b.append("INSERT INTO ");
                b.append(qualify("RXCOMPONENTCOMMUNITY"));
                b.append(" SELECT COMPONENTID, ");
                b.append(community.getId());
                b.append(" FROM ");
                b.append(qualify("RXSYSCOMPONENT"));

                SQLQuery st = session.createSQLQuery(b.toString());
                st.executeUpdate();
            } else {
                PSCommunity current = null;

                try {
                    current = loadCommunity(community.getGUID());
                } catch (PSSecurityException e) {
                    // ignore, should never happen
                }

                if (current != null) {
                    // copy over properties
                    current.merge(community);
                    community = current;
                }

                session.merge(community);
            }
        } catch (SQLException e) {
            ms_log.error("Couldn't save community", e);
        } catch (NamingException e) {
            ms_log.error("Couldn't save community", e);
        }
    }

    /**
     * Qualify a tablename for a native SQL statement
     *
     * @param tablename the tablename, assumed not <code>null</code>
     * @return the qualified name
     * @throws NamingException
     * @throws SQLException
     */
    private Object qualify(String tablename)
        throws NamingException, SQLException {
        PSConnectionDetail detail = PSConnectionHelper.getConnectionDetail();

        return PSSqlHelper.qualifyTableName(tablename, detail.getDatabase(),
            detail.getOrigin(), detail.getDriver());
    }

    /*
     * (non-Javadoc)
     *
     * @see IPSBackendRoleMgr#findRolesByName(String)
     */
    public List<PSBackEndRole> findRolesByName(String name) {
        Session session = getSessionFactory().getCurrentSession();

        if (StringUtils.isBlank(name)) {
            name = "%";
        }

        Criteria criteria = session.createCriteria(PSBackEndRole.class);
        criteria.add(Restrictions.ilike("name", name));
        criteria.addOrder(Order.asc("name"));

        return (List<PSBackEndRole>) criteria.list();
    }

    /*
     * (non-Javadoc)
     *
     * @see IPSBackendRoleMgr#findRolesByName(String)
     */
    public PSBackEndRole findRoleById(long id) {
        Session session = getSessionFactory().getCurrentSession();

        return session.byId(PSBackEndRole.class).load(id);
    }

    public List<PSCommunityRoleAssociation> findCommunitiesByRole(
        List<IPSGuid> roleIds) {
        if ((roleIds == null) || roleIds.isEmpty()) {
            throw new IllegalArgumentException(
                "roleIds may not be null or empty");
        }

        Session session = getSessionFactory().getCurrentSession();

        IPSGuid[] ids = roleIds.toArray(new IPSGuid[roleIds.size()]);
        Query query = session.createQuery(
                "from PSCommunityRoleAssociation cr where cr.id.roleId in (:ids)");
        query.setParameterList("ids", PSGuidUtils.toLongArray(ids));

        return query.list();
    }

    /**
     * The container class, used to hold the role configuration and its
     * related objects, which are used to update the role configuration.
     */
    private class RoleConfig {
        /**
         * The role configuration, not <code>null</code> after initialized.
         */
        PSRoleConfiguration roleCfg;

        /**
         * The server lock, not <code>null</code> after initialized.
         * This lock must be released after its usage.
         */
        PSXmlObjectStoreLockerId lockId;

        /**
         * Security token, used to retrieve and save the role configuration.
         * Not <code>null</code> or modified after initialized.
         */
        PSSecurityToken securityToken;

        /**
         * The object store, used to retrieve and save the role configuration.
         * Not <code>null</code> or modified after initialized.
         */
        PSServerXmlObjectStore objectStore;
    }
}
