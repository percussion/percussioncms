 {
     bAdd=1;
     n=split( SERVER_DIR, path, ":" )
     for ( i = 1; i <= n; ++i )
     {
        if( path[i] == loc)
           bAdd=0;
     }
     if (bAdd)
     {
        newPath=""
        if (n < 1)
        {
           newPath=loc;
        }
        else
        {
           newPath=path[1];
           for ( i = 2; i <= n; ++i )
           {
              if( length (path[i] ) > 0)
                 newPath=newPath ":" path[i];
           }
           newPath=newPath ":" loc
        }
        bWrite=1;
        len=length($0);
        if( len > 1 )
        {
           pos=match($0,"SERVER_DIR=");
           if( pos )
           {
              if( len = length("SERVER_DIR=") )
               {
                 printf("%s%s\n",$0,newPath);
                  bWrite=0;
               }
           }
        }
        if( bWrite )
        {
           printf("%s\n",$0);
        }
     }
 }
