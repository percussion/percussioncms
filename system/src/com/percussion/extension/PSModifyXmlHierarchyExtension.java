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

package com.percussion.extension;

import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This sample extension is used to modify an XML documents hierarchy.
 * <P>
 * The concept of XML hierarchy modification is based on the need for setting up
 * a discussion thread system. Each discussion topic submission can be
 * considered separately, thus having no relationship with other submissions.
 * However, most of the time, a submission may be a "response" to a previously
 * submitted topic. This creates a new discussion "thread" with response
 * submissions becoming children to a parent submission topic. A relationship
 * between the submissions is required to make this work.
 * <P>
 * To give a
 * relationship to different submission topics, a node-key pair comparison is
 * used in this exit extension to provide a hierarchical relationship between
 * submission topics. The 4 parameters are:
 * <UL>
 * <LI>response node: (required) This is the name of the XML node that contains
 * the attribute, or "response key", for looking up
 * parent node of this submission topic.
 * <LI>response key: (required) This is the attribute owned by the "response
 * node". This key defines which node is this response node's parent by looking
 * at the "parent key" defined in the "parent node".
 * <LI>parent node: (required) This is the name of the XML node that contains
 * the attribute, or "parent key", for holding the key where response
 * nodes would look up to find the parent.
 * <LI>parent key: (required) This is the attribute owned by the "parent node".
 * This key defines the value for the "response node" to try and match its
 * "response key" value for the determination if this "parent node" is the
 * parent. The value of the response key MUST BE UNIQUE amongst all the response
 * nodes!
 * </UL>
 * <P>
 * <BIG>Example:</BIG> Pay special attention to the relationship between
 * parentid and id attributes
 * <UL>
 * <LI>response node = Discussion/Topic
 * <LI>response key = Discussion/Topic/@parentid
 * <LI>parent node = Discussion/Topic
 * <LI>parent key = Discussion/Topic/@id
 * </UL>
 * <P>
 * Original XML Document:
 * <BR>
 * <PRE>
 * &lt;Discussion&gt;
 *   &lt;Topic id="1" parentid="0"&gt;
 *     &lt;body&gt;This is the first thread in the discussion&lt;/body&gt;
 *   &lt;/Topic&gt;
 *   &lt;Topic id="2" parentid="0"&gt;
 *     &lt;body&gt;This is the second thread in the discussion&lt;/body&gt;
 *   &lt;/Topic&gt;
 *   &lt;Topic id="3" parentid="1"&gt;
 *     &lt;body&gt;This is the first response to the first thread&lt;/body&gt;
 *   &lt;/Topic&gt;
 * &lt;/Discussion&gt;
 * </PRE>
 * <P>
 * After ModifyXmlHierarchyExtension exit:
 * <BR>
 * <PRE>
 * &lt;Discussion&gt;
 *   &lt;Topic id="1" parentid="0"&gt;
 *     &lt;body&gt;This is the first thread in the discussion&lt;/body&gt;
 *     &lt;Topic id="3" parentid="1"&gt;
 *       &lt;body&gt;This is the first response to the first thread&lt;/body&gt;
 *     &lt;/Topic&gt;
 *   &lt;/Topic&gt;
 *   &lt;Topic id="2" parentid="0"&gt;
 *     &lt;body&gt;This is the second thread in the discussion&lt;/body&gt;
 *   &lt;/Topic&gt;
 * &lt;/Discussion&gt;
 * </PRE>
 *
 * @author     Jian Huang
 * @version    1.1
 * @since      1.1
 */
