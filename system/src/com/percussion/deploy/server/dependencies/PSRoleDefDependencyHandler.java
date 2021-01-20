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


import com.percussion.conn.PSServerException;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSTransactionSummary;
import com.percussion.deploy.server.PSArchiveHandler;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.deploy.server.PSImportCtx;
import com.percussion.design.objectstore.PSRole;
import com.percussion.design.objectstore.PSRoleConfiguration;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.design.objectstore.server.PSXmlObjectStoreLockerId;
import com.percussion.error.PSException;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.data.PSBackEndRole;
import com.percussion.util.PSIteratorUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class to handle packaging and deploying a role definition.
 */
public class PSRoleDefDependencyHandler extends PSDependencyHandler
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
   public PSRoleDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   // see base class
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      // there is no children for roles
      return PSIteratorUtils.emptyIterator();
    }

   // see base class
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      List roleDeps = new ArrayList();
      
      Iterator<String> roles = ms_roleMgr.getRhythmyxRoles().iterator();
      while (roles.hasNext())
         roleDeps.add(getRoleDep(roles.next()));
            
      return roleDeps.iterator();
   }

   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      PSDependency roleDep = null;
         
      if (doesDependencyExist(tok, id))
         roleDep = getRoleDep(id);
         
      return roleDep;
   }
   
   // see base class
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      List<PSBackEndRole> roles = ms_roleMgr.findRolesByName(id);

      return roles.size() > 0;
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
      
      // don't need to save data other than the role name, which is the dep id
      return PSIteratorUtils.emptyIterator();
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
         
      // check for existing role, and if not found, create a new empty one
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      PSXmlObjectStoreLockerId lockId = null;
      try
      {  
         if (!doesDependencyExist(tok, dep.getDependencyId()))
         {
            lockId = new PSXmlObjectStoreLockerId(ctx.getUserId(), true, true,
                        tok.getUserSessionId());
            os.getServerConfigLock(lockId, 30);
            
            PSRoleConfiguration roleCfg = os.getRoleConfigurationObject(tok);
            roleCfg.getRoles().add(new PSRole(dep.getDependencyId()));            
            os.saveRoleConfiguration(roleCfg, lockId, tok);
            
            addTransactionLogEntry(dep, ctx, dep.getDependencyId(),
               PSTransactionSummary.TYPE_CMS_OBJECT, 
               PSTransactionSummary.ACTION_CREATED);               
         }
         else
         {
            addTransactionLogEntry(dep, ctx, dep.getDependencyId(),
               PSTransactionSummary.TYPE_CMS_OBJECT, 
               PSTransactionSummary.ACTION_SKIPPED_NO_OVERWRITE);
         }
      }
      catch (PSException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }  
      finally
      {
         if (lockId != null)
         {
            try
            {
               os.releaseServerConfigLock(lockId);
            }
            catch(PSServerException e)
            {
               // not fatal
            }
         }
      }      
   }
   
   /**
    * See {@link PSDependencyHandler#overwritesOnInstall()} for details.
    * 
    * @return <code>false</code> as roles are never overwritten.
    */
   public boolean overwritesOnInstall()
   {
      return false;
   }
   
   /**
    * Create a dependency from the supplied role name.
    * 
    * @param name The name of the role, assumed not <code>null</code> or empty.
    * 
    * @return A role dep, never <code>null</code>.
    */
   private PSDependency getRoleDep(String name)
   {
      return createDependency(m_def, name, name);
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
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

   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "RoleDef";

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code>, but may be empty.
    */
   private static List ms_childTypes = new ArrayList();
   
   /**
    * Get the role manager.
    */
   private static IPSBackEndRoleMgr ms_roleMgr = 
      PSRoleMgrLocator.getBackEndRoleManager();
}
