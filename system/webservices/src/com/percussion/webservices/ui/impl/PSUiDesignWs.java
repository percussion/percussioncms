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
package com.percussion.webservices.ui.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import static org.apache.commons.lang.Validate.notEmpty;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSVersionableDbComponent;
import com.percussion.data.PSIdGenerator;
import com.percussion.data.PSTableChangeEvent;
import com.percussion.data.utils.PSTableUpdateHandlerBase;
import com.percussion.fastforward.managednav.PSNavConfig;
import com.percussion.fastforward.managednav.PSNavException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.locking.IPSObjectLockService;
import com.percussion.services.locking.PSLockException;
import com.percussion.services.locking.PSObjectLockServiceLocator;
import com.percussion.services.locking.data.PSObjectLock;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.memory.PSCacheAccessLocator;
import com.percussion.services.ui.IPSUiService;
import com.percussion.services.ui.PSUiException;
import com.percussion.services.ui.PSUiServiceLocator;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.services.ui.data.PSHierarchyNodeProperty;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.timing.PSTimer;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.ui.IPSUiDesignWs;
import com.percussion.webservices.ui.data.ActionType;

/**
 * The private ui design webservice implementations.
 */
@Transactional
public class PSUiDesignWs extends PSUiBaseWs implements IPSUiDesignWs
{
   // Added to optimize hierarchy nodes
   private static volatile Map<PSHierarchyNode.NodeType, Map<IPSGuid, String>> nodeIdToPathMap = null;

   private static volatile Map<IPSGuid, IPSGuid> objectIdToNodeIdMap = null;

   private static volatile boolean initializing = false;

   private StringBuilder nodePath;

