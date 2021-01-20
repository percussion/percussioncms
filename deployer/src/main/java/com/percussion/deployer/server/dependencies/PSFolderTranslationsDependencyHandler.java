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

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.security.PSSecurityToken;
import com.percussion.util.PSIteratorUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class to handle packaging and deploying a folder's translation.
 */
public class PSFolderTranslationsDependencyHandler
   extends PSFolderObjectDependencyHandler
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
    * @throws PSDeployException if any other error occurs.
    */
   public PSFolderTranslationsDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap) throws PSDeployException
   {
      super(def, dependencyMap);
   }

   // see base class
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      return PSIteratorUtils.emptyIterator();
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
      
      String[] parsedId = parseDependencyId(dep.getDependencyId());
      String path = parsedId[0];
      String relType = parsedId[1];
      
      // get relationship config dep
      PSDependencyHandler relHandler = getDependencyHandler(
         PSRelationshipDependencyHandler.DEPENDENCY_TYPE);
         
      PSDependency relDep = relHandler.getDependency(tok, relType);
      if (relDep != null)
         childDeps.add(relDep);         
 
      return childDeps.iterator();
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

      String[] parsedId = parseDependencyId(id);
      String path = parsedId[0];
      String relType = parsedId[1];

      if (pathSpecifiesParent(path))
      {
         PSRelationshipProcessor proc = getRelationshipProcessor(tok);
         PSComponentSummary sum = getFolderSummary(proc, path);
         if (sum != null)
         {
            PSComponentSummaries summ = getTranslatedFolderSummaries(proc, sum, 
               relType);
            if (summ.size() > 0)
               dep = createDependency(m_def, id, relType);
         }
      }
      return dep;
   }

   // see base class
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      // all the info we need is in the dependency tree
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

      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");

      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");


      try
      {
         PSRelationshipProcessor proc = getRelationshipProcessor(tok);
         
         String[] parsedId = parseDependencyId(dep.getDependencyId());
         String parentFolderPath = parsedId[0];
         String relType = parsedId[1];
         
         PSComponentSummary parentFolder = getFolderSummary(proc, 
            parentFolderPath);
         if (parentFolder == null)
         {
            // handle error
            Object[] args = {parentFolderPath, dep.getObjectTypeName(),
               parentFolderPath.substring(
                  parentFolderPath.lastIndexOf(PATH_SEP) + 1)};
            throw new PSDeployException(
               IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND, args);
         }
      
         // delete all current translations
         List deletes = new ArrayList();  
         PSComponentSummaries sums = getTranslatedFolderSummaries(proc, 
            parentFolder, relType);
         if (sums.size() > 0)
         {
            Iterator transFolders = sums.iterator();
            while (transFolders.hasNext())
            {
               PSComponentSummary sum = (PSComponentSummary)transFolders.next();
               deletes.add(sum.getLocator());            
            }

            proc.delete(relType, (PSLocator)parentFolder.getLocator(), deletes);

            // add transaction log entry
            addTransactionLogEntry(dep, ctx, m_def.getObjectTypeName(),
               PSTransactionSummary.TYPE_CMS_OBJECT,
               PSTransactionSummary.ACTION_DELETED);
         }

         // add new translations
         List list = new ArrayList();
         Iterator it = dep.getDependencies();
         while (it.hasNext())
         {
            PSDependency childDep = (PSDependency)it.next();
            
            String childFolderPath = childDep.getDependencyId();
            PSComponentSummary childFolder =
               getFolderSummary(proc, childFolderPath);
            if (childFolder == null)
            {
               // handle error
               Object[] args = {childFolderPath, dep.getObjectTypeName(),
                  childFolderPath.substring(
                     childFolderPath.lastIndexOf(PATH_SEP) + 1)};
               throw new PSDeployException(
                  IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND, args);
            }
            list.add(childFolder.getLocator());
         }
         
         proc.add(relType, list, (PSLocator)parentFolder.getLocator());
            
         // add transaction log entry
         addTransactionLogEntry(dep, ctx,m_def.getObjectTypeName(),
            PSTransactionSummary.TYPE_CMS_OBJECT,
            PSTransactionSummary.ACTION_CREATED);
      }
      catch (PSCmsException ex)
      {
         throw new PSDeployException(ex);
      }
   }
   
   /**
    * Creates a composite id from the owner folder path and the relationship
    * type.
    * 
    * @param folderPath The owner folder path, may not be <code>null</code> or 
    * empty. 
    * @param relationshipType The type of relationship, may not be 
    * <code>null</code> or empty. 
    */
   public static String createDependencyId(String folderPath, 
      String relationshipType)
   {
      if (folderPath == null || folderPath.trim().length() == 0)
         throw new IllegalArgumentException(
            "folderPath may not be null or empty");
      
      if (relationshipType == null || relationshipType.trim().length() == 0)
         throw new IllegalArgumentException(
            "relationshipType may not be null or empty");
      
      return folderPath + ":" + relationshipType;
   }
   
   /**
    * Parse the supplied dependency id into two parts, the folder path and the
    * relationship type.
    * @param depId The dependency id, assumed not <code>null</code> or empty.
    * 
    * @return A <code>String[2]</code> where the folder path is at index 0 and
    * the relationship type is at index 1, never <code>null</code>.
    * 
    * @throws PSDeployException If the id cannot be parsed.
    */
   private static String[] parseDependencyId(String depId) 
      throws PSDeployException
   {
      if (depId == null || depId.trim().length() == 0)
         throw new IllegalArgumentException("depId may not be null or empty");

      // get the parent and child ids
      boolean isValid = true;
      int sepPos = depId.indexOf(":");
      if (sepPos == -1)
         isValid = false;
         
      // test not empty child
      String relType = null;
      String folderPath = null;
      if (isValid)
      {
         folderPath = depId.substring(0, sepPos);
         relType = depId.substring(sepPos + 1);
         
         if (relType.trim().length() == 0 ||
            folderPath.trim().length() == 0)
         {
            isValid = false;
         }
      }      
      
      if (!isValid)
      {
          Object[] args = {depId};
          throw new PSDeployException(
             IPSDeploymentErrors.WRONG_FORMAT_FOR_PAIRID_DEP_ID, args);
      }
      
      return new String[] {folderPath, relType};
   }

   /**
    * Returns the summaries for the folders which are translations of the
    * folder specified by <code>folder</code>.
    *
    * @param proc relationship processor proxy, used for obtaining summaries
    * of translated folders, assumed not <code>null</code>
    *
    * @param folder specifies the folder whose translated folder summaries is
    * to be obtained, assumed not <code>null</code>
    * 
    * @param type The relationship type, assumed to be a type in the translation
    * category, not <code>null</code> or empty. 
    *
    * @return the summary of translated folders, never <code>null</code>,
    * may be empty if the folder specified by <code>folder</code> does not have
    * any translated folders.
    *
    * @throws PSDeployException if any error occurs getting the summary of
    * translated folders.
    */
   private PSComponentSummaries getTranslatedFolderSummaries(
      PSRelationshipProcessor proc, PSComponentSummary folder, String type)
      throws PSDeployException
   {
      PSComponentSummaries summ = new PSComponentSummaries();
      PSLocator owner = (PSLocator)folder.getLocator();
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(owner);
      filter.setName(type);
      filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_TRANSLATION);

      try
      {
         summ = proc.getSummaries(filter, false);         
      }
      catch (PSCmsException ex)
      {
         throw new PSDeployException(ex);
      }
      return summ;
   }
   
   private PSRelationshipSet getTranslatedFolderRelationshipSet(
      PSRelationshipProcessor proc, PSComponentSummary folder, String type)
      throws PSDeployException
   {
      PSRelationshipSet set = null;
      
      PSLocator owner = (PSLocator)folder.getLocator();
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(owner);
      filter.setName(type);
      filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_TRANSLATION);

      try
      {
         set = proc.getRelationships(filter);         
      }
      catch (PSCmsException ex)
      {
         throw new PSDeployException(ex);
      }
      
      return set;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>FolderDef</li>
    * <li>Relationship</li>
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
    * See {@link PSDependencyHandler#shouldDeferInstallation()} for more info.
    *
    * @return <code>true</code>, since translated folders must be installed
    * first.
    */
   public boolean shouldDeferInstallation()
   {
      return true;
   }

   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "FolderTranslations";

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List ms_childTypes = new ArrayList();

   static
   {
      ms_childTypes.add(PSRelationshipDependencyHandler.DEPENDENCY_TYPE);
   }

}

