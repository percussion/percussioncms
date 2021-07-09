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

package com.percussion.filetracker;

import java.util.HashMap;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;



/**
 * This class encapsulates the document merge process. When user loads the 
 * remote document if a local snapshot exists for this document the merging is 
 * performed as specified in the functional specification. The merge process 
 * is what marks nodes with proper status values. This has only one public 
 * method merge() (besides a static helper method). All other methods are used 
 * in the merging process within the class.
 */

public class PSFUDDocMerger
{
   /**
    * Constructor taking snapshot and remote documents. Just makes sure none
    * of these is null and stores references to these as local varaibles.
    *
    * @param snapshot document as DPM Document
    *
    * @param remot document as DOM Document.
    *
    * @throws PSFUDNullDocumentsException if any of the documents is null.
    *
    */
   public PSFUDDocMerger(Document snapshotDoc, Document remoteDoc)
      throws PSFUDNullDocumentsException
   {
      if(null == snapshotDoc)
         throw new PSFUDNullDocumentsException("Snapshot document cannot be null");

      if(null == remoteDoc)
         throw new PSFUDNullDocumentsException("Remote document cannot be null");

      m_snapshotDoc = snapshotDoc;
      m_remoteDoc = remoteDoc;
   }

   /**
    * Only public (not-static) method. The caller object calls this method after
    * constructing object of this class.
    *
    * @param app the PSFUDApplication object. This object is required for
    * cleaning the tree after merging the remote and local content item list
    * (snapshot) documents.If <code>null</code> method returns without cleaning.
    *
    * @throws PSFUDMergeDocumentsException when unrecoverable error occurs
    *         during merge process.
    */
   public void merge(PSFUDApplication app)
      throws PSFUDMergeDocumentsException
   {
      Element oldElem = m_snapshotDoc.getDocumentElement();
      Element newElem = m_remoteDoc.getDocumentElement();

      if(null == oldElem)
      {
         throw new PSFUDMergeDocumentsException(MainFrame.getRes().
            getString("errorRootElementNullSnapShot"));
      }
      if(null == newElem)
      {
         throw new PSFUDMergeDocumentsException(MainFrame.getRes().
            getString("errorRootElementNullRemote"));
      }

      if(!oldElem.getTagName().equals(newElem.getTagName()))
      {
         throw new PSFUDMergeDocumentsException();
      }

      merge(oldElem, newElem);

      if(null == app)
      {
         throw new PSFUDMergeDocumentsException("errorNullApplication");
      }

      try
      {
         PSFUDAppNode appNode = new PSFUDAppNode(app);
         cleanup(appNode);
         appNode = null;
      }
      catch(PSFUDNullElementException e) //Can happen only if the snapshot
                                         //document is empty
      {
         System.out.println("Snapshot cleanup failed: " + e.getMessage());
      }
   }

   /**
    * This method cleans the an element in the snapshot document. Cleaning in
    * general means, if a FUDNode has no childs at all and a remote version
    * does not exist that node shall be deleted from the document. In case of
    * a file FUD node, the node will be deleted only if there is no local copy
    * exists.
    *
    * @param current FUD Node to be cleaned, can be
    * <code>null</code>, in which case method simply returns <code>false</code>.
    *
    * @return <code>true</code> if the current is deleted from the tree,
    * <code>flase</code> otherwise.
    *
    */
   private boolean cleanup(IPSFUDNode current)
   {
      if(null == current)
         return false;

      if(current instanceof PSFUDFileNode)
      {
         PSFUDFileNode fileNode = (PSFUDFileNode)current;
         if(!fileNode.isRemoteExists() && !fileNode.isLocalCopy())
         {
            current.getElement().getParentNode().
               removeChild(current.getElement());
            return true;
         }
      }
      else
      {
         Object[] childs = current.getChildren();
         if(null == childs)
            return false;

         int count = childs.length;
         int removed = 0;
         for(int i=0; i<count; i++)
         {
            if(cleanup((IPSFUDNode)childs[i]))
               removed++;
            childs[i] = null;
         }
         if(removed == count &&
            !current.isRemoteExists() &&
            !(current instanceof PSFUDAppNode))
         {
            Node parent = current.getElement().getParentNode();
            if(null != parent)
            {
               parent.removeChild(current.getElement());
               return true;
            }
         }
      }
      return false;
   }

