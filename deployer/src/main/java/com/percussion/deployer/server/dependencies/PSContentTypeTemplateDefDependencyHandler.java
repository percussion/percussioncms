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
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.data.PSGuid;

import javax.jcr.RepositoryException;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class to handle packaging and deploying a template definition.
 * Adds relations to PSX_CONTENTTYPE_TEMPLATE table
 */

public class PSContentTypeTemplateDefDependencyHandler
      extends
         PSDependencyHandler
{
   
   /**
    * Construct the dependency handler.
    *
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be
    * <code>null</code>.
    * @throws IllegalArgumentException if any param is invalid.
    */

   public PSContentTypeTemplateDefDependencyHandler(PSDependencyDef def,
         PSDependencyMap dependencyMap) 
   {
      super(def, dependencyMap);
   }


   
   
   // see base class
   // ctID is a PSGuid for a ContentType
   // parentType is the TEMPLATE
   // parentID  is the template ID ( PSGuid )
   public PSDependency getDependency(PSSecurityToken tok, String ctID, 
         String parentID, String parentName )
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (ctID == null || ctID.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      if (parentName == null || parentName.trim().length() == 0)
         throw new IllegalArgumentException("parentType may not be null or empty");
      
      if (parentID == null || parentID.trim().length() == 0)
         throw new IllegalArgumentException("parentID may not be null or empty");
      
      // template ids are GUIDs, make sure the id is a guid.
      long templateGuid = -1;
      long ctypeGuidVal = -1;
      PSGuid ctypeGuid = null;
      PSGuid tmpGuid   = null;
      try {
         templateGuid = Long.parseLong(parentID);
         tmpGuid      = new PSGuid(PSTypeEnum.TEMPLATE, templateGuid);
      }
      catch ( NumberFormatException ne)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "ContentTypeTemplate Definition was expecting a long value: "
                     + parentID);
      }
      // parse the ContentType GUID
      try {
         ctypeGuidVal = Long.parseLong(ctID);
         ctypeGuid = new PSGuid(PSTypeEnum.NODEDEF, ctypeGuidVal);
      }
      catch ( NumberFormatException ne)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "ContentTypeTemplate Definition was expecting a long value: "
                     + ctID);
      }
      
      // Now build the dependency
      PSDependency dep = null;
      IPSContentMgr contentMgr = PSContentMgrLocator.getContentMgr();
      List<IPSNodeDefinition> nodeDefs;
      try
      {
         nodeDefs = contentMgr.findAllItemNodeDefinitions();
      }
      catch (RepositoryException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               "RepositoryException occurred");
      }
      Iterator<IPSNodeDefinition> it = nodeDefs.iterator();
      IPSNodeDefinition def = null;
      while ( it.hasNext() )
      {
         def = it.next();
         if ( def.getGUID().equals(ctypeGuid) )
         {
            break;
         }
      }
      if ( def != null )
      {
         // This dependency contains a template guid as ID and ContentType
         // i.e. NodeDef as the name.
         // so that when installing the dependency, the PSAssemblyService can 
         // locate template and PSContentMgr service can locate the contentType
         // by name
         dep = createDependency(m_def, ""+tmpGuid.longValue(), def.getName());
         if ( dep != null )
            dep.setDependencyType(PSDependency.TYPE_LOCAL);
      }
      return dep;
   }

   //see base class
   // Empty Implementation
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      List<PSDependency> childDeps = new ArrayList<PSDependency>();
      
      return childDeps.iterator();
   }
 
   // see base class
   // Empty Implementation
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok) throws PSDeployException
   {    
      List<PSDependency> deps = new ArrayList<PSDependency>(); 
      return deps.iterator();
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


      // pack the data into the files
      List<PSDependencyFile> files = new ArrayList<PSDependencyFile>();

      files.add(getEmptyDepFile());
      return files.iterator();
   }

   /**
    * Creates a dummy dependency file from a given dependency data object.
    * @return The dependency file object, it will never be <code>null</code>.
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected PSDependencyFile getEmptyDepFile()
      throws PSDeployException
   {
      String str = "<EMPTY></EMPTY>";
      
      return new PSDependencyFile(PSDependencyFile.TYPE_SERVICEGENERATED_XML,
            createXmlFile(XML_HDR_STR + str));
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

      // retrieve dependency info
      // template ids are GUIDs, make sure the id is a guid.
      long templateGuid = -1;
      PSGuid tmpGuid   = null;
      try {
         templateGuid = Long.parseLong(dep.getDependencyId());
         tmpGuid      = new PSGuid(PSTypeEnum.TEMPLATE, templateGuid);
      }
      catch ( NumberFormatException ne)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "ContentTypeTemplate Definition was expecting a long value: "
                     + dep.getDependencyId());
      }
      
      
      IPSContentMgr contentMgr = PSContentMgrLocator.getContentMgr();
      try
      {
         IPSNodeDefinition nodeDef = contentMgr.findNodeDefinitionByName(dep
               .getDisplayName());
         nodeDef.addVariantGuid(tmpGuid);
         List<IPSNodeDefinition> newList = new ArrayList<IPSNodeDefinition>();
         newList.add(nodeDef);
         contentMgr.saveNodeDefinitions(newList);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Repository Exception occurred while installing a dependency " +
               dep.getDisplayName() + ". Error was " + e.getLocalizedMessage());
      }
   }

   /**
    * @param archive the ArchiveHandler to use to retrieve the files from the 
    * archive, may not be <code>null</code> 
    * @param depFile the PSDependencyFile that was retrieved from the archive
    * may not be <code>null</code>
    * @return the actual template
    * @throws PSDeployException
    */
   protected IPSAssemblyTemplate generateTemplateFromFile(
         PSArchiveHandler archive, PSDependencyFile depFile)
         throws PSDeployException
   {
      IPSAssemblyTemplate tmp = null;
      File f = depFile.getFile();

      String tmpStr = PSDependencyUtils.getFileContentAsString(
            archive, depFile);
      tmp = new PSAssemblyTemplate();
      try
      {
         tmp.fromXML(tmpStr);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               "Could not create template from file:" + f.getName());
      }
      return tmp;
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
   protected Iterator getTemplateDependecyFilesFromArchive(
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

   
   
   // see base class
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
    * A util header for templates. IPSAssemblyTemplate upon serialization will
    * not have this header. Just prepend it.
    */
   private static final String XML_HDR_STR = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
   /**
    * Constant for this handler's supported type
    */
   public static final String DEPENDENCY_TYPE = "ContentTypeTemplateDef";

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();
}
