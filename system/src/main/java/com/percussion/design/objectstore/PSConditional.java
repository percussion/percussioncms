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

import com.percussion.error.PSException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;


/**
 * The PSConditional class is used to define conditionals. Conditionals
 * take the form
 * <code>{variable} {operator} {value} [{bool} {conditional}]</code>.
 * <P>
 * When using multiple conditionals (chaining conditionals) a boolean
 * operator must be specified on all but the last conditional. The boolean
 * operators currently supported are AND and OR. AND is the default
 * boolean operator. AND has a higher precedence than OR. All conditionals
 * joined by AND will be evaluated before the corresponding OR
 * conditionals. For instance, in the following example:
 * <P>
 * <TABLE>
 *       <TR>
 *          <TH>Name</TH>
 *          <TH>Operator</TH>
 *          <TH>Value</TH>
 *          <TH>Boolean</TH>
 *       </TR>
 *       <TR>
 *          <TD>products.status</TD>
 *          <TD>=</TD>
 *          <TD>'P'</TD>
 *          <TD>OR</TD>
 *       </TR>
 *       <TR>
 *          <TD>PSXUserContext/Logins/SecurityProvider</TD>
 *          <TD>=</TD>
 *          <TD>LDAP</TD>
 *          <TD>AND</TD>
 *       </TR>
 *       <TR>
 *          <TD>PSXUserContext/Logins/UserAttributes/ou</TD>
 *          <TD>=</TD>
 *          <TD>Engineering</TD>
 *          <TD></TD>
 *       </TR>
 * </TABLE>
 * <P>
 * Any product with a status of 'P' will be returned. In addition, any user
 * who logged in through LDAP and is part of the Engineering organizational
 * unit will get back all rows -- that is, with status set to any value.
 * If the AND conditions were not of higher precedence, the result set
 * would change. It would cause the first check to be if the status is 'P' or
 * the user logged in through LDAP. This filters correctly, but then we apply
 * the rule that they must also be in Engineering. This will cause
 * Engineering to get all records, as expected, but everyone outside of
 * Engineering will now get NO records, rather than records with a status
 * of 'P'.
 * <P>
 * One use of conditionals is in the PSRequestor object to check the input
 * data. If the input data meets the selection criteria, the request is
 * handled. Input data is often provided as an INPUT parameter defined on a
 * HTML FORM.
 *
 * @see PSResultPage#getConditionals
 * @see PSRequestor#getSelectionCriteria
 *
 * @author       Tas Giakouminakis
 * @version  1.0
 * @since       1.0
 */
public class PSConditional extends PSComponent
{
   /**
    * Use the boolean AND operator to join conditionals. AND has
    * a higher precedence than OR. This means all AND conditions will
    * be evaluated, then the ORs will be evaluated.
    */
   public static final java.lang.String OPBOOL_AND         = "AND";

   /**
    * Use the boolean OR operator to join conditionals. OR has
    * a lower precedence than AND. This means all AND conditions will
    * be evaluated, then the ORs will be evaluated.
    */
   public static final java.lang.String OPBOOL_OR         = "OR";

   /**
    *
    */
   public static final java.lang.String OPTYPE_EQUALS    = "=";

   /**
    *
    */
   public static final java.lang.String OPTYPE_NOTEQUALS = "<>";

   /**
    *
    */
   public static final java.lang.String OPTYPE_LESSTHAN = "<";

   /**
    *
    */
   public static final java.lang.String OPTYPE_LESSTHANOREQUALS = "<=";

   /**
    *
    */
   public static final java.lang.String OPTYPE_GREATERTHAN = ">";

   /**
    *
    */
   public static final java.lang.String OPTYPE_GREATERTHANOREQUALS = ">=";

   /**
    *
    */
   public static final java.lang.String OPTYPE_ISNULL = "IS NULL";

   /**
    *
    */
   public static final java.lang.String OPTYPE_ISNOTNULL = "IS NOT NULL";

   /**
    *
    */
   public static final java.lang.String OPTYPE_BETWEEN = "BETWEEN";

   /**
    *
    */
   public static final java.lang.String OPTYPE_NOTBETWEEN = "NOT BETWEEN";

   /**
    *
    */
   public static final java.lang.String OPTYPE_IN = "IN";

   /**
    *
    */
   public static final java.lang.String OPTYPE_NOTIN = "NOT IN";

   /**
    *
    */
   public static final java.lang.String OPTYPE_LIKE = "LIKE";

