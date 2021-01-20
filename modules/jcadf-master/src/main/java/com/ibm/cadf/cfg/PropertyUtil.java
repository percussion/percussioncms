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
import java.io.InputStream;
import java.util.Properties;

public final class PropertyUtil
{

    public static Properties loadProperties(String fileName) throws IOException
    {
        Properties props = new Properties();
        InputStream is = null;
        try
        {
            is = PropertyUtil.class.getResourceAsStream(fileName);
            props.load(is);
            return props;

        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }

}
