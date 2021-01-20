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
import com.percussion.util.PSFormatVersion;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;
import java.util.Objects;

/**
 * Contains all high level info describing an archive file.
 */
public class PSArchiveInfo implements IPSDeployComponent
{

   /**
    * Construct this object from its XML representation.
    *
    * @param src The source element, may not be <code>null</code>.  See
    * {@link #toXml(Document)} for the format expected.
    *
    * @throws IllegalArgumentException if <code>src</code> is <code>null</code>.
    * @throws PSUnknownNodeTypeException if <code>src</code> is malformed.
    */
   public PSArchiveInfo(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");
      fromXml(src);
   }

   /**
    * Creates a new instance with all required parameters.
    *
    * @param archiveRef The name used to identify the archive, may not be
    * <code>null</code> or empty.
    * @param serverName The name of the server on which the archive is being
    * created.  May not be <code>null</code> or empty.
    * @param serverInfo The server version information, may not be
    * <code>null</code>.
    * @param repository The source repository dbms info, may not be
    * <code>null</code>.
    * @param userName The name of the user who initiated the request to create
    * this archive.  May not be <code>null</code> or empty.
    * @param category The category of the archive, may not be <code>null</code>
    * or empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSArchiveInfo(String archiveRef, String serverName,
      PSFormatVersion serverInfo, PSDbmsInfo repository, String userName,
      String category)
   {
      if (archiveRef == null || archiveRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "archiveRef may not be null or empty");

      if (serverName == null || serverName.trim().length() == 0)
         throw new IllegalArgumentException(
            "serverName may not be null or empty");

      if (serverInfo == null)
         throw new IllegalArgumentException("serverInfo may not be null");

      if (repository == null)
         throw new IllegalArgumentException("repository may not be null");

      if (userName == null || userName.trim().length() == 0)
         throw new IllegalArgumentException(
            "userName may not be null or empty");
      
      if (category == null || category.trim().length() == 0)
         throw new IllegalArgumentException(
            "category may not be null or empty");

      m_created = (new Date().getTime());
      m_archiveRef = archiveRef;
      m_serverName = serverName;
      m_serverInfo = serverInfo;
      m_repository = repository;
      m_userName = userName;
      m_category = category;
      
   }

   /**
    * Constructs or clone the object from another object.
    *
    * @param other The object to be cloned from, may not <code>null</code>.
    */
   public PSArchiveInfo(PSArchiveInfo other)
   {
      if (other == null)
         throw new IllegalArgumentException("other may not be null");

      copyFrom(other);
   }

   /**
    * Gets the version of the server where this archive was created.
    *
    * @return The version, never <code>null</code> or empty.
    */
   public String getServerVersion()
   {
      return m_serverInfo.getVersion();
   }

   /**
    * Gets the build date of the server where this archive was created.
    *
    * @return The date, never <code>null</code>.
    */
   public Date getServerBuildDate()
   {
      return m_serverInfo.getBuildDate();
   }
   
   /**
    * Gets the build date of the server where this archive was created in the 8 
    * digit form YYYYMMDD.
    *
    * @return The build number, never <code>null</code> or empty.
    */
   public String getServerBuildNumber()
   {
      return m_serverInfo.getBuildNumber();
   }

   /**
    * Gets the build id of the server where this archive was created.
    *
    * @return The build id, never <code>null</code> or empty.
    */
   public String getServerBuildId()
   {
      return m_serverInfo.getBuildId();
   }

   /**
    * Gets the name of the server where this archive was created.
    *
    * @return The name, never <code>null</code> or empty.
    */
   public String getServerName()
   {
      return m_serverName;
   }

   /**
    * Get the name used to identify this archive on the server.
    *
    * @return The name, never <code>null</code> or empty.
    */
   public String getArchiveRef()
   {

      return m_archiveRef;
   }

