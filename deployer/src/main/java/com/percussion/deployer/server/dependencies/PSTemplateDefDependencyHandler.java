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

import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.objectstore.idtypes.PSJexlBinding;
import com.percussion.deployer.objectstore.idtypes.PSJexlBindings;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.deployer.server.PSJexlHelper;
import com.percussion.deployer.services.IPSDeployService;
import com.percussion.deployer.services.PSDeployServiceException;
import com.percussion.deployer.services.PSDeployServiceLocator;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSAssemblyTemplate.GlobalTemplateUsage;
import com.percussion.services.assembly.IPSTemplateBinding;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class to handle packaging and deploying a template definition.
 * @author vamsinukala
 */

public class PSTemplateDefDependencyHandler extends PSDependencyHandler
{
   /**
    * Construct the dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */

   public PSTemplateDefDependencyHandler(PSDependencyDef def,
         PSDependencyMap dependencyMap) 
   {
      super(def, dependencyMap);
   }

   /**
    * Helper method to init PSAssemblyServerHelper 
    */
   private void init()
   {
      if ( m_assemblyHelper == null)
         m_assemblyHelper = new PSAssemblyServiceHelper();
   }

   
   /**
    * Utility method to find the Template by a given guid(as a STRINGGGGGG)
    * @param depId the guid
    * @param loadSlots to load or not to load slots. For template catalogging,
    * we don't need to loadSlots, but for catalogging childDeps, we need to load
    * Slots
    * @return <code>null</code> if Variant is not found
    */
   protected static PSAssemblyTemplate findTemplateByDependencyID(String depId,
                                                                  boolean loadSlots)
   {
      if (depId == null || depId.trim().length() == 0)
         throw new IllegalArgumentException(
               "dependency ID may not be null or empty");
      PSAssemblyTemplate tmp = null;
      try
      {
         tmp = m_assemblySvc.loadTemplate(depId, loadSlots);
      }
      catch (PSAssemblyException ignored)
      { }
      
      return tmp;
   }

   /**
    * Given the template id as a guid string, load it
    * @param depId the string representation of the GUID 
    * @return the template
    *
    * @throws PSDeployException
    */
   private PSAssemblyTemplate loadTemplateByGuid(String depId)
         throws PSDeployException
   {
     
      if (depId == null || depId.trim().length() == 0)
         throw new IllegalArgumentException(
               "to load a template, the dependency ID may not be null or empty");

      // Generate a guid
      PSGuid guid = new PSGuid(PSTypeEnum.TEMPLATE, PSDependencyUtils
            .getGuidValFromString(depId, m_def.getObjectTypeName()));

      init();
      PSAssemblyTemplate tmp = null;
      try
      {
         tmp = m_assemblyHelper.getAssemblySvc().loadTemplate(guid, true);
      }
      catch (PSAssemblyException ignored)
      {
      }
      return tmp;
   }

   // see base class
   @Override
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (StringUtils.isBlank(id))
         throw new IllegalArgumentException("id may not be null or empty");

      if ( !PSGuid.isValid(PSTypeEnum.TEMPLATE, id) )
         return false;

