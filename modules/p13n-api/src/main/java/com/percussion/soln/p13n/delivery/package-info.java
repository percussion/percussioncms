/**
 * 
 * Delivers dynamic and possibly personalized 
 * content to visitors of a web site.
 * <p>
 * <h1>How it works</h1>
 * To delivery dynamic content to a visitor a request is made by the visitor to
 * render a  {@link com.percussion.soln.p13n.delivery.data.DeliveryListItem dynamic list}.
 * The {@link com.percussion.soln.p13n.delivery.DeliveryRequest request} contains the id of the {@link com.percussion.soln.p13n.delivery.data.DeliveryListItem list item} 
 * to render.
 * <p>
 * The system processes the request by loading the {@link com.percussion.soln.p13n.delivery.data.DeliveryListItem list item} and then loads
 * the pipeline of  {@link com.percussion.soln.p13n.delivery.IDeliverySnippetFilter snippet filters} specified by the 
 * {@link com.percussion.soln.p13n.delivery.data.DeliveryListItem#getSnippetFilterIds() list item's list of filter ids}.
 * The pipeline of {@link com.percussion.soln.p13n.delivery.IDeliverySnippetFilter snippet filters} is then executed in the order.
 * <p>
 * A {@link com.percussion.soln.p13n.delivery.IDeliverySnippetFilter snippet filter} can score, remove, decorate and some cases add 
 * {@link com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem snippets}. The {@link com.percussion.soln.p13n.delivery.IDeliverySnippetFilter snippet filter interface} 
 * is the main extension point of the delivery system.
 * 
 * @see com.percussion.soln.p13n.delivery.snipfilter
 * @see com.percussion.soln.p13n.delivery.IDeliveryService
 * @author adamgent
 */
package com.percussion.soln.p13n.delivery;