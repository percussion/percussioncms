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

import com.percussion.deploy.client.IPSDeployConstants;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deploy.objectstore.PSApplicationIDTypes;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDependencyFile;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.objectstore.PSTransactionSummary;
import com.percussion.deploy.server.IPSIdTypeHandler;
import com.percussion.deploy.server.PSAppTransformer;
import com.percussion.deploy.server.PSArchiveHandler;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.deploy.server.PSImportCtx;
import com.percussion.design.objectstore.PSApplicationFlow;
import com.percussion.design.objectstore.PSCommandHandlerStylesheets;
import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.security.PSSecurityToken;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class to handle packaging and deploying a system def override from a shared 
 * def
 */
public class PSSystemDefElementDependencyHandler 
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
   public PSSystemDefElementDependencyHandler(PSDependencyDef def, 
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }
   
   
   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Application</li>
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
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      // get all shared groups
      List deps = new ArrayList();
      PSDependency dep;
      
      dep = getDependency(tok, APP_FLOW_ID);
      if (dep != null)
         deps.add(dep);
                  
      dep = getDependency(tok, CMD_SHEETS_ID);
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
      
      return getDependency(tok, id) != null;
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
      PSContentEditorSharedDef sharedDef = getSharedDef();

      if (id.equals(APP_FLOW_ID) && sharedDef.getApplicationFlow() != null) 
         dep = createDependency(m_def, APP_FLOW_ID, APP_FLOW_NAME);
      else if (id.equals(CMD_SHEETS_ID) && sharedDef.getStylesheetSet() != null)
         dep = createDependency(m_def, CMD_SHEETS_ID, CMD_SHEETS_NAME);
      
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
      Set childDeps = new HashSet();
      
      // get dependencies specified by id type map
      childDeps.addAll(getIdTypeDependencies(tok, dep));

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSContentEditorSharedDef sharedDef = getSharedDef();
      PSComponent comp = getDepComponent(dep, sharedDef);
      if (comp != null)
         addApplicationDependencies(tok, childDeps, comp.toXml(doc));
      
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
      
      List files = new ArrayList();         
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      
      // figure out which file it's from and get it
      String id = dep.getDependencyId();
      Element srcEl = null;
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      
      File defFile = null;
      File[] defFiles = os.getContentEditorSharedDefFiles();
      if (defFiles != null)
      {
         for (int i = 0; i < defFiles.length; i++) 
         {
            PSContentEditorSharedDef def;
            try 
            {
               def = os.getContentEditorSharedDef(defFiles[i].getName());
            }
            catch (Exception e) 
            {
               throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
                  e.getLocalizedMessage());
            }
            
            if (id.equals(APP_FLOW_ID) && def.getApplicationFlow() != null) 
            {
               srcEl = def.getApplicationFlow().toXml(doc);
               defFile = defFiles[i];
               break;
            }
            else if (id.equals(CMD_SHEETS_ID) && def.getStylesheetSet() != null)
            {
               srcEl = def.getStylesheetSet().toXml(doc);
               defFile = defFiles[i];
               break;
            }
         }
      }

      if (srcEl == null)
      {
         Object[] args = {dep.getObjectTypeName(), dep.getDependencyId(), 
               dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND, 
            args);
      }
      
      
      PSXmlDocumentBuilder.replaceRoot(doc, srcEl);
      File docFile = createXmlFile(doc);
      files.add(new PSDependencyFile(PSDependencyFile.TYPE_SHARED_GROUP_XML, 
         docFile, defFile));
            
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
      
      PSContentEditorSharedDef sharedDef = getSharedDef();
      String id = dep.getDependencyId();
      Document doc = null;
      Iterator files = archive.getFiles(dep);
      File origFile = null;
      while (files.hasNext() && doc == null)
      {
         PSDependencyFile file = (PSDependencyFile)files.next();
         if (file.getType() == PSDependencyFile.TYPE_SHARED_SYSTEM_OVERRIDE_XML)
         {
            doc = createXmlDocument(archive.getFileData(file));
            origFile = file.getOriginalFile();
         }
      }
      
      // must at have the doc    
      if (doc == null)
      {
         Object[] args = 
         {
            PSDependencyFile.TYPE_ENUM[
               PSDependencyFile.TYPE_SHARED_SYSTEM_OVERRIDE_XML], 
            dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName()
         };
         throw new PSDeployException(
            IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }
            
      PSApplicationFlow appFlow = null;
      PSCommandHandlerStylesheets styleSheets = null;
      PSComponent comp = getDepComponent(dep, doc.getDocumentElement());
      
      // transform ids if necessary
      PSIdMap idMap = ctx.getCurrentIdMap();
      if (idMap != null)
      {
         // transform idTypes
         transformIds(comp, ctx.getIdTypes(), ctx.getCurrentIdMap());
      }
      
      int transAction = PSTransactionSummary.ACTION_MODIFIED;
      
      // get the shared def and file to add/replace the group 
      try 
      {
         // deploy into first file with the component specified
         // if none, use original file
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         File[] defFiles = os.getContentEditorSharedDefFiles();
         File defFile = null;
         File tgtFile = null;
         PSContentEditorSharedDef origDef = null;
         PSContentEditorSharedDef tgtDef = null;
         boolean sharedDefExists = true;
         if (defFiles == null || defFiles.length == 0)
         {
            // Content editor apps won't target with no shared def, and
            // should have been caught by loading shared def above
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               "no target shared def file");
         }
         
         // see if original file exists.  Also if find a file that already 
         // contains the same override, set that as the target
         for (int i = 0; i < defFiles.length; i++) 
         {
            if (defFiles[i].getName().equals(origFile.getName()))
            {
               defFile = defFiles[i];
               origDef = os.getContentEditorSharedDef(defFile.getName());
            }
            
            if (tgtDef == null)
            {
               PSContentEditorSharedDef def = os.getContentEditorSharedDef(
                  defFiles[i].getName());
               if (getDepComponent(dep, def) != null)
               {
                  tgtFile = defFiles[i];
                  tgtDef = def;
                  break;  // we'll use this regardless of whether original found
               }
            }
         }
         
         
         // if found target def, use that, else use orig file if found. If orig 
         // file not found, use first.
         if (tgtDef == null && origDef != null)
         {
            tgtDef = origDef;
            tgtFile = defFile;
         }
         else if (tgtDef == null)
         {
            tgtFile = defFiles[0];
            tgtDef = os.getContentEditorSharedDef(tgtFile.getName());
         }
         
         if (comp instanceof PSApplicationFlow)         
            tgtDef.setApplicationFlow((PSApplicationFlow)comp);
         else
            tgtDef.setStylesheetSet((PSCommandHandlerStylesheets)comp);
         os.saveContentEditorSharedDefFile(tgtDef, tgtFile.getName());

         // update log
         addTransactionLogEntry(dep, ctx, tgtFile.getName(), 
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
         PSContentEditorSharedDef sharedDef = os.getContentEditorSharedDef();
         
         List mappings = new ArrayList();
         PSApplicationFlow appFlow = sharedDef.getApplicationFlow();
         if (appFlow != null)
         {
            mappings.clear();
            PSAppTransformer.checkAppFlow(mappings, appFlow, null);
            idTypes.addMappings(dep.getObjectTypeName(), 
               IPSDeployConstants.ID_TYPE_ELEMENT_CE_APP_FLOW, 
               mappings.iterator());
         }
         
         PSCommandHandlerStylesheets styleSheets = sharedDef.getStylesheetSet();
         if (styleSheets != null)
         {
            mappings.clear();
            PSAppTransformer.checkStylesheetSet(mappings, styleSheets, null);
            idTypes.addMappings(dep.getObjectTypeName(), IPSDeployConstants.
               ID_TYPE_ELEMENT_CE_COMMAND_HANDLER_STYLESHEETS,
               mappings.iterator());
         }
         
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

      if (!(object instanceof PSApplicationFlow || 
         object instanceof PSCommandHandlerStylesheets))
      {
         throw new IllegalArgumentException("invalid object type");
      }
      
      // walk id types and perform any transforms
      String id = IPSDeployConstants.DEP_OBJECT_TYPE_SYSTEM_DEF_ELEMENT;
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
                  IPSDeployConstants.ID_TYPE_ELEMENT_CE_APP_FLOW) && 
                     object instanceof PSApplicationFlow)
               {
                  PSAppTransformer.transformAppFlow((PSApplicationFlow)object, 
                     mapping, idMap);
               }
               else if (element.equals(IPSDeployConstants.
                  ID_TYPE_ELEMENT_CE_COMMAND_HANDLER_STYLESHEETS) && 
                     object instanceof PSCommandHandlerStylesheets)
               {
                  PSAppTransformer.transformStylesheetSet(
                     (PSCommandHandlerStylesheets)object, mapping, idMap);
               }
            }
         }
      }
      
   }
   
   /**
    * Gets the component from the supplied shared def represented by the 
    * dependency.
    * 
    * @param dep The dependency, assumed not <code>null</code>.
    * @param def The def, assumed not <code>null</code>.
    * 
    * @return The component, or <code>null</code> if not found.
    */   
   private PSComponent getDepComponent(PSDependency dep, 
      PSContentEditorSharedDef def)
   {
      PSComponent comp = null;
      
      if (dep.getDependencyId().equals(APP_FLOW_ID))
         comp = def.getApplicationFlow();
      else
         comp = def.getStylesheetSet();
      
      return comp;
      
   }  
   
   /**
    * Restores the component specified by the supplied dependency from its 
    * source XML element.
    * 
    * @param dep The dependency that represents the component to restore, 
    * assumed not <code>null</code>.
    * @param src The source element, assumed not <code>null</code>.
    * 
    * @return The component, never <code>null</code>.
    * 
    * @throws PSDeployException if the component cannot be restored from the 
    * supplied <code>src</code>.
    */
   private PSComponent getDepComponent(PSDependency dep, Element src) 
      throws PSDeployException
   {
      PSComponent comp = null;
      try 
      {
         if (dep.getDependencyId().equals(APP_FLOW_ID))
            comp = new PSApplicationFlow(src, null, null);
         else
            comp = new PSCommandHandlerStylesheets(src, null, null);
      }
      catch (PSUnknownNodeTypeException e) 
      {
         Object[] args = 
         {
            PSDependencyFile.TYPE_ENUM[
               PSDependencyFile.TYPE_SHARED_SYSTEM_OVERRIDE_XML], 
            dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName(), 
            e.getLocalizedMessage()
         };
         throw new PSDeployException(
            IPSDeploymentErrors.INVALID_DEPENDENCY_FILE, args);
      }
      
      return comp;
   }  
   
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "SystemDefElement";
 
   /**
    * Constant for the dependency id of an application flow.
    */
   static final String APP_FLOW_ID = "ApplicationFlow";
   
   /**
    * Constant for the display name of an application flow dependency.
    */
   private static final String APP_FLOW_NAME = "Application Flow";
   
   /**
    * Constant for the dependency id of an command handler stylesheets.
    */
   static final String CMD_SHEETS_ID = "CommandHandlerStylesheets";
   
   /**
    * Constant for the display name of a command handler stylesheets dependency.
    */
   private static final String CMD_SHEETS_NAME = "Command Handler Stylesheets";
   
   /**
    * List of child types supported by this handler, never <code>null</code> or
    * empty.
    */
   private static List ms_childTypes = new ArrayList();
   
   static
   {
      ms_childTypes.add(PSApplicationDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
   }
   
}
