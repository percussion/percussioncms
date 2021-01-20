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

import com.percussion.util.PSCharSets;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;

/**
 * The PSDataMapping class defines the mapping between an XML element or
 * attribute and its corresponding back-end column. JavaScript can also be
 * used in lieu of a back-end column. This allows an XML element or
 * attribute to be mapped to a dynamically computed value.
 * <p>
 * PSDataMapping objects are used in the PSDataMapper collection.
 *
 * @see PSDataMapper
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSDataMapping extends PSComponent
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
   public PSDataMapping(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSDataMapping()
   {
      super();
      m_conditionals = new PSCollection(
         com.percussion.design.objectstore.PSConditional.class);
   }

   /**
    * Constructs a mapping between an XML field (element or attribute) and
    * its corresponding back-end column. JavaScript can also be used in
    * lieu of a back-end column. This allows an XML field to be mapped
    * to a dynamically computed value.
    *
    * @param   xmlField    the name of the XML field being mapped
    * @param   backEndMap   the back-end object the XML field is being
    *                      mapped to
    */
   public PSDataMapping( java.lang.String xmlField,
      IPSBackEndMapping backEndMap)
   {
      this(
         (IPSDocumentMapping)PSReplacementValueFactory.
            getReplacementValueFromXmlFieldName(xmlField), backEndMap);
   }

   /**
    * Constructs a mapping between an XML field (element or attribute) and
    * its corresponding back-end column. JavaScript can also be used in
    * lieu of a back-end column. This allows an XML field to be mapped
    * to a dynamically computed value.
    *
    * @param   docMap       the document object to map
    *
    * @param   backEndMap   the back-end object to map
    */
   public PSDataMapping(IPSDocumentMapping docMap, IPSBackEndMapping backEndMap)
   {
      this();
      setDocumentMapping(docMap);
      setBackEndMapping(backEndMap);
   }

   /**
    * Get the name of the XML field (element or attribute) for which this
    * mapping is defined.
    *
    * @return      the name of the XML field being mapped
    */
   public java.lang.String getXmlField()
   {
      if (m_docMapping instanceof PSXmlField)
         return ((PSXmlField)m_docMapping).getName();
      else
         return null;
   }

   /**
    * Get the document mapping (Udf, Xml field/att) for which this
    * mapping is defined.
    *
    * @return      the document mapping mapped
    */
   public IPSDocumentMapping getDocumentMapping()
   {
      return m_docMapping;
   }

   /**
    * Set the document mapping (Udf, Xml field/att) for this
    * mapping.
    *
    * @param      the name of the XML field being mapped
    */
   public void setDocumentMapping(IPSDocumentMapping docMap)
   {
      m_docMapping = docMap;
   }

   /**
    * Set the name of the XML field (element or attribute) for which this
    * mapping is defined.
    * This is limited to 255 characters.
    *
    * @param   name         the name of the XML field being mapped
    */
   public void setXmlField(java.lang.String name)
   {
      setDocumentMapping(new PSXmlField(name));
   }

   /**
    * Get the back-end object the XML field is being mapped to. This can
    * be either a back-end column (PSBackEndColumn) or a JavaScript
    * user-defined function (PSExtensionCall).
    *
    * @return      the name of the XML element or attribute being mapped
    *
    * @see         IPSBackEndMapping
    * @see         PSBackEndColumn
    * @see         PSExtensionCall
    */
   public IPSBackEndMapping getBackEndMapping()
   {
      return m_backEndMapping;
   }

   /**
    * Set the back-end object the XML field is being mapped to. This can
    * be either a back-end column (PSBackEndColumn) or a JavaScript
    * user-defined function (PSExtensionCall).
    *
    * @param   backEndMap   the back-end object the XML field is being
    *                      mapped to
    *
    * @see      IPSBackEndMapping
    * @see      PSBackEndColumn
    * @see      PSExtensionCall
    */
   public void setBackEndMapping(IPSBackEndMapping backEndMap)
   {
      IllegalArgumentException ex = validateBackEndMapping(backEndMap);

      if (ex != null)
         throw ex;

      m_backEndMapping = backEndMap;
   }

   private static IllegalArgumentException validateBackEndMapping(
      IPSBackEndMapping backEndMap)
   {
      if (backEndMap == null)
         return new IllegalArgumentException("BackEndMapping exception: backEndMap is null");

      return null;
   }

   /**
    * Get the conditional statements associated with this object. This is
    * used to determine when the mapping should be used.
    * <p>
    * When using a back-end table with multiple purposes, it may not be
    * apparent how the data should be mapped. If we have two XML fields,
    * faxPhone and workPhone, these may come from the back-end table. For
    * instance, a phone table which uses a column to define whether the
    * phone number is for a fax or a work phone. The mappings would then
    * be defined as follows:
    * <table border="1">
    * <tr>   <th>XML Field</th>
    *       <th>Back-end Column</th>
    *       <th>Conditional</th></tr>
    * <tr>   <td>faxPhone</td>
    *       <td>phone.ph_no</td>
    *       <td>phone.ph_type = 'F'</td></tr>
    * <tr>   <td>workPhone</td>
    *       <td>phone.ph_no</td>
    *       <td>phone.ph_type = 'W'</td></tr>
    * </table>
    *
    * @return     the conditional statements
    *
    * @see         PSConditional
    */
   public PSCollection getConditionals()
   {
      return m_conditionals;
   }

   /**
    * Overwrite the conditional statements associated with this object.
    * If you only want to modify certain criteria, add a
    * new condition, etc. use getSelectionCriteria to get the existing
    * collection and modify the returned collection directly.
    * <p>
    * The PSCollection object supplied to this method will be stored with
    * the PSDataMapping object. Any subsequent changes made to the object by
    * the caller will also effect this object.
    * <p>
    * This is used to determine when the mapping should be used. Specifying
    * a conditional of null will allow this object to be used without
    * testing any conditions.
    * <p>
    * When using a back-end table with multiple purposes, it may not be
    * apparent how the data should be mapped. If we have two XML fields,
    * faxPhone and workPhone, these may come from the back-end table. For
    * instance, a phone table which uses a column to define whether the
    * phone number is for a fax or a work phone. The mappings would then
    * be defined as follows:
    * <table border="1">
    * <tr>   <th>XML Field</th>
    *       <th>Back-end Column</th>
    *       <th>Conditional</th></tr>
    * <tr>   <td>faxPhone</td>
    *       <td>phone.ph_no</td>
    *       <td>phone.ph_type = 'F'</td></tr>
    * <tr>   <td>workPhone</td>
    *       <td>phone.ph_no</td>
    *       <td>phone.ph_type = 'W'</td></tr>
    * </table>
    *
    * @param      conds            the new conditional statements
    *                              (PSConditional objects)
    *
    * @see         PSConditional
    */
   public void setConditionals(PSCollection conds)
   {
      if (conds != null) {
         if (!com.percussion.design.objectstore.PSConditional.class.isAssignableFrom(
            conds.getMemberClassType()))
         {
            throw new IllegalArgumentException("BackEndMapping exception: Data Mapping conds does not contain PSConditional objects");
         }
      }

      m_conditionals = conds;
   }

   /**
    * Get the text formatter associated with this mapping. When writing
    * data into an XML document, formatting may be important. By associating
    * a text formatter (XML data is always text) with the mapping we will
    * do the appropriate text conversion. It is important to create a
    * formatter of the appropriate type. If the source data will be a
    * number, be sure not to use a DateFormat object as this will cause
    * an IllegalArgumentException to be thrown at runtime.
    *
    * @return               the text formatter for this mapping (may be null)
    */
   public java.text.Format getTextFormatter()
   {
      return m_textFormatter;
   }

   /**
    * Set the text formatter associated with this mapping. When writing
    * data into an XML document, formatting may be important. By associating
    * a text formatter (XML data is always text) with the mapping we will
    * do the appropriate text conversion. It is important to create a
    * formatter of the appropriate type. If the source data will be a
    * number, be sure not to use a DateFormat object as this will cause
    * an IllegalArgumentException to be thrown at runtime.
    *
    * @param   formatter   the text formatter for this mapping (may be null)
    */
   public void setTextFormatter(java.text.Format formatter)
   {
      m_textFormatter = formatter;
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXDataMapping XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXDataMapping defines the mapping between an XML element or
    *       attribute and its corresponding back-end column. JavaScript can
    *       also be used in lieu of a back-end column. This allows an XML
    *       element or attribute to be mapped to a dynamically computed
    *       value. PSXDataMapping objects are used in the PSXDataMapper
    *       collection.
    *
    *       ObjectReferences:
    *
    *       PSXBackEndColumn or PSXUdfCall - the back-end object
    *       the XML field is being mapped to. This can be either a back-end
    *       column (PSXBackEndColumn) or a JavaScriptuser-defined function
    *       (PSXUdfCall).
    *
    *       PSXConditional - the conditional statement associated with this
    *       object. This is used to determine when the mapping should be
    *       used.
    *    --&gt;
    *    &lt;!ELEMENT PSXDataMapping   ((xmlField | PSXmlField | PSXHtmlParameter | PSXUdfCall),
    *                                     (PSXBackEndColumn | PSXUdfCall),
    *                                   Conditionals?, textFormatter?)&gt;
    *
    *    &lt;!--
    *       the name of the XML field (element or attribute) for which this
    *       mapping is defined.
    *    --&gt;
    *    &lt;!ELEMENT xmlField         (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the conditionals
    *    --&gt;
    *    &lt;!ELEMENT Conditionals      (PSXConditional*)&gt;
    *
    *    &lt;!--
    *       a serialized representation of the text formatting object
    *    --&gt;
    *    &lt;!ELEMENT textFormatter      (#PCDATA)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXDataMapping XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement (ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));
      root.setAttribute("groupId", String.valueOf(m_groupId));

      // A document mapping will represent the front end now.
      if (m_docMapping != null)
         root.appendChild(m_docMapping.toXml(doc));

      //private              PSExtensionCall   m_backEndMapping;
      if(m_backEndMapping != null )
         root.appendChild(((IPSComponent)m_backEndMapping).toXml(doc));

      // save all the conditionals now
      if(m_conditionals != null) {
         IPSComponent comp;
         Element node = PSXmlDocumentBuilder.addEmptyElement(
            doc, root, "Conditionals");
         int size = m_conditionals.size();
         for (int i = 0; i < size; i++) {
            comp = (IPSComponent)m_conditionals.get(i);
            node.appendChild(comp.toXml(doc));
         }
      }

      if (m_textFormatter != null)
      {
         /* store the text formatting object which tells us how to do
          * text conversions. It's stored as a Base64 encoded,
          * serialized object.
          */
         try {
            java.io.ByteArrayOutputStream bOut
               = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream objOut
               = new java.io.ObjectOutputStream(bOut);

            objOut.writeObject(m_textFormatter);
            objOut.flush();

            java.io.ByteArrayInputStream bIn
               = new java.io.ByteArrayInputStream(bOut.toByteArray());
            objOut.close();   // don't need this anymore

            // the objOut close may have closed this, so create a new one
            bOut = new java.io.ByteArrayOutputStream();

            com.percussion.util.PSBase64Encoder.encode(bIn, bOut);

            bIn.close();   // don't need this anymore

            // now write it to the XML file
            PSXmlDocumentBuilder.addElement( doc, root, "textFormatter",
               new String(bOut.toByteArray(), PSCharSets.rxJavaEnc()) );

            bOut.close();   // no longer need this either
         } catch (Exception e) {
            throw new RuntimeException("Text Formatter error: " + e.toString());
         }
      }

      return root;
   }

   /**
    * This method is called to populate a PSDataMapping Java object
    * from a PSXDataMapping XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXDataMapping
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try {
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

         sTemp = tree.getElementData("groupId");
         try {
            if ((sTemp == null) || (sTemp.length() == 0))
               m_groupId = 0;
            else
               m_groupId = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, "groupId", ((sTemp == null) ? "null" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         // A document mapping will represent the front end now.
         // this is the first child node of the PSXDataMapping element
         Element node = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (node == null) {
            Object[] args = { ms_NodeType, "documentMapping", "" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         try {
            if ("xmlField".equals(node.getTagName())) {
               // for backwards compatibility
               m_docMapping = (IPSDocumentMapping)PSReplacementValueFactory.
                  getReplacementValueFromXmlFieldName(
                  tree.getElementData(".", false));
            }
            else {
               try {
                  m_docMapping = (IPSDocumentMapping)createMappingObject(
                     node, "documentMapping", parentDoc, parentComponents);
               } catch (Exception e) {   // should only be a class cast, but...
                  Object[] args = { ms_NodeType, "documentMapping", node.getTagName() };
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
            }
         } catch (IllegalArgumentException e) {
              throw new PSUnknownNodeTypeException(0, e.getLocalizedMessage());
         }

         // the back-end mapping may be PSExtensionCall or PSBackEndColumn
         node = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         if (node == null) {
            Object[] args = { ms_NodeType, "backEndMapping", "" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         try {
            m_backEndMapping = (IPSBackEndMapping)createMappingObject(
               node, "backEndMapping", parentDoc, parentComponents);
         } catch (Exception e) {   // should only be a class cast, but...
            Object[] args = { ms_NodeType, "backEndMapping", node.getTagName() };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         // get all the conditionals now
         m_conditionals.clear();

         if ((node = tree.getNextElement("Conditionals", nextFlags)) != null)
         {
            PSConditional cond;
            String curNodeType = PSConditional.ms_NodeType;
            for (   Element curNode = tree.getNextElement(curNodeType, firstFlags);
               curNode != null;
               curNode = tree.getNextElement(curNodeType, nextFlags))
            {
               cond = new PSConditional(
                  (Element)tree.getCurrent(), parentDoc, parentComponents);
               m_conditionals.add(cond);
            }

            tree.setCurrent(node);   // reset the tree to find the next element
         }

         /* get the text formatting object which tells us how to do
          * text conversions. It's stored as a Base64 encoded,
          * serialized object.
          */
         m_textFormatter = null;
         if ((node = tree.getNextElement("textFormatter", nextFlags)) != null)
         {
            String serializedFormatter = tree.getElementData(".", false);
            if ((serializedFormatter != null) && (serializedFormatter.length() != 0))
            {
               try {
                  java.io.ByteArrayInputStream bIn
                     = new java.io.ByteArrayInputStream(
                     serializedFormatter.getBytes(PSCharSets.rxJavaEnc()));
                  java.io.ByteArrayOutputStream bOut
                     = new java.io.ByteArrayOutputStream();

                  com.percussion.util.PSBase64Decoder.decode(bIn, bOut);
                  bIn.close();   // don't need this anymore

                  bIn = new java.io.ByteArrayInputStream(bOut.toByteArray());
                  bOut.close();   // don't need this anymore

                  java.io.ObjectInputStream objIn
                     = new java.io.ObjectInputStream(bIn);
                  m_textFormatter = (java.text.Format)objIn.readObject();

                  bIn.close();
                  objIn.close();
               } catch (Exception e) {
                  Object[] args = { ms_NodeType, "textFormatter", e.toString() };
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
            }
         }
      } finally {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    *   Set the mapping's group Id
    *
    *   @param   groupId      The group Id
    *
    * @param    groupId
    */
   public void setGroupId (int groupId)
   {
      IllegalArgumentException ex = validateGroupId(groupId);
      if (ex != null)
         throw ex;

      m_groupId = groupId;
   }

   private static IllegalArgumentException validateGroupId(int groupId)
   {
      if (groupId < 0)
      {
        return new IllegalArgumentException("BackEndMapping exception: Data Mapping group ID:" +  groupId + "invalid");
      }
      return null;
   }

   /**
    *   get the mapping's group Id
    *
    *   @return      The group Id
    */
   public int getGroupId()
   {
      return m_groupId;
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

      IllegalArgumentException ex = validateBackEndMapping(m_backEndMapping);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      if (m_docMapping == null)
      {
         cxt.validationError(this,
            IPSObjectStoreErrors.DATAMAPPING_XML_FIELD_EMPTY, null);
      }

      ex = validateGroupId(m_groupId);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      // validate the child objects
      cxt.pushParent(this);

      try
      {
         if (m_docMapping != null)
            m_docMapping.validate(cxt);

         // An IPSDocumentMapping does not necessarily implement IPSComponent
         if (m_backEndMapping instanceof IPSComponent)
         {
            IPSComponent cpnt = (IPSComponent)m_backEndMapping;
            cpnt.validate(cxt);
         }

         if (m_conditionals != null)
         {
            for (int i = 0; i < m_conditionals.size(); i++)
            {
               Object o = m_conditionals.get(i);
               PSConditional cond = (PSConditional)o;
               cond.validate(cxt);
            }
         }
      }
      finally
      {
         cxt.popParent();
      }
   }


   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSDataMapping)) return false;
      if (!super.equals(o)) return false;
      PSDataMapping that = (PSDataMapping) o;
      return m_groupId == that.m_groupId &&
              Objects.equals(m_backEndMapping, that.m_backEndMapping) &&
              Objects.equals(m_conditionals, that.m_conditionals) &&
              Objects.equals(m_docMapping, that.m_docMapping) &&
              Objects.equals(m_textFormatter, that.m_textFormatter);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_groupId, m_backEndMapping, m_conditionals, m_docMapping, m_textFormatter);
   }

   private IPSComponent createMappingObject(
      Element mappingNode, String nodeSource,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      return (IPSComponent)
         PSReplacementValueFactory.getReplacementValueFromXml(
            parentDoc,
            parentComponents,
            mappingNode,
            mappingNode.getNodeName(),
            nodeSource);
   }


   private   int                  m_groupId = 0;
   private   IPSBackEndMapping      m_backEndMapping = null;
   private   PSCollection         m_conditionals = null;
   private   IPSDocumentMapping   m_docMapping;
   private   java.text.Format       m_textFormatter = null;

   private static final int      MAX_XML_FIELD_NAME_LEN   = 255;

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXDataMapping";
}
