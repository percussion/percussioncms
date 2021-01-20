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

package com.percussion.deployer.server.dependencies;

import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.deployer.services.IPSDeployService;
import com.percussion.deployer.services.PSDeployServiceException;
import com.percussion.deployer.services.PSDeployServiceLocator;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.sitemgr.data.PSSite;
import com.percussion.services.sitemgr.data.PSSiteProperty;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class to handle packaging and deploying a site definition.
 */
public class PSSiteDefDependencyHandler extends PSDataObjectDependencyHandler
{

   /**
    * Construct a dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSSiteDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }


   /**
    * ID Map context ids since PSSite does not give an opportunity to id map 
    * contexts during deserialization and tries to load a non-existent Context
    * @param siteStr never <code>null</code> the serialized output of site
    * @return a serialized site string with fixed context ids 
    * @throws PSDeployException  if xml parsing fails
    */
   private String fixContextsInXmlSite(PSImportCtx ctx, String siteStr)
         throws PSDeployException
   {
      ByteArrayInputStream bis = new ByteArrayInputStream(siteStr.getBytes());

      Document doc = null;
      try
      {
         String xmlElemName = "context-id";
         doc = PSXmlDocumentBuilder.createXmlDocument(bis, false);
         if (doc == null)
            return siteStr;
         Element root = doc.getDocumentElement();
         if (root == null)
            return siteStr;
         NodeList nl = root.getElementsByTagName(xmlElemName);
         if (nl == null)
            return siteStr;
         int nlSize = nl.getLength();

         for (int j = 0; j < nlSize; j++)
         {
            Element el = (Element) nl.item(j);
            if (el.getFirstChild() != null)
            {
               String ctxId = el.getFirstChild().getNodeValue();
               ctxId = String.valueOf((new PSGuid(ctxId)).longValue());
               if (!StringUtils.isBlank(ctxId))
               {
                  PSIdMapping ctxMap = getIdMapping(ctx, ctxId,
                        PSContextDefDependencyHandler.DEPENDENCY_TYPE);
                  if (ctxMap != null
                        && !StringUtils.isBlank(ctxMap.getTargetId()))
                  {
                     String tgtId = (new PSGuid(PSTypeEnum.CONTEXT,
                           ctxMap.getTargetId()).toString());
                     el.getFirstChild().setNodeValue(tgtId);
                  }
               }
            }
         }
      }
      catch (SAXException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e,
               "could not parse serialized Site data, due to :"
                     + e.getLocalizedMessage());
      }
      catch (IOException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e,
               "could not parse serialized Site data, due to :"
                     + e.getLocalizedMessage());
      }
      return PSXmlDocumentBuilder.toString(doc);
   }
   
   
   /**
    * Helper method to 
    * 1. find and load all the **NEW** Sites: List<IPSSite>
    * 2. generate a map m_namedSites
    */
   private void init()
   {
      m_siteMgr = PSSiteManagerLocator.getSiteManager();
      
      if (m_namedSites != null)
         m_namedSites.clear();
      else
         m_namedSites = new HashMap<String, IPSSite>();
      
      if (m_guidSites != null)
         m_namedSites.clear();
      else
         m_guidSites  = new HashMap<IPSGuid, IPSSite>();
 
      List<IPSSite> sites = m_siteMgr.findAllSites();
    
      Iterator<IPSSite> it = sites.iterator();
      while (it.hasNext())
      {
         IPSSite s = it.next();
         
         m_namedSites.put(s.getName(), s);
         m_guidSites.put(s.getGUID(), s);
      }
   }
   
   
   /**
    * Utility method to find the Site by a given dependency id(as a string).
    * @param depId the id, may not be <code>null</code> or empty.
    * @return <code>null</code> if Site is not found.
    * @throws PSDeployException 
    */
   private IPSSite findSiteByDependencyID(String depId) throws PSDeployException
   {
      if (depId == null || depId.trim().length() == 0)
         throw new IllegalArgumentException(
               "dependency ID may not be null or empty");
      
      if ( Integer.parseInt(depId) <= 0 )
         return null;
      
      init();    
      
      PSGuid guid = new PSGuid(PSTypeEnum.SITE, PSDependencyUtils
            .getGuidValFromString(depId, m_def.getObjectTypeName()));
      return m_guidSites.get(guid);
   }

   //see base class
   @Override
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (StringUtils.isBlank(id))
         throw new IllegalArgumentException("id may not be null or empty");

      if ( !PSGuid.isValid(PSTypeEnum.SITE, id) )
         return false;

      IPSSite site = findSiteByDependencyID(id);
      return (site!=null)? true:false;
   }

   /**
    * Returns a mixed list of templates and legacy templates aka VARIANTS
    * @param tok the security token, assumed not <code>null</code>.
    * @param dep the site dependency, assumed not <code>null</code> and of the
    * correct type.
    * @param site the actual site loaded, assumed not <code>null</code>.
    * @return child template/variant dependencies as a list, never
    * <code>null</code> may be empty.
    * @throws PSDeployException
    */
   private List<PSDependency> getTemplateDependencies(
         PSSecurityToken tok, PSDependency dep, IPSSite site) 
         throws PSDeployException
   {
            
      List<PSDependency> deps = new ArrayList<PSDependency>();
      Iterator<IPSAssemblyTemplate> it = 
         ((PSSite) site).getAssociatedTemplates().iterator();

      PSDependencyHandler tHandler = getDependencyHandler(
            PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);
      PSDependencyHandler vHandler = getDependencyHandler(
            PSVariantDefDependencyHandler.DEPENDENCY_TYPE);

      while (it.hasNext())
      {
         IPSAssemblyTemplate tmp = it.next();
         String depId = String.valueOf(tmp.getGUID().longValue());
         PSDependency child = null;
         if (tmp.isVariant())
            child = vHandler.getDependency(tok, depId);
         else
            child = tHandler.getDependency(tok, depId);
         
         if (child != null)
         {
            deps.add(child);            
         }
      }
      return  deps;
   }

   /**
    * Iterate through site properties and get context dependencies if any
    * @param tok the security token, assumed not <code>null</code>.
    * @param dep the site dependency, assumed not <code>null</code> and of the
    * correct type.
    * @param site the actual site loaded, assumed not <code>null</code>.
    * @return child context deps dependencies as a list, never <code>null</code>
    * may be empty.
    * @throws PSDeployException
    */
   private Set<PSDependency> getContextDependencies(
         PSSecurityToken tok, PSDependency dep, IPSSite site) 
         throws PSDeployException
   {
      Set<PSDependency> deps = new HashSet<PSDependency>();
      PSDependencyHandler  ctxHandler = getDependencyHandler(
            PSContextDefDependencyHandler.DEPENDENCY_TYPE);
      Set<PSSiteProperty> props = ((PSSite)site).getProperties();
      
      for (PSSiteProperty p : props)
      {
         IPSGuid ctxId  = p.getContextId();
         PSDependency ctxDep = ctxHandler.getDependency(tok, String.valueOf(
               ctxId.longValue()));
         if (ctxDep != null)
         {
            if (ctxDep.getDependencyType() == PSDependency.TYPE_SHARED)
            {
               ctxDep.setIsAssociation(false);
            }
            deps.add(ctxDep);
         }
      }
      return deps;
   }
   
   /**
    * Retrieves the edition child dependencies for a given site.
    * @param tok the security token, assumed not <code>null</code>.
    * @param dep the site dependency, assumed not <code>null</code> and of the
    * correct type.
    * @param site the actual site loaded, assumed not <code>null</code>.
    * @return child edition dependencies as a list, never <code>null</code>,
    * may be empty.
    * @throws PSDeployException
    */
   private List<PSDependency> getEditionDependencies(
         PSSecurityToken tok, PSDependency dep, IPSSite site) 
         throws PSDeployException
   {
      List<PSDependency> deps = new ArrayList<PSDependency>();
      PSDependencyHandler  edtnHandler = getDependencyHandler(
            PSEditionDefDependencyHandler.DEPENDENCY_TYPE);
      
      List<IPSEdition> editions = m_pubSvc.findAllEditionsBySite(site.getGUID());
      for (IPSEdition edition : editions)
      {
         IPSGuid edtnId  = edition.getGUID();
         PSDependency edtnDep = edtnHandler.getDependency(tok, String.valueOf(
               edtnId.longValue()));
         if (edtnDep != null)
         {
            if (edtnDep.getDependencyType() == PSDependency.TYPE_SHARED)
            {
               edtnDep.setIsAssociation(false);
            }
            deps.add(edtnDep);            
         }
      }
      
      return deps;
   }
      
   // see base class
   @Override
   public Iterator<PSDependency> getChildDependencies(PSSecurityToken tok,
         PSDependency dep) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      String siteId = dep.getDependencyId();

      IPSSite s = findSiteByDependencyID(siteId);
      
      // get edition def child dependencies
      List<PSDependency> childDeps = getEditionDependencies(tok, dep, s);      
      
      List<PSDependency> tvDeps = getTemplateDependencies(tok,dep,s);
      childDeps.addAll(tvDeps);
      
      Set<PSDependency> ctxDeps = getContextDependencies(tok, dep, s);
      childDeps.addAll(ctxDeps);
      
      //Acl deps
      addAclDependency(tok, PSTypeEnum.SITE, dep, childDeps);

      return childDeps.iterator();
    }
    
   // see base class
   @Override
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
   {
      if (tok == null) 
         throw new IllegalArgumentException("tok may not be null");
      
      init(); 
      Set<String> siteNames = m_namedSites.keySet();
      Iterator<String> it = siteNames.iterator();
      
      List<PSDependency> deps = new ArrayList<PSDependency>();
      while (it.hasNext())
      {
         String sName = it.next();
         IPSSite s = m_namedSites.get(sName);
         PSDependency dep = createDeployableElement(m_def, 
               String.valueOf(s.getGUID().longValue()), sName);
         if (dep.getDependencyId().equals(IPSDeployConstants.PREVIEW_SITE_ID))
            dep.setDependencyType(PSDependency.TYPE_SYSTEM);
         deps.add(dep);
      }
      return deps.iterator();
   }

   // see base class
   @Override
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      PSDependency dep = null;
      IPSSite site = findSiteByDependencyID(id);
      if ( site != null )
      {
         if (id.equals(IPSDeployConstants.PREVIEW_SITE_ID))
         {
            dep = createDependency(m_def, String.valueOf(site.getGUID()
                  .longValue()), getPreviewSiteName());
            dep.setDependencyType(PSDependency.TYPE_SYSTEM);
         }
         else
            dep = createDependency(m_def, String.valueOf(site.getGUID()
                  .longValue()), site.getName());
      }
      return dep;
   }
   

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>ContextVariableDef</li>
    * <li>EditionDef</li>
    * <li>PublisherDef</li>
    * <li>VariantDef</li>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   @Override
   public Iterator<String> getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   // see base class
   @Override
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   // see base class
   @Override
   public void reserveNewId(PSDependency dep, PSIdMap idMap)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");
      
      PSDependencyUtils.reserveNewId(dep, idMap, getType());
   }

   /**
    * Creates a dependency file from a given dependency data object.
    * @param site the site that needs to be serialized to a file, never 
    * <code>null</code>
    * @return The dependency file object, it will never be <code>null</code>.
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   private PSDependencyFile getDepFileFromSite(IPSSite site)
      throws PSDeployException
   {
      if (site == null)
         throw new IllegalArgumentException("depData may not be null");
      String str;
      try
      {
         str = ((PSSite)site).toXML();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Unable to generate a dependency file for Site:"
                     + site.getName());
      }
      
      return new PSDependencyFile(PSDependencyFile.TYPE_SERVICEGENERATED_XML,
            createXmlFile(IPSDeployConstants.XML_HDR_STR + str));
   }

   
   // see base class
   @Override
   public Iterator<PSDependencyFile> getDependencyFiles(PSSecurityToken tok,
         PSDependency dep) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      List<PSDependencyFile> files = new ArrayList<PSDependencyFile>();
      IPSSite site = findSiteByDependencyID(dep.getDependencyId());
      if (site == null)
      {
         Object[] args = {dep.getDependencyId(), dep.getObjectTypeName(),
               dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND,
               args);
      }
      
      files.add(getDepFileFromSite(site));
      
      return files.iterator();
   }

   /**
    * Given the serialized data for a Site, create such PSSite. Also the 
    * associated templates are transformed
    * @param tok the security token, assumed not <code>null</code>.
    * @param archive the ArchiveHandler to use to retrieve the files from the 
    * archive, may not be <code>null</code> 
    * @param depFile the PSDependencyFile that was retrieved from the archive
    * may not be <code>null</code>
    * @param site the existing site that will be updated by the contents of the 
    * depFile, null if there is no existing site to update in which case a 
    * new site is created, may or may not be <code>null</code>, 
    * if <code>null</code>, create a new site, else use an existing site 
    * @param ctx the import context never <code>null</code>
    * @return the deserialized site may not be <code>null</code>
    * @throws PSDeployException if there is a problem reading the file content
    */
   public IPSSite generateSiteFromFile(
         PSSecurityToken tok, PSArchiveHandler archive,
         PSDependencyFile depFile, IPSSite site, PSImportCtx ctx)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("context may not be null");
      if (depFile == null)
         throw new IllegalArgumentException("depFile may not be null");

      String tmpStr = PSDependencyUtils.getFileContentAsString(
            archive, depFile);
      tmpStr = fixContextsInXmlSite(ctx, tmpStr);
      try
      {
         Set<IPSGuid> tmpGuids = PSSite.getTemplateIdsFromSite(tmpStr);
         // Transform the templates of this site.
         // Templates not found on the target system will be dropped.
         Set<IPSGuid> newGuids = new HashSet<IPSGuid>();
         for (IPSGuid g : tmpGuids)
         {
            PSIdMapping tmpMap = getTemplateOrVariantMapping(tok, ctx, 
                  String.valueOf(g.getUUID()));
            if (tmpMap != null)
            {
               IPSGuid newTmp = new PSGuid(PSTypeEnum.TEMPLATE, tmpMap
                     .getTargetId());
               newGuids.add(newTmp);
            }
         }
         
         // modify the serialized site to include the mapped template ids
         tmpStr = PSSite.replaceTemplateIdsFromSite(tmpStr, newGuids);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               "Error occurred while generating site:" +
               e.getLocalizedMessage());
      }
      // deserialize on the mapped template idss
      return deserializeDataFile(site, tmpStr);
   }
   /**
    * @param s the Site itself, may be <code>null</code>, if so create a new
    * site using SiteManager else deserialize on the passed in Object
    * @param tmpStr the serialized form of Site representation, never 
    * <code>null</code>
    * @return the site as an Object
    * @throws PSDeployException
    */
   public IPSSite deserializeDataFile(IPSSite s, String tmpStr)
         throws PSDeployException
   {
      IPSSite site = null;
      if ( s == null )
         site = m_siteMgr.createSite();
      else 
         site = s;
      
      
      try
      {
         ((PSSite)site).fromXML(tmpStr);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               "Could not deserialize site");
      }
      return site;
   }

   
   /**
    * Return an iterator for dependency files in the archive
    * @param archive The archive handler to retrieve the dependency files from,
    *           may not be <code>null</code>.
    * @param dep The dependency object, may not be <code>null</code>.
    * 
    * @return An iterator one or more <code>PSDependencyFile</code> objects.
    *         It will never be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there is no dependency file in the archive
    *            for the specified dependency object, or any other error occurs.
    */
   @SuppressWarnings("unchecked")
   protected Iterator<PSDependencyFile> getSiteDependecyFilesFromArchive(
         PSArchiveHandler archive, PSDependency dep) throws PSDeployException
   {
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      Iterator<PSDependencyFile> files = archive.getFiles(dep);

      if (!files.hasNext())
      {
         Object[] args =
         {
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.
                                       TYPE_SERVICEGENERATED_XML],
            dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName()};
            throw new PSDeployException(
               IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }
      return files;
   }

   
   // see base class
   @Override
   public void installDependencyFiles(PSSecurityToken tok,
         PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
   throws PSDeployException
   {
      Iterator<PSDependencyFile> files = getSiteDependecyFilesFromArchive(
            archive, dep);
      PSDependencyFile depFile = files.next();
      
      IPSSite site = null;
      PSIdMapping siteMapping = getIdMapping(ctx, dep); 
      if ( siteMapping != null )
         site = findSiteByDependencyID(siteMapping.getTargetId());
      else
         site = findSiteByDependencyID(dep.getDependencyId());
      
      
      boolean isNew = (site == null) ? true : false;
      Integer ver = null;
      if (!isNew)
      {
         site = m_siteMgr.loadSite(site.getGUID());
         ver = ((PSSite) site).getVersion();
         ((PSSite) site).setVersion(null);
         // keep the slots out of SITE, they are not needed for template 
         // associations anyways
         Iterator<IPSAssemblyTemplate> tmpIt = site.getAssociatedTemplates()
               .iterator();
         while ( tmpIt.hasNext() )
         {
            IPSAssemblyTemplate t = tmpIt.next();
            t.setSlots(new HashSet<IPSTemplateSlot>());
         }
      }   
      IPSDeployService depSvc = PSDeployServiceLocator.getDeployService();
      try
      {
         depSvc.deserializeAndSaveSite(tok, archive, dep, depFile, ctx, this,
               site, ver);
      }
      catch (PSDeployServiceException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "error occurred while installing site: "+e.getLocalizedMessage());
      } 
      
      //add transaction log
      addTransactionLogEntryByGuidType(dep, ctx, PSTypeEnum.SITE, isNew);
   }
   
   
   /**
    * TROLL thru the object and restore the versions of child-lings ;).
    * 
    * @param s the site
    * @param ver the version of Site
    * @throws PSDeployException
    */
   public void saveDeserializedObject(IPSSite s, Integer ver)
         throws PSDeployException
   {
      // nullify and set it to the passed version of the site, can be null
      ((PSSite) s).setVersion(null);
      ((PSSite) s).setVersion(ver);
      try
      {
         m_siteMgr.saveSite(s);
      }
      catch (Exception e1)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Could not save or update the site:" + s.getName() + "\n"
                     + e1.getLocalizedMessage());
      }
   }

   /**
    * Helper method to provide the correct mapping based on two attempts:
    * first try it as template if it does not exist, then try again as 
    * variant definition. If there is no mapping, then <code>null</code> 
    * is returned.
    * @param tok the security token, assumed not <code>null</code>.
    * @param ctx the import context never <code>null</code>
    * @param tmpId the template id never <code>null</code>
    * @return the mapping if it exists else <code>null</code>
    * @throws PSDeployException 
    * (other than IPSDeploymentErrors.MISSING_ID_MAPPING, which is handled
    * by a <code>null</code> return.)
    */
   private PSIdMapping getTemplateOrVariantMapping(PSSecurityToken tok,
         PSImportCtx ctx, String tmpId)
         throws PSDeployException
   {
      PSIdMapping m = null;
      m = getIdMappingOfAssoc(tok, ctx, tmpId,
          PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);
      if (m == null)
      {
         m = getIdMappingOfAssoc(tok, ctx, tmpId,
               PSVariantDefDependencyHandler.DEPENDENCY_TYPE);         
      }
      return m;

   }


   /**
    * Helper method to calculate the new template ids on the target
    * @param tok PSSecurity Token may not be <code>null</code>
    * @param dep the dependency for which the transformation has to happen
    * @param ctx ImportContext may not be <code>null</code>
    * @param site TemplateSlot in which idtypes are to be transformed
    * @throws PSDeployException
    */
   public void transformSiteData(PSSecurityToken tok,
         PSDependency dep, PSImportCtx ctx,
         IPSSite site) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (site == null)
         throw new IllegalArgumentException("Site may not be null");
      
      PSSite s = (PSSite)site;
      
      // Transform the Site Id 
      PSIdMapping siteMapping = getIdMapping(ctx, dep);
      if ( siteMapping != null && siteMapping.getTargetId() != null )
      {
         Long i = Long.parseLong(siteMapping.getTargetId());
         PSGuid guid = new PSGuid(PSTypeEnum.SITE, i.longValue());
         s.setGUID(guid);

         // Also, transform the Site Id that lives on each site property
         // (This may seem redundant, but it avoids a cache coherency 
         //  issue during re-installation of Site packages)
         Set<PSSiteProperty> props = s.getProperties();
         for (PSSiteProperty p : props)
         {
            ((PSSite)p.getSite()).setGUID(guid);
         }         
      }
      
      // transform the contextid in all the site properties
      Set<PSSiteProperty> props = s.getProperties();
      for (PSSiteProperty p : props)
      {
         try
         {
            PSIdMapping pMap = getIdMapping(ctx,
                  String.valueOf(p.getContextId()),
                  PSContextDefDependencyHandler.DEPENDENCY_TYPE);
            if ( pMap != null )
            {
               IPSGuid ctxId = PSGuidUtils.makeGuid(pMap.getTargetId(),
                     PSTypeEnum.CONTEXT);
               p.setContextId(ctxId); 
            }
         }
         catch(PSDeployException dex)
         {
            // dont transform
         }
      }
   }
   
   // see base class
   @Override
   public boolean shouldDeferInstallation()
   {
      return true;
   }

   /**
    * Gets the name of the preview site
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public static String getPreviewSiteName()
   {
      return PSDeploymentManager.getBundle().getString("previewSite");
   }

   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "SiteDef";
 
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();

   /**
    * Get the site manager
    */
   private static IPSSiteManager m_siteMgr = 
      PSSiteManagerLocator.getSiteManager();
   
   /**
    * List of sites stored using their names
    */
   private HashMap<String, IPSSite> m_namedSites = null;
   
   /**
    * List of sites stored using their guids
    */
   private HashMap<IPSGuid, IPSSite> m_guidSites = null;
   
   /**
    * Get the publisher service
    */
   private static IPSPublisherService m_pubSvc = 
      PSPublisherServiceLocator.getPublisherService();

   static
   {
      ms_childTypes.add(PSEditionDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSContextDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSVariantDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSAclDefDependencyHandler.DEPENDENCY_TYPE);
   }
}
