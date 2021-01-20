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
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Descriptor used to run an import job to install objects from a deployment 
 * archive.
 */
public class PSImportDescriptor extends PSDescriptor
{

   /**
    * Construct this object from its XML representation.  See 
    * {@link #toXml(Document)} for the format expected.
    * 
    * @param src The source XML element, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>src</code> is <code>null</code>.
    * @throws PSUnknownNodeTypeException if <code>src</code> is malformed.
    */
   public PSImportDescriptor(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");
         
      fromXml(src);
   }
   
   /**
    * Construct this descriptor from a previously installed archive.
    * 
    * @param archiveInfo The archive information from the archive.  May not be
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>archiveInfo</code> is 
    * <code>null</code> or does not contain a detail object.
    */
   public PSImportDescriptor(PSArchiveInfo archiveInfo)
   {
      super("temp");
      
      if (archiveInfo == null)
         throw new IllegalArgumentException("archiveInfo may not be null");
         
      PSArchiveDetail detail = archiveInfo.getArchiveDetail();
      if (detail != null)
      {
         m_archiveInfo = new PSArchiveInfo(archiveInfo);
         m_archiveInfo.setArchiveDetail(null);
      }
      else
         m_archiveInfo = archiveInfo;
      
      setName(archiveInfo.getArchiveRef());
   }
   
   /**
    * Add all packages as import packages, sets the archive ref to what's defined in the export descriptor
    */
   @SuppressWarnings("rawtypes")
   public static PSImportDescriptor configureFromArchive(PSArchiveInfo archiveInfo)
   {
       PSArchiveDetail archiveDetail = archiveInfo.getArchiveDetail();
       PSImportDescriptor desc = new PSImportDescriptor(archiveInfo);
       
       PSExportDescriptor exportDesc = archiveDetail.getExportDescriptor();
       archiveInfo.setArchiveRef(exportDesc.getName());
       List<PSImportPackage> packages = desc.getImportPackageList();
       packages.clear();
       Iterator itr = archiveDetail.getPackages();
       while(itr.hasNext())
       {
          Object obj = itr.next();
          if (obj instanceof PSDeployableElement)
          {
             packages.add(new PSImportPackage((PSDeployableElement)obj));
          }
       }
       
       return desc;
   }

   
   /**
    * Get the list of import packages contained in this descriptor.
    * 
    * @return List of zero or more <code>PSImportPackage</code> objects.  A 
    * reference to the list contained by this descriptor are returned.  Packages 
    * may be added, removed, and reordered within this list, and the changes 
    * will be reflected within this descriptor.  Never <code>null</code>, may be
    * empty.
    */
   public List<PSImportPackage> getImportPackageList()
   {
      return m_packages;
   }

   /**
    * Get the archive info supplied when this object was first constructed.
    * 
    * @return The archive info, never <code>null</code>.
    */
   public PSArchiveInfo getArchiveInfo()
   {
      return m_archiveInfo;
   }

   /**
    * Determine if the supplied package is contained in this descriptor.  
    * Package type and id are compared, but the child dependencies are ignored.
    * 
    * @param pkg The package to check, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>pkg</code> is <code>null</code>.
    */
   public boolean isPackageIncluded(PSDeployableElement pkg)
   {
      if (pkg == null)
         throw new IllegalArgumentException("pkg may not be null");
      
      boolean isIncluded = false;
      
      Iterator<PSImportPackage> i = m_packages.iterator();
      while (i.hasNext() && !isIncluded)
      {
         PSImportPackage impPkg = i.next();
         PSDeployableElement dep = impPkg.getPackage();
         if (dep.getObjectType().equals(pkg.getObjectType()) && 
            dep.getDependencyId().equals(pkg.getDependencyId()))
         {
            isIncluded = true;
         }
      }
      
      return isIncluded;
   }

   
   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXImportDescriptor (PSXDescriptor, PSXArchiveInfo, 
    *    PSXImportPackage*)>
    * &lt;!ATTLIST PSXImportDescriptor 
    *    validateAncestors (yes | no) "yes"
    * >
    * </code></pre>
    * 
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   @Override
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
         
      Element root = doc.createElement(XML_NODE_NAME);
      
      // add base class
      root.appendChild(super.toXml(doc));
      
      // add archive info
      root.appendChild(m_archiveInfo.toXml(doc));
      
      // add each package
      Iterator<PSImportPackage> pkgs = m_packages.iterator();
      while (pkgs.hasNext())
         root.appendChild((pkgs.next()).toXml(doc));
         
      // set ancestor validation
      root.setAttribute(XML_VALIDATE_ANCESTORS_ATTR, m_validateAncestors ? 
         XML_VAL_TRUE : XML_VAL_FALSE);
      
