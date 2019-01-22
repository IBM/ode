####################################################
# colon1blank.mk - makefile to test target dependencies
# that have embedded blanks in filenames, when ":" is used 
#
# run as:
#  mk -f colon1blank.mk
#
# used by the perl script MkTargDepTest.pm
####################################################
"t t.o" : "t 1.c" "t 2.c"
	@echo making "t t.o"
	@echo " " > "t t.o"

"t 1.c" :
	@echo making "t 1.c"
	@echo " " > "t 1.c"
  
"t 2.c" :
	@echo making "t 2.c"
	@echo " " > "t 2.c"
