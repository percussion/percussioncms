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

import java.io.File;



/**
 * Class to represent a file that is part of the deployable form of a deployable
 * object.
 */
public class PSDependencyFile implements IPSDeployComponent
{

   /**
    * Construct this object from its members.
    *
    * @param fileType The type of file referenced by this class.  Must be one of
    * the <code>TYPE_xxx</code> types.
    * @param file A file reference to the file on the rx filesystem, may not be
    * <code>null</code> and must be relative to the rhythmyx root.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSDependencyFile(int fileType, File file)
   {
      this(fileType, file, null);
   }

   /**
    * Construct this object from its members.
    *
    * @param fileType The type of file referenced by this class.  Must be one of
    * the <code>TYPE_xxx</code> types.
    * @param file A file reference to the file on the rx filesystem, may not be
    * <code>null</code> and must be relative to the rhythmyx root.
    * @param originalFile A file reference to the original location of the file,
    * used if the supplied <code>file</code> parameter references a temp file or
    * some other file that will not adequately identify the location to deploy 
    * the file to when installing it from the archive.  May be 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSDependencyFile(int fileType, File file, File originalFile)
   {
      if (file == null)
         throw new IllegalArgumentException("file may not be null");
         
      if (!isValidType(fileType))
         throw new IllegalArgumentException("invalid type");

      m_type = fileType;
      m_file = file;
      m_originalFile = originalFile;
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
   public PSDependencyFile(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Gets the type of file this object represents.
    *
    * @return One of the <code>TYPE_xxx</code> types.
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Gets the file location this object represents on the rx filesystem.
    *
    * @return The file, never <code>null</code>, relative to the rx root.
    */
   public File getFile()
   {
      return m_file;
   }

   /**
    * Set the archive location.
    *
    * @param archiveLocation The archive location to be set to. It may not be
    * <code>null</code>.
    */
   public void setArchiveLocation(File archiveLocation)
   {
      if (archiveLocation == null)
         throw new IllegalArgumentException("archiveLocation may not be null");

      m_archiveLocation = archiveLocation;
   }

   /**
    * Get the archive location.
    *
    * @return The archive location, it may be <code>null</code> if has not
    * been set.
    */
   public File getArchiveLocation()
   {
      return m_archiveLocation;
   }
   
