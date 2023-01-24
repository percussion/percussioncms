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

package com.percussion.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement(name = "GuidList")
@Schema(description = "A list of Guids, commonly used for bulk operations")
public class GuidList extends ArrayList<Guid> {
    public GuidList(Collection<? extends Guid> c) {
        super(c);
    }
    public GuidList(){}

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        forEach(guid->sb.append(" ").append(guid.getStringValue()));
        return sb.toString();
    }
}
