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
package com.percussion.utils.tomcat;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;
import com.percussion.util.FunctionalUtils;
import com.percussion.utils.container.IPSConnector;
import com.percussion.utils.container.PSAbstractConnector;
import com.percussion.utils.testing.UnitTest;
import junit.framework.TestCase;
import org.apache.commons.collections.CollectionUtils;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Test case for the {@link PSTomcatConnector} class
 */
@Category(UnitTest.class)
public class PSTomcatConnectorTest extends TestCase
{
    public static final String DEFAULT_CIPHERS = "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256,TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,TLS_AES_128_GCM_SHA256,TLS_AES_256_GCM_SHA384,TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256,TLS_CHACHA20_POLY1305_SHA256,TLS_DH_RSA_WITH_AES_128_GCM_SHA256,TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,TLS_RSA_WITH_AES_128_GCM_SHA256,TLS_RSA_WITH_AES_256_GCM_SHA384";
   /**
    * Test the constructors and accessors for an http connector
    * 
    * @throws Exception if there are any errors.
    */
   public void testHttpConnector() throws Exception
   {
      int port = 9992;

      IPSConnector tc = new PSAbstractConnector.Builder().setPort(port).build();
      tc.setHostAddress("0.0.0.0");
      doTestHttpConnector(tc, port);
      
      tc = new PSAbstractConnector.Builder().setPort(port).build();
      tc.setHostAddress("0.0.0.0");
      doTestHttpConnector(tc, port);


         tc = new PSAbstractConnector.Builder().setPort(port).build();

      assertTrue(tc.getScheme().equals(PSTomcatConnector.SCHEME_HTTP));
   }
   
   /**
    * Test the constructors and accessors for an https connector
    * 
    * @throws Exception if there are any errors.
    */
   public void testHttpsConnector() throws Exception
   {
      int port = 9980;
      String file = "myFile";
      String pass = "myPass";
      Set<String> ciphers = new HashSet<String>();
      Set<String> sslProtocols = new HashSet<>();
      sslProtocols.addAll( FunctionalUtils.commaStringToStream("TLSv1.2,TLSv1.3").collect(Collectors.toSet()));

      ciphers.addAll(
               FunctionalUtils.commaStringToStream(DEFAULT_CIPHERS).collect(Collectors.toSet()));
      IPSConnector tc = new PSAbstractConnector.HttpsBuilder().setPort(port).setHttps().setKeystoreFile(Paths.get(file)).setKeystorePass(pass).setCiphers(ciphers).setSslProtocols(sslProtocols).build();
      assertEquals(port, tc.getPort());
      assertEquals(tc.SCHEME_HTTPS, tc.getScheme());

      assertEquals(pass, tc.getKeystorePass());
      assertEquals(ciphers,tc.getCiphers() ) ;
      assertEquals(sslProtocols,tc.getSslProtocols());
      assertTrue(file.equalsIgnoreCase(tc.getKeystoreFile().toString()));
      
      int newPort = 9983;
      String newFile = "newFile";
      String newPass = "newPass";
      ciphers.add("cipher1");
      ciphers.add("cipher2");
      
      tc.setPort(newPort);
      assertEquals(newPort, tc.getPort());
      tc.setKeystoreFile(Paths.get(newFile));
      assertEquals(newFile, tc.getKeystoreFile().toString());
      tc.setKeystorePass(newPass);
      assertEquals(newPass, tc.getKeystorePass());
      tc.setCiphers(ciphers);
      assertTrue(CollectionUtils.isEqualCollection(tc.getCiphers(), ciphers));
      

   }
   
   /**
    * Test the XML serialization of the tomcat connector
    * 
    * @throws Exception if there are any errors.
    */
   public void testXml() throws Exception
   {
      int port = 9992;
      PSAbstractConnector tc = PSAbstractConnector.getBuilder().setPort(9992).build();
      DocumentBuilder db =
              PSSecureXMLUtils.getSecuredDocumentBuilderFactory(new PSXmlSecurityOptions(
                              true,
                              true,
                              true,
                              false,
                              true,
                              false
                      ))
                      .newDocumentBuilder();

      Element e = (Element) tc.toXml(tc.getProperties()).getElementsByTagName(PSTomcatConnector.CONNECTOR_NODE_NAME).item(0);

      assertNotNull(e);

      PSAbstractConnector tc2 = PSAbstractConnector.getBuilder().setSource(e).build();
      assertEquals(port, tc2.getPort());
      assertEquals(tc.SCHEME_HTTP, tc2.getScheme());
    

      port = 8443;
      String file = "myFile";
      String pass = "myPass";
      Set<String> ciphers = new HashSet<>();
      ciphers.addAll(
              FunctionalUtils.commaStringToStream(DEFAULT_CIPHERS).collect(Collectors.toSet()));
      tc = PSAbstractConnector.getBuilder().setPort(port).setHttps().setKeystoreFile(Paths.get(file)).setKeystorePass(pass).setCiphers(ciphers).build();
      e = (Element) tc.toXml(tc.getProperties()).getElementsByTagName(PSTomcatConnector.CONNECTOR_NODE_NAME).item(0);
      tc2 = PSAbstractConnector.getBuilder().setSource(e).build();
      assertEquals(port, tc2.getPort());
      assertEquals(PSAbstractConnector.SCHEME_HTTPS, tc2.getScheme());
      assertEquals(file, tc2.getKeystoreFile().getFileName().toString() );
      assertEquals(pass,tc2.getKeystorePass() );
      assertNotNull(tc2.getCiphers());
      assertTrue(CollectionUtils.isEqualCollection(tc2.getCiphers(), ciphers));
      ciphers.clear();
      ciphers.add("cipher1");
      ciphers.add("cipher2");
      tc.setCiphers(ciphers);
      e = (Element) tc.toXml(tc.getProperties()).getElementsByTagName(PSTomcatConnector.CONNECTOR_NODE_NAME).item(0);
      tc2 = PSAbstractConnector.getBuilder().setSource(e).build();
      assertTrue(CollectionUtils.isEqualCollection(ciphers, tc2.getCiphers()));
   }
   
   /**
    * Test the supplied http connector accessors.
    * 
    * @param tc The connector, assumed not <code>null</code>.
    * @param port The connector's expected port.
    * 
    * @throws Exception If there are any errors or failures.
    */
   private void doTestHttpConnector(IPSConnector tc, int port)
      throws Exception
   {
      assertEquals(port, tc.getPort());
      assertEquals(PSTomcatConnector.SCHEME_HTTP, tc.getScheme());
      int newPort = 9993;
      tc.setPort(newPort);
      assertEquals(newPort, tc.getPort());
      assertEquals("0.0.0.0", tc.getHostAddress());
      String address = "Bender";
      tc.setHostAddress(address);
      assertEquals(tc.getHostAddress(), address);
      tc.setHostAddress("");
      assertEquals(tc.getHostAddress(), "");
      tc.setHostAddress(null);
      assertNull(tc.getHostAddress());

   }   
}

