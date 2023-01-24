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


