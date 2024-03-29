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

package com.percussion.data;

import com.percussion.server.PSRequest;
import com.percussion.util.PSCharSets;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


/**
 * The PSCssStyleSheetMerger class implements CSS support for the
 * IPSStyleSheetMerger interface. This processor does not actually generate
 * HTML output by merging the style sheet. Rather, it depends upon the
 * user agent's support of CSS. It creates an HTML document which
 * contains a reference to the style sheet and the actual XML data.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSCssStyleSheetMerger extends PSStyleSheetMerger
{
   /**
    * Create an XML + CSS merger.
    */
   public PSCssStyleSheetMerger() {
      super();
   }

   /**
    * Merge the CSS style sheet defined in the XML document to generate
    * HTML output. The <code>stylesheet</code> processing instruction
    * must exist in the XML document and refer to a CSS style sheet.
    *
    * @param   req         the request object (may be <code>null</code>)
    *
    * @param   doc         the XML document to be processed
    *
    * @param   out         the output stream to which the results will be
    *                      written
    *
    * @param   styleFile   the style sheet to use
    * 
    * @param encoding character encoding to be used in XSL stylesheet output
    * element. Can be <code>null</code> or empty. (Not used for CSS)
    *
    * @throws   PSConversionException
    *                        if the conversion fails
    *
    * @throws  PSUnsupportedConversionException
    *                      if the style sheet defined in the XML document
    *                      is of an unsupported type
    */
   public void merge(
      PSRequest req, Document doc, OutputStream out, java.net.URL styleFile, 
         String encoding)
      throws   PSConversionException, PSUnsupportedConversionException
   {
      /* CSS needs the URL to be HTTP accessible so convert its format.
       * The URL we were passed in is either of the form
       * file:ApplicationRoot/FileName or it's already an external URL.
       * We only need to fixup file URL's by prepending /ServerRoot/
       * and removing the protocol (file:)
       */

      String sURL;
      if (!styleFile.getProtocol().equalsIgnoreCase("file"))
         sURL = styleFile.toExternalForm();
      else {
         sURL = styleFile.getFile();
         if (sURL == null)   /* some FILE URLs use the file as host ?! */
            sURL = styleFile.getHost();

         sURL = com.percussion.server.PSServer.makeRequestRoot(sURL);
      }

      try {
         BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(out,
            PSCharSets.rxJavaEnc()));

//         ProcessingInstruction pi = PSStyleSheet.getPINode(doc);
//         if (pi != null) {
//            String newData = "type=\"text/css\" href=\"" + sURL + "\"";
//            pi.setData(newData);
//         }
//         PSXmlDocumentBuilder.write(doc, buf);

         // mark it as an HTML document
         buf.write("<HTML>\r\n");
         buf.write("<BODY>\r\n");

         // send the style sheet link
         buf.write("<LINK href=\"");
         buf.write(sURL);
         buf.write("\" rel=\"stylesheet\" type=\"text/css\" />\r\n");

         // and the xml document in the body
         PSXmlDocumentBuilder.write(doc.getDocumentElement(), buf);

         buf.write("</BODY>\r\n");   // close the BODY tag
         buf.write("</HTML>\r\n");   // close the HTML tag

         buf.flush();   // and flush it to the caller's stream
      }
      catch (Throwable t) {
         throwConversionException(doc, styleFile, t.toString());
      }
   }
}

