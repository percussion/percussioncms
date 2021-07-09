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

package com.percussion.data;

import com.percussion.server.PSRequest;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.xml.PSXmlTreeWalker;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * The PSXmlFieldExtractor class is used to extract data from the
 * XML document associated with the request.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSXmlFieldExtractor extends PSDataExtractor
{
   /**
    * Construct an object from its object store counterpart.
    *
    * @param   source      the object defining the source of this value
    */
   public PSXmlFieldExtractor(
      com.percussion.design.objectstore.PSXmlField source)
   {
      super(source);
      m_source = source.getName();
      setXmlFieldBase(null);   // no base specified at construction
   }

   /**
    * Extract a data value using the run-time data.
    *
    * @param   execData    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @return               the associated value; <code>null</code> if a
    *                        value is not found
    */
   public Object extract(PSExecutionData data)
      throws PSDataExtractionException
   {
      return extract(data, null);
   }

   /**
    * Extract a data value using the run-time data.
    *
    * @param   execData    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @param   defValue      the default value to use if a value is not found
    *
    * @return               the associated value; <code>defValue</code> if a
    *                        value is not found
    */
   public Object extract(PSExecutionData data, Object defValue)
      throws PSDataExtractionException
   {
      Object value = null;

      PSXmlTreeWalker walker = data.getInputDocumentWalker();
      if (walker == null) {
         PSRequest request = data.getRequest();
         if (request != null)
         {
            Document doc = request.getInputDocument();
            if (doc != null)
            {
               walker = new PSXmlTreeWalker(doc);
               data.setInputDocumentWalker(walker);
            }
         }
      }

      if (walker != null)
      {
         // if this does not have a base node (which depends on the
         // walker being positioned correctly) we can read from the root
         String base = m_sourceFromBase == null ? m_source : m_sourceFromBase;
         String strVal = walker.getElementData(base, false);

         if (((strVal == null) || (strVal.length() == 0)) && !(base.indexOf("@") >=0) )
         {
            Node holdCur = walker.getCurrent();
            
            // see if it's a file reference
            Element ele = walker.getNextElement(base, true);

            walker.setCurrent(holdCur);
            
            String urlRef = null;

            if (ele != null)
               urlRef = ele.getAttribute(XML_URL_REFERENCE_ATTRIBUTE);
               
            if ((urlRef != null) && (urlRef.length() > 0))
            {
               PSPurgableTempFile f =
                  data.getRequest().getTempFileResource(urlRef);
                  
               if (f == null)
                  try {
                     URL url = new URL(urlRef);
                     value = url;
                  } catch(MalformedURLException e)
                  {
                     Object[] args = {XML_URL_REFERENCE_ATTRIBUTE, 
                        URL.class.getName(), e.toString()};

                     throw new PSDataExtractionException(
                        IPSDataErrors.DATA_CANNOT_CONVERT_WITH_REASON,
                        args);
                  }
               else
                  value = f;
            } else
               value = strVal;
         } else
            value = strVal;
      }

      return (value == null) ? defValue : value;
   }

   /**
    * Set the base from which this XML field will be extracted. When
    * extracting from XML fields, 
    *
    * @param   base         the base field name to use
    */
   public void setXmlFieldBase(String base)
   {
      /* part of fix for bug id TGIS-4BWSL9
       *
       * when we have conditionals and other such context insensitive
       * objects, we need to deal with their being no base. In those
       * cases, we need to get the XML field from the root
       */
      if (base == null)
      {
         m_sourceFromBase = null;
         if (!m_source.startsWith("/"))
            m_source = "/" + m_source;
      }
      else
      {
         String source;
         if (m_source.startsWith("/"))
            source = m_source.substring(1);
         else
            source = m_source;

         m_sourceFromBase
            = PSXmlTreeWalker.getRelativeFieldName(base, source);
         if (m_sourceFromBase != null)
            m_source = source;
      }
   }


   private String m_source;
   private String m_sourceFromBase;
   public static final String XML_URL_REFERENCE_ATTRIBUTE = "PSXUrlReferenceAttribute";
}

