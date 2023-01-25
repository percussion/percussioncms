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

@XmlRootElement(name = "Flash")
@JsonInclude(Include.NON_NULL)
@Schema(description="Represents a binary Flash file.")
public class Flash extends ImageInfo
{
	private String flashVersion;
	private String usage;
	/**
	 * @return the flashVersion
	 */
	public String getFlashVersion()
	{
		return flashVersion;
	}
	/**
	 * @param flashVersion the flashVersion to set
	 */
	public void setFlashVersion(String flashVersion)
	{
		this.flashVersion = flashVersion;
	}
	/**
	 * @return the usage
	 */
	public String getUsage()
	{
		return usage;
	}
	/**
	 * @param usage the usage to set
	 */
	public void setUsage(String usage)
	{
		this.usage = usage;
	}
}
