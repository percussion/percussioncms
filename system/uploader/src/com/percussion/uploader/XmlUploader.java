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
package com.percussion.uploader;

import com.percussion.HTTPClient.Cookie;
import com.percussion.HTTPClient.CookieModule;
import com.percussion.HTTPClient.HTTPConnection;
import com.percussion.HTTPClient.HTTPResponse;
import com.percussion.HTTPClient.ModuleException;
import com.percussion.HTTPClient.NVPair;
import com.percussion.HTTPClient.ProtocolNotSuppException;
import com.percussion.legacy.security.deprecated.PSCryptographer;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.tools.PSHttpRequest;
import com.percussion.util.IOTools;
import com.percussion.utils.io.PathUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * When run as a program, this class takes a set of XML files and sends them to
 * as a POST to the Rhythmyx server. The user can specify the URL to use, the
 * action type param and the action type (the last 2 via a properties file).
 * When complete, the program prints out statistics regarding the updates.
 *
 * <p>Note: currently, all updates are assumed to be inserts.
 */
public class XmlUploader
{
   /**
    * Creates a new instance of this class and initializes it.
    * The following things are done during initialization:
    * <ul>
    *   <li>Install Interrupt handler</li>
    *   <li>Initialize Logger</li>
    *   <li>Load properties</li>
    * </ul>
    * If the help option is specified or required params are missing, a usage
    * message is displayed and System.exit is called.
    *
    * @param args The command line parameters, as specified in the description
    * of <code>main</code>.
    *
    * @throws IllegalArgumentException if any required parameters are missing
    * in the supplied map or psxmldoc paramter is invalid
    */
   public XmlUploader(Map args) throws IllegalArgumentException
   {
      Utils.setInterruptHandler();

      // get the request url
      m_requestUrl = (String) args.get( Utils.REQUESTURL_OPTION );
      if ( null == m_requestUrl )
         throw new IllegalArgumentException( "Request url must be supplied." );

      // get the properties file and load it if supplied
      String propsFilename = (String) args.get(Utils.PROPERTYFILE_OPTION);
      if ((propsFilename == null) || (propsFilename.trim().length() == 0))
         propsFilename = Utils.DEFAULT_PROPERTY_FILENAME;
      propsFilename = propsFilename.trim();
      File propsFile = new File(propsFilename);
      log.info( "[Init] Using property file {}", propsFile.getAbsolutePath());

      if (propsFile.isFile())
      {
        try(FileInputStream fis = new FileInputStream(propsFile ) ){
           try(BufferedInputStream bis = new BufferedInputStream( fis)) {
              m_properties.load(bis);
           }
         }
         catch (IOException ioe)
         {
            log.info("Failed to load properties file \"{}\". Using defaults.", propsFile.getAbsolutePath());
         }
      }
      else
      {
         log.info("Properties file \"{}\" does not exist. Using defaults.", propsFile.getAbsolutePath());
      }

      // set debugging state, presence of switch enables debugging output
      m_debug = args.containsKey(Utils.DEBUG_OPTION);
      if (!m_debug)
      {
         // check if debug option is specified in the logfile
         String debug = m_properties.getProperty(Utils.RX_DEBUG_KEY);
         if (!((debug == null) || (debug.trim().length() == 0)))
         {
            if (debug.trim().equalsIgnoreCase(String.valueOf(true)))
               m_debug = true;
         }
      }
      if (args.containsKey(Utils.TIMEOUT_OPTION))
      {
         Integer timeout = new Integer((String) args.get(Utils.TIMEOUT_OPTION));
         m_timeout = timeout.intValue() * 1000; // Convert to millis
      }
      
      // get the log file
      String logFilename = (String) args.get( Utils.LOGFILE_OPTION );
      if ((logFilename == null) || (logFilename.trim().length() == 0))
      {
         // check if a log file is specified in the properties file
         logFilename = m_properties.getProperty(Utils.RX_LOGFILE_KEY);
      }
      if (!((logFilename == null) || (logFilename.trim().length() == 0)))
      {
         File logFile = new File(logFilename);
         try
         {
            if (!logFile.isFile()) {
               logFile.createNewFile();
            }
            
            log.info( "[Init] Logging to {}", logFile.getAbsolutePath());
         }
         catch (IOException e)
         {
            log.info( "Failed to open log file \"{}\". Messages will not be logged.", logFile.getAbsolutePath());
            log.debug(e.getMessage(), e);
         }
      }

      // get the Rhythmyx server
      String server = m_properties.getProperty(Utils.RX_SERVER_KEY);
      if (!((server == null ) || (server.trim().length() == 0)))
         m_rxServer = server.trim();

      // get the Rhythmyx port
      String port = m_properties.getProperty(Utils.RX_PORT_KEY);
      if (!((port == null ) || (port.trim().length() == 0)))
      {
         port = port.trim();
         try
         {
            m_rxPort = Integer.parseInt(port);
         }
         catch (NumberFormatException e)
         {
            throw new IllegalArgumentException( "Invalid port number: " + port );
         }
      }

      // get the login id
      m_loginId = (String)args.get(Utils.LOGINID_OPTION);
      if ((m_loginId == null) || (m_loginId.trim().length() == 0))
      {
         // read the login id from the properties file
         m_loginId = m_properties.getProperty(Utils.RX_LOGINID_KEY);
      }

      // get the password if login id is not null
      if (!((m_loginId == null) || (m_loginId.trim().length() == 0)))
      {
         m_loginId = m_loginId.trim();
         m_loginPwd = (String)args.get(Utils.LOGINPWD_OPTION);
         if ((m_loginPwd == null) || (m_loginPwd.trim().length() == 0))
         {
            // read the password from the properties file
            // use empty password if no password is supplied on the command
            // prompt or in the properties file.
            m_loginPwd = m_properties.getProperty(Utils.RX_PWD_KEY, "");
            m_loginPwd = m_loginPwd.trim();

            // if password read from properties file, check the password
            // encrypted key
            if (m_loginPwd.length() > 0)
            {
               String encrypted = m_properties.getProperty(Utils.RX_PWD_ENCRYPTED_KEY);
               if (!((encrypted == null) || (encrypted.trim().length() == 0)))
               {
                  if (encrypted.trim().equalsIgnoreCase(String.valueOf(true)))
                  {
                     // need to decrypt the password

                        try {
                           PSEncryptor.getInstance("AES",
                                   PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
                           ).decrypt(m_loginPwd);
                        }catch(PSEncryptionException | java.lang.IllegalArgumentException e){
                           m_loginPwd = PSCryptographer.decrypt(
                                   PSLegacyEncrypter.getInstance(PathUtils.getRxDir(null).getAbsolutePath().concat(PSEncryptor.SECURE_DIR)).OLD_SECURITY_KEY(),
                                   m_loginId, m_loginPwd);
                        }

                     catch (Exception e)
                     {
                        throw new RuntimeException("Failed to decrypt password.");
                     }
                  }
               }
            }
         }
      }

      // get the community name/id
      m_communityId = (String)args.get(Utils.COMMUNITYID_OPTION);
      if (m_communityId == null)
      {
         // read the community name/id from the properties file
         m_communityId = m_properties.getProperty(Utils.RX_COMMUNITY_KEY);
      }
      if (m_communityId != null)
         m_communityId = m_communityId.trim();

      // get the psxmldoc parameter
      m_psxmldoc = m_properties.getProperty(Utils.REQ_XML_DOC_FLAG);
      if (m_psxmldoc != null)
      {
         m_psxmldoc = m_psxmldoc.trim();
         if (m_psxmldoc.trim().length() > 0)
         {
            if (!((m_psxmldoc.equalsIgnoreCase(Utils.XML_DOC_VALIDATE)) ||
               (m_psxmldoc.equalsIgnoreCase(Utils.XML_DOC_NONVALIDATE)) ||
               (m_psxmldoc.equalsIgnoreCase(Utils.XML_DOC_AS_TEXT))))
            throw new IllegalArgumentException( "psxmldoc parameter value is invalid.");
         }
      }

      // get the extra html parameters
      // remove the '&' if present at the start
      m_extraParams = m_properties.getProperty(Utils.RX_EXTRAPARAMS_KEY);
      if (m_extraParams != null)
      {
         m_extraParams = m_extraParams.trim();
         if (m_extraParams.startsWith("&"))
            m_extraParams = m_extraParams.substring(1);
      }
   }

