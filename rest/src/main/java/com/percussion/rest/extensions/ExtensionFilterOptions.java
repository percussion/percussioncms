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

package com.percussion.rest.extensions;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ExtensionFilterOptions")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description="Represents options when filtering for extensions. ")
public class ExtensionFilterOptions {
	
	@Schema(name="handlerNamePattern", description="A case-sensitive SQL-like pattern for the extension handler name. An extension will be returned only if itshandler's name matches this pattern. If null, then this criteria is dropped from the search.")
	private String handlerNamePattern;
	@Schema(name="context", description="The context in which to search inside each handler. This is not a pattern -- it is a literal context whose canonicalized version will be used to further narrow down the list of extensions. If null, this criteria will be dropped from the search (will return extensions from all contexts within each handler).")
	private String context;
	@Schema(name="interfacePattern", description="A case-sensitive SQL-like pattern for the interfaces implemented by the extension. Only extensions which implement an interface whose name matches this pattern will be returned. If null, then this criteria is dropped from the search.")
	private String interfacePattern;
	@Schema(name="extensionNamePattern",description="A case-sensitive SQL-like pattern for the name of the extension. Only extensions whose name matches this pattern will be returned. If null, then this criteria will be dropped from the search.")
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
