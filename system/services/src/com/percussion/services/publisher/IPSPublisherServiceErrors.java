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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.publisher;

/**
 * Publisher service error codes
 * 
 * @author dougrand
 */
public interface IPSPublisherServiceErrors
{
   /**
    * Missing content list by name.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The name of the content list</TD></TR>
    * </TABLE>
    */
   public static final int LIST_MISSING = 10;
   /**
    * Invalid query.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The query</TD></TR>
    * </TABLE>
    */
   public static final int BAD_QUERY = 11;
   /**
    * Repository error.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The repository problem</TD></TR>
    * </TABLE>
    */
   public static final int REPOSITORY = 12;
   
   /**
    * Couldn't load the given site.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The site guid</TD></TR>
    * </TABLE>
    */
   public static final int SITE_LOAD = 13;
   
   /**
    * Couldn't load the given extension.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The extension name</TD></TR>
    * <TR><TD>1</TD><TD>The extension context</TD></TR>
    * <TR><TD>1</TD><TD>The extension interface</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_EXTENSION = 14;
   
   /**
    * A problem occurred while looking up an extension.
    */
   public static final int EXTENSION_LOOKUP = 15;
   
   /**
    * A problem occurred while retrieving query rows from the generator.
    */
   public static final int ROW_RETRIEVAL = 16;
   
   /**
    * An unknown database problem while retrieving data.
    */
   public static final int DB = 17;
   
   /**
    * The filter failed to function properly.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The filter name</TD></TR>
    * </TABLE>
    */
   public static final int FILTER_MALFUNCTION = 18;
   
   /**
    * An unanticipated problem occurred.
    * <p>
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The original exception message</TD></TR>
    * </TABLE>
    */
   public static final int RUNTIME_ERROR = 19;
   
   /**
    * Did not find a given publisher.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The publisher id</TD></TR>
    * </TABLE>
    */   
   public static final int MISSING_PUBLISHER = 20;
   
   /**
    * Missing site override information - no arguments
    */
   public static final int SITE_MISSING = 21;

   /**
    * Missing context override information - no arguments
    */
   public static final int CONTEXT_MISSING = 22;
   
   /**
    * An unexpected problem occurred
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The problem description</TD></TR>
    * </TABLE>
    */
   public static final int UNEXPECTED = 23;
}
