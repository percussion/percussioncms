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
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSSecurityToken;
import com.percussion.util.IPSHtmlParameters;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Base class for all dependency handlers dealing with <code>PSFolder</code>
 * objects.  Provides common functionality required by these handlers.
 */
public abstract class PSFolderObjectDependencyHandler
   extends PSCmsObjectDependencyHandler
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
   public PSFolderObjectDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap) throws PSDeployException
   {
      super(def, dependencyMap);
   }

   // see base class
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");

      List<PSDependencyFile> files = new ArrayList<PSDependencyFile>();

      PSFolder folder = getFolderObject(getRelationshipProcessor(tok),
         getComponentProcessor(tok), dep.getDependencyId());
      if (folder != null)
         files.add(createDependencyFile(folder));

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

      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");

      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      try
      {
         // restore the file
         Iterator files = getDependencyFilesFromArchive(archive, dep);
         Element root = getElementFromFile(archive, dep,
            (PSDependencyFile)files.next());
         PSFolder srcFolder = new PSFolder(root);

         // clone the source to lose the keys and persisted state
         PSFolder newFolder = (PSFolder)srcFolder.clone();

         // see if target exists and if so, update with source folder
         Map<String, String> params = new HashMap<String, String>();
         // disable nav folder effect when creating folder relationships
         params.put(IPSHtmlParameters.RXS_DISABLE_NAV_FOLDER_EFFECT, "y");
         PSRelationshipProcessor relProc = getRelationshipProcessor(tok, 
            params);
         PSComponentProcessorProxy compProc = getComponentProcessor(tok);
         String path = dep.getDependencyId();
         PSFolder tgtFolder = getFolderObject(relProc, compProc, path);
         PSComponentSummary parentSum = null;
         if (tgtFolder != null)
         {
            tgtFolder.mergeFrom(newFolder);
            newFolder = tgtFolder; 
         }
         else if (pathSpecifiesParent(path))
         {
            // be sure we can get parent or we can't save
            parentSum = getParentFolderSummary(relProc, path);
            if (parentSum == null)
            {
               // handle error
               Object[] args = {path, dep.getObjectTypeName(),
                  path.substring(0, path.lastIndexOf(PATH_SEP))};
               throw new PSDeployException(
                  IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND, args);
            }
         }

         // save folder
         newFolder = (PSFolder)compProc.save(
            new PSFolder[] {newFolder}).getResults()[0];

         // if inserted, save relationship to its parent if there is one
         int action;
         if (tgtFolder == null)
         {
            action = PSTransactionSummary.ACTION_CREATED;
            if (parentSum != null)
            {
               List<PSLocator> children = new ArrayList<PSLocator>(1);
               children.add(newFolder.getLocator());
               relProc.add(FOLDER_TYPE, null, children, parentSum.getLocator());
            }
         }
         else
            action = PSTransactionSummary.ACTION_MODIFIED;

         // add the transaction to the log
         addTransactionLogEntry(dep, ctx, newFolder, action);
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
    * Get a folder path from a given folder id.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param folderId The folder id, may not be <code>null</code> or empty.
    * 
    * @return The path, or <code>null</code> if the id does not locate a folder,
    * never empty.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public String getFolderPathFromId(PSSecurityToken tok, String folderId)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (folderId == null || folderId.trim().length() == 0)
         throw new IllegalArgumentException(
            "folderId may not be null or empty");
      
      try
      {
         String path = null;

         // get folder summary
         PSComponentProcessorProxy proc = getComponentProcessor(tok);
         Element[] sumEls = proc.load(PSDbComponent.getComponentType(
            PSComponentSummaries.class), new PSKey[] {new PSLocator(folderId)});
         
         PSComponentSummaries sums = new PSComponentSummaries(sumEls);
         if (!sums.isEmpty())
         {
            // can only get one for a folder
            PSComponentSummary sum = (PSComponentSummary)sums.iterator().next();
            path = getFolderPath(getRelationshipProcessor(tok), sum);
         }
         
         return path;
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
    * Get a folder id from a given folder path.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param path The folder path, may not be <code>null</code> or empty.
    * 
    * @return The id, or <code>null</code> if the path does not locate a folder,
    * never empty.  
    * 
    * @throws PSDeployException if there are any errors.
    */
   public String getFolderIdFromPath(PSSecurityToken tok, String path) 
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");
      
      String id = null;
      
      // get folder summary
      PSRelationshipProcessor proc = getRelationshipProcessor(tok);
      PSComponentSummary sum = getFolderSummary(proc, path);
      if (sum != null)
      {
         id = String.valueOf(sum.getCurrentLocator().getId());
      }
      
      return id;
   }

   /**
    * Gets all child dependencies.  Gets all child folders and creates
    * dependencies using the specified <code>folderDepType</code>.  Also gets
    * the community and display formats of the folder as shared and local
    * dependencies respectively.
    *
    * @param tok The security token to use, may not be <code>null</code>.
    * @param path The path the folder, may not be <code>null</code> or empty.
    * @param folderDepType The type of child folder dependencies to create, one
    * of the <code>PSDependency.TYPE_xxx</code> values.
    *
    * @return A list of zero or more <code>PSDependency</code> objects,
    * never <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if dep is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected List getChildDependencies(PSSecurityToken tok,
      String path, int folderDepType) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");

      List<PSDependency> childDeps = new ArrayList<PSDependency>();

      PSRelationshipProcessor proc = getRelationshipProcessor(tok);
      PSComponentSummary sum = getFolderSummary(proc, path);
      if (sum != null)
      {
         PSLocator loc = sum.getCurrentLocator();
         PSFolder folder = getFolderObject(getComponentProcessor(tok), loc);
         if (folder != null) // unlikely
         {
            Iterator children = getChildFolderSummaries(proc, loc);
            String pathPrefix = path + PATH_SEP;
            while (children.hasNext())
            {
               PSComponentSummary child = (PSComponentSummary)children.next();
               PSDependency childDep = createDependency(m_def, pathPrefix +
                  child.getName(), child.getName());
               childDep.setDependencyType(folderDepType);
               childDeps.add(childDep);
            }

            // get folder translations
            // walk all relationship configs, and for each type with translation
            // category, see if there are any child relationships of that type
            PSDependencyHandler trHandler = getDependencyHandler(
               PSFolderTranslationsDependencyHandler.DEPENDENCY_TYPE);
            Iterator configs = 
               PSRelationshipCommandHandler.getRelationshipConfigs(
                  PSRelationshipConfig.CATEGORY_TRANSLATION);
            while (configs.hasNext())
            {
               PSRelationshipConfig config = 
                  (PSRelationshipConfig)configs.next();
               PSDependency trDep = trHandler.getDependency(tok, 
                  PSFolderTranslationsDependencyHandler.createDependencyId(
                     path, config.getName()));
                        
               if (trDep != null)
               {
                  trDep.setDependencyType(folderDepType);               
                  childDeps.add(trDep);
               }

            }

            int commId = folder.getCommunityId();
            if (commId != -1)
            {
               PSDependency commDep = getDependencyHandler(
                  PSCommunityDependencyHandler.DEPENDENCY_TYPE).getDependency(
                     tok, String.valueOf(commId));
               if (commDep != null)
                  childDeps.add(commDep);
            }


            String dispFormatId = folder.getDisplayFormatPropertyValue();
            if (dispFormatId != null && dispFormatId.trim().length() != 0)
            {
               PSDependency dfDep = getDisplayFormatDep(tok,
                  getComponentProcessor(tok), dispFormatId);
               if (dfDep != null)
               {
                  dfDep.setIsAssociation(true);
                  childDeps.add(dfDep);
               }
            }
         }
      }

      return childDeps;
    }



   /**
    * Get the folder object specified by the supplied path.
    *
    * @param relProcessor relProcessor The processor to use, may not be <code>null</code>.
    * @param compProcessor server compontent processor proxy, never <code>null</code>.
    * @param path The path to the folder, starting from the name of the root
    * folder, delimited using forward slashes (e.g. "Sites/MyFolders/Folder1").
    *
    * @return The folder, or <code>null</code> if the folder cannot be
    * located.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any other errors.
    */
   protected PSFolder getFolderObject(PSRelationshipProcessor relProcessor,
      PSComponentProcessorProxy compProcessor,
      String path) throws PSDeployException
   {
      if (relProcessor == null)
         throw new IllegalArgumentException("relProcessor may not be null");
      if (compProcessor == null)
         throw new IllegalArgumentException("compProcessor may not be null");
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");

      PSFolder folder = null;
      PSComponentSummary sum = getFolderSummary(relProcessor, path);
      if (sum != null)
         folder = getFolderObject(compProcessor, sum.getCurrentLocator());

      return folder;
   }

   /**
    * Gets the folder summary specified by the supplied path.
    *
    * @param processor relProcessor The processor to use, may not be <code>null</code>.
    * @param path The path to the folder, starting from the name of the root
    * folder, delimited using forward slashes (e.g. "Sites/MyFolders/Folder1").
    *
    * @return The summary, or <code>null</code> if the summary cannot be
    * located.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any other errors.
    */
   protected PSComponentSummary getFolderSummary(
      PSRelationshipProcessor processor, String path)
      throws PSDeployException
   {
      if (processor == null)
         throw new IllegalArgumentException("processor may not be null");
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");

      return getChildFolderSummary(processor, parseFolderPath(path),
         m_rootLocator, 0);
   }

   /**
    * Returns the path of the folder specified by the supplied 
    * <code>folder</code>.
    *
    * @param proc relationship processor proxy, used for obtaining summaries
    * of translated folders, may not be <code>null</code>.
    *
    * @param folder summary of the folder whose path is to be obtained,
    * may not be <code>null</code>.
    *
    * @return the path of the folder specified by <code>folder</code>,
    * never <code>null</code> or empty.
    *
    * @throws PSDeployException if any error occurs getting the summary of
    * parent folders.
    */
   protected String getFolderPath(PSRelationshipProcessor proc, 
      PSComponentSummary folder) throws PSDeployException
   {
      if (proc == null)
         throw new IllegalArgumentException("processor may not be null");
 
      if (folder == null)
         throw new IllegalArgumentException("folder summary may not be null");

      String path = folder.getName();
      PSComponentSummaries summ = null;
      PSLocator dependent = null;
      PSRelationshipFilter filter = null;

      while (folder != null)
      {
         dependent = (PSLocator)folder.getLocator();
         filter = new PSRelationshipFilter();
         filter.setDependent(dependent);
         filter.setName(PSRelationshipConfig.TYPE_FOLDER_CONTENT);

         try
         {
            summ = proc.getSummaries(filter, true);
            if ((summ == null) || (summ.size() < 1))
            {
               folder = null;
            }
            else
            {
               folder = summ.toArray()[0];
               path = folder.getName() + PATH_SEP + path;
            }
         }
         catch (PSCmsException ex)
         {
            throw new PSDeployException(ex);
         }
      }
      path = path.substring(path.indexOf("/") + 1);
      return path;
   }

   /**
    * Get child folders of the folder specified by the supplied path.
    *
    * @param processor The processor to use, may not be <code>null</code>.
    * @param path The path of the parent folder, may be null to get all top
    * level folders, may not be empty.
    *
    * @return An iterator over zero or more folder paths, <code>null</code> if
    * <code>path</code> is not <code>null</code> and the folder it specifies
    * does not exist, may be empty if the specified folder has no children.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected Iterator getChildFolderPaths(PSRelationshipProcessor processor,
      String path) throws PSDeployException
   {
      if (processor == null)
         throw new IllegalArgumentException("processor may not be null");
      if (path != null && path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be empty");

      boolean exists = true;
      PSLocator loc = null;
      if (path == null)
      {
         path = "";
         loc = m_rootLocator;
      }
      else
      {
         path += PATH_SEP;
         PSComponentSummary sum = getFolderSummary(processor, path);
         if (sum != null)
            loc = sum.getCurrentLocator();
         else
            exists = false;
      }

      Iterator result = null;
      if (exists)
      {
         List<String> paths = new ArrayList<String>();
         Iterator children = getChildFolderSummaries(processor, loc);
         while (children.hasNext())
         {
            PSComponentSummary child = (PSComponentSummary)children.next();
            paths.add(path + child.getName());
         }

         result = paths.iterator();
      }

      return result;
   }

   /**
    * Load the component summaries representing the child folders of this
    * folder.
    *
    * @param processor The processor to use, may not be <code>null</code>.
    * @param locator The locator of the parent folder, may not be
    * <code>null</code>.
    *
    * @return An iterator of <code>PSComponentSummary</code> objects, never
    * <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected Iterator getChildFolderSummaries(PSRelationshipProcessor processor,
      PSLocator locator) throws PSDeployException
   {
      if (processor == null)
         throw new IllegalArgumentException("processor may not be null");
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      try
      {
         List<PSComponentSummary> summaries = new ArrayList<PSComponentSummary>();
         PSComponentSummary[] sums = processor.getChildren(FOLDER_TYPE,
            locator);
         for (int i = 0; i < sums.length; i++)
         {
            if (sums[i].isFolder())
               summaries.add(sums[i]);
         }

         return summaries.iterator();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }

   /**
    * Load the component summaries representing the child content items of this
    * folder.
    *
    * @param processor The processor to use, may not be <code>null</code>.
    * @param locator The locator of the parent folder, may not be
    * <code>null</code>.
    *
    * @return An iterator of <code>PSComponentSummary</code> objects, never
    * <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected Iterator getChildItemSummaries(PSRelationshipProcessor processor,
      PSLocator locator) throws PSDeployException
   {
      if (processor == null)
         throw new IllegalArgumentException("processor may not be null");
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      try
      {
         List<PSComponentSummary> summaries = new ArrayList<PSComponentSummary>();
         PSComponentSummary[] sums = processor.getChildren(FOLDER_TYPE,
            locator);
         for (int i = 0; i < sums.length; i++)
         {
            if (sums[i].getType() == PSComponentSummary.TYPE_ITEM)
               summaries.add(sums[i]);
         }

         return summaries.iterator();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }

   /**
    * Loads the folder object specified by the supplied locator.
    *
    * @param processor The processor to use, may not be <code>null</code>.
    * @param locator The locator of the folder, may not be <code>null</code>.
    *
    * @return The folder, or <code>null</code> if the folder cannot be located.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected PSFolder getFolderObject(PSComponentProcessorProxy processor,
      PSLocator locator) throws PSDeployException
   {
      if (processor == null)
         throw new IllegalArgumentException("processor may not be null");
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      try
      {
         PSFolder folder = null;
         Element[] elements = processor.load(FOLDER_TYPE,
            new PSKey[] {locator});
         if (elements.length > 0)
            folder = new PSFolder(elements[0]);

         return folder;
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }

   /**
    * Get the summary of the parent of the folder represented by the supplied
    * path.  This method does not validate that folder represented by the
    * supplied path exists, it only checks for the existence of its specified
    * parent folder.
    *
    * @param processor The processor to use, may not be <code>null</code>.
    * @param path The path of the child folder, may not be <code>null</code> or
    * empty.
    *
    * @return The summary, or <code>null</code> if the parent folder cannot be
    * located or if <code>path</code> specifies a top level folder and thus has
    * no real parent.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected PSComponentSummary getParentFolderSummary(
      PSRelationshipProcessor processor, String path)
      throws PSDeployException
   {
      if (processor == null)
         throw new IllegalArgumentException("processor may not be null");

      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");

      PSComponentSummary parentSum = null;
      String[] parsedPath = parseFolderPath(path);

      // no point in trying if specified path is a top-level folder
      if (pathSpecifiesParent(path))
      {
         // Try to get parent of specified folder
         String[] parentPath = new String[parsedPath.length - 1];
         System.arraycopy(parsedPath, 0, parentPath, 0, parsedPath.length - 1);
         parentSum = getChildFolderSummary(processor,
            parentPath, m_rootLocator, 0);
      }

      return parentSum;
   }

   /**
    * Get all folder objects specified by the supplied path.
    *
    * @param relProcessor  The processor to use, may not be <code>null</code>.
    * @param compProcessor server compontent processor proxy, never <code>null</code>.
    * @param path The path whose folders should be returned, starting from the
    * name of the root folder, delimited using forward slashes (e.g.
    * "Sites/MyFolders/Folder1").  May not be <code>null</code> or empty.
    *
    * @return An array containing each folder, ordered sequentially starting
    * from the root folder.  The size of the array will always equal the number
    * of folder names specified by the supplied path.  If any folder specified
    * by the path cannot be located, that entry in the array and those that
    * follow will be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected PSFolder[] getAllFolderObjects(
      PSRelationshipProcessor relProcessor,
      PSComponentProcessorProxy compProcessor, String path)
      throws PSDeployException
   {
      if (relProcessor == null)
         throw new IllegalArgumentException("relProcessor may not be null");
      if (compProcessor == null)
         throw new IllegalArgumentException("compProcessor may not be null");
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");

      String[] paths = parseFolderPath(path);
      PSFolder[] folders = new PSFolder[paths.length];

      PSLocator loc = m_rootLocator;
      for (int i = 0; i < paths.length; i++)
      {
         PSComponentSummary sum = getChildFolderSummary(relProcessor, loc,
            paths[i]);
         if (sum == null)
            break;

         PSFolder folder = getFolderObject(compProcessor, loc);
         if (folder == null)
            break;  // unlikely but possible
         folders[i] = folder;
         loc = sum.getCurrentLocator();
      }

      return folders;
   }

   /**
    * Gets a display format dependency from the supplied id.
    *
    * @param tok The security token to use, may not be <code>null</code>.
    * @param proc The processor to use, may not be <code>null</code>.
    * @param formatId The id of the display format object, may not be
    * <code>null</code> or empty.
    *
    * @return The dependency, or <code>null</code> if it is not found.  Will be
    * set as a local dependency.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected PSDependency getDisplayFormatDep(PSSecurityToken tok,
      PSComponentProcessorProxy proc, String formatId)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (proc == null)
         throw new IllegalArgumentException("proc may not be null");
      if (formatId == null || formatId.trim().length() == 0)
         throw new IllegalArgumentException(
            "formatId may not be null or empty");

      PSDependency dep = null;
      PSDisplayFormat df = null;
      try
      {
         Element[] els = proc.load(PSDbComponent.getComponentType(
            PSDisplayFormat.class), new PSKey[] {PSDisplayFormat.createKey(
               new String[] {formatId})});
         if (els.length > 0)
            df = new PSDisplayFormat(els[0]);
      }
      catch (Exception e)
      {
         throw new PSDeployException(
            IPSDeploymentErrors.UNEXPECTED_ERROR, e.getLocalizedMessage());
      }

      if (df != null)
      {
         PSDependencyHandler dfHandler = getDependencyHandler(
            PSDisplayFormatDefDependencyHandler.DEPENDENCY_TYPE);
         dep = dfHandler.getDependency(tok, formatId);
         dep.setDependencyType(PSDependency.TYPE_LOCAL);
      }

      return dep;
   }

   /**
    * Determine if the supplied path specifies a folder that has a parent.
    *
    * @param path The path to check, may not be <code>null</code> or empty.
    *
    * @return <code>true</code> if the path specifies a parent,
    * <code>false</code> if it specifies a top level folder.
    */
   protected boolean pathSpecifiesParent(String path)
   {
      return (path.indexOf(PATH_SEP) != -1);
   }

   /**
    * Get the immediate child folder with the matching name from the parent
    * folder specified by the supplied locator.
    *
    * @param processor The processor to use, assumed not <code>null</code>.
    * @param locator The locator of parent folder, assumed not
    * <code>null</code>.
    * @param name The name of child folder, assumed not <code>null</code> or
    * empty.
    *
    * @return The child folder summary, or <code>null</code> if a child folder
    * with the specified name is not found.
    *
    * @throws PSDeployException if there are any errors.
    */
   private PSComponentSummary getChildFolderSummary(
      PSRelationshipProcessor processor, PSLocator locator, String name)
      throws PSDeployException
   {
      PSComponentSummary childSum = null;
      Iterator children = getChildFolderSummaries(processor, locator);
      while (children.hasNext() && childSum == null)
      {
         PSComponentSummary test = (PSComponentSummary)children.next();
         if (test.getName().equals(name))
            childSum = test;
      }

      return childSum;
   }

   /**
    * Gets the specified folder summary, recursively walking the
    * <code>names</code> array, starting from the specified index using the
    * supplied locator.
    *
    * @param proc The processor to use, assumed not <code>null</code>.
    * @param names The folder path from the root folder, tokenized into an
    * array, assumed not <code>null</code> or empty.  See
    * {@link #parseFolderPath(String)} for more info.
    * @param index The index into the <code>names</code> array specifying the
    * a child of the folder represented by the supplied <code>locator</code>.
    * Assumed to be greater than or equal to zero and less than
    * <code>names.length</code>.
    * @param loc The locator of the parent of the folder specified by
    * <code>index</code>, assumed not <code>null</code>.
    *
    * @return The component summary of the last folder in the array, or
    * <code>null</code> if it cannot be located.
    *
    * @throws PSDeployException if there are any errors.
    */
   private PSComponentSummary getChildFolderSummary(
      PSRelationshipProcessor proc, String[] names, PSLocator loc,
      int index) throws PSDeployException
   {
      PSComponentSummary result = null;

      PSComponentSummary sum = getChildFolderSummary(proc, loc, names[index]);
      // see if we need to keep going
      if (sum != null && ++index < names.length)
         result = getChildFolderSummary(proc, names, sum.getCurrentLocator(), index);
      else
         result = sum;

      return result;
   }

   /**
    * Parses the supplied folder path using forward slashes (/) as a delimeter.
    *
    * @param path The path to parse, assumed not <code>null</code> or empty.
    *
    * @return An array of folder names, never <code>null</code> or empty.
    */
   private String[] parseFolderPath(String path)
   {
      List<String> names = new ArrayList<String>();

      StringTokenizer tok = new StringTokenizer(path, PATH_SEP);
      while (tok.hasMoreTokens())
         names.add(tok.nextToken());

      return (String[])names.toArray(new String[names.size()]);
   }

   /**
    * Separator used for folder paths, a foward slash ("/").
    */
   protected static final String PATH_SEP = "/";

   /**
    * Component type string to pass to process calls, never <code>null</code>,
    * empty or modified.
    */
   protected static final String FOLDER_TYPE = PSFolder.getComponentType(
      PSFolder.class);

   /**
    * Locator to reference the virtual root folder, which is the
    * parent of all top level folders. Never <code>null</code> or modified.
    */
   private PSLocator m_rootLocator = new PSLocator(PSFolder.ROOT_ID, 1);

}
