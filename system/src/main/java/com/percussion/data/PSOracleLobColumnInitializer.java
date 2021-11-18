/*[ PSOracleLobColumnInitializer.java ]****************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

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



