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
package com.percussion.activity.service;

import static org.junit.Assert.assertEquals;

import com.percussion.activity.data.PSContentActivity;
import com.percussion.activity.data.PSEffectiveness;
import com.percussion.activity.data.PSEffectivenessRequest;
import com.percussion.activity.service.IPSContentActivityService.PSUsageEnum;
import com.percussion.activity.service.impl.PSEffectivenessDataHandler;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author peterfrontiero
 */
public class PSEffectivenessDataHandlerTest
{
    @Test
    public void testGetEffectiveness() throws Exception
    {
        PSEffectivenessDataHandler handler = new PSEffectivenessDataHandler();
        handler.setFile("src/test/resources/activity/Effectiveness.xml");
        
        PSEffectivenessRequest request = new PSEffectivenessRequest();
        request.setDurationType("days");
        request.setDuration("5");
        request.setPath("/Sites/");
        request.setUsage(PSUsageEnum.pageviews);
        request.setThreshold(10);
        
        List<PSContentActivity> emptyList = new ArrayList<PSContentActivity>();
        List<PSEffectiveness> eList = handler.getEffectiveness(request, emptyList);
        assertEquals(2, eList.size());
        
        request.setPath("/Sites/MySite.com");
        eList = handler.getEffectiveness(request, emptyList);
        assertEquals(4, eList.size());
    }

}
