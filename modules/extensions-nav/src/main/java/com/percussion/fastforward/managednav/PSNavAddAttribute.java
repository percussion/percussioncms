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
package com.percussion.fastforward.managednav;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.fastforward.utils.PSUtils;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlTreeWalker;

import com.percussion.fastforward.managednav.PSNavon;
import com.percussion.fastforward.managednav.PSNavonType;

/**
 * Adds an attribute to the &lt;Navon&gt; nodes in a Managed Navigation tree.
 * This class references Managed Navigation, but does not depend on the specific
 * implementation.
 * <p>
 * This exit should be added to the assembler resource for the Navigation bars
 * <em>after</em> the call the NavTreeLink extension.
 * <p>
 * There are 4 parameters:
 * <ol>
 * <li>The name of the attribute to add</li>
 * <li>The query resource that returns the attribute</li>
 * <li>The index of the column in the query resource</li>
 * <li>The name of the relative level attribute. Optional. </li>
 * </ol>
 * <p>
 * The query resource name must be a name suitable for internal requests.
 * Usually this is just &lt;application name&gt;/&ltquery name&gt;
 * <p>
 * The column index starts at 1. If this column contains a value, it will be
 * converted to text and added to the &lt;Navon&gt; elements. If the value is
 * <code>null</code> no attribute will be added.
 * <p>
 * If a relative level attribute is supplied, all Navon nodes that are
 * descendents of a node where the attribute is set will recieve a relative
 * level attribute. The node where the attribute is set will be level 0, its
 * immediate descendents will be level 1, etc.
 * <p>
 * All nodes in the tree that are ancestors of the <code>self</code> node and
 * the <code>self</code> node will be examined. Siblings, Ancestor-Siblings,
 * Descendents and Other nodes will be ignored.
 * 
 * @author DavidBenua
 * 
 * 
 */
