/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.deployer.server.dependencies;


import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import com.percussion.error.PSExceptionUtils;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.collections.PSIteratorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class to handle packaging and deploying a folder's relationships to its child
 * content items.
 */
public class PSFolderContentsDependencyHandler
   extends PSFolderObjectDependencyHandler
{

   private static final Logger log = LogManager.getLogger(PSFolderContentsDependencyHandler.class);

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
    * @throws PSDeployException if any other error occurs.
    */
   public PSFolderContentsDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap) throws PSDeployException
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

      List childDeps = new ArrayList();

      PSComponentSummary sum = getFolderSummary(getRelationshipProcessor(tok),
         dep.getDependencyId());
      if (sum != null)
      {
         PSDependencyHandler handler = getDependencyHandler(
            PSContentDefDependencyHandler.DEPENDENCY_TYPE);
         Iterator sums = getChildItemSummaries(getRelationshipProcessor(tok),
            sum.getCurrentLocator());
         while (sums.hasNext())
         {
            PSComponentSummary itemSum = (PSComponentSummary)sums.next();
            PSDependency itemDep=null;
            try {
                itemDep = handler.getDependency(tok, String.valueOf(
                       itemSum.getCurrentLocator().getId()));
            } catch (PSNotFoundException e) {
               log.warn(PSExceptionUtils.getMessageForLog(e));
               log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            }

            if (itemDep != null)
            {
               itemDep.setDependencyType(PSDependency.TYPE_LOCAL);
               childDeps.add(itemDep);
            }
         }
      }

      return childDeps.iterator();
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

      PSComponentSummary sum = getFolderSummary(getRelationshipProcessor(tok), id);
      if (sum != null)
      {
         Iterator sums = getChildItemSummaries(getRelationshipProcessor(tok),
            sum.getCurrentLocator());
         if (sums.hasNext())
            dep = createDependency(m_def, id, sum.getName());
      }

      return dep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>ContentItem</li>
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

      List files = new ArrayList();

      // get all child item summaries and save them
      PSComponentSummary sum = getFolderSummary(getRelationshipProcessor(tok),
         dep.getDependencyId());
      if (sum != null)
      {
         Iterator sums = getChildItemSummaries(getRelationshipProcessor(tok),
            sum.getCurrentLocator());
         while (sums.hasNext())
         {
            PSComponentSummary itemSum = (PSComponentSummary)sums.next();
            files.add(createDependencyFile(itemSum));
         }
      }

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

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

         try
         {
            String path = dep.getDependencyId();
            
            // need to disable the managed nav folder effect when saving folder
            // contents
            Map params = new HashMap();
            params.put(IPSHtmlParameters.RXS_DISABLE_NAV_FOLDER_EFFECT, "y");
            PSRelationshipProcessor proc = getRelationshipProcessor(tok, 
               params);
            
            // get target folder locator
            PSComponentSummary tgtFolderSum = getFolderSummary(proc, path);
            if (tgtFolderSum == null)
            {
               Object[] args = {dep.getDependencyId(), dep.getObjectTypeName(),
                  dep.getDisplayName()};
               throw new PSDeployException(
                  IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND, args);
            }
            PSLocator tgtFolderLoc = tgtFolderSum.getCurrentLocator();

            // delete all current items
            List deletes = new ArrayList();
            Iterator sums = getChildItemSummaries(proc, tgtFolderLoc);
            while (sums.hasNext())
            {
               PSComponentSummary itemSum = (PSComponentSummary)sums.next();
               deletes.add(itemSum.getLocator());
            }

            proc.delete(PSRelationshipConfig.TYPE_FOLDER_CONTENT, tgtFolderLoc, 
               deletes);

            // add transaction log entry
            addTransactionLogEntry(dep, ctx,m_def.getObjectTypeName(),
               PSTransactionSummary.TYPE_CMS_OBJECT,
               PSTransactionSummary.ACTION_DELETED);

            // now add new relationships
            List adds = new ArrayList();
            Iterator files = getDependencyFilesFromArchive(archive, dep);
            while (files.hasNext())
            {
               Element root = getElementFromFile(archive, dep,
                  (PSDependencyFile)files.next());
               PSComponentSummary srcSum = new PSComponentSummary(root);
               PSLocator srcLoc = srcSum.getCurrentLocator();
               int childId = srcLoc.getId();
               PSIdMapping mapping = getIdMapping(ctx, String.valueOf(childId),
                  PSContentDefDependencyHandler.DEPENDENCY_TYPE);
               if (mapping != null)
               {
                  try
                  {
                     childId = Integer.parseInt(mapping.getTargetId());
                  }
                  catch (NumberFormatException e)
                  {
                     Object[] args =
                     {
                        PSContentDefDependencyHandler.DEPENDENCY_TYPE,
                        String.valueOf(childId),
                        ctx.getCurrentIdMap().getSourceServer(),
                        mapping.getTargetId()
                     };
                     throw new PSDeployException(
                        IPSDeploymentErrors.INVALID_ID_MAPPING_TARGET, args);
                  }
               }
               adds.add(new PSLocator(childId, srcLoc.getRevision()));
            }

            proc.add(FOLDER_TYPE, PSRelationshipConfig.TYPE_FOLDER_CONTENT, 
               adds, tgtFolderLoc);

            // add transaction log entry
            addTransactionLogEntry(dep, ctx,m_def.getObjectTypeName(),
               PSTransactionSummary.TYPE_CMS_OBJECT,
               PSTransactionSummary.ACTION_CREATED);

         }
      catch (PSCmsException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }

   /**
    * See {@link PSDependencyHandler#shouldDeferInstallation()} for more info.
    *
    * @return <code>true</code>, since child items must be installed first.
    */
   public boolean shouldDeferInstallation()
   {
      return true;
   }

   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = 
      IPSDeployConstants.DEP_OBJECT_TYPE_FOLDER_CONTENTS;

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List ms_childTypes = new ArrayList();

   static
   {
      ms_childTypes.add(PSContentDefDependencyHandler.DEPENDENCY_TYPE);
   }


}