   /**
    * Sets the name used to identify this archive on the server
    * 
    * @param archiveRef The name, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>archiveRef</code> is invalid.
    */
   public void setArchiveRef(String archiveRef)
   {
      if (archiveRef == null || archiveRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "archiveRef may not be null or empty");
         
      m_archiveRef = archiveRef;
   }
   
   /**
    * Set if a package is editable.  Defaults to true.
    * 
    * @param value boolean
    */
   public void setEditable(boolean value)
   {
      m_editable = value;
   }
   
   /**
    * Returns <code>true</code> if package is editable.
    */
   public boolean isEditable()
   {
      return m_editable;
   }
   
   /**
    * Get the server's repository info.
    *
    * @return The info, never <code>null</code>.
    */
   public PSDbmsInfo getRepositoryInfo()
   {
      return m_repository;
   }

   /**
    * Get the object containing lower-level detail regarding this archive.
    *
    * @return The archive detail, may be <code>null</code> if one has not
    * been set or if it has been cleared.
    */
   public PSArchiveDetail getArchiveDetail()
   {
      return m_detail;
   }

   /**
    * Sets the archive detail.  See {@link #getArchiveDetail()} for more info.
    *
    * @param detail The detail object to set, may be <code>null</code> to clear
    * the detail.
    */
   public void setArchiveDetail(PSArchiveDetail detail)
   {
      m_detail = detail;
   }

   /**
    * Get the name of the user that created this archive.
    *
    * @return The name, never <code>null</code> or empty.
    */
   public String getUserName()
   {
      return m_userName;
   }