   /**
    * The interface to the command line version of this class. The following
    * options are supported (see {@link Utils} for more details of each
    * option):
    * <ul>
    *   <li>HELP_OPTION</li>
    *   <li>PROPERTYFILE_OPTION - If not specified, rxuploader.properties is
    *       used.</li>
    *   <li>LOGFILE_OPTION</li>
    *   <li>REQUESTURL_OPTION - may optionally include the action type. If the
    *       action type is not specified, the property file is checked, if not
    *       found there, the default specified by RX_DEFAULT_HTML_ACTION_PARAM
    *       and RX_DEFAULT_ACTIONTYPE in Utils is used.</li>
    *   <li>DEBUG_OPTION Causes additional output to be generated and written
    *       to the console. Also, files are not deleted after a successful
    *       upload.</li>
    *   <li>FILE_OPTION Causes the uploader to read a list of content source
    *       URLs from a file. </li>
    * </ul>
    * <p>Processes all files supplied on the command line by sending them to
    * the Rhythmyx server as a post. Every file that is successfully uploaded
    * is removed from the file system. Statistics are kept regarding how many
    * files are processed and how many rows the server inserts. These stats
    * are printed at the end of the session.
    */
   public static void main(String[] params )
   {
      XmlUploader uploader = null;
      int status = Utils.SUCCESS;
      Stats stats = null;
      try
      {
         ArrayList filenames = new ArrayList();
         Map args = Utils.parseCmdParams( params, true, filenames );

         if ( null == args || args.containsKey( Utils.HELP_OPTION ) ||
            ( !args.containsKey( Utils.FILE_OPTION ) &&  filenames.isEmpty()))
         {
            usage();
            //Our launcher requires a pause
            System.out.println("Press ENTER to continue...");
            System.in.read();
            System.exit(0);
         }

         uploader = new XmlUploader( args );
         // Properties props = uploader.m_properties;

         // are they giving us a URL or a list of files?
         boolean isFile = true;
         ArrayList list = new ArrayList(10); // arbitrary initial size

         if ( args.containsKey( Utils.FILE_OPTION ))
         { // there's a list of URLs in a file.
              isFile = false;

              BufferedReader urlReader  = new BufferedReader(new FileReader(
              args.get(Utils.FILE_OPTION).toString()));

              String urlLine = urlReader.readLine();
              while (urlLine != null)
              {
                 log.info("Processing URL: {}", urlLine);
                 if(isHttpURL(urlLine))
                 {
                    list.add( uploader.new UrlContent( new URL(urlLine)));
                 }
                 else  //assume it's a file
                 {
                    list.add( uploader.new FileContent( urlLine ));
                 }

                 urlLine = urlReader.readLine();
              }
          }
         else
            {  // no -f flag, we have to look at the URL
            isFile = !isHttpURL((String) filenames.get(0));

            if ( isFile )
            {
               /* If wildcards are specified and no files are present, the wild-carded
                  param is passed thru. If we see this, just exit w/ no files msg. */
               String firstName = (String) filenames.get(0);
               if ( firstName.indexOf('*') >= 0 || firstName.indexOf('?') >= 0 )
               {
                  log.info( "No files to process." );
                  System.exit( Utils.SUCCESS );
               }

               Iterator iter = filenames.iterator();
               while ( iter.hasNext())
               {
                  String filename = (String) iter.next();
                  list.add( uploader.new FileContent( filename ));
               }
            }
            else
            {
               list.add( uploader.new UrlContent( new URL((String) filenames.get(0))));
            }
         }
         IContentSource [] sources = new IContentSource[list.size()];
         list.toArray( sources );

         Date startTime = new Date();
         stats = uploader.process( sources );
         stats.startTime = startTime;
         stats.finishTime = new Date();

      }
      catch ( FileNotFoundException e )
      {
         log.info("[Main] {}", e.getLocalizedMessage());
         log.debug(e.getMessage(), e);
         status = Utils.ERROR_CLASS_NOT_FOUND;
      }
      catch ( MalformedURLException e )
      {
         log.info( "[Main] Improperly formed URL: {}", e.getLocalizedMessage());
         log.debug(e.getMessage(), e);
         status = Utils.ERROR_IO;
      }
      catch ( IOException e )
      {
         log.info( "[Main] An IO error occurred: {}", e.getLocalizedMessage());
         log.debug(e.getMessage(), e);
         status = Utils.ERROR_IO;
      }
      catch ( IllegalArgumentException e )
      {
         log.info( "[Main] An Illegal argument exception was thrown: {}", e.getLocalizedMessage());
         log.debug(e.getMessage(), e);
         status = Utils.ERROR_UNKNOWN; // a design flaw exists, so it's not really important to the batch program
      }
      catch ( Exception e )
      {
         log.info( "[Main] Unexpected exception: {}", e.getMessage());
         log.debug(e.getMessage(), e);

         status = Utils.ERROR_UNKNOWN;
      }
      finally
      {
         if ( null != uploader )
         {
            if ( null != stats )
            {
               uploader.processStats( stats );
            }
            uploader.shutdown();
         }
         System.exit( status );
      }
   }

