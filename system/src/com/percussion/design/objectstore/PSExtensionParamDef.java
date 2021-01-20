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

import com.percussion.extension.IPSExtensionParamDef;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSExtensionParamDef class defines a basic parameter definition which can
 * be used with most exit handlers. Exits use the parameter definitions
 * to determine how the function will be called, including any data type
 * conversions.
 *
 * @see         com.percussion.extension.IPSExtensionDef
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSExtensionParamDef extends PSComponent implements IPSExtensionParamDef
{
   /**
    * 
    */
   private static final long serialVersionUID = -4878254025413256106L;

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    * 
    * @param sourceNode the XML element node to construct this object from
    * 
    * @param parentDoc the Java object which is the parent of this object
    * 
    * @param parentComponents the parent objects of this object
    * 
    * @exception PSUnknownNodeTypeException if the XML element node is not of
    * the appropriate type
    */
   public PSExtensionParamDef(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSExtensionParamDef() {
      super();
   }

   /**
    * Construct a parameter definition for use in an exit handler.
    *
    * @param   name      the name of the parameter
    * @param   dataType the data type of the parameter
    *
    * @see         #setName
    */
   public PSExtensionParamDef(  java.lang.String name,
      java.lang.String dataType)
   {
      super();
      setName(name);
      setDataType(dataType);
   }

   /**
    * Get the name of the parameter.
    *
    * @return            the name of the parameter
    */
   public java.lang.String getName()
   {
      return m_name;
   }

   /**
    * Set the name of the parameter. Be sure to follow the appropriate
    * naming convention for the exit handler this parameter will be used by.
    * This is limited to 128 characters.
    *
    * @param   name      the name of the parameter
    */
   public void setName(java.lang.String name)
   {
      IllegalArgumentException ex = validateName(name);
      if (ex != null)
         throw ex;

      m_name = name;
   }

   private static IllegalArgumentException validateName(String name)
   {
      if ((null == name) || (name.length() == 0))
         return new IllegalArgumentException("Exit param name empty");
      else if (name.length() > EXIT_PARAM_MAX_NAME_LEN)
      {
         @SuppressWarnings("unused")
         Object[] args = { new Integer(EXIT_PARAM_MAX_NAME_LEN),
            new Integer(name.length()) };
         return new IllegalArgumentException("Exit param name too big");
      }

      return null;
   }

   /**
    * Get the data type of the parameter.
    *
    * @return the datatype of the parameter, never <code>null</code>, might be
    *    empty.
    */
   public String getDataType()
   {
      return m_dataType;
   }

   /**
    * Set the data type of the parameter. Be sure to use a valid data type
    * for the exit handler this parameter will be used by. If this is not
    * specified, a string based type will be used by default.
    * This is limited to 128 characters.
    *
    * @param dataType the data type of the parameter, may be <code>null</code>
    *    or empty.
    */
   public void setDataType(String dataType)
   {
      IllegalArgumentException ex = validateDataType(dataType);
      if (ex != null)
         throw ex;

      if (dataType == null)
         m_dataType = "";
      else
         m_dataType = dataType;
   }

   private static IllegalArgumentException validateDataType(String dataType)
   {
      if ((null != dataType) && (dataType.length() > EXIT_PARAM_MAX_DT_LEN))
      {
         @SuppressWarnings("unused")
         Object[] args = { new Integer(EXIT_PARAM_MAX_DT_LEN),
            new Integer(dataType.length()) };
         return new IllegalArgumentException("Exit param name too big");
      }

      return null;
   }

   /**
    * Get the description of the parameter.
    *
    * @return the description of the parameter, never <code>null</code>, might
    *    be empty.
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    * Set the description of the parameter. Descriptions are commonly used
    * to provide details about the use of the parameter.
    *
    * @param desc the description of the parameter, may be <code>null</code>
    *    or empty. If <code>null</code> is provided it will be set to an empty
    *    String.
    */
   public void setDescription(String desc)
   {
      if (desc == null)
         m_description = "";
      else
         m_description = desc;
   }

   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXExtensionParamDef XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *     &lt;!--
    *           PSXExtensionParamDef defines a basic parameter definition which can be
    *           used with most exit handlers. Exits use the parameter definitions
    *           to determine how the function will be called, including any data
    *           type conversions.
    *     --&gt;
    *     &lt;!ELEMENT PSXExtensionParamDef   (name, dataType, description)&gt;
    *
    *     &lt;!--
    *           the name of the parameter. Be sure to follow the appropriate
    *           naming convention for the exit handler this parameter will be
    *           used by.
    *     --&gt;
    *     &lt;!ELEMENT name          (#PCDATA)&gt;
    *
    *     &lt;!--
    *           the data type of the parameter. Be sure to use a valid data type
    *           for the exit handler this parameter will be used by.
    *     --&gt;
    *     &lt;!ELEMENT dataType      (#PCDATA)&gt;
    *
    *     &lt;!--
    *           the description of the parameter. Descriptions are commonly used
    *           to provide details about the use of the parameter.
    *     --&gt;
    *     &lt;!ELEMENT description   (#PCDATA)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXExtensionParamDef XML element node
    */
   public Element toXml(Element root)
   {
      Document doc = root.getOwnerDocument();
      if (!root.getTagName().equals(ms_NodeType))
      {
         root = PSXmlDocumentBuilder.addEmptyElement(doc, root, ms_NodeType);
      }

      root.setAttribute("id", String.valueOf(m_id));

      //private      String          m_name = "";
      PSXmlDocumentBuilder.addElement(doc, root, "name", m_name);

      //private      String          m_dataType = "";
      PSXmlDocumentBuilder.addElement(doc, root, "dataType", m_dataType);

      //private      String          m_description = "";
      PSXmlDocumentBuilder.addElement(doc, root, "description", m_description);

      return root;
   }

   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      toXml(root);
      return root;
   }

   /**
    * This method is called to populate a PSExtensionParamDef Java object
    * from a PSXExtensionParamDef XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                                                                                                     of type PSXExtensionParamDef
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      if (false == ms_NodeType.equals (sourceNode.getNodeName()))
      {
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

      String sTemp = tree.getElementData("id");
      try {
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e) {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      try {      //private      String          m_name = "";
         setName(tree.getElementData("name"));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException(ms_NodeType + "name" + e);
      }

      try {      //private      String          m_dataType = "";
         setDataType(tree.getElementData("dataType"));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException(ms_NodeType + "dataType" + e);
      }

      setDescription(tree.getElementData("description"));
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws   PSValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      IllegalArgumentException ex = validateName(m_name);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateDataType(m_dataType);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

   }

   @Override
   public boolean equals(Object b)
   {
      if (!EqualsBuilder.reflectionEquals(this, b))
         return false;
      
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   private      String          m_name = "";
   private      String          m_description = "";
   private      String          m_dataType = "";

   private static final int      EXIT_PARAM_MAX_NAME_LEN      = 128;
   private static final int      EXIT_PARAM_MAX_DT_LEN      = 128;

   /* package access on this so they may reference each other in fromXml */
   public static final String   ms_NodeType            = "PSXExtensionParamDef";
}

