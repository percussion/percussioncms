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
      modern_ciphers.add("TLS_DH_RSA_WITH_AES_128_GCM_SHA256");
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
