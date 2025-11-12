package uk.ac.citystgeorges.XCD2Promela;

/**
 * This class exists so I don't have to edit the signatures of all the
 * methods that I inherit from the generic visitor XCDBaseVisitor,
 * which return a type parameter "T".
 */

class T extends LstStr {
    T() {super();}
    T(int ln) {super(ln);}
    T(LstStr o) {
        super(o.size());
        for (var el : o)
            add(el);
    }
};
