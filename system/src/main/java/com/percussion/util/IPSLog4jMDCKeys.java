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
package com.percussion.util;


/**
 * Defines MDC keys.
 * Custom keys can also be supplied in the log4j config, ie:
 * <pre>
 *   props.setProperty("log4j.appender.xml.layout",
 *     "org.apache.log4j.xml.PSMDCXmlLayout");
 *   props.setProperty("log4j.appender.xml.layout.MDCKeys",
 *     "publicationId,publisherId,siteId,editionId,clistId,ctypeId,contentId");
 * </pre>
 *
 * It is best if all the keys are defined here rather than in the props file,
 * so that there is no inconsistency. Keys MUST start with MDC_KEY_ and
 * be further grouped by short sub-system names if necessary.
 * The actual values are automatically introspected, so there is no need
 * to modify anything else.
 */
public interface IPSLog4jMDCKeys
{
   public final static String MDC_KEY_VERSION = "version";
   public final static String MDC_KEY_DATE = "date";

   public final static String MDC_KEY_PUB_PUBLISHER_ID = "publisherId";
   public final static String MDC_KEY_PUB_PUBLICATION_ID = "publicationId";
   public final static String MDC_KEY_PUB_SITE_ID = "siteId";
   public final static String MDC_KEY_PUB_EDITION_ID = "editionId";
   public final static String MDC_KEY_PUB_CLIST_ID = "clistId";
   public final static String MDC_KEY_PUB_CONTENT_TYPE_ID = "ctypeId";
   public final static String MDC_KEY_PUB_CONTENT_ID = "contentId";
   public final static String MDC_KEY_PUB_ACTION = "action";
   public final static String MDC_KEY_PUB_STATUS = "status";
   public final static String MDC_KEY_PUB_ITEMS_Inserted = "inserted";
   public final static String MDC_KEY_PUB_ITEMS_Updated = "updated";
   public final static String MDC_KEY_PUB_ITEMS_Skipped = "skipped";
   public final static String MDC_KEY_PUB_ITEMS_Failed = "failed";
   public final static String MDC_KEY_PUB_ITEMS_Unpublished = "unpublished";

}
