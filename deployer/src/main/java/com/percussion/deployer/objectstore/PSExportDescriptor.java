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
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;


/**
 * Descriptor used to run an export job to create a deployment archive.
 */
public class PSExportDescriptor extends PSDescriptor
{
   /**
    * Construct this descriptor, specifying its name. The archive type is set
    * to <code>ARCHIVE_TYPE_NORMAL</code>.
    *
    * @param name The name of this descriptor, used to identify it on the
    * server, may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>name</code> is
    * <code>null</code>.
    */
   public PSExportDescriptor(String name)
   {
      this(name, ARCHIVE_TYPE_NORMAL);
   }

   /**
    * Construct this descriptor, specifying its name and type.
    *
    * @param name The name of this descriptor, used to identify it on the
    * server, may not be <code>null</code> or empty.
    * @param archiveType the type of archive that will be created using this
    * descriptor, valid values are
    * <code>ARCHIVE_TYPE_NORMAL</code> or
    * <code>ARCHIVE_TYPE_SAMPLE</code> or
    * <code>ARCHIVE_TYPE_SUPPORT</code>
    *
    * @throws IllegalArgumentException if <code>name</code> is
    * <code>null</code> or archiveType is invalid.
    */
   public PSExportDescriptor(String name, int archiveType)
   {
      super(name);
      if ((archiveType < 0) || (archiveType >= ARCHIVE_TYPE_ENUM.length))
         throw new IllegalArgumentException("invalid archiveType");

      m_archiveType = archiveType;
   }

   
   /**
    * Construct this object from its XML representation.  See
    * {@link #toXml(Document)} for the format expected.
    *
    * @param src The source XML element, may not be <code>null</code>.
    * @param readShortDesc, read the short description, donot actually read
    * the entire PSXExportDescriptor. This can be used by PSCatalogHandler's 
    * catalogDescriptors, where it **JUST** needs the PSDescriptor element
    *
    * @throws IllegalArgumentException if <code>src</code> is <code>null</code>.
    * @throws PSUnknownNodeTypeException if <code>src</code> is malformed.
    */
   public PSExportDescriptor(Element src, boolean readShortDesc) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");
      if ( readShortDesc )
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(src);
         int firstFlags = (PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
         int nextFlags = (PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
            PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);

         // restore base class
         Element descEl = tree.getNextElement(PSDescriptor.XML_NODE_NAME,
            firstFlags);

         super.fromXml(descEl);
      }
      else
         fromXml(src);
   }

   
   
   /**
    * Construct this object from its XML representation.  See
    * {@link #toXml(Document)} for the format expected.
    *
    * @param src The source XML element, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>src</code> is <code>null</code>.
    * @throws PSUnknownNodeTypeException if <code>src</code> is malformed.
    */
   public PSExportDescriptor(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");

      fromXml(src);
   }

   /**
    * Returns the type of archive that will be created using this descriptor.
    *
    * @return the type of archive that will be created using this descriptor,
    * should be one of the following values:
    * <code>ARCHIVE_TYPE_NORMAL</code> or
    * <code>ARCHIVE_TYPE_SAMPLE</code> or
    * <code>ARCHIVE_TYPE_SUPPORT</code>
    */
   public int getArchiveType()
   {
      return m_archiveType;
   }

   /**
    * Sets the list of packages and their dependencies that will be added to an
    * archive created from this descriptor.  Replaces any previous list of
    * packages contained in this descriptor.
    * 
    * @param packages Iterator over one or more <code>PSDeployableElement</code> 
    * objects, in the order in which they should be added to the archive, may 
    * not be <code>null</code>.  For each element, <code>isIncluded()</code> 
    * must either return <code>true</code>, or else 
    * <code>canBeIncludedExcluded()</code> must return <code>true</code>.  
    * <code>setIsIncluded(true)</code> will be called on each element that is 
    * not included to ensure it is set to be included.
    * 
    * @throws IllegalArgumentException if <code>packages</code> is 
    * <code>null</code> or contains an invalid entry.
    */
   public void setPackages(Iterator packages)
   {
      if (packages == null)
         throw new IllegalArgumentException("packages may not be null");

      m_packages.clear();
      while (packages.hasNext())
      {
         Object obj = packages.next();
         if (!(obj instanceof PSDeployableElement))
            throw new IllegalArgumentException("Invalid entry in packages");
         PSDeployableElement dep = (PSDeployableElement)obj;
         if (!dep.isIncluded())
         {
            if (dep.canBeIncludedExcluded())
               dep.setIsIncluded(true);
            else
            {
               throw new IllegalArgumentException(
                  "pakages must be included or be settable as included");
            }
         }
         
         m_packages.add(dep);
      }
   }

   /**
    * Get the packages defined by this descriptor.
    *
    * @return An iterator over zero or more <code>PSDeployableElement</code>
    * objects.
    */
   public Iterator<PSDeployableElement> getPackages()
   {
      return m_packages.iterator();
   }
   
   /**
    * Get the package defined by this descriptor using the supplied key.
    * 
    * @param pkgKey The unique dependency key of the package to get, may not
    * be <code>null</code> or empty.
    *  
    * @return The package, or <code>null</code> if the specified package is not
    * defined by this descriptor.
    */
   public PSDeployableElement getPackage(String pkgKey)
   {
      PSDeployableElement pkg = null;
      
      Iterator pkgs = m_packages.iterator();
      while (pkgs.hasNext() && pkg == null)
      {
         PSDeployableElement test = (PSDeployableElement)pkgs.next();
         if (test.getKey().equals(pkgKey))
            pkg = test;
      }
      
      return pkg;
   }

   /**
    * Gets the set of dependency keys that should not be added to the exported
    * archive, if any.
    * 
    * @return A set of strings representing the keys of dependencies that should
    * not be added to the exported archive, or <code>null</code> if no such set 
    * is defined.
    */
   public Set getDepKeysToExclude()
   {
      return m_depKeysToExclude;
   }

   /**
    * Sets the set of dependencies that should not be added to the exported
    * archive.
    * 
    * @param depKeys An iterator of Strings, each representing the key of 
    * a dependency that should not be added to the exported archive.  May not
    * be <code>null</code>, but may be empty.
    */
   public void setDepKeysToExclude(Iterator depKeys)
   {
      if (depKeys == null)
         throw new IllegalArgumentException("depKeys may not be null");

      m_depKeysToExclude.clear();
      while (depKeys.hasNext())
      {
         String depKey = (String) depKeys.next();
         m_depKeysToExclude.add(depKey);
      }
   }

   /**
    * Gets the names packages whose dependencies have changed since this
    * descriptor was last saved.
    *
    * @return Iterator over zero or more package names as <code>String</code>
    * objects.
    */
   public Iterator getModifiedPackages()
   {
      return m_modifiedPackageNames.iterator();
   }

   /**
    * Sets the names of the packages whose dependencies have changed since this
    * descriptor was last saved.  Replaces any previous list.
    *
    * @names Iterator over zero or more package names as <code>String</code>
    * objects. May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>names</code> is
    * <code>null</code>.
    */
   public void setModifiedPackages(Iterator names)
   {
      if (names == null)
         throw new IllegalArgumentException("names may not be null");

      m_modifiedPackageNames.clear();
      while (names.hasNext())
      {
         m_modifiedPackageNames.add(names.next().toString());
      }
   }


   /**
    * Gets the names of the packages whose dependencies cannot be located
    *
    * @return Iterator over zero or more package names as <code>String</code>
    * objects.
    */
   public Iterator getMissingPackages()
   {
      return m_missingPackageNames.iterator();
   }

   /**
    * Sets the names of the packages whose dependencies cannot be located.  
    * Replaces any previous list.
    *
    * @names Iterator over zero or more package names as <code>String</code>
    * objects.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>names</code> is
    * <code>null</code>.
    */
   public void setMissingPackages(Iterator names)
   {
      if (names == null)
         throw new IllegalArgumentException("names may not be null");

      clearMissingPackages();  
      addMissingPackages(names);
   }
   
   /**
    * Adds the names of the packages whose dependencies cannot be located.  
    * Appends to any previous list.
    * 
    * @names Iterator over zero or more package names as <code>String</code> 
    * objects.  May not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>names</code> is 
    * <code>null</code>.
    */
   public void addMissingPackages(Iterator names)
   {
      if (names == null)
         throw new IllegalArgumentException("names may not be null");
      while (names.hasNext())
      {
         m_missingPackageNames.add(names.next().toString());
      }
   }
   
   /**
    * Clears the list of the packages whose dependencies cannot be located.  
    */
   public void clearMissingPackages()
   {
      m_missingPackageNames.clear();  
   }
   
   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXExportDescriptor (PSXDescriptor, Packages,
    *    MissingPackages, ModifiedPackages)>
    * &lt;!ATTLIST PSXExportDescriptor
    *    archiveType CDATA #REQUIRED
    * >
    * &lt;!ELEMENT Packages (PSXDeployableElement*)>
    * &lt;!ELEMENT ModifiedPackages (PackageName*)>
    * &lt;!ELEMENT MissingPackages (PackageName*)>
    * &lt;!ELEMENT PackageName (#PCDATA)>
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);

      // add base class
      root.appendChild(super.toXml(doc));

      // add the archiveType atttribute
      root.setAttribute(XML_ATTR_ARCHIVE_TYPE, ARCHIVE_TYPE_ENUM[m_archiveType]);

      // add packages
      Element packages = PSXmlDocumentBuilder.addEmptyElement(doc, root,
         XML_EL_PACKAGES);
      Iterator pkgs = m_packages.iterator();
      while (pkgs.hasNext())
      {
         PSDeployableElement dep = (PSDeployableElement)pkgs.next();
         packages.appendChild(dep.toXml(doc));
      }

      // add modified packages
      Element modsEl = PSXmlDocumentBuilder.addEmptyElement(doc, root,
         XML_EL_MODIFIED_PACKAGES);
      Iterator names = m_modifiedPackageNames.iterator();
      while (names.hasNext())
      {
         PSXmlDocumentBuilder.addElement(doc, modsEl, XML_EL_PACKAGE_NAME,
            names.next().toString());
      }

      // add missing packages
      Element missingEl = PSXmlDocumentBuilder.addEmptyElement(doc, root,
         XML_EL_MISSING_PACKAGES);
      names = m_missingPackageNames.iterator();
      while (names.hasNext())
      {
         PSXmlDocumentBuilder.addElement(doc, missingEl, XML_EL_PACKAGE_NAME,
            names.next().toString());
      }

      // add suppressed dependencies
      if (!m_depKeysToExclude.isEmpty())
      {
         Element suppressedEl = PSXmlDocumentBuilder.addEmptyElement(doc, root,
               XML_EL_SUPPRESSED_DEPENDENCIES);
         for (Iterator keys = m_depKeysToExclude.iterator(); keys.hasNext(); )
         {
            PSXmlDocumentBuilder.addElement(doc, suppressedEl, XML_EL_DEP_KEY,
                  keys.next().toString());
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

      // get the archiveType attribute
      m_archiveType = PSDeployComponentUtils.getEnumeratedAttributeIndex(tree,
         XML_ATTR_ARCHIVE_TYPE, ARCHIVE_TYPE_ENUM);

      // restore base class
      Element descEl = tree.getNextElement(PSDescriptor.XML_NODE_NAME,
         firstFlags);
      if (descEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, PSDescriptor.XML_NODE_NAME);
      }
      super.fromXml(descEl);


      // restore packages
      Element packagesEl = tree.getNextElement(XML_EL_PACKAGES,
         nextFlags);
      if (packagesEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_EL_PACKAGES);
      }

      m_packages.clear();
      Element packageEl = tree.getNextElement(PSDeployableElement.XML_NODE_NAME,
         firstFlags);
      while (packageEl != null)
      {
         m_packages.add(new PSDeployableElement(packageEl));
         packageEl = tree.getNextElement(PSDeployableElement.XML_NODE_NAME,
            nextFlags);
      }


      // restore modified package names
      m_modifiedPackageNames.clear();
      tree.setCurrent(packagesEl);
      Element modsEl = tree.getNextElement(XML_EL_MODIFIED_PACKAGES,
         nextFlags);
      if (modsEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_EL_MODIFIED_PACKAGES);
      }
      Element modName = tree.getNextElement(XML_EL_PACKAGE_NAME, firstFlags);
      while (modName != null)
      {
         String name = PSXmlTreeWalker.getElementData(modName);
         if (name != null && name.trim().length() > 0)
            m_modifiedPackageNames.add(name);
         modName = tree.getNextElement(XML_EL_PACKAGE_NAME, nextFlags);
      }

      // restore missing package names
      m_missingPackageNames.clear();
      tree.setCurrent(modsEl);
      Element missingEl = tree.getNextElement(XML_EL_MISSING_PACKAGES,
         nextFlags);
      if (missingEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_EL_MISSING_PACKAGES);
      }
      Element missingName = tree.getNextElement(XML_EL_PACKAGE_NAME, firstFlags);
      while (missingName != null)
      {
         String name = PSXmlTreeWalker.getElementData(missingName);
         if (name != null && name.trim().length() > 0)
            m_missingPackageNames.add(name);
         missingName = tree.getNextElement(XML_EL_PACKAGE_NAME, nextFlags);
      }

      // restore dependencies to suppress
      // (this element was added in 5.7, so it will not appear in older XML)
      m_depKeysToExclude.clear();
      tree.setCurrent(missingEl);
      Element suppressedEl = tree.getNextElement(XML_EL_SUPPRESSED_DEPENDENCIES,
            nextFlags);
      if (suppressedEl != null)
      {
         Element depKey = tree.getNextElement(XML_EL_DEP_KEY, firstFlags);
         while (depKey != null)
         {
            String key = PSXmlTreeWalker.getElementData(depKey);
            if (key != null && key.trim().length() > 0)
               m_depKeysToExclude.add(key);
            depKey = tree.getNextElement(XML_EL_DEP_KEY, nextFlags);           
         }
        
      }
      
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");

      if (!(obj instanceof PSExportDescriptor))
         throw new IllegalArgumentException("obj wrong type");

      PSExportDescriptor desc = (PSExportDescriptor)obj;
      super.copyFrom(desc);

      m_packages.clear();
      m_packages.addAll(desc.m_packages);

      m_modifiedPackageNames.clear();
      m_modifiedPackageNames.addAll(desc.m_modifiedPackageNames);

      m_missingPackageNames.clear();
      m_missingPackageNames.addAll(desc.m_missingPackageNames);
      
      m_depKeysToExclude.clear(); 
      m_depKeysToExclude.addAll(desc.m_depKeysToExclude);
      
      m_archiveType = desc.getArchiveType();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSExportDescriptor)) return false;
      if (!super.equals(o)) return false;
      PSExportDescriptor that = (PSExportDescriptor) o;
      return m_archiveType == that.m_archiveType &&
              Objects.equals(m_packages, that.m_packages) &&
              Objects.equals(m_modifiedPackageNames, that.m_modifiedPackageNames) &&
              Objects.equals(m_missingPackageNames, that.m_missingPackageNames) &&
              Objects.equals(m_depKeysToExclude, that.m_depKeysToExclude);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_archiveType, m_packages, m_modifiedPackageNames, m_missingPackageNames, m_depKeysToExclude);
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXExportDescriptor";

   /**
    * constants to identify the type of archives - normal, sample or support
    */
   public static final int ARCHIVE_TYPE_NORMAL = 0;
   public static final int ARCHIVE_TYPE_SAMPLE = 1;
   public static final int ARCHIVE_TYPE_SUPPORT = 2;
   public static final String[] ARCHIVE_TYPE_ENUM =
      {"normal", "sample", "support"};


   /**
    * Stores the type of archive which will be created using this descriptor,
    * valid values are :
    * <code>ARCHIVE_TYPE_NORMAL</code> or
    * <code>ARCHIVE_TYPE_SAMPLE</code> or
    * <code>ARCHIVE_TYPE_SUPPORT</code>
    * Defaults to <code>ARCHIVE_TYPE_NORMAL</code>.
    */
   private int m_archiveType = ARCHIVE_TYPE_NORMAL;

   /**
    * List of packages contained in this descriptor.  Never <code>null</code>,
    * may be empty and may be modified at any time by a call to
    * <code>setPackages()</code>.
    */
   private List<PSDeployableElement> m_packages = 
      new ArrayList<PSDeployableElement>();

   /**
    * List of names of packages that have been modified since this descriptor
    * was last saved.  Never <code>null</code>, may be empty and may be modified
    * at any time by a call to <code>setModifiedPackages()</code>.
    */
   private List m_modifiedPackageNames = new ArrayList();

   /**
    * List of names of packages whose underlying object has been deleted since
    * this descriptor was last saved.  Never <code>null</code>, may be empty and
    * may be modified at any time by a call to
    * <code>setMissingPackages()</code>.
    */
   private List m_missingPackageNames = new ArrayList();

   /**
    * Set of dependencies that should not be added in the exported archive.
    * Never <code>null</code> but frequently empty,  as dependencies only need to be filtered
    * in special circumstances.  Modified by calling setter.
    */
   private Set m_depKeysToExclude = new HashSet();
   
   // private Xml variable for serialization
   private static final String XML_EL_PACKAGES = "Packages";
   private static final String XML_EL_MODIFIED_PACKAGES = "ModifiedPackages";
   private static final String XML_EL_MISSING_PACKAGES = "MissingPackages";
   private static final String XML_EL_PACKAGE_NAME = "PackageName";
   private static final String XML_EL_SUPPRESSED_DEPENDENCIES = "SuppressedDependencies";
   private static final String XML_EL_DEP_KEY = "DepKey";
   private static final String XML_ATTR_ARCHIVE_TYPE = "archiveType";

}
