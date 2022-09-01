package com.percussion.soln.p13n.delivery;

import java.io.IOException;
import java.io.Serializable;


/**
 * 
 * A safe to serialize item of delivery list used in {@link DeliveryResponse}.
 * 
 * @author adamgent
 *
 */
public class DeliveryResponseItem implements Serializable {
    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = -3855702931946438058L;
    private String rendering;
    private String style;
    private double score;
    
    public DeliveryResponseItem() {
        //For serializers
    }
    
    public DeliveryResponseItem(IDeliveryResponseSnippetItem snipItem) {
        try {
            this.setRendering(snipItem.getRendering());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.setStyle(snipItem.getStyle());
        this.setScore(snipItem.getScore());
    }
    public String getStyle() {
        return style;
    }
    
    public void setStyle(String style) {
        this.style = style;
    }
    
    public String getRendering() {
        return rendering;
    }
    
    public void setRendering(String rendering) {
        this.rendering = rendering;
    }
    
    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
    
}
