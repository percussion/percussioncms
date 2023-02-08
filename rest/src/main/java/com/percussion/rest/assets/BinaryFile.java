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

package com.percussion.rest.assets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "BinaryFile")
@JsonInclude(Include.NON_NULL)
@Schema(description="Represents a binary file.")
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
