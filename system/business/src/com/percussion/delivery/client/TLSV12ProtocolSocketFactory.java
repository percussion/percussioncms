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

package com.percussion.delivery.client;

import com.percussion.server.PSServer;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class TLSV12ProtocolSocketFactory implements SecureProtocolSocketFactory
{

   public static final Logger log = LogManager.getLogger(TLSV12ProtocolSocketFactory.class);
   
   private final SecureProtocolSocketFactory base;
   private String enabledCiphers[];
   private String[] defaultCiphers = {
           "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
           "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
           "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
           "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
           "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
           "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
           "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256",
           "TLS_AES_128_GCM_SHA256",
           "TLS_AES_256_GCM_SHA384",
           "TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
           "TLS_CHACHA20_POLY1305_SHA256",
           "TLS_DH_RSA_WITH_AES_128_GCM_SHA256",
           "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
           "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
           "TLS_RSA_WITH_AES_128_GCM_SHA256",
           "TLS_RSA_WITH_AES_256_GCM_SHA384"};
   
   public TLSV12ProtocolSocketFactory(ProtocolSocketFactory base)
   {
      if(!(base instanceof SecureProtocolSocketFactory)) throw new IllegalArgumentException();
    
      this.base = (SecureProtocolSocketFactory) base;
      String ciphers = PSServer.getServerProps().getProperty("enabledCiphers");
      if(StringUtils.isEmpty(ciphers)){
         enabledCiphers = defaultCiphers;
      }

      enabledCiphers = ciphers.split(",");
      for(String s : enabledCiphers){
         s = s.trim();
      }

      
      
   }

   private void logTLSConfig(SSLSocket socket){
      
      if(log.isDebugEnabled()){
         SSLContext context;
         try{
            context = SSLContext.getInstance("TLS","BCJSSE");
            log.debug("Default TLS Provider is: {}" , context.getProvider().getName());
         }
         catch (NoSuchAlgorithmException e)
         {
           log.debug("WARNING!  No TLS Providers are available!");
         }
         catch (NoSuchProviderException e)
         {
             try
            {
               context = SSLContext.getDefault();
            }
            catch (NoSuchAlgorithmException e1)
            {
               log.debug("WARNING!  No TLS Providers are available!");
            }
         }
         
      log.debug("--- Enabled Protocols ---");
      for(String s : socket.getEnabledProtocols()){
       log.debug("Protocol: {} :ENABLED",s);
    }
      log.debug("--- Enabled Cipher Suites ---");
      for(String s : socket.getEnabledCipherSuites()){
         log.debug("Cipher: {} :ENABLED",s);
      }
    
      log.debug("--- Supported Cipher Suites ---");
      for(String s : socket.getSupportedCipherSuites()){
         log.debug("Cipher: {} :Supported",s);
      }
      
      }
   
   }
   
   private Socket acceptOnlyTLS12(Socket socket)
   {
      if(!(socket instanceof SSLSocket)) return socket;
      SSLSocket sslSocket = (SSLSocket) socket;
      
      sslSocket.setEnabledProtocols(new String[]{"TLSv1.2" });
      sslSocket.setEnabledCipherSuites(enabledCiphers);
      log.debug("Setting Enabled Ciphers to: {}" ,StringUtils.join(enabledCiphers,","));
      logTLSConfig(sslSocket);
      return sslSocket;
   }

   @Override
   public Socket createSocket(String host, int port) throws IOException
   {
      return acceptOnlyTLS12(base.createSocket(host, port));
   }
   @Override
   public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException
   {
      return acceptOnlyTLS12(base.createSocket(host, port, localAddress, localPort));
   }
   @Override
   public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException
   {
      return acceptOnlyTLS12(base.createSocket(host, port, localAddress, localPort, params));
   }
   @Override
   public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException
   {
      return acceptOnlyTLS12(base.createSocket(socket, host, port, autoClose));
   }

}
