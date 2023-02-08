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

package com.percussion.services.utils.jexl;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase;

/**
 * Common Jexl/Velocity utility methods.
 */
public class PSJexlUtils {

    /**
     * Get the JEXL utilities / tools, which is loaded from the tools.xml.
     * @return the map of the tools. It is <code>null</code> if failed to
     *    load the tools.
     */
    public static Map<String, Object> getToolsMap()
    {
        if (ms_toolsMap == null)
        {
            PSServiceJexlEvaluatorBase jexlBase = new PSServiceJexlEvaluatorBase(
                    false);
            try
            {
                ms_toolsMap = jexlBase.getVelocityToolBindings();
            }
            catch (Exception e)
            {
                ms_toolsMap = null;
                log.error("Failed to load Velocity Tools", e);
            }
        }
        return ms_toolsMap;
    }

    /**
     * The Velocity Tools, initialized by
     * {@link #getToolsMap(IPSTaskResult)}, never <code>null</code> after that.
     */
    private static Map<String,Object> ms_toolsMap = null;

    private static final Logger log = LogManager.getLogger(PSJexlUtils.class.getName());

}
