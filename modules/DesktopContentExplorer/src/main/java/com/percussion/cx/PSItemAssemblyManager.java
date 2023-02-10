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

package com.percussion.cx;

import com.percussion.cms.objectstore.PSActiveAssemblerHandlerRequest;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDependent;
import com.percussion.cms.objectstore.PSDependentSet;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cx.objectstore.PSNode;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSProperty;
import com.percussion.design.objectstore.PSPropertySet;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
* Class that manges all item assembly actions.
*/
public class PSItemAssemblyManager
{
   /**
    * Reference to the content explorer action manager. Basically to acces the
    * content explorer applet.
    */
   private PSActionManager m_actionManager = null;
   
   /**
    * Name of the component type for component summaries. 
    */
   private static String ms_summaryComponentType = 
      PSComponentSummaries.getComponentType(PSComponentSummaries.class);

   /**
    * Takes the action manager object mainly to acces the applet associated.
    *
    * @param actionManager, must not be <code>null</code>
    */
   public PSItemAssemblyManager(PSActionManager actionManager)
   {
      if (actionManager == null)
         throw new IllegalArgumentException("actionManager must not be null");

      m_actionManager = actionManager;
   }

   /**
    * Insert the supplied node list to the supplied target node at the specified
    * target.
    *
    * @param targetParent this is a slot target location of where to insert the
    * specified list of nodes, must not be <code>null</code>
    *
    * @param target this is a slot item target, may be a target item or a target
    * slot, if target item we use this index to decide where to insert, otherwise
    * we insert at the end of the target, must not be <code>null</code>
    *
    * @param nodeList the list of nodes to insert at the specified
    * target/targetParent, must not be <code>null</code>
    */
   public void insert(PSNode targetParent, PSNode target, Iterator nodeList)
   {
      if (targetParent == null)
         throw new IllegalArgumentException("targetParent must not be null");

      if (target == null)
         throw new IllegalArgumentException("target must not be null");

      if (nodeList == null)
         throw new IllegalArgumentException("nodeList must not be null");

      int index = Integer.MAX_VALUE;
      if (target.getType().equals(PSNode.TYPE_SLOT))
         targetParent = target;
      else
         index = target.getSortRank();

      PSActiveAssemblerHandlerRequest req = buildRequest(targetParent, nodeList);

      // set the drop location index
      req.setIndex(index);

      processActiveAssemblerRequest(req, "insert");
   }

   /**
    * Inserts the items supplied in the node list to the supplied slot of the
    * owner item represented by content id and revision. The items will be added
    * at the end.
    *
    * @param ownerContentId the owner item content id, may not be <code>null
    * </code> or empty.
    * @param slotId the slot of owner, may not be <code>null</code> or empty.
    * @param nodeList the list of <code>PSNode</code>s identifying the items,
    * may not be <code>null</code> or empty.
    */
   public void insert(String ownerContentId, String slotId,
      Iterator nodeList)
   {
      PSActiveAssemblerHandlerRequest req =
         buildRequest(ownerContentId, slotId, nodeList);

      processActiveAssemblerRequest(req, "insert");
   }

   /**
    * Delete the supplied node list from the supplied target node.
    *
    * @param target this is a slot target, must not be <code>null</code>
    *
    * @param nodeList this is the list of nodes to delete, must not be <code>
    * null</code>
    */
   public void delete(PSNode target, Iterator nodeList)
   {
      if (target == null)
         throw new IllegalArgumentException("target must not be null");

      if (nodeList == null)
         throw new IllegalArgumentException("nodeList must not be null");

      PSActiveAssemblerHandlerRequest req = buildRequest(target, nodeList);
      processActiveAssemblerRequest(req, "delete");
   }

