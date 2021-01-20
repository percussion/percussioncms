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

package com.percussion.services.integrations.siteimprove;

import com.percussion.delivery.client.TLSV12ProtocolSocketFactory;
import com.percussion.security.TLSSocketFactory;
import com.percussion.server.PSServer;
import com.percussion.services.integrations.IPSIntegrationProviderService;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Services for the REST endpoint for our Siteimprove integration.
 */
public class PSSiteImproveProviderService implements IPSIntegrationProviderService
{

   // The api endpoints for Siteimprove's api.
   private static final String NEW_SITEIMPROVE_BASE_URL = "https://api-gateway.siteimprove.com/cms-recheck";
   // on a 5.4.4 build, this looks like - 'PercussionCM15.4.4'
   private static final String PERCUSSION_CM1_VERSION = "PercussionCM1" + PSServer.getVersion();
   private static final String SITEIMPROVE_TOKEN_URL = "https://my2.siteimprove.com/auth/token?cms="
         + PERCUSSION_CM1_VERSION;
   private static final String SITEIMPROVE_RECRAWL_SITE = "recrawl";
   private static final String SITEIMPROVE_RECHECK_PAGE = "recheck";
   private static final String SITEIMPROVE_TOKEN = "token";

   // Header strings
   private static final String ACCEPTS = "Accept";
   private static final String APPLICATION_JSON = "application/json";
   private static final String CONTENT_TYPE = "Content-Type";
   private static final String UTF_8 = "UTF-8";
   private static ExecutorService pool = Executors.newFixedThreadPool(1);
   private static Log logger = LogFactory.getLog(PSSiteImproveProviderService.class);
   
   private static final int HTTPS_PORT = 443;
   private static final String DEFAULT_PROTOCOL = "http";

   /**
    * Gets a new Siteimprove token for the site. It is to be saved/persisted in
    * PSMetadata object unless the feature is disabled/re-enabled for a site.
    * 
    * @return the token. empty string if not set
    */
   public String getNewSiteImproveToken()
   {

    try{
       registerSslProtocol();
    }catch(Exception e){
       logger.error("Error initilizing SSL Engine: " + e);
       return "";
    }

      GetMethod getMethod = new GetMethod(SITEIMPROVE_TOKEN_URL);
      try
      {
         executeMethod(getMethod);
      }
      catch (Exception e1)
      {
         logger.error("Unable to get new Siteimprove token with message: " + e1);
         return "";
      }
      String token = "";

      try
      {
         JSONObject jsonObjectItems = new JSONObject(getMethod.getResponseBodyAsString());
         token = jsonObjectItems.getString(SITEIMPROVE_TOKEN);
      }
      catch (IOException e)
      {
         logger.error("Failed to get new Siteimprove token with message: " + e);
      }
      catch (JSONException e)
      {
         logger.error("Failed to get new Siteimprove token with message: " + e);
      }

      return token;

   }

   /**
    * Request a site check from siteimprove. Site is determined by id.
    *
    * @param siteId Site id of the site we wish to check.
    * @param credentials The credentials allowing us to access the siteimprove
    *           api.
    * @throws Exception Siteimprove rejected our request or the site id was bad.
    */
   @Override
   public void updateSiteInfo(final String siteId, final Map<String, String> credentials) throws Exception
   {

      if (siteId == null || siteId.isEmpty())
      {
         throw new NullPointerException("siteURL cannot be null or empty.");
      }

      pool.submit(new Runnable()
      {
         @Override
         public void run()
         {
            try
            {

               registerSslProtocol();

               PostMethod postMethod = new PostMethod(NEW_SITEIMPROVE_BASE_URL);

               JSONObject object = new JSONObject();
               object.accumulate("url", siteId);
               object.accumulate(SITEIMPROVE_TOKEN, credentials.get(SITEIMPROVE_TOKEN));
               object.accumulate("type", SITEIMPROVE_RECRAWL_SITE);

               logger.debug("JSON Object body:" + object.toString());

               StringRequestEntity requestEntity = new StringRequestEntity(object.toString(), APPLICATION_JSON, UTF_8);

               postMethod.setRequestEntity(requestEntity);

               Boolean responseStatus = executeMethod(postMethod);

               if (!responseStatus)
               {
                  throw new Exception("Failed to request a page check from siteimprove with id:  " + siteId);
               }

            }
            catch (Exception e)
            {
               logger.error(e);
            }
         }
      });
   }

