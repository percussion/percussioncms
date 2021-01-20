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
import com.percussion.util.PSDateFormatISO8601;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Encapsulates information regarding the use of an archive for the installation
 * of packages on the target server.
 */
public class PSArchiveSummary  implements IPSDeployComponent
{

   /**
    * Constructing a object from a set of parameters.
    *
    * @param    archiveInfo The archive information. It may not be
    * <code>null</code> and may not include a <code>PSArchiveDetail</code>
    *  object.
    * @param    installDate The date of installation. It may not be
    * <code>null</code>.
    * @param    packageList    Iterator over one or more package names as
    * <code>PSArchivePackage</code> objects. It may not be <code>null</code>
    * or empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSArchiveSummary(PSArchiveInfo archiveInfo, Date installDate,
      Iterator packageList)
   {
      if (archiveInfo == null || archiveInfo.getArchiveDetail() != null)
         throw new IllegalArgumentException(
            "archiveInfo may not be null or may not include PSArchiveDetail");
      if (installDate == null)
         throw new IllegalArgumentException("installDate may not be null");
      if (packageList == null || (!packageList.hasNext()))
         throw new IllegalArgumentException(
            "packageList may not be null or empty");

      m_archiveInfo = archiveInfo;
      m_installDate = installDate;
      m_packageList = sortPackageList(packageList);      
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSArchiveSummary(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");

      fromXml(src);
   }

   /**
    * Gets the <code>PSArchiveInfo</code> in this object.  It will not include
    * a <code>PSArchiveDetail</code> object.
    *
    * @return The archive info object, it will never be <code>null</code>.
    */
   public PSArchiveInfo getArchiveInfo()
   {
      return m_archiveInfo;
   }

   /**
    * Gets the installation date.
    *
    * @return The installation date, which will never be <code>null</code>.
    */
   public Date getInstallDate()
   {
      return m_installDate;
   }

   /**
    * Gets the list of packages contained in the archive.
    *
    * @return an iterator over one or more packages names as
    * <code>PSArchivePackage</code> objects. It will never be <code>null</code>
    * or empty.
    *
   */
   public Iterator getPackageList()
   {
      return m_packageList.iterator();
   }

   /**
    * Gets this summary's Id.
    *
    * @return the id, may be <code>-1</code> if this summary has not been saved
    * to the database.
    */
   public int getId()
   {
      return m_id;
   }

   /**
    * Sets the summary's id.
    *
    * @param    id The to be set summary's id. It may not be less than 0.
    *
    * @throws IllegalArgumentException if <code>id</code> is less than 0.
    */
   public void setId(int id)
   {
      if (id < 0)
         throw new IllegalArgumentException("id may not be less than 0");

      m_id = id;
   }

   /**
    * Gets the archive manifest.
    *
    * @return The archive manifest. It will be <code>null</code> if the
    * object does not contain one.
    */
   public PSArchiveManifest getArchiveManifest()
   {
      return m_archiveManifest;
   }

   /**
    * Sets the archive manifest.
    *
    * @param archMan The to be set archive manifest. It may not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>archMan</code> is
    * <code>null</code>.
    */
   public void setArchiveManifest(PSArchiveManifest archMan)
   {
      if (archMan == null)
         throw new IllegalArgumentException("archMan may not be null");

      m_archiveManifest = archMan;
   }

   /**
    * Gets a package type from given package name.
    *
    * @param pkgName The name of the package. It may not be <code>null</code>
    * or empty. It must be from one of the items in {@link #getPackageList()}
    *
    * @return The type of the specified package.
    *
    * @throws IllegalArgumentException if the <code>pkgName</code> is invalid.
    */
   public String getPackageType(String pkgName)
   {
      if (pkgName == null || pkgName.trim().length() == 0)
         throw new IllegalArgumentException("pkgName may not be null or empty");

      PSArchivePackage pkgFound = null;
      Iterator pList = m_packageList.iterator();
      while ( pList.hasNext() && pkgFound == null)
      {
         PSArchivePackage pkg = (PSArchivePackage) pList.next();
         if ( pkgName.equals(pkg.getName()) )
            pkgFound = pkg;
      }
      if (pkgFound == null)
         throw new IllegalArgumentException("pkgName cannot be found");

      return pkgFound.getType();
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXArchiveSummary (PSXArchiveInfo, PSXArchivePackage+,
    *    PSXArchiveManifest? )>
    * &lt;!ATTLIST PSXArchiveSummary
    *    id CDATA #REQUIRED
    *    installDate CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);

      root.setAttribute(XML_ATTR_ID, Integer.toString(m_id));

      PSDateFormatISO8601 dateFormat = new PSDateFormatISO8601();
      root.setAttribute(XML_ATTR_INSTALL_DATE,
         dateFormat.format(m_installDate));

      root.appendChild(m_archiveInfo.toXml(doc));
      Iterator pkgList = m_packageList.iterator();
      while (pkgList.hasNext())
      {
         PSArchivePackage pkg = (PSArchivePackage) pkgList.next();
         root.appendChild(pkg.toXml(doc));
      }
      if ( m_archiveManifest != null )
         root.appendChild(m_archiveManifest.toXml(doc));

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
      // get the attributes
      m_id = Integer.parseInt(PSDeployComponentUtils.getRequiredAttribute(
         sourceNode, XML_ATTR_ID));

