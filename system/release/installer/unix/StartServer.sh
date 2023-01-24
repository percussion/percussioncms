#! /bin/sh
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
