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

package com.percussion.deployer.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSStringOperation;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;

/**
 * Represents a file dependency that must be specified by the user as it may not
 * be automatically discovered by the Rhythmyx server.
 */
public class PSUserDependency extends PSDeployableObject
{

   /** 
    * Package private ctor.  User dependencies should always be created using 
    * {@link PSDependency#addUserDependency(String) addUserDependency} on its 
    * intended parent dependency.
    * 
    * @param path The path of the dependency file relative to the Rhythmyx 
    * server root directory.  May not be <code>null</code>.
    * @param parent  The parent dependency, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   PSUserDependency(File path, PSDependency parent)
   {
      super();
      
      // do validation
      if (path == null)
         throw new IllegalArgumentException("path may not be null");
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");
         
      // this is a bit ugly, but lets us validate input params instead of 
      // calling full super() first, possibly throwing a NullPointerException.
      PSDeployableObject obj = new PSDeployableObject(PSDependency.TYPE_USER, 
         PSDeployComponentUtils.getNormalizedPath(path.getPath()), 
         USER_DEPENDENCY_TYPE, USER_DEPENDENCY_TYPE_NAME, path.getName(), 
         false, false, false);
      
      super.copyFrom(obj);
      
      m_path = path;
      m_parentType = parent.getObjectType();
      m_parentId = parent.getDependencyId();
      m_parentKey = parent.getKey();
   }
   
   /**
    * Constructs this object from its XML representation.
    * 
    * @param src The source element.  Format expected is defined by 
    * {@link #toXml(Document)}.
    * 
    * @throws IllegalArgumentException if <code>sourceNode</code> is 
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node does not 
    * represent a type supported by the class.
    */
   public PSUserDependency(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");
      
      fromXml(src);
   }
   
   
   /**
    * Gets the path of this dependency's file.
    * 
    * @return The path, never <code>null</code>.
    */
   public File getPath()
   {
      return m_path;
   }
   
   /**
    * Gets the object type of this dependency's parent.
    * 
    * @return The type, never <code>null</code> or empty.
    */
   public String getParentType()
   {
      return m_parentType;
   }
   
   /**
    * Gets the dependency id of this dependency's parent.
    * 
    * @return The id, never <code>null</code> or empty.
    */
   public String getParentId()
   {
      return m_parentId;
   }
   
   /**
    * Returns a unique key for this dependency. Overriden for
    * this object since the default implementation returns a key that includes
    * the dependency id, and for this class that is a file path.  This key may 
    * be used to create directory names in some cases, and so returning a file
    * path as part of this key would be problematic.  
    * 
    * @return The key, never <code>null</code> or empty.  
    */
   public String getKey()
   {
      String key = super.getKey();
      key = PSStringOperation.replace(key, '/', '_');
      key = PSStringOperation.replace(key, '.', '-');
      return key;
   }
   
   /**
    * Returns the value of {@link PSDependency#getKey()} for this dependency's
    * parent.
    * 
    * @return The key, never <code>null</code> or empty.
    */
   public String getParentKey()
   {
      return m_parentKey;
   }
   
