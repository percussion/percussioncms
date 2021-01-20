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

import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyContext;
import com.percussion.deployer.objectstore.PSDependencyTreeContext;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSValidationResult;
import com.percussion.deployer.objectstore.PSValidationResults;
import com.percussion.deployer.server.dependencies.PSAclDefDependencyHandler;
import com.percussion.deployer.server.dependencies.PSDependencyHandler;
import com.percussion.rx.config.IPSConfigService;
import com.percussion.rx.config.PSConfigServiceLocator;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.utils.PSIdNameHelper;
import com.percussion.services.pkginfo.utils.PSPkgHelper;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Validator class used by the {@link PSValidationJob} for all dependency
 * validation.
 */
@SuppressWarnings(value={"unchecked"})
public class PSDependencyValidator 
{
   /**
    * Construct a dependency validator will all required parameters.
    * 
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param depElem The top level parent element, may not be <code>null</code>.
    * @param ctx The validation context to use, may not be <code>null</code>.
    * @param descriptorName The name of the import descriptor of the package
    * being validated.  May not be <code>null</code>.
    */
   PSDependencyValidator(PSSecurityToken tok, PSDeployableElement depElem,
         PSValidationCtx ctx, String descriptorName)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (depElem == null)
         throw new IllegalArgumentException("depElem may not be null");
      
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      if (descriptorName == null)
         throw new IllegalArgumentException("descriptorName may not be null");
         
