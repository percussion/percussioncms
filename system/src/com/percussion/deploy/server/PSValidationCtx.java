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
package com.percussion.deploy.server;

import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDependencyContext;
import com.percussion.deploy.objectstore.PSDependencyTreeContext;
import com.percussion.deploy.objectstore.PSDeployableElement;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.objectstore.PSImportDescriptor;
import com.percussion.deploy.objectstore.PSImportPackage;
import com.percussion.deploy.objectstore.PSValidationResult;
import com.percussion.deploy.objectstore.PSValidationResults;
import com.percussion.util.PSIteratorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    * Set whether ancestor validation should be performed.
    * 
    * @param doValidate <code>true</code> to perform ancestor validation,
    * <code>false</code> to skip it.
    */
   public void setValidateAncestors(boolean doValidate)
   {
      m_validateAncestors = doValidate;
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
    * Determine if ancestor validation should be performed.
    * 
    * @return <code>true</code> if it should, <code>false</code> if it should
    * be skipped.
    */
   public boolean getValidateAncestors()
   {
      return m_validateAncestors;
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
    * Determine if the supplied dependency is included in any package within the
    * import descriptor being validated.  
    * 
    * @param dep The depednecy to check, may not be <code>null</code>.
    * 
    * @return <code>true</code> if the archive includes the dependency, 
    * <code>false</code> if not.
    */
   public boolean archiveIncludesDependency(PSDependency dep)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      boolean found = false;
      
      PSDependencyContext ctx = m_fullTreeCtx.getDependencyCtx(dep.getKey());
      if (ctx != null)
         found = ctx.isIncluded();
      
      return found;      
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
    * Add an absent ancestor to this context.  See 
    * {@link #getAbsentAncestors(PSDependency)} for more info.
    *  
    * @param dep The dependency for which an absent ancestor is supplied, may
    * not be <code>null</code>.    
    * @param anc The absent ancestor, may not be <code>null</code>.
    */
   public void addAbsentAncestor(PSDependency dep, PSDependency anc)
   { 
      List ancList = (List)m_absentAncs.get(dep.getKey());
      if (ancList == null)
      {
         ancList = new ArrayList();
         m_absentAncs.put(dep.getKey(), ancList);
      }
      ancList.add(anc);
   }
   
   /**
    * Get any absent ancestors added for the specified dependency.  Absent
    * ancestors are ancestors of the dependency on the target server that are
    * not included in the dependency's package.
    * 
    * @param dep The dep for which ancestors may have been added, may not be
    * <code>null</code>.
    * 
    * @return An iterator over zero or more ancestors, never <code>null</code>,
    * may be empty.
    */
   public Iterator getAbsentAncestors(PSDependency dep)
   {
      Iterator ancs;
      List ancList = (List)m_absentAncs.get(dep.getKey());
      if (ancList == null)
         ancs = PSIteratorUtils.emptyIterator();
      else
         ancs = ancList.iterator();
         
      return ancs;
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
    * Flag to indicate if ancestors should be validated.  Initially 
    * <code>false</code>, modified by calls to 
    * {@link #setValidateAncestors(boolean)}.
    */
   private boolean m_validateAncestors = false;

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
   
   /**
    * Map of absent ancestors.  Key is the dependency key of the dependency for
    * which the ancestors were added as a <code>String</code>, value is a
    * <code>List</code> of {@link PSDependency} objects.  Never 
    * <code>null</code>, values are added by calls to 
    * {@link #addAbsentAncestor(PSDependency, PSDependency)}
    */
   private Map m_absentAncs = new HashMap();
}
