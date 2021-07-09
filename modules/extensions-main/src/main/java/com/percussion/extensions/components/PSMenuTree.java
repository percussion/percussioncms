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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.extensions.components;

import com.percussion.extension.*;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This exit builds a cascaded menu item list XML document by making multiple
 * internal requests to a rhythmyx resource. The tree we get depends on the
 * backend table RXSYSCOMPONENTS and RXSYSCOMPONENTRELATIONS tables. The XML
 * docuemnt generated shall have the DTD as follows:
 *
 * &lt;menuitem name="ca_inbox" id="20" type="2"&gt;
 *  &lt;displaytext&gt;Inbox&lt;/displaytext&gt;
 *  &lt;description&gt;Items assigned to me&lt;/description&gt;
 *  &lt;url&gt;http://10.10.10.56:9992/Rhythmyx/sys_ca/camain.html?sys_sortparam=title&amp;sys_componentname=ca_inbox&lt;/url&gt;
 *  &lt;userrolesurl&gt;http://127.0.0.1:9992/Rhythmyx/sys_cmpUserStatus/userstatus.xml?pssessionid=8037ca1cbcc8bd31e3db8b392d4fff8c62c9dacc&lt;/userrolesurl&gt;
 *  &lt;contexturl&gt;http://127.0.0.1:9992/Rhythmyx/sys_ComponentSupport/componentcontext.xml?pssessionid=8037ca1cbcc8bd31e3db8b392d4fff8c62c9dacc&amp;sys_componentid=20&lt;/contexturl&gt;
 *  &lt;componentname&gt;ca_inbox&lt;/componentname&gt;
 *  &lt;childitem id="1"/&gt;
 *  &lt;childitem id="2"/&gt;
 *  &lt;childitem id="6"/&gt;
 *  &lt;childitem id="7"/&gt;
 * &lt;/menuitem&gt;
 *
 * Multiple requests are made to expand each child item to menu item.
 */
public class PSMenuTree implements IPSResultDocumentProcessor
{
   /*
    * Implementation of the method required by the interface IPSExtension.
    */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      ms_fullExtensionName = extensionDef.getRef().toString();
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
      String rxAppResource = request.getCurrentApplicationName() + "/" +
         request.getRequestPage(false);
      if(rxAppResource.startsWith("/"))
         rxAppResource = rxAppResource.substring(1);

      Map<String,Object> htmlParams = request.getParameters();
      ArrayList itemsRendered = new ArrayList();
      Element elem = resDoc.getDocumentElement();
      String temp = elem.getAttribute("id").trim();
      if(temp.length() < 1)
         return resDoc;

      //If the item is of type "2" (page variant) no children are allowed!!!
      temp = elem.getAttribute("type").trim();
      if(temp.equals("2"))
         return resDoc;
      itemsRendered.add(temp);
      try
      {
         processItem(itemsRendered, resDoc.getDocumentElement(),
            request, rxAppResource);
      }
      catch(Exception e)
      {
         throw new PSExtensionProcessingException(ms_fullExtensionName, e);
      }
      request.setParameters(htmlParams);
      return resDoc;
   }

   /**
    * This method is called recursively to render the child and/or parent items
    * to render their children or parents.
    * @param itemsRendered is a list all items rendered so far. List is different
    *    for child treeand parent tree.
    * @param parent is the result element being built
    * @request <code>IPSRequestContext</code> object
    * @param rxAppResource the Rhythmyx application resource for making internal
    * request.
    */
   private void processItem(ArrayList itemsRendered, Element parent,
      IPSRequestContext request, String rxAppResource)
   {
      try
      {
         NodeList nl = parent.getChildNodes();
         if(nl == null || nl.getLength() < 1)
            return;
         HashMap params = new HashMap();
         Element elemItem = null;
         Element elemRes = null;
         String id = "";
         Node node = null;
         String temp = "";
         for(int i=0; i<nl.getLength(); i++)
         {
            node = nl.item(i);
            if(!(node instanceof Element))
               continue;

            elemItem = (Element)node;
            if(!elemItem.getTagName().equals("childitem"))
               continue;
            //If the item is of type "2" (page variant) no children are allowed!!!
            temp = elemItem.getAttribute("type").trim();
            if(temp.equals("2"))
               continue;

            id = elemItem.getAttribute("id");
            if(id.trim().length() < 1)
               continue;
            params.clear();
            params.put("sys_componentid", id);
            request.setParameters(params);
            IPSInternalRequest iReq =
               request.getInternalRequest(rxAppResource);
            iReq.makeRequest();
            Document doc = iReq.getResultDoc();
            iReq.cleanUp();
            elemRes = doc.getDocumentElement();
            Node importNode = parent.getOwnerDocument().importNode(
               elemRes, true);
            elemItem = (Element)parent.replaceChild(
               importNode, elemItem);
            if(itemsRendered.indexOf(id) == -1)
            {
               itemsRendered.add(id);
               processItem(itemsRendered, elemItem, request, rxAppResource);
            }
            else
               elemRes.setAttribute("repeat", "y");
         }
      }
      catch(Exception e)
      {
         PSConsole.printMsg("Exit:" + ms_fullExtensionName, e);
         PSXmlDocumentBuilder.addElement(
               parent.getOwnerDocument(), parent, "ExitError", e.getMessage());
      }
   }

   /**
    * Helper function to return the first child element with iven name of a
    * given paranet.
    * @param parent, parent element - may be <code>null</code>
    * @param child, child element name may be <code>null</code>
    * @return Child element with given name if exists, <code>null</code>
    * otherwise or if the parent or child element name is <code>null</code>.
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
    * @return element data represented by the first text child of the element.
    * Empty string if the Element or its first child is <code>null</code>.
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
    * The fully qualified name of this extension.
    */
   private String ms_fullExtensionName = "";
}
