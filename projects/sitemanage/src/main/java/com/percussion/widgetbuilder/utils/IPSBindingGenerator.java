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
package com.percussion.widgetbuilder.utils;

import com.percussion.widgetbuilder.data.PSWidgetBuilderFieldData;

/**
 * @author JaySeletz
 *
 */
public interface IPSBindingGenerator
{

    /**
     * Determine if this generator is valid for the supplied field
     * 
     * @param field The field to check, not <code>null</code>.
     * 
     * @return <code>true</code> if it is accepted, <code>false</code> if not.
     */
    boolean accept(PSWidgetBuilderFieldData field);

    /**
     * Generate the binding for the supplied field.
     * 
     * @param field Not <code>null</code>, {@link #accept(PSWidgetBuilderFieldData)} must be <code>true</code>.
     * 
     * @return The binding, not <code>null</code>.
     */
    String generateBinding(PSWidgetBuilderFieldData field);

}
