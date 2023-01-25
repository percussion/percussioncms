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
