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

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionParameter;
import com.percussion.cms.objectstore.PSActionVisibilityContext;
import com.percussion.cms.objectstore.PSActionVisibilityContexts;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSMenuChild;
import com.percussion.deploy.client.IPSDeployConstants;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deploy.objectstore.PSApplicationIDTypes;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDeployComponentUtils;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.server.IPSIdTypeHandler;
import com.percussion.deploy.server.PSAppTransformer;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.design.objectstore.PSParam;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.catalog.PSTypeEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.percussion.services.error.PSNotFoundException;
import org.w3c.dom.Element;

/**
 * Base class for menu action dependency handlers, provides common functionality
 * for deploying and installing <code>PSAction</code> objects.
 */
public abstract class PSMenuActionObjectDependencyHandler
   extends PSCmsObjectDependencyHandler implements IPSIdTypeHandler
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
   public PSMenuActionObjectDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap) throws PSDeployException
   {
      super(def, dependencyMap);
   }

   // see base class
   @SuppressWarnings("unchecked")
   public Iterator getChildDependencies(PSSecurityToken tok,
      PSDependency dep) throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (! dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");

      Set childDeps = new HashSet();

      PSComponentProcessorProxy proc = getComponentProcessor(tok);

      // get this action
      String actionId = dep.getDependencyId();
      PSAction action = loadAction(proc, dep, isLeaf());

      // get all parent actions and add them as dependencies
      Iterator actions = loadActions(proc, false, actionId);
      while (actions.hasNext())
      {
         PSAction parent = (PSAction)actions.next();
         String name = parent.getName();
         String childType =
            PSMenuActionCategoryDependencyHandler.DEPENDENCY_TYPE;
         PSDependencyDef def;
         if (getType().equals(childType))
            def = m_def;
         else
         {
            PSDependencyHandler childHandler = getDependencyHandler(
               childType);
            // this is okay since we are the base class for this handler
            def = childHandler.m_def;
         }
         PSDependency childDep = createDependency(def, getIdFromKey(parent,
            name), name);
         childDep.setDependencyType(PSDependency.TYPE_LOCAL);
         childDeps.add(childDep);
      }

      // now get action's dependencies
      PSDependencyHandler appDepHandler = getDependencyHandler(
         PSApplicationDependencyHandler.DEPENDENCY_TYPE);
      String url = action.getURL();
      String appName = null;
      if (url.trim().length() > 0)
         appName = PSDeployComponentUtils.getAppName(url);
      if (appName != null)
      {
         PSDependency appDep = appDepHandler.getDependency(tok,
            appName);
         if (appDep != null)
            childDeps.add(appDep);
      }

      // check community and content type contexts for dependencies
      PSActionVisibilityContexts visContexts = action.getVisibilityContexts();
      PSActionVisibilityContext comCtx = visContexts.getContext(
         PSActionVisibilityContext.VIS_CONTEXT_COMMUNITY);
      if (comCtx != null)
      {
         childDeps.addAll(
            getDepsFromMultiValuedProperty(tok, comCtx, getDependencyHandler(
               PSCommunityDependencyHandler.DEPENDENCY_TYPE)));
      }

      PSActionVisibilityContext ctCtx = visContexts.getContext(
         PSActionVisibilityContext.VIS_CONTEXT_CONTENT_TYPE);
      if (ctCtx != null)
      {
         childDeps.addAll(
            getDepsFromMultiValuedProperty(tok, ctCtx, getDependencyHandler(
               PSCEDependencyHandler.DEPENDENCY_TYPE)));
      }

      PSActionVisibilityContext wfCtx = visContexts.getContext(
         PSActionVisibilityContext.VIS_CONTEXT_WORKFLOWS_TYPE);
      if (wfCtx != null)
      {
         childDeps.addAll(
            getDepsFromMultiValuedProperty(tok, wfCtx, getDependencyHandler(
               PSWorkflowDependencyHandler.DEPENDENCY_TYPE)));
      }

      // get icon url dep
      String iconPath = action.getProperties().getProperty(
         action.PROP_SMALL_ICON);
      if (iconPath != null && iconPath.trim().length() > 0)
      {
         PSDependency iconDep = null;

         // first try as file dep
         PSDependencyHandler fileHandler = getDependencyHandler(
            PSSupportFileDependencyHandler.DEPENDENCY_TYPE);
         iconDep = fileHandler.getDependency(tok, iconPath);
         if (iconDep == null)
         {
            String iconAppName = PSDeployComponentUtils.getAppName(iconPath);
            if (iconAppName != null)
               iconDep = appDepHandler.getDependency(tok, iconAppName);
         }

         if (iconDep != null)
            childDeps.add(iconDep);
      }

      // add idtype dependencies
      childDeps.addAll(getIdTypeDependencies(tok, dep));
      
      // add acl dependency
      addAclDependency(tok, PSTypeEnum.ACTION, dep, childDeps);

      return childDeps.iterator();
    }

   // see base class
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      List deps = new ArrayList();

      PSComponentProcessorProxy proc = getComponentProcessor(tok);
      Iterator actions = loadActions(proc, isLeaf(), null);
      while (actions.hasNext())
      {
         PSAction action = (PSAction)actions.next();
         String dispName = action.getName();
         deps.add(createDependency(m_def, getIdFromKey(action, dispName),
            dispName));
      }

      return deps.iterator();
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

      PSAction action = loadAction(getComponentProcessor(tok), id, isLeaf());
      if (action != null)
         dep = createDependency(m_def, id, action.getName());

      return dep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Application</li>
    * <li>Community</li>
    * <li>ContentType</li>
    * <li>MenuActionCategory</li>
    * <li>SupportFile</li>
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
   public void reserveNewId(PSDependency dep, PSIdMap idMap)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      // create a dummy object
      PSAction action = new PSAction("dummy", "dummy");
      reserveNewId(dep, idMap, action);
   }

   //see IPSIdTypeHandler interface
   public PSApplicationIDTypes getIdTypes(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type");

      PSApplicationIDTypes idTypes = new PSApplicationIDTypes(dep);

      PSAction action = loadAction(getComponentProcessor(tok), dep, isLeaf());

      List mappings = new ArrayList();
      String reqName = "URL";

      // User parameters
      Iterator actionParams = action.getParameters().iterator();
      // convert to PSParam to leverage existing code - don't worry about
      // automatic params, as the value won't be a number
      while (actionParams.hasNext())
      {
         PSActionParameter actionParam = (PSActionParameter)actionParams.next();
         PSParam param = new PSParam(actionParam.getName(), new PSTextLiteral(
            actionParam.getValue()));

         PSAppTransformer.checkParam(mappings, param, null);
      }

      idTypes.addMappings(reqName,
         IPSDeployConstants.ID_TYPE_ELEMENT_URL_PARAMS,
            mappings.iterator());

      return idTypes;
   }

   //see IPSIdTypeHandler interface
   public void transformIds(Object object, PSApplicationIDTypes idTypes,
      PSIdMap idMap) throws PSDeployException
   {
      if (object == null)
         throw new IllegalArgumentException("object may not be null");

      if (idTypes == null)
         throw new IllegalArgumentException("idTypes may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      if (!(object instanceof PSAction))
      {
         throw new IllegalArgumentException("invalid object type");
      }

      PSAction action = (PSAction)object;

      // walk id types and perform any transforms
      Iterator resources = idTypes.getResourceList(false);
      while (resources.hasNext())
      {
         String resource = (String)resources.next();
         Iterator elements = idTypes.getElementList(resource, false);
         while (elements.hasNext())
         {
            String element = (String)elements.next();
            Iterator mappings = idTypes.getIdTypeMappings(
                  resource, element, false);
            while (mappings.hasNext())
            {

               PSApplicationIDTypeMapping mapping =
                  (PSApplicationIDTypeMapping)mappings.next();

               if (mapping.getType().equals(
                  PSApplicationIDTypeMapping.TYPE_NONE))
               {
                  continue;
               }

               if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_URL_PARAMS))
               {
                  // walk action params, build PSParam, call transform, then set
                  // resulting value back on the action param
                  Iterator actionParams = action.getParameters().iterator();
                  while (actionParams.hasNext())
                  {
                     PSActionParameter actionParam =
                        (PSActionParameter)actionParams.next();
                     PSParam param = new PSParam(actionParam.getName(),
                        new PSTextLiteral(actionParam.getValue()));

                     PSAppTransformer.transformParam(param, mapping, idMap);
                     actionParam.setValue(param.getValue().getValueText());
                  }
               }
            }
         }
      }
   }

   /**
    * Loads the action specified by the supplied dependency from the repository.
    *
    * @param proc The processor to use, may not be <code>null</code>.
    * @param dep The dependency, may not be <code>null</code>.
    * @param loadLeaf If <code>true</code>, only leaves are loaded, otherwise
    * only non-leaves are loaded.  See {@link #isLeaf(PSAction)} for more info.
    *
    * @return The action, never <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if the action is not found or if there are any
    * other errors.
    */
   protected PSAction loadAction(PSComponentProcessorProxy proc, PSDependency dep,
      boolean loadLeaf) throws PSDeployException
   {
      if (proc == null)
         throw new IllegalArgumentException("proc may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      PSAction action = loadAction(proc, dep.getDependencyId(), loadLeaf);
      if (action == null)
      {
         Object[] args = {dep.getDependencyId(), dep.getObjectTypeName(),
            dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND,
            args);
      }

      return action;
   }

   /**
    * Loads the specified action from the repository.
    *
    * @param proc The processor to use, may not be <code>null</code>.
    * @param id The id of the action, may not be <code>null</code> or empty.
    * @param loadLeaf If <code>true</code>, only leaves are loaded, otherwise
    * only non-leaves are loaded.  See {@link #isLeaf(PSAction)} for more info.
    *
    * @return The action, or <code>null</code> if it cannot be found.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected PSAction loadAction(PSComponentProcessorProxy proc, String id,
      boolean loadLeaf) throws PSDeployException
   {
      if (proc == null)
         throw new IllegalArgumentException("proc may not be null");
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      try
      {
         PSAction result = null;
         Element[] elements = proc.load(PSAction.getComponentType(
            PSAction.class), new PSKey[] {PSAction.createKey(id)});
         if (elements.length > 0)
         {
            PSAction action = new PSAction(elements[0]);
            if (loadLeaf == isLeaf(action))
               result = action;
         }

         return result;
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
    * Loads all action of the specified type from the repository.
    *
    * @param proc The processor to use, may not be <code>null</code>.
    * @param loadLeaf If <code>true</code>, only leaves are loaded, otherwise
    * only non-leaves are loaded.  See {@link #isLeaf(PSAction)} for more info.
    * @param childId If provided, only actions that are "parents" of this child
    * are returned.  May be <code>null</code>, never empty.
    * @return An iterator over zero or more <code>PSAction</code> objects,
    * never <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected Iterator loadActions(PSComponentProcessorProxy proc, boolean loadLeaf,
      String childId) throws PSDeployException
   {
      if (proc == null)
         throw new IllegalArgumentException("proc may not be null");
      if (childId != null && childId.trim().length() == 0)
         throw new IllegalArgumentException("childId may not be empty");

      try
      {
         List result = new ArrayList();
         Element[] elements = proc.load(PSAction.getComponentType(
            PSAction.class), null);
         for (int i = 0; i < elements.length; i++)
         {
            PSAction action = new PSAction(elements[i]);
            if ((loadLeaf == isLeaf(action)) && (childId == null ||
               (isChild(action, childId))))
            {
               result.add(action);
            }
         }

         return result.iterator();
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
    * Checks if the specified id represents a child action of the supplied
    * action.
    * @param action The action to check, assumed not <code>null</code>.
    * @param childId The child to check for, assumed not <code>null</code> or
    * empty.
    *
    * @return <code>true</code> if the supplied <code>childId</code> is found
    * in the child actions of the supplied <code>action</code>,
    * <code>false</code> if not.
    */
   private boolean isChild(PSAction action, String childId)
   {
      boolean isChild = false;
      Iterator childActions = action.getChildren().iterator();
      while (childActions.hasNext() && !isChild)
      {
         PSMenuChild child = (PSMenuChild)childActions.next();
         if (child.getChildActionId().equals(childId))
            isChild = true;
      }

      return isChild;
   }

   /**
    * Determines if the supplied action is a leaf (an item or a dynamic menu,
    * not a cascading category).
    *
    * @param action The action, may not be <code>null</code>.
    *
    * @return <code>true</code> if it is a leaf, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   protected boolean isLeaf(PSAction action)
   {
      if (action == null)
         throw new IllegalArgumentException("action may not be null");

      return (!action.isCascadedMenu());
   }

   /**
    * Determines if this handler's object type represents a leaf object.  See
    * {@link #isLeaf(PSAction)} for more info.
    *
    * @return <code>true</code> if it is a leaf, <code>false</code> otherwise.
    */
   protected abstract boolean isLeaf();

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List ms_childTypes = new ArrayList();

   static
   {
      ms_childTypes.add(PSApplicationDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSCEDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSCommunityDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSMenuActionCategoryDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSupportFileDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSAclDefDependencyHandler.DEPENDENCY_TYPE);
   }

}