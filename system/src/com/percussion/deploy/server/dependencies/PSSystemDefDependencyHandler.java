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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import com.percussion.deploy.client.IPSDeployConstants;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deploy.objectstore.PSApplicationIDTypes;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDependencyFile;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.objectstore.PSTransactionSummary;
import com.percussion.deploy.objectstore.idtypes.PSAppNamedItemIdContext;
import com.percussion.deploy.objectstore.idtypes.PSApplicationIdContext;
import com.percussion.deploy.server.IPSIdTypeHandler;
import com.percussion.deploy.server.PSAppTransformer;
import com.percussion.deploy.server.PSArchiveHandler;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.deploy.server.PSImportCtx;
import com.percussion.design.objectstore.PSApplicationFlow;
import com.percussion.design.objectstore.PSCommandHandlerStylesheets;
import com.percussion.design.objectstore.PSContainerLocator;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSServer;

/**
 * Class to handle packaging and deploying a system def
 */
public class PSSystemDefDependencyHandler 
   extends PSContentEditorObjectDependencyHandler implements IPSIdTypeHandler
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
   public PSSystemDefDependencyHandler(PSDependencyDef def, 
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }
   
   
   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Application</li>
    * <li>Keyword</li>
    * <li>Control</li>
    * <li>ControlFile</li>
    * <li>TableSchema</li>
    * <li>Exit</li>
    * <li>Any ID Type</li> 
    * </ol>
    * 
    * @return An iterator over zero or more types as <code>String</code> 
    * objects, never <code>null</code>, does not contain <code>null</code> or 
    * empty entries.
    */
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   
   // see base class
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      // get all shared groups
      List<PSDependency> deps = new ArrayList<PSDependency>();
      PSDependency dep = getDependency(tok, m_def.getObjectType());
      if (dep != null)
         deps.add(dep);         
         
      return deps.iterator();      
   }
   
   // see base class
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }
   
   // see base class
   public boolean doesDependencyExist(PSSecurityToken tok, String id) 
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      
      return (PSServer.getContentEditorSystemDef() != null);
   }
   
   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id) 
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
         
      PSDependency dep = null;
      
      if (id.equals(m_def.getObjectType()) && doesDependencyExist(tok, id))
         dep = createDependency(m_def, id, m_def.getObjectTypeName());
      
      return dep;
   }
   
   // see base class
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep) 
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
         
      // use set to ensure we don't add dupes
      Set<PSDependency> childDeps = new HashSet<PSDependency>();
      
      // get dependencies specified by id type map
      childDeps.addAll(getIdTypeDependencies(tok, dep));

      PSContentEditorSystemDef def = getSystemDef();
      PSUIDefinition uiDef = def.getUIDefinition();
      childDeps.addAll(checkUIDef(tok, uiDef));
      
      childDeps.addAll(checkLocatorTables(tok, def.getSystemLocator()));
      PSContainerLocator loc = def.getContainerLocator();
      if (loc != null)
         childDeps.addAll(checkLocatorTables(tok, loc));
      
      Document doc = def.toXml();
      addApplicationDependencies(tok, childDeps, doc.getDocumentElement());
      
      return childDeps.iterator();      
   }
   
   // see base class
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
         
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      
      List<PSDependencyFile> files = new ArrayList<PSDependencyFile>();         
      if (doesDependencyExist(tok, dep.getDependencyId()))
      {
         PSContentEditorSystemDef def = getSystemDef();
         File defFile = createXmlFile(def.toXml());
         files.add(new PSDependencyFile(PSDependencyFile.TYPE_SYSTEM_DEF_XML, 
            defFile));
      }
      
      return files.iterator();
   }

   // see base class
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
      
      Document doc = null;
      Iterator files = archive.getFiles(dep);
      while (files.hasNext() && doc == null)
      {
         PSDependencyFile file = (PSDependencyFile)files.next();
         if (file.getType() == PSDependencyFile.TYPE_SYSTEM_DEF_XML)
         {
            doc = createXmlDocument(archive.getFileData(file));
         }
      }
      
      // must at have the doc    
      if (doc == null)
      {
         Object[] args = 
         {
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_SYSTEM_DEF_XML], 
            dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName()
         };
         throw new PSDeployException(
            IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }
            
      // restore the system def
      PSContentEditorSystemDef sysDef;
      try
      {
         sysDef = new PSContentEditorSystemDef(doc);
      }
      catch (Exception e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
            
      // transform ids and dbms's in the def if necessary
      PSIdMap idMap = ctx.getCurrentIdMap();
      if (idMap != null)
         transformDef(ctx, sysDef);
      
      int transAction = PSTransactionSummary.ACTION_MODIFIED;
      
      try 
      {
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         os.saveContentEditorSystemDef(sysDef);

         // update log
         addTransactionLogEntry(dep, ctx, dep.getDisplayName(), 
            PSTransactionSummary.TYPE_FILE, transAction);
      }
      catch (Exception e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
   }
   
   // see IPSIdTypeHandler interface
   public PSApplicationIDTypes getIdTypes(PSSecurityToken tok, PSDependency dep) 
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
         
      PSApplicationIDTypes idTypes = new PSApplicationIDTypes(dep);
      try
      {
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         PSContentEditorSystemDef sysDef = os.getContentEditorSystemDef();
         List mappings = new ArrayList();
         String resource = dep.getObjectType();
         mappings.clear();
         PSAppTransformer.checkAppFlow(mappings, sysDef.getApplicationFlow(), 
            null);
         idTypes.addMappings(resource, 
            IPSDeployConstants.ID_TYPE_ELEMENT_CE_APP_FLOW, 
            mappings.iterator());
            
         mappings.clear();
         PSAppTransformer.checkFieldSet(mappings, sysDef.getFieldSet(), null);
         idTypes.addMappings(resource, 
            IPSDeployConstants.ID_TYPE_ELEMENT_CE_FIELD, mappings.iterator());
   
         mappings.clear();
         PSAppTransformer.checkUIDef(mappings, sysDef.getUIDefinition(), null);         
         idTypes.addMappings(resource, 
            IPSDeployConstants.ID_TYPE_ELEMENT_CE_UI_DEF, mappings.iterator());
            
         mappings.clear();
         PSAppTransformer.checkConditionalExits(mappings, 
            sysDef.getInputTranslations(), null);
         idTypes.addMappings(resource, 
            IPSDeployConstants.ID_TYPE_ELEMENT_CE_INPUT_TRANSLATIONS, 
            mappings.iterator());
         
         mappings.clear();
         PSAppTransformer.checkConditionalExits(mappings, 
            sysDef.getOutputTranslations(), null);
         idTypes.addMappings(resource, 
            IPSDeployConstants.ID_TYPE_ELEMENT_CE_OUTPUT_TRANSLATIONS, 
            mappings.iterator());
         
         mappings.clear();
         PSAppTransformer.checkConditionalExits(mappings, 
            sysDef.getValidationRules(), null);
         idTypes.addMappings(resource, 
            IPSDeployConstants.ID_TYPE_ELEMENT_CE_VALIDATION_RULES, 
            mappings.iterator());
                  
         mappings.clear();            
         Iterator links = sysDef.getSectionLinkList();  
         while (links.hasNext())
         {
            PSAppTransformer.checkUrlRequest(mappings, 
               (PSUrlRequest)links.next(), null);
         }
         idTypes.addMappings(resource, 
            IPSDeployConstants.ID_TYPE_ELEMENT_CE_SECTION_LINK_LIST, 
            mappings.iterator());
         
         
         mappings.clear();
         PSAppTransformer.checkStylesheetSet(mappings, 
            sysDef.getStyleSheetSet(), null);
         idTypes.addMappings(resource, 
            IPSDeployConstants.ID_TYPE_ELEMENT_CE_COMMAND_HANDLER_STYLESHEETS, 
            mappings.iterator());
         
         mappings.clear();
         Iterator cmds = sysDef.getInputDataExitCommands();
         while (cmds.hasNext())
         {
            String cmd = (String)cmds.next();
            PSAppNamedItemIdContext cmdCtx = new PSAppNamedItemIdContext(
               PSAppNamedItemIdContext.TYPE_SYS_DEF_INPUT_DATA_EXITS, cmd);
            PSAppTransformer.checkExtensionCalls(mappings, 
               sysDef.getInputDataExits(cmd), cmdCtx);
         }
         idTypes.addMappings(resource, 
            IPSDeployConstants.ID_TYPE_ELEMENT_INPUT_DATA_EXITS, 
            mappings.iterator());
         
         
         mappings.clear();
         cmds = sysDef.getResultDataExitCommands();
         while (cmds.hasNext())
         {
            String cmd = (String)cmds.next();
            PSAppNamedItemIdContext cmdCtx = new PSAppNamedItemIdContext(
               PSAppNamedItemIdContext.TYPE_SYS_DEF_RESULT_DATA_EXITS, cmd);
            PSAppTransformer.checkExtensionCalls(mappings, 
               sysDef.getResultDataExits(cmd), cmdCtx);
         }
         idTypes.addMappings(resource, 
            IPSDeployConstants.ID_TYPE_ELEMENT_RESULT_DATA_EXITS, 
               mappings.iterator());
         
         mappings.clear();
         Map initParams = sysDef.getInitParams();
         Iterator entries = initParams.entrySet().iterator();
         while (entries.hasNext())
         {
            Map.Entry entry = (Map.Entry)entries.next();
            String cmd = (String)entry.getKey();
            PSAppNamedItemIdContext cmdCtx = new PSAppNamedItemIdContext(
               PSAppNamedItemIdContext.TYPE_SYS_DEF_INIT_PARAMS, cmd);
                
            Iterator params = ((List)entry.getValue()).iterator();
            while (params.hasNext())
            {
               PSAppTransformer.checkParam(mappings, (PSParam)params.next(), 
                  cmdCtx);
            }
         }
         idTypes.addMappings(resource, 
            IPSDeployConstants.ID_TYPE_ELEMENT_INIT_PARAMS, 
            mappings.iterator());
            
         
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      
      return idTypes;
   }
   
   // see IPSIdTypeHandler interface
   public void transformIds(Object object, PSApplicationIDTypes idTypes, 
      PSIdMap idMap) throws PSDeployException
   {
      if (object == null)
         throw new IllegalArgumentException("object may not be null");
      
      if (idTypes == null)
         throw new IllegalArgumentException("idTypes may not be null");
      
      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      if (!(object instanceof PSContentEditorSystemDef))
         throw new IllegalArgumentException("invalid object type");
      
      PSContentEditorSystemDef sysDef = (PSContentEditorSystemDef)object;
      
      // walk id types and perform any transforms
      String id = IPSDeployConstants.DEP_OBJECT_TYPE_SYSTEM_DEF;
      Iterator resources = idTypes.getResourceList(false);
      while (resources.hasNext())
      {
         String resource = (String)resources.next();
         if (!id.equals(resource))
            continue;
            
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
               
               if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_CE_APP_FLOW))
               {
                  PSApplicationFlow appFlow = sysDef.getApplicationFlow();
                  if (appFlow != null)
                     PSAppTransformer.transformAppFlow(appFlow, mapping, idMap);
               }
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_CE_FIELD))
               {
                  PSFieldSet fs = sysDef.getFieldSet();
                  if (fs != null)
                     PSAppTransformer.transformFieldSet(fs, mapping, idMap);
               }
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_CE_UI_DEF))
               {
                  PSUIDefinition uiDef = sysDef.getUIDefinition();
                  if (uiDef != null)
                     PSAppTransformer.transformUIDef(uiDef, mapping, idMap);
               }
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_CE_INPUT_TRANSLATIONS))
               {
                  PSAppTransformer.transformConditionalExits(
                     sysDef.getInputTranslations(), mapping, idMap);
               }
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_CE_OUTPUT_TRANSLATIONS))
               {
                  PSAppTransformer.transformConditionalExits(
                     sysDef.getOutputTranslations(), mapping, idMap);
               } 
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_CE_VALIDATION_RULES))
               {
                  PSAppTransformer.transformConditionalExits(
                     sysDef.getValidationRules(), mapping, idMap);
               } 
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_CE_SECTION_LINK_LIST))
               {
                  Iterator links = sysDef.getSectionLinkList();  
                  while (links.hasNext())
                  {
                     PSAppTransformer.transformUrlRequest(
                        (PSUrlRequest)links.next(), mapping, idMap);
                  }
               }
               else if (element.equals(IPSDeployConstants.
                  ID_TYPE_ELEMENT_CE_COMMAND_HANDLER_STYLESHEETS))
               {
                  PSCommandHandlerStylesheets sheets = 
                     sysDef.getStyleSheetSet();
                  if (sheets != null)
                     PSAppTransformer.transformStylesheetSet(sheets, mapping, 
                        idMap);
               }
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_INPUT_DATA_EXITS))
               {
                  Iterator cmds = sysDef.getInputDataExitCommands();
                  while (cmds.hasNext())
                  {
                     PSAppTransformer.transformExtensionCalls(
                        sysDef.getInputDataExits(cmds.next().toString()), 
                        mapping, idMap);
                  }
               }
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_RESULT_DATA_EXITS))
               {
                  Iterator cmds = sysDef.getResultDataExitCommands();
                  while (cmds.hasNext())
                  {
                     PSAppTransformer.transformExtensionCalls(
                        sysDef.getResultDataExits(cmds.next().toString()), 
                        mapping, idMap);
                  }
               }
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_INIT_PARAMS))
               {
                  PSApplicationIdContext ctx = mapping.getContext();
                  PSApplicationIdContext root = ctx.getCurrentRootCtx();
                  if (!(root instanceof PSAppNamedItemIdContext))
                     continue;
                  PSAppNamedItemIdContext paramCtx = 
                     (PSAppNamedItemIdContext)root;
                  if (paramCtx.getType() != 
                     PSAppNamedItemIdContext.TYPE_SYS_DEF_INIT_PARAMS)
                  {
                     continue;
                  }
                  Map initParams = sysDef.getInitParams();
                  List paramList = (List)initParams.get(
                     paramCtx.getName());
                  if (paramList == null)
                     continue;
                  Iterator params = paramList.iterator();
                  while (params.hasNext()) 
                  {
                     PSAppTransformer.transformParam(
                        (PSParam)params.next(), mapping, idMap);
                  }
               }
            }
         }
      }
      
   }
   
   /**
    * Transform all required id's within the def.
    * 
    * @param ctx The current import context, assumed not <code>null</code> and
    * to have a current Id Map.
    * @param def The def to tranform, assumed not <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private void transformDef(PSImportCtx ctx, PSContentEditorSystemDef def)
      throws PSDeployException
   {
      // transform ui def
      transformUIDef(ctx.getCurrentIdMap(), def.getUIDefinition());

      // transform idTypes
      transformIds(def, ctx.getIdTypes(), ctx.getCurrentIdMap());
   }
   
   /**
    * Get the system def.
    * 
    * @return The def, never <code>null</code>
    * 
    * @throws PSDeployException If the def cannot be loaded.
    */
   private PSContentEditorSystemDef getSystemDef() throws PSDeployException
   {
      PSContentEditorSystemDef def = PSServer.getContentEditorSystemDef();
      if (def == null)
      {
         Object[] args = {m_def.getObjectType(), m_def.getObjectType(), 
               m_def.getObjectTypeName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND, 
            args);
      }      
      
      return def;
   }
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "SystemDef";
   
   /**
    * List of child types supported by this handler, never <code>null</code> or
    * empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();
   
   static
   {
      ms_childTypes.add(PSApplicationDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSControlDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSKeywordDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSchemaDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSStylesheetDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSupportFileDependencyHandler.DEPENDENCY_TYPE);
   }
   
}
