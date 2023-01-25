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

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;

public class PSDateAdapter extends XmlAdapter<String, LocalDateTime> {

	@Override
	public String marshal(LocalDateTime date) throws Exception {

		if(date != null) {
			return date.toString();
		}

		return null;
	}

	@Override
	public LocalDateTime unmarshal(String date) throws Exception {
        try {
            return LocalDateTime.parse(date);
        }catch(Exception e){
            ;
        }
        return null;
    }
}
