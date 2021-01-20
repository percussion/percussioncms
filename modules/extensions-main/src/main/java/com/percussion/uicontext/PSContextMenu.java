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

package com.percussion.uicontext;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSConsole;
import com.percussion.server.PSServerBrand;
import com.percussion.util.IPSBrandCodeConstants;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This exit expands the context menu action list in the result XML document by
 * building and adding the child actions recursively. This exit is specific to
 * context menu building for the User Interface. Multiple requests are made to
 * expand each child item to menu item. The generated XML document shall be of
 * the following DTD:
 *
 * &lt;!ELEMENT ActionList (Params?, ActionList*, Action+)&gt;
 * &lt;!ATTLIST ActionList
 * name (Edit | Item | View | Workflow) #REQUIRED
 * url CDATA #REQUIRED
 * uicontextid CDATA #REQUIRED
 * displayname CDATA #REQUIRED
 * actionid CDATA #REQUIRED
 * modeid CDATA #REQUIRED
 * &gt;
 * &lt;!ELEMENT Action (Params)&gt;
 * &lt;!ATTLIST Action
 * name CDATA #REQUIRED
 * url CDATA #REQUIRED
 * uicontextid CDATA #REQUIRED
 * displayname CDATA #REQUIRED
 * actionid CDATA #REQUIRED
 * modeid CDATA #REQUIRED
 * &gt;
 * &lt;!ELEMENT Param PCDATA&gt;
 * &lt;!ATTLIST Param
 * name CDATA #REQUIRED
 * &gt;
 * &lt;!ELEMENT Params (Param*)&gt;
 */
