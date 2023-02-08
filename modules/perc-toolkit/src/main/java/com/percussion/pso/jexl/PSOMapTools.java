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
package com.percussion.pso.jexl;

import static java.util.Arrays.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;

public class PSOMapTools extends PSJexlUtilBase {
    

    

    @IPSJexlMethod(description = "creates a map from a default map with a custom map overlayed", params =
    {
          @IPSJexlParam(name = "defaultOptions", description = "the map to be overlayed"),
          @IPSJexlParam(name = "customOptions", description = "the map to overlay on top of the previous argument")
    }, returns = "an overlayed map")
    public Map<String,Object> overlay(Map<String,Object> defaultOptions, Map<String,Object> customOptions) {
        Map<String,Object> m = new HashMap<String, Object>();
        if (defaultOptions != null) {
            m.putAll(defaultOptions);
        }
        if (customOptions != null) {
            m.putAll(customOptions);
        }
        return m;
    }
    
    public Object get(Map<String,Object> m, String key, Object d) {
        Object rvalue = m.get(key);
        return rvalue == null ? d : rvalue;
    }
    
    public Object getFirstDefined(Map<String,Object> m, List<String> keys, Object d) {
        for(String k : keys) {
            Object rvalue = m.get(k);
            if (rvalue != null){ return rvalue;}
        }
        return d;
    }
    
    public Object getFirstDefined(Map<String,Object> m, String keys, Object d) {
        return getFirstDefined(m, asList(keys.split(",")), d);
    }
    
    
    @IPSJexlMethod(description = "loads properties from a properties file.", params =
    {
          @IPSJexlParam(name = "file", description = "location of properties file.")
    }, returns = "java.util.properties")
    public Properties loadPropertiesFile(String file) throws FileNotFoundException, IOException {
        File f = new File(file);
        log.debug("Trying to load properties file: " + f.toURI().toString());
        Properties prop = new Properties();
        prop.load(new FileInputStream(f));
        return prop;
    }
    
    @IPSJexlMethod(description = "creates a map from a list of keys and list of values", params =
    {
          @IPSJexlParam(name = "keys", description = "list of strings"),
          @IPSJexlParam(name = "values", description = "list of objects")
    }, returns = "map")
    public Map<String,Object> create(List<String> keys, List<? extends Object> values) {
        if (keys == null){ throw new IllegalArgumentException("Keys cannot be null");}
        if (values == null){ throw new IllegalArgumentException("Values cannot be null");}
        //if (keys.size() < values.size()) throw new IllegalArgumentException("There cannot be more keys then values");
        Map<String, Object> m = new HashMap<String, Object>();
        for(int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            Object v = i >= values.size() ? values.get(values.size() - 1) : values.get(i); 
            m.put(k, v);
        }
        return m;
    }
    
    //@IPSJexlMethod(description = "creates a map from a default map with a custom map overlayed", params = {}, returns = "a new map.")
    public Map<String,Object> create() {
        return new HashMap<String, Object>();
    }
    
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */

    private static final Logger log = LogManager.getLogger(PSOMapTools.class);
}
