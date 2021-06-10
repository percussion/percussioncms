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
package com.percussion.test.http;

import com.percussion.test.io.IOTools;
import com.percussion.test.io.IOTools;
import com.percussion.test.io.LogSink;
import com.percussion.util.PSURLEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * An interactive low-level HTTP requestor. It lets you repeatedly send
 * GET and POST requests to an HTTP server, allowing you to set individual
 * header values and whatnot.
 */
public class HttpInteractive implements LogSink
{

   private static final Logger log = LogManager.getLogger(HttpInteractive.class);
   /**
    * The main entry point.
    */
   public static void main(String[] args)
   {
      try
      {
         ms_in = new BufferedReader(new InputStreamReader(System.in));
         boolean again = false;
         HttpInteractive http = new HttpInteractive();
         do
         {
            try
            {
               http.run();
               System.err.println("\n\n");
               again = yesNo("Send another request?", again);
            }
            catch (Throwable t)
            {
               log.error(t.getMessage());
               log.debug(t.getMessage(), t);
            }
         } while (again);
      }
      catch (Throwable t)
      {
         log.error(t.getMessage());
         log.debug(t.getMessage(), t);
      }
      System.err.println("Finished");
   }

   /**
    * Construct a new HttpInteractive object. It can be re-used
    * interactively again and again by calling the run() method.
    * It remembers your last choices so it is easier to run
    * subsequent similar or identical HTTP requests the second
    * time around.
    */
   public HttpInteractive()
      throws IOException
   {
   }

   /**
    * Queries the user for the desired settings, then sends
    * the request, captures the output, and optionally
    * displays the output.
    */
   public void run() throws IOException, HttpConnectException
   {
      HttpRequest req = buildRequest();
      req.enableTrace(this);
      System.err.println("\nReady to send request.");
      boolean send = yesNo("Send request (N=quit)?", true);
      if (send)
      {
         req.sendRequest();
         HttpHeaders hdrs = req.getResponseHeaders();
         File tmpFile = File.createTempFile("http_", ".out", new File("."));
         System.err.println("Saving content to " + tmpFile.toString() + "...");
         OutputStream out = null;
         try
         {
            out = new BufferedOutputStream(new FileOutputStream(tmpFile));
            long bytes = IOTools.copyStream(req.getResponseContent(), out);
            System.err.println("Wrote " + bytes + " bytes.");
         }
         finally
         {
            if (out != null)
               out.close();
         }

         boolean shouldPrint = yesNo("Print file to screen?", true);
         if (shouldPrint)
         {
            InputStream in = null;
            try
            {
               in = new BufferedInputStream(new FileInputStream(tmpFile));
               IOTools.copyStream(in, System.out);
            }
            finally
            {
               if (in != null)
                  in.close();
            }
         }
      }
   }