   /**
    * Prints a help message that describes the command line format used to run
    * this class as an application from the command line.
    */
   public static void usage()
   {
      log.info( "Usage: java com.percussion.uploader.XmlUploader" +
         " options sources(s)" );
      log.info( "Where the possible options are (optional ones in []):" );
      log.info( "    [-{} propertyFile]", Utils.PROPERTYFILE_OPTION);
      log.info( "    [-{} loginid]", Utils.LOGINID_OPTION);
      log.info( "    [-{} password]", Utils.LOGINPWD_OPTION);
      log.info( "    [-{} community id/name]", Utils.COMMUNITYID_OPTION);
      log.info( "    [-{} logFile]", Utils.LOGFILE_OPTION);
      log.info( "    [-{} debug]", Utils.DEBUG_OPTION);
      log.info( "    [-{} URLListFile]", Utils.FILE_OPTION);
      log.info( "    [-{} TimeInSeconds]", Utils.TIMEOUT_OPTION);
      log.info( "    [-{} requestUrl] (e.g. /Rhythmyx/application/resource.htm?...)", Utils.REQUESTURL_OPTION);
      log.info( "source(s) is either a list of files or a list of http URLs" );

   }

   /**
    * constructs the resource url from the url passed as input parameter
    * if the input url does not contain the html parameter "DBActionType"
    * then it adds this param with value equal to INSERT
    * adds psxmldoc html paramter if it is supplied by the user and if its value
    * is valid
    * @return the constructed resource url based on the input url provided by
    * the user, never <code>null</code> or empty
    */
   private String getResourceUrl()
   {
      StringBuilder urlBuf = new StringBuilder(m_requestUrl);

      // add the html parameter "DBActionType" if not present in the URL
      String actionType = m_properties.getProperty( Utils.RX_ACTIONTYPE_KEY,
         Utils.RX_DEFAULT_ACTIONTYPE );
      String htmlActionParam = m_properties.getProperty(
         Utils.RX_HTML_ACTION_PARAM_KEY, Utils.RX_DEFAULT_HTML_ACTION_PARAM );

      boolean bHasQueryParams = false;
      boolean bAddActionType = true;
      int paramStartIndex = m_requestUrl.indexOf('?');
      if (paramStartIndex > 0)
      {
         bHasQueryParams = true;
         String params = m_requestUrl.substring( paramStartIndex + 1 );
         Map htmlParams = new HashMap();
         Utils.parseHttpParamsString( params, htmlParams );
         if ( htmlParams.containsKey( htmlActionParam.toLowerCase()))
               bAddActionType = false;
      }

      StringBuilder appendBuf = new StringBuilder();
      if (bAddActionType)
         appendBuf.append(htmlActionParam + "=" + actionType);

      // add the psxmldoc parameter if specified
      if (!((m_psxmldoc == null) || (m_psxmldoc.trim().length() == 0)))
      {
         if (appendBuf.length() > 0)
            appendBuf.append("&");
         appendBuf.append(Utils.REQ_XML_DOC_FLAG + "=" + m_psxmldoc.trim());
      }

      // add the extra html parameters if specified
      if (!((m_extraParams == null) || (m_extraParams.length() == 0)))
      {
         if (appendBuf.length() > 0)
            appendBuf.append("&");
         appendBuf.append(m_extraParams);
      }

      if (appendBuf.length() > 0)
      {
         if (!bHasQueryParams)
            urlBuf.append("?");
         else
            urlBuf.append("&");

         urlBuf.append(appendBuf.toString()); // Keep this compatible with 1.3
      }
      return urlBuf.toString();
   }

