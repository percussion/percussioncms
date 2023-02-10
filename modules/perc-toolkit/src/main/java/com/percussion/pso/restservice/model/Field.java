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

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 */
@XmlRootElement
@XmlSeeAlso({StringValue.class,XhtmlValue.class,DateValue.class,FileValue.class})
public class Field {
	
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
	@XmlAttribute()
	public String getName() {
		return name;
	}
	
	
	
	
    /**
     * Method getValueAtt.
     * @return String
     */
    @XmlAttribute(name="value")
	public String getValueAtt() {
		return valueAtt;
	}
    
    /**
     * Method setValueAtt.
     * @param value String
     */
    public void setValueAtt(String value) {
		this.value=null;
		this.values=null;
		this.valueAtt = value;
	}
    
    /**
     * Method getStringValue.
     * @return String
     */
    @XmlTransient
    public String getStringValue() {
    	if (valueAtt!=null) {
    		return valueAtt;
    	} else if (this.value != null){
    		return this.value.getStringValue();
    	}
    	return null;
    }
    
    /**
     * Method setStringValue.
     * @param value String
     */
    public void setStringValue(String value) {
		if(value == null) {
			this.valueAtt=null;
			this.values=null;
			this.value=null;
			return;
		}
    	if (value.contains("<div class=\"rxbodyfield\">")) {
			Value newVal = new XhtmlValue();
			newVal.setStringValue(value);
			setValue(newVal);
		} else if (value.length()>50){
			setValueAtt(value);
		} else {
			Value newVal = new StringValue();
			newVal.setStringValue(value);
			setValue(newVal);
		}
		
	}
    
    /**
     * Method setValue.
     * @param value Value
     */
    public void setValue(Value value) {
    	this.valueAtt=null;
		this.values=null;
		this.value = value;
	}
    
    /**
     * Method getValue.
     * @return Value
     */
    @XmlAnyElement
    @XmlElementRefs({
    	@XmlElementRef(type=StringValue.class),
    	@XmlElementRef(type=XhtmlValue.class),
    	@XmlElementRef(type=DateValue.class),
    	@XmlElementRef(type=FileValue.class)
    })
	public Value getValue() {
		return value;
	}
	
	/**
	 * Method setValues.
	 * @param values List<Value>
	 */
	public void setValues(List<Value> values) {
		this.valueAtt=null;
		this.value=null;
		this.values = values;
	}
	/**
	 * Method getValues.
	 * @return List<Value>
	 */
	@XmlElementWrapper(name="Values")
	@XmlAnyElement
    @XmlElementRefs({
    	@XmlElementRef(type=StringValue.class),
    	@XmlElementRef(type=XhtmlValue.class),
    	@XmlElementRef(type=DateValue.class),
    	@XmlElementRef(type=FileValue.class)
    })
	public List<Value> getValues() {
		return values;
	}
	
	/**
	 * Constructor for Field.
	 */
	public Field() {
		super();
	}
	/**
	 * Constructor for Field.
	 * @param name String
	 * @param value String
	 */
	public Field(String name, String value) {
		super();
		this.setName(name);
		this.setStringValue(value);
	}
	
	
	/**
	 * Field name.
	 */
	private String name;
	/**
	 * Field valueAtt.
	 */
	private String valueAtt;
	/**
	 * Field value.
	 */
	private Value value;
	
	/**
	 * Field values.
	 */
	private List<Value> values;
}
