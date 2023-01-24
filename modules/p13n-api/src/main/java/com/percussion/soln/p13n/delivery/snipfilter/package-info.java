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

/**
 * 
 * Useful {@link com.percussion.soln.p13n.delivery.IDeliverySnippetFilter} adapters.
 * <p>
 * Snippet filters can be setup in a pipeline.
 * Most pipelines are in the following order:
 * <ol>
 * <li> {@link com.percussion.soln.p13n.delivery.snipfilter.AbstractScoringFilter} </li>
 * <li> {@link com.percussion.soln.p13n.delivery.snipfilter.AbstractSortingFilter} </li>
 * <li> {@link com.percussion.soln.p13n.delivery.snipfilter.AbstractRemovalFilter} </li>
 * <li> {@link com.percussion.soln.p13n.delivery.snipfilter.AbstractStylingFilter} </li>
 * </ol>
 * The {@link com.percussion.soln.p13n.delivery.IDeliverySnippetFilter}s pipeline of filters is specified on the
 * {@link com.percussion.soln.p13n.delivery.data.DeliveryListItem#getSnippetFilterIds()}.
 * 
 * @see com.percussion.soln.p13n.delivery.IDeliverySnippetFilter
 * @author adamgent
 */
package com.percussion.soln.p13n.delivery.snipfilter;