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
 
package com.percussion.deployer.server;

import com.percussion.deployer.objectstore.PSAppPolicySettings;
import com.percussion.deployer.objectstore.PSApplicationIDTypes;
import com.percussion.deployer.objectstore.PSDbmsInfo;
import com.percussion.deployer.objectstore.PSDbmsMap;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSValidationResults;
import com.percussion.utils.guid.IPSGuid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Contains objects used during the installation of deployment packages.
 */
public class PSImportCtx 
{

   /**
    * Construct the import context for the current import job.
    * 
    * @param userId The name of the user that initiated the import job, may
    * not be <code>null</code> or empty.
    * @param sourceRepository The repository of the source server being 
    * installed from.  May not be <code>null</code>.
    * @param dbmsMap The dbmsMap for the target server.  May be 
    * <code>null</code> if dbms transformations are not required.
    * @param idMapMgr The ID Map manager instance, may not be <code>null</code>.
    * @param logHander The log handler for the import job, may not be 
    * <code>null</code>.
    * @param policySettings The application policy settings, may not be 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSImportCtx(String userId, PSDbmsInfo sourceRepository, 
      PSDbmsMap dbmsMap, PSIdMapManager idMapMgr, PSLogHandler logHander, 
         PSAppPolicySettings policySettings)
   {
      if (userId == null || userId.trim().length() == 0)
         throw new IllegalArgumentException("userId may not be null or empty");
      
      if (sourceRepository == null)
         throw new IllegalArgumentException("sourceRepository may not be null");
      
      if (idMapMgr == null)
         throw new IllegalArgumentException("idMapMgr may not be null");
      
      if (logHander == null)
         throw new IllegalArgumentException("logHander may not be null");
      
      if (policySettings == null)
         throw new IllegalArgumentException("policySettings may not be null");
      
      m_userId = userId;
      m_repository = sourceRepository;
      m_dbmsMap = dbmsMap;
      m_idMapMgr = idMapMgr;
      m_logHandler = logHander;
      m_appPolicySettings = policySettings;
   }
   
   /**
    * Get the name of the user that initiated the import job.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getUserId()
   {
      return m_userId;
   }
   
   /**
    * Get the Guid of the package getting installed.
    * 
    * @return Guid of package, maybe <code>null</code>
    */
   public IPSGuid getPkgGuid()
   {
      return m_pkgGuid;
   }
    
   /**
    * Get the repository being installed from.
    * 
    * @return The repository dbms info never <code>null</code>.
    */
   public PSDbmsInfo getSourceRepository()
   {
      return m_repository;
   }

   /**
    * Get the dbms map for the target server.
    * 
    * @return The map, may be <code>null</code>.
    */
   public PSDbmsMap getdbmsMap()
   {
      return m_dbmsMap;
   }

   /**
    * Get the ID map manager instance.
    * 
    * @return The manager, never <code>null</code>.
    */
   public PSIdMapManager getIdMapMgr()
   {
      return m_idMapMgr;
   }
   
   /**
    * Set the ID Map to use when installing dependencies.
    * 
    * @param map The map, may be <code>null</code> to indicate no ID 
    * transformations are required.
    */
   public void setCurrentIdMap(PSIdMap map)
   {
      m_curIdMap = map;
   }
   
   /**
    * Get the current ID map to use when installing dependencies.
    * 
    * @return The map, may be <code>null</code> if no ID transformations are
    * required.
    */
   public PSIdMap getCurrentIdMap()
   {
      return m_curIdMap;
   }
   
   /**
    * Get the log handler to use for logging transactions.
    * 
    * @return The log handler, never <code>null</code>.
    */   
   public PSLogHandler getLogHandler()
   {
      return m_logHandler;
   }

   /**
    * Sets the current archive log ID to use to log archive info.
    * 
    * @param id The ID of an archive log that has been created.  Must be >= 0.
    * 
    * @throws IllegalArgumentException if <code>id</code> is invalid.
    */
   public void setArchiveLogId(int id)
   {
      if (id < 0)
         throw new IllegalArgumentException("id must be >= 0");
         
      m_archiveLogId = id;
   }

