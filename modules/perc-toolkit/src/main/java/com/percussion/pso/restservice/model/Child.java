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
package com.percussion.pso.restservice.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
/**
 */
@XmlRootElement(name = "Child")
public class Child {
	/**
	 * Field name.
	 */
	private String name;
	/**
	 * Field rows.
	 */
	private List<ChildRow> rows;
	
	/**
	 * Method setName.
	 * @param name String
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Method getName.
	 * @return String
	 */
	@XmlAttribute
	public String getName() {
		return name;
	}

	/**
	 * Method setRows.
	 * @param rows List<ChildRow>
	 */
	public void setRows(List<ChildRow> rows) {
		this.rows = rows;
	}
	/**
	 * Method getRows.
	 * @return List<ChildRow>
	 */
	@XmlElement(name = "Row")
	public List<ChildRow> getRows() {
		return rows;
	}
}
