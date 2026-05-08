init {
	printf("Init before\n");
	int i = 0;
	atomic {
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
% spin -a atomic-run-test.pml
% CC=gcc-13 make pan && ./pan

(Spin Version 6.5.2 -- 6 December 2019)
        + Partial Order Reduction

Full statespace search for:
        never claim             - (none specified)
        assertion violations    +
        acceptance   cycles     - (not selected)
        invalid end states      +

State-vector 40 byte, depth reached 20, errors: 0
       10 states, stored
        0 states, matched
       10 transitions (= stored+matched)
       11 atomic steps
hash conflicts:         0 (resolved)

Stats on memory usage (in Megabytes):
    0.001       equivalent memory usage for states (stored*(State-vector + overhead))
    0.291       actual memory usage for states
  128.000       memory used for hash table (-w24)
    0.534       memory used for DFS stack (-m10000)
  128.730       total actual memory usage


unreached in init
        (0 of 15 states)
unreached in proctype sendx
        (0 of 2 states)

pan: elapsed time 0 seconds
*/
