############################################
# flag_wsi.mk - makefile to test commandline
#               flags -w, -s, -i
#               also tests -, \
#
# run as:
#   mk -f flag_wsi.mk -w -s -i
#
# used by the perl script MkCmdFlagTest.pm
############################################
# there are no tabs before echo statements
all: t1 t2 t3\
t4 t5\
t6
  echo making all

t1:
          echo making t1

t2:
    @echo making t2

t3:
   -crap

t4:
   crap

t5:
     -@echo making t5
t6:
   @-crap
