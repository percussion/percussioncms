package com.percussion.cx;

public class JSClipDataItem {

    public String kind = "text";
    public String type;
    public String data;

    public JSClipDataItem(String type, String data)
    {
        this.type = type;
        this.data = data;
    }

    public String getKind()
    {
        return "text";
    }

    public String getAsString()
    {
        return this.data;
    }

    public String getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "ClipDataItem{" +
                "kind='" + kind + '\'' +
                ", type='" + type + '\'' +
                ", data='" + data + '\'' +
                '}';
    }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      JSClipDataItem other = (JSClipDataItem) obj;
      if (type == null)
      {
         if (other.type != null)
            return false;
      }
      else if (!type.equals(other.type))
         return false;
      return true;
   }

    
    
}
