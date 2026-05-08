active proctype tester () {
  chan set = [8] of { byte, byte };
  byte x, y;

  set ! 3,5 ;
  set ! 5,7 ;
  set ! 2,4 ;

  set ?? <x,y> ;    /* poll if there's an element        */
  if
  :: assert(y%x!=0) /* can it fail? Not if the third message is never considered */
  :: else -> skip
  fi ;
  skip
}
/* Run as:
spin -a spin-polling-test.pml && make pan && ./pan
*/
