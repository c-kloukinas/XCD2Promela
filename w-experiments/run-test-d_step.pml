init {
	printf("Init before\n");
	int i = 0;
	d_step {
		do
		:: i<3 ->
			run sendx(i);
			printf("Init after %d\n", i);
			i++;
		:: else -> break;
		od;
		skip;
	}
	printf("Init after %d\n", i);
}

proctype sendx(int i) {
	printf("sendx %d\n", i);
}
/*
% spin -a d-step-run-test.pml
illegal operator in 'd_step:' '(run sendx(i))'
spin: d-step-run-test.pml:7, Error: 'run operator in d_step'
*/