   /**
    * This method is called to create an XML element node with the
    * appropriate format for this object. Format is:
    * <pre><code>
    * &lt;!ELEMENT PSXUserDependency (PSXDeployableObject)>
    * &lt;!ATTLIST PSXUserDependency 
    *    path CDATA #REQUIRED
    *    parentType CDATA #REQUIRED
    *    parentId CDATA #REQUIRED
    *    parentKey CDATA #REQUIRED
    * >
    * </pre></code>
    * 
    * @param doc The document to use to create the element, may not be 
    * <code>null</code>.
    * 
    * @return the newly created XML element node, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
         
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_PARENT_ID, m_parentId);
      root.setAttribute(XML_ATTR_PARENT_TYPE, m_parentType);
      root.setAttribute(XML_ATTR_PARENT_KEY, m_parentKey);
      root.setAttribute(XML_ATTR_PATH, PSDeployComponentUtils.getNormalizedPath(
         m_path.getPath()));
      root.appendChild(super.toXml(doc));
      
      
      return root;
   }

   /**
    * This method is called to populate this object from its XML representation.
    * 
    * @param sourceNode the XML element node to populate from, not 
    * <code>null</code>.  See {@link #toXml(Document)} for the format expected.
    * 
    * @throws IllegalArgumentException if <code>sourceNode</code> is 
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node does not 
    * represent a type supported by the class.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");
         
      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      m_parentId = getRequiredAttribute(sourceNode, XML_ATTR_PARENT_ID);
      m_parentType = getRequiredAttribute(sourceNode, XML_ATTR_PARENT_TYPE);
      m_parentKey = getRequiredAttribute(sourceNode, XML_ATTR_PARENT_KEY);
      m_path = new File(getRequiredAttribute(sourceNode, XML_ATTR_PATH));
            
      int firstFlags = (PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      int nextFlags = (PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element dep = tree.getNextElement(PSDeployableObject.XML_NODE_NAME, 
         firstFlags);
      if (dep == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, 
               PSDeployableObject.XML_NODE_NAME);
      }
      super.fromXml(dep);
   }

   // see IPSDeployComponent
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");
         
      if (!(obj instanceof PSUserDependency))
         throw new IllegalArgumentException("obj wrong type");

      PSUserDependency dep = (PSUserDependency)obj;
      super.copyFrom(dep);
      
      m_parentId = dep.m_parentId;
      m_parentType = dep.m_parentType;
      m_parentKey = dep.m_parentKey;
      m_path = dep.m_path;
   }
   
   //overridden to deep copy mutable members
   public Object clone()
   {
      PSUserDependency copy = (PSUserDependency)super.clone();
      copy.m_path = new File(m_path.getPath());
      return copy;
   }

   // see IPSDeployComponent
   public int hashCode()
   {
      return super.hashCode() + m_parentId.hashCode() + m_parentType.hashCode() 
         + m_path.hashCode() + m_parentKey.hashCode();
   }

   // see IPSDeployComponent
   public boolean equals(Object obj)
   {
      boolean isEqual = true;
      if (!(obj instanceof PSUserDependency))
         isEqual = false;
      else
      {
         PSUserDependency other = (PSUserDependency)obj;
         if (!super.equals(obj))
            isEqual = false;
         else if (!m_parentId.equals(other.m_parentId))
            isEqual = false;
         else if (!m_parentType.equals(other.m_parentType))
            isEqual = false;
         else if (!m_path.equals(other.m_path))
            isEqual = false;
         else if (!m_parentKey.equals(other.m_parentKey))
            isEqual = false;
      }
      
      return isEqual;
   }

   /**
    * Constant for this object's root XML node.
    */
   public static final String XML_NODE_NAME = "PSXUserDependency";
   
   /**
    * Constant for the object type of a user depednecy.
    */
   public static final String USER_DEPENDENCY_TYPE = "sys_UserDependency";
   
   /**
    * Constant for the name of the object type of a user depednecy.
    */
   public static final String USER_DEPENDENCY_TYPE_NAME = "User Dependency";
   
   /**
    * File reference to the resource that this user dependency represents.  
    * Initialized during ctor, never <code>null</code> after that, may be 
    * modified by a call to <code>copyFrom()</code>.
    */
   private File m_path;
   
   /**
    * Type of object the owner of this dependency represents.
    * Initialized during ctor, never <code>null</code> after that, may be 
    * modified by a call to <code>copyFrom()</code>.
    */
   private String m_parentType;
   
   /**
    * Id of the object the owner of this dependency represents.
    * Initialized during ctor, never <code>null</code> after that, may be 
    * modified by a call to <code>copyFrom()</code>.
    */
   private String m_parentId;

   /**
    * Key of the object the owner of this dependency represents.
    * Initialized during ctor, never <code>null</code> after that, may be 
    * modified by a call to <code>copyFrom()</code>.
    */
   private String m_parentKey;
   
   
   // Xml constants
   private static final String XML_ATTR_PATH = "path";
   private static final String XML_ATTR_PARENT_TYPE = "parentType";
   private static final String XML_ATTR_PARENT_ID = "parentId";
   private static final String XML_ATTR_PARENT_KEY = "parentKey";
}