   /**
    * validates the community name or id provided by the user
    * @throw IllegalArgument exception if the community name or id specified
    * is invalid for the user
    * @return the community id corresponding to the community name entered by
    * the user, if the user entered community id then returns the same value
    * returns -1 if it fails to verify community id for some reason, such as
    * sys_commSupport application not running.
    * @throw IllegalArgumentException if no community name/id matches the
    * user supplied community name/id
    */
   private int validateCommunity(HTTPConnection conn)
   {
      int communityId = 0;
      try
      {
         HTTPResponse resp = conn.Get(Utils.RX_COMMUNITY_URL);
         int status = resp.getStatusCode();
         if (status == Utils.HTTP_STATUS_OK)
         {
            try(InputStream is = resp.getInputStream()) {
               Document response = PSXmlDocumentBuilder.createXmlDocument(
                       is, false);

               // check the flag so we don't do the work unless we need to
               if (m_debug) {
                  StringWriter sw = new StringWriter();
                  PSXmlDocumentBuilder.write(response, sw);
                  log.info(sw.toString());
                  log.info("\n");
               }

               int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
                       PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
               int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
                       PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

               PSXmlTreeWalker walker = new PSXmlTreeWalker(response);
               Element elCommunity = walker.getNextElement(
                       Utils.EL_COMMUNITY, firstFlags);


               while (elCommunity != null) {
                  String communityName = walker.getElementData(elCommunity);
                  String commId = elCommunity.getAttribute(Utils.ATTR_COMMID);
                  if ((m_communityId.equalsIgnoreCase(communityName)) ||
                          (m_communityId.equalsIgnoreCase(commId))) {
                     communityId = Integer.parseInt(commId);
                     return communityId;
                  }
                  elCommunity = walker.getNextElement(Utils.EL_COMMUNITY,
                          nextFlags);
               }
            }
         }
         else
         {
            communityId = -1;
         }
      }
      catch (Exception e)
      {
         log.info("Exception : {}", e.getLocalizedMessage());
         log.debug(e.getMessage(), e);
         communityId = -1;
      }
      if (communityId == 0)
      {
         throw new IllegalArgumentException(
            "No community id or name matched the input community id/name");
      }
      else
      {
         log.info("Failed to verify community id.");
      }
      return -1;
   }

