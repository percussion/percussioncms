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

package com.percussion.delivery.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import com.percussion.server.PSServer;

public class TLSV12ProtocolSocketFactory implements SecureProtocolSocketFactory
{

   public static Log log = LogFactory.getLog(TLSV12ProtocolSocketFactory.class);
   
   private final SecureProtocolSocketFactory base;
   private String enabledCiphers[];
   private String defaultCiphers[] = {
         "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
         "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
         "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
         "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
         "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
         "TLS_RSA_WITH_AES_256_CBC_SHA",
         "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",
         "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
         "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
         "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
         "TLS_RSA_WITH_AES_128_CBC_SHA",
         "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
         "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
         "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
         "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
         "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
         "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
         "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
         "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
         "TLS_ECDHE_RSA_WITH_RC4_128_SHA",
         "SSL_RSA_WITH_RC4_128_SHA",
         "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
         "TLS_ECDH_RSA_WITH_RC4_128_SHA",
         "SSL_RSA_WITH_RC4_128_MD5",
         "TLS_EMPTY_RENEGOTIATION_INFO_SCSV"};
   
   public TLSV12ProtocolSocketFactory(ProtocolSocketFactory base)
   {
      if(base == null || !(base instanceof SecureProtocolSocketFactory)) throw new IllegalArgumentException();
    
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
            log.debug("Default TLS Provider is: " + context.getProvider().getName());
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
         }finally{}
         
      log.debug("--- Enabled Protocols ---");
      for(String s : socket.getEnabledProtocols()){
       log.debug("Protocol: " + s + ":ENABLED");
    }
      log.debug("--- Enabled Cipher Suites ---");
      for(String s : socket.getEnabledCipherSuites()){
         log.debug("Cipher: " + s + ":ENABLED");
      }
    
      log.debug("--- Supported Cipher Suites ---");
      for(String s : socket.getSupportedCipherSuites()){
         log.debug("Cipher: " + s + ":Supported");
      }
      
      }
   
   }
   
   private Socket acceptOnlyTLS12(Socket socket)
   {
      if(!(socket instanceof SSLSocket)) return socket;
      SSLSocket sslSocket = (SSLSocket) socket;
      
      sslSocket.setEnabledProtocols(new String[]{"TLSv1.2" });
      sslSocket.setEnabledCipherSuites(enabledCiphers);
      log.debug("Setting Enabled Ciphers to: " + StringUtils.join(enabledCiphers,","));
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