   /**
    * Gets the current archive log id.
    * 
    * @return the id or -1 if one has not been set
    */
   public int getArchiveLogId()
   {
      return m_archiveLogId;
   }

   /**
    * Set the current package log id to use to log transactions.
    * 
    * @param id The ID of a package log that has been created. Must be >= 0.
    * 
    * @throws IllegalArgumentException if <code>id</code> is invalid.
    */
   public void setPackageLogId(int id)
   {
      if (id < 0)
         throw new IllegalArgumentException("id must be >= 0");
         
      m_packageLogId = id;
   }

   /**
    * Set the GUID of the package getting installed.
    * 
    * @param guid The package GUID 
    */
   public void setPkgGuid(IPSGuid guid)
   {
      m_pkgGuid = guid;
   }
   
   /**
    * Get the current package log id.
    * 
    * @return the id or -1 if one has not been set.
    */
   public int getPackageLogid()
   {
      return m_packageLogId;
   }
   
   /**
    * Gets the next transaction sequence id to use.  This method is not thread
    * safe, as it is assumed that installation is single threaded.  
    * 
    * @param packageLogId The package log for which the sequence is to be 
    * obtained.
    * 
    * @return A monotonically increasing sequence to use to order transaction
    * log entries for a package.
    */
   public int getNextTxnSequence(int packageLogId)
   {
      // default to 1 if first call
      Integer pkgId = new Integer(packageLogId);
      int nextId = 1;
      Integer lastId = (Integer)m_txnSeqMap.get(pkgId);
      if (lastId != null)
         nextId = lastId.intValue() + 1;
      
      m_txnSeqMap.put(pkgId, new Integer(nextId));
      
      return nextId;
   }

   /**
    * Set the id types map for the current dependency being installed.
    * 
    * @param idTypes The id types, may be <code>null</code> to clear it.
    */
   public void setIdTypes(PSApplicationIDTypes idTypes)
   {
      m_idTypes = idTypes;
   }

   /**
    * Get the current id types map.
    * 
    * @return The id types for the current dependency, may be <code>null</code>.
    */
   public PSApplicationIDTypes getIdTypes()
   {
      return m_idTypes;
   }

   /**
    * Get the app policy settings for the server.
    * 
    * @return The settings, never <code>null</code>.
    */
   public PSAppPolicySettings getAppPolicySettings()
   {
      return m_appPolicySettings;
   }
   
   /**
    * Sets the validation results for the package currently being processed.
    * 
    * @param results The results to set, may be <code>null</code> to clear
    * the results.
    */
   void setCurrentValidationResults(PSValidationResults results)
   {
      m_curValResults = results;
   }
   
   /**
    * Gets the validation results for the package currently being processed.
    * 
    * @returns The result, may be <code>null</code> if the current package did
    * not have any warnings or errors or if none have been set.
    */
   public PSValidationResults getCurrentValidationResults()
   {
      return m_curValResults;
   }
   
   /**
    * Adds the supplied dependency as already installed.
    * 
    * @param dep The dependency to add, may not be <code>null</code>, may not
    * have already been added for the specified package.
    * @param pkg The package for which the dependency is being installed, may
    * not be <code>null</code>.
    */
   void addInstalledDependency(PSDependency dep, PSDeployableElement pkg)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (pkg == null)
         throw new IllegalArgumentException("pkg may not be null");

