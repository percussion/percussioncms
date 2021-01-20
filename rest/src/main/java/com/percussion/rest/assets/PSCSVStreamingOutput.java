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
		 OutputStreamWriter writer = new OutputStreamWriter(os,"UTF-8");
         writer.write(UTF8BOM);
         for(String s : rows){
       	  writer.write(s);
         }
         writer.flush();
	}
	
}