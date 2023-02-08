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
package com.percussion.extensions.general;

import com.percussion.extension.IPSWorkflowAction;

/**
 * This workflow action touches all "active assembly parent" items of the 
 * current item. These items are found by searching the related content table 
 * for parent items, and then searching for the parents of those items, etc.
 * The relationships are in the 'active assembly' category only.
 * <p>
 * The content items which are found are then updated so that the LastModifyDate
 * column contains the current date & time.
 * 
 * @see PSTouchItemsWorkflowAction
 */
public class PSTouchParentItems
   extends PSTouchItemsWorkflowAction
   implements IPSWorkflowAction
{

}
