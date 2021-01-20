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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents the metadata for a single parameter to a content editor control,
 * as defined by the &lt;psxctl:Param&gt; node in <code>
 * sys_LibraryControlDef.dtd</code>
 */
public class PSControlParameter extends PSComponent
{
   /**
    * Initializes a newly created <code>PSControlParameter</code> object, from
    * an XML representation.  See {@link #toXml(Document)} for the format.
    *
    * @param sourceNode the XML element node to construct this object from.
    *    Cannot be <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML representation is not
    *    in the expected format.
    */
   public PSControlParameter(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException("sourceNode cannot be null.");
      fromXml(sourceNode, null, null);
   }


   /**
    * This method is called to populate an object from an XML
    * element node. An element node may contain a hierarchical structure,
    * including child objects. The element node can also be a child of
    * another element node.  See {@link #toXml(Document)} for the format.
    *
    * @param sourceNode element with name specified by {@link #XML_NODE_NAME}
    * @param parentDoc ignored.
    * @param parentComponents ignored.
    * @throws PSUnknownNodeTypeException  if an expected XML element is missing,
    *    or <code>null</code>
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
         throws PSUnknownNodeTypeException
   {
      validateElementName(sourceNode, XML_NODE_NAME);
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      m_name = getRequiredElement(tree, XML_NAME_ATTR);
      m_dataType = getRequiredElement(tree, XML_DATATYPE_ATTR);
      m_paramType = getRequiredElement(tree, XML_PARAMTYPE_ATTR);
      setRequired(getEnumeratedAttribute(tree, XML_REQUIRED_ATTR,
            XML_REQUIRED_ENUM));

      // move on to the children
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      Element child = tree.getNextElement(firstFlags);
      while (child != null)
      {
         String childName = child.getTagName();

         if (childName.equals(XML_DESCRIPTION_NODE))
            setDescription(PSXmlTreeWalker.getElementData(child));
         else if (childName.equals(XML_DEFAULTVALUE_NODE))
            setDefaultValue(PSXmlTreeWalker.getElementData(child));
         else if (childName.equals(XML_CHOICELIST_NODE))
         {
            // this node contains psxctl:Entry children; collect into a list
            ArrayList entries = new ArrayList();
            NodeList entryNodes = child.getChildNodes();
            for (int i=0; i < entryNodes.getLength(); i++)
            {
               Node n = entryNodes.item( i );
               if (n instanceof Element)
               {
                  Element element = (Element) n;
                  entries.add( new Entry( element ) );
               }
            }
            setChoiceList( entries );
         }

         child = tree.getNextElement(nextFlags);
      }

   }



   /**
    * This method is called to create an XML element node with the
    * appropriate format for the given object. An element node may contain a
    * hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    * <pre><code>
    * &lt;!ELEMENT psxctl:Param (psxctl:Description, psxctl:ChoiceList?, psxctl:DefaultValue?)>
    * &lt;!ATTLIST psxctl:Param
    *    name CDATA #REQUIRED
    *    datatype CDATA #REQUIRED
    *    required (yes | no) "no"
    *    paramtype CDATA #REQUIRED>
    * &lt;!ELEMENT psxctl:ChoiceList (psxctl:Entry+)>
    * &lt;!ELEMENT psxctl:Entry (#PCDATA)>
    * &lt;!ATTLIST psxctl:Param
    *    internalName CDATA #IMPLIED>
    * &lt;!ELEMENT psxctl:DefaultValue (#PCDATA)>
    * </code></pre>
    *
    * @return    the newly created XML element node
    * @todo write choicelists back
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_NAME_ATTR, m_name);
      root.setAttribute(XML_DATATYPE_ATTR, m_dataType);
      root.setAttribute(XML_REQUIRED_ATTR, (m_required ? "yes" : "no") );
      root.setAttribute(XML_PARAMTYPE_ATTR, m_paramType);
      if (m_description.length() > 0)
         PSXmlDocumentBuilder.addElement(doc, root, XML_DESCRIPTION_NODE,
               m_description);
      // TODO: psxctl:ChoiceList
      if (m_defaultValue.length() > 0)
         PSXmlDocumentBuilder.addElement(doc, root, XML_DEFAULTVALUE_NODE,
               m_defaultValue);
       return root;
   }


   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component.
    *
    * @param control a valid (not null) <code>PSControlParameter</code>
    */
   public void copyFrom(PSComponent object)
   {
      if ( !(object instanceof PSControlParameter) )
         throw new IllegalArgumentException(
               "Must provide a non-null PSControlParameter");
      PSControlParameter param = (PSControlParameter) object;
      super.copyFrom(param);
      m_name = param.getName();
      m_dataType = param.getDataType();
      m_required = param.isRequired();
      setParamType(param.getParamType());
      setDescription(param.getDescription());
      setDefaultValue(param.getDefaultValue());
      setChoiceList(param.getChoiceList());
   }


