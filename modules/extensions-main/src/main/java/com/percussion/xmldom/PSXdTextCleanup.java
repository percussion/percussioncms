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

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.data.PSStylesheetCleanupFilter;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSControlRef;
import com.percussion.design.objectstore.PSField;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.string.PSXmlPIUtils;
import com.percussion.utils.string.PSXmlPIUtils.Action;
import com.percussion.utils.types.PSPair;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * A Rhythmyx extension used to translate a text field, parse it, tidy it, 
 * scan it for inline links and then place it back into the same field.
 * <p>
 * This extension is a standard Rhythmyx pre-exit. There are 7 parameters:
 * <table border="1">
 * <tr>
 *    <th>Param #</th>
 *    <th>Name</th>
 *    <th>Description</th>
 *    <th>Required?</th>
 *    <th>default value</th>
 * <tr>
 * <tr>
 *    <td>1</td>
 *    <td>sourceFieldName</td>
 *    <td>
 *       The name of the field or file attachment that needs to be cleaned.
 *    </td>
 *    <td>yes</td>
 *    <td>&nbsp;</td>
 * </tr>
 * <tr>
 *    <td>2</td>
 *    <td>tidyPropertiesFile</td>
 *    <td>The tidy properties file used when parsing the source document.</td>
 *    <td>no</td>
 *    <td>None, tidy will be disabled if not provided.</td>
 * </tr>
 * <tr>
 *    <td>3</td>
 *    <td>serverPageTags</td>
 *    <td>
 *       The server page tags XML file used when parsing the source document.
 *    </td>
 *    <td>no</td>
 *    <td>None, server page tags are disabled if not supplied.</td>
 * </tr>
 * <tr>
 *    <td>4</td>
 *    <td>encodingOverride</td>
 *    <td>The name of the encoding to use when reading a file.</td>
 *    <td>no</td>
 *    <td>
 *       The encoding reported by the browser will be used. If this encoding is
 *       null, the default encoding for the platform will be used.
 *    </td>
 * </tr>
 * <tr>
 *    <td>5</td>
 *    <td>inlineDisable</td>
 *    <td>
 *       Disables scanning for in-line links if 'yes' (case insensitive) is 
 *       supplied.
 *    </td>
 *    <td>no</td>
 *    <td>If this parameter is null, in-line scanning will occur.</td>
 * </tr>
 * <tr>
 *    <td>6</td>
 *    <td>prettyPrint</td>
 *    <td>
 *       To enable pretty printing of the output supply 'yes' (case 
 *       insensitive).
 *    </td>
 *    <td>no</td>
 *    <td>Defaults to 'no'.</td>
 * </tr>
 * </table>
 * <p>
 * The source text field is always an HTML parameter. This exit will
 * automatically detect if the uploaded field is an attached file or an HTML
 * field, and process it accordingly.
 * <p>
 * The Tidy properties file is optional. If it is provided, the text will be 
 * run through the HTML Tidy program before parsing. See the
 * {@link <a href="http://www.w3.org/People/Raggett/tidy" target="_blank">
 * W3C HTML Tidy</a>} page for details and properties file formats. Note that 
 * this implementation uses the Java version of Tidy, which is not exactly 
 * identical to the C implementation. The pathname provided must be relative 
 * to the Rhythmyx server root.
 * <p>
 * The ServerPageTags.xml file is used to elmininate certain non-parsible text
 * generated by some editing programs. For details see
 * {@link ProcessServerPageTags ProcessServerPageTags}. The pathname is
 * relative to the server root.
 * <p>
 * Rhythmyx provides default files for Tidy (rxW2Ktidy.properties) and
 * ServerPageTags (rxW2KServerPageTags.xml).
 * <p>
 */

