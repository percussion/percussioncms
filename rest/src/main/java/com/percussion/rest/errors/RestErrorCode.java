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

package com.percussion.rest.errors;

public enum RestErrorCode
{
	NOT_AUTHORIZED(1)
	, UNKNOWN_USER(2)
	, LOCATION_MISMATCH(3)
	, FOLDER_NOT_FOUND(4)
	, SITE_NOT_FOUND(5)
	, PAGE_NOT_FOUND(6)
	, ASSET_NOT_FOUND(7)
	, INVALID_ASSET_TYPE(8)
	, ASSET_ALREADY_EXISTS(9)
	, TEMPLATE_NOT_FOUND(10)
	, CONTENT_MIGRATION_ERROR(11)
	, USER_DIRECTORYIMPORT_INVALIDNAME(12)
	, USER_DIRECTORYIMPORT_ERROR(14)
	, UNEXPECTED_EXCEPTION(13) //It's just unlucky...
	, USER_UNSUPPORTED_USERTYPE(15)
	, OTHER(99)
	;
	
	private int value;
	
	RestErrorCode(int val)
	{
		this.value = val;
	}
	
	public int getNumVal()
	{
		return this.value;
	}
	
}
