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
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Dependency class that represents a cms element that can only be deployed or
 * identified as a child dependency of {@link PSDeployableElement} or another
 * <code>PSDeployableObject</code>.
 */
public class PSDeployableObject extends PSDependency
{
   /**
    * Convenience ctor that calls <code>this(dependencyType, dependencyId,
    * objectType, objectTypeName, displayName, supportsIdTypes, 
    * supportsIdMapping,  supportsUserDependencies, false)</code>.
    */
   public PSDeployableObject(int dependencyType, String dependencyId,
      String objectType, String objectTypeName, String displayName,
      boolean supportsIdTypes, boolean supportsIdMapping,
         boolean supportsUserDependencies)
   {
      this(dependencyType, dependencyId, objectType, objectTypeName, 
      displayName, supportsIdTypes, supportsIdMapping, supportsUserDependencies, 
      false);
   }
   

   /**
    * Construct a dependency with all required parameters.
    * 
    * @param dependencyType The type of dependency, must be one of the 
    * <code>TYPE_xxx</code> types.
    * @param dependencyId Combined with <code>objectType</code> uniquely 
    * identifies the object this dependency represents.  May not be 
    * <code>null</code> or empty.
    * @param displayName Name to use when displaying this dependency.  May not 
    * be <code>null</code> or empty.
    * @param objectType The type of object this dependency represents. May not
    * be <code>null</code> or empty.
    * @param objectTypeName Displayable form of the <code>objectType</code>,
    * may not be <code>null</code> or empty.
    * @param supportsIdTypes <code>true</code> if this object contains static
    * ID's whose type must be identified, <code>false</code> if not.
    * @param supportsIdMapping <code>true</code> if this object's ID can change
    * across server's and thus may be included in an ID Mapping.
    * @param supportsUserDependencies If <code>true</code>, this dependency
    * allows user defined dependencies to be added as children, 
    * <code>false</code> otherwise.
    * @param supportsParentId If <code>true</code>, supports a parent id to be
    * specified, if <code>false</code>, does not.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSDeployableObject(int dependencyType, String dependencyId, 
      String objectType, String objectTypeName, String displayName, 
      boolean supportsIdTypes, boolean supportsIdMapping, 
         boolean supportsUserDependencies, boolean supportsParentId)
   {
      super(dependencyType, dependencyId, 
         objectType, objectTypeName, displayName, 
         supportsIdTypes, supportsIdMapping, supportsUserDependencies,
         supportsParentId);
               
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
   public PSDeployableObject(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");
      
      fromXml(src);
   }
   
   /**
    * Parameterless ctor for use by derived classes only.
    */
   protected PSDeployableObject()
   {
   }
   
   
   /**
    * This method is called to create an XML element node with the
    * appropriate format for this object. Format is:
    * <pre><code>
    * &lt;!ELEMENT PSXDeployableObject (PSXDependency, RequiredClasses)>
    * &lt;!ELEMENT RequiredClasses (className*)>
    * &lt;!ELEMENT className (#PCDATA)>
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
      root.appendChild(super.toXml(doc));
      
      Element classes = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
         XML_EL_REQUIRED_CLASSES);
      Iterator classNames = m_classNames.iterator();
      while (classNames.hasNext())
      {
         String name = (String)classNames.next();
         PSXmlDocumentBuilder.addElement(doc, classes, XML_EL_CLASS_NAME, name);
      }
      
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
      
      int firstFlags = (PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      int nextFlags = (PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element dep = tree.getNextElement(PSDependency.XML_NODE_NAME, firstFlags);
      if (dep == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, PSDependency.XML_NODE_NAME);
      }
      super.fromXml(dep);
      
      Element classes = tree.getNextElement(XML_EL_REQUIRED_CLASSES, 
         nextFlags);
      if (classes == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_EL_REQUIRED_CLASSES);
      }
      
      m_classNames.clear();
      Element className = tree.getNextElement(XML_EL_CLASS_NAME, firstFlags);
      while (className != null)
      {
         String name = tree.getElementData(className);
         if (name != null && name.trim().length() > 0)
            m_classNames.add(name);
         className = tree.getNextElement(XML_EL_CLASS_NAME, nextFlags);
      }
      
      
   }
   
   // see IPSDeployComponent
   public boolean equals(Object obj)
   {
      boolean isEqual = true;
      if (!(obj instanceof PSDeployableObject))
         isEqual = false;
      else
      {
         PSDeployableObject other = (PSDeployableObject)obj;
         if (!super.equals(obj))
            isEqual = false;
         else if (!m_classNames.equals(other.m_classNames))
            isEqual = false;
      }
      
      return isEqual;
   }
   
   // see IPSDeployComponent
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");
         
      if (!(obj instanceof PSDeployableObject))
         throw new IllegalArgumentException("obj wrong type");

      PSDeployableObject dep = (PSDeployableObject)obj;
      super.copyFrom(dep);
      m_classNames.clear();
      m_classNames.addAll(dep.m_classNames);
   }
   
   //overridden to deep copy mutable members
   public Object clone()
   {
      PSDeployableObject copy = (PSDeployableObject) super.clone();
      copy.m_classNames = new ArrayList();
      copy.m_classNames.addAll(m_classNames);
      
      return copy;
   }

   // see IPSDeployComponent
   public int hashCode()
   {
      return super.hashCode() + m_classNames.hashCode();
   }
   
   /**
    * Sets the non-deployable classes required by this dependency.  Set when 
    * this dependency is added to an archive by its handler.
    * 
    * @param classNames An iterator over zero or more <code>String</code> 
    * objects specifiying fully qualified class names.  May not be 
    * <code>null</code>, and each entry may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>classNames</code> is 
    * <code>null</code>, empty, or contains an <code>null</code> or empty entry.
    */
   public void setRequiredClasses(Iterator classNames)
   {
      if (classNames == null || !classNames.hasNext())
         throw new IllegalArgumentException(
            "classNames may not be null or empty");
      
      m_classNames.clear();
      while (classNames.hasNext())
      {
         String name = (String)classNames.next();
         if (name == null || name.trim().length() == 0)
         {
            m_classNames.clear();
            throw new IllegalArgumentException(
               "classNames may not contain a null or empty entry");
         }
         m_classNames.add(name);
      }
   }

   /**
    * Gets the non-deployable classes required by this dependency.  See 
    * {@link #setRequiredClasses(Iterator)} for more info.
    * 
    * @return an iterator over zero or more non-<code>null</code> non-empty
    * <code>String</code> objects specifiying fully qualified class names.  
    * Never <code>null</code>.
    */
   public Iterator getRequiredClasses()
   {
      return m_classNames.iterator();
   }
   
   
   /**
    * Constant for this object's root XML node.
    */
   public static final String XML_NODE_NAME = "PSXDeployableObject";
   
   /**
    * List of class names required by this dependency.  Never <code>null</code>, 
    * empty until contents are modified by a call to 
    * {@link #setRequiredClasses(Iterator)}.
    */
   private List m_classNames = new ArrayList();
   
   // private constants for XML serialization
   private static final String XML_EL_REQUIRED_CLASSES = "RequiredClasses";
   private static final String XML_EL_CLASS_NAME = "className";

}

