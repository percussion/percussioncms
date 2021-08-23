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

package com.percussion.rest.extensions;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ExtensionFilterOptions")
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description="Represents options when filtering for extensions. ")
public class ExtensionFilterOptions {
	
	@ApiModelProperty(value="handlerNamePattern", notes="A case-sensitive SQL-like pattern for the extension handler name. An extension will be returned only if itshandler's name matches this pattern. If null, then this criteria is dropped from the search.")
	private String handlerNamePattern;
	@ApiModelProperty(value="context", notes="The context in which to search inside each handler. This is not a pattern -- it is a literal context whose canonicalized version will be used to further narrow down the list of extensions. If null, this criteria will be dropped from the search (will return extensions from all contexts within each handler).")
	private String context;
	@ApiModelProperty(value="interfacePattern", notes="A case-sensitive SQL-like pattern for the interfaces implemented by the extension. Only extensions which implement an interface whose name matches this pattern will be returned. If null, then this criteria is dropped from the search.")
	private String interfacePattern;
	@ApiModelProperty(value="extensionNamePattern",notes="A case-sensitive SQL-like pattern for the name of the extension. Only extensions whose name matches this pattern will be returned. If null, then this criteria will be dropped from the search.")
	private String extensionNamePattern;

	public ExtensionFilterOptions(){}

	public String getHandlerNamePattern() {
		return handlerNamePattern;
	}
	public void setHandlerNamePattern(String handlerNamePattern) {
		this.handlerNamePattern = handlerNamePattern;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public String getInterfacePattern() {
		return interfacePattern;
	}
	public void setInterfacePattern(String interfacePattern) {
		this.interfacePattern = interfacePattern;
	}
	public String getExtensionNamePattern() {
		return extensionNamePattern;
	}
	public void setExtensionNamePattern(String extensionNamePattern) {
		this.extensionNamePattern = extensionNamePattern;
	}

	
	
}