public class PSNavAddAttribute extends PSDefaultExtension
      implements
         IPSResultDocumentProcessor
{

   private static final Logger logger = LogManager.getLogger(PSNavAddAttribute.class);

   /**
    * This exit never modifies the stylesheet.
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * processes the XML result document.
    * 
    * @param params the parameter array.
    * @param request the caller's request context
    * @param resultDoc the Navon assembler's result document.
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      Worker worker = new Worker(request);
      String attribute = PSUtils.getParameter(params, 0);
      if (attribute == null || attribute.trim().length() == 0)
      {
         logger.error(MSG_ATTRIB_REQUIRED);
         throw new PSParameterMismatchException(MSG_ATTRIB_REQUIRED);
      }
      logger.debug("Attribute name is " + attribute);
      worker.setAttributeName(attribute);

      String requestName = PSUtils.getParameter(params, 1);
      if (requestName == null || requestName.trim().length() == 0)
      {
         logger.error(MSG_REQUEST_REQUIRED);
         throw new PSParameterMismatchException(MSG_REQUEST_REQUIRED);
      }
      logger.debug("Query name is " + requestName);
      worker.setQueryName(requestName);

      String indexStr = PSUtils.getParameter(params, 2);

      if (indexStr == null || indexStr.trim().length() == 0)
      {
         logger.error(MSG_INDEX_REQUIRED);
         throw new PSParameterMismatchException(MSG_INDEX_REQUIRED);
      }
      logger.debug("column index is " + indexStr);
      worker.setIndex(indexStr);

      String levelAttribute = PSUtils.getParameter(params, 3);
      if (levelAttribute != null && levelAttribute.trim().length() > 0)
      {
         logger.debug("Level attribute is " + levelAttribute);
         worker.setLevelAttributeName(levelAttribute);
      }

      String wholeTreeParam = PSUtils.getParameter(params, 4);
      if (wholeTreeParam != null && wholeTreeParam.equalsIgnoreCase("yes"))
      {
         logger.debug("WholeTree specified");
         worker.setWholeTree(true);
      }

      try
      {

         logger.debug("Processing tree...");
         worker.processDoc(resultDoc);
         logger.debug("Processing finished");

      }
      catch (Exception ex)
      {
         logger.error(this.getClass().getName(), ex);
         throw new PSExtensionProcessingException(this.getClass().getName(), ex);
      }

      return resultDoc;
   }

   /**
    * Private class to hold the data for this specific request. Using a private
    * inner class for this allows the use of member variables for request
    * specific values, and eliminiates the need to pass these values as method
    * parameters.
    * 
    */
   private class Worker
   {
      /**
       * Debug logger for the inner class.
       */
      Logger logger = LogManager.getLogger(Worker.class);

      /**
       * The callers request context.
       */
      IPSRequestContext request;

      /**
       * The name of the internal request query which returns the attribute
       * value for a given Navon document.
       */
      String queryName;

      /**
       * The name of the attribute that will be added to the XML tree.
       */
      String attributeName;

      /**
       * The column index within the query.
       */
      int index;

      /**
       * Name of Level attribute (optional)
       */
      String levelAttributeName = null;

      /**
       * Flag for whole tree scan
       */
      boolean wholeTree = false;

      /**
       * Construct a new worker for the callers request.
       * 
       * @param req the caller's request context.
       */
      private Worker(IPSRequestContext req) {
         this.request = req;
      }

      /**
       * Sets the attribute name.
       * 
       * @param attributeName The attributeName to set.
       */
      public void setAttributeName(String attributeName)
      {
         this.attributeName = attributeName;
      }

      /**
       * Sets the query name.
       * 
       * @param queryName The queryName to set.
       */
      public void setQueryName(String queryName)
      {
         this.queryName = queryName;
      }

      /**
       * Sets the column index. The column index starts at 1.
       * 
       * @param str the column index as a string.
       * @throws PSParameterMismatchException
       */
      public void setIndex(String str) throws PSParameterMismatchException
      {
         try
         {
            this.setIndex(Integer.parseInt(str));
         }
         catch (NumberFormatException nfe)
         {
            logger.error("Invalid number " + str, nfe);
            throw new PSParameterMismatchException(MSG_BAD_INDEX + str);
         }
      }

      /**
       * Sets the column index. The column index starts at 1.
       * 
       * @param index The index to set.
       */
      public void setIndex(int index)
      {
         this.index = index;
      }

      /**
       * @param levelAttributeName The levelAttributeName to set.
       */
      public void setLevelAttributeName(String levelAttributeName)
      {
         this.levelAttributeName = levelAttributeName;
      }

      /**
       * @param wholeTree The wholeTree to set.
       */
      public void setWholeTree(boolean wholeTree)
      {
         this.wholeTree = wholeTree;
      }

      /**
       * Processes the entire result document. The root node, all ancestor nodes
       * and the self node will all be processed.
       * 
       * @param doc the result document.
       * @throws PSInternalRequestCallException
       * @throws SQLException
       */
      public void processDoc(Document doc)
            throws PSInternalRequestCallException, SQLException
      {
         Element docElem = doc.getDocumentElement();
         if (docElem == null)
         {
            logger.debug("no result document, can't do anything");
            return;
         }
         PSXmlTreeWalker walker = new PSXmlTreeWalker(docElem);
         Element rootNavon = walker.getNextElement(PSNavon.XML_ELEMENT_NAME);
         if (rootNavon != null)
         {
            walkElement(rootNavon);
         }
         else
         {
            logger.debug("No Navon found");
         }
      }

      /**
       * Walks the selected Navon elements, setting the attribute where
       * appropriate
       * 
       * @param navon the starting (parent) navon.
       * @throws PSInternalRequestCallException
       * @throws SQLException
       */
      public void walkElement(Element navon)
            throws PSInternalRequestCallException, SQLException
      {
         String contentId = navon.getAttribute(PSNavon.XML_ATTR_CONTENTID);
         String revision = navon.getAttribute(PSNavon.XML_ATTR_REVISION);
         PSLocator locator = new PSLocator(contentId, revision);
         String relation = navon.getAttribute(PSNavon.XML_ATTR_TYPE);
         Object value = doQuery(locator);
         if (value != null)
         {
            logger.debug("adding attribute " + value.toString());
            navon.setAttribute(this.attributeName, value.toString());
            if (this.levelAttributeName != null)
            {
               logger.debug("Setting Level Attribute " + levelAttributeName);
               setLevelAttribute(navon, 0);
            }
            if (!(wholeTree || (relation.equals(PSNavonType.TYPENAME_ANCESTOR)
                  || relation.equals(PSNavonType.TYPENAME_ROOT) || relation
                  .equals(PSNavonType.TYPENAME_SELF))))
            {
               logger
                     .debug("Found attribute on non-Ancestor node, end of this branch.");
               return;
            }
         }
         // if we get here, we have to scan children of this node.
         PSXmlTreeWalker walker = new PSXmlTreeWalker(navon);
         Element next = walker.getNextElement(PSNavon.XML_ELEMENT_NAME,
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         while (next != null)
         {
            walkElement(next);
            next = walker.getNextElement(PSNavon.XML_ELEMENT_NAME,
                  PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }
      }

      private void setLevelAttribute(Element navon, int level)
      {
         logger.debug("Setting level " + level);
         navon.setAttribute(this.levelAttributeName, String.valueOf(level));
         PSXmlTreeWalker childWalker = new PSXmlTreeWalker(navon);
         Element childNavon = childWalker.getNextElement(
               PSNavon.XML_ELEMENT_NAME,
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         level += 1;
         while (childNavon != null)
         {
            setLevelAttribute(childNavon, level);
            childNavon = childWalker.getNextElement(PSNavon.XML_ELEMENT_NAME,
                  PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }
      }

      /**
       * Executes the internal query for a specific content item.
       * 
       * @param locator the locator for this content item.
       * @return the value of the selected column or <code>null</code>.
       * @throws PSInternalRequestCallException
       * @throws SQLException
       */
      private Object doQuery(PSLocator locator)
            throws PSInternalRequestCallException, SQLException
      {
         Map xtraParams = new HashMap();
         xtraParams.put(IPSHtmlParameters.SYS_CONTENTID, locator
               .getPart(PSLocator.KEY_ID));
         xtraParams.put(IPSHtmlParameters.SYS_REVISION, locator
               .getPart(PSLocator.KEY_REVISION));
         logger.debug("Fetching value for " + xtraParams);
         IPSInternalRequest irq = request.getInternalRequest(queryName,
               xtraParams, false);
         if (irq != null)
         {
            try
            {
               ResultSet rs = irq.getResultSet();
               if (rs != null && rs.next())
               {
                  Object result = rs.getObject(index);
                  logger.debug("Query result: " + result);
                  return result;
               }
               else
               {
                  logger.debug("No Query Result");
               }
            }
            catch(SQLException e)
            {
               logger.error("Problem while extracting results", e);
            }
            catch(PSInternalRequestCallException ice)
            {
               logger.error("Problem while extracting results", ice);
            }
            finally
            {
               irq.cleanUp();
            }
         }
         return null;
      }
   }

   /**
    * Creates a loggable string from an array.
    * 
    * @param array the array to log
    * @param title the tile of the block
    * 
    */
   private static String logArray(Object[] array, String title)
   {
      StringBuffer sb = new StringBuffer();
      sb.append("logging ");
      sb.append(title);
      if (array == null)
      {
         sb.append(" is null");
      }
      else
      {
         int sz = array.length;
         sb.append(" size ");
         sb.append(sz);
         sb.append(" values ");
         for (int i = 0; i < sz; i++)
         {
            Object val = array[i];
            if (val != null)
            {
               sb.append("\n class ");
               sb.append(val.getClass().getName());
               sb.append(" value ");
               sb.append(val);
            }
            else
            {
               sb.append("\n null");
            }
         }
      }
      return sb.toString();
   }

   /**
    * Error message for missing request name
    */
   private static final String MSG_REQUEST_REQUIRED = "The request name is required";

   /**
    * Error Message for missing attribute name
    */
   private static final String MSG_ATTRIB_REQUIRED = "The attribute name is required";

   /**
    * Error message for missing column index
    */
   private static final String MSG_INDEX_REQUIRED = "The index value is required";

   /**
    * Error message for invalid column index
    */
   private static final String MSG_BAD_INDEX = "The index value must be numeric ";

   /**
    * Error message for column index out of range
    */
   private static final String MSG_INDEX_RANGE = "The index value is out of range ";

}
