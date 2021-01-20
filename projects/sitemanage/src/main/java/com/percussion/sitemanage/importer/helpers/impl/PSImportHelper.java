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
package com.percussion.sitemanage.importer.helpers.impl;

import com.percussion.sitemanage.importer.helpers.IPSImportHelper;

import org.apache.commons.lang.StringUtils;

/**
 * @author LucasPiccoli
 * 
 *         Base class for all helpers. Common behavior should be in this class.
 * 
 */
public abstract class PSImportHelper implements IPSImportHelper
{
    private static String MESSAGE_SEPARATOR = " ";

    private long startTime = 0;
    
    public final static String REGION_CONTENT = "perc-content";
    
    /**
     * All helper subclasses must implement this method to provide a custom
     * status message to show progress to the client during import process. It
     * is recommended to declare a constant STATUS_MESSAGE and return that
     * constant.
     */
    public abstract String getHelperMessage();

    @Override
    public String getStatusMessage(String statusMessagePrefix)
    {
        String statusMessage = new String();
        if (StringUtils.isNotEmpty(statusMessagePrefix))
        {
            statusMessage = statusMessagePrefix + MESSAGE_SEPARATOR;
        }
        statusMessage += getHelperMessage();
        return statusMessage;
    }
    
    public void startTimer()
    {
        startTime = System.nanoTime();
    }
    
    public void endTimer()
    {
        PSHelperPerformanceMonitor.updateStats(this.getClass().getSimpleName(), ((System.nanoTime() - startTime)/ 1000000));
    }

}
