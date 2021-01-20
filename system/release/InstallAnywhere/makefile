#
# Makefile for the InstallShield Classes.
#
#

JARFILENAME = rxinstallshield.jar
VERSIONFILENAME = com$(DIRSEP)percussion$(DIRSEP)installshield$(DIRSEP)Version.properties
PROPERTY_FILES	= com$(DIRSEP)percussion$(DIRSEP)installshield$(DIRSEP)Version.properties
IMAGE_FILES =

SOURCE_INSTALLSHIELD = com$(DIRSEP)percussion$(DIRSEP)installshield \
			com$(DIRSEP)percussion$(DIRSEP)installshield$(DIRSEP)product$(DIRSEP)action \
			com$(DIRSEP)percussion$(DIRSEP)installshield$(DIRSEP)product$(DIRSEP)condition \
			com$(DIRSEP)percussion$(DIRSEP)installshield$(DIRSEP)wizard$(DIRSEP)action \
			com$(DIRSEP)percussion$(DIRSEP)installshield$(DIRSEP)wizard$(DIRSEP)condition \
			com$(DIRSEP)percussion$(DIRSEP)installshield$(DIRSEP)wizard$(DIRSEP)panel \
			com$(DIRSEP)percussion$(DIRSEP)installshield$(DIRSEP)wizard$(DIRSEP)sequence


PACKAGEDIRS = $(SOURCE_INSTALLSHIELD)												

PACKAGES = $(PACKAGEDIRS:s~$(DIRSEP)~.~)

PACKAGEFILES = $(SOURCE_INSTALLSHIELD:+"$(DIRSEP)*.java")

.IF $(OS) == unix
  DIRSEP= /
.ELSE
  # make sure that you include a space after the backslash, otherwise it will
  # be interpreted as a continue
  DIRSEP= \ 
.END

.INCLUDE : ..$(DIRSEP)..$(DIRSEP)builtins.mak

