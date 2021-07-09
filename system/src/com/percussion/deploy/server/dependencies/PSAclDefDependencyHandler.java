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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.deploy.server.dependencies;

import java.security.acl.NotOwnerException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.percussion.deploy.client.IPSDeployConstants;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDependencyFile;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.objectstore.PSIdMapping;
import com.percussion.deploy.server.PSArchiveHandler;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.deploy.server.PSDeploymentHandler;
import com.percussion.deploy.server.PSImportCtx;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.security.IPSTypedPrincipal;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;

/**
 * Class to handle packaging and deploying an ACL definition.
 */
public class PSAclDefDependencyHandler extends PSDependencyHandler
{

   /**
    * Construct the dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSAclDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   // see base class
   @Override
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");


      PSAclImpl acl = findAclByDependencyID(id);
      return (acl != null) ? true : false;
   }

   

   // see base class
   @Override
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      List<PSDependency> childDeps = new ArrayList<>();
      PSAclImpl acl = findAclByDependencyID(dep.getDependencyId());

      if ( acl != null )    
      {
         Iterator<IPSAclEntry> it = acl.getEntries().iterator();
         PSDependencyHandler ch = 
            getDependencyHandler(PSCommunityDependencyHandler.DEPENDENCY_TYPE);
         PSDependencyHandler rh = 
            getDependencyHandler(PSRoleDefDependencyHandler.DEPENDENCY_TYPE);
         while (it.hasNext())
         {
            PSDependency d = null;
            PSAclEntryImpl e = (PSAclEntryImpl)it.next();
            String name = e.getName();
            if ( e.getType() ==  IPSTypedPrincipal.PrincipalTypes.ROLE)
            {
               d = rh.getDependency(tok, name);
               if ( d != null )
                  childDeps.add(d);
            }  
            else if ( e.getType() == IPSTypedPrincipal.PrincipalTypes.COMMUNITY)
            {
               if ( !e.getName().equals(PSTypedPrincipal.ANY_COMMUNITY_ENTRY))
               {
                  IPSBackEndRoleMgr mgr = 
                     PSRoleMgrLocator.getBackEndRoleManager();
                  List<PSCommunity> cList = mgr.findCommunitiesByName(name);
                  if ( cList.size() > 0 )
                  {
                     PSCommunity c = cList.get(0); 
                     d = ch.getDependency(tok, String.valueOf(c.getGUID()
                           .getUUID()));
                     if ( d != null )
                        childDeps.add(d);
                  }
               }
            }  
         }
      }
      return childDeps.iterator();
   }

   /**
    * Utility method to find the ACL by a given guid(as a STRINGGGGGG)
    * @param depId the guid
    * @return <code>null</code> if ACLEntry not found
    * @throws PSDeployException
    * 
    */
   private PSAclImpl findAclByDependencyID(String depId)
         throws PSDeployException
   {
      if (depId == null || depId.trim().length() == 0)
         throw new IllegalArgumentException(
               "dependency ID may not be null or empty");
      PSGuid guid = new PSGuid(PSTypeEnum.ACL, PSDependencyUtils
            .getGuidValFromString(depId, m_def.getObjectTypeName())); 
      PSAclImpl acl  = null;
      try
      {
         acl = (PSAclImpl) m_aclSvc.loadAcl(guid);
      }
      catch (PSSecurityException e)
      {
      }
      return acl;
   }

   
   // see base class
   // return an empty set, since you donot want to catalog all the existing
   // ACLs
   @Override
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      return new ArrayList<PSDependency>().iterator();
   }


   /**
    * acl dependency is returned based on ACL dep id (**NOT ITS PARENT**)
    * 
    * @param tok
    * @param id  the guid for the acl dependency
    * @throws PSDeployException 
    */
   @Override
   public PSDependency getDependency(PSSecurityToken tok, String id)
         throws PSDeployException
   {
      PSDependency dep = null;
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      PSAclImpl acl;
      try
      {
         acl = findAclByDependencyID(id);
      }
      catch (PSDeployException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Unable to generate a dependency file for ACLEntry:"
               + id);
      }
      if ( acl != null )
      {
         dep = createDependency(m_def, ""
               + acl.getGUID().longValue(), acl.getName());
         dep.setDependencyType(PSDependency.TYPE_LOCAL);
      }
      return dep;
    }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Slot</li>
    * <li>ContentTyp</li>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   @Override
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   // see base class
   @Override
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   // see base class
   @Override
   public void reserveNewId(PSDependency dep, PSIdMap idMap)
      throws PSDeployException
   {
      PSDependencyUtils.reserveNewId(dep, idMap, getType());   
   }

