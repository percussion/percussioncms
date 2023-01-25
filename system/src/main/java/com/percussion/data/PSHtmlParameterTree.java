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

package com.percussion.data;

import com.percussion.error.PSRuntimeException;
import com.percussion.server.PSRequest;
import com.percussion.util.PSBase64Encoder;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The PSHtmlParameterTree class is used to generate an XML document
 * from HTML parameters.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSHtmlParameterTree
{
   /**
    * Construct an XML input document from the HTML parameters associated
    * with the specified request. The structure created by this is:
    * <PRE><CODE>
    *    &lt;PSXParams&gt;
    *       &lt;PSXParam&gt;
    *          &lt;param1&gt;
    *          &lt;/param1&gt;
    *            ...
    *          &lt;paramX&gt;
    *          &lt;/paramX&gt;
    *       &lt;/PSXParam&gt;
    *       &lt;PSXParam&gt;
    *          &lt;param1&gt;
    *          &lt;/param1&gt;
    *            ...
    *          &lt;paramX&gt;
    *          &lt;/paramX&gt;
    *       &lt;/PSXParam&gt;
    *    &lt;/PSXParams&gt;
    *
    * @param   request      the request context
    */
   public static Document generateHtmlParameterTree(PSRequest request)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, "PSXParams");

      // we will store an array list containing the PSXParam nodes so we
      // can quickly access the parameter for a given parameter level
      ArrayList paramNodeList = new ArrayList();

      Map params = request.getBalancedParameters();
      if ((params != null) && (params.size() != 0)) {
         Iterator ite = params.entrySet().iterator();
         while (ite.hasNext()) {
            Map.Entry entry = (Map.Entry)ite.next();
            String name = (String)entry.getKey();
            Object value = entry.getValue();

            /* Add file support for binary input data (attachments)
               Bug Id: Rx-99-10-0016 */
            if (value instanceof File) {
               File f = (File) value;
               URL u = null;
               try {
                  u = f.toURL();
               } catch(MalformedURLException e)
               {
                  // shouldn't happen!  if it does we will revert to the old way
               }
               
               if (u != null)
               {
                  addParameter(doc, root, paramNodeList, 0, name, "", 
                     PSXmlFieldExtractor.XML_URL_REFERENCE_ATTRIBUTE, u.toString());

                  /*
                   * If it's a purgable temp file, add it to the request so its
                   * properties can be available for use later during request
                   * processing.
                   */
                  if (f instanceof PSPurgableTempFile)
                     request.addTempFileResource((PSPurgableTempFile) f, u.toString());
               } else
               {
                  /* Convert file contents to Base64 */
                  FileInputStream fin = null;
                  try {
                     fin = new FileInputStream(f);
                  } catch (FileNotFoundException e) {
                     value = null;
                  }

                  if (fin != null) {
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                    
                     try {
                        PSBase64Encoder.encode(fin, outputStream);
                        value = outputStream;
                     } catch (IOException e)
                     {
                        /* throw runtime exception on IO exception */
                        throw new PSRuntimeException(
                           IPSDataErrors.DATA_CANNOT_CONVERT_WITH_REASON, 
                           new Object[]{name, "Base64", e.toString()});
                     }
                  }

                  addParameter(doc, root, paramNodeList, 0, name, value);
               }
            } else if (value instanceof List) {
               List valueList = (List)value;
               Object lastValue = null;
               for (int i = 0; i < valueList.size(); i++) {
                  value = valueList.get(i);
                  if (value == null)
                     value = lastValue;
                  else
                     lastValue = value;

                  addParameter(doc, root, paramNodeList, i, name, value);
               }
            }
            else {
               addParameter(doc, root, paramNodeList, 0, name, value);
            }
         }
      }

      return doc;
   }


   /**
    * Convenience method, addParameter call without optional attribute
    * parameters, specifies null for these parameters.
    *
    * @see #addParameter(Document, Element, List, int, String, Object, String,
    *    Object)
    */
   private static void addParameter(
      Document doc, Element root, List nodeList, int index,
      String name, Object value)
   {
      addParameter(doc, root, nodeList, index, name, value, null, null);
   }
   
   /**
    * Add an Html parameter value element to the PSXParam element in the node
    * list at the specified index.  If the node list doesn't contain enough
    * elements to contain the specified index, it will be padded with empty 
    * PSXParam elements so that this method may continue.  If the optional
    * attribute information is supplied, the Html parameter value element will
    * have the attribute set as specified.  The attribute information must
    * contain at the very least a non-empty attribute name in order to be set.
    *
    * @param doc  The document of Html parameters.
    *             Assumed not <code>null</code>.
    *
    * @param root The root element of the supplied document.
    *             Assumed not <code>null</code>.
    *
    * @param nodeList The list of PSXParam nodes. Assumed not <code>null</code>.
    *
    * @param index The index at which to insert the Html parameter element.
    *                Assumed to be a valid index (<code>&gt;=0</code>)
    *
    * @param name The name of the Html parameter. Assumed not <code>null</code>
    *             or empty.
    *
    * @param value The value of the Html parameter.  Can be <code>null</code>.
    *
    * @param attributeName Optional attribute name to be set on the Html
    *                      parameter value node.  May be <code>null</code>.
    *
    * @param attributeValue Optional attribute value to be set on the Html
    *                       parameter value node.  May be <code>null</code>.
    */
   private static void addParameter(
      Document doc, Element root, List nodeList, int index,
      String name, Object value, String attributeName, Object attributeValue)
   {
      Element paramNode = null;

      while (index >= nodeList.size()) {
         nodeList.add(PSXmlDocumentBuilder.addEmptyElement(
            doc, root, "PSXParam"));
      }

      paramNode = (Element)nodeList.get(index);
      Element e = PSXmlDocumentBuilder.addElement(
         doc, paramNode, name, ((value == null) ? "" : value.toString()));

      if ((attributeName != null) && (attributeName.length() > 0))
         e.setAttribute(attributeName,
            attributeValue == null ? "" : attributeValue.toString());
   }
}
