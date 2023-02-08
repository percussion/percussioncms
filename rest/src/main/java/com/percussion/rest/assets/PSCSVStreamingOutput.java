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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.annotation.XmlTransient;

public class PSCSVStreamingOutput implements StreamingOutput{

	protected static final String UTF8BOM = "\uFEFF";
	
	@XmlTransient
	private List<String> rows;
	
	public PSCSVStreamingOutput(List<String> rows){
		this.rows = rows;
	}
	
	@Override
	public void write(OutputStream os) throws IOException, WebApplicationException {
		 try(OutputStreamWriter writer = new OutputStreamWriter(os,"UTF-8")) {
			 writer.write(UTF8BOM);
			 for (String s : rows) {
				 writer.write(s);
			 }
			 writer.flush();
		 }
	}
	
}
