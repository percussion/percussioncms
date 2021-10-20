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
package com.percussion.design.objectstore;

import com.percussion.error.PSException;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the PSXContentEditorPipe DTD defined in
 * ContentEditorLocalDef.dtd.
 */
public class PSContentEditorPipe  extends PSPipe
{
   /**
    * Create a new content editor pipe.
    *
    * @param name the content editor pipe name, not <code>null</code> or
    *    empty.
    * @param locator the container locater, not <code>null</code>.
    * @param mapper the content editor mapper, not <code>null</code>.
    */
   public PSContentEditorPipe(String name, PSContainerLocator locator,
                              PSContentEditorMapper mapper)
   {
      try
      {
         setName(name);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
      setLocator(locator);
      setMapper(mapper);
   }

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    not <code>null</code>.
    * @param parentComponents   the parent objects of this object, not
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSContentEditorPipe(Element sourceNode, IPSDocument parentDoc,
                              List parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSContentEditorPipe()
   {
   }

   /**
    * Get the container locator.
    *
    * @return the locator, never <code>null</code>.
    */
   public PSContainerLocator getLocator()
   {
      return m_locator;
   }

   /**
    * Set the new container locator.
    *
    * @param locator the new locator, never <code>null</code>.
    */
   public void setLocator(PSContainerLocator locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator cannot be null");

      m_locator = locator;
   }

   /**
    * Get the content editor mapper.
    *
    * @return the content editor mapper, never
    *    <code>null</code>.
    */
   public PSContentEditorMapper getMapper()
   {
      return m_mapper;
   }

   /**
    * Set the new content editor mapper.
    *
    * @param mapper the new mapper, not <code>null</code>.
    */
   public void setMapper(PSContentEditorMapper mapper)
   {
      if (mapper == null)
         throw new IllegalArgumentException("mapper cannot be null");

      m_mapper = mapper;
   }

   /**
    * Get the control dependency map, used to manage extensions from control 
    * dependencies.
    * 
    * @return The map, never <code>null</code>.
    */
   public PSControlDependencyMap getControlDependencyMap()
   {
      return m_controlDepMap;
   }
   
   public PSExtensionCallSet getInputDataExtensions()
   {
      PSExtensionCallSet set = new PSExtensionCallSet();
      set.addAll(m_controlDepMap.getInputDataExtensions());
      if (super.getInputDataExtensions() != null)
         set.addAll(super.getInputDataExtensions());
      else if (set.isEmpty())
         return null;
      
      return set;
   }
   
   /**
    * Same as {@link PSPipe#getInputDataExtensions()} but the results do not
    * contain control dependency extensions.  
    * 
    * @return The extensions, never <code>null</code>, may be empty.
    */
   public PSExtensionCallSet getContentEditorInputDataExtensions()
   {
      return super.getInputDataExtensions();
   }   
   
   /**
    * For content editors input data extensions include control dependencies also.
    * But the control dependencies are added to the input data extensions during
    * toXml. Inorder to avoid confusion, this method has been overwritten to
    * throw UnsupportedOperationException exception. 
    * Use {@link #setContentEditorInputDataExtensions(PSExtensionCallSet)} to set the input data
    * extensions. 
    */
   @Override
   public void setInputDataExtensions(PSExtensionCallSet extensions)
   {
      throw new UnsupportedOperationException();
   }
   
   /**
    * Same as {@link PSPipe#setInputDataExtensions(PSExtensionCallSet)} to set just input
    * data extesnions. The control dependency extensions will be added to
    * input data extensions during the toXML() of this object.
    */
   public void setContentEditorInputDataExtensions(PSExtensionCallSet extensions)
   {
      super.setInputDataExtensions(extensions);
   }
   
   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSContentEditorPipe, not <code>null</code>.
    */
   public void copyFrom(PSContentEditorPipe c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }

      setLocator(c.getLocator());
      setMapper(c.getMapper());
      m_controlDepMap = c.m_controlDepMap;
   }

   /**
    * Test if the provided object and this are equal.
    *
    * @param o the object to compare to.
    * @return <code>true</code> if this and o are equal,
    *    <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSContentEditorPipe))
         return false;

      PSContentEditorPipe t = (PSContentEditorPipe) o;

      boolean equal = true;
      if (!compare(m_locator, t.m_locator))
         equal = false;
      else if (!compare(m_mapper, t.m_mapper))
         equal = false;
      else if (!compare(m_controlDepMap, t.m_controlDepMap))
         equal = false;
      else
         equal = super.equals(o);

      return equal;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode()}.
    */
   @Override
   public int hashCode()
   {
      return super.hashCode() + m_controlDepMap.hashCode();
   }

   /**
     * Returns a backend datatank that contains a fully defined PSBackEndTable
     * for each table in this pipe's PSContainerLocator.  This tank is for
     * in-memory use only - it is not included when this object is serialized
     * to and from XML.
     *
     * @return   the back-end data tank
     */
   public PSBackEndDataTank getBackEndDataTank()
   {
      try
      {
         PSBackEndDataTank backEndDataTank = new PSBackEndDataTank();
         backEndDataTank.setTables(new PSCollection(
            getLocator().getBackEndTables().values().iterator()));
         return backEndDataTank;
     }
      catch(IllegalArgumentException e)
      {
         /*
          * This can only happen if we call setTables with an emtpy collection
          * or a collection that not only contain PSBackEndTables.  This is
          * guaranteed not to happen based on the methods we are calling to get
          * the collection of backend tables.  If this happens, it's a bug
          * in our code.
          */
          throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      List parentComponents) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);
      
      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;
      
      Element node = null;
      String data = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
         
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         
         // REQUIRED: id attribute
         data = tree.getElementData(PSComponent.ID_ATTR);
         try
         {
            m_id = Integer.parseInt(data);
         }
         catch (Exception e)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               ((data == null) ? "null" : data)
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }
         
