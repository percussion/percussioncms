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
# This script installs Rhythmyx as a daemon.
#
#****************************************************************************

#
# Get the current user.
#
getUser()
{
   echo " {                                 " > a.awk
   echo "   pos=index(\$1,\"uid=0(root)\"); " >> a.awk
   echo "   if( pos > 0 )                   " >> a.awk
   echo "     printf(\"root\");             " >> a.awk
   echo "   else                            " >> a.awk
   echo "     printf(\"other\");            " >> a.awk
   echo " }                                 " >> a.awk
   id > out.txt
   nawk -f a.awk out.txt
}

#
# Updates the S15RhythmyxD shell ( setting the installation dir) and moves
# it to /etc/rcd.3
#
updateS15RhythmyxD()
{
   if [ -f  ./tmp.awk ] ; then
      rm ./tmp.awk
   fi
   
   path="SERVER_DIR="
   
   if [ -f  /etc/init.d/RhythmyxD ] ; then
      path=`nawk '/^[^#]/ && /SERVER_DIR=/' /etc/init.d/RhythmyxD`
   fi

   echo " {                                                         " > tmp.awk   
   echo "     bAdd=1;                                               " >>tmp.awk
   echo "     n=split( SERVER_DIR, path, \":\" )                    " >>tmp.awk
   echo "     for ( i = 1; i <= n; ++i )                            " >>tmp.awk
   echo "     {                                                     " >>tmp.awk
   echo "        if( path[i] == loc)                                " >>tmp.awk
   echo "           bAdd=0;                                         " >>tmp.awk
   echo "     }                                                     " >>tmp.awk
   echo "     if (bAdd)                                             " >>tmp.awk
   echo "     {                                                     " >>tmp.awk   
   echo "        newPath=\"\"                                       " >>tmp.awk   
   echo "        if (n < 1)                                         " >>tmp.awk
   echo "        {                                                  " >>tmp.awk
   echo "           newPath=loc;                                    " >>tmp.awk
   echo "        }                                                  " >>tmp.awk
   echo "        else                                               " >>tmp.awk
   echo "        {                                                  " >>tmp.awk   
   echo "           newPath=path[1];                                " >>tmp.awk
   echo "           for ( i = 2; i <= n; ++i )                      " >>tmp.awk
   echo "           {                                               " >>tmp.awk
   echo "              if( length (path[i] ) > 0)                   " >>tmp.awk
   echo "                 newPath=newPath \":\" path[i];            " >>tmp.awk
   echo "           }                                               " >>tmp.awk   
   echo "           newPath=newPath \":\" loc                       " >>tmp.awk
   echo "        }                                                  " >>tmp.awk  
   echo "        bWrite=1;                                          " >>tmp.awk
   echo "        len=length(\$0);                                   " >>tmp.awk
   echo "        if( len > 1 )                                      " >>tmp.awk
   echo "        {                                                  " >>tmp.awk
   echo "           pos=match(\$0,\"SERVER_DIR=\");                 " >>tmp.awk
   echo "           if( pos )                                       " >>tmp.awk
   echo "           {                                               " >>tmp.awk
   echo "              if( len = length(\"SERVER_DIR=\") )          " >>tmp.awk
   echo " 	       {                                            " >>tmp.awk
   echo "                 printf(\"%s%s\\\\n\",\$0,newPath);        " >>tmp.awk
   echo " 	          bWrite=0;                                 " >>tmp.awk
   echo " 	       }                                            " >>tmp.awk
   echo "           }                                               " >>tmp.awk
   echo "        }                                                  " >>tmp.awk
   echo "        if( bWrite )                                       " >>tmp.awk
   echo "        {                                                  " >>tmp.awk
   echo "           printf(\"%s\\\\n\",\$0);                        " >>tmp.awk
   echo "        }                                                  " >>tmp.awk
   echo "     }                                                     " >>tmp.awk
   echo " }                                                         " >>tmp.awk
   
   if [ -f  ./tmp.sh ] ; then
      rm ./tmp.sh
   fi
   
   if [ -f ./rxconfig/Installer/S15RhythmyxD ] ; then
      nawk -v loc=$loc -v $path -f tmp.awk ./rxconfig/Installer/S15RhythmyxD > tmp.sh
      rm tmp.awk
   else
      echo "Failed to find the file "$loc"/rxconfig/Installer/S15RhythmyxD"
      echo "Please reinstall the file "$loc"/rxconfig/Installer/S15RhythmyxD and try again."
      exit 1
   fi
   
   if [ -s tmp.sh ]; then
      # Copy the tmp.sh to RhythmyxD and remove it
      if [ -f RhythmyxD ] ; then
         rm RhythmyxD
      fi
      
      cp tmp.sh RhythmyxD
      rm tmp.sh
      
      # Copy the script to /etc/init.d and change the attributes and owner
      if [ -f  /etc/init.d/RhythmyxD ] ; then
         rm /etc/init.d/RhythmyxD
      fi

      mv RhythmyxD /etc/init.d
      chmod 0744 /etc/init.d/RhythmyxD
      chown root:sys /etc/init.d/RhythmyxD

      # Now make the links to the proper dirs
      if [ -h /etc/rc2.d/S15RhythmyxD ] ; then
         rm /etc/rc2.d/S15RhythmyxD
      fi

      if [ -h /etc/rc2.d/K15RhythmyxD ] ; then
         rm /etc/rc2.d/K15RhythmyxD
      fi

      # Link RhythmyxD to the start/stop scripts
      ln -s /etc/init.d/RhythmyxD /etc/rc2.d/S15RhythmyxD
      ln -s /etc/init.d/RhythmyxD /etc/rc2.d/K15RhythmyxD
      echo "Installed Rhythmyx Daemon successfully!"
   else
     echo "Rhythmyx Daemon is already installed."
   fi
}

#
# Main
#
loc=`pwd`

USER=`getUser`
rm a.awk
rm out.txt

if [ "$USER" != "root" ]; then
  echo "You must be root to execute this script."
  echo "Aborting!"
  exit 1
fi

if [ ! -d $loc/bin ] ; then
   echo "This script must be executed from the Rhythmyx server root directory."
   echo "Aborting!"
   exit 1
fi

if [ ! -f $loc/rxconfig/Server/objectstore.properties ]; then
   echo "This script must be executed from the Rhythmyx server root directory."
   echo "Aborting!"
   exit 1
fi

updateS15RhythmyxD

rx_user=""

if [ -f  ./rx_user.id ] ; then
   echo " {                                 " > a.awk
   echo "     printf(SYSTEM_USER_ID);       " >> a.awk
   echo " }                                 " >> a.awk
   userid=`nawk '/^[^#]/ && /SYSTEM_USER_ID=/' ./rx_user.id`
   id > out.txt
   rx_user=`nawk -v $userid -f a.awk ./out.txt` 
   rm a.awk
   rm out.txt
fi

echo "Changing File Ownership. Please wait..."
chown -fR $rx_user .