   /**
    * Request a page check from siteimprove.
    *
    * @param siteId Id of our site that the page lives on
    * @param pageURL The live URL of a page, must pre-exist from a site crawl on
    *           siteimprove's side. Otherwise do a new site crawl.
    * @param credentials The sitename/token allowing us to access the siteimprove
    *           api.
    * @throws Exception We failed to request a page on siteimprove or the
    *            supplied page url was empty or null.
    */
   @Override
   public void updatePageInfo(final String siteId, final String pageURL, final Map<String, String> credentials)
         throws Exception
   {

      if (pageURL == null || pageURL.isEmpty())
      {
         throw new NullPointerException("pageUrl cannot be null or empty.");
      }

      pool.submit(new Runnable()
      {
         @Override
         public void run()
         {
            try
            {
               int retries = 0;
               while (retries < 4)
               {
                  registerSslProtocol();

                  PostMethod postMethod = new PostMethod(NEW_SITEIMPROVE_BASE_URL);

                  JSONObject object = new JSONObject();
                  
                  String finalURL = pageURL;
                  
                  logger.debug("canonicalDist: " + credentials.get("canonicalDist"));
                  logger.debug("siteProtocol: " + credentials.get("siteProtocol"));
                  logger.debug("defaultDocument: " + credentials.get("defaultDocument"));
                  logger.debug("token: " + credentials.get("token"));
                  logger.debug("siteName: " + credentials.get("sitename"));
                  
                  if("sections".equals(credentials.get("canonicalDist")))
                     finalURL = StringUtils.replace(pageURL, credentials.get("defaultDocument"), "");

                  object.accumulate("url", finalURL);
                  object.accumulate(SITEIMPROVE_TOKEN, credentials.get(SITEIMPROVE_TOKEN));
                  object.accumulate("type", SITEIMPROVE_RECHECK_PAGE);

                  logger.debug("JSON Object body:" + object.toString());

                  StringRequestEntity requestEntity = null;

                  requestEntity = new StringRequestEntity(object.toString(), APPLICATION_JSON, UTF_8);

                  postMethod.setRequestEntity(requestEntity);

                  Boolean responseStatus = executeMethod(postMethod);
                  if (responseStatus)
                  {
                     return;
                  }
                  Thread.sleep(3000);
                  retries++;
               }
               throw new Exception("Failed to notify siteimprove to check page with url: " + pageURL
                     + " .  Site id is: " + siteId + " .  Exceeded retry count.");
            }
            catch (Exception e)
            {
               logger.error(e);
            }
         }
      });
   }

   /**
    * Parse the credentials, set up the headers for the request such as auth and
    * accepts JSON. Then send request.
    *
    * @param httpMethod The http method we wish to execute, make sure it has the
    *           URI already.
    * @param credentials The credentials in order to access Siteimprove.
    * @throws Exception The httpclient failed to execute the method.
    */
   private Boolean executeMethod(HttpMethod httpMethod) throws Exception
   {

      HttpClient httpClient = new HttpClient();
      registerSslProtocol();
      Header acceptsHeader = new Header(ACCEPTS, APPLICATION_JSON);
      Header contentType = new Header(CONTENT_TYPE, APPLICATION_JSON);
      httpMethod.addRequestHeader(acceptsHeader);
      httpMethod.addRequestHeader(contentType);

      int statusCode = httpClient.executeMethod(httpMethod);

      return statusCode >= 200 && statusCode <= 300;
   }

   private void registerSslProtocol() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException
   {
      String scheme = "https";
      Protocol baseHttps = Protocol.getProtocol(scheme);
      int defaultPort = HTTPS_PORT;

      ProtocolSocketFactory customFactory = (ProtocolSocketFactory) new TLSSocketFactory();

      Protocol customHttps = new Protocol(scheme, customFactory, defaultPort);
      Protocol.registerProtocol(scheme, customHttps);
   }

   @Override
   public Boolean validateCredentials(Map<String, String> credentials) throws Exception
   {
      if("".equals(credentials.get(SITEIMPROVE_TOKEN)))
         return false;
      else if("".equals(credentials.get("sitename")))
         return false;
      
      if("".equals(credentials.get("siteProtocol")) || null == credentials.get("siteProtocol"))
         credentials.put("siteProtocol", DEFAULT_PROTOCOL); // default protocol to http if empty
      if("".equals(credentials.get("defaultDocument")) || null == credentials.get("defaultDocument"))
         credentials.put("defaultDocument", "index.html");
      if("".equals(credentials.get("canonicalDist")) || null == credentials.get("canonicalDist"))
         credentials.put("canonicalDist", "pages");
      return true;
   }
   
   /**
    * No need to implement as we don't need to retreive site info from backend.  It's all done 
    * from the front end Siteimprove plugin.
    */
   @Override
   public String retrieveSiteInfo(String siteName, Map<String, String> credentials) throws Exception
   {
      throw new NotImplementedException();
   }

   /**
    * No need to implement as we don't need to retreive site info from backend.  It's all done 
    * from the front end Siteimprove plugin.
    */
   @Override
   public String retrievePageInfo(String siteName, String pageURL, Map<String, String> credentials) throws Exception
   {
      throw new NotImplementedException();
   }

}
