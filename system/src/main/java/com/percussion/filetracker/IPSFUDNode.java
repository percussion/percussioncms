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

package com.percussion.filetracker;

import org.w3c.dom.Element;

/**
 * An interface that defines some common methods required for Tree Node interface
 * in the application. All the nodes in the model must implement this interface
 * directly or indirectly.
 *
 */
public interface IPSFUDNode
{
   /**
    * return String version of the node that is displayed in the treetable
    *
    * @return String representation of the node.
    *
    */
   String toString();

   /**
    * return DOM element of the node
    *
    * @return DOM Element
    *
    */
   Element getElement();

   /**
    * return array of child Nodes for the node
    *
    * @return DOM Element
    *
    */
   Object[] getChildren();

   /**
    * return status code for the node
    *
    * @return int value of the status
    *
    */
   int getStatusCode();

   /**
    * return the status string for the node
    *
    * @return status string as String
    *
    */
   String getStatusText();

   /**
    * returns true if remote file exists
    *
    *  @return true if remote file exists else false
    */
   boolean isRemoteExists();

   /**
    * Element name that stores the status for any node. This is assumed to be
    * the same for all derived nodes too.
    *
    */
   String ELEM_STATUS = "status";

   /**
    * Attribute name of the element ELEM_STATUS that stores the status code for
    * the node. This is assumed to be the same for all derived nodes too.
    *
    */
   String ATTRIB_CODE = "code";

   /**
    * Status code indicating that the remote counter part is missing.
    */
   int STATUS_CODE_ABSENT = -1;

   /**
    * Status code indicating normal status -> remote exists, local does not.
    */
   int STATUS_CODE_NORMAL = 0;

   /**
    * Status code indicating that the remote copy and local copy are in sync.
    */
   int STATUS_CODE_INSYNC = 1;

   /**
    * Status code indicating that the remote copy is newer than the local one.
    */
   int STATUS_CODE_REMOTENEW = 2;

   /**
    * Status code indicating that the local copy is newer thatn the remote one.
    */
   int STATUS_CODE_LOCALNEW = 3;
}


