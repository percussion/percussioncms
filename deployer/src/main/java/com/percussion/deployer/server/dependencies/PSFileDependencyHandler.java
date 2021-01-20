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
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSServer;
import com.percussion.services.notification.PSNotificationHelper;
import com.percussion.util.IOTools;
import com.percussion.util.PSIteratorUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for handlers that package and install files directly to and from 
 * the file system.
 */
public abstract class PSFileDependencyHandler extends PSDependencyHandler
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
   public PSFileDependencyHandler(PSDependencyDef def, 
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }
   
   
   /**
    * This class returns an empty list.  Derrived class should override this
    * method if they support child types.  See 
    * {@link PSDependencyHandler#getChildDependencies(PSSecurityToken, 
    * PSDependency) Base Class} for more info.
    */
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");
         
      return PSIteratorUtils.emptyIterator();
   }
   
   // see base class
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
         
      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");
      
      List files = new ArrayList();
      File depFile = new File(
            PSServer.getRxDir().getAbsolutePath(), 
            PSDeployComponentUtils.getNormalizedPath(dep.getDependencyId()));
      if (!depFile.exists())
      {
         Object[] args = {dep.getObjectTypeName(), dep.getDependencyId(), 
               dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND, 
            args);
      }
      
      files.add(new PSDependencyFile(PSDependencyFile.TYPE_SUPPORT_FILE, 
         depFile));
      
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
         
      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");
      
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      // get file data
      Iterator files = archive.getFiles(dep);
      PSDependencyFile depFile = null;
      if (files.hasNext())
      {
         depFile = (PSDependencyFile)files.next();
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
      
      int transAction = PSTransactionSummary.ACTION_CREATED;
      File tgtFile = new File(PSServer.getRxDir().getAbsolutePath(),
            PSDeployComponentUtils.getNormalizedPath(dep.getDependencyId()));
      if (tgtFile.exists())
         transAction = PSTransactionSummary.ACTION_MODIFIED;
      
      // create directories if they do not exist
      File parentDir = tgtFile.getParentFile();
      if (parentDir != null)
         parentDir.mkdirs();
      
      // install the file
      InputStream in = null;
      FileOutputStream out = null;
      try 
      {
         // ensure the timestamp is updated
         tgtFile.setLastModified(System.currentTimeMillis());
          if(tgtFile.exists()){
              try { //trying to handle the jvm file lock case..
                  out = new FileOutputStream(tgtFile);
                  in = archive.getFileData(depFile);
                  IOTools.copyStream(in, out);
              }catch(Exception e){;}

          }else { //default previous code
              out = new FileOutputStream(tgtFile);
              in = archive.getFileData(depFile);
              IOTools.copyStream(in, out);
          }
         addTransactionLogEntry(dep, ctx, tgtFile.getPath(),
            PSTransactionSummary.TYPE_FILE, transAction);

         // notify whoever interested after a the file is successful installed
         PSNotificationHelper.notifyFile(tgtFile);                  
      }
      catch (IOException e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      finally 
      {
         if (in != null)
            try{in.close();} catch (IOException e){}
         if (out != null)
            try{out.close();} catch (IOException e){}
      }
   }
   
   // see base class
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      return PSIteratorUtils.emptyIterator();
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
      
      id = PSDeployComponentUtils.getNormalizedPath(id);
      File depFile = new File(id);
      if (depFile.exists())
         dep = createDependency(m_def, id, depFile.getName());
      
      return dep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * Base class returns an empty list.  Derrived class should override this
    * method if they support child types.
    * 
    * @return An empty iterator, never <code>null</code>.
    */
   public Iterator getChildTypes()
   {
      return PSIteratorUtils.emptyIterator();
   }

   // see base class
   public boolean doesDependencyExist(PSSecurityToken tok, String id) 
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      
      return (getDependency(tok, id) != null);
   }
}

