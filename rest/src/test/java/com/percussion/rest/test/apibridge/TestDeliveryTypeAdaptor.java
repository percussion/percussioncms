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

package com.percussion.rest.test.apibridge;

import com.percussion.rest.deliverytypes.DeliveryType;
import com.percussion.rest.deliverytypes.IDeliveryTypeAdaptor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;

@Component
public class TestDeliveryTypeAdaptor implements IDeliveryTypeAdaptor  {
    @Override
    public DeliveryType getDeliveryTypeById(URI baseURI, String id) {
        return null;
    }

    @Override
    public DeliveryType updateDeliveryType(URI baseURI, DeliveryType type) {
        return null;
    }

    @Override
    public void deleteDeliveryTypeById(URI baseURI, String id) {

    }

    @Override
    public List<DeliveryType> getDeliveryTypes(URI baseURI) {
        return null;
    }
}