   /**
    * Get the original file location if one was provided during construction.
    * If this object has been restored from its XML representation, the file
    * path will contain normalized separators (forward slashes).
    * 
    * @return The original file, may be <code>null</code>.  
    */
   public File getOriginalFile()
   {
      return m_originalFile;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXDependencyFile (RxFile, ArchiveFile, OriginalFile?)>
    * &lt;!ATTLIST PSXDependencyFile
    *    fileType CDATA #REQUIRED
    * >
    * &lt;!ELEMENT RxFile (#PCDATA)>
    * &lt;!ELEMENT ArchiveFile (#PCDATA)>
    * &lt;!ELEMENT OriginalFile (#PCDATA)>
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    * <P>
    * Note: The archive location (the return from
    * <code>getArchiveLocation()</code>) may not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>doc</code> is <code>null</code>
    * @throws IllegalStateException If is the archive-location is
    * <code>null</code>
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      if (m_archiveLocation == null)
         throw new IllegalStateException("m_archiveLocation may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_FILE_TYPE, TYPE_ENUM[m_type]);
      PSXmlDocumentBuilder.addElement(doc, root, XML_EL_RX_FILE,
         m_file.getPath());
      PSXmlDocumentBuilder.addElement(doc, root, XML_EL_ARCHIVE_FILE,
         m_archiveLocation.getPath());
      if (m_originalFile != null)
      PSXmlDocumentBuilder.addElement(doc, root, XML_EL_ORIG_FILE,
         m_originalFile.getPath());
      

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

      String sTemp = null;

      // get type
      m_type = UNDEFINED;
      sTemp = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_FILE_TYPE);
      for (int i = 0; i < TYPE_ENUM.length && m_type == UNDEFINED; i++)
      {
         if (TYPE_ENUM[i].equals(sTemp))
            m_type = i;
      }

      if (m_type == UNDEFINED)
      {
         Object[] args = {sourceNode.getTagName(), XML_ATTR_FILE_TYPE,
               sTemp};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }


      // get rx file
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      m_file = new File(PSDeployComponentUtils.getRequiredElement(tree,
         XML_NODE_NAME, XML_EL_RX_FILE, true));

      // get archive location, it cannot be null or empty
      m_archiveLocation = null;
      tree.setCurrent(sourceNode);
      String archiveLocation = PSDeployComponentUtils.getRequiredElement(tree,
         XML_NODE_NAME, XML_EL_ARCHIVE_FILE, false);
      if (archiveLocation.trim().length() > 0)
      {
         m_archiveLocation = new File(archiveLocation);
      }
      else
      {
         Object[] args = {XML_NODE_NAME, XML_EL_ARCHIVE_FILE, "null"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      // get original file if specified
      m_originalFile = null;
      tree.setCurrent(sourceNode);
      Element fileEl = tree.getNextElement(XML_EL_ORIG_FILE, 
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (fileEl != null)
      {
         String origPath = tree.getElementData(fileEl);
         if (origPath.trim().length() > 0)
            m_originalFile = new File(PSDeployComponentUtils.getNormalizedPath(
               origPath));
         else
         {
            Object[] args = {XML_NODE_NAME, XML_EL_ORIG_FILE, "null"};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
      }
   }

   // see IPSDeployComponent
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");

      if (!(obj instanceof PSDependencyFile))
         throw new IllegalArgumentException("obj wrong type");

      PSDependencyFile dep = (PSDependencyFile)obj;
      m_type = dep.m_type;
      m_file = dep.m_file;
      m_archiveLocation = dep.m_archiveLocation;
   }

   // see IPSDeployComponent
   public int hashCode()
   {
      return m_type + m_file.hashCode() + m_archiveLocation.hashCode();
   }

   // see IPSDeployComponent
   public boolean equals(Object obj)
   {
      boolean isEqual = true;
      if (!(obj instanceof PSDependencyFile))
         isEqual = false;
      else
      {
         PSDependencyFile other = (PSDependencyFile)obj;
         if (m_type != other.m_type)
            isEqual = false;
         else if (!m_file.equals(other.m_file))
            isEqual = false;
         else if (m_archiveLocation == null ^ other.m_archiveLocation == null)
            isEqual = false;
         else if (m_archiveLocation != null && !m_archiveLocation.equals(
            other.m_archiveLocation))
         {
            isEqual = false;
         }
      }

      return isEqual;
   }

   /**
    * Validates the supplied type is one of the <code>TYPE_xxx</code> values
    * supported by this class.
    *
    * @param fileType The type to check.
    *
    * @return <code>true</code> if the type is valid, <code>false</code>
    * otherwise.
    */
   private boolean isValidType(int fileType)
   {
      return fileType == TYPE_APPLICATION_XML ||
         fileType == TYPE_APPLICATION_FILE ||
         fileType == TYPE_DBMS_SCHEMA ||
         fileType == TYPE_DBMS_DATA ||
         fileType == TYPE_EXTENSION_DEF_XML ||
         fileType == TYPE_CONTROL_XML ||
         fileType == TYPE_SUPPORT_FILE ||
         fileType == TYPE_EXTENSION_RESOURCE ||
         fileType == TYPE_SHARED_GROUP_XML ||
         fileType == TYPE_SHARED_SYSTEM_OVERRIDE_XML ||
         fileType == TYPE_SYSTEM_DEF_XML ||
         fileType == TYPE_COMPONENT_XML || 
         fileType == TYPE_NODE_DEFINITION ||
         fileType == TYPE_ITEM_DEFINITION ||
         fileType == TYPE_SERVICEGENERATED_XML;
   }

   /**
    * Constant to indicate the file represented by this class is the XML format
    * of an application.
    */
   public static final int TYPE_APPLICATION_XML = 0;

   /**
    * Constant to indicate the file represented by this class is a file located
    * in an application directory.
    */
   public static final int TYPE_APPLICATION_FILE = 1;

   /**
    * Constant to indicate the file represented by this class is the
    * tablefactory XML representation of a table schema.
    */
   public static final int TYPE_DBMS_SCHEMA = 2;

   /**
    * Constant to indicate the file represented by this class is the
    * tablefactory XML representation of data in a table.
    */
   public static final int TYPE_DBMS_DATA = 3;

   /**
    * Constant to indicate the file represented by this class is the
    * XML representation of an extension defintion.
    */
   public static final int TYPE_EXTENSION_DEF_XML = 4;

   /**
    * Constant to indicate the file represented by this class is the
    * XML representation of a content editor control.
    */
   public static final int TYPE_CONTROL_XML = 5;

   /**
    * Constant to indicate the file represented by this class is a file
    * of no particular type required by a dependency .
    */
   public static final int TYPE_SUPPORT_FILE = 6;

   /**
    * Constant to indicate the file represented by this class is a class or
    * jar file used by an exit.
    */
   public static int TYPE_EXTENSION_RESOURCE = 7;

   /**
    * Constant to indicate the file represented by this class is the xml
    * representation of a group from the shared def.
    */
   public static final int TYPE_SHARED_GROUP_XML = 8;

   /**
    * Constant to indicate the file represented by this class is the xml
    * representation of a system def overide from the shared def.
    */
   public static final int TYPE_SHARED_SYSTEM_OVERRIDE_XML = 9;

   /**
    * Constant to indicate the file represented by this class is the xml
    * representation of the system def.
    */
   public static final int TYPE_SYSTEM_DEF_XML = 10;

   /**
    * Constant to indicate the file represented by this class is the xml
    * representation of an <code>IPSDbComponent</code>.
    */
   public static final int TYPE_COMPONENT_XML = 11;
   
   /**
    * Constant to indicate the file is the new service representation
    */
   public static final int TYPE_SERVICEGENERATED_XML = 12;
   
   /**
    * Constant to indicate the Node Definition file
    */
   public static final int TYPE_NODE_DEFINITION = 13;
   
   /**
    * Constant to indicate the Item Definition file
    */
   public static final int TYPE_ITEM_DEFINITION = 14;

   /**
    * Array of file type names, the index into the array matches the
    * corresponding constant value for that type.  Must be maintained as types
    * are added, removed, or renamed.
    */
   public static final String[] TYPE_ENUM =
   {
      "APPLICATION_XML",
      "APPLICATION_FILE",
      "DBMS_SCHEMA",
      "DBMS_DATA",
      "EXTENSION_DEF_XML",
      "CONTROL_XML",
      "SUPPORT_FILE",
      "EXTENSION_RESOURCE",
      "SHARED_GROUP_XML",
      "SHARED_SYSTEM_OVERRIDE_XML",
      "SYSTEM_DEF_XML",
      "COMPONENT_XML",
      "SERVICEGENERATED_XML",
      "NODE_DEFINITION",
      "ITEM_DEFINITION"
   };

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXDependencyFile";

   /**
    * The type of file this object represents, one of the <code>TYPE_xxx</code>
    * values, set during construction, modified by <code>copyFrom()</code>
    */
   private int m_type;

   /**
    * The location of the file this object represents, relative to the rx root.
    * Intialized during construction, never <code>null</code> modified by
    * <code>copyFrom()</code>.
    */
   private File m_file;

   /**
    * The location of the file this object represents, relative to some location
    * below the rx root. Intialized during construction, may be 
    * <code>null</code> if an original location is not specified.
    */
   private File m_originalFile;
   
   /**
    * The location of the file this object represents, relative to the archive
    * root. <code>null</code> until {@link #setArchiveLocation(File)} is called.
    */
   private File m_archiveLocation;


   /**
    * Constant to indicate a value is undefined.
    */
   private static final int UNDEFINED = -1;

   // private XML constants
   private static final String XML_EL_RX_FILE = "RxFile";
   private static final String XML_EL_ARCHIVE_FILE = "ArchiveFile";
   private static final String XML_ATTR_FILE_TYPE = "fileType";
   private static final String XML_EL_ORIG_FILE = "OriginalFile";

}
