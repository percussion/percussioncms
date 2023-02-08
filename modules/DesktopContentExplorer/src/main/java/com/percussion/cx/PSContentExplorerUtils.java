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

package com.percussion.cx;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSUserInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;



public class PSContentExplorerUtils
{
   static Logger log = Logger.getLogger(PSContentExplorerUtils.class);
   public static Map<String, String> getQueryMap(String url)  
   {  
      Map<String, String> map = new HashMap<>();
      
      int idx  = url.indexOf("?");
      String query = "";
      if (idx>=0)   
            query  = url.substring(idx+1);
      else
         return map; 
              
       String[] params = query.split("&");  
       for (String param : params)  
       {  
           String name = param.split("=")[0];  
           String value = param.split("=")[1];  
           map.put(name, value);  
       }  
       return map;  
   }  
   
   public static Path download(String sourceUrl,
         String targetDirectory) throws MalformedURLException, IOException
 {
     URL url = new URL(sourceUrl);

     String fileName = url.getFile();

     Path targetPath = new File(targetDirectory + fileName).toPath();

     Files.copy(url.openStream(), targetPath,
             StandardCopyOption.REPLACE_EXISTING);

     return targetPath;
 }
   
   /**
    * Splts the supplied URL string into the url string without parameters and a
    * map of parameters. This will be useful when the url string is too large
    * and hence want to post the data to the server.
    * 
    * @param actionUrl the url to split. This will have a syntax:
    *           <p>
    *           http://<server>:port/Rhythmyx/appName/resource.html?param1=
    *           value1&param2=value2....
    *           <p>
    *           The result would be such that the returned string is
    *           <p>
    *           http://<server>:port/Rhythmyx/appName/resource.html and the
    *           params object which is assumed to empty but not
    *           <code>null</code> will containg the paramN-valueN pairs.
    * @param params An empty Map object to contain the param-value pairs after
    *           splitting. Must not be <code>null</code>. The existing map
    *           values will not be deleted, if it is not empty.
    * @return the url part as explained above. Never <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   public static String splitUrl(String actionUrl, Map params)
   {
      if (StringUtils.isEmpty(actionUrl))
      {
         throw new IllegalArgumentException("actionUrl must not be null or empty");
      }
   
      if (params == null)
      {
         throw new IllegalArgumentException("params must not be null");
      }
   
      try
      {
         // Post should the url decode form. Note this was encoded earlier.
         actionUrl = URLDecoder.decode(actionUrl, "UTF-8");
      }
      catch (UnsupportedEncodingException e)
      {
         // This should never happen
         throw new RuntimeException(e);
      }
      int index = actionUrl.indexOf('?');
      if (index == -1)
         return actionUrl;
      String url = actionUrl.substring(0, index);
      if (actionUrl.length() <= index + 1)
         return url;
   
      String paramString = actionUrl.substring(index + 1);
      StringTokenizer tokenizer = new StringTokenizer(paramString, "&");
      while (tokenizer.hasMoreElements())
      {
         String temp = tokenizer.nextToken();
         index = temp.indexOf('=');
         if (index == -1)
         {
            params.put(temp, "");
            continue;
         }
         String param = temp.substring(0, index);
         String value = "";
         if (temp.length() > index + 1)
            value = temp.substring(index + 1);
         params.put(param, value);
      }
      return url;
   }
   public static void outputUserInfo(PSContentExplorerApplet applet){
      try
      {
         log.debug("checking userinfo");
         PSUserInfo ms_userInfo = new PSUserInfo(applet.getHttpConnection(), applet.getRhythmyxCodeBase());
         log.debug("UserInfo sessionId = " + ms_userInfo.getSessionId());
         log.debug("UserInfo user = " + ms_userInfo.getUserName());
         log.debug("UserInfo locale = " + ms_userInfo.getLocale());

      }
      catch (PSCmsException e)
      {
         e.printStackTrace();
     
         log.error("Error getting userinfo", e);
      }
   }

   static void OutputJaxpImplementationInfo()
   {
      if (PSContentExplorerApplet.log.isDebugEnabled())
      {
         try
         {
            PSContentExplorerApplet.log.debug(PSContentExplorerApplet.getJaxpImplementationInfo("DocumentBuilderFactory", DocumentBuilderFactory.newInstance()
                  .getClass()));
            PSContentExplorerApplet.log.debug(PSContentExplorerApplet.getJaxpImplementationInfo("XPathFactory", XPathFactory.newInstance().getClass()));
            PSContentExplorerApplet.log.debug(PSContentExplorerApplet.getJaxpImplementationInfo("TransformerFactory", TransformerFactory.newInstance().getClass()));
            PSContentExplorerApplet.log.debug(PSContentExplorerApplet.getJaxpImplementationInfo("SAXParserFactory", SAXParserFactory.newInstance().getClass()));
         }
         catch (Exception e)
         {
            PSContentExplorerApplet.log.error("Couldn't print JAXP property",e);
         }
      }
   }
}
