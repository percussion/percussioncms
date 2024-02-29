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
            context = SSLContext.getInstance("TLS");
            log.debug("Default TLS Provider is: {}" , context.getProvider().getName());
         }
         catch (NoSuchAlgorithmException e)
         {
           log.debug("WARNING!  No TLS Providers are available!");
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