      m_tok = tok;
      m_depElem = depElem;
      m_ctx = ctx;
      m_descriptorName = descriptorName;
   }
   
   /**
    * Validates all dependencies in the package against the target server and
    * returns the results.  The rules are as follows:
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Result</TH><TH>Description</TH></TR>
    * <TR><TD><b>error</b></TD>
    *    <TD>
    *       <li>a dependency / object is included in the package and already 
    *           exists on the (target) server, but it was installed with a 
    *           different package (name) </li>
    *       <li>a dependency / object is not included in the package and not 
    *           no exists on (target) server. This can only happen on a   
    *           mis-configured system or caused by a bug</li>
    *    </TD></TR>
    * <TR><TD><b>warning</b></TD>
    *    <TD>
    *       a dependency / object is included in the package and already 
    *       exists on the (target) server, but it was not installed with any 
    *       packages.</li> 
    *    </TD></TR>
    * <TR><TD>valid</TD>
    *    <TD>
    *       <li>a dependency / object is included in the package and already 
    *           exists on the (target) server, but it was installed with the 
    *           same package (name) </li>
    *       <li>a dependency / object is included in the package, but not 
    *           exists on the (target) server.</li>
    *    </TD></TR>
    * </TABLE>
    * </p>
    * 
    * @return The results, never <code>null</code>, but may be empty if there
    * is no error or warning.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public PSValidationResults validate() throws PSDeployException
   {
      // validate local dependencies of any referenced elements at the end to
      // ensure that if the same dependency occurs elsewhere in the tree, it
      // is properly validated first.      
      Iterator deps = m_depElem.getDependencies();
      if (deps != null)
      {
         while (deps.hasNext() && !m_ctx.getJobHandle().isCancelled())
         {
            validateDependency((PSDependency)deps.next());
         }
      }
      
      Iterator locals = m_localDeps.iterator();
      while (locals.hasNext() && !m_ctx.getJobHandle().isCancelled())
      {
         validateDependency((PSDependency)locals.next());
      }
            
      return m_results;
   }

   /**
    * Gathers results from a previous validation of the given dependency.
    * Results will be added for dependencies which are either not auto
    * dependencies or not included in the element.
    * 
    * @param dep The dependency object, assumed not <code>null</code>.
    * @param reallyIncluded <code>true</code> if the dependency is included in
    * the element, <code>false</code> otherwise.
    */
   private void addPreviousResults(PSDependency dep, boolean reallyIncluded)
   {
      // get previous results if any
      PSValidationResult previousResult = m_ctx.getValidationResult(dep);
      if (previousResult != null)
      {
         // don't add overwrite warnings for auto dependencies
         if (!dep.isAutoDependency() || !reallyIncluded)
            m_results.addResult(previousResult);
      }
   }
   
   /**
    * Recursive worker method for <code>validate</code>.
    * 
    * @param dep The dependency to validate, assumed not <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private void validateDependency(PSDependency dep)
      throws PSDeployException
   {
      // immediately defer validation of local deps of a referenced deployable
      // element so that if the same dependency occurs elsewhere within the
      // tree, it is validated first since we won't re-validate the same 
      // dependency twice.
      if (dep instanceof PSDeployableElement)
      {
         // save local dependencies
         Iterator locals = dep.getDependencies(PSDependency.TYPE_LOCAL);
         if (locals != null)
         {
            while (locals.hasNext())
            {
               PSDependency local = (PSDependency)locals.next();
               m_localDeps.add(local);
            }
         }
         return;
      }
      
      PSIdMap idMap = m_ctx.getIdMap();
      IPSJobHandle jobHandle = m_ctx.getJobHandle();
      PSDependencyTreeContext treeCtx = m_ctx.getCurrentTreeCtx();
      PSDependencyManager depMgr = PSDependencyManager.getInstance();
      
      if (jobHandle != null)
         depMgr.updateJobStatus(dep, jobHandle);
      int type = dep.getDependencyType();
      PSDependencyContext depCtx = treeCtx.getDependencyCtx(dep);
      boolean reallyIncluded = depCtx.isIncluded();
      String depKey = dep.getKey();
      
      if (depCtx == null)
      {
         // must have been added, so this is a bug
         throw new RuntimeException("Dependency " + depKey + 
            " not found in treeCtx");
      }
      
      boolean doValidate = reallyIncluded;
      boolean didValidate = false;
            
      // If a dependency appears in multiple places in the package, we don't 
      // need to re-validate if its already been done
      if (m_validatedDeps.contains(depKey))
         doValidate = false;
         
      // if validating for this package and has already been validated for 
      // another package, use that.
      if (doValidate && m_ctx.alreadyValidated(dep))
      {
         addPreviousResults(dep, reallyIncluded);
         
         // add to this package's validated list
         m_validatedDeps.add(depKey);
      }
      else if (doValidate && !jobHandle.isCancelled())
      {
         PSDependency tgtDep = depMgr.getActualDependency(m_tok, dep, idMap);
         boolean exists = (tgtDep != null);

         ResourceBundle bundle = PSDeploymentManager.getBundle();
            
         if (type == PSDependency.TYPE_SERVER || 
            type == PSDependency.TYPE_SYSTEM)
         {
            // just validate it exists
            if (!exists)
            {
               // add error
               PSValidationResult result = new PSValidationResult(dep, true, 
                  bundle.getString("validationMsgDoesNotExist"), false);
               m_results.addResult(result);
            }
            
            didValidate = true;
         }
         else
         {
            // (1) Don't validate included auto dependencies as they aren't really
            //     in the package either. 
            // (2) Some dependency types are never overwritten by design.
            // (3) Skip validating ACLs of any design objects since the 
            //     design objects have or will be validated. Validating ACL
            //     of the design object is not necessary.
            
            if (PSAclDefDependencyHandler.DEPENDENCY_TYPE.equals(dep
                  .getObjectType()))
            {
               if (ms_log.isDebugEnabled())
               {
                  if (dep.getParentDependency() == null)
                     ms_log.debug("Skip validating ACL.");
                  else
                     ms_log.debug("Skip validating ACL for "
                           + dep.getParentDependency().toString()
                           + " dependency.");
               }
               return;
            }
            
            PSDependencyHandler handler = depMgr.getDependencyHandler(dep);
            if (reallyIncluded && !dep.isAutoDependency()
                  && handler.overwritesOnInstall())
            {
               if (exists && reallyIncluded)
               {
                  // see if existing dependency is installed as part of a
                  // different package
                  checkExistingDependency(dep, tgtDep, depMgr, bundle);
               }

               didValidate = true;
            }
            else if (!reallyIncluded && !exists)
            {
               if (!dep.isAssociation())
                  addMissingDependencyResult(dep);
               
               didValidate = true;
            }

            if (didValidate && !jobHandle.isCancelled())
            {
               // add to this package's validated list
               m_validatedDeps.add(dep.getKey());

               // really validate, so add to context list
               m_ctx.addValidatedDependency(dep);
            }
         }
      }
      
      // now validate children - don't do this if current dependency is not a
      // local dependency
      if (type == PSDependency.TYPE_LOCAL)   
      {
         validateChildDependencies(dep);
      }
   }

   /**
    * Validates the child dependencies of the given dependency.  Calls
    * {@link #validateDependency(PSDependency)} for each child dependency found.
    * 
    * @param dep The dependency object, assumed not <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   private void validateChildDependencies(PSDependency dep)
         throws PSDeployException
   {
      Iterator deps = dep.getDependencies();
      if (deps != null)
      {
         while (deps.hasNext() && !m_ctx.getJobHandle().isCancelled())
         {
            validateDependency((PSDependency)deps.next());
         }
      }
   }

   /**
    * Adds error result that the given dependency is missing, not included in 
    * the package and also not exists in the system.
    * <p>
    * Note, this is unexpected and can only happen in a mis-configured system 
    * or caused by a bug.
    * </p>
    * @param dep The dependency object, assumed not <code>null</code>.
    */
   private void addMissingDependencyResult(PSDependency dep)
   {
      PSValidationResult result = new PSValidationResult(dep, true,
            PSDeploymentManager.getBundle().getString(
                  "validationMsgDoesNotExist"), false);
      m_results.addResult(result);
   }

   /**
    * Adds the validation result with the following rules:
    * 
    * <p>
    * If the dependency is included and already exists on the target
    * <ul>
    * <li><b>error</b> if the dependency was installed with a different 
    * package (name)</li>
    * <li><b>warning</b> if the dependency was not installed with any 
    * package (name)</li>
    * <li><b>warning</b> if the dependency was installed with the same package
    * (name) and has been modified outside of allowed configuration.</li>
    * <li><b>no error/warning</b> if the dependency was installed with the same 
    * package (name), but has not been modified outside of allowed
    * configuration or the dependency type is not saved as pkg element.
    * No validation result is added.</li>
    * </ul>
    * </p>
    * 
    * @param dep The dependency object, assumed not <code>null</code>.
    * @param tgtDep The target dependency object, assumed not <code>null</code>.
    * @param depMgr The dependency manager, assumed not <code>null</code>.
    * @param bundle The resource bundle used for locating messages, assumed not
    * <code>null</code>.
    */
   private void checkExistingDependency(PSDependency dep, PSDependency tgtDep,
         PSDependencyManager depMgr, ResourceBundle bundle)
   {
      boolean addError = false;
      String modObjStr = null;
      String otherPkgName = null;
            
      if (dep.supportsParentId())
      {
         // don't care about this type - no error/warn
         return;
      }
      
      // find package elements using target dependency id
      PSPkgElement pkgElem = ms_pkgInfoSvc.findPkgElementByObject(
            PSIdNameHelper.getGuid(tgtDep.getDependencyId(), depMgr.getGuidType(
                  dep.getObjectType())));
      if (pkgElem != null)
      {
         IPSGuid pkgGuid = pkgElem.getPackageGuid();
         PSPkgInfo pkgInfo = ms_pkgInfoSvc.loadPkgInfo(pkgGuid);
         otherPkgName = pkgInfo.getPackageDescriptorName();

         if (pkgInfo.getLastAction() != PSPkgInfo.PackageAction.UNINSTALL)
         {         
            // see if the name of the packages are the same
            if (!otherPkgName.equalsIgnoreCase(m_descriptorName))
            {
               addError = true;
            }
            else
            {
               // element exists as part of the same package, validate
               IPSConfigService srv = PSConfigServiceLocator.getConfigService();
               Collection<String> comms = srv.loadCommunityVisibility(pkgInfo
                     .getPackageDescriptorName());            
               
               List<String> msgs = PSPkgHelper.validatePkgElement(pkgElem,
                     comms);
               if (! msgs.isEmpty())
                  modObjStr = msgs.get(0); 
            }
         }
      }
            
      if (!addError && pkgElem != null && modObjStr == null)
      {
         // was installed with the same package (name) and was not
         // modified outside of allowed configuration - no error/warn
         return;
      }
      
      PSValidationResult result;
      if (addError)
      {
         Object[] args = {otherPkgName};
         String errMsg = MessageFormat.format(bundle
               .getString("validationMsgExistsInAnotherPackage"), args);
         
         // add error - the dependency was installed with different package
         result = new PSValidationResult(dep, true, errMsg, false);
      }
      else
      {
         String warnMsg;
         if (modObjStr == null)
         {
            // add warning - the dependency exists on the (target) server, 
            //               but not installed from any package.
            warnMsg = bundle.getString("validationMsgExists");
         }
         else
         {
            // add warning - the dependency exists on the (target) server as
            //               part of same package and has been modified outside
            //               of configuration.
            warnMsg = bundle.getString("validationMsgModified");
         }
         
         result = new PSValidationResult(dep, false, warnMsg, true);
      }
      
      m_results.addResult(result);
   }
   
   /**
    * The security token to use if objectstore access is required.  Initialized
    * in ctor.  Never <code>null</code> after that.
    */
   private PSSecurityToken m_tok;
   
   /**
    * The results to which each warning or error is added.  Never
    * <code>null</code>.
    */   
   private PSValidationResults m_results = new PSValidationResults();
   
   /**
    * The top level parent element.  Initialized in ctor.  Never
    * <code>null</code> after that.
    */
   private PSDeployableElement m_depElem;
   
   /**
    * The validation context to use.  Initialized in ctor.  Never
    * <code>null</code> after that.
    */
   private PSValidationCtx m_ctx;
   
   /**
    * If a deployable element is encountered, its local dependencies are added
    * to this list to be validated at the end.  Never <code>null</code>.
    */
   private List<PSDependency> m_localDeps = new ArrayList<PSDependency>();
   
   /**
    * Set of dependency keys that have been validated for the current package.
    * Never <code>null</code>, each dependency validated by this method is
    * added to this list. Used to avoid re-validating dependencies that appear
    * multiple times in the same package.
    */
   private Set<String> m_validatedDeps = new HashSet<String>();
   
   /**
    * The name of the import descriptor corresponding to the package being
    * validated.  Initialized in ctor.  Never <code>null</code> after that.
    */
   private String m_descriptorName; 
   
   /**
    * Static reference to the package info service, never <code>null</code>.
    */
   private static IPSPkgInfoService ms_pkgInfoSvc = PSPkgInfoServiceLocator
         .getPkgInfoService();

   /**
    * Logger for the site manager
    */
   static Log ms_log = LogFactory.getLog("PSDependencyValidator");

}
