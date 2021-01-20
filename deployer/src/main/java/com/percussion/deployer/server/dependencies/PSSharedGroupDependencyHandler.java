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
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.design.objectstore.PSContainerLocator;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSSharedFieldGroup;
import com.percussion.design.objectstore.PSUIDefinition;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.security.PSSecurityToken;
import com.percussion.util.PSCollection;
import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Class to handle packaging and deploying a shared group
 */
public class PSSharedGroupDependencyHandler 
   extends PSContentEditorObjectDependencyHandler
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
   public PSSharedGroupDependencyHandler(PSDependencyDef def, 
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }
   
   
   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Application</li>
    * <li>Control</li>
    * <li>Exit</li>
    * <li>Keyword</li>
    * <li>TableSchema</li>
    * <li>Stylesheet</li>
    * <li>SupportFile</li>
    * <li>Any ID Type</li> 
    * </ol>
    * 
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
      PSContentEditorSharedDef sharedDef = PSDependencyUtils.getSharedDef();
      Iterator groups = sharedDef.getFieldGroups();      
      while (groups.hasNext()) 
      {
         PSSharedFieldGroup group = (PSSharedFieldGroup)groups.next();
         deps.add(createDependency(m_def, group.getName(), group.getName()));
      }
      
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
      PSSharedFieldGroup group = getSharedGroup(id);
      if (group != null)
         dep = createDependency(m_def, group.getName(), group.getName());
      
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
         
      PSSharedFieldGroup group = getSharedGroup(dep.getDependencyId());
      if (group == null)
      {
         Object[] args = {dep.getObjectTypeName(), dep.getDependencyId(), 
               dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND, 
            args);
      }
      
      // use set to ensure we don't add dupes
      Set<PSDependency> childDeps = new HashSet<PSDependency>();
      
      // get dependencies specified by id type map
      childDeps.addAll(getIdTypeDependencies(tok, dep));

      childDeps.addAll(checkLocatorTables(tok, group.getLocator()));
      PSUIDefinition uiDef = group.getUIDefinition();
      childDeps.addAll(checkUIDef(tok, uiDef));
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      addApplicationDependencies(tok, childDeps, group.toXml(doc));
      
      return childDeps.iterator();      
   }
   
   /**
    * Get dependencies for all tables from the supplied container locator
    *
    * @param tok The security token to use, may not be <code>null</code>.
    * @param locator The locator to check, may not be <code>null</code>.
    *
    * @return list of dependencies, never <code>null</code>, may be empty.
    *
    * @throws PSDeployException if there are any errors.
    */
   @SuppressWarnings("unchecked")
   private List<PSDependency> checkLocatorTables(PSSecurityToken tok,
      PSContainerLocator locator)
        throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      PSDependencyHandler schemaHandler = getDependencyHandler(
         PSSchemaDependencyHandler.DEPENDENCY_TYPE);

      List<PSDependency> childDeps = new ArrayList<PSDependency>();
      for (String tableName : PSDependencyUtils.getLocatorTables(locator))
      {
         PSDependency schemaDep =
            schemaHandler.getDependency(tok, tableName);
         if (schemaDep != null)
         {
            childDeps.add(schemaDep);
         }
      }

      return childDeps;
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
      
      // Before getting file list, check the content type for System controls. 
      // These are not allowed to be packaged.
      checkServerControls(tok, dep);
      
      // Build list of dependency files
      List<PSDependencyFile> files = new ArrayList<PSDependencyFile>();         
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      
      // get group and figure out which file it's from
      PSSharedFieldGroup group = null;
      File grpFile = null;
      File[] grpFiles = os.getContentEditorSharedDefFiles();
      if (grpFiles != null)
      {
         for (int i = 0; i < grpFiles.length; i++) 
         {
            PSContentEditorSharedDef def;
            try 
            {
               def = os.getContentEditorSharedDef(grpFiles[i].getName());
            }
            catch (Exception e) 
            {
               throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
                  e.getLocalizedMessage());
            }
            
            if ((group = def.getSharedGroup(dep.getDependencyId())) != null)
            {
               grpFile = grpFiles[i];
               break;
            }
         }
      }

      if (group == null)
      {
         Object[] args = {dep.getObjectTypeName(), dep.getDependencyId(), 
               dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND, 
            args);
      }
      
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.replaceRoot(doc, group.toXml(doc));
      File groupDocFile = createXmlFile(doc);
      files.add(new PSDependencyFile(PSDependencyFile.TYPE_SHARED_GROUP_XML, 
         groupDocFile, grpFile));
            
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
      
      String groupName = dep.getDependencyId();
      Document doc = null;
      Iterator files = archive.getFiles(dep);
      File origFile = null;
      while (files.hasNext() && doc == null)
      {
         PSDependencyFile file = (PSDependencyFile)files.next();
         if (file.getType() == PSDependencyFile.TYPE_SHARED_GROUP_XML)
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
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_SHARED_GROUP_XML], 
            dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName()
         };
         throw new PSDeployException(
            IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }
            
      PSSharedFieldGroup group = null;
      try 
      {
         group = new PSSharedFieldGroup(doc.getDocumentElement(), null, null);
      }
      catch (PSUnknownNodeTypeException e) 
      {
         Object[] args = 
         {
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_SHARED_GROUP_XML], 
            dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName(), 
            e.getLocalizedMessage()
         };
         throw new PSDeployException(
            IPSDeploymentErrors.INVALID_DEPENDENCY_FILE, args);
      }
      
      // transform ids and dbms's in the group if necessary
      PSIdMap idMap = ctx.getCurrentIdMap();
      if (idMap != null)
         transformGroup(ctx, group);
      
      int transAction = PSTransactionSummary.ACTION_MODIFIED;
      
      // get the shared def and file to add/replace the group 
      try 
      {
         // look for original file
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         File[] defFiles = os.getContentEditorSharedDefFiles();
         File defFile = null;
         PSContentEditorSharedDef tgtDef = null;
         File matchFile = null;
         PSContentEditorSharedDef matchDef = null;
         
         boolean sharedDefExists = true;
         if (defFiles != null && defFiles.length > 0)
         {
            for (int i = 0; i < defFiles.length; i++) 
            {
               if (defFiles[i].getName().equals(origFile.getName()))
               {
                  PSContentEditorSharedDef def = os.getContentEditorSharedDef(
                     defFiles[i].getName());
                  matchFile = defFiles[i];
                  matchDef = def;
                  if (def.getSharedGroup(groupName) != null)
                  {
                     defFile = defFiles[i];
                     tgtDef = def;
                  }
                  break;
               }
            }
         }
         else
            sharedDefExists = false;
         
         if (defFile == null && sharedDefExists)
         {
            // not in the original file, need to see if in a different one
            if (getSharedGroup(groupName) != null)
            {
               for (int i = 0; i < defFiles.length; i++) 
               {
                  PSContentEditorSharedDef def = os.getContentEditorSharedDef(
                     defFiles[i].getName());
                  if (def.getSharedGroup(groupName) != null)
                  {
                     defFile = defFiles[i];
                     tgtDef = def;
                     break;
                  }
               }
            }
         }
         
         PSCollection defCol;
         if (defFile == null)
         {
            // group not found in any existing file, add to original file if 
            // found, or else create one
            if (matchFile == null)
            {
               // create one using original file name
               defCol = new PSCollection(PSIteratorUtils.iterator(group));
               tgtDef = new PSContentEditorSharedDef(defCol);
               defFile = origFile;
               transAction = PSTransactionSummary.ACTION_CREATED;
            }
            else
            {
               defFile = matchFile;
               tgtDef = matchDef;
               defCol = new PSCollection(tgtDef.getFieldGroups());
               defCol.add(group);
            }
         }
         else
         {
            // we have the file with the existing group
            defCol = new PSCollection(tgtDef.getFieldGroups());
            for (int i = 0; i < defCol.size(); i++) 
            {
               PSSharedFieldGroup grp = (PSSharedFieldGroup)defCol.get(i);
               if (group.getName().equals(grp.getName()))
               {
                  defCol.remove(i);
                  break;
               }
            }
            defCol.add(group);
         }
         
         tgtDef.setFieldGroups(defCol);
         os.saveContentEditorSharedDefFile(tgtDef, defFile.getName());

         // update log
         addTransactionLogEntry(dep, ctx, defFile.getName(), 
            PSTransactionSummary.TYPE_FILE, transAction);
      }
      catch (Exception e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.toString());
      }
   }

   /**
    * Get the specified shared group.
    * 
    * @param groupName the name of the group, assumed not <code>null</code> or 
    * empty.
    * 
    * @return The group, may be <code>null</code> if not found.
    * 
    * @throws PSDeployException if the shared def cannot be loaded.
    */
   private PSSharedFieldGroup getSharedGroup(String groupName) 
      throws PSDeployException 
   {
      PSContentEditorSharedDef sharedDef = PSDependencyUtils.getSharedDef();
      return sharedDef.getSharedGroup(groupName);
   }
   
   /**
    * Transform all required id's within the group.
    * 
    * @param ctx The current import context, assumed not <code>null</code> and
    * to have a current Id Map.
    * @param group The group to tranform, assumed not <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private void transformGroup(PSImportCtx ctx, PSSharedFieldGroup group)
      throws PSDeployException
   {
      // transform ui def
      transformUIDef(ctx.getCurrentIdMap(), group.getUIDefinition());
   }
   
   /**
    * Constant for this handler's supported type
    */
   public final static String DEPENDENCY_TYPE = "SharedGroup";
   
   /**
    * List of child types supported by this handler, never <code>null</code> or
    * empty.
    */
   private static List ms_childTypes = new ArrayList();
   
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