   /**
    * Get the server's version object.
    *
    * @return The version object, never <code>null</code>.
    */
   public PSFormatVersion getServerInfo()
   {
      return m_serverInfo;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXArchiveInfo (PSXFormatVersion, PSXDbmsInfo,
    *    PSXArchiveDetail?)>
    * %lt;!ATTLIST PSXArchiveInfo
    *    archiveRef CDATA #REQUIRED
    *    serverName CDATA #REQUIRED
    *    userName CDATA #REQUIRED
    *    category CDATA #REQUIRED
    *    created CDATA #REQUIRED
    *    editable CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_ARCHIVE_REF, m_archiveRef);
      root.setAttribute(XML_ATTR_SERVER_NAME, m_serverName);
      root.setAttribute(XML_ATTR_USER_NAME, m_userName);
      root.setAttribute(XML_ATTR_CATEGORY, m_category);
      root.setAttribute(XML_ATTR_CREATED, String.valueOf(m_created));
      root.setAttribute(XML_ATTR_EDITABLE, String.valueOf(m_editable));
      root.appendChild(m_serverInfo.toXml(doc));
      root.appendChild(m_repository.toXml(doc));
      if (m_detail != null)
         root.appendChild(m_detail.toXml(doc));

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
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      m_archiveRef = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_ARCHIVE_REF);
      m_serverName = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_SERVER_NAME);
      m_userName = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_USER_NAME);
      m_category = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
            XML_ATTR_CATEGORY);
      if (sourceNode.getAttribute(XML_ATTR_EDITABLE).equalsIgnoreCase("true"))
      {
         setEditable(true);
      }
      else
      {
         setEditable(false);
      }
      String created = sourceNode.getAttribute(XML_ATTR_CREATED);
      m_created = created == null || created.equals("-1") 
         ? (new Date()).getTime()
         : (new Long(created)).longValue();  
               
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      Element serverInfoEl = tree.getNextElement(PSFormatVersion.NODE_TYPE,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (serverInfoEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, PSFormatVersion.NODE_TYPE);
      }

      m_serverInfo = PSFormatVersion.createFromXml(serverInfoEl);

      if (m_serverInfo == null)
      {
         Object[] args = {XML_NODE_NAME, PSFormatVersion.NODE_TYPE, "unknown"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      tree.setCurrent(sourceNode);
      Element repositoryEl = tree.getNextElement(PSDbmsInfo.XML_NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (repositoryEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, PSDbmsInfo.XML_NODE_NAME);
      }
      m_repository = new PSDbmsInfo(repositoryEl);

      tree.setCurrent(sourceNode);
      Element detailEl = tree.getNextElement(PSArchiveDetail.XML_NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (detailEl != null)
         m_detail = new PSArchiveDetail(detailEl);
   }


   // see IPSDeployComponent
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");

      if (!(obj instanceof PSArchiveInfo))
         throw new IllegalArgumentException("obj wrong type");

      PSArchiveInfo info = (PSArchiveInfo)obj;
      m_archiveRef = info.m_archiveRef;
      m_detail = info.m_detail;
      m_serverInfo = info.m_serverInfo;
      m_serverName = info.m_serverName;
      m_repository = info.m_repository;
      m_userName = info.m_userName;
      m_category = info.m_category;
      m_created = info.m_created == -1 
         ? (new Date()).getTime()
         : info.m_created;

   }

   /**
    * @return the created
    */
   public long getCreated()
   {
      return m_created;
   }   

   /**
    * Retrieves the category that this archive info represents.  The category
    * distinguishes it as either SYSTEM or USER.
    * 
    * @return The category, never <code>null</code> or empty.
    */
   public String getCategory()
   {
      return m_category;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSArchiveInfo)) return false;
      PSArchiveInfo that = (PSArchiveInfo) o;
      return m_editable == that.m_editable &&
              m_created == that.m_created &&
              m_archiveRef.equals(that.m_archiveRef) &&
              Objects.equals(m_serverName, that.m_serverName) &&
              Objects.equals(m_serverInfo, that.m_serverInfo) &&
              Objects.equals(m_userName, that.m_userName) &&
              Objects.equals(m_detail, that.m_detail) &&
              Objects.equals(m_repository, that.m_repository) &&
              Objects.equals(m_category, that.m_category);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_archiveRef, m_serverName, m_serverInfo, m_userName, m_detail, m_repository, m_category, m_editable, m_created);
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXArchiveInfo";

   /**
    * Name used to identify this archive on the server, never <code>null</code>
    * or empty after construction, may be modified by a call to 
    * <code>setArchiveRef()</code>.
    */
   private String m_archiveRef;

   /**
    * Name of the server where this archive was created, never
    * <code>null</code>, empty, or modified after ctor.
    */
   private String m_serverName;

   /**
    * Version info of the server where this archive was created, never
    * <code>null</code> or modified after ctor.
    */
   private PSFormatVersion m_serverInfo;


   /**
    * Name of the user that created this archive, never <code>null</code>,
    * empty, or modified after ctor.
    */
   private String m_userName;

   /**
    * Low-level details of the archive info, may be <code>null</code>, modified
    * by a call to {@link #setArchiveDetail(PSArchiveDetail)}.
    */
   private PSArchiveDetail m_detail;

   /**
    * Source server repository info.  Initialized during ctor, never
    * <code>null</code> after that, may be modified by a call to
    * <code>copyFrom()</code>.
    */
   private PSDbmsInfo m_repository;
   
   /**
    * Initialized during ctor, never <code>null</code> or empty after that, may
    * be modified by a call to <code>copyFrom()</code>.
    * @see #getCategory() 
    */
   private String m_category;
   
   /**
    * Flag to allow development of installed package
    */
   private boolean m_editable = true;
   
   /**
    * Archive creation timestamp. Will be -1 if not yet set.
    */
   private long m_created = -1;

   // private XML constants
   private static final String XML_ATTR_ARCHIVE_REF = "archiveRef";
   private static final String XML_ATTR_SERVER_NAME = "serverName";
   private static final String XML_ATTR_USER_NAME = "userName";
   private static final String XML_ATTR_CREATED = "created";
   private static final String XML_ATTR_CATEGORY = "category";
   private static final String XML_ATTR_EDITABLE = "editable";

}
