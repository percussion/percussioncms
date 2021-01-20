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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates a transaction for installing a package (or element).
 */
public class PSTransactionSummary  implements IPSDeployComponent
{

   /**
    * Constructing the object from the given parameters.
    *
    * @param logId The id of a log summary this transaction is a part of. It
    * may not be less than 0.
    * @param depDesc The description of the installed dependency, it may not
    * be <code>null</code> or empty.
    * @param elementName  The name of the installed element. It may not be
    * <code>null</code> or empty.
    * @param elementType  The type of the installed element. It must be
    * one of the <code>TYPE_XXX</code> values.
    * @param action The action taken. Should be one of the 
    * <code>ACTION_XXX</code> values.
    *
    * @throws IllegalArgumentException If any parameter is invalid.
    */
   public PSTransactionSummary(int logId, String depDesc, String elementName,
      String elementType, int action)
   {
      if ( logId < 0 )
         throw new IllegalArgumentException("logId may not be less than 0");
      if ( depDesc == null || depDesc.trim().length() == 0)
         throw new IllegalArgumentException(
            "depDesc may not be null or empty");
      if ( elementName == null || elementName.trim().length() == 0)
         throw new IllegalArgumentException(
            "elementName may not be null or empty");
      if ( elementType == null || (! validateElementType(elementType)) )
      {
         throw new IllegalArgumentException("invalid elementType");
      }
      if (! isActionValid(action))
      {
         throw new IllegalArgumentException("Inavalid action");
      }

      m_logId = logId;
      m_depDesc = depDesc;
      m_elementName = elementName;
      m_elementType = elementType;
      m_action = action;
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSTransactionSummary(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the description of the dependency in the log.
    *
    * @return The description of the dependency in the log. It will never be
    * <code>null</code> or empty.
    */
   public String getDepDescription()
   {
      return m_depDesc;
   }

   /**
    * Get the name of the element in the log.
    *
    * @return The name of the element in the log. It will never be
    * <code>null</code> or empty.
    */
   public String getElement()
   {
      return m_elementName;
   }

   /**
    * The type of the element in the log.
    *
    * @return The type of the element in the log. It will be one of the
    * <code>TYPE_XXX</code> values.
    */
   public String getType()
   {
      return m_elementType;
   }

   /**
    * The action taken when installing the element/package.
    *
    * @return The action taken when installing the element/package. It will
    * be one of the <code>ACTION_XXX</code> values.
    */
   public int getAction()
   {
      return m_action;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXTransactionSummary EMPTY>
    * &lt;!ATTLIST PSXTransactionSummary
    *    depDesc CDATA #REQUIRED
    *    elementName CDATA #REQUIRED
    *    elementType CDATA #REQUIRED
    *    logId CDATA #REQUIRED
    *    action CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc should not be null");

      Element root = doc.createElement(XML_NODE_NAME);

      root.setAttribute(XML_ATTR_DEPDESC, m_depDesc);
      root.setAttribute(XML_ATTR_EL_NAME, m_elementName);
      root.setAttribute(XML_ATTR_EL_TYPE, m_elementType);
      root.setAttribute(XML_ATTR_ACTION, Integer.toString(m_action));
      root.setAttribute(XML_ATTR_LOGID, Integer.toString(m_logId));

      
      return root;
   }

   /**
    * Restores this object's state from its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode should not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      m_depDesc = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_DEPDESC);

      m_elementName = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_EL_NAME);

      String sLogId = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_LOGID);
      try
      {
         m_logId = Integer.parseInt(sLogId);
      }
      catch (NumberFormatException ne)
      {
         Object[] args = { XML_NODE_NAME, XML_ATTR_LOGID, sLogId };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      String sAction = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_ACTION);
      m_action = Integer.parseInt(sAction);
      if ( !isActionValid(m_action))
      {
         Object[] args = { XML_NODE_NAME, XML_ATTR_ACTION, sAction };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      m_elementType = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_EL_TYPE);
      if ( ! validateElementType(m_elementType) )
      {
         Object[] args = { XML_NODE_NAME, XML_ATTR_EL_TYPE, m_elementType };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
   }

   /**
    * Validating a given element type.
    *
    * @param type The element type to be checked, assume not <code>null</code>
    * or empty.
    *
    * @return <code>true</code> if the given <code>type</code> is one of the
    * <code>TYPE_XXX<code> values; <code>false</code> otherwise.
    */
   private boolean validateElementType(String type)
   {
      return type.equals(TYPE_DATA) || type.equals(TYPE_EXTENSION) ||
         type.equals(TYPE_FILE) || type.equals(TYPE_SCHEMA) || 
         type.equals(TYPE_CMS_OBJECT) || type.equals(TYPE_SKIPPED);
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");

      if ((obj instanceof PSTransactionSummary))
         throw new IllegalArgumentException("obj is not PSTransactionSummary");

      PSTransactionSummary obj2 = (PSTransactionSummary) obj;
      m_action = obj2.m_action;
      m_logId = obj2.m_logId;
      m_elementName = obj2.m_elementName;
      m_elementType = obj2.m_elementType;
      m_depDesc = obj2.m_depDesc;
   }

   // see IPSDeployComponent interface
   public int hashCode()
   {
      return m_logId + m_elementName.hashCode() + m_elementType.hashCode() +
         m_action + m_depDesc.hashCode();
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = false;

      if ((obj instanceof PSTransactionSummary))
      {
         PSTransactionSummary obj2 = (PSTransactionSummary) obj;
         isEqual =  (m_action == obj2.m_action) && (m_logId == obj2.m_logId)
            && m_elementName.equals(obj2.m_elementName)
            && m_elementType.equals(obj2.m_elementType) 
            && m_depDesc.equals(obj2.m_depDesc);
      }
      return isEqual;
   }


   /**
    * Determine if a given action is one of the <code>ACTION_XXX</code> values.
    * 
    * @param action the to be checked action value.
    * 
    * @return <code>true</code> if it is one of the <code>ACTION_XXX</code>
    * values; <code>false</code> otherwise.
    */
   public static boolean isActionValid(int action)
   {
      return (action == ACTION_CREATED || action == ACTION_MODIFIED ||
              action == ACTION_DELETED || 
              action == ACTION_SKIPPED_NO_OVERWRITE || 
              action == ACTION_SKIPPED_ALREADY_INSTALLED || 
              action == ACTION_FAILED_TO_INSTALL);
   }
   
   /**
    * The constant for file element type
    */
   public static final String TYPE_FILE = "file";

   /**
    * The constant for data element type
    */
   public static final String TYPE_DATA = "data";

   /**
    * The constant for schema element type
    */
   public static final String TYPE_SCHEMA = "schema";

   /**
    * The constant for skipped element type, value is "N/A".
    */
   public static final String TYPE_SKIPPED = "N/A";

   /**
    * The constant for extension element type
    */
   public static final String TYPE_EXTENSION = "extension";

   /**
    * The constant for cms object element type
    */
   public static final String TYPE_CMS_OBJECT = "object";
   
   /**
    * The constant for the created action of a transaction
    */
   public static final int ACTION_CREATED = 0;

   /**
    * The constant for the modified action of a transaction
    */
   public static final int ACTION_MODIFIED = 1;

   /**
    * The constant for the deleted action of a transaction
    */
   public static final int ACTION_DELETED = 2;

   /**
    * The constant for the skipped action of a transaction due to a no
    * overwrite policy for the specific dependency
    */
   public static final int ACTION_SKIPPED_NO_OVERWRITE = 3;

   /**
    * The constant for the skipped action of a transaction due to the previous
    * installation of the dependency within the same archive.
    */
   public static final int ACTION_SKIPPED_ALREADY_INSTALLED = 4;
   
   
   /**
    * The constant for the failed action.
    */
   public static final int ACTION_FAILED_TO_INSTALL = 5;
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXTransactionSummary";

   // Private strings for various XML attributes
   private static final String XML_ATTR_ACTION = "action";
   private static final String XML_ATTR_EL_NAME = "elementName";
   private static final String XML_ATTR_EL_TYPE = "elementType";
   private static final String XML_ATTR_LOGID = "logId";
   private static final String XML_ATTR_DEPDESC = "depDesc";

   /**
    * The identifier of a log summary this transaction is a part of.
    * Initialized by constructor, it will not be less than 0 after that.
    */
   private int m_logId;
   
   /**
    * The description of the installed dependency. Initialized by constructor,
    * will never by <code>null</code> or empty after that.
    */
   private String m_depDesc;
   
   /**
    * The identifier of the dependency for which the element is installed.
    * Initialized by constructor, it will never be <code>null</code>
    * or empty after that.
    */
   private String m_elementName;
   
   /**
    * The type of the installed element. Initialized by constructor, it will
    * be one of the <code>TYPE_XXX</code> values after that.
    */
   private String m_elementType;
   
   /**
    * Action taken. Initialized by constructor, it will be one of the
    * <code>ACTION_XXX</code> values after that.
    */
   private int m_action;
}