   // Done

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#createActions(List<String>, List<String>)
    */
   public List<PSAction> createActions(List<String> names, List<ActionType> types, String session, String user)
         throws PSLockErrorException, PSErrorException
   {
      PSWebserviceUtils.validateParameters(names, "names", true, session, user);
      PSWebserviceUtils.validateParameters(types, "types", true, session, user);

      if (names.size() != types.size())
         throw new IllegalArgumentException("the size of names and types must be equal.");

      validateComponentNames(names, FIND_ACTIONS, PSAction.XML_NODE_NAME, PSTypeEnum.ACTION, PSAction.class);

      List<PSAction> actions = new ArrayList<PSAction>();
      for (int i = 0; i < names.size(); i++)
      {
         PSAction action = createAction(names.get(i), types.get(i));
         PSWebserviceUtils.createLock(action.getGUID(), session, user, null);
         actions.add(action);
      }

      return actions;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#createDisplayFormats(List<String>)
    */
   public List<PSDisplayFormat> createDisplayFormats(List<String> names, String session, String user)
         throws PSLockErrorException, PSErrorException
   {
      PSWebserviceUtils.validateParameters(names, "names", true, session, user);

      validateComponentNames(names, FIND_DISPLAY_FORMAT, "PSXDisplayFormat", PSTypeEnum.DISPLAY_FORMAT,
            PSDisplayFormat.class);

      List<PSDisplayFormat> results = new ArrayList<PSDisplayFormat>();
      for (int i = 0; i < names.size(); i++)
      {
         PSDisplayFormat displayFormat = createDisplayFormat(names.get(i));
         PSWebserviceUtils.createLock(displayFormat.getGUID(), session, user, null);
         results.add(displayFormat);
      }

      return results;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#createHierarchyNodes(List<String>, List<IPSGuid>,
    * List<PSHierarchyNode.NodeType>, List<String>, List<String>)
    */
   public List<PSHierarchyNode> createHierarchyNodes(List<String> names, List<IPSGuid> parents,
         List<PSHierarchyNode.NodeType> types, String session, String user)
   {
      ms_log.debug("entered createHierarchyNodes()");
      if (names == null || names.isEmpty())
         throw new IllegalArgumentException("names cannot be null or empty");

      if (parents == null || parents.isEmpty())
         throw new IllegalArgumentException("parents cannot be null or empty");

      if (types == null || types.isEmpty())
         throw new IllegalArgumentException("types cannot be null or empty");

      if (names.size() != parents.size() || names.size() != types.size())
         throw new IllegalArgumentException("names, parents and types must have the same size");

      List<PSHierarchyNode> nodes = new ArrayList<PSHierarchyNode>();
      int index = 0;
      for (String name : names)
      {
         if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("name cannot be null or empty");

         IPSGuid parent = parents.get(index);

         PSHierarchyNode.NodeType type = types.get(index);
         if (type == null)
            throw new IllegalArgumentException("type cannot be null");

         IPSUiService service = PSUiServiceLocator.getUiService();

         if (!service.findHierarchyNodes(name, parent, null).isEmpty())
         {
            PSWebserviceUtils.throwObjectExistException(name, PSTypeEnum.HIERARCHY_NODE);
         }

         PSHierarchyNode node = service.createHierarchyNode(name, parent, type);

         IPSObjectLockService lockService = PSObjectLockServiceLocator.getLockingService();
         try
         {
            lockService.createLock(node.getGUID(), session, user, node.getVersion(), false);
         }
         catch (PSLockException e)
         {
            // should never happen, ignore
         }

         nodes.add(node);
         index++;
      }

      return nodes;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#createSearches(List<String>, List<String>)
    */
   public List<PSSearch> createSearches(List<String> names, List<String> types, String session, String user)
         throws PSLockErrorException, PSErrorException
   {
      PSWebserviceUtils.validateParameters(names, "names", true, session, user);
      PSWebserviceUtils.validateParameters(types, "types", true, session, user);

      if (names.size() != types.size())
         throw new IllegalArgumentException("the size of names and types must be equal.");

      validateComponentNames(names, FIND_SEARCHES, PSSearch.XML_NODE_NAME, PSTypeEnum.SEARCH_DEF, PSSearch.class);

      List<PSSearch> searches = new ArrayList<PSSearch>();
      for (int i = 0; i < names.size(); i++)
      {
         PSSearch s = createSearch(names.get(i), types.get(i));
         PSWebserviceUtils.createLock(s.getGUID(), session, user, null);
         searches.add(s);
      }

      return searches;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#createViews(List<String>)
    */
   public List<PSSearch> createViews(List<String> names, String session, String user) throws PSLockErrorException,
         PSErrorException
   {
      PSWebserviceUtils.validateParameters(names, "names", true, session, user);

      validateComponentNames(names, FIND_SEARCHES, PSSearch.XML_NODE_NAME, PSTypeEnum.SEARCH_DEF, PSSearch.class);

      List<PSSearch> searches = new ArrayList<PSSearch>();
      for (int i = 0; i < names.size(); i++)
      {
         PSSearch s = createSearch(names.get(i), PSSearch.TYPE_VIEW);
         PSWebserviceUtils.createLock(s.getGUID(), session, user, null);
         searches.add(s);
      }

      return searches;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#deleteActions(List, boolean)
    */
   public void deleteActions(List<IPSGuid> ids, boolean ignoreDependencies, String session, String user)
         throws PSErrorsException
   {
      PSWebserviceUtils.validateParameters(ids, "ids", true, session, user);

      deleteComponents(ids, PSAction.class, PSAction.getComponentType(PSAction.class), ignoreDependencies, session,
            user);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#deleteDisplayFormats(List, boolean)
    */
   public void deleteDisplayFormats(List<IPSGuid> ids, boolean ignoreDependencies, String session, String user)
         throws PSErrorsException
   {
      PSWebserviceUtils.validateParameters(ids, "ids", true, session, user);

      deleteComponents(ids, PSDisplayFormat.class, PSDisplayFormat.getComponentType(PSDisplayFormat.class),
            ignoreDependencies, session, user);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#deleteHierarchyNodes(List, boolean, String, String)
    */
   public void deleteHierarchyNodes(List<IPSGuid> ids, boolean ignoreDependencies, String session, String user)
         throws PSErrorsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException("ids cannot be null or empty");

      IPSUiService service = PSUiServiceLocator.getUiService();

      PSErrorsException results = new PSErrorsException();
      for (IPSGuid id : ids)
      {
         if (PSWebserviceUtils.hasValidLockForDelete(id, session, user))
         {
            boolean exists = false;
            try
            {
               service.loadHierarchyNode(id);
               exists = true;
            }
            catch (PSUiException e)
            {
               // ignore, just means that the node does not exist
            }

            if (exists)
            {
               // check for dependents if requested
               if (!ignoreDependencies)
               {
                  PSErrorException error = PSWebserviceUtils.checkDependencies(id);
                  if (error != null)
                  {
                     results.addError(id, error);
                     continue;
                  }
               }

               service.deleteHierarchyNode(id);
            }

            results.addResult(id);
         }
         else
         {
            PSWebserviceUtils.handleMissingLockError(id, PSHierarchyNode.class, results);
         }
      }

      // release locks for all successfully deleted objects
      PSWebserviceUtils.releaseLocks(results.getResults(), session, user);

      // Recreate the maps nodeIdToPathMap and objectIdToNodeIdMap
      recreateStaticMaps();

      if (results.hasErrors())
         throw results;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#deleteSearches(List, boolean)
    */
   public void deleteSearches(List<IPSGuid> ids, boolean ignoreDependencies, String session, String user)
         throws PSErrorsException
   {
      PSWebserviceUtils.validateParameters(ids, "ids", true, session, user);

      deleteComponents(ids, PSSearch.class, PSSearch.getComponentType(PSSearch.class), ignoreDependencies, session,
            user);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#deleteViews(List, boolean)
    */
   public void deleteViews(List<IPSGuid> ids, boolean ignoreDependencies, String session, String user)
         throws PSErrorsException

   {
      deleteSearches(ids, ignoreDependencies, session, user);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#findActions(String, String)
    */
   public List<IPSCatalogSummary> findActions(String name, String label, List<ActionType> types)
         throws PSErrorException
   {
      List<IPSDbComponent> actions = findComponentsByNameLabel(name, label, FIND_ACTIONS, PSAction.XML_NODE_NAME,
            PSAction.class);
      if (types == null || types.isEmpty())
         return getSummaries(actions);

      List<IPSDbComponent> result = new ArrayList<IPSDbComponent>();
      PSAction action;
      for (IPSDbComponent comp : actions)
      {
         action = (PSAction) comp;
         for (ActionType type : types)
         {
            if (ActionType._item.equals(type.getValue()) && action.isMenuItem())
               result.add(comp);
            else if (ActionType._cascading.equals(type.getValue()) && action.isCascadedMenu())
               result.add(comp);
            else if (ActionType._dynamic.equals(type.getValue()) && action.isDynamicMenu())
               result.add(comp);
         }
      }

      return getSummaries(result);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#findDisplayFormats(String, String)
    */
   public List<IPSCatalogSummary> findDisplayFormats(String name, String label) throws PSErrorException
   {
      List<IPSDbComponent> actions = findComponentsByNameLabel(name, label, FIND_DISPLAY_FORMAT, "PSXDisplayFormat",
            PSDisplayFormat.class);
      return getSummaries(actions);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#findHierarchyNodes(String, PSHierarchyNode.NodeType)
    */
   public List<IPSCatalogSummary> findHierarchyNodes(String path, PSHierarchyNode.NodeType type)
   {

      List<IPSGuid> nodeIds = new ArrayList<IPSGuid>();
      String tempPath = null;
      Map<IPSGuid, String> nodePathMap = new ConcurrentHashMap<IPSGuid, String>();
      Iterator nodePathIt = null;

      PSTimer timer = new PSTimer(ms_log);
      IPSUiService service = PSUiServiceLocator.getUiService();

      initializeHierarchyNodeMaps();

      // Get all the node ids from the map for the given type that have path
      // containing the path provided.

      if (path != null && !path.isEmpty() && !path.equals("*"))
      {
         if (path.charAt(0) == '/')
            path = path.substring(1, path.length());

         tempPath = path.substring(0, path.length() - 2);
      }

      if (type == null)
      {

         nodePathMap = getNodesPathMapForAllTypes();

      }
      else
      {

         nodePathMap = nodeIdToPathMap.get(type);

      }

      nodePathIt = nodePathMap.entrySet().iterator();

      while (nodePathIt.hasNext())
      {

         Map.Entry<IPSGuid, String> pathEntry = (Map.Entry<IPSGuid, String>) nodePathIt.next();

         if (path == null || path.isEmpty() || path.equals("*"))
            nodeIds.add(pathEntry.getKey());
         else
         {

            if (path.contains("*"))
            {

               if (pathEntry.getValue().contains(tempPath))
                  nodeIds.add(pathEntry.getKey());

            }
            else
            {
               if (pathEntry.getValue().equalsIgnoreCase(path))
               {
                  nodeIds.add(pathEntry.getKey());

                  break;
               }
            }
         }
      }

      // Load Hierarchy Node for all the node ids that have the provided path as
      // their path of part of it.
      List<PSHierarchyNode> resultNodes = new ArrayList<PSHierarchyNode>();

      for (IPSGuid id : nodeIds)
      {
         try
         {
            resultNodes.add(service.loadHierarchyNode(id));
         }
         catch (PSUiException e)
         {
            e.printStackTrace();
         }
      }

      timer.logElapsed("Ready to the return the catalog summary list in the findHierarchyNodes(String path, PSHierarchyNode.NodeType type) ....");

      return PSWebserviceUtils.toObjectSummaries(resultNodes);
   }

   /**
    * Method to initialize the static maps: Map of node type and another map(map
    * of node guid and its path) AND Map of object guid and node guid.
    */

   public void initializeHierarchyNodeMaps()
   {
      if (!initializing && objectIdToNodeIdMap == null)
      {
         synchronized (this)
         {
         
            if (!initializing && objectIdToNodeIdMap == null)
            {
               initializing = true;
            nodeIdToPathMap = initializeHierarchyNodes();
            objectIdToNodeIdMap = getAllHierarchyNodesGuidProperties();
               initializing = false;
         }
      }
   }
   }

   /**
    * Initialize the map - Map of node type and another map(map of node guid and
    * its path)
    * 
    * @return The initialized map
    */
   private Map<PSHierarchyNode.NodeType, Map<IPSGuid, String>> initializeHierarchyNodes()
   {

      IPSUiService service = PSUiServiceLocator.getUiService();
      ConcurrentHashMap<PSHierarchyNode.NodeType, Map<IPSGuid, String>> nodeMap = new ConcurrentHashMap<PSHierarchyNode.NodeType, Map<IPSGuid, String>>();
      Map<IPSGuid, String> folderNodeMap = new ConcurrentHashMap<IPSGuid, String>();
      Map<IPSGuid, String> placeHolderNodeMap = new ConcurrentHashMap<IPSGuid, String>();

      nodeMap.put(PSHierarchyNode.NodeType.FOLDER, new ConcurrentHashMap<IPSGuid, String>());
      nodeMap.put(PSHierarchyNode.NodeType.PLACEHOLDER, new ConcurrentHashMap<IPSGuid, String>());

      List<PSHierarchyNode> nodes = service.getAllHierarchyNodes();

      for (PSHierarchyNode node : nodes)
      {

         nodePath = new StringBuilder();

         if (node.getType().equals(PSHierarchyNode.NodeType.FOLDER))
            folderNodeMap.put(node.getGUID(), getNodePath(node, nodes));
         else
         {
            if (node.getType().equals(PSHierarchyNode.NodeType.PLACEHOLDER))
               placeHolderNodeMap.put(node.getGUID(), getNodePath(node, nodes));
         }
      }

      nodeMap.get(PSHierarchyNode.NodeType.FOLDER).putAll(folderNodeMap);
      nodeMap.get(PSHierarchyNode.NodeType.PLACEHOLDER).putAll(placeHolderNodeMap);

      return nodeMap;
   }

   /**
    * Generate a path string for a given node
    * 
    * @param node - for which the path string is to be created
    * @param nodes - list of all the nodes in the hierarchy node, so as to find
    *           the parent(s) of the given node
    * @return The path string generated here.
    */
   private String getNodePath(PSHierarchyNode node, List<PSHierarchyNode> nodes)
   {

      PSHierarchyNode parentNode = null;

      if (node.getParentId() == null)
      {

         nodePath.insert(0, "/" + node.getName());
      }
      else
      {

         nodePath.insert(0, "/" + node.getName());

         for (PSHierarchyNode pNode : nodes)
         {

            if (pNode.getGUID().equals(node.getParentId()))
            {

               parentNode = pNode;
               break;
            }
         }

         if (parentNode != null)
            getNodePath(parentNode, nodes);
         else
            //throw new IllegalArgumentException("Parent Node for the recursive call cannot be null");
            ms_log.error("Parent Node for the recursive call cannot be null. " + nodePath.toString());
      }

      return nodePath.toString();
   }

   /**
    * Get the object id of the node and create a map of the object guid and the
    * node guid of this object.
    * 
    * @return Map of object guid and node guid.
    */
   private HashMap<IPSGuid, IPSGuid> getAllHierarchyNodesGuidProperties()
   {

      IPSUiService service = PSUiServiceLocator.getUiService();
      HashMap<IPSGuid, IPSGuid> objectNodeMap = new HashMap<IPSGuid, IPSGuid>();
      HashMap<IPSGuid, String> allNodeTypeMap = getNodesPathMapForAllTypes();

      List<PSHierarchyNodeProperty> guidProps = service.getAllHierarchyNodesGuidProperties();

      for (PSHierarchyNodeProperty prop : guidProps)
      {

         if (allNodeTypeMap.containsKey(new PSGuid(prop.getNodeId())))

            // create the hashmap with the object guid as key and node id as the
            // value
            objectNodeMap.put(new PSGuid(prop.getValue()), new PSGuid(prop.getNodeId()));
      }

      return objectNodeMap;
   }

   /**
    * Combine the maps of node guid and path strings for the different node
    * types.
    * 
    * @return The combined map.
    */
   private HashMap<IPSGuid, String> getNodesPathMapForAllTypes()
   {

      HashMap<IPSGuid, String> nodeMap = new HashMap<IPSGuid, String>();

      initializeHierarchyNodeMaps();
      
      if (nodeIdToPathMap != null && !nodeIdToPathMap.isEmpty())
      {
         nodeMap.putAll(nodeIdToPathMap.get(PSHierarchyNode.NodeType.PLACEHOLDER));
         nodeMap.putAll(nodeIdToPathMap.get(PSHierarchyNode.NodeType.FOLDER));
      }

      return nodeMap;
   }

   /**
    * To recreate the static maps, at various occasions like after the
    * operations - delete, save, move children and remove children to be sure
    * that the static maps are up to date always.
    */
   private synchronized void recreateStaticMaps()
   {

      Map<PSHierarchyNode.NodeType, Map<IPSGuid, String>> newNodeIdToPathMap = initializeHierarchyNodes();
      Map<IPSGuid, IPSGuid> newObjectIdToNodeIdMap = getAllHierarchyNodesGuidProperties();

      nodeIdToPathMap = newNodeIdToPathMap;
      objectIdToNodeIdMap = newObjectIdToNodeIdMap;

   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#findSearches(String, String)
    */
   public List<IPSCatalogSummary> findSearches(String name, String label) throws PSErrorException
   {
      List<IPSDbComponent> searchViews = findComponentsByNameLabel(name, label, FIND_SEARCHES, PSSearch.XML_NODE_NAME,
            PSSearch.class);

      List<PSSearch> searches = getSearchOrViews(searchViews, false);
      return getSummaries(new ArrayList<IPSDbComponent>(searches));
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#findViews(String, String)
    */
   public List<IPSCatalogSummary> findViews(String name, String label) throws PSErrorException
   {
      List<IPSDbComponent> searchViews = findComponentsByNameLabel(name, label, FIND_SEARCHES, PSSearch.XML_NODE_NAME,
            PSSearch.class);

      List<PSSearch> searches = getSearchOrViews(searchViews, true);
      return getSummaries(new ArrayList<IPSDbComponent>(searches));
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#getChildren(IPSGuid)
    */
   public List<IPSGuid> getChildren(IPSGuid id)
   {
      IPSUiService service = PSUiServiceLocator.getUiService();

      List<PSHierarchyNode> children = service.findHierarchyNodes("%", id, null);

      return PSGuidUtils.getIds(children);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#idsToPaths(List)
    */
   @SuppressWarnings("unchecked")
   public List<String> idsToPaths(List<IPSGuid> ids) throws PSErrorResultsException
   {
      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids cannot be null or empty");

      PSErrorResultsException results = new PSErrorResultsException();
      for (IPSGuid id : ids)
      {
         try
         {
            List<PSHierarchyNode> tree = new ArrayList<PSHierarchyNode>();
            getHierarchyNodeTree(id, tree);

            results.addResult(id, treeToPath(tree));
         }
         catch (PSUiException e)
         {
            int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code, PSWebserviceErrors.createErrorMessage(code,
                  PSHierarchyNode.class.getName(), guid.getValue()), ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      if (results.hasErrors())
         throw results;

      return results.getResults(ids);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#loadActions(List, boolean, boolean, String, String)
    */
   @SuppressWarnings("unchecked")
   public List<PSAction> loadActions(List<IPSGuid> ids, boolean lock, boolean overrideLock, String session, String user)
         throws PSErrorResultsException
   {
      PSWebserviceUtils.validateParameters(ids, "ids", lock, session, user);

      return loadComponents(ids, PSAction.class, PSAction.getComponentType(PSAction.class), lock, overrideLock,
            session, user);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#loadDisplayFormats(List, boolean, boolean, String,
    * String)
    */
   @SuppressWarnings("unchecked")
   public List<PSDisplayFormat> loadDisplayFormats(List<IPSGuid> ids, boolean lock, boolean overrideLock,
         String session, String user) throws PSErrorResultsException
   {
      PSWebserviceUtils.validateParameters(ids, "ids", lock, session, user);

      return loadComponents(ids, PSDisplayFormat.class, PSDisplayFormat.getComponentType(PSDisplayFormat.class), lock,
            overrideLock, session, user);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.ui.IPSUiDesignWs#findDisplayFormat(com.percussion.utils.guid.IPSGuid)
    */
   public PSDisplayFormat findDisplayFormat(IPSGuid id)
   {

      PSDisplayFormat dispFormat =  loadDisplayFormat(id);

      return dispFormat;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.webservices.ui.IPSUiDesignWs#findDisplayFormat(java.lang.String)
    */
   public PSDisplayFormat findDisplayFormat(String name)
   {
      notEmpty(name);

      // load from repository
      PSDisplayFormat dispFormat = loadDisplayFormat(name);

      return dispFormat;
   }

   /**
    * Loads a specified display format from repository.
    * @param name the name of the display format, assumed not <code>null</code> or empty.
    * @return the specified display format, it may be <code>null</code> if cannot find one.
    */
   private PSDisplayFormat loadDisplayFormat(String name)
   {
      List<IPSCatalogSummary> results = findDisplayFormats(name, null);
      for (IPSCatalogSummary summary : results)
      {
         if (summary.getName().equalsIgnoreCase(name))
         {
            return loadDisplayFormat(summary.getGUID());
         }
      }
      return null;
   }
   
   /**
    * Loads a display format for the given ID.
    * 
    * @param id the ID of the display format in question, assumed not <code>null</code>.
    * 
    * @see IPSUiDesignWs#loadHierachyNodes(List, boolean, boolean, 
    *    String, String)
    * @see IPSUiDesignWs#loadHierachyNodes(List, boolean, boolean, String,
    * String)
    * @return the display format, it may be <code>null</code> if it does not exist or error occurs.
    */
   private PSDisplayFormat loadDisplayFormat(IPSGuid id)
   {
      try
      {
         List<PSDisplayFormat> dispList = loadDisplayFormats(Collections.singletonList(id), false, false, null, null);
         return dispList.isEmpty() ? null : dispList.get(0);
      }
      catch (PSErrorResultsException e)
      {
         ms_log.error("Failed to load display format with id = " + id.toString(), e);
         return null;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#loadHierachyNodes(List, boolean, boolean, 
    *    String, String)
    */
   @SuppressWarnings("unchecked")
   public List<PSHierarchyNode> loadHierachyNodes(List<IPSGuid> ids, boolean lock, boolean overrideLock,
         String session, String user) throws PSErrorResultsException
   {
      if (PSGuidUtils.isBlank(ids))
         throw new IllegalArgumentException("ids cannot be null or empty");

      if (lock && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (lock && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSUiService service = PSUiServiceLocator.getUiService();

      PSErrorResultsException results = new PSErrorResultsException();
      for (IPSGuid id : ids)
      {
         try
         {
            PSHierarchyNode node = service.loadHierarchyNode(id);
            results.addResult(id, node);
         }
         catch (PSUiException e)
         {
            int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code, PSWebserviceErrors.createErrorMessage(code,
                  PSHierarchyNode.class.getName(), guid.getValue()), ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      if (lock)
      {
         IPSObjectLockService lockService = PSObjectLockServiceLocator.getLockingService();
         lockService.createLocks(results, session, user, overrideLock);
      }

      if (results.hasErrors())
         throw results;

      return results.getResults(ids);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#loadSearches(List, boolean, boolean, String, String)
    */
   @SuppressWarnings("unchecked")
   public List<PSSearch> loadSearches(List<IPSGuid> ids, boolean lock, boolean overrideLock, String session, String user)
         throws PSErrorResultsException
   {
      return loadSearchViews(ids, lock, overrideLock, session, user, false);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#loadViews(List, boolean, boolean, String, String)
    */
   public List<PSSearch> loadViews(List<IPSGuid> ids, boolean lock, boolean overrideLock, String session, String user)
         throws PSErrorResultsException
   {
      return loadSearchViews(ids, lock, overrideLock, session, user, true);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#moveChildren(IPSGuid, IPSGuid, List)
    */
   public void moveChildren(IPSGuid source, IPSGuid target, List<IPSGuid> ids)
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");

      if (target == null)
         throw new IllegalArgumentException("target cannot be null");

      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids cannot be null or empty");

      IPSUiService service = PSUiServiceLocator.getUiService();

      service.moveChildren(source, target, ids);

      // Recreate the maps nodeIdToPathMap and objectIdToNodeIdMap
      recreateStaticMaps();
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#pathsToIds(List)
    */
   @SuppressWarnings("unchecked")
   public List<List<IPSGuid>> pathsToIds(List<String> paths) throws PSErrorException
   {
      if (paths == null || paths.isEmpty())
         throw new IllegalArgumentException("paths cannot be null or empty");

      List<List<IPSGuid>> results = new ArrayList<List<IPSGuid>>();
      for (String path : paths)
      {
         List<PSHierarchyNode> tree = getHierarchyNodeTree(path);
         results.add(PSGuidUtils.toGuidList(tree));
      }

      return results;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#removeChildren(IPSGuid, List)
    */
   public void removeChildren(IPSGuid parent, List<IPSGuid> ids)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent cannot be null");

      if (ids == null || ids.isEmpty())
         throw new IllegalArgumentException("ids cannot be null or empty");

      IPSUiService service = PSUiServiceLocator.getUiService();

      service.removeChildren(parent, ids);

      // Recreate the maps nodeIdToPathMap and objectIdToNodeIdMap
      recreateStaticMaps();
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#saveActions(List, boolean, String, String)
    */
   public void saveActions(List<PSAction> actions, boolean release, String session, String user)
         throws PSErrorsException
   {
      PSWebserviceUtils.validateParameters(actions, "actions", true, session, user);

      List<IPSDbComponent> components = new ArrayList<IPSDbComponent>(actions);
      saveComponents(components, PSAction.class, release, session, user);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#saveDisplayFormats(List, boolean, String, String)
    */
   public void saveDisplayFormats(List<PSDisplayFormat> displayFormats, boolean release, String session, String user)
         throws PSErrorsException
   {
      PSWebserviceUtils.validateParameters(displayFormats, "displayFormats", true, session, user);

      List<IPSDbComponent> components = new ArrayList<IPSDbComponent>(displayFormats);
      saveComponents(components, PSDisplayFormat.class, release, session, user);

   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#saveHierarchyNodes(List, boolean, String, String)
    */
   public void saveHierarchyNodes(List<PSHierarchyNode> nodes, boolean release, String session, String user)
         throws PSErrorsException
   {
      if (nodes == null || nodes.isEmpty())
         throw new IllegalArgumentException("nodes cannot be null or empty");

      if (release && StringUtils.isBlank(session))
         throw new IllegalArgumentException("session cannot be null or empty");

      if (release && StringUtils.isBlank(user))
         throw new IllegalArgumentException("user cannot be null or empty");

      IPSUiService service = PSUiServiceLocator.getUiService();

      List<IPSGuid> ids = PSGuidUtils.toGuidList(nodes);

      IPSObjectLockService lockService = PSObjectLockServiceLocator.getLockingService();

      PSErrorsException results = new PSErrorsException();
      for (PSHierarchyNode node : nodes)
      {
         IPSGuid id = node.getGUID();
         try
         {
            if (lockService.isLockedFor(id, session, user))
            {
               // set the correct version
               Integer version = lockService.getLockedVersion(id);
               if (version != null)
                  node.setVersion(version);

               // save the object and extend the lock
               service.saveHierarchyNode(node);
               if (!release)
                  lockService.extendLock(id, session, user, node.getVersion());

               results.addResult(id);
            }
            else
            {
               PSObjectLock lock = lockService.findLockByObjectId(id, null, null);
               if (lock == null)
               {
                  int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED;
                  PSDesignGuid guid = new PSDesignGuid(id);
                  PSErrorException error = new PSErrorException(code, PSWebserviceErrors.createErrorMessage(code,
                        PSHierarchyNode.class.getName(), guid.getValue()),
                        ExceptionUtils.getFullStackTrace(new Exception()));
                  results.addError(id, error);
               }
               else
               {
                  int code = IPSWebserviceErrors.OBJECT_NOT_LOCKED_FOR_REQUESTOR;
                  PSDesignGuid guid = new PSDesignGuid(id);
                  PSErrorException error = new PSErrorException(code, PSWebserviceErrors.createErrorMessage(code,
                        PSHierarchyNode.class.getName(), guid.getValue(), lock.getLocker(), lock.getRemainingTime()),
                        ExceptionUtils.getFullStackTrace(new Exception()));
                  results.addError(id, error);
               }
            }
         }
         catch (PSLockException e)
         {
            int code = IPSWebserviceErrors.SAVE_FAILED;
            PSDesignGuid guid = new PSDesignGuid(id);
            PSErrorException error = new PSErrorException(code, PSWebserviceErrors.createErrorMessage(code,
                  PSHierarchyNode.class.getName(), guid.getValue(), e.getLocalizedMessage()),
                  ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
      }

      if (release)
      {
         List<PSObjectLock> locks = lockService.findLocksByObjectIds(ids, session, user);
         lockService.releaseLocks(locks);
      }

      // Recreate the maps nodeIdToPathMap and objectIdToNodeIdMap
      recreateStaticMaps();

      if (results.hasErrors())
         throw results;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#saveSearches(List, boolean, String, String)
    */
   public void saveSearches(List<PSSearch> searches, boolean release, String session, String user)
         throws PSErrorsException
   {
      PSWebserviceUtils.validateParameters(searches, "searches", true, session, user);

      List<IPSDbComponent> components = new ArrayList<IPSDbComponent>();
      // Clean the community property from the components before saving
      for (PSSearch s : searches)
      {
         s = (PSSearch) s.cloneFull();
         String values[] = s.getPropertyValues("sys_community");
         if (values != null)
         {
            for (String comm : values)
            {
               s.removeProperty("sys_community", comm);
            }
         }
         components.add(s);
      }
      saveComponents(components, PSSearch.class, release, session, user);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSUiDesignWs#saveViews(List, boolean, String, String)
    */
   public void saveViews(List<PSSearch> views, boolean release, String session, String user) throws PSErrorsException
   {
      saveSearches(views, release, session, user);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.webservices.ui.IPSUiDesignWs#findAllSearches()
    */
   public List<PSSearch> findAllSearches() throws PSErrorResultsException, PSErrorException
   {
      IPSCacheAccess cache = PSCacheAccessLocator.getCacheAccess();
      Vector<PSSearch> searches = (Vector<PSSearch>) cache.get(ALL_SEARCHES_CACHE_KEY, IPSCacheAccess.IN_MEMORY_STORE);
      if (searches == null)
      {
         List<IPSDbComponent> searchViews = findComponentsByNameLabel(null, null, FIND_SEARCHES,
               PSSearch.XML_NODE_NAME, PSSearch.class);
         List<PSSearch> s = getSearchOrViews(searchViews, false);
         searches = new Vector<PSSearch>();
         searches.addAll(s);
         cache.save(ALL_SEARCHES_CACHE_KEY, searches, IPSCacheAccess.IN_MEMORY_STORE);
      }
      return searches;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.webservices.ui.IPSUiDesignWs#objectIdToPath(IPSGuid
    * guid)
    */
   @SuppressWarnings("unchecked")
   public String objectIdToPath(IPSGuid guid) throws PSErrorsException
   {
      PSTimer timer = new PSTimer(ms_log);
      IPSUiService service = PSUiServiceLocator.getUiService();
      PSErrorsException results = new PSErrorsException();
      IPSGuid id = null;
      Map<IPSGuid, String> paths;
      String nodePath = "";

      if (nodeIdToPathMap == null || nodeIdToPathMap.isEmpty())
         initializeHierarchyNodeMaps();

      paths = nodeIdToPathMap.get(PSHierarchyNode.NodeType.PLACEHOLDER);

      IPSGuid node = objectIdToNodeIdMap.get(guid);
      nodePath = (node==null)? null : paths.get(node);

      // Only create dummy Navigation folder for content type guid.
      if (nodePath == null || nodePath.isEmpty() && (guid.getType() == PSTypeEnum.NODEDEF.ordinal()))
      {
         try
         {
            if (PSNavConfig.isManagedNavUsed())
            {
               PSNavConfig config = PSNavConfig.getInstance();
               if (config.getNavonType() == guid.getUUID())
                  nodePath = CONTENTTYPES_NAV_PATH + config.getPropertyString(config.NAVON_CONTENT_TYPE);
               if (config.getNavImageType() == guid.getUUID())
                  nodePath = CONTENTTYPES_NAV_PATH + config.getPropertyString(config.NAVIMAGE_CONTENT_TYPE);
               if (config.getNavTreeType() == guid.getUUID())
                  nodePath = CONTENTTYPES_NAV_PATH + config.getPropertyString(config.NAVTREE_CONTENT_TYPE);
            }
         }

         catch (PSNavException e)
         {
            int code = IPSWebserviceErrors.FAILED_TO_OBTAIN_PATH_FROM_OBJECT_ID;
            PSErrorException error = new PSErrorException(code, PSWebserviceErrors.createErrorMessage(code,
                  e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);

         }
      }
      if (results.hasErrors())
         throw results;

      timer.logElapsed("Got the node path '" +nodePath+ "' for the guid " + guid);

      return nodePath != null ? nodePath : "";
   }

   /**
    * Spring property accessor
    * 
    * @return get the cache service
    */
   public IPSCacheAccess getCache()
   {
      return m_cache;
   }

   /**
    * Set the cache service
    * 
    * @param cache the service, never <code>null</code>
    */
   public void setCache(IPSCacheAccess cache)
   {
      if (cache == null)
      {
         throw new IllegalArgumentException("cache may not be null");
      }
      m_cache = cache;

      PSServer.addInitListener(new EvictionListener(m_cache));
   }

   /**
    * Creates an action object from a name and a type.
    * 
    * @param name the name of the action, assumed not <code>null</code>.
    * @param type the type of the action, assumed not <code>null</code> or
    *           empty. It must be either {@link PSAction#TYPE_MENUITEM} or
    *           {@link PSAction#TYPE_MENU}.
    * 
    * @return the created action object, never <code>null</code>.
    * 
    * @throws RuntimeException if failed to get the next available id.
    */
   private PSAction createAction(String name, ActionType type)
   {
      PSAction source = new PSAction(name, name);
      if (ActionType._item.equals(type.getValue()))
      {
         source.setMenuType(PSAction.TYPE_MENUITEM);
      }
      else if (ActionType._cascading.equals(type.getValue()))
      {
         source.setMenuType(PSAction.TYPE_MENU);
      }
      else if (ActionType._dynamic.equals(type.getValue()))
      {
         source.setMenuType(PSAction.TYPE_MENU);
         source.setMenuDynamic(true);
      }
      source.setClientAction(false);
      int id = getNextId(PSTypeEnum.ACTION.getKey());
      PSKey key = PSAction.createKey(String.valueOf(id));
      key.setPersisted(false);
      source.setLocator(key);

      return source;
   }

   /**
    * Creates a displayformat object from a name.
    * 
    * @param name the name of the displayformat, assumed not <code>null</code>.
    * 
    * @return the created displayformat object, never <code>null</code>.
    * 
    * @throws RuntimeException if failed to get the next available id.
    */
   private PSDisplayFormat createDisplayFormat(String name)
   {
      PSDisplayFormat source;
      try
      {
         source = new PSDisplayFormat();
      }
      catch (PSCmsException e)
      {
         // not possible
         e.printStackTrace();
         throw new RuntimeException("Failed to create displayformat object.", e);
      }

      source.setInternalName(name);
      source.setDisplayName(name);
      source.setDescription(name);
      int id = getNextId(PSTypeEnum.DISPLAY_FORMAT.getKey());
      PSKey key = PSDisplayFormat.createKey(new String[]
      {String.valueOf(id)});
      key.setPersisted(false);
      source.setLocator(key);
      // don't need to set allowed communities for workbench client
      // source.setAllowedCommunities(getAllCommunities());

      return source;
   }

   /**
    * Validates the specified component names, to make sure each name is not
    * blank, does not contain space character and is not used by an existing
    * component (case insensitive).
    * 
    * @param names the names in question; assumed not <code>null</code> or
    *           empty.
    * @param resourcePath the resource path used to lookup the components,
    *           assumed not <code>null</code> or empty.
    * @param nodeName the XML node name of the component, assumed not
    *           <code>null</code> or empty.
    * @pram type the type of the component; assumed not <code>null</code>. It
    *       must be one of the {@link PSTypeEnum#ACTION},
    *       {@link PSTypeEnum#SEARCH_DEF} or {@link PSTypeEnum#DISPLAY_FORMAT}.
    * @param objClass the class of the component, assumed not <code>null</code>.
    *           It must be {@link PSAction} for if type is
    *           {@link PSTypeEnum#ACTION}, {@link PSSearch} for if type is
    *           {@link PSTypeEnum#SEARCH_DEF}, or {@link PSDisplayFormat} for if
    *           type is {@link PSTypeEnum#DISPLAY_FORMAT}.
    * 
    * @throws PSErrorException if failed to catalog the specified component.
    */
   private void validateComponentNames(List<String> names, String resourcePath, String nodeName, PSTypeEnum type,
         Class objClass) throws PSErrorException
   {
      // catalog the names of all component objects
      List<IPSDbComponent> comps = findComponentsByNameLabel(null, null, resourcePath, nodeName, objClass);
      List<String> existNames = new ArrayList<String>();
      for (IPSDbComponent comp : comps)
      {
         String name = null;
         if (type == PSTypeEnum.ACTION)
         {
            name = ((PSAction) comp).getName();
         }
         else if (type == PSTypeEnum.SEARCH_DEF)
         {
            name = ((PSSearch) comp).getName();
         }
         else if (type == PSTypeEnum.DISPLAY_FORMAT)
         {
            name = ((PSDisplayFormat) comp).getName();
         }
         else
         {
            throw new IllegalArgumentException("type must be ACTION, SEARCH_DEF or DISPLAY_FORMAT");
         }

         existNames.add(name.toLowerCase());
      }

      // validating the specified names
      for (String name : names)
      {
         if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("name must not be null or empty.");
         if (StringUtils.contains(name, ' '))
            throw new IllegalArgumentException("name cannot contain spaces");

         if (existNames.contains(name.toLowerCase()))
         {
            PSWebserviceUtils.throwObjectExistException(name, type);
         }
      }
   }

   /**
    * Creates a search definition from a name and a type.
    * 
    * @param name the name of the new search def, assumed not <code>null</code>
    *           or empty.
    * @param type the search type, it must be one of the PSSearch.TYPE_XXXSEARCH
    *           values.
    * 
    * @return the created object, never <code>null</code>.
    */
   private PSSearch createSearch(String name, String type)
   {
      if ((!type.equals(PSSearch.TYPE_STANDARDSEARCH)) && (!type.equals(PSSearch.TYPE_CUSTOMSEARCH))
            && (!type.equals(PSSearch.TYPE_USERSEARCH)) && (!type.equals(PSSearch.TYPE_VIEW)))
         throw new IllegalArgumentException("type must be one of the following values, " + PSSearch.TYPE_STANDARDSEARCH
               + ", " + PSSearch.TYPE_CUSTOMSEARCH + ", " + PSSearch.TYPE_USERSEARCH + ", " + PSSearch.TYPE_VIEW + ".");

      PSSearch source;

      try
      {
         source = new PSSearch(name);
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         throw new RuntimeException("Failed creating PSSearch with \"" + name + "\" and \"" + type
               + "\" type, due to exception: " + e);
      }
      source.setType(type);

      int id = getNextId(PSTypeEnum.SEARCH_DEF.getKey());
      PSKey key = PSSearch.createKey(new String[]
      {String.valueOf(id)});
      key.setPersisted(false);
      source.setLocator(key);

      return source;
   }

   /**
    * Deletes a list of components specified by the given parameters.
    * 
    * @param ids the ids of the components, assumed not <code>null</code>.
    * @param objClass the class of the component, assumed not <code>null</code>.
    * @param objType the component type, assumed not <code>null</code> or empty.
    * @param ignoreDependencies the ignore dependencies flag, see other public
    *           deleteXXX API for detail.
    * @param session current session, assumed not <code>null</code> or empty.
    * @param user current user name, assumed not <code>null</code> or empty.
    * 
    * @throws PSErrorsException if an error occurs.
    */
   private void deleteComponents(List<IPSGuid> ids, Class objClass, String objType, boolean ignoreDependencies,
         String session, String user) throws PSErrorsException
   {
      PSErrorsException results = new PSErrorsException();
      for (IPSGuid id : ids)
      {
         PSKey key = getComponentKey(id, objType);

         if (!ignoreDependencies)
         {
            PSErrorException error = PSWebserviceUtils.checkDependencies(id);
            if (error != null)
            {
               results.addError(id, error);
               continue;
            }
         }

         if (PSWebserviceUtils.hasValidLockForDelete(id, session, user))
         {
            deleteComponentReqired(key, objType, id, objClass, results);
            results.addResult(id);
         }
         else
         {
            PSWebserviceUtils.handleMissingLockError(id, objClass, results);
         }
      }

      // release locks for all successfully deleted objects
      PSWebserviceUtils.releaseLocks(results.getResults(), session, user);

      if (results.hasErrors())
         throw results;
   }

   /**
    * Gets the next available id for the given key.
    * 
    * @param key the key used to get the next number, assumed not
    *           <code>null</code> or empty.
    * 
    * @return the next number.
    */
   private int getNextId(String key)
   {
      try
      {
         return PSIdGenerator.getNextId(key);
      }
      catch (SQLException e)
      {
         // this is not possible
         e.printStackTrace();
         throw new RuntimeException("Failed to get next id for \"" + key + "\", due to exception.", e);
      }
   }

   /**
    * Convenience method, just like
    * {@link #deleteComponent(PSKey, String, IPSGuid, Class, PSErrorsException)}
    * except this method does not throw exception.
    * 
    * @param results it used to store the GUID if successful or an
    *           {@link PSErrorException} object if failed the delete operation.
    *           It may not be <code>null</code>.
    */
   private void deleteComponentReqired(PSKey key, String componentType, IPSGuid id, Class cz, PSErrorsException results)
   {
      if (results == null)
         throw new IllegalArgumentException("result may not be null.");

      try
      {
         deleteComponent(key, componentType, id, cz, results);
      }
      catch (PSCmsException e)
      {
         // This is NOT possible, ignore
      }

   }

   /**
    * Converts a list of components to a list of catalog summaries.
    * 
    * @param components the to be converted components, assumed not
    *           <code>null</code> and it has implemented
    *           {@link IPSCatalogSummary}.
    * @return the converted catalog summaries, never <code>null</code>, but may
    *         be empty.
    */
   private List<IPSCatalogSummary> getSummaries(List<IPSDbComponent> components)
   {
      return PSWebserviceUtils.toObjectSummaries(components);
   }

   /**
    * Just like
    * {@link IPSUiDesignWs#loadSearches(List, boolean, boolean, String, String)}
    * or {@link IPSUiDesignWs#loadViews(List, boolean, boolean, String, String)}
    * , except this is loading either searches or views according to isView
    * parameter.
    * 
    * @param isView <code>true</code> if loading views; otherwise loading
    *           searches.
    */
   private List<PSSearch> loadSearchViews(List<IPSGuid> ids, boolean lock, boolean overrideLock, String session,
         String user, boolean isView) throws PSErrorResultsException
   {
      PSWebserviceUtils.validateParameters(ids, "ids", lock, session, user);

      List sv = loadComponents(ids, PSSearch.class, PSSearch.getComponentType(PSSearch.class), lock, overrideLock,
            session, user);

      return getSearchOrViews(sv, isView);
   }

   /**
    * Loads the specified components.
    * 
    * @param ids the ids of the components to be loaded, assumed not
    *           <code>null</code>.
    * @param objClass the component class, assumed not <code>null</code>.
    * @param objType the component type, assumed not <code>null</code> or empty.
    * @param lock the lock flag, see public saveXXXX() API for detail.
    * @param overrideLock override lock flag, see public saveXXX() API for
    *           detail.
    * @param session current session, assumed not <code>null</code> or empty.
    * @param user current user, assumed not <code>null</code> or empty.
    * @return the loaded components, never <code>null</code>.
    * @throws PSErrorResultsException if an error occurs.
    */
   private List loadComponents(List<IPSGuid> ids, Class objClass, String objType, boolean lock, boolean overrideLock,
         String session, String user) throws PSErrorResultsException
   {
      IPSObjectLockService lockService = PSObjectLockServiceLocator.getLockingService();

      PSErrorResultsException results = new PSErrorResultsException();
      for (IPSGuid id : ids)
      {
         PSKey key = getComponentKey(id, objType);

         try
         {
            // load the component
            IPSDbComponent component = loadComponent(key, objType, id, objClass);

            // create or extend lock with version from component if versionable,
            // otherwise set version = 1
            if (lock)
            {
               Integer version;
               if (component instanceof PSVersionableDbComponent)
               {
                  version = ((PSVersionableDbComponent) component).getVersion();
               }
               else
               {
                  version = new Integer(1);
               }

               lockService.createLock(id, session, user, version, overrideLock);
            }

            results.addResult(id, component);
         }
         catch (PSErrorException e)
         {
            results.addError(id, e);
         }
         catch (PSLockException e)
         {
            results.addError(id, e);
         }
      }

      if (results.hasErrors())
         throw results;

      return results.getResults(ids);
   }

   /**
    * Create a component key for the supplied id and object type.
    * 
    * @param id the id for which to create the component key, assumed not
    *           <code>null</code>.
    * @param objType the type of the object for which to create the component
    *           key, assumed not <code>null</code> or empty.
    * @return the component key for the specified id and object type, never
    *         <code>null</code>.
    */
   private PSKey getComponentKey(IPSGuid id, String objType)
   {
      PSKey key = null;

      if (objType.equals(PSAction.getComponentType(PSAction.class)))
         key = PSAction.createKey(String.valueOf(id.longValue()));
      else if (objType.equals(PSDisplayFormat.getComponentType(PSDisplayFormat.class)))
         key = PSDisplayFormat.createKey(new String[]
         {String.valueOf(id.longValue())});
      else if (objType.equals(PSSearch.getComponentType(PSSearch.class)))
         key = PSSearch.createKey(new String[]
         {String.valueOf(id.longValue())});
      else
         // should never happen
         throw new RuntimeException("Cannot create component key for object type: " + objType);

      return key;
   }

   /**
    * Saves a list of components. It will delete a persisted component, then
    * save the updated one to avoid merging child components between the
    * original and the updated component. For a persisted component, it is
    * assumed the version property of its lock is not <code>null</code>;
    * otherwise it is not a persisted component.
    * 
    * @param components the to be saved component list, assumed not
    *           <code>null</code> or empty. The component must implemented
    *           {@link IPSCatalogSummary}.
    * @param cz the class of the saved component, assumed not <code>null</code>.
    * @param release <code>true</code> to release all object locks after the
    *           save, <code>false</code> to keep the locks. Defaults to
    *           <code>true</code> if not supplied.
    * @param session the rhythmyx session for which to release the saved
    *           objects, not <code>null</code> or empty.
    * @param user the user for which to release the saved objects, not
    *           <code>null</code> or empty.
    * 
    * @throws PSErrorsException if failed to save at least one of the component.
    */
   private void saveComponents(List<IPSDbComponent> components, Class cz, boolean release, String session, String user)
         throws PSErrorsException
   {
      IPSObjectLockService lockService = PSObjectLockServiceLocator.getLockingService();

      PSErrorsException results = new PSErrorsException();
      List<IPSGuid> releasedIds = new ArrayList<IPSGuid>();
      for (IPSDbComponent component : components)
      {
         IPSCatalogSummary summary = (IPSCatalogSummary) component;
         IPSGuid id = summary.getGUID();
         if (lockService.isLockedFor(id, session, user))
         {
            try
            {
               Integer version = lockService.getLockedVersion(id);

               if (!saveComponent(component, id, cz, results, version))
                  continue;

               if (!release)
               {
                  if (component instanceof PSVersionableDbComponent)
                  {
                     version = ((PSVersionableDbComponent) component).getVersion();
                  }
                  else
                  {
                     version = new Integer(1);
                  }

                  PSWebserviceUtils.extendLock(id, cz, session, user, version, results);
               }
               else
               {
                  releasedIds.add(id);
               }
               results.addResult(id);
            }
            catch (Exception e)
            {
               results.addError(id, e);
               continue;
            }
         }
         else
         {
            PSWebserviceUtils.handleMissingLockError(id, cz, results);
         }
      }

      if (release)
      {
         List<PSObjectLock> locks = lockService.findLocksByObjectIds(releasedIds, session, user);
         lockService.releaseLocks(locks);
      }

      if (results.hasErrors())
         throw results;
   }

   /**
    * Save a specified component.
    * 
    * @param comp the to be saved component, assumed not <code>null</code>.
    * @param id the GUID of the component, assumed not <code>null</code>.
    * @param cz the class of the to be saved component, assumed not
    *           <code>null</code>.
    * @param results the object to add the failure info, assumed not
    *           <code>null</code>.
    * @param version version to restore on the template before saving it.
    * 
    * @return <code>true</code> if successfully saved the component;
    *         <code>false</code> if failed to save the component and added the
    *         error into the <code>results</code>.
    */
   private boolean saveComponent(IPSDbComponent comp, IPSGuid id, Class cz, PSErrorsException results, Integer version)
   {
      try
      {
         Integer saveVersion;
         if (version != null)
         {
            // delete the original component first, then save the updated object
            // this is a quick & dirty updating, so that we don't have to
            // "merge" all child lists.
            deleteComponent(comp.getLocator(), comp.getComponentType(), id, cz, null);

            saveVersion = version;
         }
         else
         {
            saveVersion = new Integer(0);
         }

         if (comp instanceof PSVersionableDbComponent)
         {
            ((PSVersionableDbComponent) comp).setVersion(saveVersion);
         }

         getComponentProxy().save(new IPSDbComponent[]
         {comp});
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         int code = IPSWebserviceErrors.SAVE_FAILED;
         PSDesignGuid guid = new PSDesignGuid(id);
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors.createErrorMessage(code, cz.getName(),
               guid.longValue(), e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
         results.addError(guid, error);
         return false;
      }

      return true;
   }

   /**
    * Loads a component with the supplied id.
    * 
    * @param key the to be loaded component key.
    * @param componentType the to be loaded component type.
    * @param id the to be loaded component id.
    * @param objClass the to be loaded component class.
    * 
    * @return the loaded component, never <code>null</code>.
    * 
    * @throws PSErrorException if cannot find the component or failed to load
    *            due to an error.
    */
   private IPSDbComponent loadComponent(PSKey key, String componentType, IPSGuid id, Class objClass)
         throws PSErrorException
   {
      Element[] elem = null;
      PSDesignGuid guid = new PSDesignGuid(id);
      try
      {
         elem = getComponentProxy().load(componentType, new PSKey[]
         {key});

         if (elem.length == 0) // if failed to load the object (not exist)
         {
            int code = IPSWebserviceErrors.OBJECT_NOT_FOUND;
            PSErrorException error = new PSErrorException(code, PSWebserviceErrors.createErrorMessage(code,
                  objClass.getName(), guid.longValue()), ExceptionUtils.getFullStackTrace(new Exception()));
            throw error;
         }

         return createComponent(elem[0], objClass);
      }
      catch (PSCmsException e)
      {
         int code = IPSWebserviceErrors.LOAD_FAILED;
         PSErrorException error = new PSErrorException(code, PSWebserviceErrors.createErrorMessage(code,
               objClass.getName(), guid.longValue()), ExceptionUtils.getFullStackTrace(e));
         throw error;
      }
   }

   /**
    * Get the children recursive for all supplied nodes.
    * 
    * @param nodes the nodes for which to get the children recursive, assumed
    *           not null, may be empty.
    * @param type the node type for which to filter the results, may be
    *           <code>null</code> to ignore this filter.
    * @param results the list into which to collect all results, assumed not
    *           <code>null</code>, may be empty.
    */
   private void getChildrenRecursive(List<PSHierarchyNode> nodes, PSHierarchyNode.NodeType type,
         List<PSHierarchyNode> results)
   {
      IPSUiService service = PSUiServiceLocator.getUiService();

      for (PSHierarchyNode node : nodes)
      {
         List<PSHierarchyNode> children = service.findHierarchyNodes("%", node.getGUID(), type);
         results.addAll(children);

         getChildrenRecursive(children, type, results);
      }
   }

   /**
    * Get the hierarchy node tree starting with the supplied id up the tree to
    * the root.
    * 
    * @param id the id of the node to start with, not <code>null</code>.
    * @param tree the tree into which to fill the found nodes, not
    *           <code>null</code> may be empty. The list is filled with the root
    *           node first following by all sub nodes.
    * @throws PSUiException if any of the specified or referenced node is not
    *            found.
    */
   private void getHierarchyNodeTree(IPSGuid id, List<PSHierarchyNode> tree) throws PSUiException
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");

      if (tree == null)
         throw new IllegalArgumentException("tree cannot be null");

      IPSUiService service = PSUiServiceLocator.getUiService();

      PSHierarchyNode node = service.loadHierarchyNode(id);
      IPSGuid parentId = node.getParentId();
      if (parentId != null)
         getHierarchyNodeTree(parentId, tree);

      tree.add(node);
   }

   /**
    * Convert the supplied list of nodes into a path string of the form
    * <code>/tree[0].getName()/tree[1].getName()/...</code>.
    * 
    * @param tree the list of nodes to convert, not <code>null</code>, may be
    *           empty.
    * @return the path string, never <code>null</code> or empty.
    */
   private String treeToPath(List<PSHierarchyNode> tree)
   {
      if (tree == null)
         throw new IllegalArgumentException("tree cannot be null");

      StringBuffer path = new StringBuffer();
      for (PSHierarchyNode node : tree)
         path.append("/" + node.getName());

      return path.toString();
   }

   /**
    * Get the hierarchy node tree for the supplied path.
    * 
    * @param path the path for which to get the node tree, not <code>null</code>
    *           or empty.
    * @return the hierarchy node tree as list stating with the root node, never
    *         <code>null</code> or empty.
    * @throws PSErrorException if any of the nodes defined in the path is not
    *            found.
    */
   private List<PSHierarchyNode> getHierarchyNodeTree(String path) throws PSErrorException
   {
      if (StringUtils.isBlank(path))
         throw new IllegalArgumentException("path cannot be null or empty");

      IPSUiService service = PSUiServiceLocator.getUiService();

      List<PSHierarchyNode> tree = new ArrayList<PSHierarchyNode>();
      IPSGuid parentId = null;
      String[] names = StringUtils.split(path, '/');
      for (String name : names)
      {
         List<PSHierarchyNode> nodes = service.findHierarchyNodes(name, parentId, null);
         if (nodes.isEmpty())
         {
            String parent = "root";
            if (parentId != null)
               parent = parentId.toString();

            int code = IPSWebserviceErrors.MISSING_HIERARCHY_NODE_FOR_PARENT;
            PSErrorException error = new PSErrorException(code, PSWebserviceErrors.createErrorMessage(code, name,
                  parent), ExceptionUtils.getFullStackTrace(new Exception()));
            throw error;
         }
         if (nodes.size() > 1)
         {
            String parent = "root";
            if (parentId != null)
               parent = parentId.toString();

            int code = IPSWebserviceErrors.DUPLICATE_HIERARCHY_NODE_FOR_PARENT;
            PSErrorException error = new PSErrorException(code, PSWebserviceErrors.createErrorMessage(code, name,
                  parent), ExceptionUtils.getFullStackTrace(new Exception()));
            throw error;
         }

         PSHierarchyNode node = nodes.get(0);
         parentId = node.getGUID();
         tree.add(node);
      }

      return tree;
   }

   /**
    * Deletes a specified component.
    * 
    * @param key the key of the component, assumed not <code>null</code>.
    * @param id the GUID of the component, assumed not <code>null</code>.
    * @param cz the class of the to be deleted object, assumed not
    *           <code>null</code>.
    * @param results it used to store the GUID if successful or an
    *           {@link PSErrorException} object if failed the delete operation.
    *           It may be <code>null</code> if the caller wants to catch the
    *           exception due the failure of the delete operation.
    * 
    * @throws PSCmsException if failed to delete the component and the 'results'
    *            parameter is <code>null</code>.
    */
   private void deleteComponent(PSKey key, String componentType, IPSGuid id, Class cz, PSErrorsException results)
         throws PSCmsException
   {
      try
      {
         getComponentProxy().delete(componentType, new PSKey[]
         {key});
         if (results != null)
            results.addResult(id);
      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
         if (results == null)
         {
            throw e;
         }
         else
         {
            int code = IPSWebserviceErrors.DELETE_FAILED;
            PSErrorException error = new PSErrorException(code, PSWebserviceErrors.createErrorMessage(code,
                  cz.getName(), id.longValue(), e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
            results.addError(id, error);
         }
      }
   }

   /**
    * @return a local component proxy, never <code>null</code>.
    */
   private PSComponentProcessorProxy getComponentProxy()
   {
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);

      try
      {
         return new PSComponentProcessorProxy(PSComponentProcessorProxy.PROCTYPE_SERVERLOCAL, req);
      }
      catch (PSCmsException e)
      {
         // this is not possible
         e.printStackTrace();
         throw new RuntimeException("Failed to create PSComponentProcessorProxy.");
      }
   }

   /**
    * This listener responds to table change notices by removing the cached
    * cllection of all searches.
    */
   @SuppressWarnings("unchecked")
   public static class EvictionListener extends PSTableUpdateHandlerBase
   {
      /**
       * Search tables
       */
      static String msi_tables[] =
      {"PSX_SEARCHES", "PSX_SEARCHFIELDS", "PSX_SEARCHPROPERTIES"};

      /**
       * Access to the cache service, wired when the service is wired.
       */
      IPSCacheAccess mi_cache = null;

      /**
       * Ctor
       * 
       * @param cache the cache accessor, never <code>null</code>
       */
      public EvictionListener(IPSCacheAccess cache)
      {
         super(msi_tables);
         if (cache == null)
         {
            throw new IllegalArgumentException("cache may not be null");
         }
         mi_cache = cache;
      }

      /**
       * This listener doesn't care about the columns
       */
      public Iterator getColumns(@SuppressWarnings("unused")
      String tableName, @SuppressWarnings("unused")
      int actionType)
      {
         return Collections.EMPTY_LIST.iterator();
      }

      /**
       * Just destroy everything in the ALL_SEARCHES_CACHE_KEY section of the
       * cache.
       */
      public void tableChanged(@SuppressWarnings("unused")
      PSTableChangeEvent e)
      {
         mi_cache.evict(ALL_SEARCHES_CACHE_KEY, IPSCacheAccess.IN_MEMORY_STORE);
         ms_log.debug("Clearing cache key: " + ALL_SEARCHES_CACHE_KEY);
      }

   }

   
   /**
    * The cache key for storing the collection of searches in the
    * IPSCacheAccess.IN_MEMORY_STORE region.
    */
   private static final String ALL_SEARCHES_CACHE_KEY = "All_Searches_In_System";

   /**
    * The cache key for storing the collection of searches in the
    * IPSCacheAccess.IN_MEMORY_STORE region.
    */
   private static final String All_OBJECT_PATHS_CACHE_KEY = "All_Object_Paths_In_System";

   /**
    * Hierarchy node path prefix for navigation content types
    * 
    */
   private static final String CONTENTTYPES_NAV_PATH = "/contentTypes/Navigation/";

   /**
    * The cache key for storing the map that maps the ID to its related
    * display format object in the IPSCacheAccess.IN_MEMORY_STORE region. 
    */
   private static final String DISPLAY_FORMAT_ID_OBJ_MAP = "displayformat_id_object_map";   

   /**
    * Cache service
    */
   IPSCacheAccess m_cache = null;

   /**
    * Commons logger
    */
    private static final Logger ms_log = LogManager.getLogger(PSUiDesignWs.class);

}
