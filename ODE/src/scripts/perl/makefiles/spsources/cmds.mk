all: foo1 foo2 foo3


foo1 : 
	@echo cmd1 
	@echo cmd2 
foo1 : .PRECMDS 
	@echo cmd3 
	@echo cmd4 
foo1 : .POSTCMDS 
	@echo cmd5 
foo1 : .PRECMDS .POSTCMDS 
	@echo cmd6 
	@echo cmd7
foo1 : .POSTCMDS        
	@echo end_foo1        


foo2 : 
	@echo cmd1 
	@echo cmd2 
	@echo cmd3 
	@echo cmd4 
foo2 : .REPLCMDS 
	@echo cmda
foo2 : .POSTCMDS 
	@echo cmdb 
	@echo end_foo2        


foo3 : 
	@echo cmd1 
	@echo cmd2 
foo3 : .REPLCMDS 
	@echo cmdc
foo3 : .PRECMDS .REPLCMDS .POSTCMDS 
	@echo cmdx 
	@echo cmdy 

