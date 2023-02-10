Segmentation Solution README and Installation
_____________________________________________

TODO: automate installation

**THIS VERSION REQUIRES Percussion CMS 8.1 or later** 

This package allows you to segment (categorize) your content using
folders and the content editor Checkbox Tree control.

The package installs a Segmentation service that allows you to
query the segmentation tree programmatically. At this time the Rhythmyx implementation
of this service does not allow you to add segments so you will have to do this 
through othermeans (Content Editor or Web Services).

The package also installs four exits that are needed to use the checkbox tree
control with segmentation. Refer to the provided Extensions.xml for documentation
on those exits.


Installation
============
 
Install Segment Components
--------------------------
#. Install the ``soln.segment.ppkg`` package.

#. Create Segment Folder Tree.
	
   In the Content Explorer under the *Folders* folder add a folder called Segments (//Folders/Segments)

#. Restart Rhythmyx so that changes are loaded.

Add Segment capabilities to content types
-----------------------------------------
Open the Content Type Editor for content types you want to segment:

#) **Add** a *segment* field.

   This can be accomplished by adding a *shared* or a *local* field.
   
   #) Add the *shared* field.
      
      **Add** shared field ``soln_segment`` under the shared group ``soln_segmentation``. 
   
   #) Add a *local* field
      
      You may ignore this step if you have added a shared field described in previous step.
      
      #) **Add** a Checkbox Tree Field (the control is ``sys_CheckBoxTree``). You can name the field whatever you like. While the field is selected:

      #) Under *All Properties... -> Control-Properties (ellipsis next to control dropdown) -> Control* tab **set** ``tree_src_url`` **to** ``../soln_segment_xml/tree.xml``.

      #) In the same window (*Control-Properties*) under the *Choices* tab **Click** on the ellipsis next to *Retrieve from xml application URL:* 

         #) **Set** *Application name* **to**  ``soln_segment_xml``

         #) **Set** *Resource name* **to** ``lookup``

         #) Under *Params* add a parameter with *Param name* set to ``sys_contentid`` and with a value of "PSXContentItemStatus/CONTENTSTATUS.CONTENTID"   (you have to enter it by selecting from the dropdowns, you cannot paste this value). 

#) Under *Properties->Output-Transform* tab **add** the extension ``soln_segment_Select_ext`` to the content type.

   In *Extension-Parameters* **set** ``fieldName`` **to** the name of the checkbox tree field and **set** ``selectAll`` **to** ``false``

#) Under *Properties->Post-Processing* tab **add** the extension ``soln_segment_Assign_ext`` to the content type.

   In *Extension-Parameters* **set** ``fieldName`` **to** the name of the checkbox tree field
