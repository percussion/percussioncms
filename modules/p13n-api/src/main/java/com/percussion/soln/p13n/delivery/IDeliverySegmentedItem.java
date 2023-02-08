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

package com.percussion.soln.p13n.delivery;

import java.util.Collection;

import com.percussion.soln.segment.Segment;

/**
 * A response item that has segments associated to it.
 * @author adamgent
 *
 */
public interface IDeliverySegmentedItem extends IDeliveryResponseItem {
    /**
     * Segments associated to this item.
     * @return never <code>null</code>.
     */
    public Collection<? extends Segment> getSegments();
}
