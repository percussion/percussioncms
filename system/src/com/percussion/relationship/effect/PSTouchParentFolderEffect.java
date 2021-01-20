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