   /**
    * The main processing engine of the uploader. For each file in the supplied
    * list, a post is created using the request URL supplied on the command line.
    * The post is submitted to the Rhythmyx server. The response from the server
    * is analyzed and messages and statistics are updated. If exceptions/errors
    * occur during processing of a file, the error information is logged and
    * processing continues w/ the next file.
    *
    * @param sources All the files to process. If null or empty, an empty stats
    * object is returned.
    *
    * @return A statistics structure that contains how many errors, warnings,
    * inserts, skips, failures and documents processed.
    */
   public Stats process(IContentSource [] sources)
   {
      Stats stats = new Stats();
      if (null == sources || sources.length == 0)
         return stats;


      // construct the resource url
      String urlText = getResourceUrl();

      if (m_debug)
      {
         log.info("Using host: {}", m_rxServer);
         log.info("Using port: {}", m_rxPort);
         log.info("Using resource URL: {}", urlText);
      }

      // set up the connection
      HTTPConnection conn = null;
      try
      {
         conn = new HTTPConnection("http", m_rxServer, m_rxPort);
      }
      catch (ProtocolNotSuppException e)
      {
         // Signals that the protocol is not supported.
         // this will never happen since we are only using http protocol
         throw new IllegalArgumentException("Invalid protocol.");
      }

      // accept all cookies, don't display the dialog box for
      // accepting cookies
      CookieModule.setCookiePolicyHandler(null);

      // if the authenticatin fails, this will display a dialog box for
      // entering the login id and password
      conn.setAllowUserInteraction(true);
      conn.setTimeout(m_timeout);
      NVPair[] defaultHeaders = {new NVPair("Content-Type", "text/xml")};
      conn.setDefaultHeaders(defaultHeaders);

      if (m_loginId != null)
      {
         // using empty realm
         conn.addBasicAuthorization("", m_loginId, m_loginPwd);
      }

      // if community name or id is specified then validate it
      int communityId = -1;
      if (!((m_communityId == null) || (m_communityId.trim().length() == 0)))
         communityId = validateCommunity(conn);
      if (communityId != -1)
      {
         // add the sys_community cookie
         Cookie cookie = new Cookie(Utils.COMMUNITY_COOKIE_NAME,
            String.valueOf(communityId), conn.getHost(), "/", null, false);
         CookieModule.addCookie(cookie);
      }

      for (int i = 0; i < sources.length; ++i)
      {
            InputStream errResponse = null;
            stats.docsProcessed++;

            log.info("\nProcessing {}...\n", sources[i].getDisplayName());
            // variables used outside try block
            boolean isResponseOk = false;
            Exception exc = null;
            int rowsInserted = 0;   // used in finally block

            try(BufferedInputStream bis = new BufferedInputStream(
                  sources[i].getContent())){
               int bufLen = (int)sources[i].getContentLength();
               byte[] buf = new byte[bufLen];
               bis.read(buf, 0, bufLen);
               HTTPResponse resp = conn.Post(urlText, buf);
               int status = resp.getStatusCode();
               if (status == Utils.HTTP_STATUS_OK) {
                  isResponseOk = true;
                  try (InputStream is = resp.getInputStream()) {
                     Document response = PSXmlDocumentBuilder.createXmlDocument(
                             is, false);

                     // check the flag so we don't do the work unless we need to
                     if (m_debug) {
                        StringWriter sw = new StringWriter();
                        PSXmlDocumentBuilder.write(response, sw);
                        log.info(sw.toString());
                        log.debug("\n");
                     }

                     PSXmlTreeWalker walker = new PSXmlTreeWalker(response);
                     rowsInserted = Integer.parseInt(
                             walker.getElementData("RowsInserted"));
                     stats.rowsInserted += rowsInserted;
                     stats.rowsSkipped += Integer.parseInt(
                             walker.getElementData("RowsSkipped"));
                     stats.rowsFailed += Integer.parseInt(
                             walker.getElementData("RowsFailed"));
                     log.info("Deleting file.");
                  }
               }
               else
               {
                  // get the stream, it will be written below
                  errResponse = resp.getInputStream();
               }
            }
            catch (ModuleException | IOException | SAXException e)
            {
               exc = e;
            }

            finally {
               try {
                  sources[i].close(!m_debug && rowsInserted > 0);
               } catch (Exception e) {
                  log.info("An error occurred while trying to close the stream: {}", e.getLocalizedMessage());
                  log.debug(e.getMessage(), e);
               }


               String userMsg = null;
               String pattern = null;
               if (isResponseOk && exc != null) {
                  userMsg = "The request was successful, but an error occurred" +
                          " while trying to process the response. Can't verify that" +
                          " a row was inserted successfully. File not deleted.\r\n";
                  pattern = DEFAULT_WARNING;
                  stats.warnings++;
               } else if (null != errResponse) {
                  try {
                     stats.errors++;
                     // read stream into String
                     byte[] buf = new byte[1024]; // arbitrary size
                     int bytesRead = 1; // to get into loop
                     try(ByteArrayOutputStream byteStr = new ByteArrayOutputStream()){
                        while (bytesRead > 0) {
                           bytesRead = errResponse.read(buf);
                           if (bytesRead > 0)
                              byteStr.write(buf, 0, bytesRead);
                        }
                        userMsg = byteStr.toString(DEFAULT_RX_ENCODING);
                     }
                  } catch (UnsupportedEncodingException e) {
                     // this should never happen since we picked a well know encoding
                     userMsg = "Couldn't convert response from server, unknown encoding: " +
                             DEFAULT_RX_ENCODING;
                  } catch (IOException e) {
                     userMsg = "Failed to read response document from Rhythmyx server.\r\n" +
                             e.getLocalizedMessage();
                  }
                  IOUtils.closeQuietly(errResponse);
                  pattern = DEFAULT_ERROR;
               } else if (null != exc) {
                  stats.errors++;
                  pattern = DEFAULT_ERROR;
                  userMsg = "";
               } else {
                  pattern = DEFAULT_SUCCESS;
                  userMsg = "";
               }

               log.info("{} {}", pattern, new Object[]{sources[i].getDisplayName()});
               String excText = null != exc ? exc.getLocalizedMessage() : "";
               if (excText.length() > 0 || userMsg.length() > 0) {
                  log.info("{} {}", userMsg, excText);
               }
            }
      }
      return stats;
   }

