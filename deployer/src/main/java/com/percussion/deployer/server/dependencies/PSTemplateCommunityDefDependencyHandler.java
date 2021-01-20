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
import com.percussion.deployer.objectstore.PSDependencyData;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.data.PSGuid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class to handle packaging and deploying a **PART OF** template definition.
 * Adds definitions to RXVARIANTCOMMUNITY table
 */

public class PSTemplateCommunityDefDependencyHandler
      extends  PSDataObjectDependencyHandler
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
    * @throws PSDeployException if any other error occurs.
    */

   public PSTemplateCommunityDefDependencyHandler(PSDependencyDef def,
         PSDependencyMap dependencyMap) 
   {
      super(def, dependencyMap);
   }


   
   
   // see base class
   // Given a templateID, locate all the communities that it is registered to
   // tempID is a PSGuid for a Template
   // parentType is the TEMPLATE
   // parentID  is the template ID ( PSGuid )
/*   public PSDependency getDependency(PSSecurityToken tok, String tempID)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (tempID == null || tempID.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      // template ids are GUIDs, make sure the id is a guid.
      long templateGuid = -1;
      long ctypeGuidVal = -1;
      PSGuid ctypeGuid = null;
      PSGuid tmpGuid   = null;
      try {
         templateGuid = Long.parseLong(tempID);
         tmpGuid      = new PSGuid(PSTypeEnum.TEMPLATE, templateGuid);
      }
      catch ( NumberFormatException ne)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "ContentTypeTemplate Definition was expecting a long value: "
                     + tempID);
      }

      PSDependency dep = getDependency(tok, ""+templateGuid, 
            VARCOMMUNITYTABLE, TEMPLATEID, COMMUNITYID);
      if ( dep != null )
         dep.setDependencyType(PSDependency.TYPE_LOCAL);

      return dep;
   }
*/
   
   public PSDependency getDependency(PSSecurityToken tok, String commID)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (commID == null || commID.trim().length() == 0)
         throw new IllegalArgumentException("community ID may not be null or empty");
      PSDependency dep = null;
      dep = createDependency(m_def, commID, commID);
      if ( dep != null )
         dep.setDependencyType(PSDependency.TYPE_LOCAL);

      return dep;
   }
   
   
   //see base class
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      String templateID = dep.getDependencyId();

      List childDeps = getChildDepsFromParentID(VARCOMMUNITYTABLE,
            COMMUNITYID, TEMPLATEID, templateID,
          PSTemplateCommunityDefDependencyHandler.DEPENDENCY_TYPE, tok);

      return childDeps.iterator();
   }
 
   // see base class
   // Empty Implementation
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
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
      // get the first dep data for the slot object of itself
      PSDependencyData vrData = getDepDataFromTable(dep, VARCOMMUNITYTABLE,
            TEMPLATEID, true);

      files.add(getDepFileFromDepData(vrData));
      if ( vrData != null )
         files.add(getDepFileFromDepData(vrData));

      return files.iterator();
   }

   /**
    * Creates a dummy dependency file from a given dependency data object.
    *
    * @param depData The dependency data object, may not be <code>null</code>.
    *
    * @return The dependency file object, it will never be <code>null</code>.
    *
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
               "Repository Exception occurred while installing a dependency" +
               dep.getDisplayName());
      }
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
      
   
   private static final String COMMUNITYID = "COMMUNITYID";
   private static final String VARCOMMUNITYTABLE="RXVARIANTCOMMUNITY";
   private static final String TEMPLATEID = "VARIANTID";
   
   /**
    * A util header for templates. IPSAssemblyTemplate upon serialization will
    * not have this header. Just prepend it.
    */
   private static final String XML_HDR_STR = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
   /**
    * Constant for this handler's supported type
    */
   public static final String DEPENDENCY_TYPE = "TemplateCommunityDef";

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();
   static
   {
      ms_childTypes.add(PSCommunityDependencyHandler.DEPENDENCY_TYPE);
   }
}
