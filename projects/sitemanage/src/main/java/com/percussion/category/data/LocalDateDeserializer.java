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

package com.percussion.category.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.percussion.share.service.exception.PSDataServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;

public class LocalDateDeserializer extends JsonDeserializer<LocalDateTime>
{
    private static final Logger log = LogManager.getLogger(LocalDateDeserializer.class);
    public LocalDateTime deserialize(JsonParser arg0, DeserializationContext arg1){
        String dateInStringFormat= "";
        try{
            dateInStringFormat = arg0.getText();
            StringBuilder date = new StringBuilder();
            for(String doubledigit : dateInStringFormat.split("\\.")[0].split(":")){
                if(doubledigit.length()==1){
                    doubledigit = "0"+doubledigit;
                }
                date.append(doubledigit);
                date.append(":");
            }
            dateInStringFormat= date.substring(0, date.length()-1).toString()+"."+dateInStringFormat.split("\\.")[1];
            String time = dateInStringFormat.split("T")[1];
            String hour = time.split(":")[0];
            if(hour.length() == 1){
                dateInStringFormat = dateInStringFormat.replace("T"+hour,"T0"+hour);
            }
            return LocalDateTime.parse(dateInStringFormat);
        }catch (Exception e){
            log.error("Exception occurred while parsing : "+ dateInStringFormat +" : ", new PSDataServiceException(e.getMessage()));
        }
        return null;
    }
}

