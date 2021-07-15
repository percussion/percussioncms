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


