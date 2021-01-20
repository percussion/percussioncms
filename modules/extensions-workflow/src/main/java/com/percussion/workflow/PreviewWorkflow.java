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

package com.percussion.workflow;

import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PreviewWorkflow extends PSPostExitHandler
{
   public PreviewWorkflow()
   {
   }

   /**
    * Standard interface for all result document extensions. For each state
    * in the supplied document, the transitions associated with that state are
    * looked up in the db. A visual representation of the states and
    * transitions is created.
    * In the visual model, each state is represented by a vertical line and
    * each transition by a horizontal line with an arrow.
    *
    * @param params Unused
    *
    * @param request See interface definition. May not be <code>null</code>.
    *
    * @param resDoc A new element called transitions is added to this document
    * that contains a model of the visual representation of the workflow.
    *
    * @return The modified document is returned, or <code>null</code> if
    *    resDoc is <code>null</code>.
    *
    * @throws PSParameterMismatchException Never thrown.
    * @throws PSExtensionProcessingException Never thrown.
    */
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext request,
                                         Document resDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      if ( null == resDoc )
         return null;
      String sScale = request.getParameter("scale");
      scale = 1.0f;
      if(null != sScale)
      {
         try
         {
            scale = Float.parseFloat(sScale);
         }
         catch(Exception e)
         {
         }
      }
      IPSInternalRequest transitionsReq = null;
      Element state = null;
      Document doc = null;
      try
      {
         NodeList nl = resDoc.getElementsByTagName("state");
         NodeList nlTrans = null;
         for(int i=0; nl != null && i < nl.getLength(); i++)
         {
            state = (Element) nl.item(i);
            request.setParameter("stateid", state.getAttribute("id"));
            transitionsReq = request.getInternalRequest(
                  "sys_wfPreviewWorkflow/transitionslookup");
            try
            {
               transitionsReq.makeRequest();
               doc = transitionsReq.getResultDoc();
            }
            finally
            {
               transitionsReq.cleanUp();
            }
            nlTrans = doc.getElementsByTagName("transition");
            for(int j=0; nlTrans != null && j < nlTrans.getLength(); j++)
            {
               Node importNode = resDoc.importNode(nlTrans.item(j), true);
               state.appendChild(importNode);
            }
         }
      }
      catch(Throwable t)
      {
         PSConsole.printMsg("PreviewWorkflow", t);
      }
      resDoc = processDocument(resDoc);

      return resDoc;
   }

   /**
    * Expects each state element to have 0 or more transitions. It takes the
    * states and transitions and creates a 'drawing sequence' that can be used
    * to generate a visual representation of the workflow. A new transitions
    * node is added to contain this information. This node will have a
    * transition element for each transition found in the supplied doc. Each
    * of these elements represents a single row in the output image.
    * The image is built up of graphical 'blocks' such as white space, vertical
    * and horizontal lines and arrows. Each transition element contains several
    * draw elements, each of which specifies a graphical image.
    *
    * @param doc Assumed not <code>null</code>.
    *
    * @return The modified doc, never <code>null</code>.
    */
   private Document processDocument(Document doc)
   {
      NodeList nlTransitions = doc.getElementsByTagName("transition");

      NodeList nl = doc.getElementsByTagName("states");
      Element elemStates = (Element)nl.item(0);

      Element elemTransitions = doc.createElement("transitions");

      elemTransitions = (Element) elemStates.getParentNode().
         appendChild(elemTransitions);

      Element elemTransition;
      String tmp="";
      List toAppendEl = new ArrayList();

      for(int i=0; i<nlTransitions.getLength(); i++)
      {
         elemTransition = (Element)nlTransitions.item(i);
         tmp = elemTransition.getAttribute("from");
         if(null==tmp || tmp.trim().length() < 1)
            continue;

         tmp = elemTransition.getAttribute("to");
         if(null==tmp || tmp.trim().length() < 1)
            continue;

         toAppendEl.add(elemTransition.cloneNode(true));
      }

      for (int j=0; j < toAppendEl.size(); j++)
         elemTransitions.appendChild((Node)toAppendEl.get(j));

      nl = elemStates.getElementsByTagName("state");
      Element elemState = null;
      HashMap statesMap = new HashMap(nl.getLength());

      int x= Math.round(20*scale);
      int y= Math.round(20*scale);
      int height = Math.round(40*scale);
      int totalWidth= Math.round(800*scale);
      int nStates = nl.getLength();
      int width = totalWidth/nStates;

      y += height;
      for(int i=0; i<nl.getLength(); i++)
      {
         elemState = (Element)nl.item(0);
         State state = new State(i, elemState, width, height);
         statesMap.put(state.getID(), state);
         elemStates.removeChild(elemState);
         elemStates.appendChild(state.makeElement(elemStates, y));
      }

      nl = doc.getElementsByTagName("transitions");
      elemTransitions = (Element)nl.item(0);

      nl = elemTransitions.getElementsByTagName("transition");
      for(int i=0; i<nl.getLength(); i++)
      {
         elemTransition = (Element)nl.item(i);
         Transition transition = new Transition(elemTransition);

         if(!statesMap.containsKey(transition.getFrom()) ||
            !statesMap.containsKey(transition.getTo()))
         {
            //for some reason state map does not have either from state or to
            // state so remove it from the transition list
            elemTransitions.removeChild(elemTransition);
         }
         else
            elemTransitions.replaceChild(transition.makeElement(
               elemTransitions, statesMap, height), elemTransition);
      }

      return doc;
   }

   /**
    * This is a scaling factor that can be overridden by the requestor by
    * supplying an html param named 'scale'. The value should be a float.
    * The default value is 1.0.
    * @todo (ph): There are bugs for factors other than 1.0
    */
   static float scale = 1.0f;
}
