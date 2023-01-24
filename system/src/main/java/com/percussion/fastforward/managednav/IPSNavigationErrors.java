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