      IPSAssemblyTemplate tmp = findTemplateByDependencyID(id, false);
      return tmp != null;
   }

   
   // see base class
   // id is a PSGuid for a template def
   @Override
   public PSDependency getDependency(PSSecurityToken tok, String id)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      
      init();
      
      IPSAssemblyTemplate tmp = findTemplateByDependencyID(id, false);
      PSDependency dep = null;
      // only return templates
      if ( tmp != null && !tmp.isVariant())
         dep = createDependency(m_def, String.valueOf(tmp.getGUID().longValue()), tmp.getName());
      return dep;
   }

   /**
    * Given the variant id, figure out the slot deps
    * @param tok may not be <code>null</code>
    * @param tmp may not be <code>null</code>
    * @return List of Dependencies of SLOT type
    * @throws PSDeployException
    */
   protected List<PSDependency> getSlotDependencies(PSSecurityToken tok,
         IPSAssemblyTemplate tmp) throws PSDeployException, PSNotFoundException {
      List<PSDependency> depList = new ArrayList<>();
      
      Set<IPSTemplateSlot> slots = tmp.getSlots();
      
      PSDependencyHandler handler = getDependencyHandler(
            PSSlotDefDependencyHandler.DEPENDENCY_TYPE);

      for (IPSTemplateSlot slot : slots) {
         IPSGuid slotGuid = slot.getGUID();
         String id = String.valueOf(slotGuid.longValue());
         PSDependency childDep = handler.getDependency(tok, id);
         if (childDep != null) {
            if (childDep.getDependencyType() == PSDependency.TYPE_SHARED) {
               childDep.setIsAssociation(false);
            }
            depList.add(childDep);
         }
      }

      
      return depList;
   }

   //see base class
   @Override
   @SuppressWarnings("unchecked")
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      
      init();
      
      PSDependencyHandler handler = null;
      IPSAssemblyTemplate tmp = null;
      tmp = findTemplateByDependencyID(dep.getDependencyId(), true);
      
      // Add the child dependencies 
      // 1. Global Templates 
      // 2. ACL deps
      // 3. Rhino - SLOT dependency
      // 4. Assembly Plugins (extensions)
      
      Set<PSDependency> childDeps = new HashSet<>();
      PSDependency childDep;
      
      // 1. Global templates
      if (tmp.getGlobalTemplateUsage().equals(GlobalTemplateUsage.Defined)
            && tmp.getGlobalTemplate() != null)
      {
         PSDependencyHandler th = getDependencyHandler(
               PSTemplateDependencyHandler.DEPENDENCY_TYPE);
         PSDependency d = th.getDependency(tok, String.valueOf(tmp
               .getGlobalTemplate().longValue()));
         if ( d != null )
            childDeps.add(d);
      }
      // 2. Acl deps
      addAclDependency(tok, PSTypeEnum.TEMPLATE, dep, childDeps);

      // 3. Rhino - Slot deps
      List<PSDependency> slotDeps = getSlotDependencies(tok, tmp);
      childDeps.addAll(slotDeps);
      
      // 4. Assembly Plugins - Exits ??
      handler = getDependencyHandler(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
      String assemblyExt = tmp.getAssembler();
      if (StringUtils.isNotBlank(assemblyExt))
      {
         assemblyExt = ASSEMBLY_PLUGIN_PREFIX+assemblyExt+ASSEMBLY_PLUGIN_SUFFIX;
         childDep    = handler.getDependency(tok, assemblyExt);
         if (childDep != null)
         {
             if (childDep.getDependencyType() == PSDependency.TYPE_SHARED)
             {
                childDep.setIsAssociation(false);          
             }
             childDeps.add(childDep);
         }
      }
      
      // Get any extension references in the JEXL bindings for this template
      // and add them to the dependencies
      List<PSDependency> bindingsList;
      try
      {
         bindingsList = handleExitsInJexlExp(tok, tmp);
      }
      catch (PSExtensionException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Could not find an extension: "); 
      }
      if ( bindingsList.size() > 0 )
      {
         for (PSDependency dependency : bindingsList)
         {
            if (dependency.getDependencyType() == PSDependency.TYPE_SHARED)
            {
               dependency.setIsAssociation(false);          
            }
            childDeps.add(dependency);
         }
      }
      
      //    Dont forget the stupid idTypes...
      childDeps.addAll(PSIdTypeDependencyHandler.getIdTypeDependencies(tok, 
            dep, this));
      return childDeps.iterator();
   }
 
   /**
    * Given a JEXL expression, get all the extension dependencies from the 
    * expression.
    * @param tok The security Token may not be <code>null</code>
    * @param exp the JEXL expression may not be <code>null</code>
    * @param exitHandler may mot be <code>null</code>
    * @param jexlHelper helper object to figure out extension names using patterns        
    * @return an iterator over a list of PSDependency objects
    * @throws PSExtensionException
    * @throws PSDeployException
    */
   private Iterator<PSDependency> getExitDependenciesFromJEXLExp(
         PSSecurityToken tok, String exp, PSDependencyHandler exitHandler,
         PSJexlHelper jexlHelper) throws PSExtensionException,
           PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (exp == null || exp.trim().length() == 0)
         throw new IllegalArgumentException("JEXL expression may not be null or empty");
      
      if (exitHandler == null)
         throw new IllegalArgumentException("exitHandler can not  be null");
      List<PSDependency> exitDeps = new ArrayList<>();
      HashMap<String, List<String>> bExtns = PSJexlHelper.getExtensionsFromBindings(exp); 
      
      List<String> sysExtns = bExtns.get(PSJexlHelper.SYS);
      for (String extn : sysExtns)
      {
         PSExtensionRef eRef = jexlHelper.getExtensionRef(extn);
         PSDependency dep = null;
         if ( eRef != null )
            dep = exitHandler.getDependency(tok, eRef.toString());
         // add the dependency if it is not already added, due to some other 
         // expression, unlikely but not impossible
         if ( dep != null && !exitDeps.contains(dep))
            exitDeps.add(dep);
      }
      
      // repeat for user extensions
      List<String> userExtns = bExtns.get(PSJexlHelper.USER);
      for (String extn : userExtns)
      {
         PSExtensionRef eRef = jexlHelper.getExtensionRef(extn);
         PSDependency dep = null;
         if ( eRef != null )
            dep = exitHandler.getDependency(tok, eRef.toString());
         // add the dependency if it is not already added, due to some other 
         // expression, unlikely but not impossible
         if ( dep != null && !exitDeps.contains(dep))
            exitDeps.add(dep);
      }
      return exitDeps.iterator();
   }
   
   /**
    * Utility method to remove the bindings, then remove the template
    * @param t the template that needs to be deleted from the system
    * @throws PSDeployException 
    */
   @Deprecated
   protected static  void deleteTemplate(PSAssemblyTemplate t) throws PSDeployException
   {
      Iterator<PSTemplateBinding> bindingsIt= t.getBindings().iterator();
      
      while ( bindingsIt.hasNext() )
      {
         PSTemplateBinding b = bindingsIt.next();
         if  ( b == null )
            continue;
         t.removeBinding(b);
         bindingsIt = t.getBindings().iterator();
      }
      try
      {
         IPSAssemblyService service = PSAssemblyServiceLocator
               .getAssemblyService();
         service.deleteTemplate(t.getGUID());
      }
      catch (PSAssemblyException e)
      {
         String err = "Error occurred while deleting the template:"
               + t.getDescription() + "==>\n " + e.getLocalizedMessage();
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, err);
      }
   }
   /**
    * Top level call to generate any extension dependencies for this template
    * that has JEXL Bindings with extension references
    * For each binding, get the expression and parse for extensions 
    * @param tok the security token may not be <code>null</code>
    * @param t the template that needs to be evaluated for jexl expression, 
    * may not be <code>null</code>
    * @return List of PSDependency objects
    * @throws PSDeployException 
    * @throws PSExtensionException 
    */
   private List<PSDependency> handleExitsInJexlExp(
         PSSecurityToken tok, IPSAssemblyTemplate t) throws PSDeployException,
           PSExtensionException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (t == null)
         throw new IllegalArgumentException("Template may not be null");

      List<PSDependency> childExts = new ArrayList<>();
      
      Iterator<PSTemplateBinding> bindingsIt= t.getBindings().iterator();
      PSDependencyHandler exitHandler = 
            getDependencyHandler(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
      PSJexlHelper jHelper = new PSJexlHelper();
      while ( bindingsIt.hasNext() )
      {
         PSTemplateBinding b = bindingsIt.next();
         if  ( b == null )
            continue;
         String exp = b.getExpression();
         Iterator<PSDependency> depIt = getExitDependenciesFromJEXLExp(tok,
               exp, exitHandler, jHelper);
         while(depIt.hasNext())
         {
            PSDependency exitDep =  depIt.next();
            if ( !childExts.contains(exitDep) )
               childExts.add(exitDep);
         }
      }
      return childExts;
   }
   
   
   
   // see base class
   // Load all the templates and return dependencies
   @Override
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
         throws PSDeployException
   {
      init(); 
      HashMap<String, IPSAssemblyTemplate> templates = m_assemblyHelper
            .getNamedTemplatesMap();
      Iterator tmpNames   = templates.keySet().iterator(); 
      List<PSDependency> deps = new ArrayList<>();
      PSDependency dep;
      
      while (tmpNames.hasNext())
      {
         String name = (String) tmpNames.next();
         IPSAssemblyTemplate tmp = templates.get(name);
         dep = createDeployableElement(m_def, ""
               + tmp.getGUID().longValue(), name);
         if ( dep != null )
            deps.add(dep);
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

      init();
      // pack the data into the files
      List<PSDependencyFile> files = new ArrayList<>();

      IPSAssemblyTemplate tmp = findTemplateByDependencyID(dep.getDependencyId(), true);
      files.add(getDepFileFromTemplate(tmp));
      return files.iterator();
   }

   /**
    * Creates a dependency file from a given dependency data object.
    * @param tmp the template never <code>null</code>
    * @return The dependency file object, it will never be <code>null</code>.
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected static PSDependencyFile getDepFileFromTemplate(
         IPSAssemblyTemplate tmp) throws PSDeployException
   {
      if (tmp == null)
         throw new IllegalArgumentException("depData may not be null");
      String str;
      try
      {
         str = tmp.toXML();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Unable to generate a dependency file for Template:"
                     + tmp.getName());
      }
      
      return new PSDependencyFile(PSDependencyFile.TYPE_SERVICEGENERATED_XML,
            createXmlFile(XML_HDR_STR + str));
   }
   
   @Override  
   public void installDependencyFiles(PSSecurityToken tok,
      PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      init();
      
      
      // retrieve datas, variant data followed by its child data if any
      Iterator files = getTemplateDependecyFilesFromArchive(archive, dep);

      PSDependencyFile depFile = (PSDependencyFile) files.next();
      PSIdMapping clMapping = getIdMapping(ctx, dep);
      PSAssemblyTemplate tmp = null;
      
      // assume the template has been installed and find its mapping and load
      if (clMapping != null && clMapping.getTargetId() != null)
         tmp = loadTemplateByGuid(clMapping.getTargetId());
      else
         tmp = loadTemplateByGuid(dep.getDependencyId());
      
      boolean isNew = tmp == null;
      Integer ver = null;
      HashMap<Long, Integer> bVer = new HashMap<>();
      if (!isNew)
      {
         // deserialize on the existing template
         ver = ((PSAssemblyTemplate) tmp).getVersion();
         List<PSTemplateBinding> bList = tmp.getBindings();
         for (PSTemplateBinding b : bList)
         {
            if ( b != null )
               bVer.put(b.getId(),
                     b.getVersion());
         }
         ((PSAssemblyTemplate) tmp).setVersion(null);
         
         // remove any existing slot associations
         List<Long> slots = ((PSAssemblyTemplate) tmp).getTemplateSlotIds();
         
         Iterator<Long> it = slots.iterator();
         while(it.hasNext())
         {
            IPSTemplateSlot s = m_assemblySvc.findSlot(new PSGuid(
                  PSTypeEnum.SLOT, it.next()));
            if (s != null)
               tmp.removeSlot(s);
         }

      }    
      
      IPSDeployService depSvc = PSDeployServiceLocator.getDeployService();
      try
      {
         depSvc.deserializeAndSaveTemplate(tok, archive, dep, depFile, ctx,
               this, tmp, ver, bVer);
      }
      catch (PSDeployServiceException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "error occurred while installing template: "
                     + e.getLocalizedMessage());
      }

      // add transaction log
      addTransactionLogEntryByGuidType(dep, ctx, PSTypeEnum.TEMPLATE, isNew);
   }

   /**
    * TROLL thru the object and restore the versions of child-lings ;).
    * 
    * @param t
    * @param ver  the version of template
    * @param bVer the versions list of bindings that need be restored.
    * @throws PSDeployException
    */
   public static void saveTemplate(IPSAssemblyTemplate t, Integer ver,
         HashMap<Long, Integer> bVer) throws PSDeployException
   {
      // nullify and set it to the passed version of the template, can be null
      ((PSAssemblyTemplate) t).setVersion(null);
      ((PSAssemblyTemplate) t).setVersion(ver);
      List<PSTemplateBinding> bList = t.getBindings();

      for (IPSTemplateBinding b : bList)
      {
         if (b != null)
         {
            ((PSTemplateBinding) b).setVersion(null);
            ((PSTemplateBinding) b).setVersion(bVer.get(((PSTemplateBinding) b)
                  .getId()));
         }
      }
      try
      {
         m_assemblySvc.saveTemplate(t);
      }
      catch (Exception e1)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Could not save or update the template:" + t.getName() + "\n"
                     + e1.getLocalizedMessage());
      }
   }

   
   /**
    * Extract the template definition file from the archive.
    * Read the xml string representation of the template and transform the 
    * slot ids to the target server's ids and  
    * <br>
    * <b>NOTE:</b><br>
    * <u>This is also used by PSVariantDefDependencyHandler</u>
    * 
    * @param archive the ArchiveHandler to use to retrieve the files from the
    *           archive, may not be <code>null</code>
    * @param depFile the PSDependencyFile that was retrieved from the archive
    *           may not be <code>null</code>
    * @param template if not <code>null</code>, use it for deserialization
    *           else ask service to create a new template
    * @param ctx the import context never <code>null</code>
    * @return the actual template
    * @throws PSDeployException
    */
   public PSAssemblyTemplate generateTemplateFromFile(
           PSArchiveHandler archive, PSDependencyFile depFile,
           PSAssemblyTemplate template, PSImportCtx ctx)
         throws PSDeployException
   {
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (depFile == null)
         throw new IllegalArgumentException("depFile may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      PSAssemblyTemplate tmp = null;
      File f = depFile.getFile();
      String tmpStr = PSDependencyUtils.getFileContentAsString(
            archive, depFile);
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      if ( template == null )
         tmp = service.createTemplate();
      else 
         tmp = template;
      try
      {
         Set<IPSGuid> tmpGuids = PSAssemblyTemplate
               .getSlotIdsFromTemplate(tmpStr);
         // transform the slots of this template
         Set<IPSGuid> newGuids = new HashSet<>();

         for (IPSGuid g : tmpGuids)
         {
            PSIdMapping tmpMap = getIdMapping(ctx, String.valueOf(g.getUUID()),
                  PSSlotDefDependencyHandler.DEPENDENCY_TYPE);

            if (tmpMap != null)
            {
               IPSGuid newTmp = new PSGuid(PSTypeEnum.SLOT, tmpMap
                     .getTargetId());
               newGuids.add(newTmp);
            }
         }

         // modify the serialized site to include the mapped template ids
         tmpStr = PSAssemblyTemplate.replaceSlotIdsFromTemplate(tmpStr,
               newGuids);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               "Error occurred while generating site:" + e.getLocalizedMessage());
      }
      
      try
      { 
         tmp.fromXML(tmpStr);
      }
      catch (Exception e)
      {
         String err = e.getLocalizedMessage();
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               "Could not create template from file:" + f.getName() + " Error was:\n" + err);
      }
      return tmp;
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
   protected static Iterator getTemplateDependecyFilesFromArchive(
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
         {PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_SERVICEGENERATED_XML],
               dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName()};
         throw new PSDeployException(
               IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }
      return files;
   }

   
   
   // see base class
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
   
   // see base class
   public boolean shouldDeferInstallation()
   {
      return true;
   }
 
   
   /**
    * From the templates map, return all the template names
    * @return iterator on a set of names
    * @throws PSDeployException 
    */
   public Iterator getNewAssemblyTemplateNames() throws PSDeployException
   {
      init();
      Set templateNames = m_assemblyHelper.getNamedTemplatesMap().keySet();
      return templateNames.iterator();
   }
   
   // see base class
   @Override
   public void reserveNewId(PSDependency dep, PSIdMap idMap)
         throws PSDeployException
   {
      PSDependencyUtils.reserveNewId(dep, idMap, getType());
      // guids dont need ids, they are unique ids
      return; 
   }

   

   
   /**
    * Transform any IdTypes and ids in Bindings .
    * @param t the template never <code>null</code>
    * @param ctx import context never <code>null</code>
    * @param dep the dependency never <code>null</code>
    * @return do the transforms on the passed in template and return it back
    * @throws PSDeployException
    */
   public PSAssemblyTemplate doTransforms(PSAssemblyTemplate t,
                                           PSImportCtx ctx, PSDependency dep)
         throws PSDeployException
   {    
      if ( t == null )
         throw new IllegalArgumentException(
               "template cannot be null for idtype mapping");      
      PSIdMapping clMapping = getIdMapping(ctx, dep);
      if (clMapping != null && clMapping.getTargetId() != null)
      {
          t = (PSAssemblyTemplate) PSDependencyUtils.transformElementId(t,
                dep, ctx, clMapping);
      }
      
      // map any global template ids
      if (t.getGlobalTemplate() != null
            && t.getGlobalTemplateUsage().equals(GlobalTemplateUsage.Defined))
      {    
         PSIdMapping m = PSDependencyUtils.getTemplateOrVariantMapping(
               this, ctx, String.valueOf(t.getGlobalTemplate().getUUID()));
         if ( m != null && m.getTargetId() != null)
            t.setGlobalTemplate(new PSGuid(PSTypeEnum.TEMPLATE,m.getTargetId()));
      }

      return transformBindings(t, ctx);
   }

   /** 
    * util method to update the binding on the acutal template. Some bindings
    * donot have the "key", so carry the original binding
    * @param t the template never <code>null</code>
    * @param orig_b the original binding never <code>null</code>
    * @param new_b the transformed binding never <code>null</code>
    */
   private void updateTemplateBinding(IPSAssemblyTemplate t,
         PSJexlBinding orig_b, PSJexlBinding new_b)
   {
      if ( t == null )
         throw new IllegalArgumentException("template may not be null");
      if ( orig_b == null )
         throw new IllegalArgumentException("source binding may not be null");
      if ( new_b == null )
         throw new IllegalArgumentException("transformed binding may not be null");
     
      PSTemplateBinding b;
      if ( StringUtils.isNotBlank(new_b.getName()))
      {
         b = t.getBindings().stream().filter(tmp_b -> tmp_b != null && tmp_b.getVariable() != null &&
                 tmp_b.getVariable().compareTo(new_b.getName()) == 0).findFirst().map(tmp_b -> tmp_b).orElse(null);
      }
      else
      {
         b = t.getBindings().stream().filter(tmp_b -> tmp_b != null && tmp_b.getExpression() != null &&
                 tmp_b.getExpression().compareTo(orig_b.getExpression()) == 0).findFirst().map(tmp_b -> tmp_b).orElse(null);
      }
      if ( b != null )
      b.setExpression(new_b.getExpression());
   }

   /**
    * Transform any JEXL bindings in this template that have IDMappable elements
    * @param t template that needs to be evaluated for its bindings, this has
    * not yet been saved on a hibernate session.
    * @param ctx the import context never <code>null</code>
    * @return the template never <code>null</code>
    */
   private PSAssemblyTemplate transformBindings(PSAssemblyTemplate t,
                                                PSImportCtx ctx) {
      if (t == null)
         throw new IllegalArgumentException("template may not be null");
      if ( ctx == null )
         throw new IllegalArgumentException("ctx may not be null");
      /*
        Xform any id mappings
       */
      PSIdMap idMap = ctx.getCurrentIdMap();
      // No need to xform
      if (idMap == null)
         return t;

      // Set template Ids in all the bindings
      //for (PSTemplateBinding b : t.getBindings()) {
      //   b.setTemplate(t);
      //}
      
      // build the bindingIX, <name,value> pairs for the bindings
      PSJexlBindings bindings = getBindingsForIDTypes(t);
      PSJexlBindings origBindings = bindings.backupBindings().getSrcBindings();
      
      if (!bindings.getBindings().isEmpty())
      {
         for (PSJexlBinding new_b : bindings.getBindings()) {
            PSJexlBinding orig_b = origBindings.getByIndex(new_b.getIndex());
            if (StringUtils.isNotBlank(new_b.getExpression()) &&
                    StringUtils.isNotBlank(orig_b.getExpression()) &&
                    !StringUtils.equals(orig_b.getExpression(), new_b.getExpression()))
               updateTemplateBinding(t, orig_b, new_b);
         }
      }
      return t;
   }

   /**
    * Util method to generate bindings
    * @param t the template may be <code>null</code>
    * @return the bindings map may be <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private PSJexlBindings getBindingsForIDTypes(
         IPSAssemblyTemplate t)
   {
      PSJexlBindings bindings = new PSJexlBindings();
      if (t == null)
         return bindings;
      
      Iterator<PSTemplateBinding> bindingsIt= t.getBindings().iterator();
      int ix = 0;
      while ( bindingsIt.hasNext() )
      {
         IPSTemplateBinding b = bindingsIt.next();
         if  ( b != null )
            bindings.addBinding(ix++, b.getVariable(), b.getExpression());
      }
      return bindings;
   }
    
   
   /**
    * A util header for templates. IPSAssemblyTemplate upon serialization will
    * not have this header. Just prepend it.
    */
   private static final String XML_HDR_STR = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
   
   /**
    * Constant for this handler's supported type
    */
   public static final String DEPENDENCY_TYPE = "TemplateDef";

   /**
    * Da assembly Service
    */
   private static IPSAssemblyService m_assemblySvc = 
                           PSAssemblyServiceLocator.getAssemblyService();
   
   /**
    * Da assembly Service Helper
    */
   private PSAssemblyServiceHelper m_assemblyHelper = null;
   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<>();
   
   /**
    * Assembly Templates have a "different" way of storing the extension names
    * i.e: just the extensionname, no Context, no Handler name HOW NICE :-)
    * So repair to suit the current MSM scheme of extension references
    */
   private static final String ASSEMBLY_PLUGIN_PREFIX = "Java/global/percussion/assembly/";
   
   /**
    * All template assemblers are suffixed with Assembler DOH!
    */
   private static final String ASSEMBLY_PLUGIN_SUFFIX ="Assembler";
   
   /**
    * IdType naming for template bindings, its counterpart exists in 
    * PSIDTypesPanelResources.properties
    */
   private static final String TEMPLATE_BINDINGS  = "TemplateBinding";
   
   static
   {
      ms_childTypes.add(PSCEDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSlotDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSAclDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSTemplateDependencyHandler.DEPENDENCY_TYPE);
   }
}
