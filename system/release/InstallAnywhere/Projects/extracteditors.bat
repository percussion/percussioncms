set classpath=\%2\build\classes;\%2\Tools\xerces\xercesImpl.jar;\%2\Tools\xerces\xmlParserAPIs.jar;%classpath%

java com.percussion.build.ExtractAllEditorApplicationDefs \%2\build\dist\RxFastForward\Editors \%2\design\dtd\sys_ContentEditorLocalDef.dtd DTD\sys_ContentEditorLocalDef.dtd
java com.percussion.build.PSCETemplateFilesCreator \%2\build\dist\RxFastForward\Editors \%2\design\dtd\sys_ContentEditorLocalDef.dtd  DTD\sys_ContentEditorLocalDef.dtd

copy "%1\%2\cms\content\applications\Editors\rx_ceTemplates\rx_ceTemplates.xml" "%1\%2\cms\content\applications\Editors\sys_psxTemplates\rx_ceTemplates.xml"

rem change the above classes to create rxs_ce files, so that we would be able to deal with these CE templates on the folder level 
rem rather then having to pick each one separetely

rem rmdir /S /Q "%1\%2\build\dist\RxFastForward\Editors\rx_ceTemplates"
rem mkdir "%1\%2\build\dist\RxFastForward\Editors\rx_ceTemplates"

rem copy "%1\%2\build\dist\RxFastForward\Editors\sys_psxTemplates\sys_ArticleWord.xml" "%1\%2\build\dist\RxFastForward\Editors\rx_ceTemplates\rx_ArticleWord.xml"

rem copy "%1\%2\build\dist\RxFastForward\Editors\sys_psxTemplates\sys_Brief.xml" "%1\%2\build\dist\RxFastForward\Editors\rx_ceTemplates\rx_Brief.xml"

rem copy "%1\%2\build\dist\RxFastForward\Editors\sys_psxTemplates\sys_ExternalURL.xml" "%1\%2\build\dist\RxFastForward\Editors\rx_ceTemplates\rx_ExternalURL.xml"

rem copy "%1\%2\build\dist\RxFastForward\Editors\sys_psxTemplates\sys_File.xml" "%1\%2\build\dist\RxFastForward\Editors\rx_ceTemplates\rx_File.xml"

rem copy "%1\%2\build\dist\RxFastForward\Editors\sys_psxTemplates\sys_Image.xml" "%1\%2\build\dist\RxFastForward\Editors\rx_ceTemplates\rx_Image.xml"

rem copy "%1\%2\build\dist\RxFastForward\Editors\sys_psxTemplates\sys_Index.xml" "%1\%2\build\dist\RxFastForward\Editors\rx_ceTemplates\rx_Index.xml"

rem copy "%1\%2\build\dist\RxFastForward\Editors\sys_psxTemplates\sys_IndexAutomated.xml" "%1\%2\build\dist\RxFastForward\Editors\rx_ceTemplates\rx_IndexAutomated.xml"

rem copy "%1\%2\build\dist\RxFastForward\Editors\sys_psxTemplates\sys_Article.xml" "%1\%2\build\dist\RxFastForward\Editors\rx_ceTemplates\rx_Article.xml"

rem copy "%1\%2\build\dist\RxFastForward\Editors\sys_psxTemplates\sys_Page.xml" "%1\%2\build\dist\RxFastForward\Editors\rx_ceTemplates\rx_Page.xml"

