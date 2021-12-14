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
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * The PSExtensionFile class represents the file data of a request by
 * an extension to load, save, or remove a file. It contains an input
 * stream from which the raw file data can be read.
 * <P>
 * This object does <B>not</B> encapsulate a request to load, save, or
 * remove a file. It is the result of, or an argumnent to, such a request.
 *
 * @author     Jian Huang
 * @version    1.1
 * @since      1.1
 */

public class PSExtensionFile extends PSFile
{
   /**
    * No args constructor for use with fromXml()
    */
   public PSExtensionFile()
   {
      super();
   }

   /**
    * Constructor with a file, for sending requests to load or remove
    * a file. The input stream will not be valid for this object.
    *
    * @param      fileName    the file name, understood to be relative to an extension
    *
    * @exception   PSIllegalArgumentException
    *                         if fileName is empty or <code>null</code>
    */
   public PSExtensionFile(File fileName)
      throws PSIllegalArgumentException
   {
      super(fileName);
   }

   /**
    * Construct an extension file object. Supplies 0L as the
    * (undefined) modification date of the file.
    *
    * @param   stream   the input stream
    *
    * @param   fileName the input file with a specific name
    *
    * @exception PSIllegalArgumentException  if any argument is not acceptable
    * 
    */
   public PSExtensionFile(InputStream stream, File fileName)
      throws PSIllegalArgumentException
   {
      this(stream, fileName, 0L);
   }

   /**
    * Construct an extension file object.
    * 
    * @param   stream   an input stream positioned at the start of the file data.
    * If this object is to be converted to XML and/or serialized in the form of
    * a request, then this stream must remain open and unchanged until the
    * conversion process is complete. After the conversion, the input stream will
    * be closed by this object. So consider the input stream as belonging only
    * to this object.
    *
    * @param   fileName       the file name, relative to an extension.
    *
    * @param   lastModified   the modification date of the file, or 0L if not
    * known or undefined.
    *
    * @exception PSIllegalArgumentException If <CODE>stream</CODE> is
    * <CODE>null</CODE>, or <CODE>fileName</CODE> is <CODE>null</CODE>, or if
    * <CODE>fileName</CODE> specifies an empty pathname.
    *
    */
   public PSExtensionFile(InputStream stream, File fileName, long lastModified)
      throws PSIllegalArgumentException
   {
      super(stream, fileName, lastModified);
   }

   /**
    * Construct an extension file object from MIME content. Supplies 0L as the
    * (undefined) modification date of the file.
    *
    * @param   in    the MIME content
    *
    * @param   fileName the input file with a specific name
    *
    * @exception PSIllegalArgumentException  if any argument is not acceptable
    */
   public PSExtensionFile(IPSMimeContent in, File fileName)
      throws PSIllegalArgumentException
   {
      this(in, fileName, 0L);
   }

   /**
    * Construct an extension file object from MIME content. Ususally one
    * does this in order to save this content to the server under an extension
    * root. The extension information (i.e., what extension?) will be supplied
    * when saving.
    * 
    * @param   content MIME content whose stream is positioned at the start
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
    *
    * @exception PSIllegalArgumentException If <CODE>in</CODE> is
    * <CODE>null</CODE>, or <CODE>fileName</CODE> is <CODE>null</CODE>, or if
    * <CODE>fileName</CODE> specifies an empty or non-relative pathname.
    *
    */
   public PSExtensionFile(IPSMimeContent content, File fileName, long lastModified)
      throws PSIllegalArgumentException
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
    * @return     the newly created XML element node
    */
   public Element toXml(Document doc)
   {
      // create a PSXExtensionFile element, add the meta data, and
      // add the base64 content to it -- it is important that the
      // base64 alphabet contain no special XML chars (like gt, lt)
      Element root = PSXmlDocumentBuilder.addEmptyElement(
         doc, doc.getDocumentElement(), ms_nodeType);

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
   public void fromXml(Element sourceNode, IPSDocument parentDoc, 
                       List parentComponents)
      throws PSUnknownNodeTypeException
   {
      super.fromXml(sourceNode, parentDoc, parentComponents, ms_nodeType);
   }

   /** The element tag name in the XML document. */
   static final String ms_nodeType = "PSXExtensionFile";
}

