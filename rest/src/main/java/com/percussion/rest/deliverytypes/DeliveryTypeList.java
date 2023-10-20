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

package com.percussion.rest.deliverytypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.Collection;


@XmlRootElement(name = "DeliveryType")
@XmlSeeAlso(DeliveryType.class)
@ArraySchema(schema=@Schema(implementation = DeliveryType.class))
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryTypeList extends ArrayList<DeliveryType> {
    public DeliveryTypeList(Collection<? extends DeliveryType> c) {
        super(c);
    }
    public DeliveryTypeList(){}
}
