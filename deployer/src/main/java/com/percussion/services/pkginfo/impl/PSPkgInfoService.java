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
package com.percussion.services.pkginfo.impl;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.data.PSPkgDependency;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageAction;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the primary interface for the Package Information Service.  
 * <p>
 * For more information, consult the documentation in the interface definition
 * file @see IPSPkgInfoService
 *
 */
@Component("sys_pkgInfoService")
@Transactional
public class PSPkgInfoService
implements IPSPkgInfoService
{
   private static final Logger log = LogManager.getLogger(PSPkgInfoService.class);

   private SessionFactory sessionFactory;

   public SessionFactory getSessionFactory() {
      return sessionFactory;
   }

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
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
      
       sessionFactory.getCurrentSession().saveOrUpdate(obj);
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
      
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSPkgInfo.class);
         criteria.add(Restrictions.eq("guid", new Long(id.longValue())));
         PSPkgInfo pkgInfo = (PSPkgInfo) criteria.uniqueResult();
         
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
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSPkgInfo.class);
         criteria.add(Restrictions.eq("descriptorName", name).ignoreCase());
         pkgInfoList = criteria.list();

      return pkgInfoList.size() == 0 ? null : pkgInfoList.get(0);
   }

   /* (non-Javadoc)
    * @see IPSPkgInfoService#findLatestPkgInfos(String nameFilter)
    */
   @SuppressWarnings(value = { "unchecked" })
   public List<PSPkgInfo> findAllPkgInfos()
   {
      List<PSPkgInfo>  pkgInfoList = null;
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSPkgInfo.class);
         pkgInfoList = criteria.list();      

      return pkgInfoList;
   }


   /* (non-Javadoc)
    * @see IPSPkgInfoService#loadPkgInfo(IPSGuid id)
    */
   public PSPkgInfo loadPkgInfo(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      PSPkgInfo pkgInfo = loadPkgInfoModifiable(id);
      
      return pkgInfo;
   }

   /* (non-Javadoc)
    * @see IPSPkgInfoService#loadPkgInfoModifiable(IPSGuid id)
    */
   public PSPkgInfo loadPkgInfoModifiable(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      PSPkgInfo pkgInfo = (PSPkgInfo) sessionFactory.getCurrentSession().
         get(PSPkgInfo.class, new Long(id.longValue()));

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

         log.debug("Trying to save PackageElement: " + obj.toString());
         sessionFactory.getCurrentSession().saveOrUpdate(obj);
         log.debug("PackageElement save completed for: " + obj.toString());


   }


   /* (non-Javadoc)
    * @see IPSPkgInfoService#deletePkgElement(IPSGuid)
    */
   public void deletePkgElement(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSPkgElement.class);
         criteria.add(Restrictions.eq("guid", id.longValue()));
         PSPkgElement pkgElement = (PSPkgElement) criteria.uniqueResult();
         
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
      
      
      List<Long> longList = new ArrayList<Long>();
      List<IPSGuid> guidList = new ArrayList<IPSGuid>();
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSPkgElement.class);
         criteria.setProjection( Projections.property("guid"));
         criteria.add(Restrictions.eq("packageGuid",
               new Long(parentPkgInfoId.longValue())));
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
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSPkgElement.class);
         criteria.add(Restrictions.eq("guid", new Long(id.longValue())));
         pkgElement = (PSPkgElement) criteria.uniqueResult();

      return pkgElement;
   }

   /* (non-Javadoc)
    * @see IPSPkgInfoService#findPkgElementByObject(IPSGuid)
    */
   @SuppressWarnings("unchecked")
   public PSPkgElement findPkgElementByObject(IPSGuid objId)
   {
      if (objId == null)
         throw new IllegalArgumentException("objId may not be null");
             
      Session session = sessionFactory.getCurrentSession();

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
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSPkgElement.class);
         criteria.add(Restrictions.eq("packageGuid", parentPkgId.longValue()));
         criteria.setCacheable(true);
         pkgElementList = criteria.list();

      return pkgElementList;
   }


   /* (non-Javadoc)
    * @see IPSPkgInfoService#loadPkgElements(List<IPSGuid>)
    */
   @SuppressWarnings(value = { "unchecked" })
   public List<PSPkgElement> loadPkgElements(List<IPSGuid> ids)
   {
      List<Long> idList = new ArrayList<Long>();

      if (ids == null)
         throw new IllegalArgumentException("ids may not be null");

      if (ids.size() == 0)
         throw new IllegalArgumentException("ids may not be empty");

      for (IPSGuid id : ids)
      {
        if (id == null)
        {
           throw new IllegalArgumentException("ids may not be have null entry");
        }
        else
        {
           idList.add(new Long(id.longValue()));
        }
      }

      List<PSPkgElement>  pkgElementList = null;
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSPkgElement.class);
         criteria.add(Restrictions.in("guid", idList));
         criteria.setCacheable(true);
         pkgElementList = criteria.list();

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
   public PSPkgElement loadPkgElement(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      // todo: Handle caching!

      PSPkgElement pkgElement = loadPkgElementModifiable(id);

      return pkgElement;
   }

   /* (non-Javadoc)
    * @see IPSPkgInfoService#loadPkgElementModifiable(IPSGuid)
    */
   public PSPkgElement loadPkgElementModifiable(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      PSPkgElement pkgElement = null;
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSPkgElement.class);
         criteria.add(Restrictions.eq("guid", id.longValue()));
         pkgElement = (PSPkgElement) criteria.uniqueResult();

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
      List<IPSGuid> dPkgGuids = new ArrayList<IPSGuid>();        
      List<PSPkgDependency> pkgDeps = new ArrayList<PSPkgDependency>();
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSPkgDependency.class);
         criteria.add(Restrictions.eq("ownerPackageGuid", guid.longValue()));
         pkgDeps = criteria.list();
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
      List<IPSGuid> oPkgGuids = new ArrayList<IPSGuid>();        
      List<PSPkgDependency> pkgDeps = new ArrayList<PSPkgDependency>();
      Session session = sessionFactory.getCurrentSession();

         Criteria criteria = session.createCriteria(PSPkgDependency.class);
         criteria.add(Restrictions.eq("dependentPackageGuid", guid.longValue()));
         pkgDeps = criteria.list();
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

      List<PSPkgDependency> pkgDeps = loadPkgDependenciesModifiable(guid,
            depType);

      return pkgDeps;
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
      List<PSPkgDependency> pkgDeps = new ArrayList<PSPkgDependency>();
      String temp = depType?"ownerPackageGuid":"dependentPackageGuid";
      Session session = sessionFactory.getCurrentSession();

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

      sessionFactory.getCurrentSession().saveOrUpdate(pkgDependency);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.pkginfo.IPSPkgInfoService#deletePkgDependency(long)
    */
   public void deletePkgDependency(long pkgDepId)
   {
      Session session = sessionFactory.getCurrentSession();

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
