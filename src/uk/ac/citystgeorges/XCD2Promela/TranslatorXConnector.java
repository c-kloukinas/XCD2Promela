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

public class TranslatorXConnector {

    static public T translate(BaseVisitor<T> bv
                              , CompositeOrConnectorDeclarationContext ctx
                              , String compName
                              , SymbolTable framenow
                              , SymbolTableComposite thisEnv) {
        T res = new T();
        return res;
    }

}
