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
package com.percussion.rx.design.impl;

import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;

public class PSDesignModelFactory extends PSBaseServiceLocator implements
        IPSDesignModelFactory

{
    /*
     * (non-Javadoc)
     *
     * @see com.percussion.rx.design.IPSDesignModelFactory#getDesignObjModel(com.percussion.services.catalog.PSTypeEnum)
     */
    public IPSDesignModel getDesignModel(PSTypeEnum type)
    {
        if (type == null)
            throw new IllegalArgumentException("type must not be null");
        IPSDesignModel model = (IPSDesignModel) getBean(DESIGN_BEAN_SUFFIX
                + type.name());
        ((PSDesignModel)model).setType(type);
        return model;
    }

    /**
     * Constant for the design bean suffix.
     */
    private static final String DESIGN_BEAN_SUFFIX = "sys_design_";
}
