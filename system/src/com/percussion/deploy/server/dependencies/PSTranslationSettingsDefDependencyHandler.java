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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.deploy.server.dependencies;

import com.percussion.deploy.client.IPSDeployConstants;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deploy.objectstore.PSApplicationIDTypes;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDependencyFile;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.objectstore.PSIdMapping;
import com.percussion.deploy.server.PSArchiveHandler;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.deploy.server.PSImportCtx;
import com.percussion.i18n.PSLocale;
import com.percussion.i18n.PSLocaleException;
import com.percussion.i18n.PSLocaleManager;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.IPSContentService;
import com.percussion.services.content.PSContentServiceLocator;
import com.percussion.services.content.data.PSAutoTranslation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.percussion.services.error.PSNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * Class to handle packaging and deploying TranslationSettings definition for 
 * a particular locale. This will serialize/deserialize as aggregate translation
 * settings.
 * 
 * @author vamsinukala
 */

public class PSTranslationSettingsDefDependencyHandler
      extends
         PSDependencyHandler
{

   /**
    * Construct the dependency handler for translation settings.
    * 
    * @param def The def for the type supported by this handler. May not be
    *           <code>null</code> and must be of the type supported by this
    *           class. See {@link #getType()} for more info.
    * @param dependencyMap The full dependency map. May not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */

   public PSTranslationSettingsDefDependencyHandler(PSDependencyDef def,
         PSDependencyMap dependencyMap) 
   {
      super(def, dependencyMap); 
   }

   /**
    * Utility method to find the TranslationSettings by a given locale
    * 
    * @param depId the locale id 
    * @return <code>null</code> if TranslationSettings don't exist else get
    *         DA TranslationSettings
    */
   private List<PSAutoTranslation> findTranslationSettingsByLocaleID(
         String depId)
   {
      if (depId == null || depId.trim().length() == 0)
         throw new IllegalArgumentException(
               "dependency ID may not be null or empty");
      List<PSAutoTranslation> xlnList = new ArrayList<PSAutoTranslation>();
      xlnList = m_svc.loadAutoTranslationsByLocale(depId);
      return xlnList;
   }

   // see base class
   @Override
   public PSDependency getDependency(PSSecurityToken tok, String id)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      PSDependency dep = null;

      List<PSAutoTranslation> xlnList = findTranslationSettingsByLocaleID(id);
      if (!xlnList.isEmpty())
         dep = createDependency(m_def, id, id);
      return dep;
   }

   // see base class
   @Override
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      List<PSAutoTranslation> xlnList = findTranslationSettingsByLocaleID(dep
            .getDependencyId());
      if (xlnList.isEmpty())
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "could not find a TranslationSettings for locale: "
                     + dep.getDependencyId());
      // OK, we have the TranslationSettings, now package its dependencies..
      Set<PSDependency> childDeps = new HashSet<PSDependency>();

      Iterator<PSAutoTranslation> it = xlnList.iterator();
      PSDependencyHandler ctHandler = getDependencyHandler(
            PSContentTypeDependencyHandler.DEPENDENCY_TYPE);
      PSDependencyHandler cmHandler = getDependencyHandler(
            PSCommunityDependencyHandler.DEPENDENCY_TYPE);
      PSDependencyHandler wfHandler = getDependencyHandler(
            PSWorkflowDependencyHandler.DEPENDENCY_TYPE);
      while(it.hasNext())
      {
         PSAutoTranslation at = it.next();

         PSDependency ctDep = ctHandler.getDependency(tok, String.valueOf(at
               .getContentTypeId()));
         if (ctDep != null)
            childDeps.add(ctDep);

         PSDependency cmDep = cmHandler.getDependency(tok, String.valueOf(at
               .getCommunityId()));
         if (ctDep != null)
            childDeps.add(cmDep);
         PSDependency wfDep = wfHandler.getDependency(tok, String.valueOf(at
               .getWorkflowId()));
         if (ctDep != null)
            childDeps.add(wfDep);
      }
      
      return childDeps.iterator();
   }
   
   
   // see base class
   @Override
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      List<PSDependency> deps = new ArrayList<PSDependency>();
      PSLocaleManager m_localeMgr;
      try
      {
         m_localeMgr = PSLocaleManager.getInstance();
         Iterator<PSLocale> locales = m_localeMgr.getLocales();
         PSDependency dep = null;
         while (locales.hasNext())
         {
            PSLocale loc = locales.next();
            String lName = loc.getLanguageString();
            List<PSAutoTranslation> xlnList = findTranslationSettingsByLocaleID(lName);
            if (!xlnList.isEmpty())
            {
               dep = createDeployableElement(m_def, lName, lName);
               deps.add(dep);
            }
         }
      }
      catch (PSLocaleException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "could not retrieve locales");
      }
      return deps.iterator();
   }

   // see base class
   @Override
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      // pack the data into the files
      List<PSDependencyFile> files = new ArrayList<PSDependencyFile>();
      List<PSAutoTranslation> xlnList = findTranslationSettingsByLocaleID(dep
            .getDependencyId());
      PSDependencyFile f = getDepFileFromTranslationSettings(xlnList);
      files.add(f);
      return files.iterator();
   }

   /**
    * Creates a dependency file from a given dependency data object.
    * 
    * @param xlnList the TranslationSettings, never <code>null</code>
    * @return The dependency file object, it will never be <code>null</code>.
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   private PSDependencyFile getDepFileFromTranslationSettings(
         List<PSAutoTranslation> xlnList) throws PSDeployException
   {
      if (xlnList == null)
         throw new IllegalArgumentException("depData may not be null");
      StringBuffer str = new StringBuffer();
      try
      {
         for (PSAutoTranslation xln : xlnList)
            str.append(xln.toXML()).append(AUTOTRANSLATIONS_DELIM);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Unable to generate a dependency file for translation settings");
      }

      return new PSDependencyFile(PSDependencyFile.TYPE_SERVICEGENERATED_XML,
            createXmlFile(str.toString()));
   }

   // see base class
   @Override
   public void installDependencyFiles(PSSecurityToken tok,
         PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
         throws PSDeployException
   {
      PSDependencyFile depFile = (PSDependencyFile) getTranslationSettingsDependecyFilesFromArchive(
            archive, dep).next();
      // delete existing translations
      List<PSAutoTranslation> xlnList = findTranslationSettingsByLocaleID(dep
            .getDependencyId());
      for (PSAutoTranslation xln : xlnList)
         m_svc.deleteAutoTranslation(xln.getContentTypeId(), xln.getLocale());

      xlnList = generateTranslationSettingsFromFile(archive, depFile);

      doTransforms(tok, archive, dep, ctx, xlnList);
      saveTranslationSettings(xlnList);
      // add transaction log
      addTransactionLogEntryByGuidType(dep, ctx, PSTypeEnum.AUTO_TRANSLATIONS,
            true);
   }

   /**
    * Save translation settings
    * 
    * @param xlnList
    * @throws PSDeployException
    */
   private void saveTranslationSettings(List<PSAutoTranslation> xlnList)
         throws PSDeployException
   {
      // nullify and set it to the passed version of the template, can be null
      Iterator<PSAutoTranslation> it = xlnList.iterator();
      while (it.hasNext())
      {
         try
         {
            PSAutoTranslation at = it.next();
            // Delete Translation Settings, if any of them exist in this list
            m_svc.deleteAutoTranslation(at.getContentTypeId(), at.getLocale());
            at.setVersion(null);
            m_svc.saveAutoTranslation(at);
         }
         catch (Exception e)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
                  "could not save or update the TranslationSettings:"
                        + e.getLocalizedMessage());
         }
      }
   }

   /**
    * Transform any IdTypes
    * 
    * @param tok the security token, never <code>null</code>
    * @param archive the archive, never <code>null</code>
    * @param dep the dependency
    * @param ctx import context never <code>null</code>
    * @param xlnList
    * @throws PSDeployException
    */
   private void doTransforms(PSSecurityToken tok, PSArchiveHandler archive,
         PSDependency dep, PSImportCtx ctx, List<PSAutoTranslation> xlnList)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (ctx == null)
         throw new IllegalArgumentException("context may not be null");

      if (xlnList == null)
         throw new IllegalArgumentException(
               "TranslationSettings Definition cannot be null for idtype mapping");
      PSIdMap idMap = ctx.getCurrentIdMap();
      //No need to xform
      if (idMap == null)
         return;
      
      Iterator<PSAutoTranslation> it = xlnList.iterator();
      while (it.hasNext())
      {
         PSAutoTranslation at = it.next();
         PSIdMapping m = getIdMapping(ctx, String.valueOf(at
               .getContentTypeId()),
               PSContentTypeDependencyHandler.DEPENDENCY_TYPE);
         
         if ( m != null && m.getTargetId()!= null )
            at.setContentTypeId(Long.valueOf(m.getTargetId()));
         
         m = getIdMapping(ctx, String.valueOf(at.getWorkflowId()),
               PSWorkflowDefDependencyHandler.DEPENDENCY_TYPE);
         if ( m != null && m.getTargetId()!= null )
            at.setWorkflowId(Long.valueOf(m.getTargetId()));
         
         m = getIdMapping(ctx,String.valueOf(at.getCommunityId()),
               PSCommunityDefDependencyHandler.DEPENDENCY_TYPE);
         if ( m != null && m.getTargetId()!= null )
            at.setCommunityId(Long.valueOf(m.getTargetId()));
      }
   }


   // see base class
   @Override
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (StringUtils.isBlank(id))
         throw new IllegalArgumentException("id may not be null or empty");

      if (Integer.parseInt(id) <= 0)
         return false;
      
      List<PSAutoTranslation> xlnList = findTranslationSettingsByLocaleID(id);
      return (xlnList != null && !xlnList.isEmpty()) ? true : false;
   }

   /**
    * Use the serialized data for translation settings to  recreate them
    * 
    * @param archive the ArchiveHandler to use to retrieve the files from the
    *           archive, may not be <code>null</code>
    * @param depFile the PSDependencyFile that was retrieved from the archive
    *           may not be <code>null</code>
    * 
    * @return the actual template
    * @throws PSDeployException
    */
   protected List<PSAutoTranslation> generateTranslationSettingsFromFile(
         PSArchiveHandler archive, PSDependencyFile depFile)
         throws PSDeployException
   {
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");

      if (depFile == null)
         throw new IllegalArgumentException("depFile may not be null");
      String xlnStr = "";
      List<PSAutoTranslation> xlnList = null;
      try
      {
         if (depFile.getType() == PSDependencyFile.TYPE_SERVICEGENERATED_XML)
         {
            xlnStr = PSDependencyUtils.getFileContentAsString(archive, depFile);
         }
         else
         {
            Object[] args =
            {
                  PSDependencyFile.TYPE_ENUM[depFile.getType()],
                  PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_SERVICEGENERATED_XML]};
            throw new PSDeployException(
                  IPSDeploymentErrors.WRONG_DEPENDENCY_FILE_TYPE, args);
         }
         xlnList = fromXML(xlnStr);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "could not deserialize translation settings");
      }
      return xlnList;
   }

   /**
    * Utility method that serializes and deserializes the translation settings. 
    * The need for aggregation is to avoid tons of individual files for 
    * translation setttings that need to be written, read in an iteration.
    * @param str the list of translation settings separated by a delimiter
    * @return a list of AutoTranslations never <code>null</code>, may be empty
    * @throws SAXException
    * @throws IOException
    */
   private List<PSAutoTranslation> fromXML(String str) throws SAXException,
         IOException
   {
      List<PSAutoTranslation> xlnList = new ArrayList<PSAutoTranslation>();
      if (StringUtils.isBlank(str))
         return xlnList;

      String[] xlns = str.split(AUTOTRANSLATIONS_DELIM);
      int sz = xlns.length;
      for (int i = 0; i < sz; i++)
      {
         PSAutoTranslation at = new PSAutoTranslation();
         at.fromXML(xlns[i]);
         xlnList.add(at);
      }
      return xlnList;
   }

   /**
    * Return an iterator for dependency files in the archive
    * 
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
   protected Iterator getTranslationSettingsDependecyFilesFromArchive(
         PSArchiveHandler archive, PSDependency dep) throws PSDeployException
   {
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      Iterator files = archive.getFiles(dep);

      if (!files.hasNext())
      {
         Object[] args =
         {
               PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_SERVICEGENERATED_XML],
               dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName()};
         throw new PSDeployException(
               IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }
      return files;
   }

   // see base class
   @Override
   public void reserveNewId(PSDependency dep, PSIdMap idMap)
         throws PSDeployException
   {
      PSDependencyUtils.reserveNewId(dep, idMap, getType());
   }

   // see base class
   @SuppressWarnings("unchecked")
   public void transformIds(Object object, PSApplicationIDTypes idTypes,
         PSIdMap idMap) throws PSDeployException
   {
      if (object == null)
         throw new IllegalArgumentException("object may not be null");

      if (idTypes == null)
         throw new IllegalArgumentException("idTypes may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");
      
      if (!(object instanceof List))
         throw new IllegalArgumentException("invalid object type");
      
      List<PSAutoTranslation> xlnList = (List<PSAutoTranslation>)object;
      // walk id types and perform any transforms
      Iterator resources = idTypes.getResourceList(false);
      while (resources.hasNext())
      {
         String resource = (String)resources.next();
         Iterator elements = idTypes.getElementList(resource, false);
         while (elements.hasNext())
         {
            String element = (String)elements.next();
            Iterator mappings = idTypes.getIdTypeMappings(
                  resource, element, false);
            while (mappings.hasNext())
            {
               PSApplicationIDTypeMapping mapping =
                  (PSApplicationIDTypeMapping)mappings.next();

               if (mapping.getType().equals(
                  PSApplicationIDTypeMapping.TYPE_NONE))
               {
                  continue;
               }

               if (mapping.getType().equals(
                  IPSDeployConstants.DEP_OBJECT_TYPE_CONTENT_TYPE))
               {
                  // transform the CONTENTTYPE Ids
                  Iterator<PSAutoTranslation> entries = xlnList.iterator();
                  int newCtId = idMap.getNewIdInt(mapping.getValue(),
                        PSContentTypeDependencyHandler.DEPENDENCY_TYPE);
                  while (entries.hasNext())
                  {
                     PSAutoTranslation at = entries.next();
                     if (String.valueOf(at.getContentTypeId()).equals(
                           mapping.getValue()))
                        at.setContentTypeId(newCtId);
                  }
               }
               if (mapping.getType().equals(
                     IPSDeployConstants.TYPE_COMMUNITY))
                  {
                     // transform the community Ids
                     Iterator<PSAutoTranslation> entries = xlnList.iterator();
                     int newCmId = idMap.getNewIdInt(mapping.getValue(),
                           PSCommunityDefDependencyHandler.DEPENDENCY_TYPE);
                     while (entries.hasNext())
                     {
                        PSAutoTranslation at = entries.next();  
                        if (String.valueOf(at.getCommunityId()).equals(
                              mapping.getValue()))
                           at.setCommunityId(newCmId);
                     }
                  }
               if (mapping.getType().equals("WorkflowDef"))
               {
                  // transform the Workflow Ids
                  int newWfId = idMap.getNewIdInt(mapping.getValue(),
                        PSWorkflowDefDependencyHandler.DEPENDENCY_TYPE);
                  Iterator<PSAutoTranslation> entries = xlnList.iterator();
                  while (entries.hasNext())
                  {
                     PSAutoTranslation at = entries.next();

                     if (String.valueOf(at.getWorkflowId()).equals(
                           mapping.getValue()))
                        at.setWorkflowId(newWfId);
                  }
               }
            }
         }
      } 
      
   }
   @Override
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   // see base class
   @Override
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   /**
    * Constant for this handler's supported type
    */
   public final static String DEPENDENCY_TYPE = IPSDeployConstants.DEP_OBJECT_TYPE_TRANSLATIONSETTINGS_DEF;

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();

   /**
    * Assembly Helper instance
    */
   private static IPSContentService m_svc = PSContentServiceLocator
         .getContentService();

   /**
    * a delimiter for deserializing the translation settings that have been
    * aggregated
    */
   public static final String AUTOTRANSLATIONS_DELIM = "<!--  -->";

   static
   {
      ms_childTypes.add(PSContentTypeDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSWorkflowDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSCommunityDependencyHandler.DEPENDENCY_TYPE);
   }
}
