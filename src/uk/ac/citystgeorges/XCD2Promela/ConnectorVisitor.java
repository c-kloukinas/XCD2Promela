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
public abstract class ConnectorVisitor extends ComponentVisitor {
    @Override
    public LstStr visitConnectorDeclaration(XCDParser.ConnectorDeclarationContext ctx) { return null; }
}
