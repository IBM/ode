#####################################################
# links.mk - makefile to test special source .LINKS
#
# run only on UNIX as:
#    touch tst1f
#    ln -s -f tst1f tst1l
#    touch tst1l
#    mk -f links.mk FILEPATH=<path>
#    path : path to tst1f and tst1l
#
# used by the perl script MkSpecSrcTest.pm
#####################################################

tst3l: tst2l .LINKS
	ln -s -f ${FILEPATH}/tst2l ${FILEPATH}/tst3l
tst2l: ${FILEPATH}/tst1f ${FILEPATH}/tst1l .LINKS
	ln -s -f ${FILEPATH}/tst1f ${FILEPATH}/tst2l

