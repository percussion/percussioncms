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
import com.percussion.utils.collections.PSMapUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class to encapsulate low-level archive detail added at completion of
 * creating all export packages.
 */
public class PSArchiveDetail implements IPSDeployComponent
{
   /**
    * Construct this class from its member info.
    * 
    * @param desc The export descriptor used to create the archive.  May not be
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSArchiveDetail(PSExportDescriptor desc)
   {
      if (desc == null)
         throw new IllegalArgumentException("desc may not be null");

      m_exportDescriptor = desc;

      initDbmsInfoMap(desc);      
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
   public PSArchiveDetail(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
         
      fromXml(source);

      initDbmsInfoMap(m_exportDescriptor);
   }
   
   /**
    * Sets the external dbms info list for the specified package.  Replaces any
    * previously added list for this package.  See 
    * {@link #getExternalDbmsList(PSDeployableElement)} for more info.
    * 
    * @param pkg The element to add it for, may not be <code>null</code>.  Must
    * be a package specified in the export descriptor supplied during 
    * construction.
    * @param infoList The list of <code>PSDbmsInfo</code> objects to add, may 
    * not be <code>null</code>, may be empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public void setDbmsInfoList(PSDeployableElement pkg, List infoList)
   {
      if (pkg == null)
         throw new IllegalArgumentException("pkg may not be null");
         
      if (infoList == null)
         throw new IllegalArgumentException("infoList may not be null");
         
      List dbmsList = (List)m_externalDbmsMap.get(pkg.getKey());
      if (dbmsList == null)
      {
         // export descriptor may have changed, refresh the map and try again
         refreshDbmsInfoMap();
         dbmsList = (List)m_externalDbmsMap.get(pkg.getKey());
         if (dbmsList == null)
            throw new IllegalArgumentException(
               "pkg not found in export descriptor");
      }
      dbmsList.clear();
      dbmsList.addAll(infoList);
   }

   /**
    * Get the deployable elements packaged in this archive.
    * 
    * @return An Iterator over one or more <code>PSDeployableElement</code> 
    * objects.  Never <code>null</code>.
    */
   public Iterator getPackages()
   {
      return m_exportDescriptor.getPackages();
   }

   /**
    * Get the descriptor used to create this archive.
    * 
    * @return The descriptor, never <code>null</code>.
    */
   public PSExportDescriptor getExportDescriptor()
   {
      return m_exportDescriptor;
   }

