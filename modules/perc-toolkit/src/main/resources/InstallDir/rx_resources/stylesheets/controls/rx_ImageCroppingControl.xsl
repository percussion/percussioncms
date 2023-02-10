<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:psxctl="URN:percussion.com/control" xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="psxi18n" xmlns:psxi18n="urn:www.percussion.com/i18n" >

<xsl:template match="/" />

 <xsl:template match="Control[ParamList/Param[@name='helptext'] and @isReadOnly='no']" priority="25" mode="psxcontrol">
      <!-- override all controls to display helptext, then return control to standard template -->
      
      
      <!-- dispatch to normal control template -->
      <xsl:variable name="control-stripped-of-help">
         <xsl:apply-templates select="." mode="copy-all-but-help-param" />
      </xsl:variable>
      
      <xsl:variable name="control-help" select="concat('help_', @paramName)" /> 
      
      <xsl:apply-templates select="exsl:node-set($control-stripped-of-help)" mode="psxcontrol" />
      
      <!-- display help text -->
      <img src="../rx_resources/images/help.gif" onmouseover="TagToTip('{$control-help}', CLOSEBTN, true, STICKY, true);" onmouseout="UnTip()" />
      <span id="{$control-help}"><xsl:value-of select="ParamList/Param[@name='helptext']"/></span>
   </xsl:template>

   <xsl:template match="@* | node()" mode="copy-all-but-help-param">
      <xsl:copy>
         <xsl:apply-templates select="@* | node()" mode="copy-all-but-help-param" />
      </xsl:copy>
   </xsl:template>

   <!-- suppress the helptext so the dispatch isn't recursive -->
   <xsl:template match="Param[@name='helptext']" mode="copy-all-but-help-param" />

  
  <!-- *************** -->
  <!-- *************** -->
  <!-- *************** -->
  <!-- *************** -->
  <!-- *************** -->
  <!-- *************** -->
  <!-- *************** -->
  <!-- *************** -->
  <!-- *************** -->
  <!-- *************** -->
  <!-- *************** -->
  
  <!-- START rx_ImageCroppingControl -->
  <psxctl:ControlMeta name="rx_ImageCroppingControl" dimension="single" choiceset="none">
    <psxctl:Description>a image cropping control</psxctl:Description>
    <psxctl:ParamList>
      <psxctl:Param name="cropping_attributes" datatype="Number" paramtype="generic">
        <psxctl:Description>The attributes of the image cropping control.</psxctl:Description>
      </psxctl:Param>
    </psxctl:ParamList>
    <psxctl:AssociatedFileList>
      <psxctl:FileDescriptor type="css" name="default" mimetype="text/css">
        <psxctl:FileLocation>../rx_resources/css/jquery.Jcrop.min.css</psxctl:FileLocation>
        <psxctl:Timestamp/>
      </psxctl:FileDescriptor>
      <psxctl:FileDescriptor name="jquery.jCrop.min.js" type="script" mimetype="text/javascript">
        <psxctl:FileLocation>../rx_resources/js/jquery.Jcrop.min.js</psxctl:FileLocation>
        <psxctl:Timestamp/>
      </psxctl:FileDescriptor>  
      <psxctl:FileDescriptor name="jquery.color.js" type="script" mimetype="text/javascript">
        <psxctl:FileLocation>../rx_resources/js/jquery.color.js</psxctl:FileLocation>
        <psxctl:Timestamp/>
      </psxctl:FileDescriptor>
      <psxctl:FileDescriptor name="cropper_custom.js" type="script" mimetype="text/javascript">
        <psxctl:FileLocation>../rx_resources/js/cropper_custom.js</psxctl:FileLocation>
        <psxctl:Timestamp/>
      </psxctl:FileDescriptor>      
    </psxctl:AssociatedFileList>
  </psxctl:ControlMeta>
  <xsl:template match="Control[@name='rx_ImageCroppingControl']" mode="psxcontrol">
    <xsl:variable name="cropping_attributes">
      <xsl:value-of select="ParamList/Param[@name='cropping_attributes']"/>
    </xsl:variable>
    <div class="image_cropper_inputs" data-param-name="{@paramName}" >
      <label for="{@paramName}_x">X Position</label>
      <input type="text" name="{@paramName}_x"></input>
      <label for="{@paramName}_y">Y Position</label>
      <input type="text" name="{@paramName}_y"></input><br/>
      <label for="{@paramName}_w">Width</label>
      <input type="text" name="{@paramName}_w"></input>
      <label for="{@paramName}_h">Height</label>
      <input type="text" name="{@paramName}_h"></input><br/>
      <label for="{@paramName}_resize_w">Resize Width</label>
      <input type="text" name="{@paramName}_resize_w"></input>
      <label for="{@paramName}_resize_h">Resize height</label>
      <input type="text" name="{@paramName}_resize_h"></input><br/>
    </div>
    <div class="image_cropper" data-param-name="{@paramName}" id="{@paramName}">
    
    </div>
    
    <input type="hidden" name="{@paramName}">
      <xsl:attribute name="value">
        <xsl:value-of select="Value"/>
      </xsl:attribute>
    </input>


  </xsl:template>
  <!-- read only template for dropdown single -->
  <xsl:template match="Control[@name='rx_ImageCroppingControl' and @isReadOnly='yes']" priority="11" mode="psxcontrol">
    <xsl:variable name="cropping_attributes">
      <xsl:value-of select="ParamList/Param[@name='cropping_attributes']"/>
    </xsl:variable>
    <div class="warning">Note: Cropping on a read-only display has no effect.</div>
    <div class="image_cropper_inputs" data-param-name="{@paramName}" >
      <label for="{@paramName}_x">X Position</label>
      <input type="text" name="{@paramName}_x"></input>
      <label for="{@paramName}_y">Y Position</label>
      <input type="text" name="{@paramName}_y"></input><br/>
      <label for="{@paramName}_w">Width</label>
      <input type="text" name="{@paramName}_w"></input>
      <label for="{@paramName}_h">Height</label>
      <input type="text" name="{@paramName}_h"></input><br/>
      <label for="{@paramName}_resize_w">Resize Width</label>
      <input type="text" name="{@paramName}_resize_w"></input>
      <label for="{@paramName}_resize_h">Resize height</label>
      <input type="text" name="{@paramName}_resize_h"></input><br/>
    </div>
    <div class="image_cropper" data-param-name="{@paramName}" id="{@paramName}">
    
    </div>
      
    <input type="hidden" name="{@paramName}">
      <xsl:attribute name="value">
        <xsl:value-of select="Value"/>
      </xsl:attribute>
    </input>

  </xsl:template>
  
</xsl:stylesheet>