   /**
    * @return A valid String representing the name of this parameter
    */
   public String toString()
   {
      return m_name;
   }


   /**
    * @return <code>true</code> if this parameter is required for its control;
    *         <code>false</code> otherwise.
    */
   public boolean isRequired()
   {
      return m_required;
   }


   /**
    * @return The default value for this parameter.  Never <code>null</code>,
    *         but may be empty.
    */
   public String getDefaultValue()
   {
      return m_defaultValue;
   }


   /**
    * @return A descriptive term for the type of the expected data supplied to
    *         this parameter. May be empty, but never <code>null</code>.
    */
   public String getDataType()
   {
      return m_dataType;
   }


   /**
    * @return The type of this parameter. The possible values are control-
    *         specific. May be empty, but never <code>null</code>.
    */
   public String getParamType()
   {
      return m_paramType;
   }


   /**
    * @return The name of this parameter; never empty or <code>null</code>.
    */
   public String getName()
   {
      return m_name;
   }


   /**
    * @return The description of this parameter; may be empty, but never
    *         <code>null</code>.
    */
   public String getDescription()
   {
      return m_description;
   }


   /**
    * Gets the list of entries for this parameter's choice list.
    *
    * @return List of Entry objects, or <code>null</code> if no list is defined
    * for this parameter.
    */
   public List getChoiceList()
   {
      return m_choiceList;
   }


   /**
    * Sets the list of entries for this parameter's choice list.
    *
    * @param choiceList List of Entry objects, or <code>null</code> to clear
    * the list.
    */
   public void setChoiceList(List choiceList)
   {
      m_choiceList = choiceList;
   }


   /**
    * Sets the parameter type member variable, enforcing the assertion that this
    * variable will never be <code>null</code>.
    *
    * @param paramType The value to assign. If <code>null</code>, the empty
    *        string is used instead.
    */
   private void setParamType(String paramType)
   {
      if (null == paramType)
         m_paramType = "";
      else
         m_paramType = paramType;
   }


   /**
    * Sets the description member variable, enforcing the assertion that this
    * variable will never be <code>null</code>.
    *
    * @param description The value to assign. If <code>null</code>, the empty
    *        string is used instead.
    */
   private void setDescription(String description)
   {
      if (null == description)
         m_description = "";
      else
         m_description = description;
   }

   /**
    * Sets the default value member variable, enforcing the assertion that this
    * variable will never be <code>null</code>.
    *
    * @param defaultValue The value to assign. If <code>null</code>, the empty
    *        string is used instead.
    */
   private void setDefaultValue(String defaultValue)
   {
      if (null == defaultValue)
         m_defaultValue = "";
      else
         m_defaultValue = defaultValue;
   }


   /**
    * Sets whether this parameter is required
    *
    * @param required "yes" if this parameter is required; any other value will
    * be iinterpreted as not required.
    */
   public void setRequired(String required)
   {
      if (required != null && required.equals("yes"))
         m_required = true;
      else
         m_required = false;
   }


   /**
    * Name of parent XML node
    */
   public static final String XML_NODE_NAME = "psxctl:Param";

   private static final String XML_NAME_ATTR = "name";
   private static final String XML_DATATYPE_ATTR = "datatype";
   private static final String XML_PARAMTYPE_ATTR = "paramtype";
   private static final String XML_REQUIRED_ATTR = "required";

   private static final String XML_DESCRIPTION_NODE = "psxctl:Description";
   private static final String XML_DEFAULTVALUE_NODE = "psxctl:DefaultValue";
   private static final String XML_CHOICELIST_NODE = "psxctl:ChoiceList";

   /**
    * An array of legal values for the choiceset XML attribute.  The value at
    * index 0 is the default.
    */
   private static final String[] XML_REQUIRED_ENUM = {
      "no", "yes"
   };


