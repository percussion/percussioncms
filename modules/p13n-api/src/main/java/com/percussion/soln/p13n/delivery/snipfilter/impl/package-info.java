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
 * Default provided {@link com.percussion.soln.p13n.delivery.IDeliverySnippetFilter snippet filters}.
 * <p>
 * The default pipeline is:
 * <ol>
 * <li>{@link com.percussion.soln.p13n.delivery.snipfilter.impl.BestMatchScoringFilter}</li>
 * <li>{@link com.percussion.soln.p13n.delivery.snipfilter.impl.ScoreRangeFilter}</li>
 * <li>{@link com.percussion.soln.p13n.delivery.snipfilter.impl.SortBasedOnScoreFilter}</li>
 * <li>{@link com.percussion.soln.p13n.delivery.snipfilter.impl.ListSizeFilter}</li>
 * </ol>
 * See {@link com.percussion.soln.p13n.delivery.snipfilter Snippet Filters Developer Guide}.
 * @see com.percussion.soln.p13n.delivery.IDeliverySnippetFilter
 * @author adamgent
 */
package com.percussion.soln.p13n.delivery.snipfilter.impl;