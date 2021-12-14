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
package com.percussion.xmldom;

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A Rhythmyx pre-exit that examines a temporary XML object
 * and extracts HTML parameters.
 * The XML document must have been loaded previously with PSXdTextToDom.
 * <p>The parameter nodes are in the form:
 * <pre>
 *   &lt;PSXParam&gt;
 *       &lt;title&gt;This is the title&lt;/title&gt;
 *       &lt;author&gt;Author Name&lt;/author&gt;
 *   &lt;/PSXParam&gt;
 *
 * </pre>
 * Will add the "title" and "author" to the HTML parameter map, replacing
 * any "title" or "author" parameters that are already there if appendParameter
 * flag is not set or is "no". If flag is set to "yes" then the new values will
 * be appended to the existing parameters.
 * <p>
 *
 * <p>The parameters for this exit are:
 * <table border="1">
 *   <tr><th>Param #</th><th>Description</th><th>Required?</th>
 *   <th>default value</th><tr>
 *   <tr>
 *     <td>1</td>
 *     <td>the name of the temporary XML document object,
 *       which must have been loaded previously with
 *        com.percussion.xmldom.PSXdDomToText.</td>
 *     <td>yes</td>
 *     <td>XMLDOM</td>
 *   </tr>
 *   <tr>
 *     <td>2</td>
 *     <td>Append parameter flag. If set to "yes" the value will be appended to
 *        an existing parameter of the same name. If not set or set to "no" value
 *        will replace existing parameter with the same name.</td>
 *     <td>no</td>
 *     <td>no</td>
 *   </tr>
 * </table>
 * </p>
 */
public class PSXdDomToParams extends PSDefaultExtension implements
   IPSRequestPreProcessor
{
   /**
    * This method handles the pre-exit request.
    *
    * @param params an array of objects representing the parameters.
    *
    * @param request the request context for this request
    *
    * @throws PSExtensionProcessingException when a run time error is detected.
    *
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
          throws PSAuthorizationException,
                 PSRequestValidationException,
                 PSParameterMismatchException,
                 PSExtensionProcessingException
   {
      PSXmlDomContext contxt = new PSXmlDomContext(m_extname, request);
      String sourceName = PSXmlDomUtils.getParameter(params, 0,
         PSXmlDomUtils.DEFAULT_PRIVATE_OBJECT);

      m_appendParam =
         PSXmlDomUtils.getParameter(params, 1, "no").equalsIgnoreCase("yes");

      Document sourceDoc;
      if(sourceName.equals("InputDocument"))
      {
         sourceDoc = request.getInputDocument();
      }
      else
      {
         sourceDoc = (Document) request.getPrivateObject(sourceName);
      }

      if(null == sourceDoc)
      {
         // there is no input document.  This is not necessarily an error
         request.printTraceMessage("no document found");
         return;
      }

      extractParams(contxt, sourceDoc, request);
   }

   /**
    * Walk the DOM tree and extract the parameters into the HTML parameter map.
    *
    * @param cx The context for this exit, assumed not <code>null</code>.
    *
    * @param xmlDoc The document to scan, assumed not <code>null</code>.
    *
    * @param request The request context, assumed not <code>null</code>.
    */
   private void extractParams(PSXmlDomContext cx, Document xmlDoc,
      IPSRequestContext request)
   {
      PSXmlTreeWalker walker = new PSXmlTreeWalker(xmlDoc);
      //Find the PSXParam element
      Node realRoot = walker.getCurrent();

      if(realRoot.getNodeName().trim().equals("PSXParam") == false )
      {
         // The document root is not PSXParam, let's go look for it.
         Element PSXroot = walker.getNextElement("PSXParam");
         if(PSXroot == null)
         {
            //<PSXParam> was not found, we can't do much of anything
            cx.printTraceMessage("<PSXParam> not found");
            return;
         }
      }
      //Find the first child of PSXParam
      Element currNode = walker.getNextElement(walker.GET_NEXT_ALLOW_CHILDREN);
      for(int i=0;currNode != null;i++)
      {
         String tagName = currNode.getTagName();
         String tagData = walker.getElementData(currNode);
         if(m_appendParam)
         {
            request.appendParameter(tagName, tagData);
         }
         else
         {
            request.setParameter(tagName, tagData);
         }

         currNode = walker.getNextElement(walker.GET_NEXT_ALLOW_SIBLINGS);
      }
   }

   /**
    * Flag indicating parameter appending. Defaults to <code>false</code>.
    */
   private boolean m_appendParam = false;

   /**
    * The name of the class, used for error handling and tracing.
    */
   private static final String m_extname = "PSXdDomToParams";
}