   /**
    * Reorder the supplied seleciton node list up or down, using the
    * direction specified.
    *
    * @param selection the current selection within the list of nodes, must not
    * be <code>null</code>
    *
    * @param direction the direction to move the specified nodes, < 0 is move up,
    * > 0 is move down
    */
   public void reorder(PSSelection selection, int direction)
   {
      if (selection == null)
         throw new IllegalArgumentException("selection must not be null");

      boolean moveDown = (direction > 0);
      if (moveDown)           // move down requires 1 more because
         direction++;         // we move 1 above the index specified

      int newIndex = (moveDown) ? 0 : Integer.MAX_VALUE;
      if (selection.getNodeListSize() > 0)
      {
         Iterator iter = selection.getNodeList();
         while (iter.hasNext())
         {
            PSNode node = (PSNode)iter.next();
            int sortRank = node.getSortRank();
            if (moveDown)
            {
               if (sortRank > newIndex)
                  newIndex = sortRank;
            }
            else // moveUp
            {
               if (sortRank < newIndex)
                  newIndex = sortRank;
            }
         }
         PSActiveAssemblerHandlerRequest req =
            buildRequest(selection.getParent(), selection.getNodeList());

         // set the drop location index
         req.setIndex(newIndex + direction);

         processActiveAssemblerRequest(req, "reorder");
      }
   }

  /**
    * Move the supplied source node list to the specified target node.
    *
    * @param targetParent this is a slot target location of where to update the
    * specified list of nodes, must not be <code>null</code>
    *
    * @param target this is a slot item target, may be a target item or a target
    * slot, if target item we use this index to decide where to update, otherwise
    * we update to the end of the target, must not be <code>null</code>
    *
    * @param nodeList the list of nodes to update at the specified
    * target/targetParent, must not be <code>null</code>
    */
   public void update(PSNode targetParent, PSNode target, Iterator nodeList)
   {
      if (targetParent == null)
         throw new IllegalArgumentException("targetParent must not be null");

      if (target == null)
         throw new IllegalArgumentException("target must not be null");

      if (nodeList == null)
         throw new IllegalArgumentException("nodeList must not be null");

      int index = Integer.MAX_VALUE;
      if (target.getType().equals(PSNode.TYPE_SLOT))
         targetParent = target;
      else
         index = target.getSortRank();

      PSActiveAssemblerHandlerRequest req = buildRequest(targetParent, nodeList);

      // set the drop location index
      req.setIndex(index);

      processActiveAssemblerRequest(req, "update");
   }

   /**
    * Reorder the supplied node list to the supplied target node.
    *
    * @param targetParent this is a slot target location of where to reorder the
    * specified list of nodes, must not be <code>null</code>
    *
    * @param target this is a slot item target, may be a target item or a target
    * slot, if target item we use this index to decide where to reorder, otherwise
    * we reorder to the end of the target, must not be <code>null</code>
    *
    * @param nodeList the list of nodes to reorder at the specified
    * target/targetParent, must not be <code>null</code>
    */
   public void reorder(PSNode targetParent, PSNode target, Iterator nodeList)
   {
      if (targetParent == null)
         throw new IllegalArgumentException("targetParent must not be null");

      if (target == null)
         throw new IllegalArgumentException("target must not be null");

      if (nodeList == null)
         throw new IllegalArgumentException("nodeList must not be null");

      PSActiveAssemblerHandlerRequest req = buildRequest(targetParent, nodeList);

      int index = Integer.MAX_VALUE;
      if (!target.getType().equals(PSNode.TYPE_SLOT))
         index = target.getSortRank();

      // set the drop location index
      req.setIndex(index);

      processActiveAssemblerRequest(req, "reorder");
   }

   /**
    * Create an active assembler handler request to use as a base for actions.
    * The calling routines should reset the index if they do not want the action
    * to insert/reorder/update to the end.
    *
    * @param targetSlot the slot node from which to get the slot id which all
    * actions need to effect changes, must not be <code>null</code>
    *
    * @param nodeList the list of nodes to put in the list of nodes to be acted
    * upon when the active assembler handler is called, must not be <code>null
    * </code>
    *
    * @return an active assembler object that can be transformed to XML and
    * posted to the active assembler handler
    */
   private PSActiveAssemblerHandlerRequest buildRequest(
      PSNode targetSlot, Iterator nodeList)
   {
      return buildRequest(targetSlot.getContentId(),
         targetSlot.getSlotId(), nodeList);
   }

