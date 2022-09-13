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