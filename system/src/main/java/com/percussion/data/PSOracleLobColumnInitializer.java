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

package com.percussion.data;

/**
 * The PSOracleLobColumnInitializer class is used to determing what text
 * needs to be placed on the right side of an INSERT or UPDATE statement when
 * initializing Clob and Blob columns instead of a placeholder.
 */
public class PSOracleLobColumnInitializer implements IPSLobColumnInitializer
{

   private PSOracleLobColumnInitializer(){
      //Hide constructor
   }

   /* <*** IPSLobColumnInitializer interface methods **> */

   /**
    * Get the Lob Initializer for the specified Clob value.  This will
    * be <code>empty_clob()</code> when the clob is to be set to a specified
    * value and <code>null</code> when the clob is to be set to null.
    *
    * @param   clobVal the object to be placed in the clob column
    *                  <I>not used by this implementation</I>
    *
    * @param   isNull  is the object considered to be null?
    *
    * @return          the initializer for clobs for this DBMS,
    *                  never <code>null</code>
    */
   public String getClobInitializer(Object clobVal, boolean isNull)
   {
      if (isNull)
      {
         return "null";
      } else
      {
         return "empty_clob()";
      }
   }

   /**
    * Get the Lob Initializer for the specified Blob value.  This will
    * be <code>empty_blob()</code> when the blob is to be set to a specified
    * value and <code>null</code> when the blob is to be set to null.
    *
    * @param   blobVal the object to be placed in the blob column
    *                  <I>not used by this implementation</I>
    *
    * @param   isNull  is the object considered to be null?
    *
    * @return          the initializer for blobs for this DBMS,
    *                  never <code>null</code>
    */
   public String getBlobInitializer(Object blobVal, boolean isNull)
   {
      if (isNull)
      {
         return "null";
      } else
      {
         return "empty_blob()";
      }
   }

   /**
    *  Get the single instance of this initializer which will be used
    *  for all Lob-based columns for Oracle.
    *
    *  @return The IPSLobColumnInitializer instance for Oracle.
    */
   public static synchronized PSOracleLobColumnInitializer getInstance()
   {
      if (ms_instance == null)
         ms_instance = new PSOracleLobColumnInitializer();

      return ms_instance;
   }

   /*
    *  The single instance of the oracle lob column initializer.
    *  This will be initialized lazily (created when first requested).
    */
   private static PSOracleLobColumnInitializer ms_instance;
}



