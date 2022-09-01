package com.percussion.soln.p13n.delivery.data;


/**
 * 
 * The data object that represents the rendered snippet.
 * This is usually XHTML/Text/XML. {@link DeliveryListItem List items}
 * contain a one to many to this class.
 * 
 * @author adamgent
 *
 */
public class DeliverySnippetItem extends DeliveryItem {

    /**
     * Serial
     */
    private static final long serialVersionUID = 333882841261192640L;
    
    private String rendering;
    
    /**
     * The rendering of the snippet.
     * @return maybe <code>null</code>.
     */
    public String getRendering() {
        return rendering;
    }

    /**
     * See Getter.
     * @param rendering should not be <code>null</code>.
     * @see #getRendering()
     */
    public void setRendering(String rendering) {
        this.rendering = rendering;
    }
}
