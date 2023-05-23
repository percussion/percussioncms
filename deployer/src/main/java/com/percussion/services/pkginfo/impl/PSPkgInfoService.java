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
package com.percussion.services.pkginfo.impl;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filestorage.data.PSBinaryMetaKey;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.data.PSPkgDependency;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageAction;
import com.percussion.services.ui.data.PSHierarchyNodeProperty;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the primary interface for the Package Information Service.  
 * <p>
 * For more information, consult the documentation in the interface definition
 * file @see IPSPkgInfoService
 *
 */
@PSBaseBean("sys_pkgInfoService")
@Transactional
public class PSPkgInfoService
implements IPSPkgInfoService
{
   private static final Logger log = LogManager.getLogger(PSPkgInfoService.class);

   @PersistenceContext
   private EntityManager entityManager;

   private Session getSession(){
      return entityManager.unwrap(Session.class);
   }



   //-------------------------------------------------------------------
   // Package Information (PSPkgInfo) service methods
   //-------------------------------------------------------------------

   /* (non-Javadoc)
    * @see IPSPkgInfoService#createPkgInfo(String name)
    */
   public PSPkgInfo createPkgInfo(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty string");
      
      PSPkgInfo pkgInfo = new PSPkgInfo();
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      pkgInfo.setGuid(guidMgr.createGuid(PSTypeEnum.PACKAGE_INFO));

      pkgInfo.setPackageDescriptorName(name);
      
      return pkgInfo;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.pkginfo.IPSPkgInfoService#createPkgInfoCopy(java.lang.String)
    */
   public PSPkgInfo createPkgInfoCopy(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty string");
      PSPkgInfo pkgInfo = createPkgInfo(name);
      PSPkgInfo pkgInfo1 = findPkgInfo(name);
      if(pkgInfo1 != null)
      {
         pkgInfo.setPublisherName(pkgInfo1.getPublisherName());
         pkgInfo.setPublisherUrl(pkgInfo1.getPublisherUrl());
         pkgInfo.setPackageDescription(pkgInfo1.getPackageDescription());
         pkgInfo.setPackageVersion(pkgInfo1.getPackageVersion());
         pkgInfo.setShippedConfigDefinition(pkgInfo1.getShippedConfigDefinition());
         pkgInfo.setLastAction(pkgInfo1.getLastAction());
         pkgInfo.setLastActionByUser(pkgInfo1.getLastActionByUser());
         pkgInfo.setLastActionStatus(pkgInfo1.getLastActionStatus());
         pkgInfo.setType(pkgInfo1.getType());
         pkgInfo.setPackageDescriptorName(pkgInfo1.getPackageDescriptorName());
         pkgInfo.setPackageDescriptorGuid(pkgInfo1.getPackageDescriptorGuid());
         pkgInfo.setCmVersionMinimum(pkgInfo1.getCmVersionMinimum());
         pkgInfo.setCmVersionMaximum(pkgInfo1.getCmVersionMaximum());      
      }
      return pkgInfo;
   }
   
   /* (non-Javadoc)
    * @see IPSPkgInfoService#savePkgInfo(PSPkgInfo obj)
    */
   public void savePkgInfo(PSPkgInfo obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");
      
       getSession().saveOrUpdate(obj);
   }

   /* (non-Javadoc)
    * @see IPSPkgInfoService#deletePkgInfo(IPSGuid id)
    */
   public void deletePkgInfo(IPSGuid pkgGuid)
   {
      deletePkgInfoChildren(pkgGuid);
      deletePkgInfoRow(pkgGuid);
   }

   /**
    * Deletes a row in the package info repository.
    *  
    * @param id the ID of the deleted package info, assumed not 
    * <code>null</code>.
    */
   private void deletePkgInfoRow(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      Session session = getSession();
      
      CriteriaBuilder builder = session.getCriteriaBuilder();
      CriteriaQuery<PSPkgInfo> criteria = builder.createQuery(PSPkgInfo.class);
      Root<PSPkgInfo> critRoot = criteria.from(PSPkgInfo.class);
      criteria.where(builder.equal(critRoot.get("guid"),id.longValue()));
      PSPkgInfo pkgInfo = session.createQuery(criteria).getSingleResult();

      if (pkgInfo == null)
            return;

         session.delete(pkgInfo);

   }
   
   /* (non-Javadoc)
    * @see IPSPkgInfoService#deleteCreatedPkg(String nameFilter)
    */
   public void deletePkgInfo(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException(
               "name may not be null or empty string");
      }
      
      PSPkgInfo info = findPkgInfo(name);
      if (null != info)
         deletePkgInfo(info.getGuid());
   }

   /* (non-Javadoc)
    * @see IPSPkgInfoService#findPkgInfos(String name)
    */
   @SuppressWarnings(value = { "unchecked" })
   public PSPkgInfo findPkgInfo(String name)
   {
      if (StringUtils.isBlank(name))
         return null;
     
      List<PSPkgInfo> pkgInfoList = null;
      Session session = getSession();

         Criteria criteria = session.createCriteria(PSPkgInfo.class);
         criteria.add(Restrictions.eq("descriptorName", name).ignoreCase());
         pkgInfoList = criteria.list();

      return pkgInfoList.isEmpty() ? null : pkgInfoList.get(0);
   }

   /* (non-Javadoc)
    * @see IPSPkgInfoService#findLatestPkgInfos(String nameFilter)
    */
   @SuppressWarnings(value = { "unchecked" })
   public List<PSPkgInfo> findAllPkgInfos()
   {
      List<PSPkgInfo>  pkgInfoList = null;
      Session session = getSession();

         Criteria criteria = session.createCriteria(PSPkgInfo.class);
         pkgInfoList = criteria.list();      

      return pkgInfoList;
   }


   /* (non-Javadoc)
    * @see IPSPkgInfoService#loadPkgInfo(IPSGuid id)
    */
   public PSPkgInfo loadPkgInfo(IPSGuid id) throws PSNotFoundException {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      return loadPkgInfoModifiable(id);

   }

   /* (non-Javadoc)
    * @see IPSPkgInfoService#loadPkgInfoModifiable(IPSGuid id)
    */
   public PSPkgInfo loadPkgInfoModifiable(IPSGuid id) throws PSNotFoundException {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      PSPkgInfo pkgInfo = getSession().
         get(PSPkgInfo.class, id.longValue());

      if (pkgInfo == null)
         throw new PSNotFoundException(id);

      return pkgInfo;
   }

   //----------------------------------------------------------------------------
   // Package Information Element (PSPkgElement) service methods
   //----------------------------------------------------------------------------
   
   public PSPkgElement createPkgElement(IPSGuid parentId)
   {
      PSPkgElement pkgElem = new PSPkgElement();
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      pkgElem.setGuid(guidMgr.createGuid(PSTypeEnum.PACKAGE_ELEMENT));
      pkgElem.setPackageGuid(parentId);
      return pkgElem;
   }
  
   /* (non-Javadoc)
    * @see IPSPkgInfoElement#savePkgInfo(PSPkgElemen)
    */
   public void savePkgElement(PSPkgElement obj)
   {
         if (obj == null)
            throw new IllegalArgumentException("obj may not be null");

         log.debug("Trying to save PackageElement: {}" , obj);
         getSession().saveOrUpdate(obj);
         log.debug("PackageElement save completed for: {}" , obj);


   }


   /* (non-Javadoc)
    * @see IPSPkgInfoService#deletePkgElement(IPSGuid)
    */
   public void deletePkgElement(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      Session session = getSession();


      CriteriaBuilder builder = session.getCriteriaBuilder();
      CriteriaQuery<PSPkgElement> criteria = builder.createQuery(PSPkgElement.class);
      Root<PSPkgElement> critRoot = criteria.from(PSPkgElement.class);
      criteria.select(critRoot);
      criteria.where(builder.equal(critRoot.get("guid"),id.longValue()));
      PSPkgElement pkgElement = session.createQuery(criteria).getSingleResult();


      if (pkgElement == null)
            return;

         session.delete(pkgElement);

   }


   /* (non-Javadoc)
    * @see IPSPkgInfoService#findPkgElementGuids(IPSGuid)
    */
   @SuppressWarnings(value = { "unchecked" })
   public List<IPSGuid> findPkgElementGuids(IPSGuid parentPkgInfoId)
   {
      if (parentPkgInfoId == null)
         throw new IllegalArgumentException("parentPkgInfoId may not be null");
      
      
      List<Long> longList;
      List<IPSGuid> guidList = new ArrayList<>();
      Session session = getSession();

         Criteria criteria = session.createCriteria(PSPkgElement.class);
         criteria.setProjection( Projections.property("guid"));
         criteria.add(Restrictions.eq("packageGuid",
                 parentPkgInfoId.longValue()));
         longList =  criteria.list();


      for (Long l : longList)
      {
         guidList.add( new PSGuid(PSTypeEnum.PACKAGE_ELEMENT, l));
      }
      
      return guidList;
   }
   
 
   
   /* (non-Javadoc)
    * @see IPSPkgInfoService#findPkgElement(IPSGuid)
    */
   public PSPkgElement findPkgElement(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      
      PSPkgElement pkgElement = null;
      Session session = getSession();

         Criteria criteria = session.createCriteria(PSPkgElement.class);
         criteria.add(Restrictions.eq("guid", id.longValue()));
         pkgElement = (PSPkgElement) criteria.uniqueResult();

      return pkgElement;
   }

   /* (non-Javadoc)
    * @see IPSPkgInfoService#findPkgElementByObject(IPSGuid)
    */
   public PSPkgElement findPkgElementByObject(IPSGuid objId)
   {
      if (objId == null)
         throw new IllegalArgumentException("objId may not be null");
             
      Session session = getSession();

         /*
          * A query is required to handle the following special case:
          * Install a pkg w/ object foo
          * Uninstall that pkg, but foo doesn't get deleted due to content deps
          * Install a different pkg with an object named foo
          * We don't want to find the foo object associated with the uninstalled 
          * pkg
          */
         String query = "select p from PSPkgElement p, PSPkgInfo i where"
            + " p.objectGuid = :objId"
            + " and i.lastAction != :action and p.packageGuid = i.guid";
         Query q = session.createQuery(query);
         q.setString("objId", objId.toString());
         q.setString("action", PackageAction.UNINSTALL.name());
         q.setCacheable(true);
         return (PSPkgElement) q.uniqueResult();

   }   

   /* (non-Javadoc)
    * @see IPSPkgInfoService#findPkgElements(IPSGuid)
    */
   @SuppressWarnings(value = { "unchecked" })
   public List<PSPkgElement> findPkgElements(IPSGuid parentPkgId)
   {
      List<PSPkgElement>  pkgElementList = null;
      Session session = getSession();

      CriteriaBuilder builder = session.getCriteriaBuilder();
      CriteriaQuery<PSPkgElement> criteria = builder.createQuery(PSPkgElement.class);
      Root<PSPkgElement> critRoot = criteria.from(PSPkgElement.class);
      criteria.where(builder.equal(critRoot.get("packageGuid"),parentPkgId.longValue()));
      return session.createQuery(criteria).getResultList();


   }


   /* (non-Javadoc)
    * @see IPSPkgInfoService#loadPkgElements(List<IPSGuid>)
    */
   @SuppressWarnings(value = { "unchecked" })
   public List<PSPkgElement> loadPkgElements(List<IPSGuid> ids) throws PSNotFoundException {
      List<Long> idList = new ArrayList<>();

      if (ids == null)
         throw new IllegalArgumentException("ids may not be null");

      if (ids.isEmpty())
         throw new IllegalArgumentException("ids may not be empty");

      for (IPSGuid id : ids)
      {
        if (id == null)
        {
           throw new IllegalArgumentException("ids may not be have null entry");
        }
        else
        {
           idList.add(id.longValue());
        }
      }

      List<PSPkgElement>  pkgElementList = null;
      Session session = getSession();



      CriteriaBuilder builder = session.getCriteriaBuilder();
      CriteriaQuery<PSPkgElement> criteria = builder.createQuery(PSPkgElement.class);
      Root<PSPkgElement> critRoot = criteria.from(PSPkgElement.class);
      criteria.select(critRoot);
      criteria.where(critRoot.get("guid").in(idList));
      pkgElementList = session.createQuery(criteria).getResultList();


      // If one ids does not match and object, the object is missing.
         // Find id and throw exception 
         if (pkgElementList.size() != idList.size())
         {
            for (long id : idList)
            {
               PSGuid guid = new PSGuid(id);
               if (findPkgElement(guid) == null)
               {
                  throw new PSNotFoundException(guid);
               }
            }
         }


      return pkgElementList;
   }


   /* (non-Javadoc)
    * @see IPSPkgInfoService#loadPkgElement(IPSGuid)
    */
   public PSPkgElement loadPkgElement(IPSGuid id) throws PSNotFoundException {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      // todo: Handle caching!

      return  loadPkgElementModifiable(id);

   }

   /* (non-Javadoc)
    * @see IPSPkgInfoService#loadPkgElementModifiable(IPSGuid)
    */
   public PSPkgElement loadPkgElementModifiable(IPSGuid id) throws PSNotFoundException {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      PSPkgElement pkgElement = null;
      Session session = getSession();


      CriteriaBuilder builder = session.getCriteriaBuilder();
      CriteriaQuery<PSPkgElement> criteria = builder.createQuery(PSPkgElement.class);
      Root<PSPkgElement> critRoot = criteria.from(PSPkgElement.class);
      criteria.select(critRoot);
      criteria.where(builder.equal(critRoot.get("guid"),id.longValue()));
      pkgElement = (PSPkgElement) session.createQuery(criteria).getSingleResult();


      if (pkgElement == null)
            throw new PSNotFoundException(id);

      return pkgElement;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.pkginfo.IPSPkgInfoService#createPkgDependency()
    */
   public PSPkgDependency createPkgDependency()
   {
      PSPkgDependency pkgDep = new PSPkgDependency();
      IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
      pkgDep.setId(guidMgr.createId("PKG_DEPENDENCY_ID"));
      return pkgDep;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.pkginfo.IPSPkgInfoService#findDependentPkgGuids(com.percussion.utils.guid.IPSGuid)
    */
   @SuppressWarnings("unchecked")
   public List<IPSGuid> findDependentPkgGuids(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid may not be null");
      List<IPSGuid> dPkgGuids = new ArrayList<>();
      List<PSPkgDependency> pkgDeps = new ArrayList<>();
      Session session = getSession();



      CriteriaBuilder builder = session.getCriteriaBuilder();
      CriteriaQuery<PSPkgDependency> criteria = builder.createQuery(PSPkgDependency.class);
      Root<PSPkgDependency> critRoot = criteria.from(PSPkgDependency.class);
      criteria.where(builder.equal(critRoot.get("ownerPackageGuid"),guid.longValue()));
      pkgDeps = session.createQuery(criteria).getResultList();

         for (PSPkgDependency dep : pkgDeps)
         {
            dPkgGuids.add(dep.getDependentPackageGuid());
         }

      
      return dPkgGuids;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.pkginfo.IPSPkgInfoService#findOwnerPkgGuids(com.percussion.utils.guid.IPSGuid)
    */
   @SuppressWarnings("unchecked")
   public List<IPSGuid> findOwnerPkgGuids(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid may not be null");
      List<IPSGuid> oPkgGuids = new ArrayList<>();
      List<PSPkgDependency> pkgDeps = new ArrayList<>();
      Session session = getSession();

      CriteriaBuilder builder = session.getCriteriaBuilder();
      CriteriaQuery<PSPkgDependency> criteria = builder.createQuery(PSPkgDependency.class);
      Root<PSPkgDependency> critRoot = criteria.from(PSPkgDependency.class);
      criteria.where(builder.equal(critRoot.get("dependentPackageGuid"),guid.longValue()));
      pkgDeps = session.createQuery(criteria).getResultList();

         for (PSPkgDependency dep : pkgDeps)
         {
            oPkgGuids.add(dep.getOwnerPackageGuid());
         }

      return oPkgGuids;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.pkginfo.IPSPkgInfoService#loadPkgDependencies(com.percussion.utils.guid.IPSGuid)
    */
   public List<PSPkgDependency> loadPkgDependencies(IPSGuid guid,
         boolean depType)
   {

      if (guid == null)
         throw new IllegalArgumentException("ownerGuid may not be null");

      return  loadPkgDependenciesModifiable(guid,
            depType);

   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.pkginfo.IPSPkgInfoService#loadPkgDependenciesModifiable(com.percussion.utils.guid.IPSGuid)
    */
   @SuppressWarnings("unchecked")
   public List<PSPkgDependency> loadPkgDependenciesModifiable(IPSGuid guid,
         boolean depType)
   {
      if (guid == null)
         throw new IllegalArgumentException("ownerGuid may not be null");
      List<PSPkgDependency> pkgDeps = new ArrayList<>();
      String temp = depType?"ownerPackageGuid":"dependentPackageGuid";
      Session session = getSession();

         Criteria criteria = session.createCriteria(PSPkgDependency.class);
         criteria.add(Restrictions.eq(temp, guid.longValue()));
         pkgDeps = criteria.list();


      return pkgDeps;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.pkginfo.IPSPkgInfoService#savePkgDependency(com.percussion.services.pkginfo.data.PSPkgDependency)
    */
   public void savePkgDependency(PSPkgDependency pkgDependency)
   {
      if (pkgDependency == null)
         throw new IllegalArgumentException("pkgDependency may not be null");

      getSession().saveOrUpdate(pkgDependency);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.pkginfo.IPSPkgInfoService#deletePkgDependency(long)
    */
   public void deletePkgDependency(long pkgDepId)
   {
      Session session = getSession();

         Criteria criteria = session.createCriteria(PSPkgDependency.class);
         criteria.add(Restrictions.eq("pkgDependencyId", pkgDepId));
         PSPkgDependency pkgDep = (PSPkgDependency) criteria.uniqueResult();

         if (pkgDep == null)
            return;

         session.delete(pkgDep);

   }

   public void deletePkgInfoChildren(IPSGuid pkgGuid)
   {
      List<IPSGuid> pkgElemGuids = findPkgElementGuids(pkgGuid);
      for(IPSGuid elemGuid:pkgElemGuids)
      {
         deletePkgElement(elemGuid);
      }
      List<PSPkgDependency> deps = loadPkgDependencies(pkgGuid, true);
      for (PSPkgDependency dep : deps)
      {
         deletePkgDependency(dep.getId());
      }
   }

   public void deletePkgInfoChildren(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException(
               "name may not be null or empty string");
      }
      
      PSPkgInfo info = findPkgInfo(name);
      if (null != info)
         deletePkgInfoChildren(info.getGuid());
   }
}
