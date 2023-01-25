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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

/**
 * Implements a socket factory supporting SSL3
 * 
 * @author natechadwick
 *
 */
public class SSL3ProtocolSocketFactory extends SSLProtocolSocketFactory{

   private final SSLSocketFactory socketfactory;
   private final static String protocols[] = {"SSLv3"};
   
   public SSL3ProtocolSocketFactory(SSLContext sslContext) {
      super(); 
      this.socketfactory = sslContext.getSocketFactory();
     }

   /**
    * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
    */
     @Override public Socket createSocket(String host, int port) throws IOException ,UnknownHostException 
     {
        SSLSocket sslSocket = (SSLSocket) this.socketfactory.createSocket(host, port);
    
        sslSocket.setEnabledProtocols(protocols);
        
        return sslSocket;
     }
    
     
     @Override public Socket createSocket(String host, int port, java.net.InetAddress localAddress, int localPort, org.apache.commons.httpclient.params.HttpConnectionParams params) throws IOException ,UnknownHostException ,org.apache.commons.httpclient.ConnectTimeoutException {
        
        SSLSocket sslSocket = null;
        
        if (params == null) {
           throw new IllegalArgumentException("Parameters may not be null");
        }
      
        if (localPort <= 0)
           localPort = 443;
        
        int timeout = params.getConnectionTimeout();
       
       SocketFactory socketfactory = this.socketfactory;
       
       if (timeout == 0 && localAddress != null) {
          sslSocket = (SSLSocket) socketfactory.createSocket(host, port, localAddress, localPort);
       } else {
           sslSocket = (SSLSocket)socketfactory.createSocket();
           
           SocketAddress localaddr=null;
           
           if(localAddress != null)
              localaddr = new InetSocketAddress(localAddress, localPort);
           
           SocketAddress remoteaddr = new InetSocketAddress(host, port);
           sslSocket.setEnabledProtocols(protocols);
           
           if(localAddress != null)
              sslSocket.bind(localaddr);
           
           sslSocket.connect(remoteaddr, timeout);
       }   
       
       return sslSocket;
     }
     
   @Override
   public Socket createSocket(String host, int port, java.net.InetAddress clientHost, int clientPort) throws IOException ,UnknownHostException {
      SSLSocket sslSocket = null;
     
      SocketFactory socketfactory = this.socketfactory;
     
      sslSocket = (SSLSocket)socketfactory.createSocket();
      SocketAddress localaddr = new InetSocketAddress(clientHost, clientPort);
      SocketAddress remoteaddr = new InetSocketAddress(host, port);
      sslSocket.setEnabledProtocols(protocols);
      sslSocket.bind(localaddr);
      sslSocket.connect(remoteaddr);
        
     
     return sslSocket;
   }
   
    /* (non-Javadoc)
    * @see org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory#createSocket(java.net.Socket, java.lang.String, int, boolean)
    */
   @Override
   public Socket createSocket(Socket socket, String host, int port,
         boolean autoClose) throws IOException, UnknownHostException
   {
      return super.createSocket(socket, host, port, autoClose);
   }
      
}
