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
package com.percussion.HTTPClient;

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.testing.PSClientTestCase;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

@Deprecated
@Category(IntegrationTest.class)
@SuppressFBWarnings("INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE")
public class HTTPClientTest extends PSClientTestCase
{
   private static final Logger log = LogManager.getLogger(IPSConstants.TEST_LOG);

   /**
    * Ctor.
    * @param name - test name.
    */
   public HTTPClientTest(String name) {
      super(name);
   }

   public HTTPConnection getConnection()
   {
      Properties props;
      try {
         props = getConnectionProps(CONN_TYPE_TOMCAT);
      } catch (IOException e) {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
      HTTPConnection con =
         new HTTPConnection(props.getProperty(PROP_HOST_NAME),
             Integer.parseInt(props.getProperty(PROP_PORT)));

      con.setAllowUserInteraction(false);
      con.setContext(this);

      AuthorizationInfo authInfo = null;
      // Using empty "Basic realm" for webservices handler, which is
      // consistent with all other handlers in the Rhythmyx Server.
      authInfo =
         new AuthorizationInfo(
            props.getProperty(PROP_HOST_NAME),
            Integer.parseInt(props.getProperty(PROP_PORT)),
            props.getProperty(PROP_SCHEME),
            "",
            Codecs.base64Encode(
               props.getProperty(PROP_LOGIN_ID) +
               ":" + props.getProperty(PROP_LOGIN_PW)));

      authInfo.addPath(m_webdavPath);
      AuthorizationInfo.addAuthorization(authInfo, this);

      NVPair[] defaultHeaders =
      {
         new NVPair("User-Agent", "Rhythmyx Remote Requestor"),
         new NVPair("Content-Type", "text/xml")
      };
      con.setDefaultHeaders(defaultHeaders);

      return con;
   }

   private String m_webdavPath = "/Rhythmyx/rxwebdav/Sites/EnterpriseInvestments/Files";

   @Test
   public void testPROPFIND() throws Exception
   {

      HTTPConnection con = getConnection();
      byte[] data = new byte[0];
      HTTPResponse resp = con.ExtensionMethod("PROPFIND", m_webdavPath, data, null);
      String respData = resp.getText();

      Document doc =
         PSXmlDocumentBuilder.createXmlDocument(resp.getInputStream(), false);

      NodeList nl = doc.getElementsByTagName("D:multistatus");

      assertTrue("Not found expected webdav 'PROPFIND' " +
            "'/Rhythmyx/rxwebdav/Sites/CorporateInvestments/Files' response doesn't have:" +
            " 'D:multistatus' ", nl.getLength() > 0);

      log.info(respData);

   }

   public static void main(String[] args)
   {
      try
      {
         HTTPClientTest test = new HTTPClientTest("HTTPClientTest");
         test.testPROPFIND();
      }
      catch (Exception e)
      {
        log.error(PSExceptionUtils.getMessageForLog(e));
      }
   }
}
