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
package com.percussion.relationship.effect;

import com.percussion.relationship.IPSExecutionContext;

/**
 * This effect is used to touch the items that are the dependents of a folder
 * relationship. This effect is meant to be run in the contexts of
 * {@link IPSExecutionContext#RS_PRE_CONSTRUCTION},
 * {@link IPSExecutionContext#RS_PRE_DESTRUCTION} and
 * {@link IPSExecutionContext#RS_PRE_UPDATE}.
 * <p>
 * It will only touch the dependents who are in public or
 * quick-edit state. It "touches" the last modified date for the dependents 
 * and their Active Assembly relationship parents, so that they will be picked 
 * up by the next incremental publishing. For folder dependents, it touches
 * all item descendants of the folders, but not the folder themselves.
 * <p> 
 * @deprecated Please use {@link PSTouchItemsFolderEffect}
 */
public class PSTouchParentFolderEffect extends PSTouchItemsFolderEffect
{
   
}


