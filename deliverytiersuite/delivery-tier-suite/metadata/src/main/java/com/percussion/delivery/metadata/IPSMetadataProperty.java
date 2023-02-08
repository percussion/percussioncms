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

package com.percussion.delivery.metadata;

import java.util.Date;


public interface IPSMetadataProperty {

	/**
	 * @return the name
	 */
	public String getName();

	/**
	 * @param name the name to set
	 */
	public void setName(String name);

	/**
	 * @return the valuetype
	 */
	public VALUETYPE getValuetype();

	/**
	 * Returns the untyped value.
	 * 
	 * @return May be <code>null</code>.
	 */
	public Object getValue();
	
	public Date getDatevalue();
	
	public Double getNumbervalue();
	
	public String getStringvalue();
	
    public void setDatevalue(Date val);
    
    public void setNumbervalue(Double val);
    
    public void setStringvalue(String val);
    
    public void setTextvalue(String val);

	
	
	public enum VALUETYPE {
        DATE, NUMBER, STRING, TEXT
    }

}