   /**
    * The private version of the merge() method.
    *
    * @param elemOld element in the snapshot document as DOM Element,
    *        never <code>null</code>
    *
    * @param elemNew element in the remote document as DOM Element,
    *        never <code>null</code>
    *
    * @throws PSFUDMergeDocumentsException when unrecoverable error occurs
    *         during merge process
    */
   private void merge(Element elemOld, Element elemNew)
      throws PSFUDMergeDocumentsException
   {
      if(null == elemOld) //never happens
      {
         throw new PSFUDMergeDocumentsException(MainFrame.getRes().
            getString("errorElementNullSnapshot"));
      }
      if(null == elemNew) //never happens
      {
         throw new PSFUDMergeDocumentsException(MainFrame.getRes().
            getString("errorElementNullRemote"));
      }

      String elemTag = elemOld.getTagName();

      //never happens!!!
      if(!elemTag.equals(elemNew.getTagName()))
      {
         throw new PSFUDMergeDocumentsException(MainFrame.getRes().
            getString("errorElementTagsConfilct"));
      }

      HashMap mapOldElem = null;
      HashMap mapNewElem = null;


      setStatus(elemOld, IPSFUDNode.STATUS_CODE_NORMAL, "");
      // copy all attributes from new element to element
      copyAttributes(elemOld, elemNew);

      if(elemTag.equals(PSFUDApplication.ELEM_APPLICATION))
      {
         mapOldElem = getMap(elemOld, PSFUDApplication.ELEM_CATEGORY,
                        PSFUDApplication.ATTRIB_CATEGORYID);
         mapNewElem = getMap(elemNew, PSFUDApplication.ELEM_CATEGORY,
                        PSFUDApplication.ATTRIB_CATEGORYID);
         merge(mapOldElem, mapNewElem, elemOld);
      }
      else if(elemTag.equals(PSFUDApplication.ELEM_CATEGORY))
      {
         mapOldElem = getMap(elemOld, PSFUDApplication.ELEM_CONTENTITEM,
                        PSFUDApplication.ATTRIB_CONTENTID);
         mapNewElem = getMap(elemNew, PSFUDApplication.ELEM_CONTENTITEM,
                        PSFUDApplication.ATTRIB_CONTENTID);
         merge(mapOldElem, mapNewElem, elemOld);
      }
      else if(elemTag.equals(PSFUDApplication.ELEM_CONTENTITEM))
      {
         mapOldElem = getMap(elemOld, PSFUDApplication.ELEM_FILE,
                        PSFUDApplication.ATTRIB_NAME);
         mapNewElem = getMap(elemNew, PSFUDApplication.ELEM_FILE,
                        PSFUDApplication.ATTRIB_NAME);
         merge(mapOldElem, mapNewElem, elemOld);
      }
      else if(elemTag.equals(PSFUDApplication.ELEM_FILE))
      {
         mergeFileElements(elemOld, elemNew);
      }
      else
      {
         System.out.println("Merge for '" + elemTag + "' is not implemented!");
      }
      //help garbage collector!
      mapOldElem = null;
      //help garbage collector!
      mapNewElem = null;
   }

   /**
    * This method extracts a map of child elements and key attributes for a given
    * parent element.
    *
    * @param parent element as DOM Element, not <code>null</code>
    *
    * @param child element name as String, not <code>null</code> or empty
    *
    * @param key attribute as String, not <code>null</code> or empty
    *
    * @return map of child element-key pairs, never <code>null</code> but may
    *         be empty
    *
    */
   private HashMap getMap(Element elemParent, String child, String attrib)
   {
      HashMap map = new HashMap();

      if(null == elemParent     ||
         null == child          ||
         null == attrib          ||
         child.length() < 1      ||
         attrib.length() < 1  )
         return map;

      NodeList nl = elemParent.getElementsByTagName(child);
      String id = "";
      Element elem = null;
      for(int i=0; (nl != null && i<nl.getLength()); i++)
      {
         elem = (Element)nl.item(i);
         id = elem.getAttribute(attrib);
         if(null != id && id.trim().length() > 0)
            map.put(id, elem);
      }
      return map;
   }

   /**
    * Yet another version of merg() method.
    *
    * @param map of Old element-key pairs as HashMap, not <code>null</code>
    *
    * @param map of new element-key pairs as HashMap, not <code>null</code>
    *
    * @param old parent element as DOM Element. This is used to set the status
    *        code. Not <code>null</code>
    *
    * @throws PSFUDMergeDocumentsException when unrecoverable error occurs
    *         during merge process
    */
   private void merge(HashMap mapOldElem, HashMap mapNewElem, Element oldParent)
      throws PSFUDMergeDocumentsException
   {
      if(null == mapOldElem ||
         null == mapNewElem ||
         null == oldParent)  //never happens
      {
         return;
      }
      //both maps' size is zero, remove the unncessary element oldParent except
      //for application element
      if(mapOldElem.size() < 1 && mapNewElem.size() < 1
         && !oldParent.getTagName().equals(PSFUDApplication.ELEM_APPLICATION))
      {
         oldParent.getParentNode().removeChild(oldParent);
         return;
      }

      Object[] keys = mapNewElem.keySet().toArray();
      Object key = null;
      Element elem = null;
      //see if every element in the remote (new) document exists in the local
      // (snapshot) document.
      for(int i=0; i<keys.length; i++)
      {
         key = keys[i];
         //if exists merge them
         if(mapOldElem.containsKey(key))
         {
            merge((Element)mapOldElem.get(key), (Element)mapNewElem.get(key));
         }
         else //otherwise append to the snapshot
         {
            elem = (Element)mapNewElem.get(key);
            oldParent.appendChild(elem.cloneNode(true));
         }
      }

      //see if every element in the local (snapshot) document exists in the
      //remote (new) document
      keys = mapOldElem.keySet().toArray();
      for(int i=0; i<keys.length; i++)
      {
         key = keys[i];
         elem = (Element)mapOldElem.get(key);
         //if does not exist mark it as missing element
         if(!mapNewElem.containsKey(key))
            setStatus(elem, IPSFUDNode.STATUS_CODE_ABSENT, "Remote missing");
      }
   }

