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
