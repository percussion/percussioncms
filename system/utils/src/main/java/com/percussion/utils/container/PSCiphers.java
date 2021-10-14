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

package com.percussion.utils.container;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PSCiphers
{
   
   /**
    * The List of ciphers supported by server. Initialized statically and never
    * modified after that.
    */
   private static Vector<String> system_ciphers = new Vector<String>(8);
   
   
   
   private static ArrayList<String> modern_ciphers = new ArrayList<String>();
   private static ArrayList<String> intermediate_ciphers = new ArrayList<String>();
   private static ArrayList<String> modern_combined_ciphers;
   private static ArrayList<String> intermediate_combined_ciphers;
   
   static {
      
      SSLContext context;
      try {
          context = SSLContext.getDefault();
          SSLSocketFactory sf = context.getSocketFactory();
          String[] cipherSuites = sf.getSupportedCipherSuites();
          StringBuilder systemCiphers = new StringBuilder();
          for (String cipher : cipherSuites)
          {
             system_ciphers.add(cipher);
             if (systemCiphers.length()>0)
                systemCiphers.append(",");
             systemCiphers.append(cipher);
             
          }      
          
      } catch (NoSuchAlgorithmException ex) {
         throw new IllegalArgumentException("Error getting cipher list ",ex);
      }

      modern_ciphers.add("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384");
      modern_ciphers.add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
      modern_ciphers.add("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256");
      modern_ciphers.add("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
      modern_ciphers.add("TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256");
      modern_ciphers.add("TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256");
      modern_ciphers.add("TLS_AES_128_GCM_SHA256");
      modern_ciphers.add("TLS_AES_256_GCM_SHA384");
      modern_ciphers.add("TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256");
      modern_ciphers.add("TLS_CHACHA20_POLY1305_SHA256");
      modern_ciphers.add("TLS_DH_RSA_WITH_AES_128_GCM_SHA256");

      intermediate_ciphers.add("TLS_DHE_RSA_WITH_AES_128_GCM_SHA256");
      intermediate_ciphers.add("TLS_DHE_RSA_WITH_AES_256_GCM_SHA384");
      intermediate_ciphers.add("TLS_RSA_WITH_AES_128_GCM_SHA256");
      intermediate_ciphers.add("TLS_RSA_WITH_AES_256_GCM_SHA384");

      
      modern_combined_ciphers = new ArrayList<String>(modern_ciphers);
      modern_combined_ciphers.retainAll(system_ciphers);
      
      intermediate_combined_ciphers = new ArrayList<String>(modern_ciphers);
      intermediate_combined_ciphers.addAll(intermediate_ciphers);
      intermediate_combined_ciphers.retainAll(system_ciphers);
   }
   
   public static List<String> getSystemCiphers() {
      return system_ciphers;
   }
   
   public static List<String> getModernCiphers() {
      return modern_combined_ciphers;
   }
   
   public static List<String> getIntermediateCiphers() {
      return intermediate_combined_ciphers;
   }
}
