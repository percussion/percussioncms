How-to create a signed CAB file for word_prj.ocx

1. It is important when compiling the OCX point VB to the old OCX file
   this will preserve the CLSID, which is used in sys_Templates.xsl
   
2. Let the gatekeeper know that the OCX has changed and a new CAB file
   has to be created 
   
This is how the CAB file gatekeeper would create a CAB file:

1. Check the version and the CSLID of the OCX file
   Version can be checked by a right click on the OCX file in the Windows explorer
   CLSID can be verified by using the OLE View tool from Visual Studio 6

2. If OCX version got changed it has to be updated in two places:
   a. sys_Templates.xsl
   b. word_prj.inf

3. Execute signocx.bat located under word_prj directory in the Source Control
   
4. Test the newly created cab file by executing the test.html page located under word_prj directory

OCX VERSION RULES
1. Major - 4 for Rhythmyx 4+
2. Minor - see version ranges below:
   E240 from 51 to 500;
   E245 from 501 to 1000
   E2 from 1501 to 2000
3. Revision is any number greater then 0

ie: if in VB project you have Major 4; Minor 51; Revision 1
the VERSION number in the INF file and in the sys_Templates.xsl
file would have to be 4,51,0,1


Additional info:

Sign code download:
http://msdn.microsoft.com/downloads/default.asp?url=/code/sample.asp?url=/msdn-files/027/000/219/msdncompositedoc.xml


SignCode help:
http://msdn.microsoft.com/library/default.asp?url=/workshop/security/authcode/signing.asp#SignCode



