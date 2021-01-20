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
 * Encapsulates a package name, package type and summary's log id.
 */
public class PSArchivePackage  implements IPSDeployComponent
{
   /**
    * Constructing the object with given parameters.
    *
    * @param pkgName The name of a package, may not be <code>null</code> or
    * empty.
    * @param pkgType The type of a package, may not be <code>null</code> or
    * empty..
    * @param status The status of a package, should be one of the
    * <code>STATUS_XXX</code> values.
    * @param logId the id of the log for this package in the log summary table,
    * <code>-1</code> if no log for this package exists
    *
    * @throws IllegalArgumentException If any parameter is invalid.
    */
   public PSArchivePackage(String pkgName, String pkgType, int status, 
      int logId)
   {
      if ( pkgName == null || pkgName.trim().length() == 0 )
         throw new IllegalArgumentException("pkgName may not be null or empty");
      if ( pkgType == null || pkgType.trim().length() == 0 )
         throw new IllegalArgumentException("pkgType may not be null or empty");
      if (! validateStatus(status))
         throw new IllegalArgumentException("status value is not a valid");

      m_name = pkgName;
      m_type = pkgType;
      m_status = status;
      m_logId = logId;
   }

   /**
    * Validating a given status.
    *
    * @param status The to be checked status value.
    *
    * @return <code>true</code> if the <code>status</code> is one of the
    * <code>STATUS_XXX<</code> values; <code>false</code>otherwise.
    */
   public static boolean validateStatus(int status)
   {
      return (status == STATUS_COMPLETED || status == STATUS_ABORTED ||
         status == STATUS_IN_PROGRESS);
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
   public PSArchivePackage(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }
   
   /**
    * Get the id of the log for this package in the log summary table.
    * 
    * @return the id of the log for this package in the log summary table,
    * <code>-1</code> if no log for this package exists
    */
   public int getLogId()
   {
      return m_logId;
   }
   
   /**
    * Determines if a log exists for this package.
    * 
    * @return <code>true</code> if there is a log, <code>false</code> if not.
    */
   public boolean hasLog()
   {
      return m_logId != -1;
   }

   /**
    * Determines if the object has been successfully installed or not.
    *
    * @return <code>true</code> if it has been installed, <code>false</code>
    * if installation failed or was never attempted.
    */
   public boolean isInstalled()
   {
      return (m_status == STATUS_COMPLETED);
   }

   /**
    * Determines if the installation of the object was attempted and aborted
    * due to a failure of some kind.  
    * 
    * @return <code>true</code> if it failed, <code>false</code> if it succeeded
    * or if installation was never attempted (due to skipping the package or
    * aborting installation due to a failure in a previous package).
    */
   public boolean isFailed()
   {
      return (m_status == STATUS_ABORTED);
   }

   /**
    * Gets the status.
    *
    * @return The status, will be one of the <code>STATUS_XXXX</code> values.
    */
   public int getStatus()
   {
      return m_status;
   }


   /**
    * Gets the name of the package.
    *
    * @return The name of the package, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Gets the name of the package.
    *
    * @return The type of the package, never <code>null</code> or empty.
    */
   public String getType()
   {
      return m_type;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXArchivePackage EMPTY >
    * &lt;!ATTLIST PSXArchivePackage
    *    name CDATA #REQUIRED
    *    type CDATA #REQUIRED
    *    status CDATA #REQUIRED
    *    logId CDATA #REQUIRED
    * >
    * </code>/<pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc should not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, m_name);
      root.setAttribute(XML_ATTR_TYPE, m_type);
      root.setAttribute(XML_ATTR_STATUS, Integer.toString(m_status));
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
      m_name = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_NAME);
      m_type = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_TYPE);
      String sStatus = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
            XML_ATTR_STATUS);
      String slogId = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
            XML_ATTR_LOGID);
            
      String intString = sStatus;       // for exception argument
      String intAttr = XML_ATTR_STATUS; // for exception argument
      try
      {
         m_status = Integer.parseInt(sStatus);
         
         intString = slogId;
         intAttr = XML_ATTR_LOGID;
         m_logId = Integer.parseInt(slogId);
      }
      catch (NumberFormatException ne)
      {
         Object[] args = { XML_NODE_NAME, intAttr, intString };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");

      if ((obj instanceof PSArchivePackage))
         throw new IllegalArgumentException("obj is not PSXArchivePackage");

      PSArchivePackage other = (PSArchivePackage) obj;
      m_name = other.m_name;
      m_type = other.m_type;
      m_status = other.m_status;
      m_logId = other.m_logId;
   }

   // see IPSDeployComponent interface
   public int hashCode()
   {
      return m_name.hashCode() + m_status + m_type.hashCode() + m_logId;
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = false;

      if ((obj instanceof PSArchivePackage))
      {
         PSArchivePackage other = (PSArchivePackage) obj;
         isEqual = m_name.equals(other.m_name) &&
                   m_type.equals(other.m_type) &&
                   (m_status == other.m_status) &&
                   (m_logId == other.m_logId) ;
      }
      return isEqual;
    }

   /**
    * The completed status for a deployed package
    */
   public static final int STATUS_COMPLETED = 0;

   /**
    * The aborted status for a deployed package
    */
   public static final int STATUS_ABORTED = 1;

   /**
    * The In progress status for a deployed package
    */
   public static final int STATUS_IN_PROGRESS = 2;

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXArchivePackage";

   // Private XML node and attribute names
   private static final String XML_ATTR_NAME = "name";
   private static final String XML_ATTR_TYPE = "type";
   private static final String XML_ATTR_STATUS = "status";
   private static final String XML_ATTR_LOGID = "logId";

   /**
    * The name of the package. Initialized by constructor, will never be
    * <code>null</code> or empty after that.
    */
   private String m_name;

   /**
    * The type of the package. Initialized by constructor, will never be
    * <code>null</code> or empty after that.
    */
   private String m_type;

   /**
    * The status of the package. Initialized by constructor to be one of
    * the <code>STATUS_XXXX</code> values after that.
    */
   private int m_status;

   /**
    * The id for the log summary table. Initialized by constructor, never
    * modified after that.
    */
   private int m_logId;
}
