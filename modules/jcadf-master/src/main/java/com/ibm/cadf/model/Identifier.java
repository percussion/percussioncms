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

package com.ibm.cadf.model;

import java.io.Serializable;
import java.util.UUID;

import com.ibm.cadf.cfg.Config;
import com.ibm.cadf.util.Constants;

public class Identifier implements Serializable
{

    private static final long serialVersionUID = 1L;

    
    /**
     * Generate the unique ID
     * 
     * @return String newly generated unique ID
     */
    public static String generateUniqueId()
    {
        UUID uid = UUID.randomUUID();
        String strId = "" + uid;
        return generateUniqueId(strId);
    }

    /**
     * Generate the unique ID and prefix that with given prefix string
     * 
     * @param strId
     * @return String newly generated unique ID
     */

    public static String generateUniqueId(String strId)
    {
        String prefix = Config.getInstance().getProperty("namespace");
        if (prefix != null)
        {
            prefix = prefix + ":";
        }
        else
        {
            prefix = "";
        }
        return prefix + strId;
    }

}
