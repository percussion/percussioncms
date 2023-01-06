package com.percussion.cx;

import javafx.scene.input.DataFormat;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base model to represent a DataTransfer object used as the clipboardData propecty
 * of a ClipboardEvent.  This allows us to Create a fake EventObject to pass
 * to the paste event of tinymce and populate from JavaFx Clipboard.   
 * The fields are public and are represented as an array to map to 
 * DataTransfer.items and DataTransfer.types
 * when the Object is mapped to a JSObject.  
 * https://developer.mozilla.org/en-US/docs/Web/API/ClipboardEvent/clipboardData
 * 
 * @author stephenbolton
 *
 */
public class JSClipDataBridge {
    static Logger log = Logger.getLogger(JSClipDataBridge.class);

    public String[] types = new String[0];
    public JSClipDataItem[] items = new JSClipDataItem[0];
    private static volatile boolean isInit = false;
   
    public JSClipDataBridge()
    {
       javafx.scene.input.Clipboard clipboardFx = javafx.scene.input.Clipboard.getSystemClipboard();
       
       log.debug("Current Types in clipboard = "+clipboardFx.getContentTypes());
      
       try {

          if (clipboardFx.hasHtml()) {
             this.setData("text/html",clipboardFx.getHtml());
          }

          if (clipboardFx.hasString()) {
             this.setData("text/plain", clipboardFx.getString());
          }
       } catch (Exception e) {
          log.error("Failed getting clipboard data",e);
       }
       isInit = true;

       
    }
    
    
    public String[] getTypes()
    {
       return types;
    }

    public String getData(String type)
    {
       return Arrays.stream(items)
             .filter(i -> i.getType().equals(type))
             .findFirst().map(JSClipDataItem::getAsString)
             .orElse(null);
    
    }

    public void setData(String type, String value)
    {
     
       if (!type.equals("text/html") && !type.equals("text/plain"))
       {
          log.debug("Unsupported clipboard type "+type);
          return;
       }
 
       JSClipDataItem newItem = new JSClipDataItem(type,value);
       ArrayList<JSClipDataItem> list = Arrays.stream(items)
             .filter(i -> !i.getType().equals(type))
             .collect(Collectors.toCollection(ArrayList<JSClipDataItem>::new));
       list.add(newItem);
       items = list.toArray(new JSClipDataItem[list.size()]);
       ArrayList<String> typesList = Arrays.stream(types).filter(t -> !t.equals(type)).collect(Collectors.toCollection(ArrayList<String>::new));
       typesList.add(type);
       types = typesList.toArray(new String[typesList.size()]);
       if (isInit) setClipboardData(this);
      
    }

    public void clearData()
    {
       items = new JSClipDataItem[0];
       types = new String[0];
       javafx.scene.input.Clipboard clipboardFx = javafx.scene.input.Clipboard.getSystemClipboard();
       clipboardFx.clear();
    }

    
    private void setClipboardData(JSClipDataBridge dataBridge)
    {
     
       javafx.scene.input.Clipboard clipboardFx = javafx.scene.input.Clipboard.getSystemClipboard();
       
       Map<DataFormat,Object> content = new HashMap<>();
       for (JSClipDataItem item : dataBridge.items)
       {
          DataFormat df = DataFormat.lookupMimeType(item.getType());
          if (df == null)
          {
             log.error("Skipping invalid mime type in clipboard data "+item.getType());
          }
          if (df.equals(DataFormat.HTML) || df.equals(DataFormat.PLAIN_TEXT))
          {
             content.put(df,item.getAsString());
          }
          else
          {
             log.debug("Cannot handle mime type in clipboard data "+item.getType());
          }
    }
       boolean success = clipboardFx.setContent(content);
       if (!success)
          log.error("Could not add content to clipboard");

    }
    
    @Override
    public String toString() {
        return "ClipDataBridge{" +
                "items=" + Arrays.toString(items) +
                '}';
    }
}
