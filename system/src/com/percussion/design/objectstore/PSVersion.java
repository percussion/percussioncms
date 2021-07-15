/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang3.time.FastDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.ParseException;
import java.util.Date;

/**
 * This class represents a particular version of a feature.
 */
public class PSVersion
{

   /**
    * The constructor for this class.
    *
    * @param versionNumber The version number this object represents
    * @param dateIntroduced The date this version was introduced.  Format is YYYYMMDD
    * @throws IllegalArgumentException If the date string passed in cannot
    * be parsed into a date.
    * @roseuid 39FD8CBC038A
    */
   public PSVersion(int versionNumber, String dateIntroduced)
   {
      try
      {
         m_versionNumber = versionNumber;

         // parse the date
         getFormatter();
         m_dateIntroduced = m_formatter.parse(dateIntroduced);
      }
      catch(ParseException e)
      {
         throw new IllegalArgumentException("dateIntroduced must be in format "
               + DATE_FORMAT + ": " + dateIntroduced);
      }
   }

   /**
    * Constructor for this class.  Must be passed a valid PSXVersion node
    *
    * @param sourceNode the Xml element node from which to construct this object.
    * @throws PSUnknownNodeTypeException if node is not found or invalid
    * @see PSFeatureSet#fromXml(Document)  for more information.
    */
   public PSVersion(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null){
         throw new PSUnknownNodeTypeException(
         IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_nodeName);
      }

      // make sure we got the correct type node
      if (false == ms_nodeName.equals(sourceNode.getNodeName())){
         Object[] args = { ms_nodeName, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      // get this version's attributes
      String sTemp = tree.getElementData("Number");
      try {
         m_versionNumber = Integer.parseInt(sTemp);
      } catch (Exception e) {
         Object[] args = { ms_nodeName, "Number",
                           ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      sTemp = tree.getElementData("Introduced");
      try {
         getFormatter();
         m_dateIntroduced = m_formatter.parse(sTemp.trim());
      } catch (Exception e) {
         Object[] args = { ms_nodeName, "Introduced",
                           ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
   }

   /**
    * Returns the version number of this version
    * @roseuid 39FD8D360157
    */
   public int getNumber()
   {
      return m_versionNumber;
   }

   /**
    * Returns the date this version was introduced
    *
    * @return The date introduced as a string, format is YYYYMMDD
    * @roseuid 39FD8D520399
    */
   public String getDateString()
   {
      return m_formatter.format(m_dateIntroduced);
   }

   /**
    * Returns the date this version was introduced
    *
    * @return The date introduced.
    * @roseuid 39FD8D7B007D
    */
   public Date getDate()
   {
      return m_dateIntroduced;
   }

   /**
    * creates the SimpleDateFormatter used to process dates
    */
   private void getFormatter()
   {
      m_formatter = FastDateFormat.getInstance(DATE_FORMAT);
   }

   /**
    * The number of this version
    */
   private int m_versionNumber = 0;

   /**
    * The date this version was introduced.
    */
   private Date m_dateIntroduced = null;

   /**
    * Formatter to parse date to and from a string
    */
   private FastDateFormat m_formatter = null;

   /**
    * Format string to use with the formatter
    */
   private static final String DATE_FORMAT = "yyyyMMdd";

   /**
    * The name of the Xml node this object is serialized to and from.
    */
   static final String ms_nodeName = "PSXVersion";
}
