# This makefile tests the .rmkdep realtime command in mk

# Make a dependency file with no flags.
test1:
	.rmkdep .
	@echo ODEMKPASS

# Make a dependency file using valid flags.
test2:
	.rmkdep -top -elxdep .
	@echo ODEMKPASS

# Should be an error, invalid flag for mkdep.
# Should not execute the echo.
test3:
	.rmkdep -badflag .
	@echo ODEMKERROR: Test 3 for .rmkdep failed

# Should be an error, ode flag.
# Should not execute the echo.
test4:
	.rmkdep -verbose .
	@echo ODEMKERROR: Test 4 for .rmkdep failed