         // REQUIRED: pipe name
         try
         {
            setName(tree.getElementData(PSPipe.NAME_ELEM));
         }
         catch (IllegalArgumentException e)
         {
            throw new PSUnknownNodeTypeException(
               XML_NODE_NAME, PSPipe.NAME_ELEM, new PSException (
                  e.getLocalizedMessage()));
         }
         
         // OPTIONAL: pipe description
         try
         {
            setDescription(tree.getElementData(PSPipe.DESCRIPTION_ELEM));
         }
         catch (IllegalArgumentException e)
         {
            throw new PSUnknownNodeTypeException(
               XML_NODE_NAME, PSPipe.DESCRIPTION_ELEM, new PSException (
                  e.getLocalizedMessage()));
         }
         
         Node current = tree.getCurrent();
         
         // OPTIONAL: input data exits
         node = tree.getNextElement(PSPipe.INPUT_DATA_EXITS_ELEM, firstFlags);
         if (node != null)
         {
            // the input data exits can be empty
            if (tree.getNextElement(
               PSExtensionCallSet.ms_NodeType, firstFlags) != null)
            {
               // the next element had better be the exit set
               m_inputDataExtensions = new PSExtensionCallSet(
                  (Element) tree.getCurrent(), parentDoc, parentComponents);
            }
         }
         
         tree.setCurrent(current);
         
         // OPTIONAL: output data exits
         node = tree.getNextElement(PSPipe.RESULT_DATA_EXITS_ELEM, firstFlags);
         if (node != null)
         {
            // the result data exits can be empty
            if (tree.getNextElement(
               PSExtensionCallSet.ms_NodeType, firstFlags) != null)
            {
               // the next element had better be the exit set
               m_resultDataExtensions = new PSExtensionCallSet(
                  (Element) tree.getCurrent(), parentDoc, parentComponents);
            }
         }
         
         tree.setCurrent(current);
         
