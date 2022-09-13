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