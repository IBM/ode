############################################################
# path_error1.mk - makefile to test special target .PATH
#                  Should produce an error as testfile1.txt
#                  cannot be searched using .PATH.c 
#
# run as:
#   mk -f path_error1.mk FILEPATH=<path>
#     path: path to testfile1.txt
#
# used by the perl script MkSpecTgtTest.pm
############################################################

.SUFFIXES : .c
.PATH.c : ${FILEPATH}

test: testfile1.txt
	@echo testing

.ERROR:
	@echo file not found
