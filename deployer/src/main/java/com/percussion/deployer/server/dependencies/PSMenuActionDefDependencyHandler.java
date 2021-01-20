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
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSChildActions;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.cms.objectstore.PSMenuChild;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSSecurityToken;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class to handle packaging and deploying a menu action definition.  This
 * includes menu items and dynamic menus.
 */
public class PSMenuActionDefDependencyHandler
   extends PSMenuActionObjectDependencyHandler
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
   public PSMenuActionDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap) throws PSDeployException
   {
      super(def, dependencyMap);
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

      // first get action
      PSComponentProcessorProxy proc = getComponentProcessor(tok);
      files.add(getActionDepFile(proc, dep, true));

      // Now walk local dependency tree and save child categories as well
      getChildDepFiles(proc, dep, files);

      return files.iterator();
   }

   /**
    * Recurse all child depedendencies and create and add a dependency file
    * for each child of type MenuActionCategory.
    *
    * @param proc The processor to use, assumed not <code>null</code>.
    * @param dep The dependency to recurse, assumed not <code>null</code>.
    * @param files The list of files to add to, assumed not <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   private void getChildDepFiles(PSComponentProcessorProxy proc, PSDependency dep,
      List files) throws PSDeployException
   {
      Iterator deps = dep.getDependencies(PSDependency.TYPE_LOCAL);
      if (deps != null)
      {
         while (deps.hasNext())
         {
            PSDependency child = (PSDependency)deps.next();
            if (!child.getObjectType().equals(
               PSMenuActionCategoryDependencyHandler.DEPENDENCY_TYPE))
            {
               continue;
            }
            files.add(getActionDepFile(proc, child, false));
            getChildDepFiles(proc, child, files);
         }
      }
   }

   /**
    * Create a dependency file for the supplied action id.
    *
    * @param proc The processor to use, assumed not <code>null</code>.
    * @param dep The dependency representing the action to save, assumed not
    * <code>null</code>.
    * @param isLeaf <code>true</code> if the id specifies a MenuActionDef,
    * <code>false</code> if it specifies a MenuActionCategory.
    *
    * @return The file, never <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   private PSDependencyFile getActionDepFile(PSComponentProcessorProxy proc,
      PSDependency dep, boolean isLeaf) throws PSDeployException
   {
      PSAction action = loadAction(proc, dep, isLeaf);
      if (!isLeaf)
         action.getChildren().clear();

      return createDependencyFile(action);
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

      try
      {
         // restore all actions - build map of action to its children so we
         // can restore parent-child relationships as well
         Map actionMap = new HashMap();
         Iterator files = getDependencyFilesFromArchive(archive, dep);
         while (files.hasNext())
         {
            Element root = getElementFromFile(archive, dep,
               (PSDependencyFile)files.next());
            PSAction action = new PSAction(root);
            actionMap.put(getIdFromKey(action, action.getLabel()),
               action.clone());
         }

         PSComponentProcessorProxy proc = getComponentProcessor(tok);
         List txnList = new ArrayList();
         installActions(proc, ctx, dep, txnList, actionMap, null);

         // process transactions for all dependencies installed
         Iterator txns = txnList.iterator();
         while (txns.hasNext())
         {
            Object[] txnEntry = (Object[])txns.next();
            PSAction action = (PSAction)txnEntry[0];
            PSDependency txnDep = (PSDependency)txnEntry[1];
            Integer txnAction = (Integer)txnEntry[2];

            // be sure to change id mapping to not new if we have one
            PSIdMapping txnIdMapping = getIdMapping(ctx, txnDep);
            if (txnIdMapping != null)
               txnIdMapping.setIsNewObject(false);

            // if the action's dependency type is not a Menu Action Def Depend.
            // delegate it to the object's handler
            if (  !txnDep.getObjectType().equals(getType()) )
            {
               PSDependencyHandler depHndlr = getDependencyHandler(txnDep
                     .getObjectType());
               depHndlr.addTransactionLogEntry(txnDep, ctx, action
                     .getComponentType(), PSTransactionSummary.TYPE_CMS_OBJECT,
                     txnAction.intValue());
            }
            else
               addTransactionLogEntry(txnDep, ctx, action, txnAction.intValue());
         }
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
    * Installs all actions specified by the supplied dependency tree.
    * Installs the supplied dependency and recurses its child actions and
    * installs them.  The save is done in two steps.  Deleted actions are
    * processed first, followed by actions which need to be inserted.
    *
    * @param proc The processor to use, assumed not <code>null</code>.
    * @param ctx The import ctx, assumed not <code>null</code>.
    * @param dep The dependency to install, assumed not <code>null</code>.
    * @param txnList The list to use to collect transaction entries as
    * components are added to the <code>compList</code>.  Each entry is an
    * <code>Object[]</code> containing the <code>PSAction</code>,
    * <code>PSDependency</code>, and
    * <code>PSTransactionSummary.ACTION_xxx</code> value.  Assumed not
    * <code>null</code>.
    * @param actionMap A map of all source action objects from the archive,
    * where the key is the id as a <code>String</code> and the value is a clone
    * of the source <code>PSAction</code> object.
    * @param tgtChild The action to set as the child of the action represented
    * by the supplied dependency, may be <code>null</code> if the dependency
    * does not have any children.  This action's id represents the action on the
    * target system.
    *
    * @throws PSCmsException if errors occur during the save.
    * @throws PSDeployException if there are any other errors.
    */
   private void installActions(PSComponentProcessorProxy proc, PSImportCtx ctx,
      PSDependency dep, List txnList, Map actionMap, PSAction tgtChild)
         throws PSDeployException, PSCmsException
   {
      PSDbComponentCollection compDeletes = new PSDbComponentCollection(
            PSAction.class);
      PSDbComponentCollection compInserts = new PSDbComponentCollection(
            PSAction.class);
      
      PSAction srcAction = (PSAction)actionMap.get(dep.getDependencyId());
      if (srcAction == null)
      {
         Object args[] =
         {
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_COMPONENT_XML],
            dep.getObjectTypeName(), dep.getDependencyId(),
            dep.getDisplayName()
         };

         throw new PSDeployException(
            IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }

      // try to restore the current action from the db
      String tgtId = dep.getDependencyId();
      PSIdMapping idMapping = getIdMapping(ctx, dep);
      if (idMapping != null)
         tgtId = idMapping.getTargetId();
      boolean loadLeaf = isLeaf(srcAction);
      PSAction tgtAction = loadAction(proc, tgtId, loadLeaf);

      boolean foundChild = false;
      PSChildActions srcChildActions = srcAction.getChildren();
      if (tgtAction != null)
      {
          if (srcChildActions != null)
          {
            // add all current children to the source action object, checking
            // for the tgtChild along the way
            PSChildActions tgtChildActions = tgtAction.getChildren();
            if (tgtChildActions != null)
            {
               Iterator tgtChildren = tgtChildActions.iterator();
               String tgtChildId = null;
               if (tgtChild != null)
                  tgtChildId = getIdFromKey(tgtChild, tgtChild.getLabel());
               while (tgtChildren.hasNext())
               {
                  PSMenuChild menuChild = (PSMenuChild)tgtChildren.next();
                  if (tgtChildId != null &&
                     menuChild.getChildActionId().equals(tgtChildId))
                  {
                     foundChild = true;
                  }
                  srcChildActions.add((PSMenuChild)menuChild.clone());
               }
            }
          }

         // add the "old" tgtAction to the list for delete
         tgtAction.markForDeletion();
         compDeletes.add(tgtAction);
         txnList.add(new Object[] {tgtAction, dep, new Integer(
            PSTransactionSummary.ACTION_DELETED)});
         
         // keep tgt version information
         srcAction.setVersion(tgtAction.getVersion());
      }

      // set tgt id on the source object
      srcAction.setLocator(PSAction.createKey(tgtId));

      // if the source child is not in the list, add it
      if (srcChildActions != null && !foundChild && tgtChild != null)
         srcChildActions.add(tgtChild);

      // add source action to list for save (will be inserted using supplied id)
      compInserts.add(srcAction);
      txnList.add(new Object[] {srcAction, dep, new Integer(
         PSTransactionSummary.ACTION_CREATED)});

      // process "parents" - these are actually the child dependencies
      Iterator deps = dep.getDependencies(PSDependency.TYPE_LOCAL);
      while (deps.hasNext())
      {
         PSDependency childDep = (PSDependency)deps.next();
         String childDepType = childDep.getObjectType();
         if ( childDepType.equals(DEPENDENCY_TYPE) || childDepType.equals(
                    PSMenuActionCategoryDependencyHandler.DEPENDENCY_TYPE))
         {
            installActions(proc, ctx, childDep, txnList, actionMap, srcAction);
         }
      }
      
      // perform the save
      proc.save(new PSDbComponentCollection[] {compDeletes});
      proc.save(new PSDbComponentCollection[] {compInserts});
   }

   // see base class
   protected boolean isLeaf()
   {
      return true;
   }

   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "MenuActionDef";
}
