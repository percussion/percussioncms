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
package com.percussion.proxyconfig.data;

import com.percussion.proxyconfig.service.impl.ProxyConfig;
import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LucasPiccoli
 * 
 *         This class contains proxy configuration information to allow traffic
 *         for certain protocols going through a proxy server.
 * 
 */
public class PSProxyConfig extends PSAbstractDataObject
{

   private static final long serialVersionUID = 1L;

   protected String host;

   protected String port;

   protected String user;
   
   protected String password;

   protected List<String> protocols;

   public PSProxyConfig()
   {
      super();
   }
   
   /**
    * 
    */
   public PSProxyConfig(String host, String port, String user, String password, List<String> protocols)
   {
      super();
      
      this.host = host;
      this.port = port;
      this.user = user;
      this.password = password;

      if (protocols == null)
      {
         this.protocols = new ArrayList<>();
      }
      else
      {
         this.protocols = new ArrayList<>(protocols);
      }
   }
   
   /**
    * 
    * @param proxyConfig
    */
   public PSProxyConfig(ProxyConfig proxyConfig)
   {
      this.host = proxyConfig.getHost();
      if (proxyConfig.getPassword() != null)
         this.password = proxyConfig.getPassword().getValue();
      this.port = proxyConfig.getPort();
      this.user = proxyConfig.getUser();

      if (proxyConfig.getProtocols() == null)
      {
         this.protocols = new ArrayList<>();
      }
      else
      {
         this.protocols = new ArrayList<>();
         for (String protocol : proxyConfig.getProtocols().getProtocols())
         {
            this.protocols.add(protocol);
         }
      }
   }
   
   /**
    * @return the host
    */
   public String getHost()
   {
      return host;
   }

   /**
    * @param host the host to set
    */
   public void setHost(String host)
   {
      this.host = host;
   }

   /**
    * @return the port
    */
   public String getPort()
   {
      return port;
   }

   /**
    * @param port the port to set
    */
   public void setPort(String port)
   {
      this.port = port;
   }

   /**
    * @return the user
    */
   public String getUser()
   {
      return user;
   }

   /**
    * @param user the user to set
    */
   public void setUser(String user)
   {
      this.user = user;
   }

   /**
    * @return the password
    */
   public String getPassword()
   {
      return password;
   }

   /**
    * @param password the password to set
    */
   public void setPassword(String password)
   {
      this.password = password;
   }

   /**
    * @return the protocols
    */
   public List<String> getProtocols()
   {
      return protocols;
   }

   /**
    * @param protocols the protocols to set
    */
   public void setProtocols(ArrayList<String> protocols)
   {
      this.protocols = protocols;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((host == null) ? 0 : host.hashCode());
      result = prime * result + ((port == null) ? 0 : port.hashCode());
      result = prime * result + ((protocols == null) ? 0 : protocols.hashCode());
      result = prime * result + ((user == null) ? 0 : user.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (getClass() != obj.getClass())
         return false;
      PSProxyConfig other = (PSProxyConfig) obj;
      if (host == null)
      {
         if (other.host != null)
            return false;
      }
      else if (!host.equals(other.host))
         return false;
      if (password == null)
      {
         if (other.password != null)
            return false;
      }

      if (port == null)
      {
         if (other.port != null)
            return false;
      }
      else if (!port.equals(other.port))
         return false;
      if (user == null)
      {
         if (other.user != null)
            return false;
      }
      else if (!user.equals(other.user))
         return false;
      if (protocols == null)
      {
         if (other.protocols != null)
            return false;
      }
      else if (protocols.size() != other.protocols.size() || !protocols.containsAll(other.protocols))
         return false;

      return true;
   }
   
   
}