      String depKey = dep.getKey();
      Set pkgSet = (Set)m_installedPkgDeps.get(depKey);
      if (pkgSet == null)
      {
         pkgSet = new HashSet();
         m_installedPkgDeps.put(depKey, pkgSet);
      }
      if (!pkgSet.add(pkg.getKey()))
         throw new IllegalStateException(
            "dep has already been added as intalled for this package");        
   }
   
   /**
    * Determine if 
    * {@link #addInstalledDependency(PSDependency, PSDeployableElement)} has 
    * already been called for the supplied dependency without regard for 
    * package.
    * 
    * @param dep The dependency to check, may not be <code>null</code>.
    * 
    * @return <code>true</code> if it has been installed, <code>false</code>
    * if not.
    */
   boolean isDependencyInstalled(PSDependency dep)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      return m_installedPkgDeps.keySet().contains(dep.getKey());
   }

   /**
    * Determine if 
    * {@link #addInstalledDependency(PSDependency, PSDeployableElement)} has 
    * already been called for the supplied dependency and package
    * 
    * @param dep The dependency to check, may not be <code>null</code>.
    * @param pkg The package of the dependency, may not be <code>null</code>.
    * 
    * @return <code>true</code> if it has been installed, <code>false</code>
    * if not.
    */
   boolean isDependencyInstalled(PSDependency dep, PSDeployableElement pkg)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (pkg == null)
         throw new IllegalArgumentException("pkg may not be null");

      boolean installed = false;
      
      Set pkgSet = (Set)m_installedPkgDeps.get(dep.getKey());
      if (pkgSet != null)
         installed = pkgSet.contains(pkg.getKey());
      
      return installed;
   }
   
   /**
    * Accessor for getting the current dependency that is being installed
    * 
    * @return PSDependency the current dependency if any, may be <code>null</code>
    */
   public PSDependency getCurrentDependency()
   {
      return m_curDependency;
   }
   
  /**
   * Sets the current dependency that is being installed so that in case of  any
   * failure[s] in the package installation, more information can be provided.
   * @param d the dependency may be <code>null</code>
   */
   public void setCurrentDependency(PSDependency d )
   {
      m_curDependency = d;
   }

   /**
    * The repository set during ctor, never <code>null</code> or 
    * modified after that.
    */
   private PSDbmsInfo m_repository;
   
   /**
    * The dbms map set during ctor, may be <code>null</code>, never modified 
    * after that.
    */
   private PSDbmsMap m_dbmsMap;
   
   /**
    * The id map mgr set during ctor, never <code>null</code> or modified 
    * after that.
    */
   private PSIdMapManager m_idMapMgr;
   
   /**
    * The log handler set during ctor, never <code>null</code> or modified 
    * after that.
    */
   private PSLogHandler m_logHandler;
   
   /**
    * The current ID map set by {@link #setCurrentIdMap(PSIdMap)}.  May be
    * <code>null</code>.
    */
   private PSIdMap m_curIdMap;
   
   /**
    * The current archive log id, used to write to the archive log.  Intialized
    * to <code>-1</code>, set by calls to {@link #setPackageLogId(int)}.
    */
   private int m_archiveLogId = -1;
   
   /**
    * The current package log id, used to write to the package log.  Intialized
    * to <code>-1</code>, set by calls to {@link #setArchiveLogId(int)}.
    */
   private int m_packageLogId = -1;
   
   /**
    * The id types map for the deployable object currently being installed.
    * <code>null</code> unless set for the current dependency being processed,
    * modified by calls to {@link #setIdTypes(PSApplicationIDTypes)}.
    */
   private PSApplicationIDTypes m_idTypes = null;
   
   /**
    * Name of the user that initiated the import job, never <code>null</code> or 
    * empty after construction.
    */
   private String m_userId;
   
   /**
    * The app policy settings for the server, never <code>null</code> or 
    * modified after construction.
    */
   private PSAppPolicySettings m_appPolicySettings;
   
   /**
    * The validation results for the package currently being processed, may
    * be <code>null</code>, modified by calls to 
    * {@link #setCurrentValidationResults(PSValidationResults)}.
    */
   private PSValidationResults m_curValResults = null;

   
   /**
    * Map of installed dependencies.  Key is the dependency key as a 
    * <code>String</code> object, value is a <code>List</code> of package
    * keys as <code>String</code> objects.
    */
   private Map m_installedPkgDeps = new HashMap();
   
   /**
    * Map of pacakge log ids to next transaction sequence number.  Key is the
    * package log id, and value is the last transaction sequence number used,
    * both as <code>Integer</code> objets.  Never <code>null</code>, modified
    * by calls to {@link #getNextTxnSequence(int)}.
    */
   private Map m_txnSeqMap = new HashMap();
   
   /**
    * A place holder for the current dependency that is being deployed. This is 
    * for tracking installation of package errors, so that the user can have 
    * a better handle on identifying what was the last dependency that failed
    * to finish installation
    */
   private PSDependency m_curDependency = null;
   
   /**
    * Guid assigned to the package that is being installed.
    */
   private IPSGuid m_pkgGuid;   
}