   /**
    * Queries the user for request settings.
    */
   public HttpRequest buildRequest()
      throws IOException
   {
      System.err.println("Please answer the following questions about the request.");
      m_method = getResponse("Method (GET or POST)", m_method).toUpperCase();
      m_protocol = getResponse("Protocol", m_protocol).toLowerCase();
      m_host = getResponse("Host", m_host);
      String portStr = getResponse("Port", "" + m_port);
      m_port = Integer.parseInt(portStr);

      m_url = getResponse(
         "URL (not including the query string -- for example, /Rhythmyx/MyApp/request.xml)", m_url);
      
      m_shouldEncode = yesNo("Should we encode the URL?", m_shouldEncode);
      if (m_shouldEncode)
      {
         m_url = PSURLEncoder.encodePath(m_url);
         // System.err.println("Encoded URL: " + url);
      }

      m_query = getResponse("Query string (for example, ?foo=bar&bar=baz)", m_query);
      if (m_query.length() > 0)
      {
         if (!m_query.startsWith("?"))
         {
            System.err.println("WARNING: Automatically prepending a ? to the query");
            m_query = "?" + m_query;
         }

         m_shouldEncodeQuery = yesNo("Should we encode the query?", !m_shouldEncodeQuery);
         if (m_shouldEncodeQuery)
         {
            m_query = PSURLEncoder.encodeQuery(m_query);
            // System.err.println("Encoded query: " + query);
         }
      }
      String fullUrl = m_protocol + "://" + m_host + ":" + m_port + m_url + m_query;
      System.err.println("Full URL is: " + fullUrl);

      HttpRequest req = new HttpRequest(fullUrl, m_method, null);

      if (m_reqHdrs != null)
      {
         boolean addPrevHdrs = yesNo("Use previous request headers?", true);
         if (!addPrevHdrs)
         {
            m_reqHdrs = new HttpHeaders();
         }
      }
      else
      {
         m_reqHdrs = new HttpHeaders();
      }

      System.err.println("Enter header values one per line in the form Name: value");
      System.err.println("An empty line finishes headers.");
      while (true)
      {
         String hdr = getResponse("", "");
         if (hdr.length() == 0)
            break;

         int colonPos = hdr.indexOf(":");
         int startVal = colonPos + 1;
         if (-1 == colonPos)
         {
            System.err.println("WARNING: Malformed header \"" + hdr + "\"");
            boolean shouldKeep = yesNo("Are you sure you want to send this header?", false);
            if (!shouldKeep)
               continue;
            colonPos = hdr.length();
         }
         
         String hdrName = hdr.substring(0, colonPos).trim();
         if (colonPos == hdr.length())
            colonPos = hdr.length() - 1;
         String hdrVal = hdr.substring(colonPos+1).trim();

         System.err.println("Adding header NAME(" + hdrName + ") VALUE(" + hdrVal + ")");
         m_reqHdrs.addHeader(hdrName, hdrVal);
      }

      req.addRequestHeaders(m_reqHdrs);

      m_hasContent = yesNo("Does request have content?", m_hasContent);
      if (m_hasContent)
      {
         long contentLength = 0;
         InputStream content = null;
         m_isFile = yesNo("Send content from file?", m_isFile);
         while (m_isFile)
         {
            m_filename = getResponse("Filename", m_filename);
            File f = new File(m_filename);
            if (!f.exists() || f.isDirectory())
            {
               System.err.println("Cannot open file " + f.getCanonicalPath());
            }
            else
            {
               System.err.println("Opened file " + f.getCanonicalPath()
                  + " (" + f.length() + " bytes)");
               contentLength = f.length();
               content = new BufferedInputStream(new FileInputStream(f));
               break;
            }
         }

         if (!m_isFile)
         {
            StringBuffer buff = new StringBuffer();
            System.err.println("Type the content below. End with a line containing only a .");
            while (true)
            {
               String line = ms_in.readLine();
               if (line.equals("."))
                  break;
               buff.append(line);
            }

            m_charset = getResponse("Character encoding", m_charset);
            
            // TODO: allow content-type

            byte[] bytes = buff.toString().getBytes(m_charset);
            contentLength = bytes.length;
            content = new ByteArrayInputStream(bytes);
            System.err.println("Encoded input to " + bytes.length + " bytes.");
         }

         boolean shouldCalcLen = yesNo(
            "Should we set the Content-Length header to " + contentLength + "?", true);
         if (shouldCalcLen)
            req.addRequestHeader("Content-Length", "" + contentLength);

         req.setRequestContent(content);
      }

      return req;
   }

   /**
    * Gets a one-line response from the user. Prompts with the given
    * string, and also displays and uses the given defaultValue. If
    * the user presses ENTER, the given default value will be used.
    * If the given default value is <CODE>null</CODE>, then an empty
    * line will not be accepted, and the prompt will be repeated
    * until the user types something in.
    */
   public static String getResponse(String prompt, String defaultValue)
      throws IOException
   {
      String val = null;
      while (val == null)
      {
         System.err.print(prompt);
         if (defaultValue != null)
            System.err.print(" [" + defaultValue + "]");
         System.err.print(" : ");
         val = ms_in.readLine();
         if (val == null)
         {
            throw new IOException("Standard input closed.");
         }
         else if (val.trim().length() == 0)
         {
            if (defaultValue == null)
            {
               System.err.println("\07Response required.");
            }
            val = defaultValue;
         }
         else
         {
            break;
         }
      }
      return val.trim();
   }

   /**
    * Prompts the user for a yes or no response. Returns true
    * if the user chose yes, false if the user chose no. The
    * default will be returned if the user hits ENTER. If the
    * user types something other than a yes or a no, the
    * input will not be accepted.
    */
   public static boolean yesNo(String prompt, boolean defaultYes)
      throws IOException
   {
      String def = (defaultYes ? "Y" : "N");
      while (true)
      {
         String resp = getResponse(prompt, def);
         if (resp.toLowerCase().startsWith("y"))
            return true;
         else if (resp.toLowerCase().startsWith("n"))
            return false;
         else
            System.err.println("\07Please answer Y or N.");
      }
   }

   public void log(String msg, Throwable t)
   {
      log(msg);
      log(t);
   }

   public void log(Throwable t)
   {
      t.printStackTrace();
   }

   public void log(String message)
   {
      System.err.println("\t" + message);
   }

   private String m_method = "GET";
   private String m_protocol = "http";
   private String m_host = "localhost";
   private int m_port = 80;
   private String m_url = null;
   private boolean m_shouldEncode = false;
   private boolean m_shouldEncodeQuery = false;
   private String m_query = "";
   private boolean m_hasContent = false;
   private boolean m_isFile = false;
   private String m_filename = null;
   private String m_charset = System.getProperty("file.encoding");
   private HttpHeaders m_reqHdrs;

   private static BufferedReader ms_in;
}