   /**
    * Any additional work beyond copying attributes for file elements
    *
    * @param elemOld element in the snapshot document as DOM Element
    *
    * @param elemNew element in the remote document as DOM Element
    *
    */
   private void mergeFileElements(Element oldElem, Element newElem)
   {
      if(null == oldElem || null == newElem)
         return;

      Element elemOld = getChildElement(oldElem,
         PSFUDApplication.ELEM_DOWNLOADURL);

      Element elemNew = getChildElement(newElem,
         PSFUDApplication.ELEM_DOWNLOADURL);

      if(null != elemNew)
      {
         if(null != elemOld)
            oldElem.replaceChild(elemNew.cloneNode(true), elemOld);
         else
            oldElem.appendChild(elemNew.cloneNode(true));
      }
   }

   /**
    * Helper method to copy all attributes from new element to old one.
    *
    * @param elemOld element in the snapshot document as DOM Element,
    *        never <code>null</code>
    *
    * @param elemNew element in the remote document as DOM Element,
    *        never <code>null</code>
    *
    */
   private void copyAttributes(Element oldElem, Element newElem)
   {
      if(null == oldElem || null == newElem)
         return;
      NamedNodeMap map = newElem.getAttributes();
      if(null == map)
         return;

      Attr attr = null;
      for(int i=0; i<map.getLength(); i++)
      {
         attr = (Attr)map.item(i);
         oldElem.setAttribute(attr.getName(), attr.getValue());
      }
   }

   /**
    * Helper function to set status code and text for a node element
    */
   private void setStatus(Element parent, int nCode, String msg)
   {
      if(null == parent)
         return;
      if(null == msg)
         msg = "";
      String code = "";
      try
      {
         code = Integer.toString(nCode);
      }
      catch(NumberFormatException e)
      {
      }
      Element status = PSFUDDocMerger.createChildElement(parent,
                              IPSFUDNode.ELEM_STATUS);

      status.setAttribute(IPSFUDNode.ATTRIB_CODE, code);
      Node node = status.getFirstChild();
      if(null == node || Node.TEXT_NODE != node.getNodeType())
      {
         node = status.getOwnerDocument().createTextNode(msg);
         status.appendChild(node);
      }
      else
      {
         ((Text)node).setData(msg);
      }
   }
   /**
    * A generic method to get the status child element of an element
    *
    * @param the parent node as Element
    *
    * @return the status element if exists, null otherwise.
    *
    */
   static public Element getChildElement(Element parent, String elemName)
   {
      Element elem = null;

      if(null == parent || null == elemName)
         return elem;

      NodeList nl = parent.getChildNodes();
      if(null == nl || nl.getLength() < 1)
         return elem;

      Node node = null;
      for(int i=0; i<nl.getLength(); i++)
      {
         node = nl.item(i);
         if(Node.ELEMENT_NODE !=  node.getNodeType())
            continue;
         if(((Element)node).getTagName().equals(elemName))
         {
            elem = (Element)node;
            break;
         }
      }
      return elem;
   }

   /**
    * Creates a child element if does not already exist. This is a static
    * helper method to append a child element.
    *
    * @param parent as Element. Never <code>null</code>.
    *
    * @param elemName name of the child element to be appended.
    *        Never <code>null</code>.
    *
    * @return new element as Element, <code>null</code> if parent iis
    *         <code>null</code>
    *
    */
   public static Element createChildElement(Element parent, String elemName)
   {
      if(null == parent || null == elemName)
         return null;

      Element elem = getChildElement(parent, elemName);
      if(null != elem)
         return elem;

      elem = (Element)parent.getOwnerDocument()
                           .createElement(elemName);
      return (Element)parent.appendChild(elem);
   }

   //Reference to snapshot (Local) document
   private Document m_snapshotDoc = null;

   //Reference to the remote Document
   private Document m_remoteDoc = null;

}
