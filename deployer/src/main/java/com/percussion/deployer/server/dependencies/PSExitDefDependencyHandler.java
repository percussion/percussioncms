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
 
package com.percussion.deployer.server.dependencies;

import com.percussion.content.PSMimeContentAdapter;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSDeployableObject;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.error.PSNotFoundException;
import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionManager;
import com.percussion.extension.PSExtensionDefFactory;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSServer;
import com.percussion.utils.collections.PSIteratorUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class to handle packaging and deploying an exit defintion.
 */
public class PSExitDefDependencyHandler extends PSDependencyHandler
{
   /**
    * Logger for this class
    */
   private static final Logger log = LogManager.getLogger(PSExitDefDependencyHandler.class);


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
   public PSExitDefDependencyHandler(PSDependencyDef def, 
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
      m_extMgr = PSServer.getExtensionManager(null);
   }
   
   
   // see base class
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, com.percussion.services.error.PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      
      Set appDeps = new HashSet<>();
      
      // Get the def
      IPSExtensionDef def;
      try
      {
         def = m_extMgr.getExtensionDef(new PSExtensionRef(
            dep.getDependencyId()));
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      
      // Check for app deps
      PSDependencyHandler handler = getDependencyHandler(
         PSApplicationDependencyHandler.DEPENDENCY_TYPE);
      Iterator apps = def.getRequiredApplications();
      while (apps.hasNext())
      {
         PSDependency appDep = handler.getDependency(tok, (String)apps.next());
         if (appDep != null)
         {
            if (appDep.getDependencyType() == PSDependency.TYPE_SHARED)
            {
               appDep.setIsAssociation(false);
            }
            appDeps.add(appDep);            
         }
      }      
      
      return appDeps.iterator();
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
      
      List fileList = new ArrayList();
      
      try 
      {
         // get the def
         PSExtensionRef ref = new PSExtensionRef(dep.getDependencyId());
         IPSExtensionDef def = m_extMgr.getExtensionDef(ref);
         
         // create xml doc with def
         Document extDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(extDoc, 
            EXTENSIONS_ROOT);
         PSExtensionDefFactory factory = new PSExtensionDefFactory();
         factory.toXml(root, def);
         File defXmlFile = createXmlFile(extDoc);
         
         // add def xml file
         PSDependencyFile defDepFile = new PSDependencyFile(
            PSDependencyFile.TYPE_EXTENSION_DEF_XML, defXmlFile);
         fileList.add(defDepFile);
               
         // add classes, jars etc if stored with extension   
         Iterator files = m_extMgr.getExtensionFiles(ref);
         while (files.hasNext())
         {
            URL url = (URL)files.next();
            if (url.getProtocol().equalsIgnoreCase("FILE"))
            {
               // need to get real path
               File codeBase = m_extMgr.getCodeBase(def);
               File file = new File(codeBase, url.getFile());
               if (!file.exists())
                  continue;
               
               // may be a directory, take what's below.               
               Iterator subFiles;
               if (file.isDirectory())
               {
                  List subList = new ArrayList();
                  catalogFiles(file, subList);
                  subFiles = subList.iterator();
               }
               else
                  subFiles = PSIteratorUtils.iterator(file);
                  
               while (subFiles.hasNext())
               {
                  File subFile = (File)subFiles.next();
                  PSDependencyFile depFile = new PSDependencyFile(
                     PSDependencyFile.TYPE_EXTENSION_RESOURCE, subFile, 
                     stripCodeBase(codeBase, subFile));
                  fileList.add(depFile);
               }
            }
         }
      }
      catch (Exception e) 
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());
      }
      
      return fileList.iterator();
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
      
      // get file data
      Document defDoc = null;
      List resourceFiles = new ArrayList();
      Iterator files = archive.getFiles(dep);
      while (files.hasNext())
      {
         PSDependencyFile file = (PSDependencyFile)files.next();
         
         // process files
         if (file.getType() == PSDependencyFile.TYPE_EXTENSION_DEF_XML)
         {
            defDoc = createXmlDocument(archive.getFileData(file));
         }
         else if (file.getType() == PSDependencyFile.TYPE_EXTENSION_RESOURCE)
         {
            PSMimeContentAdapter content = new PSMimeContentAdapter(
               archive.getFileData(file), null, null, null, -1);
            content.setName(file.getOriginalFile().getPath());
            resourceFiles.add(content);
         }
      }
      
      if (defDoc == null || defDoc.getDocumentElement() == null)
      {
         Object[] args = 
         {
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_EXTENSION_DEF_XML], 
            dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName()
         };
         throw new PSDeployException(
            IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }      
      
      try
      {
         PSExtensionDefFactory factory = new PSExtensionDefFactory();
         IPSExtensionDef def = factory.fromXml(defDoc.getDocumentElement());
         
         int transAction = m_extMgr.exists(def.getRef()) ? 
            PSTransactionSummary.ACTION_MODIFIED : 
            PSTransactionSummary.ACTION_CREATED;
         
         switch (transAction) 
         {
            case PSTransactionSummary.ACTION_CREATED:
               m_extMgr.installExtension(def, resourceFiles.iterator());
               break;
            case PSTransactionSummary.ACTION_MODIFIED:
               m_extMgr.updateExtension(def, resourceFiles.iterator());
               break;
         }
         
               
         // add txn log entry
         addTransactionLogEntry(dep, ctx, dep.getDisplayName(), 
            PSTransactionSummary.TYPE_EXTENSION, transAction);
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,e, 
               e.getLocalizedMessage());
      }
   }
   
   // see base class
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      // get all extensions
      List deps = new ArrayList();
      try 
      {
         Iterator exts = m_extMgr.getExtensionNames(null, null, null, null);
         while (exts.hasNext())
         {
            PSExtensionRef ref = (PSExtensionRef)exts.next();
            
            PSDependency dep = createDependency(m_def, ref.getFQN(), 
               ref.getExtensionName());
   
            if (isSystemExit(ref))
               dep.setDependencyType(PSDependency.TYPE_SYSTEM);
            else
               addRequiredClasses(dep);
               
            deps.add(dep);
         }
         
      }
      catch(PSNotFoundException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      catch (PSExtensionException e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      
      
      return deps.iterator();
   }
   
   
   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id) 
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
         
      PSDeployableObject dep = null;
      
      try
      {
         if (doesDependencyExist(tok, id))
         {
            PSExtensionRef ref = new PSExtensionRef(id);
            dep = createDependency(m_def, id, ref.getExtensionName());
            if (isSystemExit(ref))
               dep.setDependencyType(PSDependency.TYPE_SYSTEM);
            else
               addRequiredClasses(dep);
         }
      }
      catch(PSNotFoundException e)
      {
         // should not happen if doesDependencyExist returns true
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      catch (PSExtensionException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
            
      return dep;      
   }

   /**
    * Adds any required classes to the supplied dependency.
    * 
    * @param dep The dependency, assumed not <code>null</code>.
    * 
    * @throws PSDeployException If there are any errors.
    */
   private void addRequiredClasses(PSDependency dep) throws PSDeployException
   {
      try 
      {
         PSExtensionRef ref = new PSExtensionRef(dep.getDependencyId());
         Iterator files = m_extMgr.getExtensionFiles(ref);
         if (!files.hasNext())
         {
            // no files, so need to add classes as required
            IPSExtensionDef def = m_extMgr.getExtensionDef(ref);
            String className = def.getInitParameter("className");
            if (className != null && dep instanceof PSDeployableObject)
            {
               PSDeployableObject depObj = (PSDeployableObject)dep;
               depObj.setRequiredClasses(PSIteratorUtils.iterator(className));
            }
         }
      }
      catch (Exception e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      
      
   }
   
   /**
    * Provides the list of child dependency types this class can discover.
    * The child types currently supported by this handler are:
    * <ol>
    * <li>Application</li>
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

   /**
    * Get the type of depedency supported by this handler.
    * 
    * @return the type, never <code>null</code> or empty.
    */
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
      
      boolean exists = false;
      try
      {
         exists = m_extMgr.exists(new PSExtensionRef(id));
      }
      catch (PSExtensionException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      
      return exists;
   }
 
   /**
    * Determine if the extension def refers to a system exit.  Currently that is
    * determined by the name starting with the reserved system prefix 
    * <code>sys_</code> or if the extension is deprecated.
    * 
    * @param ref The extension ref to check, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the ref is a system exit, <code>false</code>
    * otherwise.
    * 
    * @throws PSNotFoundException if the extension defintion cannot be located.
    * @throws PSExtensionException if there are any other errors retrieving
    * the extension defintion.
    */
   private boolean isSystemExit(PSExtensionRef ref) throws PSNotFoundException, 
      PSExtensionException
   {
      boolean isSystem = false;
      if (ref.getExtensionName().toLowerCase().startsWith(SYS_PREFIX))
         isSystem = true;
      else
      {
         IPSExtensionDef def = m_extMgr.getExtensionDef(ref);
         isSystem = def.isDeprecated();
      }
      return isSystem;
   }  

   /**
    * Recurses supplied directory and returns all files below it.
    * 
    * @param dir Valid file reference to a directory, assumed not 
    * <code>null</code>.
    * @param files List to which files are added, assumed not <code>null</code>.
    */   
   private void catalogFiles(File dir, List files)
   {
      File[] subFiles = dir.listFiles();
      for (int i = 0; i < subFiles.length; i++) 
      {
         File subFile = subFiles[i];
         if (subFile.isDirectory())
            catalogFiles(subFile, files);
         else
            files.add(subFile);
      }
   }
   
   /**
    * Strips the codebase from the supplied file returning a file relative to
    * the extension codebase.
    * @param codeBase The code base of the extension, assumed not 
    * <code>null</code>.
    * @param file The file, assumed not <code>null</code> and that its path
    * includes the <code>codeBase</code>.
    * 
    * @return The file, never <code>null</code>.
    */
   private File stripCodeBase(File codeBase, File file)
   {
      String path = file.getPath();
      String newPath = path.substring(codeBase.getPath().length() + 1);
      return new File(newPath);
   }
   
   /**
    * Constant for this handler's supported type
    */
   private static final String EXTENSIONS_ROOT = "Extensions";
   
   /**
    * Constant for the root element of the def xml file when stored in the 
    * archive.
    */
   public static final String DEPENDENCY_TYPE = "Extension";
   
   /**
    * Constant for system exit name prefix.
    */
   private static final String SYS_PREFIX = "sys_";

   /**
    * Extension manager instance, initialized during ctor, 
    * never <code>null</code> or modified after that.
    */   
   private IPSExtensionManager m_extMgr;    
   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List ms_childTypes = new ArrayList();

   static
   {
      ms_childTypes.add(PSApplicationDependencyHandler.DEPENDENCY_TYPE);
   }
}
