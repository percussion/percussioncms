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

import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyTreeContext;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.deployer.objectstore.PSValidationResult;
import com.percussion.deployer.objectstore.PSValidationResults;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Encapsulates various objects used to perform validation across multiple
 * packages.
 */
public class PSValidationCtx
{
   /**
    * Construct a validation context with the objects used across packages
    * 
    * @param jobHandle The validation job handle, may not be <code>null</code>.
    * @param desc The import descriptor being validated, may not be 
    * <code>null</code>.
    * @param idMap The current id map, may be <code>null</code> if no
    * transformation is required.
    */
   public PSValidationCtx(IPSJobHandle jobHandle, PSImportDescriptor desc,
      PSIdMap idMap)
   {
      if (jobHandle == null)
         throw new IllegalArgumentException("jobHandle may not be null");

      if (desc == null)
         throw new IllegalArgumentException("desc may not be null");

      m_jobHandle = jobHandle;
      m_idMap = idMap;
      
      // build "full" tree context from desc so we can check absent ancestors
      m_fullTreeCtx = new PSDependencyTreeContext();
      Iterator pkgs = desc.getImportPackageList().iterator();
      while (pkgs.hasNext())
      {
         PSImportPackage pkg = (PSImportPackage)pkgs.next();
         m_fullTreeCtx.addPackage(pkg.getPackage(), true);
      }
   }
   
   /**
    * Add the dependency to this context's list of already validated 
    * dependencies.
    * 
    * @param dep The validated dependency, may not be <code>null</code>.
    */
   public void addValidatedDependency(PSDependency dep)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      // save in map, only overwrite a local (we want to store a shared over a
      // local - see alreadyValidated() for comments)
      PSDependency prev = (PSDependency)m_validatedDeps.get(dep.getKey());
      if (prev == null || prev.getDependencyType() == PSDependency.TYPE_LOCAL)
         m_validatedDeps.put(dep.getKey(), dep);
   }
   
   /**
    * Determine if the supplied dependency has already been validated.
    * 
    * @param dep The dependency to check, may not be <code>null</code>.
    * 
    * @return <code>true</code> if it has already been validated, 
    * <code>false</code> if not.
    */
   public boolean alreadyValidated(PSDependency dep)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      // if shared, check for results first.  if none, then only counts
      // if already validated as a non-local dependency.  This is because local
      // dependencies are not fully validated (we don't check for overwrites).
      boolean validated = (getValidationResult(dep) != null);
      if (!validated)
      {
         PSDependency valDep = (PSDependency)m_validatedDeps.get(dep.getKey());
         validated =  (valDep != null && 
            valDep.getDependencyType() != PSDependency.TYPE_LOCAL);
      }      
      
      return validated;
   }
   
   /**
    * Get the job handle to use to report job status.
    * 
    * @return The job handle, never <code>null</code>.
    */
   public IPSJobHandle getJobHandle()
   {
      return m_jobHandle;
   }   

   /**
    * Get the current id map to use for transforms.
    * 
    * @return The id map, may be <code>null</code> if transforms are not
    * required.
    */
   public PSIdMap getIdMap()
   {
      return m_idMap;
   }   

   /**
    * Get the current dependency tree context, contains all packages in the 
    * import descriptor.
    * 
    * @return The tree context, never <code>null</code>.
    */
   public PSDependencyTreeContext getCurrentTreeCtx()
   {
      return m_fullTreeCtx;
   }
   
   /**
    * Adds a package to this context so that previous packages validation 
    * results may retrieved by calls to 
    * {@link #getValidationResult(PSDependency)}.
    * 
    * @param pkg The package to add, may not be <code>null</code>.
    */
   public void addPackage(PSImportPackage pkg)
   {
      if (pkg == null)
         throw new IllegalArgumentException("pkg may not be null");

      PSDeployableElement de = pkg.getPackage();
      m_pkgMap.put(de.getKey(), pkg);      
   }   
   

   /**
    * Get any previously added validation result for the supplied dependency.
    * 
    * @param dep The dependency to check for, may not be <code>null</code>.
    * 
    * @return The result, may be <code>null</code> if no result has been added.
    */
   public PSValidationResult getValidationResult(PSDependency dep)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      PSValidationResult result = null;
      Iterator pkgs = m_pkgMap.values().iterator();
      while (pkgs.hasNext() && result == null)
      {
         PSImportPackage pkg = (PSImportPackage)pkgs.next();
         PSValidationResults results = pkg.getValidationResults();
         if (results != null)
            result = results.getResult(dep);
      }
      return result;
   }
   
   /**
    * The jobhandle supplied during construction, never <code>null</code> or 
    * modified after that.
    */
   private IPSJobHandle m_jobHandle;
   
   /**
    * Full tree context containing all packages from the import descriptor
    * supplied during construction.  Used to validate ancestor inclusion in the
    * archive.  Never <code>null</code> or modified after construction.
    */
   private PSDependencyTreeContext m_fullTreeCtx;
   
   /**
    * The ID Map supplied during construction, never <code>null</code> or 
    * modified after that.
    */
   private PSIdMap m_idMap;
   
   /**
    * Map of import packages.  Key is the dependency key of the root element of
    * the package as a <code>String</code>, value is the corresponding 
    * {@link PSImportPackage}. Never <code>null</code> after construction.  
    * Packages are added by calls to {@link #addPackage(PSImportPackage)}.
    */
   private Map m_pkgMap = new HashMap();
   
   /**
    * Map of previously validated dependencies.  Key is the dependency key of 
    * the dependency as a <code>String</code>, value is the {@link PSDependency}
    * object.  Never <code>null</code>, entries are added by calls to
    * {@link #addValidatedDependency(PSDependency)}.
    */
   private Map m_validatedDeps = new HashMap();   
}
