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
package com.percussion.services.catalog;


/**
 * Errors in cataloging 
 * 
 * @author dougrand
 *
 */
public interface IPSCatalogErrors
{
   /**
    * Error while enumerating summaries
    */
   public static final int SUMMARY_ERROR = 1;
   
   /**
    * Unknown type
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the type</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_TYPE = 2;

   /**
    * Database error
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the object being saved or loaded</TD></TR>
    * </TABLE>
    */
   public static final int REPOSITORY = 3;

   /**
    * XML error reading an XML representation of an object
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The xml source of the object being loaded</TD></TR>
    * </TABLE>
    */
   public static final int XML = 4;

   /**
    * An io error
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The id of the object being saved or loaded</TD></TR>
    * </TABLE>
    */
   public static final int IO = 5;

   /**
    * Error while serializing an object to XML
    */
   public static final int TOXML = 6;
}