   /**
    * Writes all statistics to the logger in a nicely formatted way and adds
    * a new row to the publishing history table. This should be called after
    * all processing has completed.
    *
    * @param stats The statistics after processing is complete. Must not be
    * null. Both dates in the stats object must be valid.
    *
    * @throws IllegalArgumentException If stats is null or either date object
    * is null.
    */
   private void processStats( Stats stats )
   {
      if ( null == stats || null == stats.startTime || null == stats.finishTime )
         throw new IllegalArgumentException( "Either the stats or one of its dates is null." );

      DateFormat formatter = DateFormat.getDateTimeInstance( DateFormat.LONG,
         DateFormat.LONG );
      log.info( "Started at {}", formatter.format( stats.startTime ));
      log.info( "Finished at {}", formatter.format( stats.finishTime ));
      if ( stats.errors > 0 ) {
         log.info("****************** Errors Encountered ******************");
      }
      log.info( "Error Count: {}", stats.errors );
      log.info( "Total Documents processed {}", stats.docsProcessed );
      log.info( "Total Rows Processed: {}", stats.getRowsProcessed());
      log.info( "Rows Inserted: {}", stats.rowsInserted );
      log.info( "Rows Skipped: {}", stats.rowsSkipped );
      log.info( "Rows Failed: {}", stats.rowsFailed );
   }


   /**
    * Should be called when the class is no longer needed. Performs any
    * cleanup necessary.
    */
   public void shutdown()
   {
      //NOOP
   }
   /**
    * test if a given URL represents an HTTP URL or a file
    *
    **/
   private static boolean isHttpURL(String urlString)
   {
     try
        {
           URL url = new URL(urlString);
           if ( url.getProtocol().equalsIgnoreCase( "http" ))
              return true;
        }
     catch ( MalformedURLException e )
        {  /* ignore, it must be files */ }

     return false;
   }
   /**
    * A simple interface to allow content to be supplied from varying sources.
    * Initially, it is to allow URL and file support.
    */
   interface IContentSource
   {
      /**
       * Returns a stream that will supply the contents of an XML document. After
       * the caller is finished with the stream, the <code>close</code> method
       * must be called. The caller should not close the stream.
       *
       * @return A valid stream that will provide a complete XML document. Will
       * not be null.
       */
      public InputStream getContent();

