
package uk.ac.citystgeorges.XCD2Promela;

import java.util.ArrayList;

// Just a shorthand for ArrayList<String>
class LstStr extends ArrayList<String> {
        LstStr() { super(); }
        LstStr(int sz) { super(sz); }
        LstStr(LstStr other) { super( other!=null ? other : new LstStr() ); }
        LstStr(String s1) { super(1); add(s1); }
        LstStr(String s1, String s2) { super(2); add(s1); add(s2); }
};
