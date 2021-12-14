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

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Class to represent an associated file within metadata for a content editor 
 * control, as defined by the &lt;psxctl:AssociatedFileList&gt; node in 
 * <code>sys_LibraryControlDef.dtd</code>
 */
public class PSFileDescriptor extends PSComponent
{
   /**
    * Initializes a newly created <code>PSFileDescriptor</code> object, from
    * an XML representation.  See {@link #toXml(Document)} for the format.
    *
    * @param sourceNode the XML element node to construct this object from.
    *    Cannot be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>
    * @throws PSUnknownNodeTypeException if the XML representation is not
    *    in the expected format
    */
   public PSFileDescriptor(Element sourceNode)
         throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException("sourceNode cannot be null.");
      fromXml(sourceNode, null, null);
   }
   
   /**
    * This method is called to populate an object from an XML
    * element node. An element node may contain a hierarchical structure,
    * including child objects. The element node can also be a child of
    * another element node.  See {@link #toXml(Document)} for the format.
    *
    * @param sourceNode element with name specified by {@link #XML_NODE_NAME}
    * @param parentDoc ignored.
    * @param parentComponents ignored.
    * 
    * @throws IllegalArgumentException if <code>sourceNode</code> is <code>null</code>.
    * @throws PSUnknownNodeTypeException  if an expected XML element is missing,
    *    or <code>null</code>
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       List parentComponents)
         throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");
      
      validateElementName(sourceNode, XML_NODE_NAME);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      m_name = sourceNode.getAttribute(XML_ATTR_NAME);
      if (m_name == null || m_name.trim().length() == 0)
      {
         Object[] args = {sourceNode.getTagName(), XML_ATTR_NAME, "empty"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      
      m_type = getEnumeratedAttribute(tree, XML_ATTR_TYPE, TYPE_ENUM);
      
      m_mimetype = sourceNode.getAttribute(XML_ATTR_MIMETYPE);
      m_originalLocation = sourceNode.getAttribute(XML_ATTR_ORIG_LOC);
      
      m_fileLocation = getRequiredElement(tree, XML_EL_FILE_LOC);
      m_timestamp = tree.getElementData(XML_EL_FILE_TIMESTAMP);
      if (m_timestamp == null)
         m_timestamp = "";
   }
   
   
   /**
    * This method is called to create an XML element node with the
    * appropriate format for this object. Format expected is defined by the
    * sys_LibraryControlDef.dtd DTD:
    *
    * <pre><code>
    * &lt;!ELEMENT psxctl:AssociatedFileList (psxctl:FileLocation, 
    *    psxctl:Timestamp)>
    * &lt;!ATTLIST psxctl:AssociatedFileList
    *    name CDATA #REQUIRED                                 
    *    type (script | image | include | css | other) "other"
    *    mimetype CDATA #IMPLIED                              
    *    originalLocation CDATA #IMPLIED                      
    * >
    * &lt; !ELEMENT psxctl:FileLocation (#PCDATA)>
    * &lt; !ELEMENT psxctl:Timestamp (#PCDATA)>
    * </code></pre>
    *
    * @param doc The XML document being constructed, needed to create new
    *    elements.  Cannot be <code>null</code>.
    *    
    * @return    the newly created XML element node
    * 
    * @throws IllegalArgumentException if <code>doc</code> is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, m_name);
      root.setAttribute(XML_ATTR_TYPE, m_type);
      root.setAttribute(XML_ATTR_MIMETYPE, m_mimetype);
      root.setAttribute(XML_ATTR_ORIG_LOC, m_mimetype);
      
      PSXmlDocumentBuilder.addElement(doc, root, XML_EL_FILE_LOC, 
         m_fileLocation);
      PSXmlDocumentBuilder.addElement(doc, root, XML_EL_FILE_TIMESTAMP, 
         m_timestamp == null ? "" : m_timestamp);
         
      return root;
   }
   
   /**
    * Get the name of the file
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Get the type of file, one of the {@link #TYPE_ENUM} values
    * 
    * @return The type, never <code>null</code> or empty.
    */
   public String getType()
   {
      return m_type;
   }
   
   /**
    * Get the mime type if available.
    * 
    * @return The mime type, never <code>null</code>, may be empty.
    */
   public String getMimeType()
   {
      return m_mimetype;
   }
   
   /**
    * Get the original location of the file if known.
    * 
    * @return The original location, never <code>null</code>, may be empty.
    */
   public String getOriginalLocation()
   {
      return m_originalLocation;
   }
   
   /**
    * Get the path of the file on the Rhythmyx server relative to the root.
    * 
    * @return The location, never <code>null</code>, may be empty.
    */
   public String getFileLocation()
   {
      return m_fileLocation;
   }
   
   /**
    * Get the time this file was saved on the server, if known.
    * 
    * @return The timestamp, in the fomat <code>YYYYMMDD HH:MM:SS</code>, never 
    * <code>null</code>, may be empty.
    */
   public String getTimeStamp()
   {
      return m_timestamp;
   }
   
   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component.
    *
    * @param source object to be shallow copied; may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if source is <code>null</code> or
    * not an instance of <code>PSFileDescriptor</code>
    */
   public void copyFrom(PSComponent source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      if (!(source instanceof PSFileDescriptor))
         throw new IllegalArgumentException("source wrong type");

      PSFileDescriptor file = (PSFileDescriptor)source;
      
      m_id = file.m_id;      
      m_name = file.m_name;
      m_type = file.m_type;
      m_mimetype = file.m_mimetype;
      m_fileLocation = file.m_fileLocation;
      m_originalLocation = file.m_originalLocation;
      m_timestamp = file.m_timestamp;
   }
 
   /**
    * Test if the provided object and this are equal.
    *
    * @param obj the object to compare to.
    * @return <code>true</code> if this and o are equal, 
    *    <code>false</code> otherwise.
    */
   public boolean equals(Object obj)
   {
      boolean isEqual = true;
      if (!(obj instanceof PSFileDescriptor))
         isEqual = false;
      else
      {
         PSFileDescriptor other = (PSFileDescriptor)obj;
         if (m_id != other.m_id)
            isEqual = false;
         else if (!m_name.equals(other.m_name))
            isEqual = false;
         else if (!m_type.equals(other.m_type))
            isEqual = false;
         else if (!m_mimetype.equals(other.m_mimetype))
            isEqual = false;
         else if (!m_fileLocation.equals(other.m_fileLocation))
            isEqual = false;
         else if (!m_originalLocation.equals(other.m_originalLocation))
            isEqual = false;
         else if (!m_timestamp.equals(other.m_timestamp))
            isEqual = false;
      }
      
      return isEqual;
   }
   
   /**
    * Returns a hash code value for the object. See 
    * {@link java.lang.Object#hashCode() Object.hashCode()} for more info.
    */
   public int hashCode()
   {
      return m_id + m_name.hashCode() + m_type.hashCode() + 
         m_mimetype.hashCode() + m_fileLocation.hashCode() + 
         m_originalLocation.hashCode() + m_timestamp.hashCode();
   }

   /**
    * Array of allowed types.
    */
    public static final String[] TYPE_ENUM = {"other", "script", "image", 
      "include", "css"};
    
    /**
     * Name of the file, never <code>null</code>, empty, or modified after ctor.
     */ 
    private String m_name;
    
    /**
     * Type of file, one of the {@link #TYPE_ENUM} values, never 
     * <code>null</code>, empty, or modified after ctor.
     */
    private String m_type;
    
    /**
     * The specific mimetype of the file, never <code>null</code> or modified 
     * after ctor, may be empty.
     */
    private String m_mimetype;
    
    /**
     * The original location of the file, never <code>null</code> or modified 
     * after ctor, may be empty.
     */
    private String m_originalLocation;
    
    /**
     * The location of the file relative to the rx root, never 
     * <code>null</code>, empty, or modified.
     */
    private String m_fileLocation;
    
    /**
     * The timestamp of the file when it was uploaded to the server, 
     * never <code>null</code> or modified after ctor, may be empty.
     */
    private String m_timestamp;
 
 
 
   /**
    * Name of this object's root XML element
    */
   public static final String XML_NODE_NAME = "psxctl:FileDescriptor";

   // private xml constants
   private static final String XML_ATTR_NAME = "name";
   private static final String XML_ATTR_TYPE = "type";
   private static final String XML_ATTR_MIMETYPE = "mimetype";
   private static final String XML_ATTR_ORIG_LOC = "originalLocation";
   private static final String XML_EL_FILE_LOC = "psxctl:FileLocation";
   private static final String XML_EL_FILE_TIMESTAMP = "psxctl:Timestamp";
}