public class PSModifyXmlHierarchyExtension
   implements IPSResultDocumentProcessor
{
   /**
    * Return false (this extension can not modify the style sheet).
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }


   /**
    * No-op
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {}

   /**
    * Modifies the XML hierarchy by linking nodes as children to the
    * specified parent nodes. This is done by performing a key-based
     * search from a value within the child node to a value within the
    * parent node.
    *
    * @param      params         the parameters for this extension; this is an
    * array of 4 Objects, a toString() is called to convert the object to a
    * String representation.
    * <UL>
    * <LI>response node: (required) This is the XML node (defined by the name of
    * the node) that contains the attribute, or "response key", for looking up
    * parent node of this submission topic.
    * <LI>response key: (required) This is the attribute owned by the "response
    * node". This key defines which node is this response node's parent by looking
    * at the "parent key" defined in the "parent node".
    * <LI>parent node: (required) This XML node (defined by the name of the node)
    * contains the attribute, or "parent key", for holding the key where response
    * nodes would look up to find the parent.
    * <LI>parent key: (required) This is the attribute owned by the "parent node".
    * This key defines the value for the "response node" to try and match its
    * "response key" for the determination if this "parent node" is the parent.
    * </UL>
    *
    * @param      request         the request context
    *
    * @param      resultDoc      the result XML document
    *
    * @return                     <code>resultDoc</code> is always returned
    *
    * @exception  PSParameterMismatchException  if the parameter number is incorrect
    * @exception  PSExtensionProcessingException      if any parameter is <code>null</code>
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      int len = (params == null) ? 0 : params.length;
      if (len != 4){ // four parameters are required
         throw new PSParameterMismatchException(len, 4);
      }

      for (int j=0; j < 4; j++){
         if (params[j] == null){
            String msg = "parameters must not be null to call processResultDocument";
            IllegalArgumentException ex = new IllegalArgumentException(msg);
            throw new PSExtensionProcessingException( getClass().getName(), ex);
         }
      }


      if (resultDoc == null)   // no doc, no work
         return resultDoc;

      Element root = resultDoc.getDocumentElement();
      if (root == null)   // no root, no work
         return resultDoc;

      try {
         /* we'll approach this problem by first creating a hash of the
          * parent nodes (key=parentKey, value=parentNode). We will then
          * traverse the child (response) nodes looking for their
          * corresponding parents.
          */

         String responseNode = params[0].toString();
         String responseKey = params[1].toString();
         String parentNode = params[2].toString();
         String parentKey = params[3].toString();

         /* this method will figure out the relative path from the
          * response node to its key value.
          */
         String relativeResponseKey = PSXmlTreeWalker.getRelativeFieldName(
            responseNode, responseKey);
         if (relativeResponseKey == null)
         {   // no path?!
            throw new IllegalArgumentException(
               "no path found between " + responseNode + " and " + responseKey);
         }

         String relativeParentKey = PSXmlTreeWalker.getRelativeFieldName(
            parentNode, parentKey);
         if (relativeParentKey == null)
         {   // no path?!
            throw new IllegalArgumentException(
               "no path found between " + parentNode + " and " + parentKey);
         }

         // also strip off the root node names
         responseNode = responseNode.substring(responseNode.indexOf("/")+1);
         parentNode = parentNode.substring(parentNode.indexOf("/")+1);

         /* now we can locate all the parent nodes, which we'll store in
          * the specified hash
          */
         HashMap parents = new HashMap();

         PSXmlTreeWalker tree = new PSXmlTreeWalker(resultDoc);
         Element el = tree.getNextElement(parentNode, PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         while (el != null)
         {
            /* this is a bit simplistic -- that is, we're assuming the parent
             * key will only be found once. We really don't have much of a
             * choice as we can't set something as the child of two nodes.
             * For now, we'll treat this as successful and use the last node
             * we find as the match for the key. In the future we may want
             * to treat this as an error condition, or even use just the
             * first element.
             */
            String key = tree.getElementData(relativeParentKey, false);
            parents.put(key, el);

            // get the next element with the same name (must be a sibling!)
            el = tree.getNextElement(el.getNodeName(), PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }

         /* now we can move on to phase two, the processing of the children
          * against the parent map. We don't want to change the tree
          * structure until we've processed all the children, so we will now
          * traverse the tree once more and collect the response nodes into
          * a list. We will then walk the list to do the tree fixup.
          */

         ArrayList kids = new ArrayList();

         // move back to the root to do the second pass of the tree
         tree.setCurrent(root);
         el = tree.getNextElement(responseNode, PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         while (el != null)
         {
            kids.add(el);   // add the kid to the list

            // get the next element with the same name (must be a sibling!)
            el = tree.getNextElement(el.getNodeName(), PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }

         // now we can do the child-parent fixup
         int size = kids.size();
         for (int i = 0; i < size; i++)
         {
            // get the child and set it as the current node in the tree
            // we can then use the walker to get the value of the lookup key
            el = (Element)kids.get(i);
            tree.setCurrent(el);
            String key = tree.getElementData(relativeResponseKey, false);

            Element parent = (Element)parents.get(key);
            if ((parent != null) && (parent != el))
            {   // we found our parent, move ourselves under the parent
               parent.appendChild(el);
            }
         }

         // and return the modified document
         return resultDoc;
      } catch (Exception e) {
         throw new PSExtensionProcessingException( getClass().getName(), e);
      }
   }
}