   /**
    * Get the non-cms dbms definitions used by the supplied package.
    * 
    * @param pkg The package to get definitions for.  May not be 
    * <code>null</code>.
    * 
    * @return iterator over zero or more <code>PSDbmsInfo</code> objects
    */
   public Iterator getExternalDbmsList(PSDeployableElement pkg)
   {
      if (pkg == null)
         throw new IllegalArgumentException("pkg may not be null");
      
      List result = null;
      
      refreshDbmsInfoMap();
      result = (List)m_externalDbmsMap.get(pkg.getKey());
         
      return result.iterator();
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXArchiveDetail (PSXExportDescriptor, DbmsInfoMap)>
    * &lt;!ELEMENT DbmsInfoMap (DbmsInfoMapping*)>
    * &lt;!ELEMENT DbmsInfoMapping (PSXDbmsInfo+)>
    * &lt;!ATTLIST DbmsInfoMapping
    *    PkgKey CDATA #REQUIRED>
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
      root.appendChild(m_exportDescriptor.toXml(doc));
      
      refreshDbmsInfoMap();
      Element mapEl = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
         XML_EL_DBMS_INFO_MAP);
      Iterator entries = m_externalDbmsMap.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry)entries.next();
         String pkgKey = (String)entry.getKey();
         List dbmsList = (List)entry.getValue();
         
         Element mappingEl = PSXmlDocumentBuilder.addEmptyElement(doc, mapEl, 
            XML_EL_DBMS_INFO_MAPPING);
         mappingEl.setAttribute(XML_ATTR_PKGKEY, pkgKey);
         
         Iterator infos = dbmsList.iterator();
         while (infos.hasNext())
         {
            PSDatasourceMap dsMap = (PSDatasourceMap)infos.next();
            Element dsElem = dsMap.toXml(doc);
            mappingEl.appendChild(dsElem);
         }
      }
         
         
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
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      int firstFlags = (PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      int nextFlags = (PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      
      // first load the export descriptor
      Element descEl = tree.getNextElement(PSExportDescriptor.XML_NODE_NAME, 
         firstFlags);
      if (descEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, 
               PSExportDescriptor.XML_NODE_NAME);
      }
      m_exportDescriptor = new PSExportDescriptor(descEl);
      
      
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");
         
      if (!(obj instanceof PSArchiveDetail))
         throw new IllegalArgumentException("obj wrong type");

      PSArchiveDetail other = (PSArchiveDetail)obj;
      m_exportDescriptor = other.m_exportDescriptor;
      m_externalDbmsMap = new HashMap();
      m_externalDbmsMap.putAll(other.m_externalDbmsMap); 
      
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSArchiveDetail)) return false;
      PSArchiveDetail that = (PSArchiveDetail) o;
      return  Objects.equals(m_exportDescriptor, that.m_exportDescriptor) &&
               PSMapUtils.areEqualWithArrayListValue(m_externalDbmsMap,that.m_externalDbmsMap);

   }


   @Override
   public int hashCode() {
      return Objects.hash(m_exportDescriptor, m_externalDbmsMap.hashCode());
   }

   /**
    * Initializes the external dbms info map.
    * 
    * @param desc The descriptor used to init the map.  Assumed not 
    * <code>null</code>.
    */
   private void initDbmsInfoMap(PSExportDescriptor desc)
   {
      m_externalDbmsMap.clear();
      Iterator pkgs = desc.getPackages();
      while (pkgs.hasNext())
      {
         PSDeployableElement pkg = (PSDeployableElement)pkgs.next();
         m_externalDbmsMap.put(pkg.getKey(), new ArrayList());
      }
   }
   
   /**
    * Ensures the external dbms info map has all packages currently in the
    * export descriptor.
    * 
    * @param desc The descriptor used to init the map.  Assumed not 
    * <code>null</code>.
    */
   private void refreshDbmsInfoMap()
   {
      Iterator pkgs = m_exportDescriptor.getPackages();
      Map newMap = new HashMap();
      while (pkgs.hasNext())
      {
         PSDeployableElement pkg = (PSDeployableElement)pkgs.next();
         String pkgKey = pkg.getKey();
         List pkgList = (List)m_externalDbmsMap.get(pkgKey);
         if (pkgList == null)
            pkgList = new ArrayList();
         newMap.put(pkgKey, pkgList);         
      }
      m_externalDbmsMap = newMap;
   }
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXArchiveDetail";
   
   /**
    * The export descriptor supplied during ctor.  Never <code>null</code> after
    * that.  May be modified externally if reference passed to ctor is held by
    * the caller and modified after this class is constructed.
    */
   private PSExportDescriptor m_exportDescriptor;
   
   /**
    * Map of external database information for each package in the 
    * {@link #m_exportDescriptor}.  The key is a package key 
    * (<code>String</code> object) constructed from each of the 
    * <code>PSDeployableElement</code> objects from the export descriptor, 
    * and the value is a <code>List</code> of <code>PSDbmsInfo</code> objects.
    * There is an entry for each package, and each value will contain a list
    * even if it is empty.  Contents are initialized during ctor and 
    * {@link #fromXml(Element)}, never <code>null</code>.
    */
   private Map m_externalDbmsMap = new HashMap();
   
   // private Xml constants
   private static final String XML_EL_DBMS_INFO_MAP = "DbmsInfoMap";
   private static final String XML_EL_DBMS_INFO_MAPPING = "DbmsInfoMapping";
   private static final String XML_ATTR_PKGKEY = "PkgKey";
}
