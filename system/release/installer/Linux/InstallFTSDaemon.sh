#! /bin/sh
#
#     Percussion CMS
#     Copyright (C) 1999-2020 Percussion Software, Inc.
#
#     This program is free software: you can redistribute it and/or modify
#     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
#
#     This program is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#     GNU Affero General Public License for more details.
#
#     Mailing Address:
#
#      Percussion Software, Inc.
#      PO Box 767
#      Burlington, MA 01803, USA
#      +01-781-438-9900
#      support@percussion.com
#      https://www.percussion.com
#
#     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
#

#****************************************************************************
#
# This script installs Rhythmyx AppServer as a daemon.
#
#****************************************************************************

#
# Get the current user.
#
getUser()
{
   echo "{                                  " > a.awk
   echo "   pos=index(\$1,\"uid=0(root)\"); " >> a.awk
   echo "   if( pos > 0 )                   " >> a.awk
   echo "    printf(\"root\");              " >> a.awk
   echo "   else                            " >> a.awk
   echo "     printf(\"other\")             " >> a.awk
   echo "}                                  " >> a.awk
   id > out.txt
   gawk -f a.awk out.txt

   rm a.awk
   rm out.txt
}

#
# Updates the S15RhythmyxFTSD shell (sets the installation dir)
#
updateS15RhythmyxFTSD()
{
   echo "Updating S15RhythmyxFTSD"
   echo " {                                                         " > tmp.awk
   echo "     bWrite=1;                                             " >>tmp.awk
   echo "     len=length(\$0);                                      " >>tmp.awk
   echo "     if( len > 1 )                                         " >>tmp.awk      
   echo "     {                                                     " >>tmp.awk
   echo "        pos=match(\$0,\"SERVER_DIR=\");                    " >>tmp.awk 
   echo "        if( pos )                                          " >>tmp.awk
   echo "        {                                                  " >>tmp.awk
   echo "            if( len = length(\"SERVER_DIR=\") )            " >>tmp.awk
   echo " 	     {                                              " >>tmp.awk
   echo "               printf(\"%s%s\\n\",\$0,loc);                " >>tmp.awk
   echo " 	        bWrite=0;                                   " >>tmp.awk
   echo " 	     }                                              " >>tmp.awk
   echo "        }                                                  " >>tmp.awk
   echo "     }                                                     " >>tmp.awk
   echo "     if( bWrite )                                          " >>tmp.awk
   echo "     {                                                     " >>tmp.awk
   echo "       printf(\"%s\\n\",\$0);                              " >>tmp.awk
   echo "     }                                                     " >>tmp.awk
   echo " }                                                         " >>tmp.awk


   gawk -v loc=$loc -f tmp.awk S15RhythmyxFTSD > tmp.sh
 
   rm tmp.awk

   if [ -s tmp.sh ]; then 
      # Copy the tmp.sh to RhythmyxFTSD and remove it
      if [ -f RhythmyxFTSD ] ; then 
         rm RhythmyxFTSD
      fi
  
      cp tmp.sh RhythmyxFTSD
      chmod 0744 RhythmyxFTSD
      if [ -s S15RhythmyxFTSD ]; then
	 rm -f S15RhythmyxFTSD
      fi
      rm tmp.sh
   
      # Copy the script to /etc/init.d and change the attributes and owner
      if [ -f  /etc/init.d/RhythmyxFTSD ] ; then
         rm /etc/init.d/RhythmyxFTSD
      fi

      cp RhythmyxFTSD /etc/init.d
      chown root:sys /etc/init.d/RhythmyxFTSD
 
      # Now make the links to the proper dirs
      if [ -h /etc/rc2.d/S15RhythmyxFTSD ] ; then
         rm /etc/rc2.d/S15RhythmyxFTSD
      fi

      if [ -h /etc/rc2.d/K15RhythmyxFTSD ] ; then
         rm /etc/rc2.d/K15RhythmyxFTSD
      fi

      # Link RhythmyxFTSD to the start/stop scripts
      ln -s /etc/init.d/RhythmyxFTSD /etc/rc2.d/S15RhythmyxFTSD
      ln -s /etc/init.d/RhythmyxFTSD /etc/rc2.d/K15RhythmyxFTSD

      #set the run levels at which Rhythmyx Server should start
      /sbin/chkconfig --level 2 RhythmyxFTSD on
      /sbin/chkconfig --level 3 RhythmyxFTSD on
      /sbin/chkconfig --level 4 RhythmyxFTSD on
      /sbin/chkconfig --level 5 RhythmyxFTSD on

      #echo success message
      echo "Installed Rhythmyx FTS Daemon successfully!"
   else
     echo "S15RhythmyxFTS is not located on "$loc " server will not be installed as a service." 
   fi
}

#
# Main
#
loc=`pwd`
curr_user=`getUser`

if [ "$curr_user" != "root" ]; then
  echo "You must be root to execute this script."
  echo "Aborting!"
  exit 1
fi

if [ ! -d $loc/bin ] ; then 
   echo "This script must be executed from the Rhythmyx AppServer root directory."
   echo "Aborting!"
   exit 1
fi

updateS15RhythmyxFTSD

