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

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.security.PSUserEntry;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.security.*;
import com.percussion.services.security.data.PSAccessLevelImpl;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.services.security.data.PSUserAccessLevel;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.security.IPSTypedPrincipal;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.security.acl.Permission;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the interface
 * {@link com.percussion.services.security.IPSAclService}.
 */
@Transactional()
@PSBaseBean("sys_aclService")
public class PSAclService implements IPSAclService
{

   private static final String OBJ_COM_VIS = "OBJ_COM_VIS_";
   
   private static final String COM_OBJ_VIS = "COM_OBJ_VIS_";

   
   static final long BIT32 = 0xFFFFFFFFL;

   /**
    * The hibernate session factory injected by spring
    */
   private SessionFactory sessionFactory;

   /**
    * Default ctor.
    */
   public PSAclService()
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.security.acl.IPSAclService#getUserAccessLevel(
    * com.percussion.utils.guid.IPSGuid)
    */
   public PSUserAccessLevel getUserAccessLevel(IPSGuid objectGuid)
   {
      if (objectGuid == null)
         throw new IllegalArgumentException("objectGuid may not be null");

      IPSAcl acl = loadAclForObject(objectGuid);
      return calculateUserAccessLevel(acl);
   }

   // see interface
   public PSUserAccessLevel calculateUserAccessLevel(IPSAcl acl)
   {
      if (acl != null && !(acl instanceof PSAclImpl))
      {
         throw new IllegalArgumentException("acl must be obtained from this interface.");
      }

      if (acl == null)
      {
         // no acl gets everything
         return new PSUserAccessLevel(new ArrayList<>(ms_defaultPerms));
      }

      // get the user entry.
      PSUserEntry userEntry = getUserEntry();
      if (userEntry == null)
      {
         // no permissions
         return new PSUserAccessLevel(new ArrayList<>(0));
      }
      List<String> roles = getUserRequest().getUserSession().getUserRoles();

      // allow all for user user role
      String superRole = PSServer.getProperty("superUserRole");
      if (superRole != null && roles.contains(superRole))
      {
         return new PSUserAccessLevel(new ArrayList<>(ms_defaultPerms));
      }
     
      // determine permissions
      Set<PSPermissions> userPerms = new HashSet<>();
      Set<String> userCommunities = getUserCommunities();
      Iterator<IPSAclEntry> entryIterator = new PSAclEntryImplIterator((PSAclImpl) acl, userEntry, userCommunities);
      while (entryIterator.hasNext())
         addPermissions(userPerms, entryIterator.next());

      if (m_grantDefaultCommunityVisibility && userCommunities.contains(DEFAULT_COMMUNITY_NAME))
         userPerms.add(PSPermissions.RUNTIME_VISIBLE);
      
      return new PSUserAccessLevel(userPerms);
   }

   /**
    * Get the user's communities as a set of names.
    * 
    * @return The set, never <code>null</code>, may be empty.
    */
   private Set<String> getUserCommunities()
   {
      PSRequest req = getUserRequest();
      PSUserSession sess = req.getUserSession();
      List<String> comms;
      try
      {
         comms = sess.getUserCommunityNames(req);
      }
      catch (PSInternalRequestCallException e)
      {
         throw new RuntimeException(e);
      }

      return new HashSet<>(comms);
   }

   /**
    * Add the permissions from the supplied entry to the supplied set.
    * 
    * @param userPerms The set to which the permissions are added, assumed not
    *           <code>null</code>.
    * @param entry The entry to use, assumed not <code>null</code>.
    */
   private void addPermissions(Set<PSPermissions> userPerms, IPSAclEntry entry)
   {
      Enumeration<Permission> enumPerms = entry.permissions();
      while (enumPerms.hasMoreElements())
      {
         PSPermissions perm = (PSPermissions) enumPerms.nextElement();
         userPerms.add(perm);

      }
   }

   /**
    * Get the current user's entry.
    * 
    * @return The entry, may be <code>null</code> if no current authenticated
    *         entry can be found.
    */
   private PSUserEntry getUserEntry()
   {
      PSUserEntry userEntry = null;
      PSUserEntry[] userEntries = getUserRequest().getUserSession().getAuthenticatedUserEntries();
      if (userEntries.length > 0)
         userEntry = userEntries[0];

      return userEntry;
   }

   /**
    * Get the current user's session.
    * 
    * @return The request, never <code>null</code>.
    */
   private PSRequest getUserRequest()
   {
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      if (req == null)
         throw new RuntimeException("No request found in the request info");

      return req;
   }

   public IPSAcl createAcl(IPSGuid objGuid, IPSTypedPrincipal owner)
   {
      if (objGuid == null)
         throw new IllegalArgumentException("objGuid may not be null");
      if (owner == null)
         throw new IllegalArgumentException("owner may not be null");

      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();

      PSAclEntryImpl aclEntry = new PSAclEntryImpl(owner);
     
      PSAccessLevelImpl perm = new PSAccessLevelImpl(aclEntry,PSPermissions.OWNER);
      aclEntry.addPermission(perm);

      PSAclImpl acl = new PSAclImpl("temp", aclEntry);
      acl.setGUID(guidMgr.createGuid(PSTypeEnum.ACL));
      acl.setObjectId(objGuid.getUUID());
      acl.setObjectType(objGuid.getType());
      addToAclCache(acl);
      getSession().persist(acl);
      return acl;
   }

   private void addToAclCache(PSAclImpl acl)
   {

      setupAclMap();
      ms_objectIdToAclIdMap.put(acl.getObjectGuid(), acl.getGUID());
      
   }

   /**
    * Clears the ACL cache from memory.
    */
   public final void clearCache(){

         if (ms_objectIdToAclIdMap != null) {
            ms_objectIdToAclIdMap.clear();
         }

   }
   // @see IPSAclService#loadModifiableAcls(List)
   public List<IPSAcl> loadAclsModifiable(List<IPSGuid> aclGuids) throws PSSecurityException
   {
      List<IPSAcl> results = doLoadModifiableAcls(aclGuids, false);
      if (aclGuids != null && results.size() != aclGuids.size())
      {
         throw new PSSecurityException(IPSSecurityErrors.ACL_NOT_FOUND, aclGuids);
      }

      return results;

   }

   // @see IPSAclService#loadModifiableAclsForObjects(List)
   public List<IPSAcl> loadAclsForObjectsModifiable(List<IPSGuid> objectGuids) {
      return doLoadAclsForObjects(objectGuids);
   }

   @SuppressWarnings("unchecked")
   private List<IPSAcl> doLoadAclsForObjects(List<IPSGuid> objectGuids)
   {  
      setupAclMap();
      
      if (objectGuids==null)
         return loadAllAcls();
      
      List<IPSAcl> acls = new ArrayList<>();
      List<IPSAcl> acl;
      for (IPSGuid guid : objectGuids)
      {


         IPSGuid aclId = ms_objectIdToAclIdMap.get(guid);

         if (aclId!=null)
         {
            acl = doLoadModifiableAcls(Collections.singletonList(aclId), true);
            acls.add(acl.get(0));
         }
         else {
            //ACL is not in cache.
            Session session = getSession();
            Criteria crit = session.createCriteria(PSAclImpl.class).add(Restrictions.eq("objectId",Long.valueOf(guid.getUUID())));
            List<PSAclImpl> results = crit.list();

            if(results != null && results.size() >0) {
               acls.addAll(results);

               //Add to cache
               for (PSAclImpl a : results) {
                  ms_objectIdToAclIdMap.put(guid, a.getGUID());
               }
            }else{
               acls.add(null);  //Downstream behavior looks for null acls.
            }

         }

      }
         
      return acls;
     
   }
   
   public void setupAclMap()
   {

      if (ms_objectIdToAclIdMap==null || ms_objectIdToAclIdMap.size()==0)
      {
         ms_objectIdToAclIdMap.clear();
         List<IPSAcl> acls = loadAllAcls();
         for (IPSAcl acl : acls)
         {
             ms_objectIdToAclIdMap.put(acl.getObjectGuid(), acl.getGUID());
         }
      }


   }
   
   private List<IPSAcl> loadAllAcls()
   {
      synchronized (this) {
         getSession().flush();
         List<IPSAcl> acls = getSession().createCriteria(PSAclImpl.class).setCacheable(false).list();
         for (IPSAcl acl : acls) {
            if (acl.getObjectId() >> 32 != 0) {
               ms_logger.error("Fixing acl entry with bad id " + acl.getObjectId());
               PSAclImpl dup = getSession().get(PSAclImpl.class, acl.getObjectId() & BIT32);
               if (dup != null) {
                  ms_objectIdToAclIdMap.remove(acl.getObjectId());
                  getSession().delete(acl);
               } else {
                  acl.setObjectId(acl.getObjectId() & BIT32);
               }
            }
         }
         return acls;
      }
     
   }

   /**
    * Loads ACLs from the DB, never from cache and doesn't save the loaded ACLs
    * to cache.
    * 
    * @param aclGuids Guids to load. If <code>null</code>, all ACLs are loaded.
    *           If empty, immediately returns an empty list.
    * @return An ACL for each supplied guid for which one was found. The size of
    *         this set may be less than the supplied list. Never
    *         <code>null</code>. Although a <code>List</code>, no order is
    *         guaranteed.
    */
   @SuppressWarnings("unchecked")
   private List<IPSAcl> doLoadModifiableAcls(List<IPSGuid> aclGuids, boolean returnNulls)
   {
      
      if (aclGuids == null) 
         return loadAllAcls();
      List<IPSAcl> aclList = new ArrayList<>();
      for (IPSGuid aclGuid : aclGuids)
      {
         IPSAcl acl = aclGuid == null ? null : getSession().get(PSAclImpl.class, aclGuid.longValue());
         
         if (acl!=null)
            ((PSAclImpl)acl).fixOwner();
         
         if (acl != null || returnNulls) 
            aclList.add(acl);
      }
  
      return aclList;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.security.acl.IPSAclService#loadAcls(java.util.Set)
    */
   @SuppressWarnings(value =
   {"unchecked"})
   public List<IPSAcl> loadAcls(List<IPSGuid> aclGuids) throws PSSecurityException
   {
      if (aclGuids != null && aclGuids.isEmpty())
         throw new IllegalArgumentException("ids cannot be empty");

      List<IPSAcl> aclList;

      aclList = doLoadModifiableAcls(aclGuids, false);

      if (aclGuids != null && aclList.size() != aclGuids.size())
      {
         throw new PSSecurityException(IPSSecurityErrors.ACL_NOT_FOUND, -1);
      }
      return aclList;
   }


   // see IPSAclService
   public Collection<IPSGuid> findObjectsVisibleToCommunities(List<String> communityNames, PSTypeEnum type)
   {
      return findObjectsVisibleToCommunities(communityNames, type, null);
   }

   // see IPSAclService
   @SuppressWarnings("unchecked")
   private Collection<IPSGuid> findObjectsVisibleToCommunities(List<String> communityNames, PSTypeEnum type,
         List<IPSGuid> guids)
   {
      if (communityNames == null || communityNames.isEmpty())
      {
         ms_logger.debug("findObjectsVisibleToCommunities is called with empty"
               + "communityNames returning empty collection.");
         return Collections.emptyList();
      }
      
      List<String> communities = new ArrayList<>();
      communities.add("AnyCommunity");
      communities.addAll(communityNames);
      
      
      Criteria crit = getSession().createCriteria(PSAclImpl.class);
      // May have better performance returning more object and filtering in java for communityNames or types.
      if (type!=null)
            crit = crit.add( Restrictions.eq("objectType", (int)type.getOrdinal()));
      
           List<PSAclImpl> acls =  crit.createCriteria("entries")
            .add( Restrictions.and(
                     Restrictions.eq("type", (int)PrincipalTypes.COMMUNITY.getOrdinal()),
                     Restrictions.in("name", communities)
                )
                 
              )  
            .createCriteria("psPermissions")
            .add( Restrictions.eq("permission", (int)PSPermissions.RUNTIME_VISIBLE.getOrdinal()))
            .setCacheable(true).list();

      Collection<IPSTypedPrincipal> principals = new ArrayList<IPSTypedPrincipal>();
      for (String comm : communityNames)
      {
         principals
                 .add(new PSTypedPrincipal(comm, PrincipalTypes.COMMUNITY));
      }
           List<IPSGuid> retGuids = new ArrayList<>();
           for (PSAclImpl acl:acls)
           {
              boolean visible = false;
              for (IPSTypedPrincipal p : principals)
              {
                 // Return all acls for the "Default" community if grant all setting is true
                 if (m_grantDefaultCommunityVisibility && DEFAULT_COMMUNITY_NAME.equals(p.getName()))
                 {
                    retGuids.add(acl.getObjectGuid());
                    continue;
                 }

                 try
                 {
                    IPSAclEntry entry = acl.findEntry(p);
                    if (entry != null)
                    {
                       visible =
                               entry.checkPermission(PSPermissions.RUNTIME_VISIBLE);
                       if (visible)
                          break;
                    }
                    else
                    {
                       entry = acl.findDefaultEntry(true);
                       if (entry != null)
                       {
                          visible =
                                  entry.checkPermission(PSPermissions.RUNTIME_VISIBLE);
                          if (visible)
                             break;
                       }
                    }
                 }
                 catch (SecurityException e)
                 {
                    // Treat as no permission
                 }
              }

              if (visible)
              {
                 retGuids.add(acl.getObjectGuid());
              }
           }
    
      return retGuids;

      
   }

   // see IPSAclService
   @SuppressWarnings(value =
   {"unchecked"})
   public IPSAcl loadAcl(IPSGuid aclGuid) throws PSSecurityException
   {
      List<IPSAcl> result = loadAcls(Collections.singletonList(aclGuid));
      if (result == null)
      {
         throw new PSSecurityException(IPSSecurityErrors.ACL_NOT_FOUND, aclGuid.toString());
      }
      return result.get(0);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.security.acl.IPSAclService#loadAclsForObjects(
    * java.util.List)
    */
   @SuppressWarnings(value =
   {"unchecked"})
   public List<IPSAcl> loadAclsForObjects(List<IPSGuid> objectGuids)
   {
      if (null == objectGuids)
         throw new IllegalArgumentException("objectGuids cannot be null");
      if (objectGuids.isEmpty())
         return Collections.emptyList();

      return doLoadAclsForObjects(objectGuids);

   }

   // see interface
   public IPSAcl loadAclForObject(IPSGuid objectGuid)
   {
       if(objectGuid != null)
            return loadAclsForObjects(Collections.singletonList(objectGuid)).get(0);
        else
            return null;
   }

   // see interface
   public IPSAcl loadAclForObjectModifiable(IPSGuid objectGuid)
   {
      List<IPSAcl> acls = loadAclsForObjectsModifiable(Collections.singletonList(objectGuid));
      return acls.isEmpty() ? null : acls.get(0);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.security.acl.IPSAclService#saveAcls(java.util.Set)
    */
   public List<IPSAcl> saveAcls(List<IPSAcl> aclList) throws PSSecurityException
   {
         List<IPSAcl> result = internalPersist(aclList);
         // need to really pass back aclList as return but not changing interface at this time.
         int i = 0;
         for (IPSAcl acl : result) {
            PSAclImpl orig = (PSAclImpl) aclList.get(i++);
            if (orig.getGUID() == null)
               orig.setGUID(acl.getGUID());
         }

         return result;

   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.security.acl.IPSAclService#saveAcls(java.util.Set)
    */
   public List<IPSAcl> internalPersist(List<IPSAcl> aclList) throws PSSecurityException
   {


      List<IPSAcl> updatedList = new ArrayList<>();

      for (IPSAcl iacl : aclList)
      {
         if(iacl != null) {
            try {
               ms_logger.debug("Saving ACL:" + iacl.toString());
               updatedList.add((IPSAcl) getSession().merge(iacl));
                ms_logger.debug("Save complete.");
            } catch (Exception ex) {
               try {
                  ms_logger.error("Error persisting Acl: " + ((PSAclImpl) iacl).toXML(), ex);
               } catch (Exception e) {
                  ms_logger.error("Error persisting Acl: " + iacl.getId());
               }
            }
         }
      }

      getSession().flush();

      return updatedList;
   }

   public void deleteAcl(IPSGuid aclGuid) throws PSSecurityException
   {

         try {
            List<IPSAcl> acls = doLoadModifiableAcls(Collections.singletonList(aclGuid), false);
            IPSAcl acl = acls.isEmpty() ? null : acls.get(0);
            if (acl != null) {
               ms_objectIdToAclIdMap.remove(acl.getObjectGuid());
               getSession().delete(acl);
            }
         } catch (DataAccessException e) {
            throw new PSSecurityException(IPSSecurityErrors.ACL_DELETE_ERROR, e, aclGuid.longValue(),
                    e.getLocalizedMessage());
         }

   }

   /**
    * If set to <code>true</code>, all design objects will automatically give
    * runtime visibility to the "Default" community. Used by CM1 to reduce
    * reliance on community settings which are no longer relevant.
    * 
    * @param always <code>true</code> value to always grant visibility to "Default",
    * <code>false</code> work "normally".
    */
   @Value("true")
   public void setGrantDefaultCommunityVisibility(boolean always)
   {
      m_grantDefaultCommunityVisibility = always;
   }

   /**
    * List of all permissions used by default, never <code>null</code>.
    */
   private static List<PSPermissions> ms_defaultPerms = new ArrayList<>();

   /**
    * Name of the community granted runtime visibility by m_grantDefaultCommunityVisibility
    */
   private static final String DEFAULT_COMMUNITY_NAME = "Default";
   
   /**
    * Determine if the "Default" community should always be granted runtime visibility.
    * Expected to be set in the beans definition.
    */
   private boolean m_grantDefaultCommunityVisibility = false;


   static
   {
      // default is all permissions except RUNTIME_VISIBLE
      for (PSPermissions perm : PSPermissions.values())
      {
         if (perm.equals(PSPermissions.RUNTIME_VISIBLE))
            continue;
         ms_defaultPerms.add(perm);
      }
   }

   /**
    * The hibernate session factory injected by spring
    * 
    * @param sessionFactory
    */
   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory)
   {
      this.sessionFactory = sessionFactory;
   }

   /**
    * @return The hibernate session factory injected by spring
    */
   public Session getSession()
   {
      return sessionFactory.getCurrentSession();
   }

   private volatile Map<IPSGuid,IPSGuid>  ms_objectIdToAclIdMap = new ConcurrentHashMap<>();


   /**
    * Logger for this class.
    */

   private static Logger ms_logger = LogManager.getLogger(PSAclService.class);

   @Override
   public Collection<IPSGuid> filterByCommunities(List<IPSGuid> objectIds, List<String> communityNames)
   {
      if (communityNames == null)
         return objectIds;

      return findObjectsVisibleToCommunities(communityNames, null, objectIds);
   }
   
  
}
