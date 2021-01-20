#****************************************************************************
# Makefile for the InlineLinkConverter package.
#
#*****************************************************************************

JARFILENAME = InlineLinkConverter.jar
VERSIONFILENAME = 

PROPERTY_FILES =  

IMAGE_FILES =

SOURCE_INLINE = com$(DIRSEP)percussion$(DIRSEP)inlinelinkconverter

PACKAGEDIRS = $(SOURCE_INLINE) 

PACKAGES = $(PACKAGEDIRS:s~$(DIRSEP)~.~)

PACKAGEFILES = $(SOURCE_INLINE :+"$(DIRSEP)*.java")

.IF $(OS) == unix
  DIRSEP= /
.ELSE
  # make sure that you include a space after the backslash, otherwise it will
  # be interpreted as a continue
  DIRSEP= \ 
.END

.INCLUDE : ..$(DIRSEP)..$(DIRSEP)builtins.mak


