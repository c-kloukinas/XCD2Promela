package uk.ac.citystgeorges.XCD2Promela;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.FileWriter;
import java.io.IOException;

import org.antlr.v4.runtime.*;
/*
	ArrayList<String> res = new ArrayList<String>();
	String s = "";

	res.add(s);
	return res;

	var framenow = env.get(env.size()-1);
	var symbolInfo = getIdInfo(symbol);
*/

public class XCD2PromelaVisitor extends ConnectorVisitor {
    /*
      Provisionally empty methods.
    */
    @Override public LstStr visitElementVariableDeclaration(XCDParser.ElementVariableDeclarationContext ctx) { return visitChildren(ctx); }

    /*
      Always empty methods
     */
    @Override
    public LstStr visitCompilationUnits(XCDParser.CompilationUnitsContext ctx) { return visitChildren(ctx); }

    @Override
    public LstStr visitCompilationUnit(XCDParser.CompilationUnitContext ctx) { return visitChildren(ctx); }

}
