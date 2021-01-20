/*
 * Copyright 2016 IBM Corp.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ibm.cadf.cfg;

import java.io.IOException;
import java.util.Properties;

import com.ibm.cadf.exception.CADFException;
import com.ibm.cadf.util.Constants;

public class Config
{
    private Properties properties;

    private static Config config = new Config();

    private Config()
    {
        loadDefaultSettings();
    }

    public static Config getInstance()
    {
        return config;
    }

    public void setProperties(Properties properties)
    {
        // Update the existing files
        if (properties != null && !properties.isEmpty())
        {
            this.properties.putAll(properties);
        }
    }

    private void loadDefaultSettings()
    {
        String confFile = System.getProperty(Constants.API_AUDIT_MAP, Constants.API_AUDIT_MAP_CONF);
        try
        {
            this.properties = PropertyUtil.loadProperties(confFile);
        }
        catch (IOException e)
        {
            throw new CADFException(e);
        }
    }

    public String getProperty(String key)
    {
        if (properties == null)
        {
            return null;
        }
        return properties.getProperty(key);
    }

    public void registerProperty(String key, String value)
    {
        if (properties == null)
        {
            properties = new Properties();
        }
        properties.setProperty(key, value);
    }

}