/**
    * Creates a dependency file from a given dependency data object.
    * @param acl actual acl, may or may not be <code>null</code>.
    * If the acl is present, deserialize 
    * @return The dependency file object, it will never be <code>null</code>.
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   private PSDependencyFile getDepFileFromAcl(PSAclImpl acl)
      throws PSDeployException
   {
      if (acl == null)
         throw new IllegalArgumentException("acl may not be null");
      String str;
      try
      {
         str = acl.toXML();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Unable to generate a dependency file for ACLEntry:"
                     + acl.getName());
      }
      
      return new PSDependencyFile(PSDependencyFile.TYPE_SERVICEGENERATED_XML,
            createXmlFile(IPSDeployConstants.XML_HDR_STR + str));
   }

   // see base class
   @Override
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      // pack the data into the files
      List<PSDependencyFile> files = new ArrayList<>();

      PSAclImpl acl = findAclByDependencyID(dep.getDependencyId());

      if ( acl != null )    
         files.add(getDepFileFromAcl(acl));
      return files.iterator();
   }
   
   /**
    * Return an iterator for dependency files in the archive
    * @param archive The archive handler to retrieve the dependency files from,
    *           may not be <code>null</code>.
    * @param dep The dependency object, may not be <code>null</code>.
    * 
    * @return An iterator one or more <code>PSDependencyFile</code> objects.
    *         It will never be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there is no dependency file in the archive
    *            for the specified dependency object, or any other error occurs.
    */
   protected static Iterator getAclDependecyFilesFromArchive(
         PSArchiveHandler archive, PSDependency dep) throws PSDeployException
   {
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      Iterator files = archive.getFiles(dep);

      if (!files.hasNext())
      {
         Object[] args =
         {PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_SERVICEGENERATED_XML],
               dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName()};
         throw new PSDeployException(
               IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }
      return files;
   }

   /**
    * Extract the ACL definition file from the archive and install/update
    * the <br>
    * 
    * @param archive the ArchiveHandler to use to retrieve the files from the
    *           archive, may not be <code>null</code>
    * @param depFile the PSDependencyFile that was retrieved from the archive
    *           may not be <code>null</code>
    * @param acl if not <code>null</code>, use it for deserialization
    *           else ask service to create a new template
    * @return the actual template
    * @throws PSDeployException
    */
   private PSAclImpl generateAclFromFile(PSArchiveHandler archive,
         PSDependencyFile depFile, PSAclImpl acl) throws PSDeployException
   {
      PSAclImpl tmp = null;
     
      String tmpStr = PSDependencyUtils.getFileContentAsString(
            archive, depFile);

      if ( acl == null )
         tmp = new PSAclImpl();
      else 
         tmp = acl;
      try
      { 
         tmp.fromXML(tmpStr);
      }
      catch (Exception e)
      {
         String err = e.getLocalizedMessage();
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Could not create acl from file:" + depFile.getFile().getName()
                     + " Error was:\n" + err);
      }
      return tmp;
   }

   /**
    * Transform idmappings 
    * 
    * @param tok the security token never <code>null</code>
    * @param archive the archive handler never <code>null</code>
    * @param dep the dependency never <code>null</code>
    * @param ctx import context never <code>null</code>
    * @param acl the acl that needs to be deployed never <code>null</code>
    */
   private void doTransforms(PSSecurityToken tok, PSArchiveHandler archive,
         PSDependency dep, PSImportCtx ctx, PSAclImpl acl)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dependency may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("import context may not be null");
      if (acl == null)
         throw new IllegalArgumentException("acl definition may not be null");

      PSTypeEnum type = PSTypeEnum.valueOf(acl.getObjectType());
      
      List<String> depTypes = new ArrayList<>();
      for (String t : PSDeploymentHandler.getInstance()
         .getDependencyManager().getDeploymentType(type))
      {
         PSDependencyDef def = m_map.getDependencyDef(t);
         if (def == null)
            throw new RuntimeException(
               "No type def found in map for type :" + t);
         
         if (def.supportsIdMapping())
            depTypes.add(t);
      }
      
      if ( depTypes.size() == 0 )
         return;
      
      PSIdMapping m = null;
      PSDeployException dex = null;
      
      for (String t : depTypes)
      {
         try
         {
            m = getIdMapping(ctx, String.valueOf(acl.getObjectGuid().getUUID()),
               t);
            dex = null;
            break;
         }
         catch (PSDeployException ex)
         {
            dex = ex;
         }
      }
      if ( m == null && dex != null )
      {
         throw new PSDeployException(dex.getErrorCode(), dex.getLocalizedMessage());
      }
      
      
      if (m != null && m.getTargetId() != null)
      {
         IPSGuid g = new PSGuid(type, m.getTargetId());
         acl.setObjectId(g.getUUID());
      }      
   }
   
   
   // see base class
   @Override
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

      // retrieve datas, acl data
      Iterator files = getAclDependecyFilesFromArchive(archive, dep);
      while (files.hasNext())
      {
         PSDependencyFile depFile = (PSDependencyFile) files.next();
         PSAclImpl tmp = generateAclFromFile(archive, depFile, null);
         IPSGuid originalGuid = tmp.getGUID();
         doTransforms(tok, archive, dep, ctx, tmp);
         //  Hack to remove old ids and version fields so we can match up based upon object guid correctly 
         
         PSAclImpl tmp2 = new PSAclImpl(tmp);
         
         
         
         List<IPSAcl> aclList = new ArrayList<>();
         aclList.add(tmp2);
         boolean requireSave = false;
         try
         {
            IPSAcl acl = m_aclSvc.loadAclForObject(originalGuid);
            if (acl==null)
            {
               requireSave = true;
               // Create new ACL for object and do not dirty with acl from other system
               // Communities set with Package Manager.
               acl = createNewAcl(tmp);
               
            }

            // only map community permissions ignore all other user access permissions
            requireSave |= updateCommunityPermissions(tmp, acl);

            if (requireSave) {
               aclList.add(acl);
               m_aclSvc.saveAcls(aclList);
            }
         }
         catch (PSSecurityException e)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e,
                  "Could not install the ACL: " + tmp.getName() + " for object "+tmp.getObjectGuid().toString());
         }
         catch (NotOwnerException e)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e,
                  "Could not install the ACL: " + tmp.getName() + " for object "+tmp.getObjectGuid().toString() + " acl owner not correct");

         }
      }
   }

   private IPSAcl createNewAcl(PSAclImpl tmp)
   {
      IPSAcl acl;
      PSTypedPrincipal principal = new PSTypedPrincipal(PSAclEntry.DEFAULT_USER_NAME,
            PrincipalTypes.USER);
      acl =  m_aclSvc.createAcl(tmp.getObjectGuid(), principal);
      
      PSAclEntryImpl defaultOwner = 
         (PSAclEntryImpl) acl.findEntry(principal);
      
      defaultOwner.addPermission(PSPermissions.READ);
      defaultOwner.addPermission(PSPermissions.UPDATE);
      defaultOwner.addPermission(PSPermissions.DELETE);
      return acl;
   }

   private boolean updateCommunityPermissions(PSAclImpl tmp, IPSAcl acl) throws NotOwnerException
   {
      boolean changed = false;
      IPSTypedPrincipal owner = acl.getFirstOwner();
      for (IPSAclEntry entry : tmp.getEntries())
      {
         IPSTypedPrincipal principal = entry.getTypedPrincipal();
         if (principal.isCommunity())
         {
            boolean visible = entry.checkPermission(PSPermissions.RUNTIME_VISIBLE);
            
            IPSAclEntry existingEntry = acl.findEntry(principal);
            if (visible)
            {
               if (existingEntry == null )
               {
                  changed |= true;
                  existingEntry =
                        acl.createEntry(entry.getName(), PrincipalTypes.COMMUNITY);
                  existingEntry.addPermission(PSPermissions.RUNTIME_VISIBLE);
                    
                  acl.addEntry(owner, existingEntry);
                   
         }
               
               if (!existingEntry.checkPermission(PSPermissions.RUNTIME_VISIBLE))
               {
                  changed |= true;
                  existingEntry.addPermission(PSPermissions.RUNTIME_VISIBLE);
      }
            } else if (existingEntry!=null)
            {
                  changed |= true;
                  acl.removeEntry(owner,existingEntry);
            }
           
         }
      }
      return changed;
   }


   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "AclDef";

    /**
    * Da assembly Service Helper
    */
   private static IPSAclService m_aclSvc = PSAclServiceLocator.getAclService();
   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<>();

   static
   {
      ms_childTypes.add(PSRoleDefDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSCommunityDependencyHandler.DEPENDENCY_TYPE);
   }

}
