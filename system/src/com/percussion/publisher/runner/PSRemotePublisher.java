/******************************************************************************
 *
 * [ PSRemotePublisher.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.publisher.runner;

import com.percussion.HTTPClient.HTTPConnection;
import com.percussion.HTTPClient.HTTPResponse;
import com.percussion.HTTPClient.ModuleException;
import com.percussion.HTTPClient.NVPair;
import com.percussion.server.agent.PSUtils;
import com.percussion.tools.Base64;
import com.percussion.tools.PSHttpRequest;
import com.percussion.tools.PSURIEncoder;
import com.percussion.utils.tools.PSCopyStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class provides a means of initiating publication of a specified
 * edition. This is basically done by sending an HTTP request with all
 * parameters required to start publishing an edition.
 */
public class PSRemotePublisher
{
    /**
     * This method is the one which does actual publishing. It initiates
     * publication of an edition by making an HTTP request to the Rhythmyx
     * Publisher Manager on the server.
     *
     * @param server - name or IPAddress of the Rhythmyx server, must not be
     * <code>null</code> or <code>empty</code>.
     *
     * @param port - port number (as String) of the Rhythmyx server, may be
     * <code>null</code> or <code>empty</code> in which case a default port is
     * used.
     *
     * @param editionid - EditionID to publish (as String) of the Rhythmyx
     * server, must not be <code>null</code> or <code>empty</code>.
     *
     * @param userid - UserId to access the Rhythmyx CMS, may be
     * <code>null</code> or <code>empty</code>.
     *
     * @param password - password to access the Rhythmyx CMS, may be
     * <code>null</code> or <code>empty</code>.
     *
     * @param useSSL - flag to indicate whther to use secure publishing or not.
     * <code>true</code> to publish using SSL and <code>false</code> otherwise.
     *
     * @return output (as String) resulting from the HTTP request to the Rhythmyx
     * Publisher Manager on the server.
     *
     * @throws IllegalArgumentException if invalid server or editionid is
     * specified.
     *
     * @throws PSPublisherRunnerException if publisher initiation fails for any
     * reason.
     *
     */
     public static String publish(String server, String port, String editionid,
                                 String userid, String password, boolean useSSL)
            throws PSPublisherRunnerException
    {
        if(server == null || server.trim().length() < 1)
        {
            throw new IllegalArgumentException("server cannot be null or empty");
        }
        if(editionid == null || editionid.trim().length() < 1)
        {
            throw new IllegalArgumentException(
                    "editionid cannot be null or empty");
        }
        if(port == null)
        {
            port = "0";
        }
        if(userid == null)
        {
            userid = "";
        }

        //save the original password before it is encoded so we can use it when
        //adding basic authorization
        String origPassword = password;

        if(password == null)
        {
            password = "";
            origPassword = "";
        }
        else
        {
            //base64 encode the password
            password = Base64.encode(password.getBytes());
            //password may contain special chars - escape it.
            password = PSURIEncoder.escape(password);
        }

        URL urlQuery = null;
        String url = "/Rhythmyx/sys_pubHandler/publisher.xml?PUBAction=" +
                "publish&editionid=" + editionid + "&userid=" + userid+ "&password=" +
                password;
        String protocol = "http";
        if(useSSL)
        {
            protocol = "https";
            System.setProperty("java.protocol.handler.pkgs", "HTTPClient");
        }
        InputStream content = null;
        ByteArrayOutputStream out = null;
        String result = null;
        try
        {
            String sHost = server;
            if(!port.equals("0") && !port.equals("80"))
            {
                sHost += ":" +port;
            }
            int nPort = 0;
            try
            {
                nPort = Integer.parseInt(port);
            }
            catch(NumberFormatException e)
            {
                //nPort = 0;
            }

            if(nPort == 0 || nPort == 80)
                urlQuery = new URL(protocol, server, url);
            else
                urlQuery = new URL(protocol, server, nPort, url);
            HTTPConnection con = new HTTPConnection(urlQuery);

            con.setAllowUserInteraction(false);
            con.setTimeout(PSUtils.DEFAULT_TIMEOUT_MILLIS);


            NVPair[] defaultHeaders =
                    { new NVPair(PSHttpRequest.HTTP_USERAGENT,
                            "Rhythmyx Publisher Runner") };
            con.setDefaultHeaders(defaultHeaders);

            con.addBasicAuthorization("", userid, origPassword);

            NVPair[] headers = null;

            HTTPResponse   resp = con.Get(urlQuery.getFile(),
                    (String)null,
                    headers);

            int nStatus = resp.getStatusCode();

            // here we don't check for the status range!!!
            if(nStatus != PSHttpRequest.HTTP_STATUS_OK)
            {
                throw new PSPublisherRunnerException("HTTP Error - " +
                        "Error code: " + nStatus);
            }

            content = resp.getInputStream();

            out = new ByteArrayOutputStream();
            PSCopyStream.copyStream(content, out);
            result = out.toString();
            out.close();
            content.close();
            content = null;
        }
        catch(MalformedURLException e)
        {
            throw new PSPublisherRunnerException(e.getMessage());
        }
        catch(IOException e)
        {
            throw new PSPublisherRunnerException(e.getMessage());
        }
        catch(ModuleException e)
        {
            throw new  PSPublisherRunnerException(e.getMessage());
        }
        finally
        {
            urlQuery = null;
            if ( null != content)
            {
                try
                {
                    content.close();
                } catch (Throwable e)
                {

                }
            }
            content = null;
            out = null;
        }

        return result;
    }

    /**
     * The main method
     */
    public static void main(String[] args)
    {
        if(args.length < 3)
        {
            System.err.println("Usage: java PSRemotePublisher <server> <port> " +
                    "<editionid> [cmsuserid] [password]");
            System.err.println();
            System.err.println("where");
            System.err.println("  server     is name or IP Address of the " +
                    "Rhythmyx server to initiate publish from");
            System.err.println("  port       is listening port number of the " +
                    "Rhythmyx server, 0 to use standard HTTP port");
            System.err.println("  editionid  is the EditionID of the edition " +
                    "to be published");
            System.err.println("  cmsuserid  is the UserID to access Rhythmyx " +
                    "CMS applications, optional");
            System.err.println("  password   is the Password to access Rhythmyx "
                    + "CMS applications, optional");
            System.err.println("  useSSL   is the flag to indicate whether to "
                    + "use SSL connection to Rhythmyx serverPassword " +
                    "to access Rhythmyx server (Yes/No) case " +
                    "insensitive, optional default is No");
            System.exit(1);
        }

        String server = args[0];
        String port = args[1];
        String editionid = args[2];
        String userid = null;
        String password = null;
        boolean useSSL = false;
        if(args.length > 3)
        {
            userid = args[3];
        }
        if(args.length > 4)
        {
            password = args[4];
        }
        if(args.length > 5)
        {
            String temp = args[5];
            if(temp.equalsIgnoreCase("yes"))
                useSSL = true;
        }
        try
        {
            System.out.println(
                    publish(server, port, editionid, userid, password, useSSL));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }
}
