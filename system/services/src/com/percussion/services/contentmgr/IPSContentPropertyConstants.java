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
package com.percussion.services.contentmgr;

import com.percussion.cms.objectstore.PSComponentSummary;

/**
 * Common node property names defined here. Inclusion here is because it is
 * either a special property for which there is no corresponding field in the
 * system definition, or because it requires special handling due to the 
 * mapping from {@link PSComponentSummary}.
 * 
 * @author dougrand
 */
public interface IPSContentPropertyConstants
{
   /**
    * The property name for the path (or a path) of a content item
    */
   static final String JCR_PATH = "jcr:path";
   
   /**
    * The synthetic property that is set for nodes which are checked out
    */
   static final String JCR_IS_CHECKEDOUT = "jcr:isCheckedOut";

   /**
    * The property name for the revision id
    */
   static final String RX_SYS_REVISION = "rx:sys_revision";

   /**
    * The property name for the content id
    */
   static final String RX_SYS_CONTENTID = "rx:sys_contentid";
   
   /**
    * The property name for the content type id
    */
   static final String RX_SYS_CONTENTTYPEID = "rx:sys_contenttypeid";
   
   /**
    * The last modified date for the content
    */
   static final String RX_SYS_CONTENTLASTMODIFIEDATE = "rx:sys_contentlastmodifieddate";
   
   /**
    * Who modified the content last
    */
   static final String RX_SYS_CONTENTLASTMODIFIER = "rx:sys_contentlastmodifier";
   
   /**
    * The creation date
    */
   static final String RX_SYS_CONTENTCREATEDDATE = "rx:sys_contentcreateddate";
   
   /**
    * The author
    */
   static final String RX_SYS_CONTENTCREATEDBY = "rx:sys_contentcreatedby";
   
   /**
    * The post date of the content
    */
   static final String RX_SYS_CONTENTPOSTDATE = "rx:sys_contentpostdate";
    
   
   /**
    * The property name for the folder parent of a content item
    */
   static final String RX_SYS_FOLDERID = "rx:sys_folderid";

   /**
    * The community id, included here because it is an integer field.
    */
   static final String RX_SYS_COMMUNITYID = "rx:sys_communityid";

   /**
    * The object type, included here because it is an integer field.
    */
   static final String RX_SYS_OBJECTTYPE = "rx:sys_objecttype";
   
   /**
    * The workflow state id, included here because it is an integer field.
    */
   static final String RX_SYS_CONTENTSTATEID = "rx:sys_contentstateid";
   
   /**
    * The workflow id, included here because it is an integer field.
    */
   static final String RX_SYS_WORKFLOWID = "rx:sys_workflowid";   
}
