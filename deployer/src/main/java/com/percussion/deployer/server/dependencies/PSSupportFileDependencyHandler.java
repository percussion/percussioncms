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
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.util.PSIteratorUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Class to handle packaging and deploying a support file.
 */
public class PSSupportFileDependencyHandler extends PSAppObjectDependencyHandler
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
   public PSSupportFileDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   // see base class
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      // if specifies an app, return as system or server dependency
      List deps = new ArrayList();
      String appName = PSDeployComponentUtils.getAppName(dep.getDependencyId());
      if (appName != null)
      {
         PSDependency appDep = getAppHandler().getDependency(tok, appName);
         if (appDep != null)
         {
            deps.add(appDep);
         }
      }
      
      return deps.iterator();
    }

   // see base class
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      // not supported
      return PSIteratorUtils.emptyIterator();
   }

   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      
      PSDependency dep = null;
      String appName = PSDeployComponentUtils.getAppName(id);
      if (appName != null)
      {
         boolean treatAsApp = getAppHandler().doesDependencyExist(tok, appName);
         treatAsApp = treatAsApp && !(appName.equals(SYS_CONTROL_APP) || 
         appName.equals(USER_CONTROL_APP) ||
         appName.equals(WEB_RESOURCES_APP));
         
         if (!treatAsApp)
         {
            File file = getAppFile(tok, id);
            if (file != null)
            {
               dep = createDependency(m_def, id, file.getName());
               if (isSystemFile(appName))
                  dep.setDependencyType(PSDependency.TYPE_SYSTEM);
            }
         }
      }
      
      return dep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
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

   // see base class
   public String getType()
   {
      return DEPENDENCY_TYPE;
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
      
      return getDependencyFiles(tok, dep.getDependencyId());
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
      
      installFileDependencyFiles(tok, archive, dep, ctx);
   }

   // see base class
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      return getDependency(tok, id) != null;
   }

   /**
    * Get the dependency files for the supplied id.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param id The dependency id, may not be <code>null</code> or empty.  
    * Should reference a support file.
    * 
    * @return An iterator over zero or more <code>PSDependencyFile</code> 
    * objects, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If there are any errors.
    */
   protected Iterator getDependencyFiles(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");         
      
      List files = new ArrayList();
      
      String appName = PSDeployComponentUtils.getAppName(id);
      File appfile = getAppFile(tok, id);
      if (appName != null && appfile != null)
      {
         File tmpFile = getFileFromApp(tok, appName, appfile);
         PSDependencyFile depFile = new PSDependencyFile(
            PSDependencyFile.TYPE_SUPPORT_FILE, tmpFile, appfile);
         files.add(depFile);
      }
      
      return files.iterator();
   }

   
   /**
    * Installs the dependency files for the supplied file dependency.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param archive The archive handler to get the dependency data from,
    * may not be <code>null</code>.
    * @param dep The dependency, may not be <code>null</code>, and must be a 
    * type supported by this handler or one of its derived handlers.
    * @param ctx The import context, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If there are any errors.
    */
   protected void installFileDependencyFiles(PSSecurityToken tok,
      PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
         
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      // expecting 1 file
      PSDependencyFile depFile = null;
      
      Iterator files = archive.getFiles(dep);
      if (files.hasNext())
      {
         PSDependencyFile file = (PSDependencyFile)files.next();
         if (file.getType() == PSDependencyFile.TYPE_SUPPORT_FILE)
            depFile = file;
      }
      
      if (depFile == null)
      {
         Object[] args = 
         {
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_SUPPORT_FILE], 
            dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName()
         };
         
         throw new PSDeployException(
            IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }

      saveAppFile(tok, archive, dep, ctx, depFile);
   }

   /**
    * Get the application dependency handler, from cached reference after first
    * call.
    * 
    * @return The handler, never <code>null</code>.
    * 
    * @throws PSDeployException if the handler cannot be loaded.
    */
   private PSDependencyHandler getAppHandler() throws PSDeployException
   {
      if (m_appHandler == null)
         m_appHandler = getDependencyHandler(
            PSApplicationDependencyHandler.DEPENDENCY_TYPE);
      
      return m_appHandler;         
   }
   
   
   /**
    * Determine if the specified file is a system file.
    * 
    * @param appName The name of the app supplying the file, it may not be 
    * <code>null</code> or empty.
    * 
    * @return <code>true</code> if it is a system file, <code>false</code>
    * otherwise
    * 
    * @throws IllegalArgumentException if <code>appName</code> is 
    * <code>null</code> or empty
    */
   private static boolean isSystemFile(String appName)
   {
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");
         
      boolean isSystem = false;
      
      isSystem = PSApplicationDependencyHandler.isSystemApp(appName);
      isSystem = isSystem && !(appName.equalsIgnoreCase("rx_resources") || 
      appName.equalsIgnoreCase("web_resources"));
      
      return isSystem;
   }
   
   
   /**
    * App dependency handler, initialized by first call to 
    * {@link #getAppHandler()}, never <code>null</code> or modified after that.
    */
   private PSDependencyHandler m_appHandler = null;

   /**
    * Constant for this handler's supported type
    */
   public static final String DEPENDENCY_TYPE = "SupportFile";

   /**
    * Constant for web resources app
    */
   private static final String WEB_RESOURCES_APP = "web_resources";
   
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
