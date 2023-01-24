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
 * The IPSLobColumnInitializer interface must be utilized by any classes
 * which require LOB initializer instead of placeholders in LOB based update
 * and insert statements.
 *
 * An example of replacement strings from Oracle are:<B>
 *    <code>empty_clob()</code>      for Clob initialization, and<B>
 *    <code>empty_blob()</code>      for Blob initialization
 *<B>
 * UPDATE mytable set myblobcol = ?, myclobcol = ?, mydata = ? where mykey = ?
 *<B>
 * Would become:<B>
 * UPDATE mytable set myblobcol = empty_blob(), myclobcol = empty_clob(), 
 * mydata = ? where mykey = ?
 *<B>
 * when the statement were generated at runtime to update the lob columns
 * to a non-null value.
 */
public interface IPSLobColumnInitializer
{
   /**
    * Get the Lob Initializer for the specified Clob value.  This is
    *    the text that will be put in place of the placeholders for
    *    initial insert and update statements.
    *
    * @param   clobVal the object to be placed in the clob column
    *
    * @param   isNull  is the object considered to be null?
    *
    * @return          the initializer for clobs for this DBMS or 
    *                  <code>null</code> to use a placeholder 
    *                  <I> Some DBMS' may allow the value to be sent
    *                  directly in the future... allowing null to 
    *                  be returned will facilitate this</I>
    */
   public String getClobInitializer(Object clobVal, boolean isNull);

   /**
    * Get the Lob Initializer for the specified Blob value.  This is
    *    the text that will be put in place of the placeholders for
    *    initial insert and update statements.
    *
    * @param   blobVal the object to be placed in the blob column
    *
    * @param   isNull  is the object considered to be null?
    *
    * @return          the initializer for blobs for this DBMS or 
    *                  <code>null</code> to use a placeholder
    *                  <I> Some DBMS' may allow the value to be sent
    *                  directly in the future... allowing null to 
    *                  be returned will facilitate this</I>
    */
   public String getBlobInitializer(Object blobVal, boolean isNull);
}