         // REQUIRED: get the locator
         node = tree.getNextElement(
            PSContainerLocator.XML_NODE_NAME, firstFlags);
         if (node != null)
         {
            m_locator = new PSContainerLocator(
               node, parentDoc, parentComponents);
         }
         else
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSContainerLocator.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         
         // REQUIRED: get the mappeer
         node = tree.getNextElement(
            PSContentEditorMapper.XML_NODE_NAME, nextFlags);
         if (node != null)
         {
            m_mapper = new PSContentEditorMapper(
               node, parentDoc, parentComponents);
         }
         else
         {
            Object[] args =
            {
               XML_NODE_NAME,
               PSContentEditorMapper.XML_NODE_NAME,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         
         tree.setCurrent(current);
         
         // Optional: get user props for control deps
         node = tree.getNextElement(USER_PROPERTIES, firstFlags);
         if (node != null)
         {
            Map<String, String> userProps = loadUserProps(node);
            m_controlDepMap = new PSControlDependencyMap(userProps, 
               m_inputDataExtensions);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    *
    * @see IPSComponent
    */
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(PSComponent.ID_ATTR, String.valueOf(m_id));

      // defined in base class PSPipe
      PSXmlDocumentBuilder.addElement(doc, root, PSPipe.NAME_ELEM, m_name);

      // defined in base class PSPipe
      PSXmlDocumentBuilder.addElement(
         doc, root, PSPipe.DESCRIPTION_ELEM, m_description);

      // generate the user properties from control dependencies, add control
      // extensions to the input data extensions.
      PSExtensionCallSet inputDataExts = new PSExtensionCallSet();
      Map<String, String> userProps = m_controlDepMap.generateUserProperties(
         inputDataExts);
      // defined in base class PSPipe
      if (m_inputDataExtensions != null)
      {
         inputDataExts.addAll(m_inputDataExtensions);
      }

      if (!inputDataExts.isEmpty())
      {
         Element parent = PSXmlDocumentBuilder.addEmptyElement(
            doc, root, PSPipe.INPUT_DATA_EXITS_ELEM);
         parent.appendChild(inputDataExts.toXml(doc));
      }
      

      // defined in base class PSPipe
      if (m_resultDataExtensions != null)
      {
         Element parent = PSXmlDocumentBuilder.addEmptyElement(
            doc, root, PSPipe.RESULT_DATA_EXITS_ELEM);
         parent.appendChild(m_resultDataExtensions.toXml(doc));
      }

      // REQUIRED: create the locator
      root.appendChild(m_locator.toXml(doc));

      // REQUIRED: create the mapper
      root.appendChild(m_mapper.toXml(doc));

      if (!userProps.isEmpty())
      {
         Element userPropsEl = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
            USER_PROPERTIES);
         for (Map.Entry<String, String> entry : userProps.entrySet())
         {
            Element propEl = PSXmlDocumentBuilder.addElement(doc, userPropsEl, 
               USER_PROPERTY, entry.getValue());
            propEl.setAttribute(USER_PROP_NAME_ATTR, entry.getKey());
         }
      }
      return root;
   }

   
   /**
    * Load user properties from the supplied node.  Package access for unit test
    * purposes.
    * 
    * @param node The parent node, may not be <code>null</code>.
    * 
    * @return The map of properties where the key is the name and the value is 
    * the property value, never <code>null</code>, might be empty.
    */
   static Map<String, String> loadUserProps(Element node)
   {
      if (node == null)
         throw new IllegalArgumentException("node may not be null");
      
      Map<String, String> props = new HashMap<>();
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(node);
      Element propEl = tree.getNextElement(USER_PROPERTY, 
         tree.GET_NEXT_ALLOW_CHILDREN);
      while (propEl != null)
      {
         String name = propEl.getAttribute(USER_PROP_NAME_ATTR);
         if (StringUtils.isBlank(name))
            continue;
         String value = tree.getElementData();
         props.put(name, value);
         propEl = tree.getNextElement(USER_PROPERTY, 
            tree.GET_NEXT_ALLOW_SIBLINGS);
      }
      
      return props;
   }      

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
   {
      if (!context.startValidation(this, null))
         return;

      // do children
      context.pushParent(this);
      try
      {
         if (m_locator != null)
            m_locator.validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_CONTENT_EDITOR_PIPE, null);

         if (m_mapper != null)
            m_mapper.validate(context);
         else
            context.validationError(this,
               IPSObjectStoreErrors.INVALID_CONTENT_EDITOR_PIPE, null);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXContentEditorPipe";
   
   /** the XML node name for user properties */
   public static final String USER_PROPERTIES = "userProperties";
   
   /** the XML node name for user property */
   public static final String USER_PROPERTY = "userProperty";
   
   /** the XML name attribute */
   public static final String USER_PROP_NAME_ATTR = "name";
   
   /** The container locator, always valid after construction. */
   private PSContainerLocator m_locator = null;

   /** The content editor mapper, always valid after construction. */
   private PSContentEditorMapper m_mapper = null;
   
   /**
    * Manages the controls dependencies based on the pipe's user properties,
    * never <code>null</code>, modified by 
    * {@link #fromXml(Element, IPSDocument, List)}.
    */
   private PSControlDependencyMap m_controlDepMap = 
      new PSControlDependencyMap();
   
   
   
}

