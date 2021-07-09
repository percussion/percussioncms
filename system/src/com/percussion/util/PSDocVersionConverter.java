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

package com.percussion.util;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * The PSDocVersionConverter class converts an old version of a Rhythmyx application's
 * XML document to a new version. The structure and nodes of the XML document will be
 * updated.
 * <p>
 * Users are responsible for validating the file or document to be converted and
 * knowing about the version to be changed to.
 *
 * @author     Jian Huang
 * @version    1.1
 * @since      1.1
 */
public class PSDocVersionConverter
{

   private static final Logger log = LogManager.getLogger(PSDocVersionConverter.class);

   public static void main(String[] args)
   {
      String inFile = args[0];

      try
      {
         PSDocVersionConverter conv = new PSDocVersionConverter("1.1");
         InputStream in = null;
         try
         {
            in = new BufferedInputStream(new FileInputStream(inFile));
            Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
            in.close();
            in = null;

            doc = conv.convertOneZeroToOneOne(doc);
            PSXmlDocumentBuilder.write(doc, System.out);
         }
         finally
         {
            if (in != null)
            {
               try { in.close(); } catch (IOException e) { /* ignore */ }
            }
         }
      }
      catch (Throwable t)
      {
         log.error(t.getMessage());
         log.debug(t.getMessage(), t);
      }
   }

   /**
    * Construct an object of document version converter.
    *
    * @param      newVersion  the new version number, such as "1.1"
    */
   public PSDocVersionConverter(String newVersion)
   {
      if (newVersion == null){
         throw new IllegalArgumentException("PSDocVersionConverter/constructor exception: input version is null");
      }

      boolean versionFound = false;
      for (int i = 0; i < ms_versionArray.length; i++){
         if (newVersion.equals(ms_versionArray[i])){
            m_newVersion = newVersion;
            versionFound = true;
            break;
         }
      }

      if (!versionFound){
         throw new IllegalArgumentException("PSDocVersionConverter/constructor exception: input version not supported");
      }
   }

   /**
    * Construct an object of document version converter. The default new
    * version number ("1.1") is assumed.
    */
   public PSDocVersionConverter()
   {
      super();
   }

   /**
    * Determine whether the document has been converted to a new version or not.
    *
    * @return  <code>true</code> if the version is new; <code>false</code> if the
    *          version is the same
    */
   public boolean isNewVersion()
   {
      return m_isConverted;
   }

   /**
    * Convert version one point zero to version one point one. Only versions lower
    * than one point one will be converted. If the version is one point one or higher,
    * then the file content will be returned immediately without being converted.
    *
    * @param   app   the application file of a version 1.0 XML document (may be null)
    *
    * @exception     Exception    if any exception happened during the conversion
    */
   public void convertOneZeroToOneOne(File app) throws Exception
   {
      if (app == null)
         return;

      FileInputStream input = null;
      FileOutputStream output = null;
      try{
         String fileName = app.getPath();  // must be full-path file name
         input = new FileInputStream(fileName);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(input, false);
         doc = convertOneZeroToOneOne(doc);
         if (!m_isConverted)
            return;
         output = new FileOutputStream(fileName);
         PSXmlDocumentBuilder.write(doc, output);
      } catch (java.io.IOException fe){
         throw new Exception(fe.toString() + " in convertOneZeroToOneOne(File)");
      } catch (SecurityException se){
         throw new Exception(se.toString() + " in convertOneZeroToOneOne(File)");
      } catch (org.xml.sax.SAXException sax){
         throw new Exception(sax.toString() + " in convertOneZeroToOneOne(File)");
      } finally{
         try{
            if (input != null)
               input.close();
         } catch (Exception e){};
         try{
            if (output != null)
               output.close();
         } catch (Exception e){};
      }
   }

