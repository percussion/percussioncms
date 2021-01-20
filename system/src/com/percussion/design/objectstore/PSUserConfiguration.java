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

package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Enumeration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSUserConfiguration class is used to store and retrieve user
 * configuration information in the E2 object store. This allows users,
 * primarily designers, to store preferences and other such information
 * on the server. They can then use any machine and still have all their
 * customizations.
 * <p>
 * Use the PSObjectStore class to load a PSUserConfiguration object from
 * an E2 server (getUserConfiguration) or to save changes back to the server
 * (saveUserConfiguration).
 * <p>
 * <em>Note:</em> Use of this object as a hash table is strictly
 * discouraged. The new getPropertyTree and setPropertyTree methods
 * should be used instead to take advantage of the more rich 
 * structure of XML documents.
 *
 * @see         PSObjectStore
 * @see         PSObjectStore#getUserConfiguration
 * @see         PSObjectStore#saveUserConfiguration
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSUserConfiguration extends java.util.Hashtable
   implements IPSDocument
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml() toXml} method for a description of the XML object.
    *
    * @param      sourceDoc      the XML document to construct this
    *                              object from
    *
    * @exception   PSUnknownDocTypeException
    *                              if the XML document is not of the
    *                              appropriate type
    *
    * @exception   PSUnknownNodeTypeException
    *                              if an XML element node is not of the
    *                              appropriate type
    */
   public PSUserConfiguration(org.w3c.dom.Document sourceDoc)
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceDoc);
   }

   /**
    * Constructs an empty set of user configuration options.
    *
    * @param   userName    the name of the user for which the configuration
    *                      information is being defined
    */
   PSUserConfiguration(java.lang.String userName)
   {
      super();
      m_userName = userName;
   }
   
   /**
    * For fromXml use only.
    */
   PSUserConfiguration()
   {
      super();
   }
   
   /**
    * Returns the user name for whom this configuration applies.
    *
    * @author   chadloder
    * 
    * @version 1.10 1999/05/07
    * 
    * @return   String
    */
   public String getUserName()
   {
      return m_userName;
   }

   /**
    * Get the properties associated with this user. By specifying properties,
    * a designer can store context information, etc. on the Rhythmyx server.
    * The user defined XML tree structure is stored in the object store,
    * allowing the client to access their personal configuration information
    * regardless of the client machine they're using.
    *
    * @return              the user defined properties associated with this
    *                     user
    *
    * @see               #setPropertyTree
    */
   public org.w3c.dom.Document getPropertyTree()
   {
      return m_propertyTree;
   }
   
   /**
    * Overwrite the properties associated with this user with the specified
    * object. If you only want to modify some of the properties, add new
    * properies, etc. use getPropertyTree to get the existing object and
    * modify the returned object directly.
    * <p>
    * The Document object supplied to this method will be stored with the
    * PSUserConfiguration object. Any subsequent changes made to the object
    * by the caller will also effect the user configuration.
    *
    * @param   props      the new properties to associate with this user
    *
    * @see               #getPropertyTree
    */
   public void setPropertyTree(org.w3c.dom.Document propTree)
   {
      m_propertyTree = propTree;
   }


   /* *************** IPSDocument Interface Implementation *************** */
   
   /**
    * This method is called to create a PSXUserConfiguration XML document
    * containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXUserConfiguration is used to store and retrieve user
    *       configuration information in the E2 object store. This allows
    *       users, primarily designers, to store preferences and other such
    *       information on the server. They can then use any machine and
    *       still have all their customizations.
    *    --&gt;
    *    &lt;!ELEMENT   PSXUserConfiguration (option*)&gt;
    *
    *    &lt;!--
    *       the name of the user for which the configuration options
    *       have been defined. This is used as a key in the object store
    *       when storing the options. If the name already exists, any
    *       existing options will be overwritten.
    *    --&gt;
    *    &lt;!ATTLIST   PSXUserConfiguration
    *       name         ID                   #REQUIRED
    *    &gt;
    *
    *    &lt;!--
    *       a user configuration option. Options are stored in the object
    *       store as key/value pairs, guaranteeing their existence
    *       between uses.
    *
    *       the option's value should be stored as the value of this element
    *    --&gt;
    *    &lt;!ELEMENT   option               (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the name (key) associated with this option
    *    --&gt;
    *    &lt;!ATTLIST   option
    *       name         ID                   #REQUIRED
    *    &gt;
    *
    *    &lt;!--
    *       the user-defined property tree. This will contain an XML
    *         tree where the "root" node of the user-defined tree is the
    *       child of this node.
    *    --&gt;
    *    &lt;!ELEMENT PropertyTree           (#PCDATA)&gt;
    * </code></pre>
    *
    * @return     the newly PSXUserConfiguration XML document
    */
   public Document toXml()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      
      // create the object and add its name attribute
      Element root = PSXmlDocumentBuilder.createRoot(doc, ms_NodeType);
      root.setAttribute("name", m_userName);

      //store the options
      Element opt;
      for (Enumeration e = keys() ; e.hasMoreElements() ;)
      {
         String key = (String)e.nextElement();
         Object value = get(key);
         if (value instanceof java.lang.String)
            opt = PSXmlDocumentBuilder.addElement(   doc, root, "option",
            (String)value);
         else
            opt = PSXmlDocumentBuilder.addElement(   doc, root, "option",
            value.toString());
         opt.setAttribute("name", key);
      }
      
      // if a property tree was specified, copy it over...
      Element propRoot
         = (m_propertyTree == null) ? null : m_propertyTree.getDocumentElement();
      if (propRoot != null)
      {
         // in one op, we can copy the tree as a child of our tree root
         PSXmlDocumentBuilder.copyTree(
            doc,
            PSXmlDocumentBuilder.addEmptyElement(doc, root, "PropertyTree"),
            propRoot);
      }

      return doc;
   }
   
   /**
    * This method is called to populate a PSUserConfiguration Java object
    * from a PSXUserConfiguration XML document. See the
    * {@link #toXml() toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownDocTypeException   if the XML document is not
    *                                        of type PSXUserConfiguration
    */
   public void fromXml(Document sourceDoc)
      throws PSUnknownDocTypeException
   {
      if (sourceDoc == null)
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      Element root = sourceDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      if (false == ms_NodeType.equals (root.getNodeName()))
      {
         Object[] args = { ms_NodeType, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
   
      //Read PSXUserConfiguration object attributes
      m_userName = root.getAttribute ("name");
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceDoc);
      
      //remove all the properties in the table now
      clear();

      final String curNodeType = "option";
      final int firstFlags
         = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      final int nextFlags
         = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      for (   Element curNode = tree.getNextElement(curNodeType, firstFlags);
            curNode != null;
            curNode = tree.getNextElement(curNodeType, nextFlags))
      {
         String sKey = tree.getElementData("name", false);
         String sValue = tree.getElementData("option", false);
         if (sValue == null)   // put doesn't like null
            sValue = "";

         put(sKey, sValue);
      }

      // if a property tree was specified, copy it over...
      m_propertyTree = null;   // reset it first
      Element propRoot = tree.getNextElement("PropertyTree", nextFlags);
      if (propRoot != null)
      {
         // our one and only child contains the property tree, so get it
         propRoot = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (propRoot != null)
         {
            m_propertyTree = PSXmlDocumentBuilder.createXmlDocument();

            // in one op, we can copy the tree as a child of our doc
            // (which creates the root and all the children)
            PSXmlDocumentBuilder.copyTree(
               m_propertyTree, m_propertyTree, propRoot);
         }
      }
   }
   
   
   /**
    * the name of the user this object refers to
    *
    * @serial
    */
   private java.lang.String   m_userName = "";

   private   Document            m_propertyTree = null;
   
   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXUserConfiguration";
}

