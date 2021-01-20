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

import com.percussion.error.PSException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;


/**
 * The PSPageDataTank class is used to define the definition of the XML
 * document being used in a PSDataSet object. The definition may be a
 * Document Type Definition (DTD) or an XML file.
 *
 * @see PSDataSet
 * @see PSDataSet#getPageDataTank
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSPageDataTank extends PSComponent
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                              object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                              object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type
    */
   public PSPageDataTank(Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSPageDataTank()
   {
      super();
   }

   /**
    * Construct a page data tank object with the XML definition to use.
    *
    * @param      schemaSource   the URL of the DTD or XML file to use as
    *                            the XML document's schema
    */
   public PSPageDataTank(URL schemaSource)
   {
      super();
      setSchemaSource(schemaSource);
   }

   /**
    * Get the schema defining the XML document. This is either a DTD or
    * an XML file.
    *
    * @return     the schema defining the XML document
    */
   public URL getSchemaSource()
   {
      return m_schemaSource;
   }

   /**
    * Set the schema defining the XML document. This is either a DTD or
    * an XML file. The URL defining the schema must refer to a file which is
    * available to the E2 server at run-time. This can be on the E2 file
    * system, or on a web site which E2 can access. As such, validation
    * of the URL is not performed until the application is saved to the
    * server.
    *
    * @param      schemaSource   the URL of the DTD or XML file to use as
    *                            the XML document's schema
    */
   public void setSchemaSource(URL schemaSource)
   {
      IllegalArgumentException ex = validateSchemaSource(schemaSource);
      if (ex != null)
         throw ex;

      m_schemaSource = schemaSource;
   }

   private static IllegalArgumentException validateSchemaSource(
      URL schemaSource)
   {
      if (null == schemaSource)
         return new IllegalArgumentException("page tank schema is null");

      return null;
   }

   /**
    * Get the XML field defining the action to take on the XML objects
    * within the XML document.
    * <p>
    * When modifying the contents of an XML
    * document containing multiple objects, E2 needs to know which objects
    * are being updated, deleted, inserted or skipped. By specifying the
    * name of the field containing the action type, E2 can perform the
    * appropriate action. See the {@link PSRequestLink PSRequestLink}
    * class for more details.
    *
    * @return     the XML field, either a non-empty string or null
    */
   public String getActionTypeXmlField()
   {
      return m_xmlField;
   }

   /**
    * Set the XML field defining the action to take on the XML objects
    * within the XML document.
    * This is limited to 255 characters.
    * <p>
    * When modifying the contents of an XML
    * document containing multiple objects, E2 needs to know which objects
    * are being updated, deleted, inserted or skipped. By specifying the
    * name of the field containing the action type, E2 can perform the
    * appropriate action. See the {@link PSRequestLink PSRequestLink}
    * class for more details.
    *
    * @param      xmlField    the XML field to use as the action type
    *                           (may be null)
    */
   public void setActionTypeXmlField(String xmlField)
   {
      if ( null != xmlField && xmlField.length() == 0 )
         xmlField = null;

      IllegalArgumentException ex = validateActionTypeXmlField(xmlField);
      if (ex != null)
         throw ex;

      m_xmlField = xmlField;
   }

   /**
    * Validates the XML action type name against internal rules. A null element
    * is always valid.<p/>
    * Current rules:
    * <ul>
    *   <li>Length must be less than or equal to MAX_XML_FIELD_NAME_LEN</li>
    * </ul>
    *
    * @param xmlField The element name to validate. May be null.
    *
    * @return If validation fails, an exception of the appropriate type is
    * returned, otherwise null is returned.
    */
   private static IllegalArgumentException validateActionTypeXmlField(
      String xmlField)
   {
      if ( null != xmlField && xmlField.length() > MAX_XML_FIELD_NAME_LEN) {
         return new IllegalArgumentException("page tank XML field is too big" +
            MAX_XML_FIELD_NAME_LEN + " " + xmlField.length());
      }

      return null;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param tank a valid PSPageDataTank. 
    */
   public void copyFrom( PSPageDataTank tank )
   {
      copyFrom((PSComponent) tank );
      // assume the tank is valid
      m_schemaSource = tank.getSchemaSource();
      m_xmlField = tank.getActionTypeXmlField();
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXPageDataTank XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXPageDataTank is used to define the definition of the XML
    *       document being used in a PSXDataSet object. The definition
    *       may be a Document Type Definition (DTD) or an XML file.
    *    --&gt;
    *    &lt;!ELEMENT PSXPageDataTank      (schemaSource,
    *                                      actionTypeXmlField?)&gt;
    *
    *    &lt;!--
    *       the schema defining the XML document. This is either a DTD or an
    *       XML file. The URL defining the schema must refer to a file which
    *       is available to the E2 server at run-time. This can be on the E2
    *       file system, or on a web site which E2 can access.
    *    --&gt;
    *    &lt;!ELEMENT schemaSource         (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the XML field defining the action to take on the XML objects
    *       within the XML document. When modifying the contents of an XML
    *       document containing multiple objects, E2 needs to know which
    *       objects are being updated, deleted, inserted or skipped. By
    *       specifying the name of the field containing the action type,
    *       E2 can perform the appropriate action.
    *    --&gt;
    *    &lt;!ELEMENT actionTypeXmlField   (#PCDATA)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXPageDataTank XML element node
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      PSXmlDocumentBuilder.addElement(   doc, root, "schemaSource",
         m_schemaSource.toExternalForm());

      String xmlField = null == m_xmlField ? "" : m_xmlField;
      PSXmlDocumentBuilder.addElement(   doc, root, "actionTypeXmlField",
         xmlField);

      return root;
   }

   /**
    * This method is called to populate a PSPageDataTank Java object
    * from a PSXPageDataTank XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXPageDataTank
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

      sTemp = tree.getElementData("schemaSource");
      if ((sTemp == null) || (sTemp.length() == 0)) {
         Object[] args = { ms_NodeType, "schemaSource", "" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      try {
         m_schemaSource = new URL(sTemp);
      } catch(MalformedURLException e) {
         Object[] args = { ms_NodeType, "schemaSource",
                           "(URL: " + sTemp + ") " + e.getMessage() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      try {
         setActionTypeXmlField(tree.getElementData("actionTypeXmlField"));
      } catch (IllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "actionTypeXmlField",
                        new PSException (e.getLocalizedMessage()));
      }
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

      IllegalArgumentException ex = validateSchemaSource(m_schemaSource);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      ex = validateActionTypeXmlField(m_xmlField);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSPageDataTank)) return false;
      if (!super.equals(o)) return false;
      PSPageDataTank that = (PSPageDataTank) o;
      return Objects.equals(m_schemaSource, that.m_schemaSource) &&
              Objects.equals(m_xmlField, that.m_xmlField);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_schemaSource, m_xmlField);
   }

   // NOTE: when adding members, be sure to update the copyFrom method,
   // the to/fromXml methods, the equals method, the compareTo method,
   // and the validate method
   private              URL            m_schemaSource = null;

   /**
    * The full name of the XML element that contains the db action type (such
    * as insert, update and delete). It is either null, or contains an non-empty
    * string.
    */
   private              String                  m_xmlField = null;

   private static final int      MAX_XML_FIELD_NAME_LEN      = 255;

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXPageDataTank";
}

