.SUFFIXES: .a .b .c

all: dir1/6.b 88.b ../99.b ../dir2/1.c

dir1/6.a 88.a ../99.a %.a ../dir2/1.a: .NORMTARG
	@echo Working With ${.TARGET}


%.c: %.a
	@echo one
	@echo Converting: $(.ALLSRC) to ${.TARGET}


.a.b:
	@echo two
	@echo Converting: $(.IMPSRC) to ${.TARGET}   

       

