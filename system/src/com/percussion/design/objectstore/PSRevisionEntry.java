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
package com.percussion.design.objectstore;

import com.percussion.util.PSDateFormatISO8601;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.ParsePosition;
import java.util.Date;
import java.util.Objects;

/**
 * A PSRevisionEntry represents one change in a PSRevisionHistory object.
 *
 * @see com.percussion.design.objectstore.PSRevisionHistory
 */
public class PSRevisionEntry extends PSComponent
{
   /**
    * Construct a new revision entry.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * @param   agent The agent responsible for the change. Can be
    * <CODE>null</CODE>.
    *
    * @param   description A description of the change. Can be
    * <CODE>null</CODE>.
    *
    * @param   majorVersion   The major version number corresponding to this
    * entry.
    *
    * @param   minorVersion   The minor version number corresponding to this
    * entry.
    *
    * @param   time The date/time of the change. If <CODE>null</CODE>,
    * defaults to the current system time.
    * 
    */
   public PSRevisionEntry(
      String agent,
      String description,
      Date time,
      int majorVersion,
      int minorVersion)
   {
      if (agent == null)
         agent = "";
      if (description == null)
         description = "";
      if (time == null)
         time = new Date();

      m_majorVersion = majorVersion;
      m_minorVersion = minorVersion;
      m_agent = agent;
      m_desc = description;
      m_time = time;
   }

   /**
    * Package access no-args constructor for use with toXml/fromXml, etc.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    */
   PSRevisionEntry()
   {
      m_agent = null;
      m_desc = null;
      m_time = null;
   }

   /**
    * Gets the agent responsible for the change.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * @return   String
    */
   public String getAgent()
   {
      return m_desc;
   }

   /**
    * Gets a description of the change.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * @return   String
    */
   public String getDescription()
   {
      return m_desc;
   }

   /**
    * Gets the date/time of the change.
    *
    * @author   chad loder
    * 
    * @version 1.0 1999/7/8
    * 
    * @return   Date
    */
   public Date getTime()
   {
      return m_time;
   }

   /**
    * Gets the major version.
    *
    * @author   chadloder
    * 
    * @version 1.2 1999/07/12
    * 
    * @return   int
    */
   public int getMajorVersion()
   {
      return m_majorVersion;
   }

   /**
    * Gets the minor version.
    *
    * @author   chadloder
    * 
    * @version 1.3 1999/07/12
    * 
    * @return   int
    */
   public int getMinorVersion()
   {
      return m_minorVersion;
   }

   /**
    * Gets the version as a string with the format <CODE>M.m</CODE>,
    * where <CODE>M</CODE> is the major version and <CODE>m</CODE> is
    * the minor.
    *
    * @author   chadloder
    * 
    * @version 1.3 1999/07/12
    * 
    * @return   String
    */
   public String getVersion()
   {
      return m_majorVersion + "." + m_minorVersion;
   }

   /**
    * Returns a string representation of this entry.
    *
    * @author   chadloder
    * 
    * @version 1.2 1999/07/12
    * 
    * @return   String
    */
   public String toString()
   {
      return (getVersion() + ": "
         + new java.text.SimpleDateFormat().format(m_time)) + ": " + m_agent
         + ": " + m_desc;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSRevisionEntry)) return false;
      if (!super.equals(o)) return false;
      PSRevisionEntry that = (PSRevisionEntry) o;
      return m_majorVersion == that.m_majorVersion &&
              m_minorVersion == that.m_minorVersion &&
              Objects.equals(m_agent, that.m_agent) &&
              Objects.equals(m_desc, that.m_desc) &&
              Objects.equals(m_time, that.m_time);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_agent, m_desc, m_time, m_majorVersion, m_minorVersion);
   }

   /* **************  IPSComponent Interface Implementation ************** */
   
   /**
    * This method is called to create a PSXRevisionEntry XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXRevisionEntry defines a list of changes associated with a
    *       component or document.
    *    --&gt;
    *    &lt;!ELEMENT PSXRevisionEntry   (agent, description, time)&gt;
    *    &lt;!ELEMENT name   (#PCDATA)&gt;
    *    &lt;!ELEMENT agent (#PCDATA)&gt;
    *    &lt;!ELEMENT time (#PCDATA)&gt;
    *    &lt;!--
    *         The time is in ISO8601 format
    *    --&gt;
    *
    *    &gt;
    * </code></pre>
    *
    * @return     the newly created PSXRevisionEntry XML element node
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(ms_nodeType);

      // version
      root.setAttribute("majorVersion", "" + m_majorVersion);
      root.setAttribute("minorVersion", "" + m_minorVersion);

      // agent
      PSXmlDocumentBuilder.addElement(doc, root, "agent", m_agent);

      // description
      PSXmlDocumentBuilder.addElement(doc, root, "description", m_desc);

      // time
      PSXmlDocumentBuilder.addElement(
         doc,
         root,
         "time",
         new PSDateFormatISO8601().format(m_time));

      return root;
   }
   
   /**
    * This method is called to populate a PSRevisionEntry Java object
    * from a PSXRevisionEntry XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXRevisionEntry
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc, 
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_nodeType);
      }
      
      if (!ms_nodeType.equals (sourceNode.getNodeName()))
      {
         Object[] args = { ms_nodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);

      // version
      m_majorVersion = Integer.parseInt(walker.getElementData("majorVersion"));
      m_minorVersion = Integer.parseInt(walker.getElementData("minorVersion"));

      // agent
      m_agent = walker.getElementData("agent");
      
      // description
      m_desc = walker.getElementData("description");
      
      // time
      PSDateFormatISO8601 df = new PSDateFormatISO8601();
      String time = walker.getElementData("time");
      if (time == null)
         m_time = new Date();
      else
         m_time = df.parse(time, new ParsePosition(0));
   }
   
   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    * 
    * @param   cxt The validation context.
    * 
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      if (m_majorVersion < 1)
         cxt.validationError(this, 0, "Invalid version: " + getVersion());

      if (m_minorVersion < 0)
         cxt.validationError(this, 0, "Invalid version: " + getVersion());

      if (m_agent == null)
         cxt.validationError(this, 0, "Revision entry agent is null");

      if (m_desc == null)
         cxt.validationError(this, 0, "Revision entry description is null.");

      if (m_time == null)
         cxt.validationError(this, 0, "Revision entry time is null.");
   }

   /** the agent responsible for the change */
   private String m_agent;

   /** the description of the change */
   private String m_desc;

   /** the date/time of the change */
   private Date m_time;

   /** the major version */
   private int m_majorVersion;

   /** the minor version */
   private int m_minorVersion;

   static final String ms_nodeType = "PSXRevisionEntry";
}
