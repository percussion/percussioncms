/*[ HttpItemCopier.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

import com.percussion.server.IPSHttpErrors;
import com.percussion.test.http.HttpRequest;
import com.percussion.test.io.IOTools;
import com.percussion.util.PSBase64Encoder;
                       
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Makes http requests against the Rhythmyx clone handler to create multiple
 * copies of an existing item.  This is equivalent to pressing the "New Version"
 * button while viewing an item in the browser.
 */
public class HttpItemCopier
{
   /**
    * This program creates a specified number of copies of a content item by
    * making clone requests against a Rhythmyx CMS server.
    *
    * The syntax of the command is
    * <p>java HttpItemCopier
    *       server [port] appName resourceName contentId revision
    *          numberOfCopies credential
    * <p>The following table lists the arguments and each of their
    * descriptions:
    *
    * <table>
    *    <th>
    *       <td>Param</td>
    *       <td>Description</td>
    *       <td>Required?</td>
    *    </th>
    *    <tr>
    *       <td>server</td>
    *       <td>The name or IP address of the Rhythmyx server. The server must
    *           be running</td>
    *       <td>yes</td>
    *    </tr>
    *    <tr>
    *       <td>port</td>
    *       <td>The port to use.  If not specified, 9992 is used.</td>
    *       <td>no</td>
    *    </tr>
    *    <tr>
    *       <td>appName</td>
    *       <td>The name of the content editor application to make the request
    *           against.</td>
    *       <td>yes</td>
    *    </tr>
    *    <tr>
    *       <td>resourceName</td>
    *       <td>The name of the resource in the app that will handle the 
    *           request.</td>
    *       <td>yes</td>
    *    </tr>
    *    <tr>
    *       <td>contentId</td>
    *       <td>The content id of the item to copy.</td>
    *       <td>yes</td>
    *    </tr>
    *    <tr>
    *       <td>revision</td>
    *       <td>The revision of the item to copy.</td>
    *       <td>yes</td>
    *    </tr>
    *    <tr>
    *       <td>numberOfCopies</td>
    *       <td>The number of copies to create.</td>
    *       <td>yes</td>
    *    </tr>
    *    <tr>
    *       <td>credentials</td>
    *       <td>The userid and password to send with the request.  Must be in
    *           the form userid:pwd.</td>
    *       <td>yes</td>
    *    </tr>
    * </table>
    */
   public static void main(String[] args)
   {
      if (args.length < 7 || args.length > 8)
      {
         System.out.println("Invalid number of arguments.");
         printUsage();
         return;
      }

      boolean hasPort = (args.length == 8);
      try
      {
         HttpItemCopier copier = null;
         int next = 1;
         if(hasPort)
         {
            copier = new HttpItemCopier(args[0], args[1]);
            next++;
         }
         else
         {
            copier = new HttpItemCopier(args[0]);
         }

         String appName = args[next++];
         String resource = args[next++];
      
         int contentId;
         int revision;
         int numCopies;
         try
         {
            contentId = Integer.parseInt(args[next++]);
         }
         catch (NumberFormatException e)
         {
            System.out.println("Invalid contentId");
            printUsage();
            return;
         }

         try
         {
            revision = Integer.parseInt(args[next++]);
         }
         catch (NumberFormatException e)
         {
            System.out.println("Invalid revision");
            printUsage();
            return;
         }

         try
         {
            numCopies = Integer.parseInt(args[next++]);
         }
         catch (NumberFormatException e)
         {
            System.out.println("Invalid number of copies");
            printUsage();
            return;
         }

         String credential = args[next++];

         String result = copier.makeCopies(appName, resource, contentId,
            revision, numCopies, credential);

         System.out.println(result);
      }
      catch (Exception e)
      {
         System.out.println("An error has occurred:");
         System.out.println( e.getLocalizedMessage());
      }
   }

   /**
    * Prints command line usage of this class to the console.
    */
   public static void printUsage()
   {
      System.out.println( "Usage:" );
      System.out.println("java HttpItemCopier server [port] appName " +
         "resourceName contentId revision numberOfCopies userid:pw");
   }


