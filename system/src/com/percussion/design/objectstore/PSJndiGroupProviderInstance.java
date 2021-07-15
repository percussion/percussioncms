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

package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * Used to define a source of group information for the JNDI directory security
 * provider to use.  Specifies information required to catalog groups and to
 * determine group membership.
 */
public class PSJndiGroupProviderInstance extends PSGroupProviderInstance
{
   /**
    * Parameterless constructor for this class.  Used for serialization.
    * Should always call <code>fromXml</code> following use of this constructor.
    */
   protected PSJndiGroupProviderInstance()
   {
   }

   /**
    * Constructs an instance of this class from its member values.
    *
    * @param name The name of this provider, may not be <code>null</code> or
    * empty.
    * @param type The one of the <code>PSSecurityProvider.SP_TYPE_xxx</code>
    * types.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSJndiGroupProviderInstance(String name, int type)
   {
      super(name, type, PSJndiGroupProviderInstance.class.getName());
   }


   /**
    * Called by base class to serialize this provider.
    * Creates an element with this provider's state.
    * <p>
    * The structure of the xml element is:
    * <pre><code>
    *  &lt;!--
    *        PSXJnidGroupProviderInstance defines a group provider used
    *        to locate groups and determine group membership.
    *  --&gt;
    *  &lt;!ELEMENT PSXJndiGroupProviderInstance  (objectClasses, groupNodes)
    *  &gt;
    *
    *  &lt;!--
    *       A list of object classes supported.
    *  --&gt;
    *  &lt;!ELEMENT objectClasses  (objectClass*)&gt;
    *
    *  &lt;!--
    *       An object class that is supported.
    *  --&gt;
    *  &lt;!ELEMENT objectClass  (#PCDATA)&gt;
    *
    *  &lt;!--
    *  Attributes associated with an objectClass:
    *
    *  name - The name of the objectClass.
    *  memberAttr - The name of the attribute the member list of this
    *  objectclass is stored in.
    *  type - Does the value of the member attribute specify a static or dynamic
    *     list?
    *  --&gt;
    *  &lt;!ATTLIST objectClass
    *        name       CDATA       #REQUIRED
    *        memberattr CDATA       #REQUIRED
    *        type      (static | dynamic) "static"
    *  &gt;
    *
    *  &lt;!--
    *       A list of group node entries.
    *  --&gt;
    *  &lt;!ELEMENT groupNodes  (groupNode*)&gt;
    *
    *  &lt;!--
    *       A location in a directory that may contain group entries.
    *       must be the distinguished name of a node in this provider's
    *       directory server.
    *  --&gt;
    *  &lt;!ELEMENT groupNode  (#PCDATA)&gt;
    *
    * </code></pre>
    *
    *
    * @param doc The document to use when creating the element containing the
    * state of this class.  May not be <code>null</code>.
    *
    * @return The element containing the state.  Never
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   protected Element toXmlEx(Document doc)
   {
      // create the root
      Element root = doc.createElement(XML_NODE_NAME);

      // add objectClasses
      Element objectClasses = PSXmlDocumentBuilder.addEmptyElement(doc, root,
         OBJECT_CLASSES_ELEMENT);
      Iterator ocs = m_objectClasses.iterator();
      while (ocs.hasNext())
      {
         PSJndiObjectClass oc = (PSJndiObjectClass)ocs.next();
         Element objectClass = PSXmlDocumentBuilder.addEmptyElement(doc,
            objectClasses, OBJECT_CLASS_ELEMENT);
         objectClass.setAttribute(NAME_ATTR, oc.getObjectClassName());
         objectClass.setAttribute(MEMBER_ATTR, oc.getMemberAttribute());
         objectClass.setAttribute(TYPE_ATTR,
            PSJndiObjectClass.MEMBER_ATTR_TYPE_ENUM[oc.getAttributeType()]);
      }

      // add groupNodes
      Element groupNodes = PSXmlDocumentBuilder.addEmptyElement(doc, root,
         GROUP_NODES_ELEMENT);
      Iterator nodes = m_groupNodes.iterator();
      while (nodes.hasNext())
      {
         String node = (String)nodes.next();
         PSXmlDocumentBuilder.addElement(doc, groupNodes, GROUP_NODE_ELEMENT,
            node);
      }

      return root;

   }

   /**
    * @see #toXmlEx(Document)
    * @see PSGroupProviderInstance#fromXmlEx(Element)
    */
   protected void fromXmlEx(Element source) throws PSUnknownNodeTypeException
   {
      // get root of this class's Xml state
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      PSXmlTreeWalker tree = new PSXmlTreeWalker(source);
      Element root = tree.getNextElement(XML_NODE_NAME, firstFlags);
      if (root == null)
      {
         String parentName = source.getNodeName();
         Object[] args = {  parentName, XML_NODE_NAME, "null" };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      // load object classes
      clearObjectClasses();
      Element objectClasses = tree.getNextElement(OBJECT_CLASSES_ELEMENT,
         firstFlags);
      if (objectClasses == null)
      {
         Object[] args = {  XML_NODE_NAME, OBJECT_CLASSES_ELEMENT, "null" };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      Element objectClass = tree.getNextElement(OBJECT_CLASS_ELEMENT,
         firstFlags);
      while (objectClass != null)
      {
         String name = objectClass.getAttribute(NAME_ATTR);
         if (name == null || name.trim().length() == 0)
         {
            Object[] args = {OBJECT_CLASS_ELEMENT, NAME_ATTR, "null"};
            throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         String memberAttr = objectClass.getAttribute(MEMBER_ATTR);
         if (memberAttr == null || memberAttr.trim().length() == 0)
         {
            Object[] args = {OBJECT_CLASS_ELEMENT, MEMBER_ATTR, "null"};
            throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         int type = -1;
         String attrType = objectClass.getAttribute(TYPE_ATTR);
         if (attrType == null || attrType.trim().length() == 0)
            type = 0;
         else
         {
            for (int i = 0; i < PSJndiObjectClass.MEMBER_ATTR_TYPE_ENUM.length; i++)
            {
               if (attrType.equals(PSJndiObjectClass.MEMBER_ATTR_TYPE_ENUM[i]))
               {
                  type = i;
               }
            }

            if (type == -1)
            {
               Object[] args = {OBJECT_CLASS_ELEMENT, MEMBER_ATTR, attrType};
               throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }

         addObjectClass(name, memberAttr, type);
         objectClass = tree.getNextElement(OBJECT_CLASS_ELEMENT, nextFlags);
      }

      // load group nodes
      clearGroupNodes();
      tree.setCurrent(objectClasses);
      Element groupNodes = tree.getNextElement(GROUP_NODES_ELEMENT, nextFlags);
      if (groupNodes == null)
      {
         Object[] args = {  XML_NODE_NAME, GROUP_NODES_ELEMENT, "null" };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      Element groupNode = tree.getNextElement(GROUP_NODE_ELEMENT, firstFlags);
      while (groupNode != null)
      {
         String node = tree.getElementData(groupNode);
         if (node.trim().length() == 0)
         {
            Object[] args = {  GROUP_NODES_ELEMENT, GROUP_NODE_ELEMENT, node };
            throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         m_groupNodes.add(node);
         groupNode = tree.getNextElement(GROUP_NODE_ELEMENT, nextFlags);
      }
   }


   /**
    * Gets a list of group locations to search.
    *
    * @return An iterator over zero or more group locations. Each location is a
    * String representation of the distinguished name of the node to search,
    * never <code>null</code>.
    */
   public Iterator getGroupNodes()
   {
      return m_groupNodes.iterator();
   }

   /**
    * Gets a list of supported group objectclasses. Modifying the returned list
    * does not modify this instance's objectclasses.
    *
    * @return An iterator over zero or more <code>PSJndiObjectClass</code>
    * objects, never <code>null</code>.
    */
   public Iterator getObjectClasses()
   {
      List classes = new ArrayList(m_objectClasses.size());

      Iterator ocs = m_objectClasses.iterator();
      while (ocs.hasNext())
         classes.add(ocs.next());

      return classes.iterator();
   }

   /**
   * Gets a list of supported group objectclasses.
   *
   * @return An iterator over zero or more objectclass names as Strings, never
   * <code>null</code>.
   */
   public Iterator getObjectClassesNames()
   {
      // need to return names in list order, so build list of names
      List classNames = new ArrayList(m_objectClasses.size());
      Iterator ocs = m_objectClasses.iterator();
      while (ocs.hasNext())
      {
         PSJndiObjectClass oc = (PSJndiObjectClass)ocs.next();
         classNames.add(oc.getObjectClassName());
      }


      return classNames.iterator();
   }

   /**
    * Gets the name of the attribute in which this objectClass' members are
    * stored.
    *
    * @param objectClass The name of the objectClass, may not be
    * <code>null</code> or empty, must be an objectClass supported by this
    * provider.  Use {@link #getObjectClasses()} to get a list of supported
    * objectClass names.  Comparison is case insensitive.
    *
    * @return The Attribute, never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if objectClass is
    * <code>null</code>, empty, or not supported.
    */
   public String getMemberAttribute(String objectClass)
   {
      if (objectClass == null)
         throw new IllegalArgumentException("objectClass may not be null");

      PSJndiObjectClass oc = (PSJndiObjectClass)m_objectClassMap.get(
         objectClass.toLowerCase());

      if (oc == null)
         throw new IllegalArgumentException("objectClass not supported");

      return oc.getMemberAttribute();
   }

   /**
    * Gets the type of the attribute in which this objectClass' members are
    * stored.
    *
    * @param objectClass The name of the objectClass, may not be
    * <code>null</code> or empty, must be an objectClass supported by this
    * provider.  Use {@link #getObjectClasses()} to get a list of supported
    * objectClass names.  Comparison is case insensitive.
    *
    * @return The type, one of the MEMBER_ATTR_xxx types.
    *
    * @throws IllegalArgumentException if objectClass is
    * <code>null</code>, empty, or not supported.
    */
   public int getMemberAttributeType(String objectClass)
   {
      if (objectClass == null)
         throw new IllegalArgumentException("objectClass may not be null");

      PSJndiObjectClass oc = (PSJndiObjectClass)m_objectClassMap.get(
         objectClass.toLowerCase());

      if (oc == null)
         throw new IllegalArgumentException("objectClass not supported");

      return oc.getAttributeType();
   }

   /**
    * Removes all current informations regarding objectclasses supported and
    * their member attributes and types.  see {@link #addObjectClass(String,
    * String, int)} for more info.
    */
   public void clearObjectClasses()
   {
      m_objectClasses.clear();
      m_objectClassMap.clear();
   }


   /**
    * Adds info regarding a supported objectclass.  Defines a group objectClass
    * to support, its member attribute name, and whether or not that attribute
    * contains a static list of names, or a dynamic search filter used to
    * determine membership.
    *
    * @param objectClass The name of the object class.  May not be
    * <code>null</code> or empty.
    * @param memberAttribute The name of the attribute in which this objectClass
    * stores its member list.  May not be <code>null</code> or empty.
    * @param attributeType The type of memberlist stored in the memberAttribute.
    * Must be one of the <code>MEMBER_ATTR_xxx</code> constant values.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public void addObjectClass(String objectClass, String memberAttribute,
      int attributeType)
   {
      // this will validate the params for us
      PSJndiObjectClass oc = new PSJndiObjectClass(objectClass,
         memberAttribute, attributeType);
      m_objectClasses.add(oc);
      m_objectClassMap.put(oc.getObjectClassName().toLowerCase(), oc);
   }


   /**
    * Clears all group node entires.  See {@link #addGroupNode(String)} for more
    * information.
    */
   public void clearGroupNodes()
   {
      m_groupNodes.clear();
   }


   /**
    * Adds a group node to the list of group locations to search.  See
    * {@link #getGroupNodes()} for more info.
    *
    * @param groupNode The distinguished name of a node in the directory that
    * may contain group objects.  May not be <code>null</code> or empty.
    * Assumed to be a valid url for the directory this provider is using.
    *
    * @throws IllegalArgumentException if groupNode is <code>null</code> or
    * empty.
    */
   public void addGroupNode(String groupNode)
   {
      if (groupNode == null || groupNode.trim().length() == 0)
         throw new IllegalArgumentException(
            "groupNode may not be null or empty");

      m_groupNodes.add(groupNode);
   }


   /**
    * compares this instance to another object.
    *
    * @param obj the object to compare
    * @return returns <code>true</code> if the object is a
    * PSJndiGroupProviderInstance with identical values. Otherwise returns
    * <code>false</code>.
    */
   public boolean equals(Object obj)
   {
      boolean isMatch = true;
      if (!(obj instanceof PSJndiGroupProviderInstance))
         isMatch = false;
      else
      {
         PSJndiGroupProviderInstance other = (PSJndiGroupProviderInstance)obj;
         if (!super.equals(other))
            isMatch = false;
         else if (!this.m_objectClasses.equals(other.m_objectClasses))
            isMatch = false;
         else if (!this.m_groupNodes.equals(other.m_groupNodes))
            isMatch = false;
      }

      return isMatch;
   }
   
   
   /**
    * Returns hash code.
    */
   @Override
   public int hashCode()
   {
      return super.hashCode() +
            m_objectClasses.hashCode() + m_groupNodes.hashCode();
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSGroupProviderInstance. May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if c is null
    */
   public void copyFrom( PSJndiGroupProviderInstance c )
   {
      // call super's copy
      super.copyFrom(c);

      // copy all members
      m_objectClasses.clear();
      m_objectClasses.addAll(c.m_objectClasses);
      m_groupNodes.clear();
      m_groupNodes.addAll(c.m_groupNodes);
   }

   // see interface for description
   public Object clone()
   {
      // call super's clone - gives deep copy of everything except for this
      // classes mutable members, which are shallow.
      PSJndiGroupProviderInstance copy =
         (PSJndiGroupProviderInstance)super.clone();

      // now clone this class's mutable members
      copy.m_objectClasses = new ArrayList();
      copy.m_objectClassMap = new HashMap();
      Iterator ocs = m_objectClasses.iterator();
      while (ocs.hasNext())
      {
         PSJndiObjectClass oc = (PSJndiObjectClass)ocs.next();
         PSJndiObjectClass ocClone = new PSJndiObjectClass(
            oc.getObjectClassName(), oc.getMemberAttribute(),
            oc.getAttributeType());
         copy.m_objectClasses.add(ocClone);
         copy.m_objectClassMap.put(ocClone.getObjectClassName().toLowerCase(),
            ocClone);
      }

      return copy;
   }

   /**
    * Name of root XML element.
    */
   public static final String XML_NODE_NAME = "PSXJnidGroupProviderInstance";


   /**
    * List of PSJndiObjectClass objects.  Never <code>null</code>, may be
    * empty.
    */
   private List m_objectClasses = new ArrayList();

   /**
    * Map of objectClasses with the name lowercased as the key (a String) and
    * the corresponding PSJndiObjectClass as the value.  Never <code>null</code>
    * or empty.  Maintained in addition to the List so as to provide case
    * insensitive named access to an object class, and to maintain an ordered
    * list as well.
    *
    * @todo write a PSListMap class.
    */
   private Map m_objectClassMap = new HashMap();

   /**
    * List of group nodes as Strings.  Never <code>null</code>, may be
    * empty.
    */
   private List m_groupNodes = new ArrayList();

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String OBJECT_CLASSES_ELEMENT = "objectClasses";
   private static final String OBJECT_CLASS_ELEMENT = "objectClass";
   private static final String GROUP_NODES_ELEMENT = "groupNodes";
   private static final String GROUP_NODE_ELEMENT = "groupNode";
   private static final String NAME_ATTR = "name";
   private static final String MEMBER_ATTR = "memberattr";
   private static final String TYPE_ATTR = "type";
}
