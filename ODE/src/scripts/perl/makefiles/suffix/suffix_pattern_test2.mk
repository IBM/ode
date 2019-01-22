SUBDIR=dir1
.SUFFIXES: .a .b .c .x.y .z .p.q .q

all: dir2/6.b dir2/1.c 18.z 20|.p.q  dir2/43

dir1/6.a ../dir1/1.a dir1/18.x.y dir1/dir2/20.q dir1/43.x.y:
	@echo Working With ${.TARGET}


dir2/%.c: ../dir1/%.a
	@echo one
	@echo Converting: $(.ALLSRC) to ${.TARGET}


<dir1>.a|<dir2>.b:
	@echo two
	@echo Converting: $(.IMPSRC) to ${.TARGET}


<${SUBDIR}>.x.y.z:     #Same as <dir1>.x.y|.z
	@echo three      
	@echo Converting: $(.IMPSRC) to ${.TARGET} 


%.p.q : ${SUBDIR}/dir2/%.q
	@echo four      
	@echo Converting: $(.ALLSRC) to ${.TARGET}

        
.x.y|<dir2>:
	@echo five      
	@echo Converting: $(.IMPSRC) to ${.TARGET}


<dir1>.x.y<dir2>:
	@echo six      
	@echo Converting: $(.IMPSRC) to ${.TARGET}

