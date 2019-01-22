#### Testing malformed runtime conditional with exists
.PATH:${TMPFILE} ${LOGFILE}
FILE=tempfile

test1:
	.rif exists(temp.txt)
	@echo FAIL
	.relif (exists(tempfile.txt && ${MACHINE}.bldlog))
	@echo PASS
	.relse
	@echo FAIL
	.rendif

