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


import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;

import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

/**
 * Implements a socket factory supporting TLSv1.2 and TLSv1.2
 * 
 * @author natechadwick
 *
 */
public class TLSProtocolSocketFactory extends SSLProtocolSocketFactory{

   private final static String defaultProtocols = "TLSv1.1,TLSv1.2";
   private final String[] protocols;
   
   public TLSProtocolSocketFactory() {
      super(); 
      String protocols = System.getProperty("https.protocols", defaultProtocols);
      this.protocols = protocols.split(",");
     }

   /**
    * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
    */
     @Override public Socket createSocket(String host, int port) throws IOException ,UnknownHostException 
     {
        SSLSocket sslSocket = (SSLSocket)super.createSocket(host, port);
        sslSocket.setEnabledProtocols(protocols); 
        return sslSocket;
     }
    
     
     @Override public Socket createSocket(String host, int port, java.net.InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException ,UnknownHostException ,org.apache.commons.httpclient.ConnectTimeoutException {
        SSLSocket sslSocket = (SSLSocket)super.createSocket(host, port, localAddress, localPort, params);
        sslSocket.setEnabledProtocols(protocols);
       return sslSocket;
     }
     
   @Override
   public Socket createSocket(String host, int port, java.net.InetAddress clientHost, int clientPort) throws IOException ,UnknownHostException {
      SSLSocket sslSocket = (SSLSocket)super.createSocket(host,port,clientHost,clientPort);
      sslSocket.setEnabledProtocols(protocols);
      return sslSocket;
   }
   
    /* (non-Javadoc)
    * @see org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory#createSocket(java.net.Socket, java.lang.String, int, boolean)
    */
   @Override
   public Socket createSocket(Socket socket, String host, int port,
         boolean autoClose) throws IOException, UnknownHostException
   {
      SSLSocket sslSocket = (SSLSocket)super.createSocket(socket, host, port, autoClose);
      sslSocket.setEnabledProtocols(protocols);
      return sslSocket;
   }
      
}
