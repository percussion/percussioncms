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
package com.percussion.activity.service.impl;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.activity.data.PSContentActivity;
import com.percussion.activity.data.PSEffectiveness;
import com.percussion.activity.data.PSEffectivenessRequest;
import com.percussion.activity.service.IPSEffectivenessService;
import com.percussion.share.service.impl.PSXmlDataHandler;
import com.percussion.share.service.impl.jaxb.Pair;
import com.percussion.share.service.impl.jaxb.Property;
import com.percussion.share.service.impl.jaxb.Response;
import com.percussion.share.service.impl.jaxb.Result;
import com.percussion.share.service.impl.jaxb.Property.Pvalues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This handler which provides sample effectiveness data from an xml file.
 * 
 * @author peterfrontiero
 */
public class PSEffectivenessDataHandler extends PSXmlDataHandler implements IPSEffectivenessService
{
    @SuppressWarnings("unused")
    public List<PSEffectiveness> getEffectiveness(PSEffectivenessRequest request, List<PSContentActivity> activity)
    {
        notNull(request);
        notNull(activity);
        
        List<PSEffectiveness> eList = new ArrayList<PSEffectiveness>();
                
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("duration", request.getDuration());
        props.put("durationType", request.getDurationType());
        props.put("path", request.getPath());
        props.put("usage", request.getUsage().name());
        props.put("threshold", String.valueOf(request.getThreshold()));
        
        Response response = getData(props);
        if (response != null)
        {                
            List<Result> results = response.getResult();
            if (!results.isEmpty())
            {
                Result result = results.get(0);
                List<Property> propList = result.getProperty();
                if (!propList.isEmpty())
                {
                    Property prop = propList.get(0);
                    Pvalues pvalues = prop.getPvalues();
                    if (pvalues != null)
                    {
                        List<Pair> pairList = pvalues.getPair();
                        for (Pair pair : pairList)
                        {
                            eList.add(new PSEffectiveness(pair.getValue1(), Long.valueOf(pair.getValue2())));
                        }
                    }
                }
            }
        }
               
        return eList;
    }

}
