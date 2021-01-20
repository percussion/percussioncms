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

package com.percussion.xml;

//java
import java.io.ByteArrayInputStream;

import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XNIException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class is used for finding the root element of a DTD.
 */
public class PSSaxDtdParser extends SAXParser
{
   /**
    * Returns the root of the DTD. Scans the DTD string for "DOCTYPE" and then
    * returns the root element.
    * @param strDtd the DTD in string format, may not be <code>null</code>
    * or empty
    * @return the root of the DTD, or <code>null</code> if it fails to find the
    * root of the DTD
    * @throw IllegalArgumentException if strDtd is <code>null</code> or empty
    */
   protected String getRootElement(String strDtd)
   {
      if ((strDtd == null) || (strDtd.trim().length() == 0))
         throw new IllegalArgumentException("strDtd may not be null or empty");

      ByteArrayInputStream bis = new ByteArrayInputStream(strDtd.getBytes());
      InputSource is = new InputSource(bis);
      try
      {
         parse(is);
      }
      catch (SAXException se)
      {
         String err = se.getMessage();
         if (err.startsWith(DTDROOTELEMENT_TAG))
         {
            String ret = err.substring(DTDROOTELEMENT_TAG.length(),
               err.length());
            return ret;
         }
      }
      catch (Exception ex)
      {
      }
      return null;
   }

   /**
    * @see org.apache.xerces.xni.XMLDocumentHandler
    */
   public void doctypeDecl(String rootElement, String publicId, String systemId,
      Augmentations augs) throws XNIException
   {
      throw new XNIException(DTDROOTELEMENT_TAG + rootElement);
   }

   /**
    * tag for identifying root element in the dtd, used in
    * <code>getRootElement</code> method
    */
   protected static final String DTDROOTELEMENT_TAG = "DTDROOTELEMENT=";

}
