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

package com.percussion.fastforward.managednav;

public interface IPSNavigationErrors {

    int NAVIGATION_SERVICE_CATEGORY=18000; // through 18250
    int NAVIGATION_SERVICE_FOLDER_ID_NOT_FOUND_FOR_PATH=NAVIGATION_SERVICE_CATEGORY+1;
    int NAVIGATION_SERVICE_CANT_FIND_RELATED_FOLDER_FOR_NAVON=NAVIGATION_SERVICE_CATEGORY+2;
    int NAVIGATION_SERVICE_FAILED_TO_MOVE_SOURCE_NAVON_TO_TARGET=NAVIGATION_SERVICE_CATEGORY+3;
    int NAVIGATION_SERVICE_FAILED_TO_MOVE_SECTION_BECAUSE_TARGET_ALREADY_HAS_ITEM=NAVIGATION_SERVICE_CATEGORY+4;
    int NAVIGATION_SERVICE_NAVTREE_CANNOT_BE_ADDED_TO_FOLDER_WITH_NAVON=NAVIGATION_SERVICE_CATEGORY+5;
    int NAVIGATION_SERVICE_NAVTREE_CANNOT_BE_ADDED_TO_FOLDER_WITH_NAVTREE=NAVIGATION_SERVICE_CATEGORY+6;
    int NAVIGATION_SERVICE_ERROR_ADDING_NAVTREE_TO_FOLDER=NAVIGATION_SERVICE_CATEGORY+7;
    int NAVIGATION_SERVICE_CANNOT_FIND_ANY_NAVONS=NAVIGATION_SERVICE_CATEGORY+8;
    int NAVIGATION_SERVICE_CANNOT_FIND_NAVTREE_FOR_SITE=NAVIGATION_SERVICE_CATEGORY+9;
}
