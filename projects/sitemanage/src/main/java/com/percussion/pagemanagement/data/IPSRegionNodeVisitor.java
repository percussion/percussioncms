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
package com.percussion.pagemanagement.data;

/**
 * Visitor pattern is used here mainly to avoid casting
 * and not to define the order of the visit.
 * 
 * The order of the visit will be defined by different iterators.
 * @author adamgent
 *
 */
public interface IPSRegionNodeVisitor {
    void visit(PSRegionCode regionCode);
    void visit(PSRegion region);
}
