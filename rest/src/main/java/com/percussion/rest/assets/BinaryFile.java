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

package com.percussion.rest.assets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;

@XmlRootElement(name = "BinaryFile")
@JsonInclude(Include.NON_NULL)
@ApiModel(value="BinaryFile",description="Represents a binary file.")
public class BinaryFile
{
	private String filename;
	private String extension;
	private long size;
	private String type;

	/**
	 * @return the fileName
	 */
	public String getFilename()
	{
		return filename;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFilename(String fileName)
	{
		this.filename = fileName;
	}

	/**
	 * @return the extension
	 */
	public String getExtension()
	{
		return extension;
	}

	/**
	 * @param extension the extension to set
	 */
	public void setExtension(String extension)
	{
		this.extension = extension;
	}

	/**
	 * @return the size
	 */
	public long getSize()
	{
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(long size)
	{
		this.size = size;
	}

	/**
	 * @return the type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type)
	{
		this.type = type;
	}
}