   /**
    * Convert version one point zero to version one point one. Only versions lower
    * than 1.1 will be converted. If the version is 1.1 or higher, then the document
    * will be returned immediately without being converted.
    *
    * @param   doc   the input document (may be <code>null</code>)
    *
    * @return        the document of version 1.1 (may be <code>null</code>)
    *
    * @exception  Exception   if doc can not be processed
    */
   public Document convertOneZeroToOneOne(Document doc) throws Exception
   {
      if (doc == null)
         return doc;

      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);

      String appVersion = tree.getElementData("version");
      if (appVersion == null){
         return doc;
      }
      try{
         Float value = Float.valueOf(appVersion);
         float oldVersion = value.floatValue();
         if (oldVersion >= 1.1)
            return doc;
      } catch (NumberFormatException e){  // unknown version
         throw new Exception("The document's version " + appVersion + " is not 1.0");
      }

      Element root = (Element)tree.getCurrent(); // <PSXApplication ...>
      try{
         root.removeAttribute("version");
         root.setAttribute("version", "1.1");
      } catch (Exception e){
         throw new Exception("can not convert to version 1.1");
      }

      Element curNode = null;

      // First, walk to <ApplicationUdfs> and replace Children
      curNode = tree.getNextElement("ApplicationUdfs", ms_firstFlags);
      if (curNode != null){
         // search for <PSXUdfExit> and <PSXJavaExit>
         replaceUdfExitNodeBlocks(doc, curNode, tree);
         tree.setCurrent(curNode);
         replaceJavaExitNodeBlocks(doc, curNode, root, tree);
      }

      tree.setCurrent(root);

      // Second, walk to <ServerUdfReferences> and replace Children
      curNode = tree.getNextElement("ServerUdfReferences", ms_firstFlags);
      if (curNode != null){
         // search for <PSXUdfExit> and <PSXJavaExit>
         replaceUdfExitNodeBlocks(doc, curNode, tree);
         tree.setCurrent(curNode);
         replaceJavaExitNodeBlocks(doc, curNode, root, tree);
      }

      tree.setCurrent(root);

      // Third, walk to <ApplicationExits> and replace Children
      curNode = tree.getNextElement("ApplicationExits", ms_firstFlags);
      if (curNode != null){
         // search for <PSXUdfExit> and <PSXJavaExit>
         replaceUdfExitNodeBlocks(doc, curNode, tree);
         tree.setCurrent(curNode);
         replaceJavaExitNodeBlocks(doc, curNode, root, tree);
      }

      tree.setCurrent(root);

      // Fourth, walk to <ServerExitReferences> and replace Children
      curNode = tree.getNextElement("ServerExitReferences", ms_firstFlags);
      if (curNode != null){
         // search for <PSXUdfExit> and <PSXJavaExit>
         replaceUdfExitNodeBlocks(doc, curNode, tree);
         tree.setCurrent(curNode);
         replaceJavaExitNodeBlocks(doc, curNode, root, tree);
      }

      tree.setCurrent(root);

      // search for <PSXUdfCall>, <PSXExitCall>, <PSXExitCallSet>,
      // <PSXExitParamValue>
      curNode = tree.getNextElement("PSXDataSet", ms_firstFlags);
      while (curNode != null){
         replaceFourSingleNodes(doc, curNode, tree);
         tree.setCurrent(curNode);
         curNode = tree.getNextElement("PSXDataSet", ms_nextFlags);
      }

      m_isConverted = true;

