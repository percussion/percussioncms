#! /bin/bash
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
   awk -f a.awk out.txt
}


#
# Updates the S15RhythmyxD shell ( setting the installation dir) and moves
# it to /etc/rcd.3
#
updateS15RhythmyxD()
{
   path="SERVER_DIR="
   
   if [ -f  /etc/init.d/PercussionD ] ; then
      path=`awk '/^[^#]/ && /SERVER_DIR=/' /etc/init.d/PercussionD`
   fi

   if [ -f  ./tmp.sh ] ; then
      rm ./tmp.sh
   fi
   
   if [ -f ./rxconfig/Installer/S15RhythmyxD ] ; then
      awk -v loc=$loc -v $path -f InstallDaemon.awk ./rxconfig/Installer/S15RhythmyxD > tmp.sh
   else
      echo "Failed to find the file "$loc"/rxconfig/Installer/S15RhythmyxD"
      echo "Please reinstall the file "$loc"/rxconfig/Installer/S15RhythmyxD and try again."
      exit 1
   fi
   
   if [ -s tmp.sh ]; then
      # Copy the tmp.sh to PercussionD and remove it
      if [ -f PercussionD ] ; then
         rm PercussionD
      fi
      
      cp tmp.sh PercussionD
      rm tmp.sh
      
      # Copy the script to /etc/init.d and change the attributes and owner
      if [ -f  /etc/init.d/PercussionD ] ; then
         rm /etc/init.d/PercussionD
      fi

      mv PercussionD /etc/init.d
      chmod 0744 /etc/init.d/PercussionD
      chown root:sys /etc/init.d/PercussionD

      # Now make the links to the proper dirs
      if [ -h /etc/rc2.d/S15RhythmyxD ] ; then
         rm /etc/rc2.d/S15RhythmyxD
      fi

      if [ -h /etc/rc2.d/K15RhythmyxD ] ; then
         rm /etc/rc2.d/K15RhythmyxD
      fi

      #set the run levels at which Rhythmyx Server should start
      which chkconfig >& /dev/null
      if [ $? -gt 0 ]
      then
         sysv-rc-conf --level 2 PercussionD on
         sysv-rc-conf --level 3 PercussionD on
         sysv-rc-conf --level 4 PercussionD on
         sysv-rc-conf --level 5 PercussionD on
      else
         /sbin/chkconfig --level 2 PercussionD on
         /sbin/chkconfig --level 3 PercussionD on
         /sbin/chkconfig --level 4 PercussionD on
         /sbin/chkconfig --level 5 PercussionD on
      fi

      #echo success message
      echo "Installed Percussion Daemon successfully!"
   else
     echo "Percussion Daemon is already installed."
   fi
}

#
# Main
#
which awk >& /dev/null
if [ $? -gt 0 ]
then
   echo "The awk command is required to run this script."
   echo "Please rerun this script after installing it."
   exit 2
fi

which chkconfig >& /dev/null
hasconf1=$?
which sysv-rc-conf >& /dev/null
hasconf2=$?
if [ $hasconf1 -gt 0 ] && [ $hasconf2 -gt 0 ]
then
   echo "The chkconfig or sysv-rc-conf command is required to run this script."
   echo "Please rerun this script after installing it."
   exit 2
fi


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
   echo "This script must be executed from the Percussion server root directory."
   echo "Aborting!"
   exit 1
fi

if [ ! -f $loc/rxconfig/Server/objectstore.properties ]; then
   echo "This script must be executed from the Percussion server root directory."
   echo "Aborting!"
   exit 1
fi

updateS15RhythmyxD

rx_user=""

if [ -f  ./rx_user.id ] ; then
   echo " {                                 " > a.awk
   echo "     printf(SYSTEM_USER_ID);       " >> a.awk
   echo " }                                 " >> a.awk
   userid=`awk '/^[^#]/ && /SYSTEM_USER_ID=/' ./rx_user.id`
   id > out.txt
   rx_user=`awk -v $userid -f a.awk ./out.txt` 
   rm a.awk
   rm out.txt
fi

echo "Changing File Ownership. Please wait..."
chown -fR $rx_user .




