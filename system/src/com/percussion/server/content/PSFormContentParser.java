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

package com.percussion.server.content;

import com.percussion.content.IPSMimeContentTypes;
import com.percussion.log.PSLogManager;
import com.percussion.log.PSLogServerWarning;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestParsingException;
import com.percussion.server.PSServer;
import com.percussion.util.*;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.w3c.dom.Document;

/**
 * The PSFormContentParser class is used by the PSRequestParser to handle
 * content of types application/x-www-form-urlencoded and multipart/form-data
 * which are sent with POST requests or as part of the URL for GET requests.
 *
 * @see        com.percussion.server.PSRequestParser
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSFormContentParser extends PSContentParser
{

   /**
    * Parse the specified input stream and add it to the appropriate
    * place in the request structure.
    *
    * @param   request        the request object to store the contents in
    *
    * @param   contentType    the Content-Type HTTP header value
    *
    * @param   charset        the character encoding of the content
    *
    * @param   content        the input stream containing the data
    *
    * @param   length         the amount of data to read
    *
    * @throws  IOException    if an i/o error occurs
    *
    * @throws  PSRequestParsingException
    *                         if the content is invalid or the
    *                         contentType is not supported
    */
   public void parse(
      PSRequest request, String contentType, String charset,
      PSInputStreamReader content, int length)
      throws IOException, PSRequestParsingException
   {
      if (isMultipartContentType(contentType)) {
         /* multi-part form data is in a nasty format!!! */
         parseMultipartForm(contentType, content, request, charset);
         return;
      }

      if (!isSupportedContentType(contentType)) {
         //FB: DMI_INVOKING_TOSTRING_ON_ARRAY 1-17-16
         Object[] args = { contentType, Arrays.toString(ARRAY_SUPPORTED_TYPES) };
         throw new PSRequestParsingException(
                     IPSServerErrors.PARSER_UNSUPPORTED_CONTENT_TYPE, args);
      }

      /* this must be simple old URL encoded */

      if (length == 0)   // nothing to do
         return;

      byte[] buf = new byte[length];
      int newLength = getContentFromReader(content, buf, length);
      
      // The container may have parsed this into params for us already in which
      // case the content stream is empty
      if (newLength == 0)
         return;
      
      // if we got less than the reported length, warn and then trim the buffer
      if (newLength < length) 
      {
         Object[] args = { request.getUserSessionId(), contentType,
            String.valueOf(length), String.valueOf(newLength) };
         PSLogManager.write(
            new PSLogServerWarning(
            IPSServerErrors.CONTENT_LENGTH_DOES_NOT_MATCH_DATA_READ,
            args, true, "PSXmlContentParser"));
         length = newLength;
         byte[] newBuf = new byte[length];
         System.arraycopy(buf, 0, newBuf, 0, length);
         buf = newBuf;
      }

      /* if we already started putting params in for this request,
       * append any additional params
       */
      parseParameterString(
         request.getParameters(), createUrlDecodedString(buf, charset));
   }

   /**
    * Add the request parameters defined in the specified parameter string
    * to the specified hash map.
    *
    * @param  params the hash map to add the parameters to, may not be
    * <code>null</code>, can be empty.
    *
    * @param paramString the request parameter string to parse, may not be
    * <code>null</code>, can be empty.
    * 
    * @throws PSRequestParsingException if an error occurs parsing the
    * parameters.
    */
   public static void parseParameterString( HashMap<String, Object> params, String paramString)
      throws PSRequestParsingException
   {
      if (params == null)
         throw new IllegalArgumentException("Parameter map must be specified.");

      if(paramString == null)
         throw new IllegalArgumentException("paramString can not be null");

      try
      {
         paramString = createUrlDecodedString(
            paramString.getBytes(PSCharSetsConstants.rxJavaEnc()), 
            PSCharSetsConstants.rxJavaEnc());
      }
      catch (UnsupportedEncodingException e)
      {
         // This should never happen as we are supplying the proper
         //character encoding. 
         e.printStackTrace();
      } catch (IOException e)
      {
         // This should never happen as we are supplying the non-null
         //String. There is no real IO.
         e.printStackTrace();
      }

      String curTok;
      String curValue = "";
      String curName  = null;
      String lastTok  = STR_URLENCODING_PARAM_TOKEN;


      PSParamContext paramContext =
         ms_innerBuilder.new PSParamContext(params);

      /* Replace all ampersand entity refs w/ just the ampersand for XHTML
         compliance (ampersands can't exist directly in an xhmtl file because
         they aren't allowed in xml) */

      paramString = convertEntities( paramString );

      StringTokenizer tok = new StringTokenizer(
         paramString,
         STR_URLENCODING_VALUE_TOKEN + STR_URLENCODING_PARAM_TOKEN,
         true);

      while (tok.hasMoreTokens()) {
         curTok = tok.nextToken();
         if (curTok.equals(STR_URLENCODING_PARAM_TOKEN)) {
            if (curName != null) {
               paramContext.addParam(curName, curValue);
               curName  = null;
               curValue = "";
            }

            lastTok = curTok;
         }
         else if (curTok.equals(STR_URLENCODING_VALUE_TOKEN)) {
            lastTok = curTok;
         }
         else {
            if (lastTok.equals(STR_URLENCODING_PARAM_TOKEN))   // must be on a name now
               curName = urlDecode(curTok);
            else
               curValue = urlDecode(curTok);
         }
      }

      if (curName != null) {
         paramContext.addParam(curName, curValue);
      }
   }


   /**
    * Replaces all occurrences of entity refs allowed in URL query strings
    * with their corresponding character. Currently, this is just &amp;
    * <p>In XHTML, the ampersand used to separate params in the query string
    * must be escaped because it is an xml document.
    *
    * @param query The query string from a URL. If <code>null</code> or empty,
    * it is returned unmodified.
    *
    * @return The supplied query with all entity refs replaced with their
    * corresponding character.
    */
   private static String convertEntities( String query )
   {
      if ( null == query )
         return null;
      StringBuffer buf = new StringBuffer( query );
      boolean done = false;
      int len = AMPERSAND_ENTITY.length();
      int nextPos = 0;  // position in search string of next entity
      /* This is a little complex because we are searching one string and
       * modifying a copy of that string that is changing as we go. What we
       * do is keep a count of how many chars have been removed from the
       * dynamic string and use that count to correctly position the index
       * calculated from the static string.
       */
      for ( int lostChars = 0; !done; lostChars += len-1 )
      {
         nextPos = query.indexOf( AMPERSAND_ENTITY, nextPos );
         if ( nextPos < 0 )
            done = true;
         else
         {
            int pos = nextPos - lostChars;
            buf.replace( pos, pos+len, STR_URLENCODING_PARAM_TOKEN );
            nextPos += len;
         }
      }
      return buf.toString();
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

   /**
    * Is the content type for a multi-part form?
    *
    * @param      contentType      the content type to check
    *
    * @return                     <code>true</code> if it is
    */
   private boolean isMultipartContentType(String contentType)
   {
      /* multi-part content contains additional info */
      if (contentType.toLowerCase().startsWith(
            IPSMimeContentTypes.MIME_TYPE_MULTIPART_FORM.toLowerCase()))
         return true;

      return false;
   }

   /**
    * Find a boundary pattern in the "content-type" header.
    *
    * @param contentType Either the entire header or the value for the http
    *    "content-type" header.
    *
    * @return The value of the 'boundary' parameter in the supplied content
    *    type, if there is one, modified by prefixing '--' so it is ready for
    *    use.
    *
    * @throws PSRequestParsingException If the boundary parameter isn't found
    *    or the content-type value is malformed.
    */
   private static String findBoundary(String contentType)
      throws PSRequestParsingException
   {
      String boundary = "";
      try
      {
         String contentTypeValue;
         if ( contentType.toLowerCase().startsWith("content-type:"))
            contentTypeValue = contentType.substring(contentType.indexOf(':')+1);
         else
            contentTypeValue = contentType;
         Map contentParams = new HashMap();
         String type = PSBaseHttpUtils.parseContentType(contentTypeValue, contentParams);
         boundary = (String) contentParams.get( "boundary" );
         if ( null == boundary )
            // always throws
            contentTypeError( contentType );
         else
            boundary = "--" + boundary;
      }
      catch ( IllegalArgumentException e )
      {
         // always throws
         contentTypeError( contentType );
      }
      /* this is outside the try block because the compiler isn't smart enough
         to realize that contentTypeError always throws */
      return boundary;
   }

   /**
    * Throw "content-type" line exception, e.g. "boundary" error
    *
    * @param   contentType   the line string starts with "content-type"
    */
   private static void contentTypeError(String contentType)
      throws PSRequestParsingException
   {
      Object[] args = { contentType };
      throw new PSRequestParsingException(
         IPSServerErrors.INVALID_MULTIPART_CONTENT_TYPE, args);
   }

   /**
    * Throw "content-disposition" line exception
    *
    * @param   contentDisp   the line string starts with "content disposition"
    */
   private static void contentDispError(String contentDisp)
      throws PSRequestParsingException
   {
      Object[] args = { contentDisp };
      throw new PSRequestParsingException(
         IPSServerErrors.INVALID_MULTIPART_CONTENT_DISP, args);
   }

   /**
    * Set the parameter HashMaps in the specified request with
    * the request parameters defined in the specified parameter string.
    *
    * @param   contentType          The content-type header of the request.
    * Assumed not <code>null</code>.
    *
    * @param   reader               The input stream for the request to be
    * built.  Assumed not <code>null</code>.
    *
    * @param   request              The request. Assumed not <code>null</code>.
    *
    * @param   originalPageCharSet  The character set of the original request.
    * May be <code>null</code> or empty.
    *
    * @throws  IOException    If an I/O exception occurs processing the request.
    *
    * @throws  PSRequestParsingException If an error occurs parsing the request.
    */
   private static void parseMultipartForm(
      String contentType, PSInputStreamReader reader, PSRequest request,
      String originalPageCharSet)
      throws IOException, PSRequestParsingException
   {
      PSParamContext paramContext
         = ms_innerBuilder.new PSParamContext( request.getParameters() );
      String line, boundary, name;

      boundary = findBoundary(contentType);

      /* advance to the first boundary */
      while ((line = reader.readLine()) != null) {
         if (line.equals(boundary))
            break;
      }

      /* then parse all the blocks */
      if (line != null){
         name = null;
         boolean more = true;
         List<String> xmlDocFlags = null;
         do {
            /* Get the 'psxmldoc' parameter value which defines how to treat the
             * uploaded xml document. If the request has multiple
             * xml documents to be uploaded, then this parameter might have
             * values for each xml document delimited by ';'. So store all
             * values as list and use the flags in the order xml documents found.
             * All the xml documents found before we got this value, uses the
             * server default value for this flag. If the number of flags in the
             * list does not match the number of documents all the remaining
             * docs will get the server default.
             */
            if(xmlDocFlags == null || xmlDocFlags.size() == 0)
            {
               String xmlDocValue = request.getParameter(
                     PSRequest.REQ_XML_DOC_FLAG );
               if(xmlDocValue != null && xmlDocValue.trim().length() != 0)
               {
                  xmlDocFlags = new ArrayList<>();
                  StringTokenizer st = new StringTokenizer(xmlDocValue, ";");
                  while(st.hasMoreTokens())
                     xmlDocFlags.add(st.nextToken());

                  Collections.reverse(xmlDocFlags);
               }
            }
            more = parseNextBlock(reader, boundary, name, paramContext, request,
               originalPageCharSet, xmlDocFlags);
         }while (more);
      }
   }

   /**
    * Parse the next data block of a posted multipart form submission.
    * <br>
    * If the next data block has an xml file, then it uses the last value in
    * <code>xmlDocFlags</code> list as the flag to define how to treat the
    * uploaded xml document and removes that value from the list, so that the
    * xml document uploaded gets their flags in the order when the next blocks
    * are parsed. If the <code>xmlDocFlags</code> is <code>null</code> or empty
    * it uses the server default value for 'useNonValidating' for not to
    * validate the xml document if it finds the document in the next data block.
    *
    * @todo Cleanup: replace findBoundary w/ PSBaseHttpUtils.parseContentType
    *
    * @param   reader   The input stream for the request to be built.
    * Assumed not <code>null</code>.
    *
    * @param   boundary The boundary string. Assumed not <code>null</code>.
    *
    * @param   name     The paramater name. May be <code>null</code>
    *
    * @param   paramContext   The parameter context. Assumed not
    * <code>null</code>.
    *
    * @param   request  The request. Assumed not <code>null</code>.
    *
    * @param   originalPageCharSet  The character set of the original request.
    * May be <code>null</code> or empty.
    *
    * @param xmlDocFlags the list of flags to define how to treat the xml
    * document uploaded in the reverse order of xml documents uploaded, may be
    * <code>null</code> or empty. If it is not <code>null</code> or empty and
    * finds an xml document in this block, it uses the last one in the list as
    * the flag to define how to treat the uploaded xml document.
    *
    * @return <code>true</code> if the end boundary is not reached, otherwise
    * <code>false</code>
    *
    * @throws  IOException    If an I/O exception occurs processing the request.
    *
    * @throws  PSRequestParsingException If an error occurs parsing the request.
    */
   private static boolean parseNextBlock(
      PSInputStreamReader reader, String boundary, String name,
      PSParamContext paramContext, PSRequest request,
      String originalPageCharSet, List<String> xmlDocFlags)
      throws IOException, PSRequestParsingException
   {
      String line;
      String endBoundary = boundary + "--";   /* means no more blocks */

      /* format is:
       *
       * content-disposition: form-data; name="name"
       *
       * value
       * <boundary>
       *
       * **** OR ****
       *
       * content-disposition: form-data; name="name"; filename="file"
       * Content-Type: <MIME type of file's data>
       *
       * ... file contents ...
       * <boundary>
       *
       * **** OR ****
       *
       * content-disposition: form-data; name="name"
       * Content-Type: multipart/mixed, boundary="innerBoundary"
       *
       * <innerBoundary>
       *
       * **** OR ****
       * content-disposition: attachment; filename="file"
       * Content-Type: <MIME type of file's data>
       *
       * ... file contents ...
       * <innerBoundary>
       *
       */

      /* Read all headers into vector, which leaves the reader on
       * the first line of real data which should be a blank line.
       * Check specification in RFC-1521
       */
      ArrayList<String> headers = new ArrayList<>();
      String firstDataLine;

      /* TODO: Need to i18n-alize the name and filename
       * MS basically doesn't US-ASCII encode content-disposition
       * data!  For now, we are going to extend these readLine methods
       * to accept UTF8 (a superset of ASCII) instead of US-ASCII, this is
       * what MS-IE appears to be sending.
       */
      for (line = reader.readLine("UTF8");
         (line != null) && (line.length() > 0);
         line = reader.readLine("UTF8"))
      {
         headers.add(line);
      }

      if (headers.size() == 0)    // no header is wrong
         contentDispError("Without any header");

      firstDataLine = line;       // save this line in case we need it

      line = headers.get(0);  // first header is content-disposition

      // Ignore non-content entries
      if(!line.trim().toLowerCase().startsWith("content-disposition"))
      {
          Iterator<String> it = headers.iterator();
          while(it.hasNext())
          {
             String header = it.next().trim();
             if(header.equals(boundary) || header.equals(endBoundary))
                return !endBoundary.equals(header);  
          }
          contentDispError(line);
      }
      
      /* Processing the content-disposition header */
      StringTokenizer tok = new StringTokenizer(line, ":;\"", true);
      String curTok;

      String filename = null;
      Object value = null;
      String encType = null;
      String innerBoundary = null;
      String endInnerBoundary = null;
      String charsetName = null;

      int i;
      int ctf = 0;
      boolean isAttach = false;

      try {
         curTok = tok.nextToken().trim(); /* skip the Content-Disposition text */
         curTok = tok.nextToken();   /* skip the : delimiter */
         curTok = tok.nextToken().trim();
         if (!"form-data".equalsIgnoreCase(curTok)){
            if ("attachment".equalsIgnoreCase(curTok))
               isAttach = true;
            else
               contentDispError(line);
         }
         curTok = tok.nextToken();   /* skip the ; delimiter */
         curTok = tok.nextToken().trim();
         if (isAttach == false){
            if ("name=".equalsIgnoreCase(curTok)) {
               curTok = tok.nextToken();
               if ("\"".equals(curTok)) {
                  name = "";
                  while (!"\"".equals(curTok = tok.nextToken()))
                     name += curTok;
               }
               else
                  name = curTok;
            }
            else
               contentDispError(line);
         }  // end block of "if (isAttach == false){"

         /* is there a filename section? */
         while (tok.hasMoreTokens()) {
            curTok = tok.nextToken().trim();
            if ("filename=".equalsIgnoreCase(curTok)) {
               curTok = tok.nextToken();
               if ("\"".equals(curTok)) {
                  filename = "";
                  while (!"\"".equals(curTok = tok.nextToken()))
                     filename += curTok;
               }
               else
                  filename = curTok;
            }
         }
      } catch (NoSuchElementException e){
         /* we received tokens in an invalid order!!! */
         contentDispError(line);
      }

      /*Now these two "content-disposition" cases have been checked:*/
      /* content-disposition: form-data; name="name"; filename="file"  */
      /* content-disposition: attachment; filename="file" */

      boolean isText = true;
      boolean isXml = false;
      boolean validateXML = false; //by default we don't validate xml

      /* see if there is a header defining a content-type */
      if (headers.size() > 1) {
         for (i=1; i<headers.size(); i++){
            line = headers.get(i);
            line = line.toLowerCase();
            if (line.startsWith("content-type:"))
               break;
         }
      }

      String mimeType = "";   // don't want it null (may cause an exception!)
      String contentType = "";   // the content-type header value
      line = line.toLowerCase();
      if (line.startsWith("content-type:"))
      {
         HashMap contentParams = new HashMap();
         try
         {
            contentType = line.substring(line.indexOf(':')+1);
            mimeType = PSBaseHttpUtils.parseContentType(contentType, contentParams);
         } catch (IllegalArgumentException e)
         {
            // Ignore, as this doesn't affect the type itself,
            // since we are getting it from the line directly.
         }

         // treat text/xml and application/xml as isText, isXml
         if (mimeType.startsWith("text/xml") ||
            mimeType.startsWith("application/xml"))
         {
            isXml = true;

            /* Now we have to find out whether 'psxmldoc' is specified for this
             * xml document which defines how to treat an xml document.
             * Get the last one and remove that from this list, so that when
             * the parser finds another document the last one will be its value.
             */            
            if(xmlDocFlags != null)
            {
               if(!xmlDocFlags.isEmpty())
               {
                  String xmlDocValue = xmlDocFlags.remove(
                     xmlDocFlags.size() - 1);
                  if( xmlDocValue.equalsIgnoreCase(
                        PSRequest.XML_DOC_AS_TEXT) )
                  {
                     isXml = false;
                  }
                  else if( xmlDocValue.equalsIgnoreCase(
                        PSRequest.XML_DOC_VALIDATE) )
                  {
                     validateXML = true;
                  }
               }
            }
         }
         else if (!mimeType.startsWith("text/"))
            isText = false;

         // If it's text and not xml see if it has a charset specification
         if (isText && !isXml)
         {
            charsetName = (String) contentParams.get("charset");
            if (charsetName != null && charsetName.length() < 1)
            {
               charsetName = null; // an empty string is as good as null
            }
         }
      }

      /*what type of data in the content-type header?*/
      if (isText == false){
         if (mimeType.startsWith("multipart/mixed")){
            innerBoundary = findBoundary(line);
            if (innerBoundary.equals(boundary))  // will cause trouble
               contentTypeError("Boundary ambiguity occurred.");

            endInnerBoundary = innerBoundary + "--";
            encType = null;
         } /* end block of "multipart/mixed" content-type case */

         else{   // other content-types such as octet/stream, image, audio, etc.
            /* see if there is content-transfer-encoding */
            if (headers.size() > 1) {
               for (i=1; i<headers.size(); i++){
                  line = headers.get(i);
                  line = line.toLowerCase();
                  if (line.startsWith("content-transfer-encoding"))
                     break;
               }
            }

            if (line.toLowerCase().startsWith("content-transfer-encoding:"))
               encType = line.substring(line.indexOf(":")+1).trim();
            else
               encType = null;
         }   // end block of "else{", all other nontext content-types
      }     // enc block of "if (isText == false)"
      else{    // isText is ture
         if (isXml){
            StringTokenizer xmlTok3 = new StringTokenizer(line, ":;\"", false);
            curTok = xmlTok3.nextToken();  // skip content-type
            curTok = xmlTok3.nextToken();  // skip text/xml
            while (xmlTok3.hasMoreTokens()){
               curTok = xmlTok3.nextToken().trim();
               if ("charset=".equalsIgnoreCase(curTok)){
                  curTok = xmlTok3.nextToken().trim();
                  charsetName = curTok;
                }
            }   // content-type has been checked at this step

            /* see if there is content-transfer-encoding */
            if (headers.size() > 1) {
               for (i=1; i<headers.size(); i++){
                  line = headers.get(i);
                  line = line.toLowerCase();
                  if (line.startsWith("content-transfer-encoding"))
                     break;
               }
            }

            if (line.toLowerCase().startsWith("content-transfer-encoding")){
               ctf = 1;
               StringTokenizer tTok = new StringTokenizer(line, ":", false);
               String tCurTok;
               tCurTok = tTok.nextToken();
               tCurTok = tTok.nextToken().trim();
               encType = tCurTok;
            }
         }    // end block of "if (isXml == true){"
      }      // end block of isText is true

      /*Check if innerBoundary mingled with outside boundary*/
      /*
         if (!boundaryOrderMatch(reader, boundary, innerBoundary))
            contentDispError("Boundary has wrong order");
      */

      /* If it's a text file, it's still a file! */
      if (!isXml && filename != null)
         isText = false;

      /* now comes the data
       *
       * We can easily do text data. We can also decode any
       * binary data which are transferred as text. At this time,
       * we support:
       *   - base64
       *
       * todo: we must add support for:
       *   - quoted-printable
       *   - any other formats we can find
       */
      // boolean isBase64
      //    = (encTpye != null) && "base64".equalsIgnoreCase(encType);
      // if (isText || isBase64)
      if (isText)
      {
         /* if this is XML data (file or otherwise), we'll assume it may be
          * very large. As such, we'll write it directly to disk. If it's
          * anything else, we'll write to a string buffer
          */
         Writer out = null;
         Object sourceValue = null;
         if (isXml) {
            File f = new PSPurgableTempFile(
               "psx", ".xml", null, filename, contentType, encType);
            sourceValue = f;
            out = new BufferedWriter(new OutputStreamWriter(
               new FileOutputStream(f)));
         }
         else {
            out = new StringWriter();
         }

         // we can do text based data simply as it's on line boundaries, etc.
         boolean firstLine = true;

         /* for regular text add a character set name from the request
          * leave Xml alone it appears xml does its own thing above w/charset
          */
         if (!isXml && ((charsetName == null) || (charsetName.length() == 0)))
            charsetName = originalPageCharSet;

         while ( (line = reader.readLine(charsetName)) != null) {
            if ((innerBoundary != null) &&
               (line.equals(innerBoundary) || line.equals(endInnerBoundary)))
               break;          // finished one data block
            else if (line.equals(boundary) || line.equals(endBoundary))
               break;          // finished one data block

            /* If XML has no explicit content-transfer-encoding header,
             * then need to check the first line of XML data (RFC-2376)
             */
            if (isXml && (ctf == 0)){
               encType = encTypeOfXml(charsetName, line);
               ctf = 2;       // remove the flag (ctf==0)
            }

            if (firstLine)   // don't write CR/LF before first line
               firstLine = false;
            else {
               out.write('\r');
               out.write('\n');
            }
            out.write(line);
         }   // end of while loop

         if ((filename != null) || isXml)
            value = sourceValue;
         else   // this is a bytearray otherwise
            value = ((StringWriter)out).toString();
         out.flush();
         out.close();
      }
      else {   // treat this as raw binary data
         /* if this is a file, we'll assume it may be very large.
          * As such, we'll write it directly to disk. If it's anything
          * else, we'll write to a memory (byte[])
          */
         OutputStream out = null;
         Object sourceValue = null;
         if (filename != null) {
            File f = new PSPurgableTempFile(
               "psx", ".bin", null, filename, contentType, encType);
            sourceValue = f;

            out = new BufferedOutputStream(
               new FileOutputStream(f));
         }
         else {
            out = new ByteArrayOutputStream();
         }

         // are we looking for the end of the inner or outer boundary?
         // when using boundaries, RFC1521 states we should consider
         // CRLF boundary CRLF all as part of the boundary!
         byte[] readBuf = (innerBoundary == null) ?
            boundary.getBytes() : innerBoundary.getBytes();

         byte[] aBoundary = new byte[readBuf.length + 2];
         aBoundary[0] = 13;
         aBoundary[1] = 10;
         System.arraycopy(readBuf, 0, aBoundary, 2, readBuf.length);

         // now create our read-ahead buffer
         readBuf = new byte[aBoundary.length];

         int matchFrom = 0;

         // read chars from the stream until we find our boundary
         line = null;
         int c;
         int bufsize = readBuf.length;
         for (   int curRead = reader.read(readBuf);
               curRead >= 0;
               curRead = reader.read( readBuf ))
         {
            for ( i = 0; i < curRead; ++i )
            {
               c = readBuf[i];
               if (c == aBoundary[matchFrom]) {
                  matchFrom++;
                  if (matchFrom == aBoundary.length)
                  {
                    /* Found the end of the boundary. Put back what we
                     * read past the end
                     * of the boundary and then
                     * read to the end of the current line.
                     */
                     if ( i+1 < readBuf.length )
                     {
                        reader.unread( readBuf, i+1, curRead - (i+1));
                     }

                     // complete reading the line in case this is
                     // the end boundary
                     String endLine = reader.readLine();

                     // Build a string to compare with the end boundary
                     // without the crlf at the front
                     line = new String(aBoundary, 2, aBoundary.length - 2) + endLine;
                     break;
                  }
               }
               else
               {
                  if (matchFrom > 0)      // write any bytes we skipped
                  {
                     out.write(aBoundary, 0, matchFrom);
                     if ( c == aBoundary[0] )
                        matchFrom = 1;
                     else
                     {
                        matchFrom = 0;
                        out.write(c);
                     }
                  }
                  else
                     out.write(c);
               }
            }

            if (line != null)      // signifies we're done
               break;
         }

         // if this is file based,
         if (filename != null)
            value = sourceValue;
         else   // this is a bytearray otherwise
            value = ((ByteArrayOutputStream)out).toByteArray();

         out.flush();
         out.close();
      }

      if (value == null)
         value = "";

      if (line == null) {   /* no boundary is an error!!! */
         Object[] args = { "End boundary not found: " + value };
         throw new PSRequestParsingException(
                     IPSServerErrors.INVALID_MULTIPART_CONTENT_DISP, args);
      }

      /* unless this is an XML file, we're still storing it in the param
       * list. For XML fields, we'll store it as the input document
       */
      if (isXml){
         Document doc = PSXmlContentParser.getXMLDocument((File)value,
            null, validateXML);
         request.setInputDocument(doc);
         // remove the temp file, which is no longer needed
         if (value instanceof PSPurgableTempFile)
         {
            PSPurgableTempFile tmpFile = (PSPurgableTempFile) value;
            tmpFile.release();
         }
      }
      else {
         paramContext.addParam(name, value);
      }

      return !line.equals(endBoundary);
   }

   private static String urlDecode(String str)
      throws PSRequestParsingException
   {
      StringBuffer newStr = new StringBuffer(str.length());
      int iSrc = 0;
      int iDst = 0;
      char ch;

      /* initialize it to the full capacity
       * (otherwise it exceptions when calling newStr.setCharAt)
       */
      newStr.setLength(str.length());

      for (; iSrc < str.length();) {
         if ( (ch = str.charAt(iSrc++)) == URLENCODING_SPACE_TOKEN)
            ch = URLENCODING_SPACE_REAL;
         else if (ch == URLENCODING_HEX_TOKEN) {
            try {
               char ch1 = str.charAt(iSrc++);
               char ch2 = str.charAt(iSrc++);

               ch = (char)(Integer.parseInt("" + ch1 + ch2, 16));
            } catch (StringIndexOutOfBoundsException e) {
               /* this can only happen when it's poorly formed! */
               Object[] args = { str };
               throw new PSRequestParsingException(
                           IPSServerErrors.FORM_PARSER_BAD_HEX_CHAR, args);
            }
         }

         newStr.setCharAt(iDst++, ch);
      }

      /* now let it know where we actually terminated the value */
      newStr.setLength(iDst);

      return newStr.toString();
   }

   /* The format could be
    *
    * <?xml version="1.0" encoding="encoding" standalone="answer"?>
    *
    * **** OR ****
    *
    * {BOM}<?xml version="1.0" encoding="encoding" standalone="answer"?>
    */
   private static String encTypeOfXml(String charsetName, String line)
   {
      int bomFlag = 0;
      String encoding = "";
      String encType = null;

      StringTokenizer xmlTok = new StringTokenizer(line, " ", false);
      String curTok = null;
      if ( xmlTok.hasMoreTokens())
      {
         curTok = xmlTok.nextToken();
         if ("{BOM}<?xml".equalsIgnoreCase(curTok))
            bomFlag = 1;
      }
      while (xmlTok.hasMoreTokens()){
         curTok = xmlTok.nextToken();
         if ("encoding=".equalsIgnoreCase(curTok)){
            curTok = xmlTok.nextToken();
            if ("\"".equals(curTok)){
               while (!"\"".equals(curTok = xmlTok.nextToken()))
                  encoding += curTok;
            }
            else
               encoding = curTok;
         }
      }

      if (charsetName != null)
         encType = charsetName;
      else{
         if (bomFlag == 1)
            encType = "UTF-16";
         else{
            if (encoding == null)
               encType = "UTF-8";
            else
               encType = encoding;
         }
      }

      return encType;
   }

   /**
    * Utility class to add the parameters to request parameters map while
    * parsing the form parameters and content.
    */
   private class PSParamContext
   {
      /**
       * Constructs a new <code>PSParamContext</code> object and initializes its
       * members.
       *
       * @param params the request parameter map, may not be <code>null</code>,
       * can be empty.
       */
      PSParamContext(HashMap<String, Object> params)
      {
         if (params == null)
            throw new IllegalArgumentException(
               "A parameter map must be supplied");

         m_params = params;
      }

      /**
       * Add an entry to the parameter hash map. If the parameter exists, then
       * it appends the value to its values list.
       *
       * @param name the name of the parameter, may not be <code>null</code> or
       * empty.
       *
       * @param value the parameter value to add, may be <code>null</code>
       */
      void addParam(String name, Object value)
      {
         if(name == null || name.trim().length() == 0)
            throw new IllegalArgumentException("name can not be null or empty");

         Object o = m_params.put(name, value);
         if (o != null)
         {
            if (o instanceof ArrayList)
            {
               ((ArrayList<Object>) o).add(value);
               m_params.put(name, o);
            }
            else
            {
               ArrayList<Object> l = new ArrayList<>();
               l.add(o);
               l.add(value);
               m_params.put(name, l);
            }
         }
      }

      /**
       * The map of parameter values with parameter name as key and value as
       * value that were passed in with the request. Initialized in constructor
       * and never <code>null</code> after that. Gets filled in <code>
       * addParam(String, Object)</code> method.
       */
      HashMap<String, Object>  m_params = null;
   }

   /**
    * Create a string from the specified buffer, by:
    *
    * <ol>
    *    <li>url-decoding the buffer</li>
    *    <li>creating a string using the specified encoding</li>
    * </ol>
    * <I>Note:</I>The special URL encoding characters will be left alone
    * so that our parameter processing engine will handle them correctly.
    *
    * @param buf  The buffer to decode.
    *
    * @param charset The character set to create the <code>String</code>
    *    from the decoded buffer with.  If <code>null</code> or empty,
    *    the server's default character set will be used.
    *    You should use the IANA MIME-preferred name of the character
    *    set or the Java canonical name (preferred).  The encoding will
    *    first be checked against our internal map of character set
    *    names and aliases to see if there is an alias that matches this
    *    value, if a match is found, the javaName specified in that
    *    entry will be substituted before Java is called.  These maps
    *    are contained in the csmaps.xml file and loaded at server
    *    startup.
    *
    * @return The decoded string.
    *
    * @throws IOException  If an I/O error occurs.
    */
   public static String createUrlDecodedString(byte[] buf, String charset)
      throws IOException
   {
      /* This implementation should be fairly efficient in that
       * it minimizes the number of function calls. Hence, we
       * operate on an array. We will convert all %XX escape sequences
       * in place, and keep track of where we're writing to and where
       * we're reading from. This will allow us to properly deal with
       * UTF-8 URLs which use the syntax %XX%XX to the correct
       * UTF-8 char.
       */
      final int len = buf.length;
      byte b;

      int readFrom = 0;
      int writeTo = 0;
      while (readFrom < len)
      {
         b = buf[readFrom];
         if (b == 37) {  // 37 is the decimal representation of the % char
            /* We want to make sure we don't go past the end of the buffer
             * even when this is really a bad data represntation, so
             * we'll ignore it for that case
             */
            if ((readFrom + 2) < len)
            {
               // we have to take the next two hex bytes (high/low) to
               // create the real byte representation
               final byte hexh = buf[++readFrom];
               final byte hexl = buf[++readFrom];
               final byte decodedByte = getByteFromEscapedHex(hexh, hexl);

               if ( (decodedByte == (byte)URLENCODING_VALUE_TOKEN) ||
                    (decodedByte == (byte)URLENCODING_PARAM_TOKEN) ||
                    (decodedByte == (byte)URLENCODING_HEX_TOKEN) ||
                    (decodedByte == (byte)URLENCODING_SPACE_TOKEN))
               {   // we can't convert these as they'll break our parser
                  // instead, we write them back URL encoded
                  buf[writeTo++] = 37;      // put the % back in
                  buf[writeTo++] = hexh;
                  buf[writeTo]   = hexl;
               }
               else
               {
                  buf[writeTo] = decodedByte;
               }
            }
            else if (readFrom != writeTo)
            {  // since we've decoded an escape char, we need to move in any
               // subsequent bytes
               buf[writeTo] = buf[readFrom];
            }

            // we'll bump writeto/readfrom below, plus the
            // two extra bumps of readFrom above, if needed)
         } else if (readFrom != writeTo)
         {  // since we've decoded an escape char, we need to move in any
            // subsequent bytes
            buf[writeTo] = buf[readFrom];
         }

         // just move to the next byte for each case
         readFrom++;
         writeTo++;
      }

      if (charset == null || charset.length() == 0)
      {
         charset = PSServer.getDefaultServerHttpCharset();
      }

      return new String(buf, 0, writeTo,
         PSCharSets.getJavaName(charset));
   }

   public static byte getByteFromEscapedHex(byte hexh, byte hexl)
   {
      // this will take the high and low hex bytes of an escaped URL
      // char and create the single byte representation
      byte b = 0;

      if ((hexh - '0') < 10)
         b = (byte)(hexh - '0');
      else if ((hexh >= 'a') && (hexh <= 'f'))
         b = (byte)(10 + (hexh - 'a'));
      else
         b = (byte)(10 + (hexh - 'A'));
      b <<= 4; // shift over to the high nibble

      if ((hexl - '0') < 10)
         b += (byte)(hexl - '0');
      else if ((hexl >= 'a') && (hexl <= 'f'))
         b += (byte)(10 + (hexl - 'a'));
      else
         b += (byte)(10 + (hexl - 'A'));

      return b;
   }

   private static final String[] ARRAY_SUPPORTED_TYPES =
   {
      IPSMimeContentTypes.MIME_TYPE_URLENCODED_FORM,
      IPSMimeContentTypes.MIME_TYPE_MULTIPART_FORM
   };

   private static final char     URLENCODING_SPACE_TOKEN = '+';
   private static final char     URLENCODING_SPACE_REAL  = ' ';
   private static final char     URLENCODING_HEX_TOKEN   = '%';
   private static final char     URLENCODING_PARAM_TOKEN = '&';
   private static final char     URLENCODING_VALUE_TOKEN = '=';

   private static final String   STR_URLENCODING_PARAM_TOKEN   = "&";
   private static final String   STR_URLENCODING_VALUE_TOKEN   = "=";
   private static final String   HEX_STRING_HEADER             = "0x";

   /** This is the entity reference for an ampersand character, used by xml. */
   private static final String   AMPERSAND_ENTITY = "&amp;";

   private static final PSFormContentParser  ms_innerBuilder = new PSFormContentParser();
}