public class PSXdTextCleanup extends PSDefaultExtension
   implements IPSRequestPreProcessor
{
   /**
    * This method handles the pre-exit request.
    *
    * @param params an array of objects representing the parameters. See the
    * description under {@link PSXdTextToDom} for parameter details.
    *
    * @param request the request context for this request
    *
    * @throws PSExtensionProcessingException when a run time error is detected.
    */
   @SuppressWarnings("unchecked")
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSAuthorizationException, PSRequestValidationException,
      PSParameterMismatchException, PSExtensionProcessingException
   {
      String encodingDefault = null;
      boolean inlineDisable = false;
      Document tempXMLDocument = null;

      PSXmlDomContext contxt = new PSXmlDomContext(ms_className, request);
      contxt.setRxCommentHandling(true);

      if (params.length < 1 || null == params[0] || 
         0 == params[0].toString().trim().length())
         throw new PSParameterMismatchException(params.length, 1);

      /*
       * fileParamName is also the field name 
       */
      String fileParamName = PSXmlDomUtils.getParameter(params, 0, null);

      String param = PSXmlDomUtils.getParameter(params, 1, null);
      if (param != null && param.length() > 0)
      {
         try
         {
            contxt.setTidyProperties(param);
         }
         catch (IOException e)
         {
            contxt.printTraceMessage("Tidy Properties file " + 
               params[1].toString().trim() + " not found ");
            
            throw new PSExtensionProcessingException(ms_className, e);
         }
      }
      
      String contenttypeid = request.getParameter(IPSHtmlParameters.SYS_CONTENTTYPEID);
      PSField field = null;
      PSItemDefinition itemDef = null;
      
      if (!StringUtils.isBlank(contenttypeid))
      {
         // Null for search
         field = getField(fileParamName,contenttypeid);
         try
         {
            itemDef = PSItemDefManager.getInstance().getItemDef(
               Long.parseLong(contenttypeid),
               request.getSecurityToken());
         }         
         catch (PSInvalidContentTypeException e)
         {
            throw new PSExtensionProcessingException(ms_className, e);
         }
      }
      boolean escape_tags = field != null && field.isAllowActiveTags();
      boolean cleanup_namespaces = field != null && field.isCleanupNamespaces();
      String declared_namespaces[] = field != null ? field
            .getDeclaredNamespaces() : null;
      
      param = PSXmlDomUtils.getParameter(params, 2, null);
      if (param != null && param.length() > 0)
         contxt.setServerPageTags(param);

      param = PSXmlDomUtils.getParameter(params, 3, null);
      if (param != null)
         encodingDefault = param;

      param = PSXmlDomUtils.getParameter(params, 4, null);
      if (param != null && param.toLowerCase().startsWith("y"))
         inlineDisable = true;

      param = PSXmlDomUtils.getParameter(params, 5, null);
      if (param != null && param.equalsIgnoreCase("yes"))
         contxt.setUsePrettyPrint(false);

      Object inputSourceObj = request.getParameterObject(fileParamName);
      if (null == inputSourceObj)
      {
         // there is no input source. This is not really an error.
         request.printTraceMessage("source is null, exiting");
         return;
      }
      
      try
      {
         if (inputSourceObj instanceof PSPurgableTempFile)
         {
            String encoding = null;
            PSPurgableTempFile inputSourceFile =
                  (PSPurgableTempFile) inputSourceObj;
            request.printTraceMessage
                  ("Loading file " + inputSourceFile.getSourceFileName());

            encoding = PSXmlDomUtils.determineCharacterEncoding
                  (contxt, inputSourceFile, encodingDefault);
            if (encoding == null)
            {
               tempXMLDocument = PSXmlDomUtils.loadXmlDocument
                     (contxt, (File) inputSourceObj);
            }
            else
            {
               tempXMLDocument = PSXmlDomUtils.loadXmlDocument
                     (contxt, (File) inputSourceObj, encoding);
            }
         }
         else
         {
            // inputSourceObj is a String
            String inputSourceString = inputSourceObj.toString().trim();
            if (inputSourceString.length() < 1)
            {
               contxt.printTraceMessage("the source is empty");
               return;
            }
            PSPair<Map<Integer, PSPair<Action, String>>, String> piresult = null;

            if (escape_tags)
            {
               piresult = PSXmlPIUtils.encodeTags(inputSourceString);
               inputSourceString = piresult.getSecond();
            }

            tempXMLDocument = PSXmlDomUtils.loadXmlDocument(contxt,
                   inputSourceString);

            if (escape_tags)
            {
               PSXmlPIUtils.substitutePIs(tempXMLDocument, piresult.getFirst());
            }
            if (tempXMLDocument == null)
            {
               contxt.printTraceMessage("the source document is null");
               return;
            }
         }

         NodeList nl = tempXMLDocument.getElementsByTagName("body");
         Element divBody = null;
         Map nsMap = new HashMap();
         if(nl.getLength() > 0)
           divBody = (Element)nl.item(0);
         if(divBody!=null)
         {
            NamedNodeMap nm = ((Element)divBody).getAttributes();
            for(int i=0; i<nm.getLength(); i++)
            {
               Attr atr = (Attr)nm.item(i);
               if(atr.getName().startsWith("xmlns"))
                  nsMap.put(atr.getName(), atr.getValue());
            }
         }
        Document resultDoc = findBodyField(contxt, tempXMLDocument);

        if (resultDoc != null)
         {
           classicCleanup(request, inlineDisable, contxt, fileParamName,
                 nsMap, resultDoc, itemDef, field);
           if (cleanup_namespaces)
              improvedCleanup(resultDoc, declared_namespaces);  
           String outputString =
              PSXmlDomUtils.copyTextFromDocument(contxt, resultDoc, ".");
           request.setParameter(fileParamName, outputString);           
         }
      }
      catch (Exception e)
      {
         // TODO: catch Exceptions explicitly
         contxt.handleException(e);
      }
   }

   /**
    * The improved cleanup of namespaces. In this we just strip namespace
    * declarations and add the specific configured namespaces.
    * 
    * @param resultDoc the document to examine and modify, assumed not <code>null</code>
    * @param declared_namespaces the default namespaces to add
    */
   private void improvedCleanup(Document resultDoc, String[] declared_namespaces)
   {
      NodeList nl = resultDoc.getElementsByTagName("div");
      PSStylesheetCleanupFilter scf = PSStylesheetCleanupFilter.getInstance();
      
      // Clean existing declarations
      int len = nl.getLength();
      for(int i = 0; i < len; i++)
      {
         Element el = (Element) nl.item(i);
         removeNamespaceAttributes(el);

      }
      
      if (len > 0 && declared_namespaces != null)
      {
         Element el = (Element) nl.item(0);
         // Add needed namespaces
         for(String ns : declared_namespaces)
         {
            if (ns.equals(PSField.DEFAULT_NAMESPACE))
               ns = "";
            String uri = scf.getNSUri(ns);
            if (!StringUtils.isBlank(uri))
            {
               if (ns.length() == 0)
                  el.setAttribute("xmlns", uri);
               else
                  el.setAttribute("xmlns:" + ns, uri);
            }
         }
      }
   }
   
   /**
    * Clean the namespace attributes from a specific node
    * @param node the node, assumed not <code>null</code>
    */
   private void removeNamespaceAttributes(Node node)
   {
      NamedNodeMap nnm = node.getAttributes();
      Set<String> remove = new HashSet<>();
      if (nnm != null)
      {
         Element el = (Element) node;
         int len = nnm.getLength();
         for(int i = 0; i < len; i++)
         {
            Node attr = nnm.item(i);
            if (attr.getNodeName().startsWith("xmlns"))
            {
               remove.add(attr.getNodeName());
            }
         }
      
         for (String attr : remove)
         {
            el.removeAttribute(attr);
         }
      }
      NodeList children = node.getChildNodes();
      if (children != null)
      {
         int len = children.getLength();
         for(int i = 0; i < len; i++)
         {
            removeNamespaceAttributes(children.item(i));
         }
      }
   }

   /**
    * Do classic cleanup of field
    * 
    * @param request
    * @param inlineDisable
    * @param contxt
    * @param fileParamName
    * @param nsMap
    * @param resultDoc
    * @param itemDef the item definition of the contentype id for this field.
    * May be <code>null</code>.
    * @param field the field to be cleaned, may be <code>null</code>.
    * @throws FileNotFoundException
    * @throws IOException
    */
   private void classicCleanup(IPSRequestContext request, boolean inlineDisable,
      PSXmlDomContext contxt, String fileParamName, Map nsMap, Document resultDoc,
      PSItemDefinition itemDef, PSField field) throws FileNotFoundException, IOException
   {
      boolean isEditLiveControl = false;
      if(itemDef != null && field != null)
      {
         PSContentEditor ce = itemDef.getContentEditor();
         PSControlRef control = ce.getFieldControl(field.getSubmitName());
         isEditLiveControl = 
            control.getName().toLowerCase().indexOf("editlive") != -1;
      }
      NodeList nl;
      /* Sometimes text copied from Word has some extra process
       * instructions for namespace declarations, clean it up
       */
      nl = resultDoc.getElementsByTagName("div");
      Element divElem = null;
      if(nl.getLength() > 0)
        divElem = (Element)nl.item(0);
      if(divElem != null)
      {
         //transfer any namespace declarations from body element to div
         //element
         Iterator iter = nsMap.keySet().iterator();
         while(iter.hasNext())
         {
            String key = iter.next().toString();
            divElem.setAttribute(key, nsMap.get(key).toString());
         }
        cleanUpNameSpaces(divElem, divElem);
      }
      
      //cleanup custom rxwidth and rxheight
      cleanupImages(resultDoc);
      cleanupEmptyTables(resultDoc);
      if(isEditLiveControl)
         cleanupTrailingEmptyParagraphElements(resultDoc);         

      if (contxt.isLogging())
      {
         contxt.printTraceMessage("Writing file xmldombodyonly.txt");
         PSXmlTreeWalker outWalker = new PSXmlTreeWalker(resultDoc);
         try(FileOutputStream fos = new FileOutputStream("xmldombodyonly.txt")) {
            outWalker.write(fos);
         }
      }

      if (!inlineDisable)
      {
         // TODO: extract this logic into utility method
         String fullRoot = request.getRequestRoot();
         String realRoot = fullRoot.substring(0, fullRoot.lastIndexOf("/"));
         String hostPortRoot = request.getOriginalHost() + ":" +
               String.valueOf(request.getOriginalPort()) + realRoot;

         PSXdProcessRelatedLinks.processLinks(resultDoc, hostPortRoot);
      }
   }

   /**
    * Lookup the field to get important properties
    * @param name the field name, not <code>null</code> or empty
    * @param contenttypeid the content type id, not <code>null</code>
    * and numeric
    * @return the field, should never be <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private PSField getField(String name, String contenttypeid)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      if (StringUtils.isBlank(contenttypeid))
      {
         throw new IllegalArgumentException("contenttypeid may not be null or empty");
      }
      if (!StringUtils.isNumeric(contenttypeid))
      {
         throw new IllegalArgumentException("contenttypeid must be numeric");
      }
      PSItemDefManager idm = PSItemDefManager.getInstance();
      long ctypeids[] = new long[1];
      ctypeids[0] = Long.parseLong(contenttypeid);
      Collection<PSField> fields = idm.getFieldsByName(ctypeids, name);
      if (fields == null || fields.size() == 0)
      {
         throw new IllegalStateException("No field found for " + name);
      }
      return fields.iterator().next();
   }

   /**
    * Looks at img elements. If found checks to see if width and rxwidth are set.
    * If both are the same, removes either one. If width and height are different
    * then only removes rxwidth and leaves width alone.
    * Same logic of course applies to height and rxheight.
    * 
    * @param doc The doc, never <code>null</code>, may be <code>empty</code>.
    */
   private void cleanupImages(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      NodeList nl = doc.getElementsByTagName("img");
      
      int len = nl.getLength();
      
      if (len < 1)
         return; //no images - nothing to do
      
      for (int i = 0; i < len; i++)
      {
         Node node = nl.item(i);

         if (!(node instanceof Element))
            continue;
         
         Element elImg = (Element)node;
         
         String height = elImg.getAttribute(IPSHtmlParameters.ATTR_HEIGHT);
         String width = elImg.getAttribute(IPSHtmlParameters.ATTR_WIDTH);
         String rxheight = elImg.getAttribute(IPSHtmlParameters.ATTR_RX_HEIGHT);
         String rxwidth = elImg.getAttribute(IPSHtmlParameters.ATTR_RX_WIDTH);

         if (height.trim().equals(rxheight.trim()) &&
             width.trim().equals(rxwidth.trim()))
         {
            //remove if actual image size is the same as its view 
            elImg.removeAttribute(IPSHtmlParameters.ATTR_HEIGHT);
            elImg.removeAttribute(IPSHtmlParameters.ATTR_WIDTH);

         }

         //always remove rxwidth and rxheight - need to be XHTML compliant!
         elImg.removeAttribute(IPSHtmlParameters.ATTR_RX_HEIGHT);
         elImg.removeAttribute(IPSHtmlParameters.ATTR_RX_WIDTH);
      }
   }
   
   /**
    * Removes any empty table elements from the passed in document.
    * A table is empty if it has no Element or Text node children.
    * @param doc the XML document, cannot be <code>null</code>.
    */
   private void cleanupEmptyTables(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      NodeList nl = doc.getElementsByTagName("table");
      int len = nl.getLength();
      if (len < 1)
         return; // nothing to do
      for (int i = 0; i < len; i++)
      {
         Node node = nl.item(i);
         if(isEmptyNode(node))
         {
            //remove the empty table
            Node parent = node.getParentNode();
            parent.removeChild(node);
         }
      }
   }
   
   /**
    * Removes all empty trailing P tags. This method works backwards and
    * removes empty P tags until another type of element or text is encountered.
    * A P tag is considered empty if it has no characters except for tab, space, nbsp or
    * line feed.
    * @param doc the XML document containing the body, assumed not <code>null</code>.
    */
   private void cleanupTrailingEmptyParagraphElements(Document doc)
   {
        Element divEl = doc.getDocumentElement();
        // Get body div element
        if(divEl != null && divEl.getNodeName().equalsIgnoreCase("div"))
        {
           // Verify that this is indeed the body div with the rxbodyfield
           // class attribute
           String classAttrVal = divEl.getAttribute("class");
           if(classAttrVal != null && classAttrVal.equals(RXBODYFIELD_CLASS))
           {
              Node current = divEl.getLastChild();
              boolean stop = false;
              // loop through all immiediate children of the div element
              while(!stop && current != null)
              {
                 if(current instanceof Element)
                 {
                    Element el = (Element)current;
                    if(current.getNodeName().equalsIgnoreCase("p"))
                    {
                       // Is this element empty
                       boolean isEmpty = true;
                       NodeList nl = el.getChildNodes();
                       int len = nl.getLength();
                       for(int i = 0; i < len; i++)
                       {
                          Node item = nl.item(i);
                          if(item instanceof Element)
                          {
                             isEmpty = false;
                             break;
                          }
                          if(item instanceof Text)
                          {
                             if(!isTextNodeEmpty((Text)item))
                             {
                                isEmpty = false;
                                break;
                             }
                          }
                       }
                       current = el.getPreviousSibling();
                       if(isEmpty)
                       {
                          Node parent = el.getParentNode();
                          parent.removeChild(el);
                       }
                       else
                       {
                          stop = true;
                       }
                    }
                    else
                    {
                       stop = true;
                    }
                 }
                 else
                 {
                    current = current.getPreviousSibling();
                 }
              }
           }
        }
   }  
   
   /**
    * Determines whether a text node is empty.
    * A text node is empty if it is not an empty or <code>null</code> string and
    * does not contain entities other than:
    * <table>
    * <tbody>
    *    <tr><th>Numeric</th><th>Description</th></tr>
    *    <tr><td>9</td><td>Tab</td></tr>
    *    <tr><td>10</td><td>Line Feed</td></tr>
    *    <tr><td>32</td><td>Space</td></tr>
    *    <tr><td>160</td><td>Non-breaking Space</td></tr>
    * </tbody>   
    * </table>
    * @param node assumed not <code>null</code>.
    * @return <code>true</code> if empty.
    */
   private boolean isTextNodeEmpty(Text node)
   {
      String data = node.getData();     
      for(int c : data.toCharArray())
      {
         if(!(c == 9 || c == 10 || c == 32 || c == 160))
            return false;
      }
      return true;
   }
   
   /**
    * Helper method to determine if a node is considered empty. A node
    * is empty if it does not contain element or text node children.
    * @param node cannot be <code>null</code>.
    * @return <code>true</code> if the node is empty.
    */
   private boolean isEmptyNode(Node node)
   {
      if(node == null)
         throw new IllegalArgumentException("node cannot be null");
      if(!node.hasChildNodes())
         return true;
      NodeList nl = node.getChildNodes();
      int len = nl.getLength();
      for(int i = 0; i < len; i++)
      {
         Node n = nl.item(i);
         if(n instanceof Element || n instanceof Text)
            return false;
      }
      return true;
   }
   
   /**
    * this method finds the body field and returns it in a new document
    * there are a couple of different places that the body field can be found
    * If normal Tidy has run, the field will always contain
    * <code>&lt;html&gt;</code> tags. Otherwise, the leading
    * <code>&lt;div&gt;</code> tag
    * may be found at the beginning of the field.
    *
    * @param ctx the context for this XMLDom instance
    *
    * @param inputDoc the XML Document that contains the parsed input field.
    *
    * @return the document after post processing.
    *
    **/
   private Document findBodyField(PSXmlDomContext ctx, Document inputDoc)
         throws PSExtensionProcessingException
   {
      //a new empty document
      Document outputDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlTreeWalker srcWalker = new PSXmlTreeWalker(inputDoc);
      Element docElement = (Element) srcWalker.getCurrent();

      if (docElement.getNodeName().equalsIgnoreCase("div") &&
            docElement.getAttribute("class") != null &&
            docElement.getAttribute("class").equals(RXBODYFIELD_CLASS))
      {
         Node importNode = outputDoc.importNode(docElement, true);
         outputDoc.appendChild(importNode);
         return outputDoc;
      }

      Element bodyElement = srcWalker.getNextElement("body", true);
      if (bodyElement == null)
      {
         ctx.printTraceMessage("body not found");
         throw new PSExtensionProcessingException(0, "cannot find <body> element");
      }
      if (!bodyElement.hasChildNodes())
      {
         ctx.printTraceMessage("body node is empty");
         return null;
      }
      NodeList bodyNodes = bodyElement.getChildNodes();

      int i = 0;
      Element outputRoot = null;

      Node firstBody = bodyNodes.item(0);
      ctx.printTraceMessage("first body node is:" + firstBody.getNodeName());
      while (i < bodyNodes.getLength()
            && PSXmlDomUtils.isOnlyWhiteSpace(bodyNodes.item(i)))
      {
         i++;
      }
      if (i >= bodyNodes.getLength())
      {
         // there are no non-whitespace nodes in the body
         // tidy has failed, but it didn't tell us
         ctx.printTraceMessage("no non-whitespace nodes in the body");
         // ?? TODO: should we throw an exception here ??
         return null;
      }
      firstBody = bodyNodes.item(i);

      if (firstBody.getNodeType() == Node.ELEMENT_NODE &&
            ((Element) firstBody).getNodeName().equalsIgnoreCase("div") &&
            ((Element) firstBody).getAttribute("class") != null &&
            ((Element) firstBody).getAttribute("class").equals(RXBODYFIELD_CLASS))
      {
         // the first body field is a <div class="rxbodyfield">
         outputRoot = (Element) firstBody.cloneNode(true); //first node becomes the root
         i++; // subsequent loop skips this node.
      }
      else
      {
         //we need to make a new root
         outputRoot = outputDoc.createElement("div");
         outputRoot.setAttribute("class", RXBODYFIELD_CLASS);
         // subsequent loop starts with first non-whitespace node.
      }
      outputRoot = (Element)PSXmlDocumentBuilder.copyTree(outputDoc, outputDoc,
         outputRoot, true);

      /*
      loop through the rest of the body nodes, appending them to the root
      Note that there can be nodes after the <div class="rxbodyfield> node.
      This can happen if the user appends text to the end of the field in the
      DHTML editor.
      */
      while (i < bodyNodes.getLength())
      {
         Node importNode = outputDoc.importNode(bodyNodes.item(i), true);
         outputRoot.appendChild(importNode);
         i++;
      }
      if(isElementEmpty(outputRoot))
         outputDoc.removeChild(outputRoot);
      return outputDoc;
   }
   
   /**
    * Determines if that the element should be considered empty. This
    * is needed for content from the rich text editor so we can determine
    * if the &lt;Div> tag with class rxbodyfield should be added. There
    * is similar logic in the Ephox EditLive javascript that follows these
    * rules to determine empty content.
    * @param elem to be checked, assumed not <code>null</code>.
    * @return <code>true<code> if one of the following:
    * <pre>
    * &lt;p>&lt;/p>
    * &lt;p>&amp;#160;&lt;/p>
    * &lt;p>&amp;nbsp;&lt;/p>
    * &amp;nbsp;
    * &amp;#160;
    * </pre>
    */
   private boolean isElementEmpty(Element elem)
   {
      if(!elem.hasChildNodes())
         return true;
      NodeList nl = elem.getChildNodes();
      int len = nl.getLength();
      //If there are more than one child node treat it as not empty and return. 
      if(len>1)
         return false;
      int pCount = 0;
      for(int i = 0; i < len; i++)
      {
         Node node = nl.item(i);
         if(node.getNodeType() == Node.ELEMENT_NODE)
         {
            String name = node.getLocalName().toLowerCase();
            if(name.equals("p"))
            {
               // If there are more then one set of <p> tags then
               // it is considered not empty.
               if(++pCount > 1)
                  return false;               
               if(!isElementEmpty((Element)node))
                  return false;
            }
            else
            {
               // If we find an element that is not a P tag, then it is
               // considered as not empty
               return false;
            }
         }         
         else if(node.getNodeType() == Node.TEXT_NODE)
         {
            String val = ((Text)node).getData();
            if(!isWhitespace(val))
               return false;
         }
      }
      return true;
   }
   
   /**
    * A special whitespace detection method that also considers
    * the html non breaking space entity &amp;#160; as a whitespace
    * character.
    * @param str the string to evaluate. Assumed not <code>null</code>.
    * @return <code>true</code> if the string contains only whitespace 
    * characters.
    */
   private boolean isWhitespace(String str)
   {
      char[] chars = str.toCharArray();
      for(int i = 0; i < chars.length; i++)
      {
         int ch = chars[i];
         if(CharUtils.isAsciiControl(
            chars[i]) || ch == 32 || ch == 160 || ch == 173)
            continue;
         return false;
      }
      return true;
   }

   /**
    * This method removes all process instructions for namespace declaration
    * from the given node and its children recursively. These namespace
    * definitions will be moved to the the element sent as the second parameter.
    * @param node, may be <code>null</code>, in which case the node is not
    * affected.
    * @param divElem, Element of the document the namespace attributes are
    * added to, cannot be <code>null</code>. If <code>null</code> the node is
    * not processed
    */
   private void cleanUpNameSpaces(Node node, Element divElem)
   {
      if (node == null)
         return;

      if (node instanceof ProcessingInstruction)
      {
         //process only if the there is div element
         if(divElem != null)
         {
            ProcessingInstruction pi = (ProcessingInstruction)node;
            String target = pi.getTarget();
            if(target.equals("xml:namespace"))
            {
               String data = pi.getData();
               String prefix = extractAttribute("prefix", data);
               String urn = extractAttribute("ns", data);
               String attrib = divElem.getAttribute("xmlns:" + prefix);
               if(prefix != null && urn != null && attrib.trim().length() < 1)
                  divElem.setAttribute("xmlns:" + prefix, urn);
               node.getParentNode().removeChild(node);
            }
         }
      }
      else
      {
         if (node.hasChildNodes())
         {
            NodeList kids = node.getChildNodes();
            for (int i = 0; i < kids.getLength(); i++)
            {
               cleanUpNameSpaces(kids.item(i), divElem);
            }
         }
      }
   }

   /**
    * Helper function that extracts a given attribute from the supplied string
    * of the format: attr1="val1" attr2="val2". An attribute values may contain
    * whitespaces inside, value may be enclosed within single or double quotes.
    * Quotes (double or single) around the attribute values are trimmed.
    * @param attribName, name of the attribute to be extracted, must not
    * <code>null</code>.
    * @param src, String to be parsed and extracted the attribute from,
    * if <code>null</code> result will be <code>null</code>.
    * @return value of the attribute.
    */
   private String extractAttribute(String attribName, String src)
   {
      String result = null;
      if(attribName == null)
         return result;
      int loc = src.indexOf(attribName);
      if(loc != -1)
      {
         src = src.substring(loc+attribName.length());
         loc = src.indexOf('=');
         if(loc != -1)
         {
            src = src.substring(loc+1).trim();
            if(src.startsWith("\""))
            {
               src = src.substring(1);
               loc = src.indexOf('\"');
               if(loc != -1)
                  result = src.substring(0, loc);
            }
            else if(src.startsWith("'"))
            {
               src = src.substring(1);
               loc = src.indexOf('\'');
               if(loc != -1)
                  result = src.substring(0, loc);
            }
            else
            {
               loc = src.indexOf(' ');
               if(loc != -1)
                  result = src.substring(0, loc);
               else
                  result = src;
            }
         }
      }
      return result;
   }

   /**
    * The function name used for error handling
    **/
   private static final String ms_className = "PSXdTextCleanup";
   
   /**
    * The rxbodyfield class that indicates the body div.
    */
   private static final String RXBODYFIELD_CLASS = "rxbodyfield";
}


