################################################################
# var_archive_member.mk - makefile to test .ARCHIVE and .MEMBER
#
# run as
#    mk -f var_archive_member.mk
#
# Used by the perl script MkImpVarTest.pm
################################################################
tlib.b(foo.b) : test.c
	@echo test1 archive is ${.ARCHIVE}
	@echo test1 archive using symbol is $!
	@echo test1 member is ${.MEMBER}
	@echo test1 member using symbol is $%

test.c : tlib(foo) tlib.a.b(foo.a.b)

#Testing the use of .ARCHIVE in the dependency line
tlib(foo) : $${.ARCHIVE}.c
	@echo test2 archive is ${.ARCHIVE}
	@echo test2 member is ${.MEMBER}

#Testing the use of .MEMBER in the dependency line
tlib.a.b(foo.a.b) : $${.MEMBER}.c
	@echo test3 archive is ${.ARCHIVE}
	@echo test3 member is ${.MEMBER}

tlib.c:

foo:

foo.b:

foo.a.b:

foo.a.b.c:
