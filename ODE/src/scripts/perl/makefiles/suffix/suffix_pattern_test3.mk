.SUFFIXES: .x .a .b .c

all: 6.b dir1/99.b dir1/17.b 88.b


6.c 6.x dir2/99.a dir2/17.a dir3/17.a dir1/88.a:
	@echo Working With ${.TARGET}


.c.b:
	@echo one
	@echo Converting: $(.IMPSRC) to ${.TARGET}


<dir2>.a|<dir1>.b: 
	@echo two
	@echo Converting: $(.IMPSRC) to ${.TARGET}


dir1/%.b: dir3/%.a
	@echo three
	@echo Converting: $(.ALLSRC) to ${.TARGET}


%.b: %.x 
	@echo four
	@echo Converting: $(.ALLSRC) to ${.TARGET}
   

<dir1>.a.b:
	@echo five
	@echo Converting: $(.IMPSRC) to ${.TARGET}



