/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
