#### Testing malformed conditional with exists
.PATH:${TMPFILE} ${LOGFILE}
FILE=tempfile

test1:
.if exists(temp.txt)
	@echo FAIL
.elif (exists(tempfile.txt && ${MACHINE}.bldlog))
	@echo PASS
.else
	@echo FAIL
.endif

