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

package com.percussion.deployer.objectstore;

import com.percussion.deployer.client.IPSDependencySuppressor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Tracks dependencies across multiple packages, and manages the selected
 * state of different instances of dependency objects that reference the same
 * actual dependency on the server as dependencies are added or removed.
 */
public class PSDependencyTreeContext
{
   /**
    * Convenience method that calls 
    * {@link #addPackage(PSDeployableElement, boolean)
    * addPackage(pkg, false)}
    */
   public void addPackage(PSDeployableElement pkg)
   {
      addPackage(pkg, false);
   }

   /**
    * Adds the package and optionally all of its child dependencies to the 
    * context, selecting any of its dependencies that are already selected 
    * within this context.
    * 
    * @param pkg The package to add, may not be <code>null</code> and may not
    * already be added to this context.
    * @param recurse <code>true</code> to recurse into child dependencies and
    * ancestors, <code>false</code> to only add the pkg element.
    */
   public void addPackage(PSDeployableElement pkg, boolean recurse)
   {
      if (pkg == null)
         throw new IllegalArgumentException("pkg may not be null");
      
      if (getDependencyCtx(pkg) != null)
         throw new IllegalArgumentException("pkg already added to this ctx");
      
      // add to map
      m_pkgMap.put(pkg.getKey(), pkg);
      
      // add context for package
      addPackageDependency(pkg, pkg, recurse);      
   }
   
   /**
    * Removes the supplied package and all of it's child dependencies from this 
    * context.
    * 
    * @param pkg The package to removed, may not be <code>null</code> and must
    * have been added to this context.
    * @param removeLocal <code>true</code> to un-include matching shared 
    * dependencies in other packages when removing local dependencies contained 
    * in the supplied package, <code>false</code> to leave the matching shared
    * deps as included. 
    */      
   public void removePackage(PSDeployableElement pkg, boolean removeLocal)
   {
      if (pkg == null)
         throw new IllegalArgumentException("pkg may not be null");

      if (getDependencyCtx(pkg) == null)
         throw new IllegalArgumentException("pkg not found in this ctx");
      
      // remove package from map
      m_pkgMap.remove(pkg.getKey());
      
      // remove listener for this package
      Iterator listeners = m_listeners.iterator();
      while (listeners.hasNext())
      {
         IPSDependencyTreeCtxListener listener = 
            (IPSDependencyTreeCtxListener)listeners.next();
         if (listener.listensForChanges(pkg))
         {
            removeCtxChangeListener(listener);
            break;
         }  
      }
         
      removePackageDependency(pkg, true, removeLocal);
   }

   /**
    * Get the package specified by the supplied key.
    * 
    * @param key The dependency key of the package, may not be <code>null</code> 
    * or empty and must reference a package added to the context.
    * 
    * @return The package element, never <code>null</code>.
    */
   public PSDeployableElement getPackage(String key)
   {
      if (key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");
      
      PSDeployableElement pkg = (PSDeployableElement)m_pkgMap.get(key);
      if (pkg == null)
         throw new IllegalArgumentException("Package not found for key: " + 
            key);
      
      return pkg;
   }

   /**
    * Determines if the specified dependency should be suppressed (removed) from 
    * the dependency tree, by delegating to the assigned dependency suppressor
    * (if any).
    * 
    * @param dep the dependency to consider for suppression, not 
    * <code>null</code>.
    * @return <code>true</code> if the dependency should be suppressed;
    * <code>false</code> if the dependency should not be suppressed, or if
    * there is no assigned dependency suppressor.
    */
   public boolean shouldSuppressDependency(PSDependency dep)
   {
      if (dep == null)
         throw new IllegalArgumentException("dependency may not be null");
         
      if (m_dependencySuppressor != null)
         return m_dependencySuppressor.suppress(dep);
      else
         return false;
   }
   
   /**
    * Determines if removing the supplied package will remove local dependencies
    * that are selected as shared dependencies elsewhere in the tree and returns
    * the affected packages and dependencies
    * 
    * @param pkg The package to check, may not be <code>null</code>.
    * 
    * @return Map of removed local dependencies that appear as included shared
    * dependencies in other packages.  Key is the dependency key of the package 
    * as a <code>String</code>, value is a list of matching shared 
    * dependencies found in that package as <code>PSDependency</code> objects.
    */
   public Map checkRemoveLocal(PSDeployableElement pkg)
   {
      if (pkg == null)
         throw new IllegalArgumentException("pkg may not be null");
      
      Map resultMap = new HashMap();
      checkRemoveLocal(pkg, pkg, resultMap);
      
      if (resultMap.isEmpty())
         resultMap = null;
      
      return resultMap;
   }
   
   /**
    * Adds the supplied dependency to this context.  Selects the dependency as 
    * included if it is shared and other matching dependencies within the tree 
    * are selected.  Does not recurse into the child dependencies or ancestors 
    * of the dependency.
    *   
    * @param dep The dependency to add, may not be <code>null</code>.
    * @param pkg The package containing the supplied dependency, may not be
    * <code>null</code>.
    * 
    * @return The context to which the dependency was added, never 
    * <code>null</code>.
    */      
   public PSDependencyContext addDependency(PSDependency dep, 
      PSDeployableElement pkg)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (pkg == null)
         throw new IllegalArgumentException("pkg may not be null");

      // add context for dependency
      return addPackageDependency(dep, pkg, false);
   }
   
