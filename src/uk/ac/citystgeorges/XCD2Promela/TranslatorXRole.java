package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TranslatorXRole {

    static public T translate(BaseVisitor<T> bv
                              , ComponentOrRoleDeclarationContext ctx
                              , String compName
                              , SymbolTable framenow
                              , SymbolTableComponent thisEnv) {
        T res = new T();
        return res;
    }

}