   /**
    * Name of this parameter. Never <code>null</code> or empty after
    * construction.
    */
   private String m_name;

   /**
    * Describes the type of the expected data supplied as the value of this
    * parameter. Recommend one of: String, Date, Time, Datetime, Number.
    * Never <code>null</code> after construction.
    */
   private String m_dataType;

   /**
    * Indicates if this parameter is required.
    */
   private boolean m_required;

   /**
    * Specifies what category of parameter this is.  The possible values are
    * control specific.  For example, generic, custom, or img. Never <code>null
    * </code> after construction.
    */
   private String m_paramType;

   /**
    * A full description of this parameter. Never <code>null</code>.
    */
   private String m_description = "";

   /**
    * Defines a value to use for the parameter if no value is supplied.
    * Never <code>null</code>.
    */
   private String m_defaultValue = "";

   /**
    * Holds the entries that make up the choice list for this parameter.  Will
    * be <code>null</code> if this parameter has not defined a choice list.
    */
   private List m_choiceList = null;

   /**
    * A single entry in the choice list
    */
   public class Entry
   {
      /**
       * Initializes a newly created <code>Entry</code> object, from
       * an XML representation.  See {@link #fromXml} for the format.
       *
       * @param sourceNode the XML element node to construct this object from.
       * Cannot be <code>null</code>.
       * @throws PSUnknownNodeTypeException if the XML representation is not
       * in the expected format.
       */
      public Entry (Element sourceNode) throws PSUnknownNodeTypeException
      {
         if (null == sourceNode)
            throw new IllegalArgumentException("sourceNode cannot be null.");
         this.fromXml( sourceNode );
      }


      /**
       * Populates the fields of this object from an XML representation.  The
       * format of the XML is specified in sys_LibraryControlDef.dtd:
       * <code><pre>
       * &lt;!ELEMENT psxctl:Entry (#PCDATA)>
       * &lt;!ATTLIST psxctl:Entry
       *    internalName CDATA #IMPLIED
       * >
       * </pre></code>
       *
       * @param sourceNode element with name specified by
       * {@link #XML_NODE_NAME}; cannot be <code>null</code>
       * @throws PSUnknownNodeTypeException if <code>sourceNode</code> fails
       * to {@link PSComponent#validateElementName validate}.
       */
      private void fromXml(Element sourceNode)
            throws PSUnknownNodeTypeException
      {
         PSComponent.validateElementName(sourceNode, XML_NODE_NAME);
         m_displayValue = PSXmlTreeWalker.getElementData(sourceNode);
         setInternalName( sourceNode.getAttribute("internalName") );
      }


      /**
       * Gets the value that should be passed to the parameter when this Entry
       * is selected.  If an internal name has not been explictly defined,
       * the display value is used (with spaces converted to underscores).
       * @return the value that should be passed to the parameter; never
       * <code>null</code>, may be empty.
       */
      public String getInternalName()
      {
         if (null == m_internalName)
         {
            String displayValue = getDisplayValue();
            return displayValue.replace( ' ', '_' );
         }
         return m_internalName;
      }


      /**
       * Sets the value used when this Entry is passed to a parameter
       *
       * @param internalName String to assign.  If empty, <code>null</code>
       * is assigned
       */
      public void setInternalName(String internalName)
      {
         // force empty to null
         if (internalName != null && internalName.trim().length() == 0)
            internalName = null;
         m_internalName = internalName;
      }


      /**
       * @return String representation of this object, obtained by
       * {@link #getDisplayValue}
       */
      public String toString()
      {
         return getDisplayValue();
      }


      /**
       * Gets the value that should be displayed when choosing between entries.
       * @return String; never <code>null</code>, may be empty
       */
      public String getDisplayValue()
      {
         return m_displayValue;
      }


      /**
       * The value that should be passed to the parameter when this Entry
       * is selected.  Set in {@link #fromXml} from the internalName attribute.
       * Will be <code>null</code> if not defined; never empty.
       */
      private String m_internalName = null;

      /**
       * The value that should be displayed when choosing which Entry to use.
       * Never <code>null</code>, may be empty.  Set in {@link #fromXml}.
       */
      private String m_displayValue;

      /** Name of parent XML node */
      private static final String XML_NODE_NAME = "psxctl:Entry";
   }
}
