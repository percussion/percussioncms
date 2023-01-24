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
        
        List<PSEffectiveness> eList = new ArrayList<>();
                
        Map<String, Object> props = new HashMap<>();
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