   /**
    * Creates an active assembler handler request to insert/reorder/update the
    * supplied list of items to the owner item in the specified slot.
    *
    * @param ownerContentId the owner item content id, may not be <code>null
    * </code> or empty.
    * @param slotId the slot of owner, may not be <code>null</code> or empty.
    * @param nodeList the list of <code>PSNode</code>s identifying the items,
    * may not be <code>null</code> or empty.
    *
    * @return the request, never <code>null</code>
    */
   private PSActiveAssemblerHandlerRequest buildRequest(
      String ownerContentId, String slotId,
      Iterator nodeList)
   {
      if(ownerContentId == null || ownerContentId.trim().length() == 0)
         throw new IllegalArgumentException(
            "ownerContentId may not be null or empty.");

      if(slotId == null || slotId.trim().length() == 0)
         throw new IllegalArgumentException(
            "slotId may not be null or empty.");

      if(nodeList == null || !nodeList.hasNext())
         throw new IllegalArgumentException(
            "nodeList may not be null or empty.");

    
      PSKey locators[] = new PSKey[] { new PSLocator(ownerContentId) };
      Element summariesElements[];
      PSComponentSummaries summaries = null;
      try
      {
         summariesElements = m_actionManager.getComponentProxy()
            .load(ms_summaryComponentType, locators);
         if (summariesElements.length == 0 || summariesElements[0] == null)
         {
            throw new Exception("Could not find component summary for " +
                    "content id " + ownerContentId);
         }
         summaries = new PSComponentSummaries(summariesElements);
      }
      catch (Exception e)
      {
         m_actionManager.getApplet().debugMessage(e);
         throw new RuntimeException("Couldn't load summaries");
      }

      PSComponentSummary summary = 
         (PSComponentSummary) summaries.iterator().next();
      PSLocator owner = summary.getTipLocator();

      PSDependentSet dependents = new PSDependentSet();
      while (nodeList.hasNext())
      {
         PSNode src = (PSNode)nodeList.next();
         PSLocator loc = new PSLocator(src.getContentId(), src.getRevision());

         PSPropertySet props = new PSPropertySet();

         props.add(new PSProperty(IPSHtmlParameters.SYS_SLOTID,
            PSProperty.TYPE_STRING, slotId, false, null));

         props.add(new PSProperty(IPSHtmlParameters.SYS_VARIANTID,
            PSProperty.TYPE_STRING, src.getVariantId(), false, null));

         props.add(new PSProperty(IPSHtmlParameters.SYS_SITEID,
               PSProperty.TYPE_STRING, src
                     .getProp(IPSHtmlParameters.SYS_SITEID), false, null));

         props.add(new PSProperty(IPSHtmlParameters.SYS_FOLDERID,
               PSProperty.TYPE_STRING, src
                     .getProp(IPSHtmlParameters.SYS_FOLDERID), false, null));

         dependents.add(new PSDependent(src.getRelationshipId(), loc, props));
      }
      return new PSActiveAssemblerHandlerRequest(
         owner, dependents, Integer.MAX_VALUE);
   }

   /**
    * Process the active assembler handler by passing the XML format of the
    * message to the proper app.
    *
    * @param req the active assembler handler request created by the specific
    * request and used to effect changes on the nodes supplied within the
    * active assembler request
    *
    * @param action the action to perform
    */
   private void processActiveAssemblerRequest(
      PSActiveAssemblerHandlerRequest req, String action)
   {
      if (! (req.getIndex() > 0))
         return;  // Do nothing if the index is NOT greater than 0
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Map params = new HashMap();
      params.put("sys_command", action);
      params.put(INPUT_DOC, PSXmlDocumentBuilder.toString(req.toXml(doc)));

      try
      {
         m_actionManager.postData(ACTIVE_ASSEMBLY_APP, params);
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   /**
    * The url location of the active assembly app.
    */
   private static final String ACTIVE_ASSEMBLY_APP =
      "../sys_psxActiveAssembly/app";

   /**
    * The HTML parameter that provides the input document. The document must
    * confor to the PSXActiveAssemblerHandlerRequest.dtd.
    */
   public static final String INPUT_DOC = "inputdoc";
}
