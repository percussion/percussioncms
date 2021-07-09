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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSDisplayChoices;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSStringOperation;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Represents a single search field used in a given search/view.
 */
public class PSSearchField extends PSDbComponent
   implements IPSSequencedComponent
{
   /**
    * Creates the key required by the super.
    */
   private PSSearchField()
   {
      super(new PSKey(new String []
         {KEY_COL_FIELDNAME, KEY_COL_SEARCHID}));
   }

   /**
    * Required if object needs to be contained within
    * {@link com.percussion.cms.objectstore.PSDbComponentCollection}
    */
   public PSSearchField(Element src)
      throws PSUnknownNodeTypeException, PSCmsException
   {
      this();
      fromXml(src);
   }

   /**
    * Creates the key for this component. 
    * 
    * @param fieldName the name of the field, it may be <code>null</code>
    *    or empty if creating an empty key.
    * @param searchId the (parent/search) id. It is not used if the
    *    fieldName is <code>null</code> or empty.
    *    
    * @return the created key with persisted state, never <code>null</code>.
    */
   public static PSKey createKey(String fieldName, int searchId)
   {
      PSKey key = null;
      String[] keys = new String[]{KEY_COL_FIELDNAME, KEY_COL_SEARCHID};
      String[] values = new String[]{fieldName, String.valueOf(searchId)};

      if (null == fieldName || fieldName.trim().length() == 0)
         key = new PSKey(keys);
      else
         key = new PSKey(keys, values, true);

      return key;
   }


   /**
    * Standard ctor.
    *
    * @param strName. The fieldName. Never <code>null</code> or empty.
    *
    * @param strLabel. The field label. May be <code>null</code> or empty,
    *    if so, this defaults to <code>strName</code>.
    *
    * @param strType. The type. Never <code>null</code> or empty.
    *    See description <code>m_strFieldType</code>.
    *
    * @param strDesc. The description. May be <code>null</code> or empty.
    */
   public PSSearchField(String strName, String strLabel, String mnemonic,
      String strType, String strDesc)
   {
      this();

      if (strName == null || strName.trim().length() == 0)
         throw new IllegalArgumentException(
            "field name must not be null or empty");

      // Not validated below so we check here
      if (strName.length() > FIELDNAME_LENGTH)
         throw new IllegalArgumentException(
            "field name must not exceed " + FIELDNAME_LENGTH +
            "characters");

      if (strType == null || strType.trim().length() == 0)
         throw new IllegalArgumentException(
            "field type must not be null or empty");

      if (strLabel == null || strLabel.trim().length() == 0)
         strLabel = strName;

      // strName is part of the primary key
      setFieldDescription(strDesc);
      setFieldType(strType);
      setDisplayName(strLabel);
      setMnemonic(mnemonic);
      m_strFieldName = strName;
      setValues(m_strOperator, null, null);
      PSKey key = getLocator();
      key.setPart(KEY_COL_FIELDNAME, strName);
      setLocator(key);
   }

   //see base class for description
   protected String[] getKeyPartValues(IPSKeyGenerator gen)
   {
      return new String[] {getFieldName()};
   }

   //see base class for description
   public Element toXml(Document doc)
   {
      // base class handling
      Element root = super.toXml(doc);

      root.setAttribute(SEQUENCE_ATTR, "" + m_sequence);

      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_FIELDTYPE, 
         m_strFieldType);

      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_OPERATOR, 
         m_strOperator);

      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_EXTOPERATOR, 
         m_extOperator);

      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_FIELDLABEL, 
         m_strDisplayName);
      
      if (m_mnemonic.length() > 0)
         PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_FIELDMNEMONIC, 
            m_mnemonic);

      //we store all values in a single column, comma seperated
      if (m_values.size() > 0)
      {
         Iterator iter = m_values.iterator();
         StringBuffer buf = new StringBuffer();
         boolean haveValue = false;
         while (iter.hasNext())
         {
            String value = (String) iter.next();
            if (value.length() > 0)
            {
               // escape any commas
               value = PSStringOperation.replace(value, ",", ",,");
               buf.append(value);
               haveValue = true;
            }
            buf.append(",");
         }
         if (haveValue)
         {
            PSXmlDocumentBuilder.addElement( doc, root, XML_NODE_FIELDVALUE,
               buf.toString());
         }
      }

      if (m_strDescription.length() > 0)
      {
         PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_DESCRIPTION, 
            m_strDescription);
      }

      if (m_choices != null)
         root.appendChild(m_choices.toXml(doc));

      return root;
   }

   //see base class for description
   public void fromXml(Element e)
      throws PSUnknownNodeTypeException
   {
      // base class handling
      super.fromXml(e);

      //save state so we can restore when finished w/ updates
      int state = getState();

      setPosition(PSXMLDomUtil.checkAttributeInt(e, SEQUENCE_ATTR, false));

      //todo: ph - this is not safe
      // Read the field name from the primary key:
      m_strFieldName = getLocator().getPart(KEY_COL_FIELDNAME);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(e);

      Element elOp = tree.getNextElement(XML_NODE_OPERATOR);
      tree.setCurrent(e);
      if (elOp == null)
      {
         Object[] args = {getNodeName(), XML_NODE_OPERATOR, "null"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
            
      String op = PSXmlTreeWalker.getElementData(elOp);
      if (!isValidOperator(op))
      {
         Object[] args = {getNodeName(), XML_NODE_OPERATOR, op};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      
      // this one is optional for backward compatibility
      Element extElOp = tree.getNextElement(XML_NODE_EXTOPERATOR);
      tree.setCurrent(e);
      String extOp = PSXmlTreeWalker.getElementData(extElOp); // empty if not found
      
      Element elFieldType = tree.getNextElement(XML_NODE_FIELDTYPE);
      tree.setCurrent(e);
      if (elFieldType == null)
      {
         Object[] args = {getNodeName(), XML_NODE_FIELDTYPE, "null"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      m_strFieldType = PSXmlTreeWalker.getElementData(elFieldType);

      if (!isValidFieldType(m_strFieldType))
      {
         Object[] args = {getNodeName(), XML_NODE_FIELDTYPE, m_strFieldType};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      Element elFieldLabel = tree.getNextElement(XML_NODE_FIELDLABEL);
      tree.setCurrent(e);
      if (elFieldLabel == null)
      {
         Object[] args = {getNodeName(), XML_NODE_FIELDLABEL, "null"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      m_strDisplayName = PSXmlTreeWalker.getElementData(elFieldLabel);

      Element elFieldMnemonic = tree.getNextElement(XML_NODE_FIELDMNEMONIC);
      tree.setCurrent(e);
      if (elFieldMnemonic != null)
         setMnemonic(PSXmlTreeWalker.getElementData(elFieldLabel));

      // Optional
      Element elVal = tree.getNextElement(XML_NODE_FIELDVALUE);
      tree.setCurrent(e);

      List values = null;
      if (elVal != null)
      {
         String value = PSXmlTreeWalker.getElementData(elVal);
         if (null != value )
         {
            if (value.length() > 0)
            {
               values = PSStringOperation.getSplittedList(value, ',');
            }
            else
               values = new ArrayList();
         }
      }
      // method handles all cases
      setValues(op, extOp, values);

      Element elDesc = tree.getNextElement(XML_NODE_DESCRIPTION);
      tree.setCurrent(e);

      if (elDesc != null)
         m_strDescription = PSXmlTreeWalker.getElementData(elDesc);

      // optional keywords
      m_choices = null;
      Element elChoices = tree.getNextElement(PSDisplayChoices.XML_NODE_NAME);
      if (elChoices != null)
         m_choices = new PSDisplayChoices(elChoices);
      tree.setCurrent(e);

      //set state to match what it was in serialized object
      setState(state);
   }

   /**
    * Get the field's name.
    *
    * @return the field name, never <code>null</code> or
    *    empty.
    */
   public String getFieldName()
   {
      return m_strFieldName;
   }


   /**
    * Get the operator used for internal searches. If the operator is 
    * <code>OP_BETWEEN</code> or <code>OP_IN</code> then the result of 
    * {@link #getFieldValues()} will return multiple strings. If the operator is 
    * any other valid operator defined as the constant <code>OP_xxx</code> the 
    * result of {@link #getFieldValue()} will return a single string.  This 
    * operator is ignored if {@link #usesExternalOperator()} returns <code>
    * true</code>.
    *
    * @return The operator, never <code>null</code>, may be empty if an external
    * operator was supplied.
    */
   public String getOperator()
   {
      return m_strOperator;
   }
   
   /**
    * Get the operator used for searches against the external search engine
    * configured on the server.  
    * 
    * @return The operator, will be empty if {@link #usesExternalOperator()} 
    * returns <code>false</code>, never <code>null</code>.
    */
   public String getExternalOperator()
   {
      return m_extOperator;
   }

   /**
    * Tests whether this value is a valid operator.
    *
    * @param op Any value is allowed, including <code>null</code>.
    *
    * @return <code>true</code> if it is one of the OP_xxx values (case
    *    insensitive), otherwise <code>false</code>.
    */
   public boolean isValidOperator(String op)
   {
      return (normalizeOperator(op) != null);
   }

   /**
    * Normalize the supplied operator to the correct case.  Operator values are 
    * compared case-insensitively to allowable values, so this method can be
    * used to get the normalized (case-sensitive) match for the supplied 
    * operator (one of the <code>OP_XXX</code> values).
    *  
    * @param op The operator to normalize, may be <code>null</code> or empty.
    * 
    * @return The normalized operator value, or <code>null</code> if the 
    * supplied <code>op</code> is <code>null</code> or empty, or if a match with
    * a valid operator value is not found.
    */   
   private String normalizeOperator(String op)
   {
      String normalOp = null;
      
      if (op != null && op.trim().length() > 0)
      {
         op = op.trim().toLowerCase();

         String [] ops = new String []
            {
               OP_EQUALS,
               OP_NOTEQUAL,
               OP_LESSTHAN,
               OP_LESSTHANEQUAL,
               OP_GREATERTHAN,
               OP_GREATERTHANEQUAL,
               OP_ISNULL,
               OP_ISNOTNULL,
               OP_IN,
               OP_NOTIN,
               OP_LIKE,
               OP_NOTLIKE,
               OP_BETWEEN
            };

         for (int i=0; i<ops.length && normalOp == null; i++)
         {
            if (ops[i].equalsIgnoreCase(op))
               normalOp = ops[i];
         }
      }
      
      return normalOp;
   }

   /**
    * Set the field's operator.
    *
    * @param str never <code>null</code> or empty. Must be one of the 
    * <code>OP_XXX</code> values. If {@link #usesExternalOperator()} returns
    * <code>true</code>, this value is ignored.  The value supplied is 
    * compared case-insensitively to select the correct allowable value.
    */
   public void setOperator(String str)
   {
      if (str == null || str.trim().length() == 0)
         throw new IllegalArgumentException(
            "operator must not be null or empty");

      // Threshold
      if (str.equalsIgnoreCase(m_strOperator))
         return;

      String normalOp = normalizeOperator(str);
      if (normalOp == null)
         throw new IllegalArgumentException(
            "Invalid operator specified for search field");

      if (str.length() > OPERATOR_LENGTH)
         throw new IllegalArgumentException(
            "operator must not exceed " + OPERATOR_LENGTH +
            "characters.");

      setDirty();
      m_strOperator = str;
   }

   /**
    * Set the field's external operator.  This will cause this field to be 
    * searched using the external search engine configured on the server.  
    * May supply a <code>null</code> or empty value to clear this value and 
    * cause the field to be searched with the server's internal search engine.
    *
    * @param str may be <code>null</code> or empty, if not null its length may 
    * not exceed {@link #OPERATOR_LENGTH}.
    */
   public void setExternalOperator(String str)
   {
      if (str == null)
         str = "";
 
      if (str.length() > OPERATOR_LENGTH)
         throw new IllegalArgumentException(
            "operator must not exceed " + OPERATOR_LENGTH +
            "characters.");

      if (!m_extOperator.equals(str))
      {
         setDirty();
         m_extOperator = str;
      }
   }   

   /**
    * Determine if an external operator has been specified, causing this field
    * to be searched using the external search engine configured on the server.
    * 
    * @return <code>true</code> if one has been specified, <code>false</code>
    * if not.
    */
   public boolean usesExternalOperator()
   {
      return m_extOperator.trim().length() > 0;
   }

   /**
    * The position of this field relative to other fields being used for
    * searching. Fields are sequenced from left to right or top to bottom,
    * with the first index being 0. Defaults to 0. The order of columns that
    * have the same sequence value is implmenentation dependent.
    *
    * @return A value >= 0.
    */
   public int getPosition()
   {
      return m_sequence;
   }

   /**
    * See {@link #getPosition()} for details.
    *
    * @param pos Any value is allowed. If a value < 0 is supplied, 0 is used.
    */
   public void setPosition(int pos)
   {
      if (pos < 0)
         pos = 0;
      if (m_sequence == pos)
         return;
      m_sequence = pos;
      setDirty();
   }

   /**
    * Get the field's type.
    *
    * @return never <code>null</code> may be empty.
    */
   public String getFieldType()
   {
      return m_strFieldType;
   }


   /**
    * Set the field's value.
    *
    * @param must be one of the following values.
    *
    * <table>
    *    <tr>
    *       <th>Name</th>
    *       <th>Description</th>
    *    </tr>
    *    <tr>
    *       <td>TYPE_TEXT</td>
    *       <td>Text data type, string up to 255 characters</td>
    *    </tr>
    *    <tr>
    *       <td>NUMBER</td>
    *       <td>positive integer</td>
    *    </tr>
    *    <tr>
    *       <td>Date</td>
    *       <td>Date value</td>
    *    </tr>
    * </table>
    */
   public void setFieldType(String str)
   {
      if (str == null || str.trim().length() == 0)
         throw new IllegalArgumentException(
            "type must not be null or empty");

      // Threshold
      if (m_strFieldType.equalsIgnoreCase(str))
         return;

      if (!isValidFieldType(str))
         throw new IllegalArgumentException(
            "Invalid field type specified for search field");

      if (str.length() > FIELDTYPE_LENGTH)
         throw new IllegalArgumentException(
            "field type must not exceed " + FIELDTYPE_LENGTH +
            "characters");

      setDirty();
      m_strFieldType = str;
   }

   /**
    * Tests whether the supplied type is valid or not
    *
    * @return <code>true</code> if its a valid type, otherwise
    *    <code>false</code>
    */
   public boolean isValidFieldType(String str)
   {
      String [] container = new String []
         {
            TYPE_TEXT,
            TYPE_NUMBER,
            TYPE_DATE
         };

      boolean bFound = false;

      for (int i=0; i<container.length; i++)
      {
         if (container[i].equalsIgnoreCase(str))
         {
            bFound = true;
            break;
         }
      }

      if (!bFound)
         return false;

      return true;
   }

   /**
    * Convenience method. Nearly equivalent to calling {@link #getFieldValues()
    * getFieldValues().get(0)}. Differs if operator is OP_IS[NOT]NULL. In that
    * case, returns "" where getFieldValues() would return an empty list.
    *
    * @return Never <code>null</code> may be empty
    */
   public String getFieldValue()
   {
      return m_values.size() == 0 ? "" : (String) m_values.get(0);
   }

   /**
    * The content that is used on the RHS of the WHERE clause. If the operator
    * for this field is OP_[NOT]IN, or if an external operator was supplied 
    * (using one of the <code>setExternalFieldValue(s)</code> methods), then 
    * there can be from 1 to n entries. If OP_[NOT]BETWEEN, there will be 
    * exactly 2 entries. OP_IS[NOT]NULL will have exactly 0 entries.  All other 
    * operators will have exactly 1 entry. <p>No entry will ever be 
    * <code>null</code>, but they may be empty.
    *
    * @return List of Strings, never <code>null</code>, may be empty. The
    *    caller takes ownership of the returned list.
    */
   public List getFieldValues()
   {
      return (List) m_values.clone();
   }

   /**
    * Set the field's operator and values. It's the responsibility of the
    * caller to trim values if they don't want leading/trailing white space.
    *
    * @param op One of the <code>OP_xxx</code> values.  The value supplied is 
    * compared case-insensitively to select the correct allowable value.
    *
    * @param values What is used depends on the operator. The operator will
    *    read values from the supplied list until it has fulfilled its need or
    *    the end of the list is reached. If values is <code>null</code>, then
    *    empty values will be used. If any entry is <code>null</code>, then
    *    an empty value will be used.
    */
   public void setFieldValues(String op, List values)
   {
      if (op == null || op.trim().length() == 0)
         throw new IllegalArgumentException(
            "operator must not be null or empty");

      if (!isValidOperator(op))
         throw new IllegalArgumentException(
            "Invalid operator specified for search field");

      if (setValues(op, null, values))
         setDirty();
   }

   /**
    * Convenience method, equivalent to calling {@link #setFieldValues(String,
    * List) setFieldValues(op, (new ArrayList()).add(value))}.
    */
   public void setFieldValue(String op, String value)
   {
      // Validate value length
      if (value.length() > FIELDVALUE_LENGTH)
         throw new IllegalArgumentException(
            "field value must not exceed " + FIELDVALUE_LENGTH +
            "characters");

      List values = new ArrayList();
      values.add(value);
      setFieldValues(op, values);
   }

   /**
    * Set the field's operator and values to use when searching against an
    * external search engine. It's the responsibility of the
    * caller to trim values if they don't want leading/trailing white space.
    *
    * @param extOp The external operator, may not be <code>null</code> or empty.
    * Possible values are defined by the search engine configured on the server.
    *
    * @param values What is used depends on the operator. The operator will
    *    read values from the supplied list until it has fulfilled its need or
    *    the end of the list is reached. If values is <code>null</code>, then
    *    empty values will be used. If any entry is <code>null</code>, then
    *    an empty value will be used.
    */
   public void setExternalFieldValues(String extOp, List values)
   {
      if (extOp == null || extOp.trim().length() == 0)
         throw new IllegalArgumentException(
            "operator must not be null or empty");

      if (setValues(null, extOp, values))
         setDirty();
   }

   /**
    * Convenience method, equivalent to calling 
    * {@link #setExternalFieldValues(String, List) 
    * setExternalFieldValues(extOp, (new ArrayList()).add(value))}.
    */
   public void setExternalFieldValue(String extOp, String value)
   {
      // Validate value length
      if (value.length() > FIELDVALUE_LENGTH)
         throw new IllegalArgumentException(
            "field value must not exceed " + FIELDVALUE_LENGTH +
            "characters");

      List values = new ArrayList();
      values.add(value);
      setExternalFieldValues(extOp, values);
   }

   /**
    * Set the field's display name.
    *
    * @param str never <code>null</code>.
    */
   public void setDisplayName(String str)
   {
      if (str == null)
         throw new IllegalArgumentException("display name must not be null");

      // Threshold
      if (str.equalsIgnoreCase(m_strDisplayName))
         return;

      if (str.length() > FIELDLABEL_LENGTH)
         throw new IllegalArgumentException(
            "display name must not exceed " + FIELDLABEL_LENGTH +
            "characters");

      setDirty();
      m_strDisplayName = str;
   }

   /**
    * Get the display name.
    *
    * @return never <code>null</code> or empty.
    */
   public String getDisplayName()
   {
      return m_strDisplayName;
   }
   
   /**
    * Set a new mnemonic character.
    * 
    * @param mnemonic the new mnemonic character, may be <code>null</code> or
    *    empty, it's size must be <= 1.
    */
   public void setMnemonic(String mnemonic)
   {
      if (mnemonic == null)
         mnemonic = "";
      mnemonic = mnemonic.trim();
      if (mnemonic.length() > 1)
         throw new IllegalArgumentException("mnemonic size must be <= 1");
         
      m_mnemonic = mnemonic;
   }
   
   /**
    * Get the mnemonic.
    *
    * @return the mnemonic string, never <code>null</code> may be empty,
    *    it's size is always <= 1.
    */
   public String getMnemonic()
   {
      return m_mnemonic;
   }
   
   /**
    * Get the mnemonic character.
    *
    * @return the mnemonic character or 0 if none is defined
    */
   public char getMnemonicChar()
   {
      if (m_mnemonic.length() == 0)
         return 0;
         
      return m_mnemonic.charAt(0);
   }

   /**
    * Get the description of this field.
    *
    * @return never <code>null</code> may be empty.
    */
   public String getFieldDescription()
   {
      return m_strDescription;
   }

   /**
    * Set the field's description.
    *
    * @param str may be <code>null</code> to
    * specify the empty string.
    */
   public void setFieldDescription(String str)
   {
      if (str == null)
         str = "";
      str = str.trim();

      // Threshold
      if (str.equalsIgnoreCase(m_strDescription))
         return;

      if (str.length() > DESCRIPTION_LENGTH)
         throw new IllegalArgumentException(
            "description must not exceed " + DESCRIPTION_LENGTH +
            "characters");

      setDirty();
      m_strDescription = str;
   }

   /**
    * Because the field name is immutable, this method will not transfer that
    * property. See base class for further details.
    */
   public void copyFrom(IPSDbComponent src)
   {
      // Threshold - base class handling
      if (null == src || !getClass().isInstance(src))
         throw new IllegalArgumentException(
            "src must be a " + getClass().getName());

      /*m_strFieldName isn't copied because that property is immutable on an
         object. If we copied it, that contract would be broken. */

      PSSearchField other = (PSSearchField) src;

      // setters handle dirty flagging
      setDisplayName(other.getDisplayName());
      setFieldDescription(other.getFieldDescription());
      setFieldType(other.getFieldType());
      //don't call getFieldValues() because it clones
      setValues(other.getOperator(), other.getExternalOperator(), 
         other.m_values);
      m_sequence = other.m_sequence;
      m_choices = other.m_choices;
   }

   public boolean equals(Object obj)
   {
      // Threshold - base class handling
      if (!super.equals(obj))
         return false;

      PSSearchField s2 = (PSSearchField) obj;

      return m_strDescription.equals(s2.m_strDescription)
         && m_strDisplayName.equals(s2.m_strDisplayName)
         && m_mnemonic.equals(s2.getMnemonic())
         && m_strFieldType.equals(s2.m_strFieldType)
         && m_strFieldName.equals(s2.m_strFieldName)
         && m_values.equals(s2.m_values)
         && m_strOperator.equals(s2.m_strOperator)
         && m_extOperator.equals(s2.m_extOperator)
         && m_sequence == s2.m_sequence
         && compare(m_choices, s2.m_choices);
   }

   //see base class for description
   public Object clone()
   {
      PSSearchField copy = null;

      copy = (PSSearchField) super.clone();

      //don't call getFieldValues because that makes a clone
      copy.setValues(getOperator(), getExternalOperator(), m_values);

      return copy;
   }

   // see base class for description
   public int hashCode()
   {
      int nHash = super.hashCode();

      StringBuffer buf = new StringBuffer(m_strDescription.toLowerCase()
            + m_strFieldType + m_strFieldName.toLowerCase() + m_strOperator
            + m_sequence + m_extOperator);
      Iterator iter = m_values.iterator();
      while (iter.hasNext())
      {
         buf.append((String) iter.next());
      }
      return nHash + buf.toString().hashCode() + (m_choices != null ?
         m_choices.hashCode() : 0);
   }

   /**
    * Determine if this field has a list of keywords specified.
    *
    * @return <code>true</code> if they are specified, <code>false</code>
    * otherwise.
    */
   public boolean hasDisplayChoices()
   {
      return m_choices != null;
   }

   /**
    * Sets the choices to use for keyword entries for this field.
    * <p>
    * Note, this is transient data, it will not be saved into the database.
    *
    * @param choices The display choices to use for keyword support, may be
    * <code>null</code> to clear the choices.
    */
   public void setDisplayChoices(PSDisplayChoices choices)
   {
      m_choices = choices;
   }

   /**
    * Get the keyword choices for this field.
    * <p>
    * Note, this is transient data, it will not be saved into the database.
    *
    * @return The display choices object, may be <code>null</code> if
    * this field does not support keywords.
    */
   public PSDisplayChoices getDisplayChoices()
   {
      return m_choices;
   }

   /**
    * Just like {@link #setFieldValues(String,List)}, except it doesn't set
    * the dirty flag, it returns a boolean indicating whether this operation
    * changed the state of this instance.
    *
    * @param op Assumed to be a valid operator if supplied.  May be 
    * <code>null</code> or empty if <code>extOp</code> is not <code>null</code> 
    * or empty, in which case it is ignored.
    * @param extOp An operator to use to search using an external search engine,
    * may be <code>null</code> or empty if not using an external engine to 
    * search on this field.
    * @param values A list of objects to use as the value, may be 
    * <code>null</code> or empty, may contain null values.
    *
    * @return <code>true</code> if the state changed due to this call,
    *    <code>false</code> otherwise.
    */
   private boolean setValues(String op, String extOp, List values)
   {
      op = normalizeOperator(op);
      op = (op == null ? "" : op);
      extOp = (extOp == null ? "" : extOp);
      
      if (op.trim().length() == 0 && extOp.trim().length() == 0)
      {
         throw new IllegalArgumentException(
            "either op or extOp must not be null or emtpy");
      }
      
      boolean dirty = false;
      if (!m_strOperator.equalsIgnoreCase(op) || !m_extOperator.equals(extOp))      
      {
         dirty = true;
         // only overwrite op if provided
         if (op.trim().length() > 0)
            m_strOperator = op;
         m_extOperator = extOp;
      }

      if (values == null)
         values = new ArrayList();

      ArrayList newValues = new ArrayList();
      if (extOp.trim().length() > 0)
      {
         newValues.addAll(values);
      }
      else
      {
         /* We add empty values to fulfill the contract of getFieldValues() */
         if (op.equalsIgnoreCase(OP_BETWEEN) && values.size() < 2)
         {
            values.add("");
            if (values.size() < 2)
               values.add("");
         }
         else if (values.size() < 1)
            values.add("");

         Iterator iter = values.iterator();
         boolean finished = false;
         if (!(op.equalsIgnoreCase(OP_ISNULL) || 
            op.equalsIgnoreCase(OP_ISNOTNULL)))
         {
            while (iter.hasNext() && !finished)
            {
               String value = (String) iter.next();
               if (null == value)
                  value = "";

               newValues.add(value);
               //there is no limit for OP_IN, OP_NOTIN
               if (op.equalsIgnoreCase(OP_BETWEEN))
               {
                  if (newValues.size() == 2)
                     finished = true;
               }
            }
         }      
      }

      /* Order is not important for IN, but we won't worry about that minor
         optimization */
      if (!dirty && newValues.equals(getFieldValues()))
         dirty = false;
      else
      {
         m_values = newValues;
         dirty = true;
      }

      
      return dirty;
   }

   /**
    * Is this field of type text?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */   
   public boolean isTextValue()
   {
      return getFieldType().equalsIgnoreCase(TYPE_TEXT);
   }

   /**
    * Is this field of type number?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */   
   public boolean isNumberValue()
   {
      return getFieldType().equalsIgnoreCase(TYPE_NUMBER);
   }

   /**
    * Is this field of type date?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */   
   public boolean isDateValue()
   {
      return getFieldType().equalsIgnoreCase(TYPE_DATE);
   }

   /**
    * Get the field's name.
    *
    * @return the field name, never <code>null</code> or  <code>empty</code>.
    */
   public String toString()
   {
      return m_strFieldName;
   }

   // private defines
   private static final String KEY_COL_SEARCHID = "SEARCHID";
   private static final String KEY_COL_FIELDNAME = "FIELDNAME";
   // public defines
   public static final String XML_NODE_FIELDTYPE = "FIELDTYPE";
   public static final String XML_NODE_FIELDVALUE = "FIELDVALUE";
   public static final String XML_NODE_FIELDLABEL = "FIELDLABEL";
   public static final String XML_NODE_FIELDMNEMONIC = "FIELDMNEMONIC";
   public static final String XML_NODE_DESCRIPTION = "FIELDDESCRIPTION";
   public static final String XML_NODE_OPERATOR = "OPERATOR";
   public static final String XML_NODE_EXTOPERATOR = "EXTOPERATOR";
   public static final String SEQUENCE_ATTR = "sequence";

   public static final String TYPE_TEXT = "Text";
   public static final String TYPE_NUMBER = "Number";
   public static final String TYPE_DATE = "Date";

   public static final int FIELDNAME_LENGTH = 128;
   public static final int FIELDLABEL_LENGTH = 128;
   public static final int FIELDTYPE_LENGTH = 50;
   public static final int OPERATOR_LENGTH = 50;
   public static final int FIELDVALUE_LENGTH = 255;
   public static final int DESCRIPTION_LENGTH = 255;

   /**
    * Description of search field. Initialized in definition, never <code>
    * null</code> but may be empty.
    */
   private String m_strDescription = "";

   /**
    * Field type. Must be one of the following
    *
    * <table>
    *    <tr>
    *       <th>Name</th>
    *       <th>Description</th>
    *    </tr>
    *    <tr>
    *       <td>TYPE_TEXT</td>
    *       <td>Text data type, string up to 255 characters</td>
    *    </tr>
    *    <tr>
    *       <td>NUMBER</td>
    *       <td>positive integer</td>
    *    </tr>
    *    <tr>
    *       <td>Date</td>
    *       <td>Date value</td>
    *    </tr>
    * </table>
    * Defaults to TYPE_TEXT.
    */
   private String m_strFieldType = TYPE_TEXT;

   /**
    * See {@link #getFieldValues()} for description. Never <code>null</code>,
    * may be empty. The max size of the list is determined by the operator.
    * Note: ArrayList is used because we need to call clone on m_values.
    */
   private ArrayList m_values = new ArrayList();

   /**
    * Display name. Initialized in definition, never <code>null</code>
    * or empty.
    */
   private String m_strDisplayName = "";

   /**
    * Field name. Initialized in definition, never <code>null</code>
    * or empty, part of primary key.
    */
   private String m_strFieldName = "";

   /**
    * Operator attribute must be one of the following contansts beginning
    * with OP_xxx. Defaults to <code>OP_LIKE</code>.  May be emtpy if 
    * if {@link #m_extOperator} is not empty, never <code>null</code>.  Modified 
    * by {@link #setValues(String, String, List)}.
    */
   private String m_strOperator = OP_LIKE;

   /**
    * External operator supplied if using an external search engine to search on
    * this field.  Never <code>null</code>, may be empty if not using an 
    * external engine to search on this field.  Modified by 
    * {@link #setValues(String, String, List)}.
    */
   private String m_extOperator = "";

   /**
    * See {@link #getPosition()} for details.
    */
   private int m_sequence = 0;

   /**
    * The display choices object representing keyword choices for this field.
    * Modified by calls to <code>setDisplayChoices()</code>, may be
    * <code>null</code>.  This object is not persisted when this field is
    * saved.
    */
   private PSDisplayChoices m_choices = null;
   
   /**
    * The menmonic character. Initialized in constructor, never 
    * <code>null</code>, may be empty.
    */
   private String m_mnemonic = "";

   // public static defines for allowable operators as
   // currently defined in A_RHYTHMYX_ROOT/design/schemas/
   //sys_SearchParameters.xsd
   public static final String OP_EQUALS = "equal";
   public static final String OP_NOTEQUAL = "notEqual";
   public static final String OP_LESSTHAN = "lessThan";
   public static final String OP_LESSTHANEQUAL = "lessThanEqual";
   public static final String OP_GREATERTHAN = "greaterThan";
   public static final String OP_GREATERTHANEQUAL = "greaterThanEqual";
   public static final String OP_ISNULL = "isNull";
   public static final String OP_ISNOTNULL = "isNotNull";
   public static final String OP_IN = "in";
   public static final String OP_NOTIN = "notIn";
   public static final String OP_LIKE = "like";
   public static final String OP_NOTLIKE = "notLike";
   public static final String OP_BETWEEN = "between";
}