      /**
       * Must be called after the associated stream is finished with. Performs
       * necessary cleanup. The supplied flag may be used to determine what to
       * do with the source of the stream. After this method returns, the stream
       * associated with this object cannot be read.
       *
       * @param isSuccessful Should be <code>true</code> if the the stream was
       * successfully read and processed, <code>false</code> otherwise.
       *
       * @throws IOException If the associated stream has any problems while
       * closing. Even if an exception occurs, any action that would be taken
       * when the supplied flag is <code>true</code> will still be taken.
       */
      public void close( boolean isSuccessful )
         throws IOException;

      /**
       * @return The number of bytes that will be returned by the stream returned
       * by the <code>getContent</code> method. Always greater than or equal to
       * 0.
       */
      public long getContentLength();

      /**
       * @return A name that can be shown to the end user, or an empty string
       * if there is no appropriate name. Never returns null.
       */
      public String getDisplayName();
   }


   /**
    * Provides a content source for a file. It is assumed the user knows the type
    * of the content. The stream is created when this object is constructed, so
    * if the ctor is successful, the other methods are guaranteed to succeed.
    * <p>The returned stream is opened on the file, so the file will be locked
    * while this object is open.
    */
   class FileContent implements IContentSource
   {
      public FileContent( String filename )
              throws IOException
      {
         m_file = new File( filename );
         try(FileInputStream fi = new FileInputStream( m_file ) ) {
            m_content = new BufferedInputStream(fi);
            m_filename = m_file.getAbsolutePath();
         }
      }

      /**
       * See the interface method {@link XmlUploader.IContentSource#getContent() getContent}
       * for details.
       */
      public InputStream getContent()
      {
         return m_content;
      }

      /**
       * Closes the associated stream and deletes the associated file if the
       * supplied flag is <code>true</code>.
       *
       * @param isSuccessful Flag that indicates whether to delete the file
       * associated with this stream.
       *
       * @throws IOException If the associated stream has any problems while
       * closing.
       */
      public void close( boolean isSuccessful )
         throws IOException
      {
         try
         {
            m_content.close();
         }
         finally
         {
            if ( isSuccessful && null != m_file )
               m_file.delete();
            m_file = null;
         }
      }

      /**
       * See the interface method {@link XmlUploader.IContentSource#getContentLength()
       * getContentLength} for details.
       */
      public long getContentLength()
      {
         return null == m_file ? 0 : m_file.length();
      }

      /**
       * @return The full name of the file associated with this object.
       */
      public String getDisplayName()
      {
         return m_filename;
      }

      /**
       * A buffered stream opened on the file associated w/ this object. Valid
       * during lifetime of object.
       */
      private InputStream m_content;

      /**
       * We keep the file object around so we can delete it upon successful
       * processing. It is set to null after it is deleted.
       */
      private File m_file;

      /**
       * The filename of the associated file. We keep a separate object rather
       * than using m_file so it is still available after the stream has been
       * closed.
       */
      private String m_filename;
   }


   /**
    * Provides a content source for a URL. The contents of the URL are downloaded
    * into temporary storage before the input stream is returned. The stream is
    * created when this object is constructed, so if the ctor is successful, the
    * other methods are guaranteed to succeed.
    */
   class UrlContent implements IContentSource
   {
      /**
       * Reads the document found at the location specified by the supplied url,
       * which must use the HTTP protocol. The document is read into local
       * storage during construction.
       *
       * @param url A valid URL that specifies the content to retrieve. This
       * must be an HTTP url.
       *
       * @throws IllegalArgumentException if url is null or does not use the
       * HTTP protocol.
       *
       * @throws IOException If the content can't be retrieved from the url for
       * any reason.
       */
      public UrlContent( URL url )
         throws IOException
      {
         if ( null == url )
            throw new IllegalArgumentException( "Url cannot be null" );
         if ( !url.getProtocol().toLowerCase().equals( "http" ))
            throw new IllegalArgumentException( "Url must use HTTP protocol." );

         PSHttpRequest req = new PSHttpRequest( url );
         req.sendRequest();
         int responseCode = req.getResponseCode();
         if ( Utils.HTTP_STATUS_OK != responseCode )
            throw new IOException( "Request failed. Returned code " + responseCode );
         try(ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            IOTools.copyStream(req.getResponseContent(), buf);
            try (ByteArrayInputStream m_content = new ByteArrayInputStream(buf.toByteArray())) {
               m_displayName = url.toString();
            }
         }
      }

