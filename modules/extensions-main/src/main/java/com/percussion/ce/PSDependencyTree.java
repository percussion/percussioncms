/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.ce;

import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.extension.*;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class modifies the result document to append all child and paraent
 * dependents of the current content item as a tree.
 */
public class PSDependencyTree implements IPSResultDocumentProcessor
{

   private static final Logger log = LogManager.getLogger(PSDependencyTree.class);

   /*
    * Implementation of the method required by the interface IPSExtension.
    */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
   }

   /*
    * Implementation of the method required by the interface
    * IPSResultDocumentProcessor.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /*
    * Implementation of the method required by the interface
    * IPSResultDocumentProcessor.
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resDoc)
         throws PSParameterMismatchException,
               PSExtensionProcessingException
   {
      Map<String,Object> htmlParams = request.getParameters();
      ArrayList itemsRendered = new ArrayList();
      PSXmlTreeWalker walker = new PSXmlTreeWalker(resDoc);
      try
      {
         String indent = INDENT;
         String url = walker.getElementData("childlinkurl");

         // get the relationshiptype, by default use related content
         String relationshiptype = request.getParameter(
            IPSHtmlParameters.SYS_RELATIONSHIPTYPE, PSRelationshipConfig.TYPE_RELATED_CONTENT);

         Iterator configs =
            PSRelationshipCommandHandler.getRelationshipConfigs();
         Element parent = PSXmlDocumentBuilder.addEmptyElement(
            resDoc, resDoc.getDocumentElement(), "Relationships");
         parent.setAttribute("showRelationshiptype", relationshiptype);
         while (configs.hasNext())
         {
            PSRelationshipConfig config = (PSRelationshipConfig) configs.next();
            Element relationship = PSXmlDocumentBuilder.addElement(resDoc,
               parent, "Relationship", config.getName());
            if (config.getName().equals(relationshiptype))
               relationship.setAttribute("selected", "yes");
         }

         parent = PSXmlDocumentBuilder.addElement(
            resDoc, resDoc.getDocumentElement(), "children", null);
         processUrl(itemsRendered, indent, parent, request, url);

         itemsRendered.clear();
         indent = INDENT;
         url = walker.getElementData("parentlinkurl");
         parent = PSXmlDocumentBuilder.addElement(
            resDoc, resDoc.getDocumentElement(), "parents", null);
         processUrl(itemsRendered, indent, parent, request, url);
      }
      catch(Exception e)
      {
         throw new PSExtensionProcessingException("sys_ceDependencyTree", e);
      }
      request.setParameters(htmlParams);
      return resDoc;
   }

   /**
    * This method is called recursively to render the child and/or parent items
    * to render their children or parents.
    * @param itemsRendered is a list all items rendered so far. List is different
    *    for child treeand parent tree.
    * @param indent to be used to indicate the child-parent relationship. Each
    * recursive call increments the indent by INDENT.
    * @param parent is the result element being built
    * @param request HTTPConnection object to extract the child XML document.
    * @param url the URL object for the child or parent item of the current
    * item
    */
   private void processUrl(ArrayList itemsRendered, String indent, Element parent,
      IPSRequestContext request, String url)
   {
      try
      {
         String resource = "";
         String root = PSServer.getRequestRoot() + "/";
         int loc = url.indexOf(root);
         if(loc > -1)
         {
            url = url.substring(loc + root.length());
            loc = url.indexOf("?");
            if(loc > -1)
               resource = url.substring(0, loc);
            else
               resource = url;
         }
         loc = resource.indexOf('.');
         if(loc > -1)
            resource = resource.substring(0, loc);
         request.setParameters(parseUrlForParams(url));
         IPSInternalRequest iReq = request.getInternalRequest(resource);
         Document doc = iReq.getResultDoc();
         NodeList nl = doc.getElementsByTagName("Item");
         for(int i=0; i<nl.getLength(); i++)
         {
            Element elem = (Element) nl.item(i);
            String contentid = elem.getAttribute("contentid");
            if(contentid.trim().length() < 1)
               continue;

            Element res = PSXmlDocumentBuilder.addElement(
               parent.getOwnerDocument(), parent, "Item", null);
            res.setAttribute("contentid", contentid);
            res.setAttribute("relationshiptype",
               elem.getAttribute("relationshiptype"));
            PSXmlDocumentBuilder.addElement(
               parent.getOwnerDocument(), res, "text",
               indent + makeItemText(elem, res));
            PSXmlDocumentBuilder.addElement(parent.getOwnerDocument(), res, "link",
               getElementData(getChildElement(elem, "PreviewLink")));

            url = getElementData(getChildElement(elem, "ChildLink"));
            if(itemsRendered.indexOf(contentid) == -1)
            {
               itemsRendered.add(contentid);
               processUrl(itemsRendered, indent + INDENT, parent, request, url);
            }
            else
               res.setAttribute("repeat", "y");
         }
      }
      catch(Exception e)
      {
         PSXmlDocumentBuilder.addElement(
               parent.getOwnerDocument(), parent, "ExitError", e.getMessage());
      }
   }

   private HashMap parseUrlForParams(String url)
   {
      HashMap map = new HashMap();
      int loc = url.indexOf('?');
      if(loc == -1)
         return map;

      url = url.substring(loc+1);

      String pair, param, value;
      do
      {
         loc = url.indexOf('&');
         if(loc == -1)
         {
            pair = url;
            url = "";
         }
         else
         {
            pair = url.substring(0, loc);
            url = url.substring(loc+1);
         }

         loc = pair.indexOf('=');
         if(loc == -1)
         {
            param = pair;
            value = "";
         }
         else
         {
            param = pair.substring(0, loc);
            value = pair.substring(loc+1);
         }
         map.put(param, value);
      } while(url.length() > 0);

      return map;
   }

   /**
    * This helper function builds the text to be rendered by stylesheet for each
    * item in the tree by appending titlt, contentid, content type etc.
    */
   private String makeItemText(Element elem, Element res)
   {
      String result = getElementData(getChildElement(elem, "Title"));
      result = result + " (" + elem.getAttribute("contentid") + ") ";
      result = result + " : " + getElementData(getChildElement(elem, "ContentType"));
      Element workflow = getChildElement(elem, "Workflow");
      if(workflow.getAttribute("stateValid").equalsIgnoreCase("y"))
         res.setAttribute("public", "y");
      else
         res.setAttribute("public", "n");

      res.setAttribute("state",
         getElementData(getChildElement(workflow, "State")) +
         " (" + workflow.getAttribute("Stateid") + ")");

      return result;
   }

   /**
    * Helper function to return the first child element with iven name of a
    * given paranet.
    * @param parent, parent element - may be <code>null</code>
    * @param child, child element name may be <code>null</code>
    */
   private Element getChildElement(Element parent, String child)
   {
      if(parent == null)
         return null;

      if(child == null || child.trim().length() < 1)
         return null;

      NodeList nl = parent.getElementsByTagName(child);
      if(nl == null || nl.getLength() < 1)
         return null;
      return (Element)nl.item(0);
   }

   /**
    * Helper function to get the text data of a given element
    * @param elem - Elelemnt to extract data of - may be <code>null</code>.
    */
   private String getElementData(Element elem)
   {
      if(elem == null)
         return "";
      Node node = elem.getFirstChild();
      if(node != null && node instanceof Text)
      {
         return ((Text)node).getData();
      }
      return "";
   }

   /**
    * Indent string for rendering the tree for the style sheet
    */
   static public final String INDENT = "..";

   /**
    * Main routine for testing
    */
   public static void main(String[] args)
   {

      PSDependencyTree pSDependencyTree = new PSDependencyTree();
      try(FileInputStream fis = new FileInputStream("c:/depend.xml")){
         Document doc = PSXmlDocumentBuilder.createXmlDocument(new InputSource(fis), false);
         doc = pSDependencyTree.processResultDocument(null, null, doc);

         StringWriter sw = new  StringWriter();
         System.out.print(sw.toString());
      }
      catch(Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
  }
}
