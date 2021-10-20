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

import com.percussion.content.IPSMimeContent;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * The PSApplicationFile object represents the file data of a request by
 * an application to load, save, rename, or remove a file. It contains an input
 * stream from which the raw file data can be read. This object
 * can also represent a directory folder which may contain
 * other folders and application files. A folder is indicated by
 * the {@link #isFolder} method.
 * <P>
 * There are two cases to consider:
 * <OL>
 *  <LI>Designer wants to load application file from server, and supplies the
 *  application name and a file name relative to the application's directory.
 *  A PSApplicationFile will be returned upon a successful request, containing
 *  an input stream from which the designer can read the bytes of the file.
 *  <LI>Designer wants to save an application file to the server. The designer
 *  sets the name of the file (relative to the application directory) and
 *  supplies an InputStream from which the file will be read in order to send
 *  it to the server.
 * </OL>
 * <P>
 * This object does <B>not</B> encapsulate a request to load, save, or
 * remove a file. It is the result of, or an argument to, such a request.
 *
 * @author     Jian Huang (v2.0) and Chad Loder (v1.0)
 * @version    2.0
 * @since      1.0
 */

public class PSApplicationFile extends PSFile
{
   /**
    * No argument constructor for serialization, such as calling fromXml().
    */
   public PSApplicationFile()
   {
      super();
   }
   
   /**
    * Constructor with a given file, for sending requests to load or remove
    * an application file.
    *
    * @param fileName the file name, understood to be relative to an
    * application.
    */
   public PSApplicationFile(File fileName)
   {
      super(fileName);
   }

   /**
    * Constructor with a given file, for sending requests to load or remove
    * an application file.
    *
    * @param fileName the file name, understood to be relative to an
    * application.
    * @param isFolder flag indicating that this app file is a folder
    */
   public PSApplicationFile(File fileName, boolean isFolder)
   {
      super(fileName);
      m_isFolder = isFolder;
   }

   /**
    * Construct an application file object. Supplies <code>0L</code> as the
    * (undefined) modification date of the file.
    *
    * @param   stream   the input stream
    *
    * @param   fileName the input file with a specific name
    * @see #PSApplicationFile(InputStream, File, long)
    *
    */
   public PSApplicationFile(InputStream stream, File fileName)
   {
      this(stream, fileName, 0L);
   }

   /**
    * Construct an application file object.
    *
    * @param   stream   an input stream positioned at the start of the file data.
    * If this object is to be converted to XML and/or serialized in the form of
    * a request, then this stream must remain open and unchanged until the
    * conversion process is complete. After the conversion, the input stream will
    * be closed by this object. So consider the input stream as belonging only
    * to this object.
    *
    * @param   fileName       the file name, relative to an application.
    *
    * @param   lastModified   the modification date of the file represented by
    * milliseconds since January 1, 1970 (<code>0L</code> means
    * if the date is unknown or undefined).
    */
   public PSApplicationFile(InputStream stream, File fileName, long lastModified)
   {
      super(stream, fileName, lastModified);      
   }

   /**
    * Construct an application file object from MIME content. Ususally one
    * does this in order to save this content to the server under an application
    * root. The application information (i.e., what application?) will be supplied
    * when saving.
    *
    * @param   in    the MIME content whose stream is positioned at the start
    * of the data. If this object is to be converted to XML and/or serialized
    * in the form of a request, then this content must remain open and
    * unchanged until this conversion process is complete. After the conversion,
    * the content will be closed by this object, so consider the content as
    * belonging only to this object.
    *
    * @param   fileName the file name, relative to an application.
    */
   public PSApplicationFile(IPSMimeContent in, File fileName)
   {
      this(in, fileName, 0L);
   }
   
   /**
    * If the content of this file <code>null</code>?
    */
   public boolean isNull()
   {
      return (m_content == null);
   }

   /**
    * Construct an application file object from MIME content. Ususally one
    * does this in order to save this content to the server under an application
    * root. The application information (i.e., what application?) will be supplied
    * when saving.
    *
    * @param   content the MIME content whose stream is positioned at the start
    * of the data. If this object is to be converted to XML and/or serialized
    * in the form of a request, then this content must remain open and
    * unchanged until this conversion process is complete. After the conversion,
    * the content will be closed by this object. So consider the content as
    * belonging only to this object.
    *
    * @param   fileName the file name, relative to an Extension.
    *
    * @param   lastModified the modification date of the file, or 0L if not
    * known or undefined.
    */
   public PSApplicationFile(IPSMimeContent content, File fileName,
         long lastModified)
   {
      super(content, fileName, lastModified);      
   }

   // **************  IPSComponent Interface Implementation **************

   /**
    * This method is called to create an XML element node with the
    * appropriate format for the given object. An element node may contain a
    * hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    *
    * @param      doc   the input XML document, (may not be <code>null</code>)
    *
    * @return    the newly created XML element node
    */
   @Override
   public Element toXml(Document doc)
   {
      // create a PSXApplicationFile element, add the meta data, and
      // add the base64 content to it -- it is important that the
      // base64 alphabet contain no special XML chars (like gt, lt)
      Element root = PSXmlDocumentBuilder.addEmptyElement(
         doc, doc.getDocumentElement(), ms_nodeType);
      root.setAttribute(
         ATTR_IS_FOLDER, String.valueOf(m_isFolder));
      root.setAttribute(ID_ATTR, String.valueOf(getId())); // Assign bogus id
                                             // so we don't throw an exception
                                             // on fromXML
      return super.toXml(doc, root);
     
   }

   /**
    * This method is called to populate an object from an XML
    * element node. An element node may contain a hierarchical structure,
    * including child objects. The element node can also be a child of
    * another element node.
    *
    * @param      sourceNode         the XML element node to construct this object from,
    *                               (may not be <code>null</code>)
    *
    * @param      parentDoc         the Java object which is the parent of this object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception PSUnknownNodeTypeException  if the XML element node does not
    *                                           represent a type supported
    *                                           by the class.
    */
   @Override
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        List parentComponents)
      throws PSUnknownNodeTypeException
   {
      String isFolderStr = 
         sourceNode.getAttribute(ATTR_IS_FOLDER);
      setIsFolder(isFolderStr != null && isFolderStr.equals("true"));
      super.fromXml(sourceNode, parentDoc, parentComponents, ms_nodeType);
   }
   
   /**
    * Allows the setting of the <code>isFolder</code>
    * flag.
    * @param isFolder boolean indicating if the app
    * file should be flagged as a folder.
    */
   public void setIsFolder(boolean isFolder)
   {
      m_isFolder = isFolder;
   }
   
   /**
    * Indicates that this application file is a directory
    * folder. 
    * @return <code>true</code> if this is a directory
    * folder.
    */
   public boolean isFolder()
   {
      return m_isFolder;
   }
   
   /**
    * Set the files last modified date
    * @param lastmod
    */
   public void setLastModified(Long lastmod)
   {
      m_lastModified = lastmod;
   }
   
   /**
    * Flag that indicates if this is a directory folder
    */
   private boolean m_isFolder;

   /** The element tag name in the XML document. */
   public static final String ms_nodeType = "PSXApplicationFile";
   
   /**
    * Application file attribute indicating that it is
    * a directory folder.
    */
   public static final String ATTR_IS_FOLDER = "isFolder";
}
