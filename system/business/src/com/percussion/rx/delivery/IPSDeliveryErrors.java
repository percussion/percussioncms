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
package com.percussion.rx.delivery;

/**
 * Error codes for delivery
 * 
 * @author dougrand
 */
public interface IPSDeliveryErrors
{
   /**
    * The delivery handler failed unexpectedly during abort processing.
    */
   public static final int ABORT_FAILURE = 1;

   /**
    * Cannot create the named directory. 
    * <table>
    * <tr>
    * <td>Argument</td>
    * <td>Description</td>
    * </tr>
    * <tr>
    * <td>0</td>
    * <td>The path of the directory that couldn't be created.</td>
    * </tr>
    * </table>
    */
   public static final int DIR_CANT_CREATE = 2;
   
   
   /**
    * Cannot create the named directory, caused by an exception. 
    * <table>
    * <tr>
    * <td>Argument</td>
    * <td>Description</td>
    * </tr>
    * <tr>
    * <td>0</td>
    * <td>The path of the directory that couldn't be created.</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>The message of the exception that occurred..</td>
    * </tr>
    * </table>
    */
   public static final int CREATE_DIR_W_EXCEPTION = 3;
   
   
   /**
    * Failed to copy temp file to the delivery destination, caused by an 
    * exception. 
    * <table>
    * <tr>
    * <td>Argument</td>
    * <td>Description</td>
    * </tr>
    * <tr>
    * <td>0</td>
    * <td>The path of the source / temp file that is opened for reading.</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>The path of the file that is opened for writing.</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>The message of the exception that occurred.</td>
    * </tr>
    * </table>
    */
   public static final int COPY_FILE_FAILED = 4;
   
   /**
    * Unexpected failure
    * <table>
    * <tr>
    * <td>Argument</td>
    * <td>Description</td>
    * </tr>
    * <tr>
    * <td>0</td>
    * <td>The text of the exception that occurred.</td>
    * </tr>
    * </table>
    */
   public static final int UNEXPECTED_ERROR = 5;
   
   /**
    * Could not write temp file, due to an exception
    * <table>
    * <tr>
    * <td>Argument</td>
    * <td>Description</td>
    * </tr>
    * <tr>
    * <td>0</td>
    * <td>The text of the exception that occurred.</td>
    * </tr>
    * </table>
    */
   public static final int COULD_NOT_WRITE_TEMP = 6;
   
   /**
    * Could not decrypt credentials, due to an exception
    * <table>
    * <tr>
    * <td>Argument</td>
    * <td>Description</td>
    * </tr>
    * <tr>
    * <td>0</td>
    * <td>The text of the exception that occurred.</td>
    * </tr>
    * </table>
    */
   public static final int COULD_NOT_DECRYPT_CREDENTIALS = 7;
   
   /**
    * Could not write temp file, due to an exception
    * <table>
    * <tr>
    * <td>Argument</td>
    * <td>Description</td>
    * </tr>
    * <tr>
    * <td>0</td>
    * <td>The file with path that is to be deleted.</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>The amazon bucket name.</td>
    * </tr>
    * <tr>
    * <td>2</td>
    * <td>The text of the exception that occurred.</td>
    * </tr>
    * </table>
    */
   public static final int COULD_NOT_COPY_TO_AMAMZON = 8;   
   
   /**
    * Could not write temp file, due to an exception
    * <table>
    * <tr>
    * <td>Argument</td>
    * <td>Description</td>
    * </tr>
    * <tr>
    * <td>0</td>
    * <td>The file with path that is to be deleted.</td>
    * </tr>
    * <tr>
    * <td>1</td>
    * <td>The amazon bucket name.</td>
    * </tr>
    * <tr>
    * <td>2</td>
    * <td>The text of the exception that occurred.</td>
    * </tr>
    * </table>
    */
   public static final int COULD_NOT_DELETE_FROM_AMAZON = 9;   
   
   /**
    * Could not process delivery-servers.xml
    * <table>
    * <tr>
    * <td>Argument</td>
    * <td>Description</td>
    * </tr>
    * <tr>
    * <td>0</td>
    * <td>The text of the exception that occurred.</td>
    * </tr>
    * </table>
    */
   public static final int BAD_DELIVERY_SERVER_CONFIGURATION = 10;
   
   /**
    * Error attempting to send to SolrServer
    * <table>
    * <tr>
    * <td>Argument</td>
    * <td>Description</td>
    * </tr>
    * <tr>
    * <td>0</td>
    * <td>The text of the exception that occurred.</td>
    * </tr>
    * </table>
    */
   public static final int SOLR_COMMUNICATION_EXCEPTION = 11;


   public static final int CANNOT_DELIVER_NO_DELIVERYTYPE=12;
}
