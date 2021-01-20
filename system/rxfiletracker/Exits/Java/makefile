#
# Makefile for the designer component of E2. It specifies all the packages.
#
# $Id: makefile 1.2 2001/06/14 00:08:23Z markdandrea Release $
#
# Version Label   : $Name: Pre_CEEditorUI RX_40_REL 20010618_3_5 $
#
# Locked By       : $Locker: $
#
# Revision History:
#
#                   $Log: makefile $
#                   Revision 1.2  2001/06/14 00:08:23Z  markdandrea
#                   Was pointing to ..\builtins.mak should be ..\..\..\
#                   
#                   Revision 1.1  2001/06/13 17:48:35Z  RammohanVangapalli
#                   Initial revision
#                   Revision 1.1  2001/01/23 18:29:31Z  markdandrea
#                   Initial revision
#

JARFILENAME = rxftexits.jar
VERSIONFILENAME = com$(DIRSEP)percussion$(DIRSEP)filetracker$(DIRSEP)Version.properties

PROPERTY_FILES	= \
	com$(DIRSEP)percussion$(DIRSEP)filetracker$(DIRSEP)*.properties

IMAGE_FILES = \
	com$(DIRSEP)percussion$(DIRSEP)filetracker$(DIRSEP)images$(DIRSEP)*.gif 

SOURCE_RELEASE = com$(DIRSEP)percussion$(DIRSEP)filetracker

PACKAGEDIRS = $(SOURCE_RELEASE)

IMAGEDIRS = $(SOURCE_RELEASE)$(DIRSEP)images

PACKAGES = $(PACKAGEDIRS:s~$(DIRSEP)~.~)

IMAGES = $(IMAGEDIRS:s~$(DIRSEP)~.~)

PACKAGEFILES    = $(SOURCE_RELEASE:+"$(DIRSEP)*.java") 

.IF $(OS) == unix
   DIRSEP= /
.ELSE
   DIRSEP= \ 
.END

.INCLUDE : ..$(DIRSEP)..$(DIRSEP)..$(DIRSEP)builtins.mak
