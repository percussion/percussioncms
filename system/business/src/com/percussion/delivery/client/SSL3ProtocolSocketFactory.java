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
