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
package com.percussion.HTTPClient;

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.testing.PSClientTestCase;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;
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
