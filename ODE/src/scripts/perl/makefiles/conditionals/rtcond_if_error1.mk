#Testing .rif with an extra .relse

a:
	.rif 1
	result=1
	.relse
	.relif 0
	result=0
	.relse
	.rendif
