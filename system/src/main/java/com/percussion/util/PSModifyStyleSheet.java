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

package com.percussion.util;

import com.percussion.error.PSExceptionUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * The PSModifyStyleSheet class is used to modify user-defined style-sheets
 *   to our latest version/format.
 * <p>
 * The latest xslt specs are located at:
 * <A HREF="http://www.w3.org/TR/xslt">XSLT-SPECS</A>.
 * <p>
 *   The current version we support is:
 * <A HREF="http://www.w3.org/TR/1999/REC-xslt-19991116">XSLT-19991116</A>.
 *
 * @author     DG
 * @version    1.0
 * @since      1.0
 */
public class PSModifyStyleSheet
{
   Logger log = LogManager.getLogger(PSModifyStyleSheet.class);
   /**
    * External construction needed for installer.
    */
   public PSModifyStyleSheet()
   {
      super();
   }

   /* function to run the guts
   * @param   strStyleSheet      the file name of the style sheet to be modified
    *                     (style-sheet-filename)
    *@returns true for error, false for success
   */

   public boolean modify(String strStyleSheet)
   {
      File xslFile = null;
      FileInputStream fIn = null;
      org.w3c.dom.Document inDoc = null;
      PSXmlTreeWalker walker = null;
      try {
         xslFile = new File(strStyleSheet);
         fIn = new FileInputStream(xslFile);
         inDoc = PSXmlDocumentBuilder.createXmlDocument(fIn, false);
         fIn.close();
      } catch (java.io.IOException | org.xml.sax.SAXException ioE) {
         log.error("An IO exception occurred accessing the file.");
         log.error(ioE.toString());
         return true;
      }

      /* Need to alter the xsl:stylesheet member's attributes */
      org.w3c.dom.Element e = inDoc.getDocumentElement();

      if (e == null)
      {
         log.error("No root element specified in this document!");
         return true;
      } else {
         if (!e.getTagName().equals("xsl:stylesheet"))
         {
           log.error("Document not stylesheet!");
            return true;
         }

         /* check to see if there is no version attribute and
            change the attributes if need be */
         String version = e.getAttribute(XSL_VERSION_ATTRIBUTE_NAME);
         if ((version == null) || (version.equals("")))
         {
            log.info("Updating stylesheet attributes");
            e.setAttribute(XSL_VERSION_ATTRIBUTE_NAME, CURRENT_XSL_VERSION);
            e.setAttribute(XSL_NAMESPACE_ATTRIBUTE_NAME, CURRENT_XSL_NAMESPACE_REF);
         } else
         {
            /* version already there, no changes to be made */
            return false;
         }
      }

      FileOutputStream fOut = null;
      try {
         fOut = new FileOutputStream(xslFile);
         PSXmlDocumentBuilder.write(inDoc, fOut);
         fOut.close();
      } catch (java.io.IOException ioE) {
         log.error("Error writing updated file: {}", ioE);
         return true;
      }

      return false;
   }

   /**
    * This will convert the server root for the entity reference in the provided
    * XSL from "./../../DTD/HTMLlat1x.ent" to "./../../DTD/HTMLlat1x.ent".
    * 
    * @param strStyleSheet the file name of the style sheet to be modified
    * @returns <code>true</code> for error, <code>false</code> for success
    */

   public boolean convertServerRoot(String strStyleSheet)
   {
      File xslFile = null;

      StringBuilder buffer;

         xslFile = new File(strStyleSheet);
         try(FileInputStream fIn = new FileInputStream(xslFile)){
            try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
               int read = 0;
               byte[] buf = new byte[1024];
               while ((read = fIn.read(buf)) >= 0) {
                  out.write(buf, 0, read);
                  if (read < buf.length)
                     break;
               }
               out.flush();

               buffer = new StringBuilder(out.toString(PSCharSets.getStdName("UTF-8")));
            }
      }
      catch (IOException e)
      {
         String fileName = "";
         fileName = xslFile.toString();
         log.error("An IO exception occurred accessing the file: {} Error: {}" , fileName,
                 PSExceptionUtils.getMessageForLog(e));
         return true;
      } 
      
      String strTest = "./../../DTD/HTMLlat1x.ent";
      int index = buffer.toString().indexOf(strTest);
      if (index != -1)
         buffer.replace(index, index+strTest.length(), 
                        "./../../DTD/HTMLlat1x.ent");
         
      strTest = "./../../DTD/HTMLsymbolx.ent";
      index = buffer.toString().indexOf(strTest);
      if (index != -1)
         buffer.replace(index, index+strTest.length(), 
                        "./../../DTD/HTMLsymbolx.ent");
         
      strTest = "./../../DTD/HTMLspecialx.ent";
      index = buffer.toString().indexOf(strTest);
      if (index != -1)
         buffer.replace(index, index+strTest.length(), 
                        "./../../DTD/HTMLspecialx.ent");


         try(FileOutputStream fOut = new FileOutputStream(xslFile)){
            fOut.write(buffer.toString().getBytes());
            fOut.flush();
         } catch (IOException e) {
            log.error("Error writing updated file",e);
            return true;
         }

      return false;
   }

   /**
    * This is the server application's entry point
    *
    * @param   args      the arguments supplied to the Style Sheet Modifier
    *                     (style-sheet-filename)
    */
   public static void main(java.lang.String[] args)
   {
      if (args.length != 1) {
         System.out.println("Usage: java PSModifyStyleSheet <style-sheet>");
         System.exit(1);
      }

      PSModifyStyleSheet modifyXSL = new PSModifyStyleSheet();
      System.exit(modifyXSL.convertServerRoot(args[0]) == true ? 1 : 0);
   }

   public static final String CURRENT_XSL_VERSION = "1.0";
   public static final String CURRENT_XSL_NAMESPACE_REF = "http://www.w3.org/1999/XSL/Transform";
   public static final String XSL_VERSION_ATTRIBUTE_NAME = "version";
   public static final String XSL_NAMESPACE_ATTRIBUTE_NAME = "xmlns:xsl";
}