      String sDate = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_INSTALL_DATE);
      PSDateFormatISO8601 dateFormat = new PSDateFormatISO8601();
      m_installDate = dateFormat.parse(sDate, new ParsePosition(0));
      if ( m_installDate == null )
      {
         Object[] args = { XML_NODE_NAME, XML_ATTR_INSTALL_DATE, sDate };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      // get the child elements
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      // get PSXArchiveInfo XML element
      Element childEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if ( childEl == null )
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, PSArchiveInfo.XML_NODE_NAME);
      }
      m_archiveInfo = new PSArchiveInfo(childEl);

      // get PSXArchivePackage+  XML element
      m_packageList.clear();
      childEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      if ( childEl == null )
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL,
            PSArchivePackage.XML_NODE_NAME);
      }
      while ( childEl != null &&
              childEl.getNodeName().equals(PSArchivePackage.XML_NODE_NAME) )
      {
         m_packageList.add(new PSArchivePackage(childEl));
         childEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }

      // get PSXArchiveManifest? XML element
      if ( childEl != null ) // the last one should be the Manifest if not null
      {
         m_archiveManifest = new PSArchiveManifest(childEl);
      }
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj parameter should not be null");
      if (!(obj instanceof PSArchiveSummary))
         throw new IllegalArgumentException(
            "obj wrong type, expecting PSArchiveSummary");

      PSArchiveSummary other = (PSArchiveSummary) obj;
      m_archiveInfo = other.m_archiveInfo;
      m_archiveManifest = other.m_archiveManifest;
      m_id = other.m_id;
      m_installDate = other.m_installDate;
      m_packageList.clear();
      m_packageList.addAll(other.m_packageList);
   }

   // see IPSDeployComponent interface
   public int hashCode()
   {
      return ((m_id > 0) ? m_id : 0) + m_installDate.hashCode() +
         m_archiveInfo.hashCode() + m_packageList.hashCode() +
         ((m_archiveManifest == null) ? 0 : m_archiveManifest.hashCode());
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean bEqual = false;

      if (obj instanceof PSArchiveSummary)
      {
         PSArchiveSummary other = (PSArchiveSummary) obj;

         if ( m_archiveManifest == null && other.m_archiveManifest == null )
         {
            bEqual = (m_id == other.m_id) &&
               m_installDate.equals(other.m_installDate) &&
               m_archiveInfo.equals(other.m_archiveInfo) &&
               m_packageList.equals(other.m_packageList);
         }
         else if (m_archiveManifest != null && other.m_archiveManifest != null)
         {
            bEqual = (m_id == other.m_id) &&
               m_installDate.equals(other.m_installDate) &&
               m_archiveInfo.equals(other.m_archiveInfo) &&
               m_packageList.equals(other.m_packageList) &&
               m_archiveManifest.equals(other.m_archiveManifest);
         }
      }

      return bEqual;
   }
   
   /**
    * Sorts the supplied packages to match the installation order.  Any package
    * not installed will appear at the end of the list, sorted alpha ascending
    * case-insensitive
    *  
    * @param packages An iterator over zero or more packages as 
    * {@link PSArchivePackage} objects, assumed not <code>null</code>.
    * 
    * @return The sorted list, never <code>null</code>, may be empty.
    */
   private List sortPackageList(Iterator packages)
   {
      List sortedList = PSDeployComponentUtils.cloneList(packages);
      Collections.sort(sortedList, new Comparator()
      {
         public int compare(Object o1, Object o2)
         {
            PSArchivePackage pkg1 = (PSArchivePackage)o1;
            PSArchivePackage pkg2 = (PSArchivePackage)o2;
            int id1 = pkg1.getLogId();
            int id2 = pkg2.getLogId();
            
            // sort id -1 (not installed) higher than id >= 0
            int result = 0;            
            if (id1 == -1 ^ id2 == -1) // one of them is -1
               result = id1 == -1 ? 1 : -1;
            else if (id1 == id2) // must both be -1
               result = pkg1.getName().compareToIgnoreCase(pkg2.getName());
            else if (id1 < id2)
               result = -1;
            else if (id1 > id2)
               result = 1;
            
            return result;
         }
      });      
      
      return sortedList;
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXArchiveSummary";

   // * Private XML attributes
   private static final String XML_ATTR_ID = "id";
   private static final String XML_ATTR_INSTALL_DATE = "installDate";

   /**
    * The archive information. Initialized by constructor, it will never be
    * <code>null</code> and will not include a <code>PSArchiveDetail</code>
    * object after that.
    */
   private PSArchiveInfo m_archiveInfo;

   /**
    * The date of installation. Initialized by constructor, it will never be
    * <code>null</code> after that.
    */
   private Date m_installDate;

   /**
    * A list over one or more package names as <code>String</code> objects.
    * Initialized by constructor, will never be <code>null</code> or empty
    * after that.
    */
   private List m_packageList = new ArrayList();

   /**
    * The summary's id, <code>-1</code> if this summary has not been saved
    * to the database. Default to <code>-1</code>.
    */
   private int m_id = -1;

   /**
    * The archive manifest. Initialized to <code>null</code>.
    */
   private PSArchiveManifest m_archiveManifest = null;
}
