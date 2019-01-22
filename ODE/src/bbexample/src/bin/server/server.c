#include <stdio.h>

extern void printmsg(char*);
extern void printnl(void);

int main(int argc, char *argv[]) {

   printmsg(0);
   printnl();
   return(0);
}
