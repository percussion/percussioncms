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

package com.percussion.data;

/**
 * The PSOracleLobColumnInitializer class is used to determing what text 
 * needs to be placed on the right side of an INSERT or UPDATE statement when
 * initializing Clob and Blob columns instead of a placeholder.
 */
public class PSOracleLobColumnInitializer implements IPSLobColumnInitializer
{
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
   static PSOracleLobColumnInitializer getInstance()
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



