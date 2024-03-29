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
package com.percussion.pubserver.data;


/**
 * @author ignacioerro
 *
 */
public class PSPublishServerProperty
{
    private static final long serialVersionUID = 1L;
    
    private String key;
    
    private String value;

    /**
     * @return the key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key)
    {
        this.key = key;
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value)
    {
        this.value = value.trim();
    }

    /**
     * @return the serialversionuid
     */
    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }
    
    @Override
    public int hashCode() {
    	final int prime = 31;
    	int result = 50;
    	result = prime * result + ((key == null) ? 0 : key.hashCode());
    	result = prime * result + ((value == null) ? 0 : value.hashCode());
    	return result;
    }

    @Override
    public boolean equals(Object obj) {
    	if (this == obj) {
			return true;
		}
    	if (obj == null) {
			return false;
		}
    	if (getClass() != obj.getClass()) {
			return false;
		}
    	PSPublishServerProperty other = (PSPublishServerProperty) obj;
    	if (key == null) {
    		if (other.key != null) {
				return false;
			}
    	} else {
			if (!key.equals(other.key)) {

				return false;
			}
		}
    	if (value == null) {
			if (other.value != null) {
				return false;
			}
		}
    	 else {
			if (!value.equals(other.value)) {
				return false;
			}
		}
    	return true;
    }
}
