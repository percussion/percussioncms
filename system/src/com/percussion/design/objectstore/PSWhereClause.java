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

import com.percussion.xml.PSXmlTreeWalker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSWhereClause class extends the concept of conditionals to
 * their application in a where clause. Where clause conditionals
 * take the form <code>{column} {operator} {value}</code>.
 * <p>
 * Where clauses also allow conditionals to be ignored when their value is
 * NULL. This allows a user to omit parameters to broaden their search. For
 * instance, a search may be defined by manufacturer and partnumber. If
 * a manufacturer only search is desired, the partnumber can be omitted
 * when this option is enabled. Otherwise, a search for NULL partnumbers will
 * be performed which will likely result in no data being returned.
 *
 * @see PSConditional
 * @see PSDataSelector#getWhereClauses
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSWhereClause extends PSConditional
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
   public PSWhereClause(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSWhereClause() {
      super();
   }

   /**
    * Construct a where clause object.
    *
    * @param name          the name of the variable to check
    *
    * @param op            the relational operator to use when comparing
    *
    * @param val            the value the variable must match
    *
    * @param omitWhenNull   <code>true</code> to omit the condition from the
    *                      WHERE clause when the value is NULL,
    *                      <code>false</code> otherwise
    */
   public PSWhereClause(IPSReplacementValue  name,
      java.lang.String op,
      IPSReplacementValue value,
      boolean omitWhenNull)
   {
      super(name, op, value);
      m_omitWhenNull = omitWhenNull;
   }

   /**
    * Is this condition omitted when the value is NULL?
    *
    * @return      <code>true</code> if the condition is omitted from the
    *             WHERE clause when the value is NULL,
    *             <code>false</code> otherwise
    */
   public boolean isOmittedWhenNull()
   {
      return m_omitWhenNull;
   }

   /**
    * Enable or disable omitting this condition when the value is NULL.
    *
    * @param enable         <code>true</code> to omit the condition from the
    *                      WHERE clause when the value is NULL,
    *                      <code>false</code> otherwise
    */
   public void setOmittedWhenNull(boolean enable)
   {
      m_omitWhenNull = enable;
   }

   public boolean equals(Object o)
   {
      boolean bEqual = true;
      if (!super.equals(o))
         bEqual = false;
      if ( bEqual && o instanceof PSWhereClause )
      {
         PSWhereClause other = (PSWhereClause) o;
         if ( m_omitWhenNull != other.m_omitWhenNull )
            bEqual = false;
      }
      return bEqual;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return super.hashCode();
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXWhereClause XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXWhereClause extends the concept of conditionals to their
    *       application in a WHERE clause. WHERE clause conditionals take
    *       the form {column} {operator} {value}.
    *
    *       WHERE clauses also allow conditionals to be ignored when their
    *       value is NULL. This allows a user to omit parameters to broaden
    *       their search. For instance, a search may be defined by
    *       manufacturer and partnumber. If a manufacturer only search is
    *       desired, the partnumber can be omitted when this option is
    *       enabled. Otherwise, a search for NULL partnumbers will be
    *       performed which will likely result in no data being returned.
    *
    *       Object References:
    *
    *       PSXConditional - the conditional information associated with the
    *       WHERE clause.
    *    --&gt;
    *    &lt;!ELEMENT PSXWhereClause   (PSXConditional)&gt;
    *
    *    &lt;!--
    *       omitWhenNull - is this condition omitted when the value is NULL?
    *    --&gt;
    *    &lt;!ATTLIST PSXWhereClause
    *       omitWhenNull    %PSXIsEnabled    #OPTIONAL
    *    &gt;
    * </code></pre>
    *
    * @return     the newly created PSXWhereClause XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      //private         boolean         m_omitWhenNull = false;
      root.setAttribute("omitWhenNull", m_omitWhenNull ? "yes" : "no");

      root.appendChild(super.toXml(doc));

      return root;
   }

   /**
    * This method is called to populate a PSWhereClause Java object
    * from a PSXWhereClause XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXWhereClause
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

         sTemp = sourceNode.getAttribute("omitWhenNull");
         m_omitWhenNull = (sTemp != null) && sTemp.equalsIgnoreCase("yes");

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         if (tree.getNextElement(PSConditional.ms_NodeType, firstFlags) == null) {
            Object[] args = { ms_NodeType, PSConditional.ms_NodeType, "" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         super.fromXml(
            (Element)tree.getCurrent(), parentDoc, parentComponents);
      } finally {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      super.validate(cxt);

      if (!cxt.startValidation(this, null))
         return;
   }

   private         boolean         m_omitWhenNull = false;

  /** public access on this so it can be referenced by other packages */
  public static final String ms_NodeType = "PSXWhereClause";
}

