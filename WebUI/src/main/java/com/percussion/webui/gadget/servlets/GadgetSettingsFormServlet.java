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
package com.percussion.webui.gadget.servlets;


import com.percussion.error.PSExceptionUtils;
import com.percussion.security.ToDoVulnerability;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.ReadListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
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
   private static final Logger log = LogManager.getLogger(GadgetSettingsFormServlet.class.getName());


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
    }

    /* (non-Javadoc)
    * @see javax.servlet.http.HttpServlet#doGet(
    *    javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
      try {
         String gadgetUrl = req.getParameter("gurl");

         if(!PSGadgetUtils.isValidGadgetPathInUrl(req, new URI(gadgetUrl))){
            resp.sendError(404);
         }

         String moduleId = req.getParameter("mid");

         resp.setContentType("application/javascript");

         PrintWriter out = resp.getWriter();
         if (gadgetUrl != null) {
            JSONObject meta = getGadgetMeta(req, resp, gadgetUrl, moduleId);
            List<JSONObject> prefs = extractUserPrefs(meta);
            PSUserPrefFormContent formContent =
                    new PSUserPrefFormContent(prefs, moduleId, getUpParams(req), this, req,resp);

            out.println(formContent.toJavaScript());
         } else {
            out.println("// Gadget URL must be specified.");
         }
      } catch (IOException | URISyntaxException e) {
         log.error(PSExceptionUtils.getMessageForLog(e));
         log.debug(e);
         try{
            resp.sendError(404);
         } catch (IOException ioException) {
            resp.reset();
            resp.setStatus(404);
         }

      }
   }

   /**
    * Validate input parameters for sane inputs.
    */
   private void validateInputParameters(){

   }

   /**
    * Calls the gadget metadata service to get information for the specified gadget url.
    * @param req the servlet request, assumed not <code>null</code>.
    * @param url the gadget.xml url, assumed not <code>null</code> or empty.
    * @param moduleId The Module Id
    * @return A json object containing the Gadget metadata
    * @throws IOException
    */
   @SuppressWarnings("unchecked")
   private JSONObject getGadgetMeta(
           HttpServletRequest req,
           HttpServletResponse response,
           String url,
           String moduleId) throws IOException {

      JSONObject obj = new JSONObject();
      JSONObject gadget = new JSONObject();

      JSONObject context = new JSONObject();
      context.put("country", "US");
      context.put("language", "en");
      context.put("view", "default");
      context.put("container", "default");

      JSONArray gadgets = new JSONArray();
      Map<String, String> upParams = getUpParams(req);
      if (moduleId == null)
         moduleId = "0";
      gadgets.add(gadget);
      obj.put("context", context);
      obj.put("gadgets", gadgets);

      gadget.put("url", url);
      gadget.put("moduleId", moduleId);
      if (!upParams.isEmpty()) {
         JSONObject ups = new JSONObject();
         for (String key : upParams.keySet()) {
            ups.put(key, upParams.get(key));
         }
         gadget.put("prefs", ups);
      }

      String result = null;

      try{
      RequestDispatcher dispatcher =
              this.getServletContext().getRequestDispatcher(METADATA_SERVICE_URL);
      StringWriter sw = new StringWriter();
      final PrintWriter pw = new PrintWriter(sw);

      //Wrap the response so that we can capture output from the rpc servlet.
      HttpServletResponse responseWrapper =
              new HttpServletResponseWrapper(response) {
                 @Override
                 public PrintWriter getWriter() throws IOException {
                    return pw;
                 }
              };

      /**
       * We wrap the request here so that we can override the inputs to the RCP Servlet.
       */
      HttpServletRequest requestWrapper = new HttpServletRequestWrapper(req) {
         /**
          * The default behavior of this method is to return getMethod()
          * on the wrapped request object.
          */
         @Override
         public String getMethod() {
            return "POST";
         }

         final byte[] bytes = obj.toString().getBytes(StandardCharsets.UTF_8);

         /**
          * The default behavior of this method is to return getInputStream()
          * on the wrapped request object.
          */
         @Override
         public ServletInputStream getInputStream() throws IOException {
            return new ServletInputStream() {
               private int lastIndexRetrieved = -1;
               private ReadListener readListener = null;

               @Override
               public boolean isFinished() {
                  return (lastIndexRetrieved == bytes.length - 1);
               }

               @Override
               public boolean isReady() {
                  // This implementation will never block
                  // We also never need to call the readListener from this method, as this method will never return false
                  return isFinished();
               }

               @Override
               public void setReadListener(ReadListener readListener) {
                  this.readListener = readListener;
                  if (!isFinished()) {
                     try {
                        readListener.onDataAvailable();
                     } catch (IOException e) {
                        readListener.onError(e);
                     }
                  } else {
                     try {
                        readListener.onAllDataRead();
                     } catch (IOException e) {
                        readListener.onError(e);
                     }
                  }
               }

               @Override
               public int read() throws IOException {
                  int i;
                  if (!isFinished()) {
                     i = bytes[lastIndexRetrieved + 1];
                     lastIndexRetrieved++;
                     if (isFinished() && (readListener != null)) {
                        try {
                           readListener.onAllDataRead();
                        } catch (IOException ex) {
                           readListener.onError(ex);
                           throw ex;
                        }
                     }
                     return i;
                  } else {
                     return -1;
                  }
               }
            };
         }
      };
         dispatcher.include(requestWrapper, responseWrapper);
         result = sw.toString();
   } catch (ServletException e) {
         log.error(PSExceptionUtils.getMessageForLog(e));
      }

      if(result != null) {
         JSONParser parser = new JSONParser();
         JSONObject meta;
         try {
            meta = (JSONObject) parser.parse(new StringReader(result));
            return meta;
         } catch (Exception e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(e);
            throw new IOException("Problem retrieving metadata.");
         }
      }else{
         throw new IOException("Unable to retrieve metadata.");
      }
   }

   /**
    *
    * @param meta a de-serialized gadget metadata entry json object
    * @return A list of JSON objects representing userPrefs
    */
   private List<JSONObject> extractUserPrefs(JSONObject meta){
      List<JSONObject> results = new ArrayList<>();
      JSONArray gArr = (JSONArray)meta.get("gadgets");
      if(gArr != null)
      {
         JSONObject prefs = (JSONObject)((JSONObject)gArr.get(0)).get("userPrefs");
         if(!prefs.isEmpty())
         {
            List<String> keys = new ArrayList<>();
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
   @ToDoVulnerability //This needs to validate the parameters for injection
   private Map<String, String> getUpParams(HttpServletRequest req)
   {
      Map<String, String> params = new HashMap<>();
      Enumeration<String> en = req.getParameterNames();
      while(en.hasMoreElements())
      {
         String name = en.nextElement();
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