      return root;
   }

   /**
    * Restores this object's state from its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See 
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    */
   @Override
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
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      int firstFlags = (PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      int nextFlags = (PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      
      // restore base class
      Element descEl = tree.getNextElement(PSDescriptor.XML_NODE_NAME, 
         firstFlags);
      if (descEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, PSDescriptor.XML_NODE_NAME);
      }
      super.fromXml(descEl);
      
      Element archiveInfoEl = tree.getNextElement(PSArchiveInfo.XML_NODE_NAME, 
         nextFlags);
      if (archiveInfoEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, PSArchiveInfo.XML_NODE_NAME);
      }
      m_archiveInfo = new PSArchiveInfo(archiveInfoEl);
      
      // restore all packages
      m_packages.clear();
      Element pkgEl = tree.getNextElement(PSImportPackage.XML_NODE_NAME, 
         nextFlags);
      while (pkgEl != null)
      {
         m_packages.add(new PSImportPackage(pkgEl));
         pkgEl = tree.getNextElement(PSImportPackage.XML_NODE_NAME, 
            nextFlags);
      }
      
      // restore ancestor validation
      String sTemp = sourceNode.getAttribute(XML_VALIDATE_ANCESTORS_ATTR);
      // default to true if not supplied
      m_validateAncestors = !XML_VAL_FALSE.equals(sTemp);
   }
   
   // see IPSDeployComponent interface
   @Override
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");
         
      if (!(obj instanceof PSImportDescriptor))
         throw new IllegalArgumentException("obj wrong type");

      PSImportDescriptor desc = (PSImportDescriptor)obj;
      super.copyFrom(desc);
      m_archiveInfo = desc.m_archiveInfo;
      m_packages.clear();
      m_packages.addAll(desc.m_packages);
      m_validateAncestors = desc.m_validateAncestors;
   }

   // see IPSDeployComponent interface
   @Override
   public int hashCode()
   {
      return super.hashCode() + m_archiveInfo.hashCode() + 
         m_packages.hashCode() + (m_validateAncestors ? 1 : 0);
   }

   // see IPSDeployComponent interface
   @Override
   public boolean equals(Object obj)
   {
      boolean isEqual = true;
      if (!(obj instanceof PSImportDescriptor))
         isEqual = false;
      else
      {
         PSImportDescriptor other = (PSImportDescriptor)obj;
         if (!super.equals(other))
            isEqual = false;
         else if (!m_archiveInfo.equals(other.m_archiveInfo))
            isEqual = false;
         else if (!m_packages.equals(other.m_packages))
            isEqual = false;
         else if (m_validateAncestors != other.m_validateAncestors)
            isEqual = false;
      }
      
      return isEqual;
   }
   
   
   /**
    * Determines if ancestor validation is enabled.  See 
    * {@link #setAncestorValidationEnabled(boolean)} for more info.
    * 
    * @return <code>true</code> if it is enabled, <code>false</code> otherwise.
    */
   public boolean isAncestorValidationEnabled()
   {
      return m_validateAncestors;
   }
   
   /**
    * Sets whether or not the ancestors of dependencies being installed should
    * be checked during validation to determine if there will be any impact to
    * them.  Initially set to <code>false</code>.
    * 
    * @param isEnabled If <code>true</code>, ancestors will be validated, if 
    * <code>true</code>, they will not.
    */
   public void setAncestorValidationEnabled(boolean isEnabled)
   {
      m_validateAncestors = isEnabled;
   }

   /**
    * Root node name of this object's XML representation.
    */
   @SuppressWarnings("hiding")
   public static final String XML_NODE_NAME = "PSXImportDescriptor";

   /**
    * List of <code>PSImportPackage</code> objects included in this descriptor.
    * Never <code>null</code>, may be empty, and may be directly modified by
    * obtaining this reference through a call to {@link #getImportPackageList()}
    */
   List<PSImportPackage> m_packages = new ArrayList<PSImportPackage>();
   
   /**
    * The archive info from the archive this descriptor will install from. 
    * Never <code>null</code> after ctor, may be modified by a call to 
    * <code>copyFrom()</code>.
    */
   PSArchiveInfo m_archiveInfo;
   
   /**
    * Determines if ancestors of dependencies marked for installation should be
    * validated to determine if they will be impacted.  Initially 
    * <code>true</code>, modified by calls to 
    * {@link #setAncestorValidationEnabled(boolean)}.
    */
   boolean m_validateAncestors = false;
   
   // xml constants
   private static final String XML_VALIDATE_ANCESTORS_ATTR = 
      "validateAncestors";
   private static final String XML_VAL_TRUE = "yes";
   private static final String XML_VAL_FALSE = "no";
}