   /**
    * Removes the supplied dependency from this context.  Also remove its child 
    * dependencies and ancestors recursively.  
    * 
    * @param dep The dependency to remove, may not be <code>null</code>.
    * @param removeLocal <code>true</code> to un-include matching shared 
    * dependencies in other packages when removing local dependencies, 
    * <code>false</code> to leave the matching shared
    * deps as included.
    */
   public void removeDependency(PSDependency dep, 
      boolean removeLocal)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      removePackageDependency(dep, true, removeLocal);
   }

   /**
    * Get the context for the supplied dependency.
    * 
    * @param dep The dependency for which the context is to be retrieved, may
    * not be <code>null</code>.
    * 
    * @return The context, <code>null</code> if a matching context containing
    * the actual instance of the supplied dependency is not found.
    */      
   public PSDependencyContext getDependencyCtx(PSDependency dep)
   {
      PSDependencyContext ctx = null;
      
      ctx = getDependencyCtx(dep.getKey());
      if (ctx != null && !ctx.containsDependency(dep))
         ctx = null;
      
      return ctx;
   }

   /**
    * Get the context for the supplied dependency key.
    * 
    * @param dep The dependency key for which the context is to be retrieved, 
    * may not be <code>null</code> or empty.
    * 
    * @return The context, <code>null</code> if a matching context is not found.
    */      
   public PSDependencyContext getDependencyCtx(String depKey)
   {
      if (depKey == null || depKey.trim().length() == 0)
         throw new IllegalArgumentException("depKey may not be null or empty");

      return (PSDependencyContext)m_depCtxMap.get(depKey);
   }   

   /**
    * Adds a context change listener to be notified of any changes to this
    * context.
    * 
    * @param listener The listener to notify, may not be <code>null</code>.
    */
   public void addCtxChangeListener(IPSDependencyTreeCtxListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");

      m_listeners.add(listener);
   }

   /**
    * Removes a context change listener to no longer be notified of any 
    * changes to this context.
    * 
    * @param listener The listener to remove, may not be <code>null</code>.  It
    * is ok if the listener has never been added.
    */
   public void removeCtxChangeListener(IPSDependencyTreeCtxListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");

      m_listeners.remove(listener);
   }
   
   /**
    * Notify all change listeners.
    * 
    * @param ctx The context that has changed, may not be <code>null</code>.
    */
   public void notifyCtxChangeListeners(PSDependencyContext ctx)
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      
      Iterator listeners = m_listeners.iterator();
      while (listeners.hasNext())
      {
         IPSDependencyTreeCtxListener listener = 
            (IPSDependencyTreeCtxListener)listeners.next();
         listener.ctxChanged(ctx);
      }
   }
   
   /**
    * Sets the object that may be consulted to determine if a given
    * dependency should be suppressed from inclusion in the dependency tree.
    * 
    * @param depSuppressor the suppressor to assign, or <code>null</code> to
    * remove the suppressor.
    */
   public void setDependencySuppressor(IPSDependencySuppressor depSuppressor)
   {
      m_dependencySuppressor = depSuppressor;
   }

   /**
    * Helper method for {@link #addPackage(PSDeployableElement, boolean)} and  
    * {@link #addDependency(PSDependency, PSDeployableElement)}.  Adds the 
    * supplied dependency and optionally recurses into its child dependencies.
    * 
    * @param dep The dependency to add, assumed not <code>null</code>.
    * @param pkg The package containing the dependency, assumed not 
    * <code>null</code>.
    * @param recurse <code>true</code> to recurse into the child dependencies,
    * <code>false</code> otherwise.
    * 
    * @return The context to which the supplied dependency was added, never
    * <code>null</code>. 
    */
   private PSDependencyContext addPackageDependency(PSDependency dep, 
      PSDeployableElement pkg, boolean recurse)
   {
      String depKey = dep.getKey();
      
      PSDependencyContext ctx = (PSDependencyContext)m_depCtxMap.get(depKey);
      if (ctx == null)
      {
         ctx = new PSDependencyContext(depKey, this);
         m_depCtxMap.put(depKey, ctx);         
      }      
      ctx.addDependency(dep, pkg);
      
      if (recurse)
      {
         Iterator childDeps = dep.getDependencies();
         if (childDeps != null)
         {
            while (childDeps.hasNext())
            {
               addPackageDependency((PSDependency)childDeps.next(), pkg, true);
            }
         }
         
         Iterator ancs = dep.getAncestors();
         if (ancs != null)
         {
            while (ancs.hasNext())
            {
               addPackageDependency((PSDependency)ancs.next(), pkg, true);
            }
         }
      }
      
      return ctx;      
   }

   /**
    * Recursive worker method for 
    * {@link #removePackage(PSDeployableElement, boolean)}. Also called by 
    * {@link #addDependency(PSDependency, PSDeployableElement)}.
    * 
    * @param dep The dependency to add, assumed not <code>null</code>.
    * @param recurse <code>true</code> to recurse into the child dependencies,
    * <code>false</code> otherwise.
    * @param removeLocal See {@link #removePackage(PSDeployableElement, 
    * boolean)} for a description
    */
   private void removePackageDependency(PSDependency dep, boolean recurse, 
      boolean removeLocal)
   {
      String depKey = dep.getKey();
      
      PSDependencyContext ctx = (PSDependencyContext)m_depCtxMap.get(depKey);
      if (ctx != null)
         ctx.removeDependency(dep, removeLocal);
      
      if (recurse)
      {
         Iterator childDeps = dep.getDependencies();
         if (childDeps != null)
         {
            while (childDeps.hasNext())
            {
               removePackageDependency((PSDependency)childDeps.next(), recurse, 
                  removeLocal);
            }
         }
         
         Iterator ancs = dep.getAncestors();
         if (ancs != null)
         {
            while (ancs.hasNext())
            {
               removePackageDependency((PSDependency)ancs.next(), recurse, 
                  removeLocal);
            }
         }
      }      
   }
   
   /**
    * Recursive worker method for {@link #checkRemoveLocal(PSDeployableElement)}
    * 
    * @param dep The current dependency to check, assumed not <code>null</code>.
    * Will recurse into its child dependencies.
    * @param pkg The package being checked, assumed not <code>null</code>.
    * @param resultMap The map to which the results are added, assumed not 
    * <code>null</code>.  
    */
   private void checkRemoveLocal(PSDependency dep, PSDeployableElement pkg, 
      Map resultMap)
   {
      String depKey = dep.getKey();
      
      PSDependencyContext ctx = (PSDependencyContext)m_depCtxMap.get(depKey);
      if (ctx != null)
      {
         ctx.checkRemoveLocal(dep, pkg, resultMap);
      }
      
      // recurse
      Iterator childDeps = dep.getDependencies();
      if (childDeps != null)
      {
         while (childDeps.hasNext())
         {
            checkRemoveLocal((PSDependency)childDeps.next(), pkg, resultMap);
         }
      }
         
      Iterator ancs = dep.getAncestors();
      if (ancs != null)
      {
         while (ancs.hasNext())
         {
            checkRemoveLocal((PSDependency)ancs.next(), pkg, resultMap);
         }
      }
   }
   
   /**
    * Map of dependency keys to the matching context.  Key is the dependency key
    * as a <code>String</code>, value is the <code>PSDependencyContext</code>.
    * Never <code>null</code> or modified after construction.
    */
   private Map m_depCtxMap = new HashMap();
 
   /**
    * List of context change listeners, never <code>null</code>, may be be 
    * empty.  Modified by calls to 
    * {@link #addCtxChangeListener(IPSDependencyTreeCtxListener)} and
    * {@link #removeCtxChangeListener(IPSDependencyTreeCtxListener)}.
    */
   private List m_listeners = new ArrayList();
   
   /**
    * Map of packages added to this context.  Key is the dependency key of the
    * package as a <code>String</code>, value is the 
    * <code>PSDeployableElement</code>.  Never <code>null</code>, modified by
    * calls to {@link #addPackage(PSDeployableElement)} and
    * {@link #removePackage(PSDeployableElement, boolean)}
    */
   private Map m_pkgMap = new HashMap();      
   
   /**
    * When assigned, the suppressor is invoked by 
    * {@link #shouldSuppressDependency(PSDependency)} to determine if a specific 
    * dependency should be suppressed from the tree. May be <code>null</code>, 
    * if no dependencies need to be suppressed.
    */
   private IPSDependencySuppressor m_dependencySuppressor = null;
}