public class PSContextMenu implements IPSResultDocumentProcessor
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
         REQUEST_NAME;
      if(rxAppResource.startsWith("/"))
         rxAppResource = rxAppResource.substring(1);

      HashMap htmlParams = request.getParameters();
      ArrayList itemsRendered = new ArrayList();

      NodeList nl = resDoc.getDocumentElement().getElementsByTagName(ELEM_ACTION);
      Element elem = null;
      for(int i=0; i<nl.getLength(); i++)
      {
         elem = (Element)nl.item(i);
         String actionid = elem.getAttribute(ATTR_ACTIONID).trim();
         if(actionid.length() < 1)
            continue;

         String modeid = elem.getAttribute(ATTR_MODEID).trim();
         if(modeid.length() < 1)
            continue;

         String uicontextid = elem.getAttribute(ATTR_UICONTEXTID).trim();
         if(uicontextid.length() < 1)
            continue;

         itemsRendered.add(actionid);
         try
         {
            processItem(itemsRendered, elem,
               request, rxAppResource);
         }
         catch(Exception e)
         {
            throw new PSExtensionProcessingException(ms_fullExtensionName, e);
         }
      }
      //Translate action need to be removed from the list of actions if there is
      //only one language enabled. addTranslateAction function returns false if
      //there is only one language enabled otherwise true.
      boolean trreq = true;
      trreq = addTranslateAction(request);

      // check if the "Active Assembly for Documents" needs to be removed from
      // the context menu. This menu item is removed if the brand code does not
      // include the license for "Document Assembler".
      boolean isDocumentAssemblerLicensed = true;
      PSServerBrand brand = new PSServerBrand();
      if (!brand.isComponentLicensed(IPSBrandCodeConstants.DOCUMENT_ASSEMBLER))
         isDocumentAssemblerLicensed =  false;

      // create a list of menu items to be removed from the context menu
      List removeMenuItemsList = new ArrayList();
      if (!trreq)
         removeMenuItemsList.add(ACTION_TRANSLATE.toUpperCase());
      if (!isDocumentAssemblerLicensed)
         removeMenuItemsList.add(ACTION_DOC_ASSEMBLER.toUpperCase());

      NodeList actionNodeList = resDoc.getElementsByTagName(ELEM_ACTION);
      // remove menu items from the context menu
      removeMenuItems(actionNodeList, removeMenuItemsList);

      request.setParameters(htmlParams);
      return resDoc;
   }

   /**
    * Removes the menu items specified in <code>removeMenuItemsList</code> from
    * the context menu.
    *
    * @param actionNodeList the list of nodes corresponding to the menu items,
    * may be <code>null</code> or empty
    *
    * @param removeMenuItemsList list of menu items to be removed from the
    * context menu, assumed not <code>null</code>. This list contains the name
    * of menu actions to be removed as <code>String</code> objects in UPPERCASE
    * form.
    */
   private void removeMenuItems(NodeList actionNodeList, List removeMenuItemsList)
   {
      if ((actionNodeList == null) || (actionNodeList.getLength() < 1))
         return;
      if (removeMenuItemsList.size() < 1)
         return;

      Element actionElem = null;
      for(int j=0; j < actionNodeList.getLength();)
      {
         actionElem = (Element)actionNodeList.item(j);
         String actionName = actionElem.getAttribute(ATTR_NAME);
         if(removeMenuItemsList.contains(actionName.toUpperCase()))
         {
            // as soon as we remove the element from the document, the nodelist
            // gets updated, its length decreases by 1 and index of all elements
            // after the element that is deleted decreases by 1. So do not
            // increment the value of <code>j</code> when removing elements from
            // the document while iterating over a nodelist.
            actionElem.getParentNode().removeChild(actionElem);
         }
         else
         {
            j++;
         }
      }
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
      String actionid = parent.getAttribute(ATTR_ACTIONID).trim();
      if(actionid.length() < 1)
         return;

      String modeid = parent.getAttribute(ATTR_MODEID).trim();
      if(modeid.length() < 1)
         return;

      String uicontextid = parent.getAttribute(ATTR_UICONTEXTID).trim();
      if(uicontextid.length() < 1)
         return;
      Document doc = null;
      IPSInternalRequest iReq = null;
      Element elemRes = null;
      Node node = null;
      try
      {
         // query the child actions of the supplied action element
         HashMap params = new HashMap();
         params.put(ATTR_ACTIONID, actionid);
         params.put(ATTR_MODEID, modeid);
         params.put(ATTR_UICONTEXTID, uicontextid);
         request.setParameters(params);
         doc = null;
         try
         {

            iReq = request.getInternalRequest(rxAppResource);
            iReq.makeRequest();
            doc = iReq.getResultDoc();
         }
         finally
         {
            iReq.cleanUp();
         }
         elemRes = (Element)parent.getOwnerDocument().importNode(
               doc.getDocumentElement(), true);

         if(!hasActionChildren(elemRes))
            return;

         // now add the retrieved child actions
         NodeList nl = null;
         if(!parent.getNodeName().equals(ELEM_ACTION_LIST))
         {
            // Since this is the top level action, we need to replace the 
            // existing parent and copy its attributes to the new node.
            // Then add all props and params from the old parent to the new 
            // element.  
            Element old = (Element)parent.getParentNode().replaceChild(elemRes, 
               parent);
            NamedNodeMap map = parent.getAttributes();
            Attr attr = null;
            for (int i=0; map != null && i<map.getLength(); i++)
            {
               attr = (Attr)map.item(i);
               elemRes.setAttribute(attr.getName(), attr.getValue());
            }
            
            // get first existing child action of the new parent, so we can 
            // insert any Props or Params before it.
            Element firstChildAction = null;
            NodeList childActions = elemRes.getElementsByTagName("*");
            if (childActions != null && childActions.getLength() > 0)
               firstChildAction = (Element)childActions.item(0);
            
            // if firstChildAction is null, the elements are just appended
            nl = old.getChildNodes();
            int count = (nl== null)?0:nl.getLength();
            for(int i=0; i<count; i++)
               elemRes.insertBefore(nl.item(0), firstChildAction);
         }
         else
         {
            // this is a child action, so just append it's child actions
            nl = elemRes.getChildNodes();
            int count = (nl== null)?0:nl.getLength();
            for(int i=0; i<count; i++)
               parent.appendChild(nl.item(0));
         }

         // now walk the new child actions and recurse to get their children
         nl = parent.getChildNodes();
         if(nl == null || nl.getLength() < 1)
            return;
         for(int i=0; i<nl.getLength(); i++)
         {
            node = nl.item(i);
            if(!(node instanceof Element))
               continue;

            processItem(itemsRendered, (Element)node, request, rxAppResource);
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
    * Helper function to find out if the actionlist element has a child action.
    * If none of the child items of the actionlist element has non empty actionid
    * then it means the action list has no children.
    * @param actionlist DOM element, will not be <code>null</code>.
    * If <code>null</code> the return value shall be <code>false</code>.
    * @return <code>true</code> if has children, <code>false</code> otherwise.
    */
   private boolean hasActionChildren(Element actionlist)
   {
      boolean res = false;
      if(actionlist == null)
         return res;

      NodeList nl = actionlist.getElementsByTagName("*");
      if(nl == null || nl.getLength() < 1)
         return res;

      Element elem = null;
      for(int i=0; i<nl.getLength(); i++)
      {
         elem = (Element)nl.item(i);
         if(!elem.getAttribute("actionid").equals(""))
         {
            res = true;
            break;
         }
      }
      return res;
   }
   /**
    * Helper function to return true or false based on the number of languages
    * enabled on the system. Returns true if there are more than one language
    * enabled otherwise false.
    * @request <code>IPSRequestContext</code> object
    * @return <code>true</code> for more than one language enabled,
    * <code>false</code> otherwise.
    */
   private boolean addTranslateAction(IPSRequestContext request)
   {
      Document doc = null;
      IPSInternalRequest iReq = null;
      NodeList nl = null;
      try{
            iReq = request.getInternalRequest(LOCALE_REQUEST);
            iReq.makeRequest();
            doc = iReq.getResultDoc();
      }
      catch(Exception e){
         PSConsole.printMsg("Exit:" + ms_fullExtensionName, e);
      }
      finally
      {
         iReq.cleanUp();
      }
      nl = doc.getElementsByTagName(ELEM_LANG);
      return nl!=null && nl.getLength()>1;
   }
   /**
    * The fully qualified name of this extension.
    */
   static private String ms_fullExtensionName = "";

   /**
    * The internal request resource name to get the child menu tree given the
    * parent information
    */
   static private final String REQUEST_NAME = "actionlistchildren";

   /**
    * Name of the Action List element
    */
   static private final String ELEM_ACTION_LIST = "ActionList";

   /**
    * Name of the Action element
    */
   static private final String ELEM_ACTION = "Action";

   /**
    * Name of the attribute/html parameter for actionid
    */
   static private final String ATTR_ACTIONID = "actionid";

   /**
    * Name of the attribute/html parameter for name
    */
   static private final String ATTR_NAME = "name";

   /**
    * Name of the translate action
    */
   static private final String ACTION_TRANSLATE = "Translate";

   /**
    * Name of the "Active Assembly for Documents" action
    */
   static private final String ACTION_DOC_ASSEMBLER = "Item_Assembly";

   /**
    * Name of the attribute/html parameter for modeid
    */
   static private final String ATTR_MODEID = "modeid";

   /**
    * Name of the attribute/html parameter for uicontextid
    */
   static private final String ATTR_UICONTEXTID = "uicontextid";

   /**
    * the internal request to get the locales
    */
   static private final String LOCALE_REQUEST = "sys_i18nSupport/locale";

   /**
    * Name of the language element
    */
   static private final String ELEM_LANG = "lang";

}