   /**
    *
    */
   public static final java.lang.String OPTYPE_NOTLIKE = "NOT LIKE";

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                                    object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                                    object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                                    if the XML element node is not of the
    *                                    appropriate type
    */
   public PSConditional(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSConditional()
   {

   }


   // see interface for description
   @Override
   public Object clone()
   {
      PSConditional copy = (PSConditional) super.clone();
      if (m_value != null)
         copy.m_value = (IPSReplacementValue) m_value.clone();
      if (m_variable != null)
      copy.m_variable = (IPSReplacementValue) m_variable.clone();
      return copy;
   }


   /**
    * Construct a conditional object.
    *
    * @param    name       the name of the variable to check
    * @param    op        the relational operator to use when comparing
    * @param    value    the value the variable must match
    * @param    bool       the boolean operator when chaining conditionals
    *
    *        @see          #setVariable
    */
   public PSConditional(IPSReplacementValue  name, java.lang.String op,
      IPSReplacementValue value, java.lang.String bool)
   {
      super();
      setVariable(name);
      setOperator(op);
      setValue(value);
      setBoolean(bool);
   }

   /**
    * Construct a conditional object.
    *
    * @param name    the name of the variable to check
    * @param op    the relational operator to use when comparing
    * @param value       the value the variable must match
    *
    *        @see          #setVariable
    */
   public PSConditional(IPSReplacementValue name, java.lang.String op,
      IPSReplacementValue value)
   {
      this(name, op, value, OPBOOL_AND);
   }

   /**
    * Get the name of the variable to check.
    *
    * @return       the name of the variable to check
    */
   public IPSReplacementValue getVariable()
   {
      return m_variable;
   }

   /**
    * Set the name of the variable to check.
    *
    * @param    name    the name of the variable to check
    */
   public void setVariable(IPSReplacementValue name)
   {
      if (null == name)
         throw new IllegalArgumentException("cond var name is empty");

      m_variable = name;
   }

   /**
    * Get the relational operator to use when comparing.
    *
    * @return       the relational operator (OPTYPE_xxx)
    */
   public java.lang.String getOperator()
   {
      return m_operator;
   }

   /**
    * @author   chadloder
    *
    * @version 1.22 1999/06/09
    *
    * Changes the operator to the logical negation of itself, and
    * returns the new (negated) operator.
    *
    * @return   String
    */
   public String negate()
   {
      // this method assumes that m_operator refers to the actual
      // static final string constants, not just equal to them
      if(m_operator == OPTYPE_EQUALS)
         m_operator = OPTYPE_NOTEQUALS;
      else if(m_operator == OPTYPE_NOTEQUALS)
         m_operator = OPTYPE_EQUALS;
      else if(m_operator == OPTYPE_LESSTHAN)
         m_operator = OPTYPE_GREATERTHANOREQUALS;
      else if(m_operator == OPTYPE_LESSTHANOREQUALS)
         m_operator = OPTYPE_GREATERTHAN;
      else if(m_operator == OPTYPE_GREATERTHAN)
         m_operator = OPTYPE_LESSTHANOREQUALS;
      else if(m_operator == OPTYPE_GREATERTHANOREQUALS)
         m_operator = OPTYPE_LESSTHAN;
      else if(m_operator == OPTYPE_ISNULL)
         m_operator = OPTYPE_ISNOTNULL;
      else if(m_operator == OPTYPE_ISNOTNULL)
         m_operator = OPTYPE_ISNULL;
      else if(m_operator == OPTYPE_BETWEEN)
         m_operator = OPTYPE_NOTBETWEEN;
      else if(m_operator == OPTYPE_NOTBETWEEN)
         m_operator = OPTYPE_BETWEEN;
      else if(m_operator == OPTYPE_IN)
         m_operator = OPTYPE_NOTIN;
      else if(m_operator == OPTYPE_NOTIN)
         m_operator = OPTYPE_IN;
      else if(m_operator == OPTYPE_LIKE)
         m_operator = OPTYPE_NOTLIKE;
      else if(m_operator == OPTYPE_NOTLIKE)
         m_operator = OPTYPE_LIKE;

      return m_operator;
   }

   /**
    * Set the relational operator to use when comparing.
    *
    * @param    op  the relational operator (OPTYPE_xxx)
    */
   public void setOperator(java.lang.String op)
   {
      // IMPORTANT: the operator should be set to refer to the
      // static final variables, so we can do reference comparisons
      // instead of equality comparisons
      if(op.equalsIgnoreCase(OPTYPE_EQUALS))
         m_operator = OPTYPE_EQUALS;
      else if(op.equalsIgnoreCase(OPTYPE_NOTEQUALS))
         m_operator = OPTYPE_NOTEQUALS;
      else if(op.equalsIgnoreCase(OPTYPE_LESSTHAN))
         m_operator = OPTYPE_LESSTHAN;
      else if(op.equalsIgnoreCase(OPTYPE_LESSTHANOREQUALS))
         m_operator = OPTYPE_LESSTHANOREQUALS;
      else if(op.equalsIgnoreCase(OPTYPE_GREATERTHAN))
         m_operator = OPTYPE_GREATERTHAN;
      else if(op.equalsIgnoreCase(OPTYPE_GREATERTHANOREQUALS))
         m_operator = OPTYPE_GREATERTHANOREQUALS;
      else if(op.equalsIgnoreCase(OPTYPE_ISNULL))
         m_operator = OPTYPE_ISNULL;
      else if(op.equalsIgnoreCase(OPTYPE_ISNOTNULL))
         m_operator = OPTYPE_ISNOTNULL;
      else if(op.equalsIgnoreCase(OPTYPE_BETWEEN))
         m_operator = OPTYPE_BETWEEN;
      else if(op.equalsIgnoreCase(OPTYPE_NOTBETWEEN))
         m_operator = OPTYPE_NOTBETWEEN;
      else if(op.equalsIgnoreCase(OPTYPE_IN))
         m_operator = OPTYPE_IN;
      else if(op.equalsIgnoreCase(OPTYPE_NOTIN))
         m_operator = OPTYPE_NOTIN;
      else if(op.equalsIgnoreCase(OPTYPE_LIKE))
         m_operator = OPTYPE_LIKE;
      else if(op.equalsIgnoreCase(OPTYPE_NOTLIKE))
         m_operator = OPTYPE_NOTLIKE;
      else
         throw new IllegalArgumentException("cond optype is unknown" + op);
   }

   /**
    * Get the value the variable must match.
    *
    * @return       the value the variable must match
    */
   public IPSReplacementValue getValue()
   {
      return m_value;
   }

   /**
    * Set the value the variable must match.
    *
    * @param    val the value the variable must match
    */
   public void setValue(IPSReplacementValue val)
   {
      m_value = val;
   }

   /**
    * Get the boolean operator to use when joining multiple conditionals.
    *
    * @return       the boolean operator (OPBOOL_xxx)
    */
   public java.lang.String getBoolean()
   {
      return m_boolean;
   }

   /**
    * Set the boolean operator to use when joining multiple conditionals.
    *
    * @param    bool    the boolean operator (OPBOOL_xxx) or <code>null</code>
    *                       to use the default (OPBOOL_AND)
    */
   public void setBoolean(java.lang.String bool)
   {
      if (bool == null) {
         m_boolean = OPBOOL_AND;
         return;
      }

      boolean valid = false;

      if(bool.equalsIgnoreCase(OPBOOL_AND))
         valid = true;
      else if(bool.equalsIgnoreCase(OPBOOL_OR))
         valid = true;

      if(false == valid)
         throw new IllegalArgumentException("cond bool unknown" + bool);

      m_boolean = bool;
   }


   // **************  IPSComponent Interface Implementation **************

   /**
    * This method is called to create a PSXConditional XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *        &lt;!--
    *           PSXConditional is used to define conditionals. Conditionals take
    *           the form: {variable} {operator} {value} [{boolean} {cond}]
    *
    *           One use of conditionals is in the PSXRequestor object to check
    *           the input data. If the input data meets the selection criteria,
    *           the request is handled. Input data is often provided as an INPUT
    *           parameter defined on a HTML FORM.
    *        --&gt;
    *        &lt;!ELEMENT PSXConditional (variable, operator, value, boolean?)&gt;
    *
    *        &lt;!--
    *           the name of the variable to check.
    *        --&gt;
    *        &lt;!ELEMENT variable          (PSXBackEndColumn | PSXLiteral |
    *                                          PSXCgiVariable | PSXHtmlParameter |
    *                                          PSXCookie | PSXUserContext |
    *                                          PSXXmlField)&gt;
    *
    *        &lt;!--
    *           supported conditional operators
    *        --&gt;
    *        &lt;!ENTITY % PSXConditionalOperator "(= | &lt;&gt; | &lt; | &lt;= | &gt; | &gt;= | IS NULL | IS NOT NULL | BETWEEN | NOT BETWEEN | IN | NOT IN | LIKE | NOT LIKE)"&gt;
    *
    *        &lt;!--
    *           the relational operator to use when comparing.
    *        --&gt;
    *        &lt;!ELEMENT operator          (%PSXConditionalOperator)&gt;
    *
    *        &lt;!--
    *           the value the variable must match.
    *        --&gt;
    *        &lt;!ELEMENT value             (PSXBackEndColumn | PSXLiteral |
    *                                          PSXCgiVariable | PSXHtmlParameter |
    *                                          PSXCookie | PSXUserContext |
    *                                          PSXXmlField)&gt;
    *
    *        &lt;!--
    *           supported boolean operators for joining multiple conditionals
    *        --&gt;
    *        &lt;!ENTITY % PSXConditionalBoolean "(AND | OR)"&gt;
    *
    *        &lt;!--
    *           the boolean operator to use when joining multiple conditionals
    *              (AND is used by default).
    *        --&gt;
    *        &lt;!ELEMENT operator          (%PSXConditionalBoolean)&gt;
    * </code></pre>
    *
    * @return      the newly created PSXConditional XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      // add the variable name, which is a tree of its own
      Element node = PSXmlDocumentBuilder.addEmptyElement(doc, root, "variable");
      node.appendChild(((IPSComponent)m_variable).toXml(doc));

      PSXmlDocumentBuilder.addElement(   doc, root, "operator", m_operator);

      // add the value, which is a tree of its own (the value can be null
      // in case of unary operators like IS NULL
      if (null != m_value)
      {
         node = PSXmlDocumentBuilder.addEmptyElement(doc, root, "value");
         node.appendChild(((IPSComponent)m_value).toXml(doc));
      }

      PSXmlDocumentBuilder.addElement(   doc, root, "boolean", m_boolean);

      return root;
   }

   /**
    * This method is called to populate a PSConditional Java object
    * from a PSXConditional XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception    PSUnknownNodeTypeException if the XML element node is not
    *                                            of type PSXConditional
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      List parentComponents)
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

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         // variable is also called name sometimes
         Node curNode = tree.getCurrent();   // need to reset later
         if (tree.getNextElement("variable", firstFlags) != null) {
            try{
               // the next element must contain our variable
               setVariable(PSReplacementValueFactory.getReplacementValueFromXml(
                  parentDoc, parentComponents,
                  tree.getNextElement(true), ms_NodeType, "variable"));
            } catch(IllegalArgumentException e) { //should never happen
               System.err.println(e.getLocalizedMessage());
            }
         }
         tree.setCurrent(curNode);   // reset to avoid failure in value get

         try {
            setOperator(tree.getElementData("operator"));
         } catch (IllegalArgumentException e) {
            throw new PSUnknownNodeTypeException(ms_NodeType, "operator",
                        new PSException (e.getLocalizedMessage()));
         }

         // get the "value" element which is the child of the "PSXConditional"
         // element. "value" element can also be a child of
         // "PSXFunctionParamValue" element. To obtain the correct "value"
         // element (child element of "PSXConditional"), get the first child
         // of "PSXConditional" and then use the tree walker to get the "value"
         // element by specifying the flag
         // PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
         Element valueEl = null;
         Node firstChild = sourceNode.getFirstChild();
         if (firstChild != null)
         {
            tree.setCurrent(firstChild);
            valueEl = tree.getNextElement(
               "value", PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }

         if (valueEl != null)
         {
            // the next element must contain our variable
            setValue(PSReplacementValueFactory.getReplacementValueFromXml(
               parentDoc, parentComponents,
               tree.getNextElement(true), ms_NodeType, "value"));
         }

         try {
            setBoolean(tree.getElementData("boolean"));
         } catch (IllegalArgumentException e) {
            throw new PSUnknownNodeTypeException(ms_NodeType, "boolean",
                        new PSException (e.getLocalizedMessage()));
         }
      } finally {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Returns the string representation of this object.
    */
   public String toString()
   {
      String condStr = m_variable.getValueText() + " " + m_operator + " ";
      if(m_value != null)
         condStr += m_value.getValueText() + " ";
      condStr += m_boolean;
      return condStr;
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
      if (!cxt.startValidation(this, null))
         return;

      if (   m_operator.equalsIgnoreCase(OPTYPE_EQUALS)
         ||   m_operator.equalsIgnoreCase(OPTYPE_NOTEQUALS)
         || m_operator.equalsIgnoreCase(OPTYPE_LESSTHAN)
         || m_operator.equalsIgnoreCase(OPTYPE_LESSTHANOREQUALS)
         || m_operator.equalsIgnoreCase(OPTYPE_GREATERTHAN)
         || m_operator.equalsIgnoreCase(OPTYPE_GREATERTHANOREQUALS)
         || m_operator.equalsIgnoreCase(OPTYPE_ISNULL)
         || m_operator.equalsIgnoreCase(OPTYPE_ISNOTNULL)
         || m_operator.equalsIgnoreCase(OPTYPE_BETWEEN)
         || m_operator.equalsIgnoreCase(OPTYPE_NOTBETWEEN)
         || m_operator.equalsIgnoreCase(OPTYPE_IN)
         || m_operator.equalsIgnoreCase(OPTYPE_NOTIN)
         || m_operator.equalsIgnoreCase(OPTYPE_LIKE)
         || m_operator.equalsIgnoreCase(OPTYPE_NOTLIKE))
      {
      }
      else
      {
         cxt.validationError(
            this,
            IPSObjectStoreErrors.COND_OPTYPE_UNKNOWN,
            m_operator);
      }

      if (!m_boolean.equals(OPBOOL_AND) && !m_boolean.equals(OPBOOL_OR))
      {
         cxt.validationError(
            this,
            IPSObjectStoreErrors.COND_BOOL_UNKNOWN,
            m_boolean);
      }

      if (m_value == null && !isUnaryOp(m_operator))
      {
         cxt.validationError(
            this,
            IPSObjectStoreErrors.COND_VALUE_NULL,
            m_operator);
      }

      cxt.pushParent(this);
      try
      {
         if (m_variable instanceof IPSComponent)
         {
            IPSComponent varCpnt = (IPSComponent)m_variable;
            varCpnt.validate(cxt);
         }

         if (m_value instanceof IPSComponent)
         {
            IPSComponent valCpnt = (IPSComponent)m_value;
            valCpnt.validate(cxt);
         }
      }
      finally
      {
         cxt.popParent();
      }
   }

   public boolean equals(Object o)
   {
      if (!(o instanceof PSConditional))
         return false;

      PSConditional other = (PSConditional)o;

      if (m_variable == null || other.m_variable == null)
      {
         if (m_variable != null || other.m_variable != null)
            return false;
      }
      else if (!m_variable.equals(other.m_variable))
         return false;

      if (m_operator == null || other.m_operator == null)
      {
         if (m_operator != null || other.m_operator != null)
            return false;
      }
      else if (!m_operator.equals(other.m_operator))
         return false;

      if (m_value == null || other.m_value == null)
      {
         if (m_value != null || other.m_value != null)
            return false;
      }
      else if (!m_value.equals(other.m_value))
         return false;

      if (m_boolean == null || other.m_boolean == null)
      {
         if (m_boolean != null || other.m_boolean != null)
            return false;
      }
      else if (!m_boolean.equals(other.m_boolean))
         return false;

      return true;
   }

   public boolean isUnary()
   {
      if (m_operator == null)
         return false;
      else
         return isUnaryOp(m_operator);
   }

   static public boolean isUnaryOp(String str)
   {
      return (str.equalsIgnoreCase(OPTYPE_ISNULL)
         || str.equalsIgnoreCase(OPTYPE_ISNOTNULL));
   }

   /** the variable whose value is compared */
   private IPSReplacementValue m_variable;

   /** the operator used to compare */
   private String m_operator;

   /** the value compared with, may be <code>null</code> */
   private IPSReplacementValue m_value;

   /** the boolean joining operator */
   private String m_boolean = OPBOOL_AND;

   private static final int         MAX_VAR_NAME_LEN   = 255;

  /* public access on this so it can be referenced by other packages */
  public static final String ms_NodeType = "PSXConditional";

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      int sum = 0;

      if (m_variable != null)
      {
         sum += m_variable.hashCode();
      }

      if (m_boolean != null)
      {
         sum += m_boolean.hashCode();
      }

      if (m_operator != null)
      {
         sum += m_operator.hashCode();
      }

      if (m_value != null)
      {
         sum += m_value.hashCode();
      }

      return sum;
   }

}








