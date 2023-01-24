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


package com.percussion.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Constants representing the IDs of components.
 */
public interface IPSBrandCodeConstants
{
   public static final int REPOSITORY = 1;
   public static final int SERVER = 2;
   public static final int PUBLISHER = 4;
   public static final int DEVELOPMENT_TOOLS = 8;
   public static final int DATABASE_PUBLISHER = 16;
   public static final int BEA_ACCELERATOR = 32;
   public static final int MULTI_SERVER_MANANGER = 64;
   public static final int CONTENT_CONNECTOR = 128;
   public static final int WORD = 256;
   public static final int INLINE_EDITING = 512;
   public static final int SPRINTA = 1024;
   public static final int APPLICATION_SERVER = 2048;
   public static final int WEB_SERVICES_LISTENER = 4096;
   public static final int DOCUMENT_ASSEMBLER = 8192;
   public static final int WEBSPHERE_ACCELERATOR = 16384;
   public static final int CONVERA_SEARCH = 32768;
   public static final int EKTRON_WEP_XML = 65536;
   public static final int EKTRON_WEBIMAGEFX = 131072;

   
   /**
    * Represents types of servers for which codes can be generated.
    */
   public enum ServerTypes
   {
      /**
       * Development server
       */
      DEVELOPMENT(0, "Development"),
      
      /**
       * Production server
       */
      PRODUCTION(1, "Production"),
      
      /**
       * Test server
       */
      TEST(2, "Test"),
      
      /**
       * Failover server
       */
      FAIL0VER(3, "Fail Over"),
      
      /**
       * Disaster Recovery server
       */
      DISATER_RECOVERY(4, "Disaster Recovery"),
      
      /**
       * Publishing Hub
       */
      PUBLISHING_HUB(5, "Publishing Hub");
      
      /**
       * Constructor
       * 
       * @param value The integer representation of this type.  
       * 
       * @param displayName The display name, assumed not <code>null</code> or 
       * empty.
       */
      private ServerTypes(int value, String displayName)
      {
         mi_value = value;
         mi_name = displayName;
      }
      
      /**
       * Get the integer value of this server type
       * 
       * @return The value
       */
      public int getValue()
      {
         return mi_value;
      }
      
      /**
       * Returns the display name of this type
       * 
       * @return The name, never <code>null</code> or empty.
       */
      @Override
      public String toString()
      {
         return mi_name;
      }
      
      /**
       * Determine if this type is an extended server type.  These are types
       * that are not available for codes that don't support extended product
       * info.
       * 
       * @return <code>true</code> if it is extended, <code>false</code>
       * otherwise.
       */
      public boolean isExtendedServerType()
      {
         return mi_value > PRODUCTION.mi_value;
      }
      
      /**
       * Lookup enum value by ordinal. Ordinals should be unique. If they are
       * not unique, then the first enum value with a matching ordinal is
       * returned.
       * 
       * @param s The enum value
       * 
       * @return an enumerated value, never <code>null</code>.
       *  
       * @throws IllegalArgumentException if the value does not match
       */
      public static ServerTypes valueOf(int s) throws IllegalArgumentException
      {
         ServerTypes types[] = values();
         for (int i = 0; i < types.length; i++)
         {
            if (types[i].getValue() == s)
               return types[i];
         }
         throw new IllegalArgumentException("No match for value: " + s);
      }      
      
      /**
       * Gets list of server types sorted on display name ascending 
       * case-insensitive.
       * 
       * @return The list, never <code>null</code> or empty.
       */
      public static List<ServerTypes> getSortedValues()
      {
         List<ServerTypes> sortedTypes = new ArrayList<ServerTypes>();
         for (ServerTypes serverTypes : values())
            sortedTypes.add(serverTypes);
         
         Collections.sort(sortedTypes, new Comparator<ServerTypes>() {

            public int compare(ServerTypes t1, ServerTypes t2)
            {
               return t1.mi_name.toLowerCase().compareTo(
                  t2.mi_name.toLowerCase());
            }});
         
         return sortedTypes;
      }
      

      /**
       * The value supplied during construction, immutable.
       */
      private int mi_value;
      
      /**
       * The display name supplied during construction, immutable.
       */
      private String mi_name;
   }
   
   
   /**
    * Represents types of evals for which codes can be generated.
    */
   public enum EvalTypes
   {
      /**
       * Development server
       */
      NOT_EVAL(0, "Non-Eval"),
      
      /**
       * Production server
       */
      M30_DAY(1, "30 Day Eval"),
      
      /**
       * Test server
       */
      M60_DAY(2, "60 Day Eval"),
      
      /**
       * Failover server
       */
      M90_DAY(3, "90 Day Eval"),
      
      /**
       * Disaster Recovery server
       */
      TERM(4, "Term License");

      
      /**
       * Constructor
       * 
       * @param value The integer representation of this type.  
       * 
       * @param displayName The display name, assumed not <code>null</code> or 
       * empty.
       */
      private EvalTypes(int value, String displayName)
      {
         mi_value = value;
         mi_name = displayName;
      }
      
      /**
       * Get the integer value of this eval type
       * 
       * @return The value
       */
      public int getValue()
      {
         return mi_value;
      }
      
      /**
       * Returns the display name of this type
       * 
       * @return The name, never <code>null</code> or empty.
       */
      @Override
      public String toString()
      {
         return mi_name;
      }
      
      /**
       * Determine if this type is an extended server type.  These are types
       * that are not available for codes that don't support extended product
       * info.
       * 
       * @return <code>true</code> if it is extended, <code>false</code>
       * otherwise.
       */
      public boolean isExtendedEvalType()
      {
         return mi_value > M90_DAY.mi_value;
      }
      
      /**
       * Lookup enum value by ordinal. Ordinals should be unique. If they are
       * not unique, then the first enum value with a matching ordinal is
       * returned.
       * 
       * @param s The enum value
       * 
       * @return an enumerated value, never <code>null</code>.
       *  
       * @throws IllegalArgumentException if the value does not match
       */
      public static EvalTypes valueOf(int s) throws IllegalArgumentException
      {
         EvalTypes types[] = values();
         for (int i = 0; i < types.length; i++)
         {
            if (types[i].getValue() == s)
               return types[i];
         }
         throw new IllegalArgumentException("No match for value: " + s);
      }      
      
      /**
       * The value supplied during construction, immutable.
       */
      private int mi_value;
      
      /**
       * The display name supplied during construction, immutable.
       */
      private String mi_name;
   }   
}
