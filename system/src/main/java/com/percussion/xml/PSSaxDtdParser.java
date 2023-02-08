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

package com.percussion.xml;

//java

import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XNIException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
      catch (SAXException | IOException se)
      {
         String err = se.getMessage();
         if (err.startsWith(DTDROOTELEMENT_TAG))
         {
            String ret = err.substring(DTDROOTELEMENT_TAG.length(),
               err.length());
            return ret;
         }
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
