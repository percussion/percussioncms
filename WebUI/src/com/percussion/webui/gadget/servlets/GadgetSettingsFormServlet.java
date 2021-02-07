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
package com.percussion.webui.gadget.servlets;



import com.percussion.delivery.client.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author erikserating
 *
 */
public class GadgetSettingsFormServlet extends HttpServlet
{
   private static Logger m_log = Logger.getLogger(GadgetSettingsFormServlet.class.getName());
   private boolean sslSocketFactoryRegistered;

   private void registerSslProtocol()
    {

        if (sslSocketFactoryRegistered)
            return;

        ProtocolSocketFactory socketFactory = new EasySSLProtocolSocketFactory();
        Protocol.registerProtocol("https", new Protocol("https", socketFactory, 443));

        sslSocketFactoryRegistered = true;
    }

    /**
     * A convenience method which can be overridden so that there's no need
     * to call <code>super.init(config)</code>.
     *
     * <p>Instead of overriding {@link # init(ServletConfig)}, simply override
     * this method and it will be called by
     * <code>GenericServlet.init(ServletConfig config)</code>.
     * The <code>ServletConfig</code> object can still be retrieved via {@link
     * #getServletConfig}.
     *
     * @throws ServletException if an exception occurs that
     *                          interrupts the servlet's
     *                          normal operation
     */
    @Override
    public void init() throws ServletException {
        super.init();

        registerSslProtocol();
    }

    /* (non-Javadoc)
    * @see javax.servlet.http.HttpServlet#doGet(
    *    javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   @SuppressWarnings("unused")
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
           throws IOException
   {
      String gadgetUrl = req.getParameter("gurl");
      String moduleId = req.getParameter("mid");
      resp.setContentType("application/javascript");

      PrintWriter out = resp.getWriter();
      if(gadgetUrl != null)
      {
         JSONObject meta = getGadgetMeta(req, gadgetUrl, moduleId);
         List<JSONObject> prefs = extractUserPrefs(meta);
         String scheme = req.getScheme();
         String serverName = "127.0.0.1";
         int serverPort = req.getServerPort();
         PSUserPrefFormContent formContent =
                 new PSUserPrefFormContent(prefs, moduleId, getUpParams(req),
                         serverName, scheme, serverPort,
                         getPSSessionId(req));
         //m_log.error("JETTY TODO - get correct hostname and port",new Throwable());
         out.println(formContent.toJavaScript());
      }
      else
      {
         out.println("// Gadget URL must be specified.");
      }
   }

   /**
    * Calls the gadget metadata service to get information for the specified gadget url.
    * @param req the servlet request, assumed not <code>null</code>.
    * @param url the gadget.xml url, assumed not <code>null</code> or empty.
    * @param moduleId
    * @return
    * @throws IOException
    */
   @SuppressWarnings("unchecked")
   private JSONObject getGadgetMeta(
           HttpServletRequest req,
           String url,
           String moduleId) throws IOException{

      JSONObject obj = new JSONObject();
      JSONObject gadget = new JSONObject();

      JSONObject context = new JSONObject();
      context.put("country", "US");
      context.put("language", "en");
      context.put("view", "default");
      context.put("container", "default");

      JSONArray gadgets = new JSONArray();
      Map<String, String> upParams = getUpParams(req);
      if(moduleId == null)
         moduleId = "0";
      gadgets.add(gadget);
      obj.put("context", context);
      obj.put("gadgets", gadgets);

      gadget.put("url", url);
      gadget.put("moduleId", moduleId);
      if(!upParams.isEmpty())
      {
         JSONObject ups = new JSONObject();
         for(String key : upParams.keySet())
         {
            ups.put(key, upParams.get(key));
         }
         gadget.put("prefs", ups);
      }
      String serverName = "127.0.0.1";
      int serverPort = req.getServerPort();

      URL metaDataServiceURL = new URL(req.getScheme(),
              serverName,
              serverPort,
              METADATA_SERVICE_URL);

      //m_log.error("JETTY TODO - get correct hostname and port",new Throwable());
      String result =
              makeJSONPostRequest(metaDataServiceURL.toString(), obj.toString());
      JSONParser parser=new JSONParser();
      JSONObject meta = null;
      try
      {
         meta = (JSONObject)parser.parse(new StringReader(result));
         return meta;
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw new IOException("Problem retrieving metadata.");
      }
   }

   /**
    * Sends a JSON string to a specific URL and returns the plain response of the server
    */
   private String makeJSONPostRequest(String url, String jsonString) throws IOException
   {

      HttpClient httpClient = new HttpClient();

      PostMethod post = new PostMethod(url);
      post.setRequestHeader("x-shindig-dos", "true");
      post.setRequestEntity(new StringRequestEntity(jsonString, "application/json", "UTF-8"));

      try
      {
         httpClient.executeMethod(post);
         return post.getResponseBodyAsString();
      }
      finally
      {
         post.releaseConnection();
      }

   }


   /**
    *
    * @param meta
    * @return
    */
   @SuppressWarnings("unchecked")
   private List<JSONObject> extractUserPrefs(JSONObject meta){
      List<JSONObject> results = new ArrayList<JSONObject>();
      JSONArray gArr = (JSONArray)meta.get("gadgets");
      if(gArr != null)
      {
         JSONObject prefs = (JSONObject)((JSONObject)gArr.get(0)).get("userPrefs");
         if(!prefs.isEmpty())
         {
            List<String> keys = new ArrayList<String>();
            for(Object k : prefs.keySet())
               keys.add((String)k);
            Collections.sort(keys);
            for(String key : keys)
            {
               JSONObject vals = (JSONObject)prefs.get(key);
               vals.put("fieldname", key);
               results.add(vals);
            }
         }
      }
      return results;
   }

   /**
    * Helper method to retrieve all user preference value params from the request.
    * @param req assumed not <code>null</code>.
    * @return map of user pref params.
    */
   @SuppressWarnings("unchecked")
   private Map<String, String> getUpParams(HttpServletRequest req)
   {
      Map<String, String> params = new HashMap<String, String>();
      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
         String name = (String)en.nextElement();
         if(name.startsWith("up_"))
            params.put(name, req.getParameter(name));
      }
      return params;
   }

   /**
    * Retrieve the pssessionid value from the request header.
    * @param request the request assumed not <code>null</code>.
    * @return the pssessionid value or <code>null</code> if not found.
    */
   private String getPSSessionId(HttpServletRequest request)
   {
      Cookie[] cookies = request.getCookies();
      for(Cookie cookie : cookies)
      {
         if(cookie.getName().equals(PSSESSIONID))
            return cookie.getValue();
      }
      return null;
   }



   private static final String METADATA_SERVICE_URL = "/cm/gadgets/metadata";
   private static final String PSSESSIONID = "JSESSIONID";



}
