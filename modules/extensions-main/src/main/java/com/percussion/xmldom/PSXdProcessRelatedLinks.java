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
package com.percussion.xmldom;

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A Rhythmyx pre-exit called to process inline related links that may exist
 * in an HTML body field.
 *
 * The related links of the form
 * <code>
 *    &lt;a href="http://RxServer:RxPort/Rhythmyx/AppName/Request.html?sys_contentid=123&amp;sys_variantid=1" &gt;
 * </code>
 * will be processed, and extra parameters for the <code>sys_contentid</code>
 * and <code>sys_variantid</code> will be added.
 * <p>
 * This processing will be performed for all links and images, or any other
 * <code>&lt;html&gt;</code> element with a <code>src=</code> or <code>href=</code>
 * attribute.
 * <p>
 * Before this exit can be called, the HTML text field must be converted to an
 * XMLDOM private object with sys_XdDOMToText. This function will also be called from
 * within sys_XdTextCleanup.
 * <p>
 * There is only one parameter: the name of the XMLDOM private object to be scanned.
 *
 *
 **/
public class PSXdProcessRelatedLinks extends PSDefaultExtension
      implements IPSRequestPreProcessor
{

   public void preProcessRequest(Object[] params,
                                 IPSRequestContext request)
         throws PSAuthorizationException,
         PSRequestValidationException,
         PSParameterMismatchException,
         PSExtensionProcessingException
   {
      Document tempXMLDocument = null;

      PSXmlDomContext contxt = new PSXmlDomContext(ms_className, request);

      String sourceName = PSXmlDomUtils.getParameter(params, 0, PSXmlDomUtils.DEFAULT_PRIVATE_OBJECT);

      Document sourceDoc;
      if (sourceName.equals("InputDocument"))
      {
         sourceDoc = request.getInputDocument();
      }
      else
      {
         sourceDoc = (Document) request.getPrivateObject(sourceName);
      }

      if (null == sourceDoc)
      {
         // there is no input document.  This is not necessarily an error
         request.printTraceMessage("no document found");
         return;
      }
      // TODO: extract this logic into utility method
      String fullRoot = request.getRequestRoot();
      String realRoot = fullRoot.substring(0, fullRoot.lastIndexOf("/"));
      String hostPortRoot = request.getOriginalHost() + ":" +
            String.valueOf(request.getOriginalPort()) + realRoot;

      PSXdProcessRelatedLinks.processLinks(sourceDoc, hostPortRoot);
   }

   /**
    * This method scans the elements of a org.w3c.dom.Document for any
    * InLine Related links or images.  This function will be used in this exit
    * as well as from @link{PSXdTextCleanup}.
    *
    * @param xmlDoc the source xml document to scan
    * @param HostPortRoot the Rhythmyx host, port and root name as a single
    * string such as: <code>RxServer:9992/Rhythmyx</code>
    **/
   static void processLinks(Document xmlDoc, String HostPortRoot)
   {

      String linkStart = "http://" + HostPortRoot.toLowerCase();
      searchNodes((Node) xmlDoc, linkStart);
   }

   /**
    * This method recursively scans all nodes that have children looking for
    * Element nodes.
    * @param nd the parent node to scan
    * @param linkStart the initial part of the link to scan for.
    **/
   private static void searchNodes(Node nd, String linkStart)
   {

      if (nd.getNodeType() == Node.ELEMENT_NODE)
      {
         searchElement((Element) nd, linkStart);
      }
      if (!nd.hasChildNodes())
      {
         return;
      }
      NodeList children = nd.getChildNodes();
      for (int i = 0; i < children.getLength(); i++)
      {
         searchNodes(children.item(i), linkStart);
      }
   }

   /**
    * This method scans the attributes of an element looking for a
    * <code>href</code> or <code>src</code> attribute.
    *
    * @param el the element to scan
    *
    * @param linkStart the initial part of the string to scan for
    *
    **/
   private static void searchElement(Element el, String linkStart)
   {
      String attribValue;
      for (int i = 0; i < ms_attribArray.length; i++)
      {
         attribValue = el.getAttribute(ms_attribArray[i]).toLowerCase();
         if (attribValue.length() > 0 &&
               attribValue.startsWith(linkStart) &&
               attribValue.indexOf(CONTENTID) > 0 &&
               attribValue.indexOf(VARIANTID) > 0)
         {
            parseElementValue(el, attribValue);
            return;
         }
      }
   }

   /**
    * This method scans an attribute value to determine if it is a link that
    * has <code>content id</code> and <code>variant id</code> parameters.
    * If it does, these parameters will be added as separate attributes of the
    * parent element.
    *
    * @param parent the parent element to attach the new attributes to
    *
    * @param sourceAttrib the <code>href<code> or other source attribute value
    * to be scanned.
    *
    **/
   private static void parseElementValue(Element parent, String sourceAttrib)
   {
      String sv;
      StringTokenizer st = new StringTokenizer(sourceAttrib.toLowerCase(), DELIMS);
      while (st.hasMoreTokens())
      {
         sv = st.nextToken();
         if (sv.equalsIgnoreCase(CONTENTID) && st.hasMoreTokens())
         {
            String sx = st.nextToken();
            if (sx.length() > 0)
            {
               parent.setAttribute(CONTENTID, sx);
            }
         }
         else if (sv.equalsIgnoreCase(VARIANTID) && st.hasMoreTokens())
         {
            String sx = st.nextToken();
            if (sx.length() > 0)
            {
               parent.setAttribute(VARIANTID, sx);
            }
         }
      }
   }

   /**
    * The allowable list of attributes to scan for.
    **/
   private static final String[] ms_attribArray = {
      "href",
      "src"
   };

   /**
    * The name of the content id HTML parameter
    **/
   private static final String CONTENTID = "sys_contentid";
   /**
    * The name of the variant id HTML parameter
    **/
   private static final String VARIANTID = "sys_variantid";
   /**
    * The allowable delimiters when scanning an <code>href</code> or
    * <code>src</code> attribute.
    **/
   private static final String DELIMS = "?&=";

   /**
    * the class name used for error reporting
    **/
   private static final String ms_className = "PSXdProcessRelatedLinks";
}
