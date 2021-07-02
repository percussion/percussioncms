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