   /**
    * Constructor for this class.
    *
    * @param server The server to make requests against.  May not be <code>null
    * </code> or empty.
    * @param port The port to use.  If <code>null</code> or empty, 9992 is used.
    *
    * @throws IllegalArgumentException if server is <code>null</code> or empty.
    */
   public HttpItemCopier(String server, String port)
   {
      if (server == null || server.trim().length() == 0)
         throw new IllegalArgumentException("server may not be null or empty");

      m_server = server;

      if (port == null || port.trim().length() == 0)
      {
         m_port = 9992;
      }
      else
      {
         try
         {
            m_port = Integer.parseInt(port);
         }
         catch (NumberFormatException e)
         {
            throw new IllegalArgumentException("Invalid port specified");
         }
      }
   }

   /**
    * Convenience ctor that passes <code>null</code> for the port.  See
    * {@link HttpItemCopier(String, String)} for more info.
    */
   public HttpItemCopier(String server)
   {
      this(server, null);
   }

   /**
    * Creates copies of the specified item.  
    *
    * @param appName The name of the content editor app to use.  May not be
    * <code>null</code> or empty.
    * @param resourceName The name of the content editor resource to use.  May
    * not be<code>null</code> or empty.  Must resolve to an existing resource in
    * the specified app.
    * @param contentId The content id of the item to copy.  If an item with this
    * content id does not exist, an error message is returned.
    * @param revisionId The revision id of the item to copy.  If this revision
    * is not found for the specified content id, and error message is returned.
    * @param numCopies The number of copies to make.  Must be greater than zero.
    * @param credential The userid and password to use when making the request.
    * Must be in the form uid:pwd.  If <code>null</code> or empty, no
    * credentials will be passed to the server.
    *
    * @return A message indicating success or errors, if any.  If an error is
    * returned, it will include the HTML that is returned by the PSCloneHandler.
    *
    * @throws IllegalArgumentException if appName or resourceName are <code>
    * null</code> or empty, or if numCopies is not greater than zero.
    *
    */
   public String makeCopies(String appName, String resourceName, int contentId,
      int revisionId, int numCopies, String credential)
   {
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");

      if (resourceName == null || resourceName.trim().length() == 0)
         throw new IllegalArgumentException(
            "resourceName may not be null or empty");

      if (numCopies <= 0)
         throw new IllegalArgumentException("numCopies must be greater than 0");

      String result = "Created " + numCopies + " copies";
      HttpRequest req = null;
      int i = 0;

      try
      {
         // construct the url
         StringBuilder strUrl = new StringBuilder("/Rhythmyx/");
         strUrl.append(appName);
         strUrl.append("/");
         strUrl.append(resourceName);
         strUrl.append("?sys_command=clone");
         strUrl.append("&sys_contentid=");
         strUrl.append(String.valueOf(contentId));
         strUrl.append("&sys_revision=");
         strUrl.append(String.valueOf(revisionId));

         URL url = new URL("http", m_server, m_port, strUrl.toString());
         req = new HttpRequest(url);

         if (credential != null & credential.trim().length() > 0)
         {
            req.addRequestHeader("Authorization", "Basic " +
               PSBase64Encoder.encode(credential));
         }
         
         // make appropriate number of requests
         for (i = 0; i < numCopies; i++)
         {
            req.sendRequest();
            int resp = req.getResponseCode();

            // we are expecting a redirect as a sign of success
            if (resp != IPSHttpErrors.HTTP_MOVED_TEMPORARILY)
            {
                result = "An error occurred after creating " + i + " copies: \n";

                // get content and write as error message
                InputStream in = req.getResponseContent();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                IOTools.copyStream(in, out);
                result += out.toString();
                break;
            }
         }
      }
      catch(Exception e)
      {
         result = "An error occurred after creating " + i + " copies: \n";
         result += e.toString();
      }
      finally
      {
         if (req != null)
         {
            try {req.disconnect();} catch (Exception e){}
         }
      }
      return result;
   }


   /**
    * Name of the Rhythmyx server.  Initialized during construction, never
    * modified after that.
    */
   private String m_server = null;

   /**
    * The port to use.  Set during construction, never modified after that.
    */
   private int m_port;

}
