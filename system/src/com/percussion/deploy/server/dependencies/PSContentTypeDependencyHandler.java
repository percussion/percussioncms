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

package com.percussion.deploy.server.dependencies;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentType;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.deploy.client.IPSDeployConstants;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deploy.objectstore.PSApplicationIDTypes;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDependencyFile;
import com.percussion.deploy.objectstore.PSDeployableObject;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.objectstore.PSIdMapping;
import com.percussion.deploy.server.IPSIdTypeHandler;
import com.percussion.deploy.server.PSAppTransformer;
import com.percussion.deploy.server.PSArchiveHandler;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyManager;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.deploy.server.PSImportCtx;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationFlow;
import com.percussion.design.objectstore.PSCommandHandlerStylesheets;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSContentEditorMapper;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentTypeHelper;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSRequestor;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSServer;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.IPSAssemblyTemplate.TemplateType;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.data.PSContentTemplateDesc;
import com.percussion.services.contentmgr.data.PSNodeDefinition;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.PSCollection;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class to handle packaging and deploying a content type definition.
 */
public class PSContentTypeDependencyHandler
      extends
         PSContentEditorObjectDependencyHandler implements IPSIdTypeHandler
{

   /**
    * Construct a dependency handler.
    * 
    * @param def The def for the type supported by this handler. May not be
    *           <code>null</code> and must be of the type supported by this
    *           class. See {@link #getType()} for more info.
    * @param dependencyMap The full dependency map. May not be
    *  <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSContentTypeDependencyHandler(PSDependencyDef def,
         PSDependencyMap dependencyMap) {
      super(def, dependencyMap);
   }

   /**
    * A util method to get the CE dependencies from the PSDataSet
    * 
    * @param tok The security token to use if objectstore access is required,
    *           may not be <code>null</code>.
    * @param name the application name that is a ContentEditor
    * @return an iterator for CE dependencies from the DataSet
    * @throws PSDeployException
    */
   private List<PSDependency> getCEChildDependencies(PSSecurityToken tok,
         String name) throws PSDeployException, PSNotFoundException {
      List<PSDependency> childDeps = new ArrayList<>();
      PSApplication app = PSAppObjectDependencyHandler
            .getApplication(tok, name);

      PSCollection dataSetColl = app.getDataSets();
      if (dataSetColl != null)
      {
         Iterator datasets = dataSetColl.iterator();
         PSDependencyHandler wfHandler = getDependencyHandler(
               PSWorkflowDependencyHandler.DEPENDENCY_TYPE);
         while (datasets.hasNext())
         {
            PSDataSet ds = (PSDataSet) datasets.next();
            if (ds instanceof PSContentEditor)
            {
               PSContentEditor ce = (PSContentEditor) ds;
               // add system def if haven't already
               PSDependencyHandler sysDefHandler = getDependencyHandler(
                     PSSystemDefDependencyHandler.DEPENDENCY_TYPE);
               Iterator<PSDependency> sysdefDeps = sysDefHandler
                     .getDependencies(tok);
               // should return a single dep
               if (sysdefDeps.hasNext())
                  childDeps.add(sysdefDeps.next());

               int wfId = ce.getWorkflowId();
               PSDependency wfDep = wfHandler.getDependency(tok, String
                     .valueOf(wfId));
               if (wfDep != null)
                  childDeps.add(wfDep);
               PSWorkflowInfo wfInfo = ce.getWorkflowInfo();
               if (wfInfo != null)
               {
                  Iterator ids = wfInfo.getValues();
                  while (ids.hasNext())
                  {
                     PSDependency childDep = null;
                     try {
                         childDep = wfHandler.getDependency(tok, ids
                                .next().toString());
                     } catch (PSNotFoundException e) {
                        log.warn(e.getMessage());
                        log.debug(e.getMessage(),e);
                     }
                     if (childDep != null)
                        childDeps.add(childDep);

                  }
               }

               PSContentEditorPipe cePipe = (PSContentEditorPipe) ce.getPipe();

               // check for tables
               childDeps.addAll(checkLocatorTables(tok, cePipe.getLocator()));

               PSContentEditorMapper ceMapper = cePipe.getMapper();
               PSUIDefinition uiDef = ceMapper.getUIDefinition();
               childDeps.addAll(checkUIDef(tok, uiDef));

               PSDependencyHandler sharedHandler = getDependencyHandler(
                     PSSharedGroupDependencyHandler.DEPENDENCY_TYPE);
               Iterator sharedIncludes = ceMapper.getSharedFieldIncludes();
               while (sharedIncludes.hasNext())
               {
                  String groupName = (String) sharedIncludes.next();
                  PSDependency sharedDep = sharedHandler.getDependency(tok,
                        groupName);
                  if (sharedDep != null)
                     childDeps.add(sharedDep);
               }

               PSDependencyHandler sysElHandler = getDependencyHandler(
                     PSSystemDefElementDependencyHandler.DEPENDENCY_TYPE);
               PSContentEditorSharedDef sharedDef = PSServer
                     .getContentEditorSharedDef();
               PSApplicationFlow appFlow = ce.getApplicationFlow();
               if (appFlow == null && sharedDef != null)
                  appFlow = sharedDef.getApplicationFlow();
               if (appFlow != null)
               {
                  PSDependency flowDep = sysElHandler.getDependency(tok,
                        PSSystemDefElementDependencyHandler.APP_FLOW_ID);
                  if (flowDep != null)
                     childDeps.add(flowDep);
               }

               PSCommandHandlerStylesheets sheets = ce.getStylesheetSet();
               if (sheets == null && sharedDef != null)
                  sheets = sharedDef.getStylesheetSet();
               if (sheets != null)
               {
                  PSDependency sheetsDep = sysElHandler.getDependency(tok,
                        PSSystemDefElementDependencyHandler.CMD_SHEETS_ID);
                  if (sheetsDep != null)
                     childDeps.add(sheetsDep);
               }
            }
         }
      }
      return childDeps;
   }

   /**
    * Helper method to package child dependencies: template and or variant
    * 
    * @param tok The security token to use if objectstore access is required,
    *           may not be <code>null</code>.
    * @param dep the ContentType Dependency may not be <code>null</code>
    * @return set of template dependencies
    * @throws PSDeployException
    */
   private Set<PSDependency> getTemplateDependencies(PSSecurityToken tok,
         PSDependency dep) throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dependency may not be null");

      Set<PSDependency> childDeps = new HashSet<>();
      PSDependencyHandler varHandler = getDependencyHandler(
            PSVariantDefDependencyHandler.DEPENDENCY_TYPE);
      PSDependencyHandler tmpPkgHandler = getDependencyHandler(
            PSTemplateDependencyHandler.DEPENDENCY_TYPE);
      PSDependencyHandler tmpDefHandler = getDependencyHandler(
            PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);
      IPSNodeDefinition node = findNodeDefByDependencyID(dep.getDependencyId());
      if (node == null)
         return childDeps;
      Set<IPSGuid> tmpGuids = node.getVariantGuids();
      IPSAssemblyService assemblySvc = PSAssemblyServiceLocator
            .getAssemblyService();

      for (IPSGuid guid : tmpGuids)
      {
         PSDependency childDep = null;
         // load just the template and not its baggage
         IPSAssemblyTemplate t = null;
         try
         {
            t = assemblySvc.loadTemplate(guid, false);
         }
         catch (PSAssemblyException e)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
                  "Unable to load template while catalogging ContentType " +
                  "dependencies\n" + e.getLocalizedMessage());
         }
         if (t == null)
            continue;
         String guidStr = String.valueOf(t.getGUID().longValue());

         /**
          * IF t.isVariant() return "variantDef"Dependency 
          * else if t.isLocal return "(local)templateDef"Dependency 
          * else return "templateElement"Dependency
          */
         if (t.isVariant())
            childDep = varHandler.getDependency(tok, guidStr);
         else if (t.getTemplateType().equals(TemplateType.Local))
         {
            childDep = tmpDefHandler.getDependency(tok, guidStr);
            childDep.setDependencyType(PSDependency.TYPE_LOCAL);
         }
         else
         {
            // get the template package
            childDep = tmpPkgHandler.getDependency(tok, guidStr);
         }
         if (childDep != null)
            childDeps.add(childDep);
      }
      return childDeps;
   }

   // see base class
   @Override
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      Set<PSDependency> childDeps = new HashSet<>();
      IPSNodeDefinition node = findNodeDefByDependencyID(dep.getDependencyId());

      String appName = PSDependencyUtils
            .getColumnAppName(((PSNodeDefinition) node).getNewRequest());
      if (StringUtils.isBlank(appName))
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "App name was null");
      childDeps.addAll(getCEChildDependencies(tok, appName));

      // Acl deps
      addAclDependency(tok, PSTypeEnum.NODEDEF, dep, childDeps);

      // Package the template deps
      childDeps.addAll(getTemplateDependencies(tok, dep));

      // Don't forget the idTypes
      childDeps.addAll(PSIdTypeDependencyHandler.getIdTypeDependencies(tok,
            dep, this));
      
      // Package the icon file
      PSItemDefinition item = findContentTypeByNodeDef(node);
      PSContentEditor editor = item.getContentEditor();
      String source = editor.getIconSource();
      if (source.equals(PSContentEditor.ICON_SOURCE_SPECIFIED))
      {      
         String iconFile = PSItemDefManager.RX_ICON_FOLDER +
            editor.getIconValue();

         PSDependency fileDep = getDependencyHandler(
               PSImageFileDependencyHandler.DEPENDENCY_TYPE).getDependency(
                     tok, iconFile);
         if (fileDep != null)
         {
            fileDep.setDependencyType(PSDependency.TYPE_LOCAL);
            childDeps.add(fileDep);
         }
      }
      
      return childDeps.iterator();
   }

   /**
    * Creates a dependency file from a given dependency data object.
    * 
    * @param item the item definition, never <code>null</code>
    * @return The dependency file object, it will never be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected PSDependencyFile getDepFileFromItemDef(PSItemDefinition item)
         throws PSDeployException
   {
      if (item == null)
         throw new IllegalArgumentException("depData may not be null");
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      try
      {
         Element elem = item.toXml(doc);
         PSXmlDocumentBuilder.replaceRoot(doc, elem);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Unable to generate a dependency file for Template:"
                     + item.getName());
      }
      return new PSDependencyFile(PSDependencyFile.TYPE_ITEM_DEFINITION,
            createXmlFile(doc));
   }

   /**
    * generate a node definition file
    * 
    * @param node node definition never <code>null</code>
    * @return the dependency file
    * @throws PSDeployException
    */
   protected PSDependencyFile getDepFileFromNodeDef(IPSNodeDefinition node)
         throws PSDeployException
   {
      if (node == null)
         throw new IllegalArgumentException("depData may not be null");
      String str;
      try
      {
         str = node.toXML();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Unable to generate a dependency file for NodeDefinition:"
                     + node.getName());
      }

      return new PSDependencyFile(PSDependencyFile.TYPE_NODE_DEFINITION,
            createXmlFile(IPSDeployConstants.XML_HDR_STR + str));
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
   protected static Iterator getItemDefFilesFromArchive(
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
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      List<PSDependencyFile> files = new ArrayList<>();

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      PSItemDefinition item = findItemDefByDependencyID(dep.getDependencyId());
      if (item != null)
         files.add(getDepFileFromItemDef(item));

      IPSNodeDefinition node = findNodeDefByDependencyID(dep.getDependencyId());
      if (node != null)
         files.add(getDepFileFromNodeDef(node));
      return files.iterator();
   }

   /**
    * Extract the item definition file from the archive and install/update
    * 
    * @param archive the ArchiveHandler to use to retrieve the files from the
    *           archive, may not be <code>null</code>
    * @param depFile the PSDependencyFile that was retrieved from the archive
    *           may not be <code>null</code>
    * @param item if not <code>null</code>, use it for deserialization else
    *           ask service to create a new template
    * @return the actual template
    * @throws PSDeployException
    */
   protected PSItemDefinition generateItemDefFromFile(PSArchiveHandler archive,
         PSDependencyFile depFile, PSItemDefinition item)
         throws PSDeployException
   {
      PSItemDefinition tmp = null;
      Document doc = null;
      if (depFile.getType() == PSDependencyFile.TYPE_ITEM_DEFINITION)
         doc = createXmlDocument(archive.getFileData(depFile));
      else
      {
         Object[] args =
         {
               PSDependencyFile.TYPE_ENUM[depFile.getType()],
               PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_ITEM_DEFINITION]};
         throw new PSDeployException(
               IPSDeploymentErrors.WRONG_DEPENDENCY_FILE_TYPE, args);
      }

      try
      {
         if (item == null)
            tmp = new PSItemDefinition(doc.getDocumentElement(), true);
         else
         {
            tmp = item;
            tmp.fromXml(doc.getDocumentElement(), null, null);
         }
      }
      catch (PSUnknownNodeTypeException e)
      {
         String err = e.getLocalizedMessage();
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Could not create template from file:"
                     + depFile.getFile().getName() + " Error was:\n" + err);
      }
      return tmp;
   }

   /**
    * Extract the item definition file from the archive and install/update
    * 
    * @param archive the ArchiveHandler to use to retrieve the files from the
    *           archive, may not be <code>null</code>
    * @param depFile the PSDependencyFile that was retrieved from the archive
    *           may not be <code>null</code>
    * @param node if not <code>null</code>, use it for deserialization else
    *           ask service to create a new node
    * @return the actual node
    * @throws PSDeployException
    */
   protected IPSNodeDefinition generateNodeDefFromFile(
         PSArchiveHandler archive, PSDependencyFile depFile,
         IPSNodeDefinition node) throws PSDeployException
   {

      File f = depFile.getFile();

      String tmpStr = PSDependencyUtils.getFileContentAsString(
            archive, depFile);

      try
      {
         if (node == null)
         {
            IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
            node = mgr.createNodeDefinition();
         }
         node.fromXML(tmpStr);
      }
      catch (Exception e)
      {
         String err = e.getLocalizedMessage();
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Could not create NodeDefinition from file:" + f.getName()
                     + " Error was:\n" + err);
      }
      return node;
   }

   /**
    * Helper method to MAP the ContentTypeID on the item definition
    * 
    * @param dep the dependency from the archive
    * @param item the definition either deserialized or loaded by the system
    * @param clMapping the mapping for this dependency
    * @return PSItemDefinition with the new ContentTypeID
    * @throws PSDeployException
    */
   private PSItemDefinition transformElementIdFromMapping(PSDependency dep,
         PSItemDefinition item, PSIdMapping clMapping) throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dependency may not be null");
      if (item == null)
         throw new IllegalArgumentException("Item definition may not be null");

      // nothing to transform ??
      if (clMapping == null || StringUtils.isBlank(clMapping.getTargetId()))
         return item;

      PSTypeEnum type = PSDependencyManager.getInstance().getGuidType(
            dep.getObjectType());
      if (type == null)
         throw new IllegalArgumentException("Dependency not a GUID type");

      int guidval = -1;
      try
      {
         guidval = Integer.parseInt(clMapping.getTargetId());
      }
      catch (NumberFormatException ne)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               " was expecting an int value: ");
      }

      item.setTypeId(guidval);
      PSContentEditor ce = item.getContentEditor();
      ce.setContentType(guidval);
      return item;
   }

   /**
    * Do the transforms on the contenttype params
    * 
    * @param tok the security token never <code>null</code>
    * @param archive the import archive never <code>null</code>
    * @param dep the dependency never <code>null</code>
    * @param ctx the import context never <code>null</code>
    * @param item the item definition never <code>null</code>
    * @throws PSDeployException
    * @throws PSDeployException
    */
   private void transformContentTypeParams(PSSecurityToken tok,
         PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx,
         PSItemDefinition item) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dependency may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("import context may not be null");
      if (item == null)
         throw new IllegalArgumentException("item definition may not be null");

      PSIdMap idMap = ctx.getCurrentIdMap();

      // translate id's using idTypes and idMap
      transformIds(item, ctx.getIdTypes(), idMap);
      transformWorkflowIds(item, ctx);

      // transform UI Defs
      PSContentEditorPipe cePipe = (PSContentEditorPipe) item
            .getContentEditor().getPipe();
      transformUIDef(idMap, cePipe.getMapper().getUIDefinition());
   }

   /**
    * Transform any IdTypes and ids in the contenttype .
    * 
    * @param tok the security token never <code>null</code>
    * @param archive the archive handler never <code>null</code>
    * @param dep the dependency never <code>null</code>
    * @param ctx import context never <code>null</code>
    * @param item the item definition never <code>null</code>
    * @param descSet the cvDescriptors describing the contenttype<==>Template
    *           relationships may be <code>null</code>
    * @param isNew boolean if the ContentType does not yet exist on the system
    * @throws PSDeployException
    * @return a set of PSContentTemplateDescriptors
    */
   private Set<PSContentTemplateDesc> doTransforms(PSSecurityToken tok,
         PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx,
         PSItemDefinition item, Set<PSContentTemplateDesc> descSet, 
         boolean isNew) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dependency may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("import context may not be null");
      if (item == null)
         throw new IllegalArgumentException("item definition may not be null");

      transformElementIdFromMapping(dep, item, getIdMapping(ctx, dep));
      transformContentTypeParams(tok, archive, dep, ctx, item);
      descSet = transformCVDescriptorMappings(ctx, descSet, item.getTypeId(),
            isNew);
      return descSet;
   }

   /**
    * TROLL thru the object and restore the versions of child-lings ;).
    * 
    * @param s node which needs to be saved/updated
    * @param ver the version of node
    * @throws PSDeployException
    */
   public void saveNode(IPSNodeDefinition s, Integer ver)
         throws PSDeployException
   {
      // nullify and set it to the passed version of the template, can be null
      ((PSNodeDefinition) s).setVersion(ver);
      try
      {
         IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
         List<IPSNodeDefinition> nodes = new ArrayList<>();
         nodes.add(s);
         mgr.saveNodeDefinitions(nodes);
      }
      catch (Exception e1)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Could not save or update the slot:" + s.getName() + "\n"
                     + e1.getLocalizedMessage());
      }
   }

   // see base class
   @Override
   public boolean shouldDeferInstallation()
   {
      return true;
   }

   /**
    * From the node definition, return the template relationships.
    * 
    * @param archive the archive handler never <code>null</code>
    * @param dep the dependency never <code>null</code>
    * @param ctx import context never <code>null</code>
    * @return the set of template relationships: cvDescriptors
    * @throws PSDeployException
    */
   private Set<PSContentTemplateDesc> getTemplateRelationships(
         PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
         throws PSDeployException
   {
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dependency may not be null");

      if (ctx == null)
         throw new IllegalArgumentException("ImportContext may not be null");

      PSDependencyFile nodeFile = null;
      Set<PSContentTemplateDesc> tmpRel = null;

      Iterator files = archive.getFiles(dep);
      while (files.hasNext())
      {
         PSDependencyFile depFile = (PSDependencyFile) files.next();
         if (depFile.getType() == PSDependencyFile.TYPE_NODE_DEFINITION)
         {
            nodeFile = depFile;
            break;
         }
      }
      // no node file, no template relationships
      if (nodeFile == null)
         return tmpRel;
      IPSNodeDefinition node = generateNodeDefFromFile(archive, nodeFile, null);
      return node == null ? tmpRel : ((PSNodeDefinition) node)
            .getCvDescriptors();
   }

   /**
    * Helper to catalog templates by guids
    * 
    * @return the set of template guids as strings
    * @throws PSDeployException
    */
   private Set<String> catalogTemplates() throws PSDeployException
   {
      IPSAssemblyService aSvc = PSAssemblyServiceLocator.getAssemblyService();
      Set<String> tmpGuids = new HashSet<>();
      Set<IPSAssemblyTemplate> tmpSet;
      try
      {
         tmpSet = aSvc.findAllTemplates();
      }
      catch (PSAssemblyException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "catalogging templates failed");
      }

      for (IPSAssemblyTemplate tmp : tmpSet)
         tmpGuids.add(String.valueOf(tmp.getGUID().longValue()));
      return tmpGuids;
   }   
   
   
   /**
    * helper method to transform the contenttypeid
    * @param descSet the ContentTypeTemplateDescriptor set, never
    *           <code>null</code>
    * @param id the contenttype id never <code>null</code>
    * @param isNew boolean <code>true</code> if itemDefinition is new
    * 
    * @return the ContentTypeTemplateDescriptor set
    * @throws PSDeployException
    */
   private Set<PSContentTemplateDesc> transformCVDescriptorMappings(
         PSImportCtx ctx, Set<PSContentTemplateDesc> descSet, int id, 
         boolean isNew) throws PSDeployException
   {
      if (descSet == null)
         return descSet;
      
      PSIdMap idMap = ctx.getCurrentIdMap();
      if (idMap == null)
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "PSIdMap cannot be null");
      
      IPSGuid nodeGuid = new PSGuid(PSTypeEnum.NODEDEF, id);   
      Set<String> tmpGuids = catalogTemplates();
      Iterator<PSContentTemplateDesc> it = descSet.iterator();
      while ( it.hasNext())
      {
         PSContentTemplateDesc desc = it.next();
         // transform template id
         String tmpId = String.valueOf(desc.getTemplateId().getUUID());
         PSIdMapping m = PSDependencyUtils.getTemplateOrVariantMapping(this,
               ctx, tmpId);
         if ( m != null && m.getTargetId()!= null &&
               tmpGuids.contains(m.getTargetId()))
         {
            PSGuid tmpGuid = new PSGuid(PSTypeEnum.TEMPLATE, m.getTargetId());
            desc.setTemplateId(tmpGuid);
         }
         else
         {
            log.warn("Removing Content <==>Template relationship{"
                  + desc.getContentTypeId().toString() + ","
                  + desc.getTemplateId().toString()
                  + "}, because one of the mapping element is not deployed.");
            
            it.remove();
            continue;
         }
         
         // transform content type id
         desc.setContentTypeId(nodeGuid);

         // transform templatetype id, only if new...
         if ( isNew )
         {
            IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
            desc.setId(gmgr.createGuid(PSTypeEnum.INTERNAL).longValue());
         }

      }
      return descSet;
   }

   // see base class
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

      // get the template relationships from the nodedef file
      Set<PSContentTemplateDesc> descSet = getTemplateRelationships(archive,
            dep, ctx);
      IPSNodeDefinition node = null;
      PSIdMapping map = getIdMapping(ctx, dep);
      
      if (map != null && !StringUtils.isBlank(map.getTargetId()) )
         node = findNodeDefByDependencyID(map.getTargetId());
      else
         node = findNodeDefByDependencyID(dep.getDependencyId());
      

      boolean isNew = (node == null) ? true : false;
      Integer curVer = (isNew == false) ? ((PSNodeDefinition) node)
            .getVersion() : null;
      PSItemDefinition item = (isNew == false)
            ? findContentTypeByNodeDef(node)
            : null;

      // assumes one nodedef and one itemdef CAN THERE BE MANY? WHY NOT????
      PSDependencyFile itemFile = null;
      Iterator files = archive.getFiles(dep);
      while (files.hasNext())
      {
         PSDependencyFile file = (PSDependencyFile) files.next();
         if (file.getType() == PSDependencyFile.TYPE_ITEM_DEFINITION)
            itemFile = file;
      }

      if (itemFile == null)
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "could not locate the item definition file in the archive");
      item = generateItemDefFromFile(archive, itemFile, item);

      try
      {
         // if a mapping exists, do the transforms
         if (map != null && map.getTargetId() != null)
            descSet = doTransforms(tok, archive, dep, ctx, item, descSet,
                  isNew);
         PSContentTypeHelper.saveContentType(item, descSet, isNew ? -1 : curVer
               .intValue(), true);
      }
      catch (Exception e)
      {
         String msg = "\n Error was: " + e.getLocalizedMessage();
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Error occurred while installing content type:"
                     + item.getAppName() + msg);
      }

      // make sure mapping is reset after update
      PSIdMapping mapping = getIdMapping(ctx, dep);
      if (mapping != null)
         mapping.setIsNewObject(false);

      // add txn log entry
      addTransactionLogEntryByGuidType(dep, ctx, PSTypeEnum.SLOT, isNew);
   }

   /**
    * catalog all the content types. This method delegates catalogging to
    * PSContentTypeHelper.
    */
   private List<IPSNodeDefinition> getContentTypes()
   {
      List<IPSNodeDefinition> ctList = null;
      ctList = PSContentTypeHelper.loadNodeDefs("*");
      return ctList;
   }

   private IPSNodeDefinition findNodeDefByDependencyID(String depId)
         throws PSDeployException
   {
      if (depId == null || depId.trim().length() == 0)
         throw new IllegalArgumentException(
               "dependency ID may not be null or empty");
      if (depId == null || depId.trim().length() == 0)
         throw new IllegalArgumentException(
               "dependency ID may not be null or empty");

      // Generate a guid
      PSGuid guid = new PSGuid(PSTypeEnum.NODEDEF, PSDependencyUtils
            .getGuidValFromString(depId, m_def.getObjectTypeName()));
      Iterator<IPSNodeDefinition> it = getContentTypes().iterator();
      IPSNodeDefinition node = null;
      boolean found = false;
      while (it.hasNext() && !found)
      {
         node = it.next();
         if (node.getGUID().equals(guid))
            found = true;
      }
      return (found == true) ? node : null;
   }

   /**
    * Utility method to find the ItemDefinition given a node def
    * 
    * @param node the IPSNodeDefinition, all it does is save one lookup of a
    *           node may not be <code>null</code>
    * @return the ItemDefinition for the node def, never <code>null</code>
    * @throws PSDeployException
    */
   private PSItemDefinition findContentTypeByNodeDef(IPSNodeDefinition node)
         throws PSDeployException
   {
      if (node == null)
         throw new IllegalArgumentException("Node definition may not be null");
      PSItemDefinition item = null;
      try
      {
         item = PSContentTypeHelper.loadItemDef(node.getGUID());
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Error occurred while cataloging this contenttype:"
                     + node.getName() + "Error was: "
                     + e.getLocalizedMessage());
      }
      return item;
   }

   /**
    * Utility method to find the ItemDefinition by a given guid(as a
    * STRINGGGGGG)
    * 
    * @param depId the guid
    * @return <code>null</code> if Variant is not found
    * @throws PSDeployException
    */
   private PSItemDefinition findItemDefByDependencyID(String depId)
         throws PSDeployException
   {
      if (depId == null || depId.trim().length() == 0)
         throw new IllegalArgumentException(
               "dependency ID may not be null or empty");

      IPSNodeDefinition node = findNodeDefByDependencyID(depId);
      PSItemDefinition item = null;
      if (node != null)
      {
         try
         {
            item = PSContentTypeHelper.loadItemDef(node.getGUID());
            /**
             * Sometimes, there can be WorkflowInfo with empty values, in which
             * case load the item definition and get WORKFLOWINFO, before
             * packaging Only known scenario: Install pre 6.0, upgrade to 6.0
             * and archive, this will not happen if the app is modified in WB.
             */
            PSWorkflowInfo wfInfo = item.getContentEditor().getWorkflowInfo();
            if ( wfInfo != null && !wfInfo.getValues().hasNext())
            {
               PSItemDefManager itemMgr = PSItemDefManager.getInstance();
               PSItemDefinition i = itemMgr.getItemDef(
                     node.getGUID().longValue(), -1);
               PSWorkflowInfo lwfInfo = i.getContentEditor().getWorkflowInfo();
               item.getContentEditor().setWorkflowInfo(lwfInfo);
            }
            
         }
         catch (Exception e)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
                  "Error occurred while cataloging this contenttype:"
                        + node.getName() + "Error was: "
                        + e.getLocalizedMessage());
         }
      }
      return item;
   }

   // see base class
   @Override
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      List<PSDependency> deps = new ArrayList<>();
      Iterator<IPSNodeDefinition> ctIt = getContentTypes().iterator();
      while (ctIt.hasNext())
      {
         IPSNodeDefinition node = ctIt.next();

         // Dont add "PSCmsObject.TYPE_ITEM"
         if (node.getObjectType() == PSComponentSummary.TYPE_FOLDER)
            continue;
         PSDependency dep = createDeployableElement(m_def, String.valueOf(node
               .getGUID().longValue()), node.getInternalName());
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

      PSDeployableObject dep = null;
      IPSNodeDefinition node = findNodeDefByDependencyID(id);
      if (node != null)
         dep = createDependency(m_def, String.valueOf(node.getGUID()
               .longValue()), node.getInternalName());
      return dep;
   }

   /**
    * Provides the list of child dependency types this class can discover. The
    * child types supported by this handler are:
    * <ol>
    * <li>Application</li>
    * <li>Acl</li>
    * <li>Template</li>
    * <li>Image</li>
    * </ol>
    * 
    * @return An iterator over zero or more types as <code>String</code>
    *         objects, never <code>null</code>, does not contain
    *         <code>null</code> or empty entries.
    */
   @Override
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   /**
    * Get the type of depedency supported by this handler..
    * 
    * @return the type, never <code>null</code> or empty.
    */
   @Override
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   // see base class
   @Override
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (StringUtils.isBlank(id))
         throw new IllegalArgumentException("id may not be null or empty");

      if ( !PSGuid.isValid(PSTypeEnum.NODEDEF, id) )
         return false;
      IPSNodeDefinition node = findNodeDefByDependencyID(id);
      return node != null;
   }

   // see base class
   @Override
   public void reserveNewId(PSDependency dep, PSIdMap idMap)
         throws PSDeployException
   {
      PSDependencyUtils.reserveNewId(dep, idMap, getType());
   }

   public PSApplicationIDTypes getIdTypes(PSSecurityToken tok, PSDependency dep)
         throws PSDeployException
   {
      PSApplicationIDTypes idTypes = new PSApplicationIDTypes(dep);
      try
      {
         PSApplication app = getCEAppFromDependencyID(tok, dep
               .getDependencyId());

         if (app == null)
            return idTypes;
         PSCollection dataSetColl = app.getDataSets();

         if (dataSetColl == null)
            return idTypes;

         Iterator datasets = dataSetColl.iterator();
         while (datasets.hasNext())
         {
            List mappings = new ArrayList();
            PSDataSet ds = (PSDataSet) datasets.next();
            String reqName = PSApplicationDependencyHandler.getResourceName(ds);

            // check page selection/validation properties
            PSRequestor requestor = ds.getRequestor();
            mappings.clear();
            PSAppTransformer.checkConditionals(mappings, requestor
                  .getSelectionCriteria().iterator(), null);
            idTypes.addMappings(reqName,
                  IPSDeployConstants.ID_TYPE_ELEMENT_REQUEST_PROPERTIES,
                  mappings.iterator());

            mappings.clear();
            PSAppTransformer.checkConditionals(mappings, requestor
                  .getValidationRules().iterator(), null);
            idTypes.addMappings(reqName,
                  IPSDeployConstants.ID_TYPE_ELEMENT_REQUEST_VALIDATIONS,
                  mappings.iterator());

            if (ds instanceof PSContentEditor)
            {
               PSContentEditor ce = (PSContentEditor) ds;

               // get the pipe
               PSContentEditorPipe cePipe = (PSContentEditorPipe) ce.getPipe();
               PSContentEditorMapper ceMapper = cePipe.getMapper();

               // check fields
               PSFieldSet fs = ceMapper.getFieldSet();
               mappings.clear();
               PSAppTransformer.checkFieldSet(mappings, fs, null);
               idTypes.addMappings(reqName,
                     IPSDeployConstants.ID_TYPE_ELEMENT_CE_FIELD, mappings
                           .iterator());

               // check uidef default ui's uiset
               mappings.clear();
               PSAppTransformer.checkUIDef(mappings,
                     ceMapper.getUIDefinition(), null);
               idTypes.addMappings(reqName,
                     IPSDeployConstants.ID_TYPE_ELEMENT_CE_UI_DEF, mappings
                           .iterator());

               // walk all other possiblities
               PSApplicationFlow appFlow = ce.getApplicationFlow();
               if (appFlow != null)
               {
                  mappings.clear();
                  PSAppTransformer.checkAppFlow(mappings, appFlow, null);
                  idTypes.addMappings(reqName,
                        IPSDeployConstants.ID_TYPE_ELEMENT_CE_APP_FLOW,
                        mappings.iterator());
               }

               mappings.clear();
               PSAppTransformer.checkCustomActionGroups(mappings, ce
                     .getCustomActionGroups(), null);
               idTypes.addMappings(reqName,
                     IPSDeployConstants.ID_TYPE_ELEMENT_CE_CUSTOM_ACTIONS,
                     mappings.iterator());

               mappings.clear();
               PSAppTransformer.checkConditionalExits(mappings, ce
                     .getInputTranslations(), null);
               idTypes.addMappings(reqName,
                     IPSDeployConstants.ID_TYPE_ELEMENT_CE_INPUT_TRANSLATIONS,
                     mappings.iterator());

               mappings.clear();
               PSAppTransformer.checkConditionalExits(mappings, ce
                     .getOutputTranslations(), null);
               idTypes.addMappings(reqName,
                     IPSDeployConstants.ID_TYPE_ELEMENT_CE_OUTPUT_TRANSLATIONS,
                     mappings.iterator());

               mappings.clear();
               Iterator links = ce.getSectionLinkList();
               while (links.hasNext())
               {
                  PSAppTransformer.checkUrlRequest(mappings,
                        (PSUrlRequest) links.next(), null);
               }
               idTypes.addMappings(reqName,
                     IPSDeployConstants.ID_TYPE_ELEMENT_CE_SECTION_LINK_LIST,
                     mappings.iterator());

               mappings.clear();
               PSAppTransformer.checkStylesheetSet(mappings, ce
                     .getStylesheetSet(), null);
               idTypes
                     .addMappings(
                           reqName,
                           IPSDeployConstants.ID_TYPE_ELEMENT_CE_COMMAND_HANDLER_STYLESHEETS,
                           mappings.iterator());

               mappings.clear();
               PSAppTransformer.checkConditionalExits(mappings, ce
                     .getValidationRules(), null);
               idTypes.addMappings(reqName,
                     IPSDeployConstants.ID_TYPE_ELEMENT_CE_VALIDATION_RULES,
                     mappings.iterator());
            }
         }
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
               .getLocalizedMessage());
      }
      return idTypes;
   }

   /**
    * From a dependency figure out the CE Application name
    * 
    * @param tok The security token to use if objectstore access is required,
    *           may not be <code>null</code>.
    * @param id the dependency id as a string never <code>null</code>
    * @return the application may be <code>null</code>
    * @throws PSDeployException
    */
   private PSApplication getCEAppFromDependencyID(PSSecurityToken tok, 
         String id) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException(" security token may not be null");
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      PSApplication app = null;

      IPSNodeDefinition node = findNodeDefByDependencyID(id);

      // unfortunately relying on the new request . . .
      String appName = PSContentType
            .getAppNameFromRequestUrl(((PSNodeDefinition) node).getNewRequest());
      if (appName != null)
         try
         {
            app = os.getApplicationObject(appName, tok);
         }
         catch (Exception e)
         {
            String msg = "\n Error was:" + e.getLocalizedMessage();
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
                  "Could not locate CE application:" + appName + msg);
         }
      return app;
   }

   public void transformIds(Object obj, PSApplicationIDTypes idTypes,
         PSIdMap idMap) throws PSDeployException
   {
      if (obj == null)
         throw new IllegalArgumentException("item definition may not be null");

      if (idTypes == null)
         throw new IllegalArgumentException("idTypes may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      // walk id types and perform any transforms
      PSItemDefinition item = null;
      if (!(obj instanceof PSItemDefinition))
         return;
      item = (PSItemDefinition) obj;

      Iterator resources = idTypes.getResourceList(false);
      while (resources.hasNext())
      {
         String resource = (String) resources.next();
         PSContentEditor ce = item.getContentEditor();
         Iterator elements = idTypes.getElementList(resource, false);
         while (elements.hasNext())
         {
            String element = (String) elements.next();
            Iterator mappings = idTypes.getIdTypeMappings(resource, element,
                  false);
            while (mappings.hasNext())
            {
               PSApplicationIDTypeMapping mapping = 
                  (PSApplicationIDTypeMapping) mappings.next();

               if (mapping.getType().equals(
                     PSApplicationIDTypeMapping.TYPE_NONE))
               {
                  continue;
               }
               PSContentEditorPipe cePipe = (PSContentEditorPipe) ce.getPipe();
               PSContentEditorMapper ceMapper = cePipe.getMapper();

               if (element
                     .equals(IPSDeployConstants.ID_TYPE_ELEMENT_CE_APP_FLOW))
               {
                  PSApplicationFlow appFlow = ce.getApplicationFlow();
                  if (appFlow != null)
                     PSAppTransformer.transformAppFlow(appFlow, mapping, idMap);
               }
               else if (element
                     .equals(IPSDeployConstants.ID_TYPE_ELEMENT_CE_COMMAND_HANDLER_STYLESHEETS))
               {
                  PSCommandHandlerStylesheets sheets = ce.getStylesheetSet();
                  if (sheets != null)
                     PSAppTransformer.transformStylesheetSet(sheets, mapping,
                           idMap);
               }
               else if (element
                     .equals(IPSDeployConstants.ID_TYPE_ELEMENT_CE_CUSTOM_ACTIONS))
               {
                  PSAppTransformer.transformCustomActionGroups(ce
                        .getCustomActionGroups(), mapping, idMap);
               }
               else if (element
                     .equals(IPSDeployConstants.ID_TYPE_ELEMENT_CE_FIELD))
               {
                  PSAppTransformer.transformFieldSet(ceMapper.getFieldSet(),
                        mapping, idMap);
               }
               else if (element
                     .equals(IPSDeployConstants.ID_TYPE_ELEMENT_CE_INPUT_TRANSLATIONS))
               {
                  PSAppTransformer.transformConditionalExits(ce
                        .getInputTranslations(), mapping, idMap);
               }
               else if (element
                     .equals(IPSDeployConstants.ID_TYPE_ELEMENT_CE_OUTPUT_TRANSLATIONS))
               {
                  PSAppTransformer.transformConditionalExits(ce
                        .getOutputTranslations(), mapping, idMap);
               }
               else if (element
                     .equals(IPSDeployConstants.ID_TYPE_ELEMENT_CE_SECTION_LINK_LIST))
               {
                  Iterator links = ce.getSectionLinkList();
                  while (links.hasNext())
                  {
                     PSAppTransformer.transformUrlRequest((PSUrlRequest) links
                           .next(), mapping, idMap);
                  }
               }
               else if (element
                     .equals(IPSDeployConstants.ID_TYPE_ELEMENT_CE_UI_DEF))
               {
                  PSAppTransformer.transformUIDef(ceMapper.getUIDefinition(),
                        mapping, idMap);
               }
               else if (element
                     .equals(IPSDeployConstants.ID_TYPE_ELEMENT_CE_VALIDATION_RULES))
               {
                  PSAppTransformer.transformConditionalExits(ce
                        .getValidationRules(), mapping, idMap);
               }
            } // mappings.hasNext()
         } // elements.hasNext()
      } // resources.hasNext()
   }

   /**
    * helper method to transform workflow ids
    * 
    * @param item the item defintion never <code>null</code>
    * @param ctx the import context never <code>null</code>
    * @throws PSDeployException
    */
   private void transformWorkflowIds(PSItemDefinition item, PSImportCtx ctx)
         throws PSDeployException
   {
      if (item == null)
         throw new IllegalArgumentException("item definition may not be null");

      if (ctx == null)
         throw new IllegalArgumentException("import context may not be null");

      PSContentEditor ce = item.getContentEditor();
      PSIdMap idMap = ctx.getCurrentIdMap();

      if (idMap == null)
         return;

      String wfType = PSWorkflowDependencyHandler.DEPENDENCY_TYPE;
      int wfId = ce.getWorkflowId();
      ce.setWorkflowId(getNewIdInt(ctx, String.valueOf(wfId), wfType));

      PSWorkflowInfo wfInfo = ce.getWorkflowInfo();
      if (wfInfo != null)
      {
         List<Integer> newIds = new ArrayList<>();
         Iterator ids = wfInfo.getValues();
         while (ids.hasNext())
         {
            Integer oldId = (Integer) ids.next();
            newIds.add(new Integer(getNewIdInt(ctx, oldId.toString(), wfType)));
         }
         wfInfo.setValues(newIds);
      }
   }

   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = 
      IPSDeployConstants.DEP_OBJECT_TYPE_CONTENT_TYPE;


   /**
    * logger 
    */
   private static final Logger log = LogManager.getLogger(PSContentTypeDependencyHandler.class);

   
   /**
    * List of child types supported by this handler, never <code>null</code>
    * or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<>();

   static
   {
      ms_childTypes.add(PSApplicationDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSAclDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSTemplateDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSImageFileDependencyHandler.DEPENDENCY_TYPE);
   }
}