      /**
       * See the interface method {@link XmlUploader.IContentSource#getContent() getContent}
       * for details.
       */
      public InputStream getContent()
      {
         return m_content;
      }

      /**
       * Closes the associated stream.
       *
       * @throws IOException If the associated stream has any problems while
       * closing.
       */
      public void close( boolean isSuccessful )
         throws IOException
      {
         m_content.close();
      }

      /**
       * See the interface method {@link XmlUploader.IContentSource#getContentLength()
       * getContentLength} for details.
       */
      public long getContentLength()
      {
         return m_content.available();
      }

      /**
       * @return The full URL associated with this object.
       */
      public String getDisplayName()
      {
         return m_displayName;
      }

      /**
       * The contents of the URL are read and stored in this stream during
       * construction. This stream is then returned as the content stream.
       * Initialized during construction and valid for lifetime of object.
       * Never null.
       */
      private ByteArrayInputStream m_content;

      /**
       * A string representation of the URL that was used to create this object.
       * Initialized during construction and valid for lifetime of object. Never
       * null.
       */
      private String m_displayName;
   }

   /**
    * The singleton instance of the logger. All methods use this guy to display
    * status and error messages.
    */
   private static final Logger log = LogManager.getLogger();

   /**
    * A debugging flag. If <code>true</code>, additional debugging information
    * will be written to the logger&apos;s stream. <code>false</code> by
    * default.
    */
   private boolean m_debug = false;

   /**
    * The URL that will be used to submit the xml files to the Rhythmyx server.
    * Initialiazed in the constructor, never <code>null</code> or empty.
    */
   private String m_requestUrl = null;

   /**
    * The contents of the properties file specified on the command line,
    * never <code>null</code>. May be empty if failed to load the properties
    * file.
    */
   private Properties m_properties = new Properties();

   /**
    * Rhythmyx server. Defaults to "127.0.0.1". Never <code>null</code> or empty
    */
   private String m_rxServer = Utils.RX_DEFAULT_SERVER_IP;

   /**
    * Rhythmyx server port. Defaults to 9992.
    */
   private int m_rxPort = Utils.RX_DEFAULT_SERVER_PORT;

   /**
    * The login id for login to the Rhythmyx server.
    * Initialiazed in the constructor, may be <code>null</code> or empty.
    * A dialog box is displayed for entering login id and password if
    * authentication fails.
    */
   private String m_loginId = null;

   /**
    * The password for login to the Rhythmyx server.
    * Initialiazed in the constructor, may be <code>null</code> or empty.
    * A dialog box is displayed for entering login id and password if
    * authentication fails.
    */
   private String m_loginPwd = null;

   /**
    * The community name or id, if numeric then it is interpreted as
    * community id else as community name. Initialiazed in the constructor,
    * may be <code>null</code> or empty.
    */
   private String m_communityId = null;

   /**
    * The psxmldoc parameter passed to the server. This html parameter
    * defines how the uploaded xml document should be treated. Initialiazed
    * in the constructor, may be <code>null</code> or empty.
    */
   private String m_psxmldoc = null;

   /**
    * Set the default timeout in milliseconds. The command line argument
    * is specified in seconds, and converted to milliseconds.
    */
   private int m_timeout = Utils.DEFAULT_TIMEOUT_MILLIS;

   /**
    * The extra html parameters. These html parameters are appended to the URL.
    * Initialiazed in the constructor, may be <code>null</code> or empty.
    */
   private String m_extraParams = null;

   /**
    * The pattern for the message that is displayed when a file is successfully
    * uploaded. The param is the file name. For use w/ MessageFormat.format.
    * Must not contain more than 1 param w/o modifying the calls to format.
    */
   private static final String DEFAULT_SUCCESS = "[{0}] uploaded successfully.";

   /**
    * The pattern for the message that is displayed when a failure occurs at any
    * point in the process. The param is the file name. For use w/
    * MessageFormat.format. Must not contain more than 1 param w/o modifying the
    * calls to format.
    */
   private static final String DEFAULT_ERROR = "[{0}] *************** ERROR ***************";

   /**
    * The pattern for the message that is displayed when a file is uploaded and
    * the return code is success, but the Rx statistics cannot be read for some
    * reason. The param is the file name. For use w/ MessageFormat.format.
    * Must not contain more than 1 param w/o modifying the calls to format.
    */
   private static final String DEFAULT_WARNING = "[{0}] ************** WARNING **************";

   /**
    * The character encoding of the response received from the Rhythmyx server.
    * We could be smarter by reading the content type and getting the encoding
    * from that.
    */
   private static final String DEFAULT_RX_ENCODING = "UTF-8";
}
