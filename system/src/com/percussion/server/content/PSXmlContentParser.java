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

package com.percussion.server.content;

import com.percussion.content.IPSMimeContentTypes;
import com.percussion.data.PSXmlFieldExtractor;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestParsingException;
import com.percussion.util.*;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * The PSXmlContentParser class is used by the PSRequestParser to handle
 * content of type text/xml or application/xml.
 *
 * @see        com.percussion.server.PSRequestParser
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSXmlContentParser extends PSContentParser {

   /**
    * Construct an XML content parser.
    */
   public PSXmlContentParser()
   {
      super();
   }

   /**
    * Parse the specified input stream and add it to the appropriate
    * place in the request structure.
    *
    * @param      request         the request object to store the contents in
    *
    * @param      contentType      the Content-Type HTTP header value
    *
    * @param      charset         the character encoding of the content
    *
    * @param      content         the input stream containing the data
    *
    * @param      length         the amount of data to read
    *
    * @exception   IOException      if an i/o error occurs
    *
    * @exception   PSRequestParsingException
    *                              if the content is invalid or the
    *                              contentType is not supported
    */
   public void parse(
      PSRequest request, String contentType, String charset,
      PSInputStreamReader content, int length)
      throws   IOException,
               PSRequestParsingException
   {
      String mimeType = getMimeType( contentType );

      if (!isSupportedContentType(mimeType)) {
         //FB: DMI_INVOKING_TOSTRING_ON_ARRAY 1-17-16
         Object[] args = { mimeType, Arrays.toString(ARRAY_SUPPORTED_TYPES) };
         throw new PSRequestParsingException(
                     IPSServerErrors.PARSER_UNSUPPORTED_CONTENT_TYPE, args);
      }
      else if (length == 0)   // return an empty document
         return;

      /* the amount of data we can read from the socket may
       * not match the amount of data in the Content-Length header.
       * Since char's are UTF-16 compliant, the only reason this should
       * happen is if the Java stream converter mixes something up. We
       * won't really see this, as it will probably cause some strange
       * error (such as a parse error for an invalid character).
       * Once we run across this scenario, we can take steps towards
       * resolving it.
       */
      PSPurgableTempFile f = readContentIntoPurgableTempFile(
         "psx", ".xml", null, content, length);
      int newLength = (int)f.length();
      if (length != newLength) {
         Object[] args = { request.getUserSessionId(), mimeType,
            String.valueOf(length), String.valueOf(newLength) };
         com.percussion.log.PSLogManager.write(
            new com.percussion.log.PSLogServerWarning(
            IPSServerErrors.CONTENT_LENGTH_DOES_NOT_MATCH_DATA_READ,
            args, true, "PSXmlContentParser"));
         length = newLength;
      }

      /* Get the parameter which defines how the xml document should be treated.
       * This parameter may have multiple values, so get the first one delimited
       * by ';'
       */
      boolean validateXML = false;
      String xmlDocValue = request.getParameter(
         PSRequest.REQ_XML_DOC_FLAG);
      if(xmlDocValue != null && xmlDocValue.trim().length() != 0)
      {
         StringTokenizer st = new StringTokenizer(xmlDocValue, ";");
         if(st.hasMoreTokens())
            xmlDocValue = st.nextToken();

         if(xmlDocValue.equalsIgnoreCase(PSRequest.XML_DOC_VALIDATE))
            validateXML = true;
      }

      // TODO: we seems OVER USE FILE here
      //          memory content -> temp file -> memory Document
      //       It would be quicker and simpler to make
      //          memory content -> memory Document
      Document doc = getXMLDocument(f, charset, validateXML);
      f.release();  // the temp file is no longer needed

      /* Check for embedded file urls (not allowed, security reasons) */
      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
      String str =
         walker.getElementData("@" +
            PSXmlFieldExtractor.XML_URL_REFERENCE_ATTRIBUTE,
            true);
      if (str != null)
      {
         //Fail!  Bad user!  Bad!
         doc = null;
      }
      request.setInputDocument(doc);
   }

   /**
    * Creates the xml document from the content in the supplied file. Uses the
    * <code>charset</code> for reading the contents. If it is <code>null</code>
    * uses rhythmyx standard encoding character set. Validates the xml document
    * if <code>validate</code> is <code>true</code>.
    *
    * @param xmlFile the file from which xml document needs to be created, may
    * not be <code>null</code>
    * @param charset the character set to use for encoding, if <code>null</code>
    * uses rhythmyx standard encoding character set.
    * @param validate if <code>true</code> validates the created xml document,
    * otherwise not.
    *
    * @return the created xml document, never <code>null</code>
    *
    * @throws IOException if any file i/o error occurs
    * @throws PSRequestParsingException if there is any exception creating the
    * document or validating it.
    */
   static Document getXMLDocument(File xmlFile, String charset,
      boolean validate)
      throws IOException, PSRequestParsingException
   {
      if(xmlFile == null)
         throw new IllegalArgumentException("xmlFile can not be null");

      FileInputStream fin = null;
      Document doc = null;

      try {
         fin = new FileInputStream(xmlFile);
         if(charset == null)
            charset = PSCharSetsConstants.rxStdEnc();

         Reader rdr = new InputStreamReader(fin,
            PSCharSets.getJavaName(charset));
         doc = PSXmlDocumentBuilder.createXmlDocument(rdr, validate);

      } catch (SAXParseException e) {
         BufferedReader brdr = new BufferedReader(
            new InputStreamReader(new FileInputStream(xmlFile)));
         Object[] args
            = { getSaxExceptionContextMessage(e) + "  " +
               getSaxExceptionContextData(e, brdr) };
         throw new PSRequestParsingException(
            IPSServerErrors.XML_PARSER_SAX_ERROR, args);
      } catch (SAXException e) {
         Object[] args = { e.getMessage() };
         throw new PSRequestParsingException(
            IPSServerErrors.XML_PARSER_SAX_ERROR, args);
      } finally {
         if (fin != null) {
            try { fin.close(); }
            catch (Exception e) { /* ignore, we're done */ }
         }
      }
      return doc;
   }

   /**
    * Create an error message containing the SAX exception's context
    * information.
    *
    * @param   e                  the SAX exception
    *
    * @return   a string containing the SAX exception info, line number,
    *            and column number
    */
   public static String getSaxExceptionContextMessage(SAXParseException e)
   {
      StringBuffer errorMsg = new StringBuffer();
      getSaxExceptionContextMessage(errorMsg, e);
      return errorMsg.toString();
   }

   /**
    * Create an error message containing the SAX exception's context
    * information.
    *
    * @param   buf               a buffer to store the output into
    *
    * @param   e                  the SAX exception
    *
    * @return   a string containing the SAX exception info, line number,
    *            and column number
    */
   public static void getSaxExceptionContextMessage(
      StringBuffer buf, SAXParseException e)
   {
      buf.append(e.getMessage());
      buf.append(" (line ");
      buf.append(e.getLineNumber());
      buf.append("; col ");
      buf.append(e.getColumnNumber());
      buf.append(") ");
   }

   /**
    * Create an error message containing the data in the specified range
    * for the exception.
    *
    * @param   e                  the SAX exception
    *
    * @param   source            a reader with the source XML data
    *
    * @return   a string containing any contextual text which can be found
    */
   public static String getSaxExceptionContextData(
      SAXParseException e, BufferedReader source)
   {
      StringBuffer errorMsg = new StringBuffer();
      getSaxExceptionContextData(errorMsg, e, source);
      return errorMsg.toString();
   }

   /**
    * Create an error message containing the data in the specified range
    * for the exception.
    *
    * @param   buf               a buffer to store the output into
    *
    * @param   e                  the SAX exception
    *
    * @param   source            a reader with the source XML data
    *
    * @return   a string containing any contextual text which can be found
    */
   public static void getSaxExceptionContextData(
      StringBuffer buf, SAXParseException e, BufferedReader source)
   {
      if (e.getLineNumber() > 0) {
         String   curLine;
         int      iNeedLine = e.getLineNumber();

         try {
            for (int i = 0; i <= iNeedLine; ) {
               i++;
               curLine = source.readLine();
               if (i >= (iNeedLine-1))
                  buf.append(curLine);
            }
         } catch (java.io.IOException ioe) {
            buf.append(ioe.toString());
         }
      }
   }

   /**
    * Create an error message containing the SAX exception's context
    * information.
    *
    * @param   e                  the SAX exception
    *
    * @param   source            a reader with the source XML data
    *
    * @return   a string containing the SAX exception info, line number,
    *            column number and any contextual text which can be found
    */
   public static String getSaxExceptionDescription(
      SAXException e, BufferedReader source)
   {
      String errorMsg = "";

      if (e instanceof org.xml.sax.SAXParseException) {
         SAXParseException se = (SAXParseException)e;

         StringBuffer buf = new StringBuffer();
         buf.append(getSaxExceptionContextMessage(se));

         if (source != null) {
            buf.append("  [data in range: ");
            getSaxExceptionContextData(buf, se, source);
            buf.append("]");
         }

         errorMsg = buf.toString();
      }
      else if (e.getMessage() != null)
         errorMsg = e.getMessage();
      else
         errorMsg = e.toString();

      return errorMsg;
   }


   /**
    * Extracts the mime type from the supplied contentType.
    *
    * @param contentType The value from the ContentType HTTP header.
    *
    * @throws PSRequestParsingException if contentType is malformed
    **/
   private String getMimeType( String contentType )
      throws PSRequestParsingException
   {
      try
      {
         return PSBaseHttpUtils.parseContentType( contentType, null );
      }
      catch ( IllegalArgumentException e )
      {
         throw new PSRequestParsingException( e.getLocalizedMessage());
      }
   }

   /**
    * Get the content type(s) supported by this driver.
    *
    * @return      an array containing the supported content type(s)
    */
   public String[] getSupportedContentTypes()
   {
      return ARRAY_SUPPORTED_TYPES;
   }


   private static final String[]   ARRAY_SUPPORTED_TYPES =
   {
      IPSMimeContentTypes.MIME_TYPE_APPLICATION_XML,
      IPSMimeContentTypes.MIME_TYPE_TEXT_XML
   };
}

