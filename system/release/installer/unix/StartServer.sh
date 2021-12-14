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

#
# Find the tools
#
OS=`uname`
if [ "$OS" = "SunOS" ]; then
    AWK_PROG="/usr/bin/nawk"
    WHOAMI_PROG="/usr/ucb/whoami"
elif [ "$OS" = "Linux" ]; then
    AWK_PROG="awk"
    WHOAMI_PROG="whoami"
else
   # SHRUG!!
    AWK_PROG=awk
    WHOAMI_PROG="whoami"
fi

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
   $AWK_PROG -f a.awk out.txt
}


USER=`getUser`

$WHOAMI_PROG > out.txt
cur_user=`$AWK_PROG '/^[^#]/' ./out.txt`
rm out.txt

rx_user=""

if [ -f  ./rx_user.id ] ; then
   echo " {                                 " > a.awk
   echo "     printf(SYSTEM_USER_ID);       " >> a.awk
   echo " }                                 " >> a.awk
   userid=`$AWK_PROG '/^[^#]/ && /SYSTEM_USER_ID=/' ./rx_user.id`
   id > out.txt
   rx_user=`$AWK_PROG -v $userid -f a.awk ./out.txt`
   rm a.awk
   rm out.txt
fi

SERVER_HOME=`pwd`
export SERVER_HOME

if [ "$USER" = "root" ]; then
    su - $rx_user -c "cd $SERVER_HOME; ./doStart.sh"
else
   ./doStart.sh
fi