      return doc;
   }

   /**
    * Replace all <PSXJavaExit> node blocks with <PSXJavaExtensionDef> node blocks.
    *
    * @param   doc            the document to be converted
    * @param   parentNode     the parent node of <PSXJavaExit>
    * @param   root           the root node which is <PSXApplication>
    * @param   tree           the XML tree walker
    *
    * @exception  Exception   if replacement procedure has errors
    */
   private void replaceJavaExitNodeBlocks(Document doc, Node parentNode, Element root,
      PSXmlTreeWalker tree) throws Exception
   {
      // search for <PSXJavaExit>
      Element oldNode = tree.getNextElement(ms_javaExit, ms_firstFlags);
      while (oldNode != null){
         Element newNode = replaceOneJavaExitNodeBlock(doc, oldNode, root, tree);
         tree.setCurrent(parentNode);
         parentNode.replaceChild(newNode, oldNode);
         oldNode = tree.getNextElement(ms_javaExit, ms_firstFlags);
      }
   }

   /**
    * Replace one <PSXJavaExit> node block with a <PSXJavaExtensionDef> node block.
    *
    * @param   doc            the document to be converted
    * @param   javaExitNode   the node of <PSXJavaExit>
    * @param   root           the root node, which is <PSXApplication>
    * @param   tree           the XML tree walker
    *
    * @exception  Exception   if replacement procedure has errors
    *
    * @return  the new Element
    */
   private Element replaceOneJavaExitNodeBlock(Document doc, Element javaExitNode,
      Element root, PSXmlTreeWalker tree) throws Exception
   {
      // first, replace all <PSXExitParamDef> nodes
      ArrayList gKidList=replaceNodeTagName(doc, tree, ms_exitParamDef, ms_extParamDef);

      NodeList childList = javaExitNode.getChildNodes(); // save all child nodes
      int listSize = (childList == null) ? 0 : childList.getLength();

      try{
         // create <ParamDefs> and fill in child elements
         Element paramDefNode = doc.createElement("ParamDefs");
         int grandChildSize = (gKidList == null) ? 0 : gKidList.size();
         for (int i = 0; i < grandChildSize; i++){
            Node grandChildNode = (Node)gKidList.get(i);
            paramDefNode.insertBefore(grandChildNode, null);
         }

         // create <description> and <version> without child
         Element descriptionNode = doc.createElement("description");
         Element versionNode = doc.createElement("version");

         // create <PSXExtensionDef> without child
         Element extDefNode = doc.createElement("PSXExtensionDef");
         extDefNode.setAttribute("id", "0");

         // create <HandlerDef> and later fill in child <PSXExtensionHandlerDef>
         Element handlerDefTag = doc.createElement("HandlerDef");
         // create <PSXExtensionHandlerDef> block
         Element handlerDefNode = doc.createElement("PSXExtensionHandlerDef");
         handlerDefNode.setAttribute("id", "0");
         handlerDefNode.setAttribute("scriptable", "no");
         PSXmlDocumentBuilder.addElement(doc, handlerDefNode, "name", "Java");
         String content = "execute Java extension classes";
         PSXmlDocumentBuilder.addElement(doc, handlerDefNode, "description", content);
         // fill in <HandlerDef>
         handlerDefTag.insertBefore(handlerDefNode, null);

         extDefNode.insertBefore(handlerDefTag, null); // add <HandlerDef> as a child

         // Note: Be careful when using childList, which is a NodeList object.
         // Once insertBefore(newNode, refNode) is called, the length of childList
         // will be shrinked (you can try getLength() to take a look). The following
         // loop is safe because the descriptionNode, versionNode, and paramDefNode
         // are not in childList, only nameNode is in childList. Once nameNode is
         // inserted, "break" ends the loop.
         Node nameNode = null;
         Node classNameNode = null;
         String type = null;
         for (int i = 0; i < listSize; i++){
            Node childNode = childList.item(i);
            if (childNode == null)
               continue;
            if ((childNode.getNodeName()).equals("className")){
               classNameNode = childNode;  // save classNameNode for later usage
               continue;
            }
            if ((childNode.getNodeName()).equals("name")){
               nameNode = childNode;  // save nameNode for later usage
               extDefNode.insertBefore(childNode, null);
               type = findType(doc, nameNode, root, tree);
               extDefNode.insertBefore(descriptionNode, null);
               extDefNode.insertBefore(versionNode, null);
               extDefNode.insertBefore(paramDefNode, null);
               break;
            }
         } // end of loop

         Element typeNode = doc.createElement("type");
         Text textNode = doc.createTextNode(type);
         typeNode.appendChild(textNode);
         extDefNode.insertBefore(typeNode, paramDefNode);

         // create <PSXJavaExtensionDef> without child
         Element scriptDefNode = doc.createElement("PSXJavaExtensionDef");
         String idValue = javaExitNode.getAttribute("id");
         scriptDefNode.setAttribute("id", idValue);

         scriptDefNode.insertBefore(extDefNode, null);

         // two "*ExitHandler" have been renamed to "*Extension" in version 1.1
         String cls = PSXmlTreeWalker.getElementData((Element)classNameNode);
         if (cls.equalsIgnoreCase("com.percussion.exit.PSSetEmptyXmlStyleSheetExitHandler")){
            String value = "com.percussion.exit.PSSetEmptyXmlStyleSheetExtension";
            PSXmlDocumentBuilder.addElement(doc, scriptDefNode, "className", value);
         }
         else if (cls.equalsIgnoreCase("com.percussion.exit.PSSetCookieExitHandler")){
            String value = "com.percussion.exit.PSSetCookieExtension";
            PSXmlDocumentBuilder.addElement(doc, scriptDefNode, "className", value);
         }
         else{
            scriptDefNode.insertBefore(classNameNode, null);
         }

         return scriptDefNode;
      } catch (Exception e){
         throw new Exception(e.toString() + " in replaceOneJavaExitNodeBlock method");
      }
   }

   /**
    * Replace all <PSXUdfExit> node blocks with <PSXScriptExtensionDef> node blocks.
    *
    * @param   doc            the document to be converted
    * @param   parentNode     the parent node of <PSXUdfExit>
    * @param   tree           the XML tree walker
    *
    * @exception  Exception   if replacement procedure has errors
    */
   private void replaceUdfExitNodeBlocks(Document doc, Node parentNode,
      PSXmlTreeWalker tree) throws Exception
   {
      Element oldNode = tree.getNextElement(ms_udfExit, ms_firstFlags);
      while (oldNode != null){
         Element newNode = replaceOneUdfExitNodeBlock(doc, oldNode, tree);
         tree.setCurrent(parentNode); // position of parentNode
         parentNode.replaceChild(newNode, oldNode);
         oldNode = tree.getNextElement(ms_udfExit, ms_firstFlags);
      }
   }

   /**
    * Replace one <PSXUdfExit> node block with a <PSXScriptExtensionDef> node block.
    *
    * @param   doc            the document to be converted
    * @param   udfExitNode    the node of <PSXUdfExit>
    * @param   tree           the XML tree walker
    *
    * @exception  Exception   if replacement procedure has errors
    *
    * @return  the new Element
    */
   private Element replaceOneUdfExitNodeBlock(Document doc, Element udfExitNode,
      PSXmlTreeWalker tree) throws Exception
   {
      // first, replace all <PSXExitParamDef> nodes
      ArrayList gKidList=replaceNodeTagName(doc, tree, ms_exitParamDef, ms_extParamDef);

      NodeList childList = udfExitNode.getChildNodes(); // save all child nodes
      int listSize = (childList == null) ? 0 : childList.getLength();

      try{
         // create <ParamDefs> and fill in child elements
         Element paramDefNode = doc.createElement("ParamDefs");
         int grandChildSize = (gKidList == null) ? 0 : gKidList.size();
         for (int i = 0; i < grandChildSize; i++){
            Node grandChildNode = (Node)gKidList.get(i);
            paramDefNode.insertBefore(grandChildNode, null);
         }

         // create <PSXExtensionDef> without child
         Element extDefNode = doc.createElement("PSXExtensionDef");
         extDefNode.setAttribute("id", "0");

         // create <HandlerDef> and fill in <PSXExtensionHandlerDef> as a child
         Element handlerDefTag = doc.createElement("HandlerDef");
         // create <PSXExtensionHandlerDef> block
         Element handlerDefNode = doc.createElement("PSXExtensionHandlerDef");
         handlerDefNode.setAttribute("id", "0");
         handlerDefNode.setAttribute("scriptable", "yes");
         PSXmlDocumentBuilder.addElement(doc, handlerDefNode, "name", "JavaScript");
         String content = "execute JavaScript code";
         PSXmlDocumentBuilder.addElement(doc, handlerDefNode, "description", content);
         // fill in <HandlerDef>
         handlerDefTag.insertBefore(handlerDefNode, null);

         extDefNode.insertBefore(handlerDefTag, null); // add <HandlerDef> as a child

         Node nameNode = null;
         Node bodyNode = null;
         for (int i = 0; i < listSize; i++){
            Node childNode = childList.item(i);
            if (childNode == null)
               continue;
            if ((childNode.getNodeName()).equals("name")){
               nameNode = childNode;  // save nameNode for later usage
               extDefNode.insertBefore(childNode, null);
               continue;
            }
            if ((childNode.getNodeName()).equals("body")){
               bodyNode = childNode;  // save bodyNode for later usage
               continue;
            }
            if ((childNode.getNodeName()).equals("description")){
               extDefNode.insertBefore(childNode, null);
               continue;
            }
            if ((childNode.getNodeName()).equals("version")){
               extDefNode.insertBefore(childNode, null);
               extDefNode.insertBefore(paramDefNode, null);
               break;   // we finish the loop here
            }
         } // end of loop

         Element typeNode = doc.createElement("type");
         Text textNode = doc.createTextNode(ms_type1); // UDF_PROC is type 1
         typeNode.appendChild(textNode);
         extDefNode.insertBefore(typeNode, paramDefNode);

         // create <PSXScriptExtensionDef> without child
         Element scriptDefNode = doc.createElement("PSXScriptExtensionDef");
         String idValue = udfExitNode.getAttribute("id");
         scriptDefNode.setAttribute("id", idValue);

         scriptDefNode.insertBefore(extDefNode, null);
         scriptDefNode.insertBefore(bodyNode, null);

         return scriptDefNode;
      } catch (Exception e){
         throw new Exception(e.toString() + " in replaceOneUdfExitNodeBlock method");
      }
   }

   /**
    * Find the type of the processor based on the given extension name. The <code>origin</code>
    * element should be high enough in the element tree.
    *
    * @param   doc         the XML document
    * @param   nameNode    the node of extension name, such as <name>concat</name>
    * @param   origin      the starting point of the search
    * @param   tree        the XML tree walker
    *
    * @exception Exception if can not find the type node
    *
    * @return  the type string
    */
   private String findType(Document doc, Node nameNode, Element origin, PSXmlTreeWalker tree)
      throws Exception
   {
      tree.setCurrent(origin);

      Node curNode = tree.getNextElement("PSXDataSet", ms_firstFlags);
      while (curNode != null){
         String ret = searchInputAndResultDataExits(doc, nameNode, curNode, tree);
         if ((ret.equals(ms_type2)) || (ret.equals(ms_type4))){
            return ret;
         }
         tree.setCurrent(curNode);
         curNode = tree.getNextElement("PSXDataSet", ms_nextFlags);
      }

      return ms_type1;
   }

   /**
    * Search inside node <code>InputDataExits</code> and node <code>ResultDataExits</code>
    * to find the type of the processor based on the given <code>nameNode</code>.
    *
    * @param   doc         the XML document
    * @param   nameNode    the node of extension name, such as <name>concat</name>
    * @param   origin      the starting point of the search
    * @param   tree        the XML tree walker
    *
    * @exception Exception if can not find the type node
    *
    * @return  the type string
    */
   private String searchInputAndResultDataExits(Document doc, Node nameNode, Node origin,
      PSXmlTreeWalker tree) throws Exception
   {
      String nameValue = PSXmlTreeWalker.getElementData((Element)nameNode);

      Node inputDataExits = tree.getNextElement("InputDataExits", ms_firstFlags);
      if (inputDataExits != null){
         // walk to the first <PSXExitCall>
         Node curNode = tree.getNextElement(ms_exitCall, ms_firstFlags);
         while (curNode != null){
            Node name = tree.getNextElement("name", ms_firstFlags);
            if (name != null){
               String refNameValue = PSXmlTreeWalker.getElementData((Element)name);
               if (nameValue.equals(refNameValue))
                  return ms_type2;
            }
            tree.setCurrent(curNode);
            curNode = tree.getNextElement(ms_exitCall, ms_nextFlags);
         }
      }

      tree.setCurrent(origin);

      Node resultDataExits = tree.getNextElement("ResultDataExits", ms_firstFlags);
      if (resultDataExits != null){
         // walk to the first <PSXExitCall>
         Node curNode = tree.getNextElement(ms_exitCall, ms_firstFlags);
         while (curNode != null){
            Node name = tree.getNextElement("name", ms_firstFlags);
            if (name != null){
               String refNameValue = PSXmlTreeWalker.getElementData((Element)name);
               if (nameValue.equals(refNameValue))
                  return ms_type4;
            }
            tree.setCurrent(curNode);
            curNode = tree.getNextElement(ms_exitCall, ms_nextFlags);
         }
      }

      return ms_type1;
   }

   /**
    * Search and replace nodes <PSXUdfCall>, <PSXExitCall>, <PSXExitCallSet>,
    * and <PSXExitParamValue>.
    *
    * @param   doc         the document to be converted
    * @param   origin      the node as the beginning position to search
    * @param   tree        the XML tree walker
    *
    * @exception  Exception   if replacement procedure has errors
    */
   private void replaceFourSingleNodes(Document doc, Node origin,
      PSXmlTreeWalker tree) throws Exception
   {
      // replace <PSXExitCallSet>
      Element oldNode = tree.getNextElement(ms_exitCallSet, ms_firstFlags);
      while (oldNode != null){
         tree.setCurrent(origin);
         replaceExitCallSet(doc, tree, ms_exitCallSet, ms_extCallSet);
         tree.setCurrent(origin);
         oldNode = tree.getNextElement(ms_exitCallSet, ms_firstFlags);
      }

      // replace <PSXExitCall>
      oldNode = tree.getNextElement(ms_exitCall, ms_firstFlags);
      while (oldNode != null){
         tree.setCurrent(origin);
         replaceUdfOrExitCall(doc, tree, ms_exitCall, ms_extCall);
         tree.setCurrent(origin);
         oldNode = tree.getNextElement(ms_exitCall, ms_firstFlags);
      }

      // replace <PSXUdfCall>
      oldNode = tree.getNextElement(ms_udfCall, ms_firstFlags);
      while (oldNode != null){
         tree.setCurrent(origin);
         replaceUdfOrExitCall(doc, tree, ms_udfCall, ms_extCall);
         tree.setCurrent(origin);
         oldNode = tree.getNextElement(ms_udfCall, ms_firstFlags);
      }

      // replace <PSXExitParamValue>
      oldNode = tree.getNextElement(ms_exitParamValue, ms_firstFlags);
      while (oldNode != null){
         tree.setCurrent(origin);
         replaceNodeTagName(doc, tree, ms_exitParamValue, ms_extParamValue);
         tree.setCurrent(origin);
         oldNode = tree.getNextElement(ms_exitParamValue, ms_firstFlags);
      }
   }

   /**
    * Replace the parent nodes of <PSExitCall> and </PSExitCall>. The parents
    * could be <PSXExitCallSet> and </PSXExitCallSet>, or any other nodes.
    *
    * @param   doc            the document to be updated
    * @param   tree           the XML tree walker
    * @param   oldTagName     the old tag name
    * @param   newTagName     the new tag name
    *
    * @exception Exception    if error occurs during the process
    *
    * @return     a list of new nodes
    */
   private ArrayList replaceExitCallSet(Document doc, PSXmlTreeWalker tree, String oldTagName,
      String newTagName) throws Exception
   {
      Node parentNode = null;

      // replace <PSXExitCallSet> or other
      Element oldNode = tree.getNextElement(oldTagName, ms_firstFlags);
      if (oldNode != null){
         parentNode = oldNode.getParentNode();
         if (parentNode == null){
            String msg = "the parent node of <" + oldTagName + "> is null";
            throw new Exception(msg);
         }
      }

      ArrayList newChildList = new ArrayList();
      ArrayList newNodeList = new ArrayList();
      while (oldNode != null){
         String idValue = oldNode.getAttribute("id");
         try{
            // first, replace <PSXExitCall>
            newChildList = replaceUdfOrExitCall(doc, tree, ms_exitCall, ms_extCall);

            Element newNode = doc.createElement(newTagName);
            newNode.setAttribute("id", idValue);

            int size = (newChildList == null) ? 0 : newChildList.size();
            for (int i = 0; i < size; i++){
               Node newChild = (Node)newChildList.get(i);
               newNode.insertBefore(newChild, null);  // add child into newNode
            }

            parentNode.replaceChild(newNode, oldNode);
            newNodeList.add(newNode);
         } catch (Exception e){
            throw new Exception(e.toString() + " in replaceExitCallSet method");
         }
         tree.setCurrent(parentNode);
         oldNode = tree.getNextElement(oldTagName, ms_firstFlags);
      }

      return newNodeList;
   }

   /**
    * Replace the parent nodes of <PSExitParamValue> and </PSExitParamValue>. The parents
    * could be <PSXUdfCall> and </PSXUdfCall>, or <PSXExitCall> and </PSXExitCall>, or
    * any other nodes.
    *
    * @param   doc            the document to be updated
    * @param   tree           the XML tree walker
    * @param   oldTagName     the old tag name
    * @param   newTagName     the new tag name
    *
    * @exception Exception    if error occurs during the process
    *
    * @return     a list of new nodes
    */
   private ArrayList replaceUdfOrExitCall(Document doc, PSXmlTreeWalker tree,
      String oldTagName, String newTagName) throws Exception
   {
      Node parentNode = null;

      // replace <PSXUdfCall> or <PSXExitCall> or other
      Element oldNode = tree.getNextElement(oldTagName, ms_firstFlags);
      if (oldNode != null){
         parentNode = oldNode.getParentNode();
         if (parentNode == null){
            String msg = "the parent node of <" + oldTagName + "> is null";
            throw new Exception(msg);
         }
      }

      ArrayList newChildList = new ArrayList();
      ArrayList newNodeList = new ArrayList();
      while (oldNode != null){
         String idValue = oldNode.getAttribute("id");

         try{
            // first, replace <PSXExitParamValue>
            newChildList = replaceNodeTagName(doc, tree, ms_exitParamValue, ms_extParamValue);

            // create <PSXExtensionCall> without child
            Element newNode = doc.createElement(newTagName);
            newNode.setAttribute("id", idValue);

            // add <name> as the first child
            Element nameChild = tree.getNextElement(ms_firstFlags);
            newNode.insertBefore(nameChild, null);

            int size = (newChildList == null) ? 0 : newChildList.size();
            for (int i = 0; i < size; i++){
               Node newChild = (Node)newChildList.get(i);
               newNode.insertBefore(newChild, null);  // add child into newNode
            }

            parentNode.replaceChild(newNode, oldNode);
            newNodeList.add(newNode);
         } catch (Exception e){
            throw new Exception(e.toString() + " in replaceUdfOrExitCall method");
         }
         tree.setCurrent(parentNode);
         oldNode = tree.getNextElement(oldTagName, ms_firstFlags);
      }

      return newNodeList;
   }

   /**
    * Replace nodes having the same tag name with a new tag name. This is not a global
    * replacement. Only those nodes having the same parent node will be replaced.
    * <p>
    * This method can not be used to change the XML document's root. That is, at least
    * one ancestor node must exist.
    *
    * @param   doc            the document to be updated
    * @param   tree           the XML tree walker
    * @param   oldTagName     the old tag name
    * @param   newTagName     the new tag name
    *
    * @exception  Exception   if there is no parent node, or error occurs during the process
    *
    * @return     a list of new nodes
    */
   private ArrayList replaceNodeTagName(Document doc, PSXmlTreeWalker tree, String oldTagName,
      String newTagName) throws Exception
   {
      Node parentNode = null;

      // try to find the first old node
      Element oldNode = tree.getNextElement(oldTagName, ms_firstFlags);
      if (oldNode != null){
         parentNode = oldNode.getParentNode();
         if (parentNode == null){
            String msg = "the parent node of <" + oldTagName + "> is null";
            throw new Exception(msg);
         }
      }

      ArrayList newNodeList = new ArrayList();
      while (oldNode != null){
         String idValue = oldNode.getAttribute("id"); // save the old ID
         NodeList childList = oldNode.getChildNodes(); // save all the child nodes
         int listSize = (childList == null) ? 0 : childList.getLength();

         try{
            Element newNode = doc.createElement(newTagName);
            newNode.setAttribute("id", idValue);

            // copy child nodes
            // Note: Be careful when using childList, which is a NodeList object.
            // Once insertBefore(newNode, refNode) is called, the length of childList
            // will be shrinked (you can try getLength() to take a look). The following
            // loop is safe because each time only the first item in the childList
            // is gone (inserted into newNode). Once the loop finishes, all items have
            // been inserted into newNode, and the childList becomes empty.
            for (int i = 0; i < listSize; i++){
               Node childNode = childList.item(0); // Warning: do not change 0 into i
               newNode.insertBefore(childNode, null);  // add child into newNode
            }

            parentNode.replaceChild(newNode, oldNode);
            newNodeList.add(newNode);  // store all the new nodes
         } catch (DOMException e){
            throw new Exception(e.toString() + " in replaceNodeTagName method");
         }
         tree.setCurrent(parentNode); // stop tree walker at the parent node
         oldNode = tree.getNextElement(oldTagName, ms_firstFlags);
      }

      return newNodeList;
   }

   // These are the old nodes to be replaced
   private static final String ms_udfExit = "PSXUdfExit";
   private static final String ms_javaExit = "PSXJavaExit";
   private static final String ms_udfCall = "PSXUdfCall";
   private static final String ms_exitCall = "PSXExitCall";
   private static final String ms_exitCallSet = "PSXExitCallSet";
   private static final String ms_exitParamDef = "PSXExitParamDef";
   private static final String ms_exitParamValue = "PSXExitParamValue";

   // These are the new nodes
   private static final String ms_extCall = "PSXExtensionCall";
   private static final String ms_extCallSet = "PSXExtensionCallSet";
   private static final String ms_extParamDef = "PSXExtensionParamDef";
   private static final String ms_extParamValue = "PSXExtensionParamValue";
   private static final String ms_scriptExtDef = "PSXScriptExtensionDef";

   /* These three types were copied from the original IPSExtensionDef, which
      was removed in v2.0. */
   /**
    * This extension is a user defined function (UDF) processor.
    */
   public static final int EXT_TYPE_UDF_PROC            = 0x01;

   /**
    * This extension is a request pre-processor.
    */
   public static final int EXT_TYPE_REQUEST_PRE_PROC      = 0x02;

   /**
    * This extension is a result document processor.
    */
   public static final int EXT_TYPE_RESULT_DOC_PROC      = 0x04;

   /**
    * Get the handler used to process this type of extension.
    *
    * @return      the handler used to process this type of extension.
    */
   private static final String ms_type1 = String.valueOf(EXT_TYPE_UDF_PROC);
   private static final String ms_type2 = String.valueOf(EXT_TYPE_REQUEST_PRE_PROC);
   private static final String ms_type4 = String.valueOf(EXT_TYPE_RESULT_DOC_PROC);

   /** Stores the supported version numbers. */
   private static final String[] ms_versionArray = {"1.1"};

   private static final int ms_firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
   private static final int ms_nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

   /** The new version number. The default has been set as "1.1". */
   private String m_newVersion = "1.1";

   /** The document is converted. */
   private boolean m_isConverted = false;
}
