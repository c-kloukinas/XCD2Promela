# MAIN?=TestGrammar
MAIN?=XCD2Promela
GRAMMAR=XCD
PKG=uk.ac.citystgeorges.XCD2Promela
PKGDIR=$(shell echo $(PKG) | tr . /)
TOPDIR=$(shell pwd)

TARGET=the$(MAIN)$(GRAMMAR)
TARGET=$(GRAMMAR)$(MAIN)
TARGET=$(MAIN)
TARGETJAR=$(TOPDIR)/$(TARGET).jar
BLDDIR=build
BLDDIRFULL=$(TOPDIR)/build
THINJAR=$(BLDDIRFULL)/$(TARGET)-thin.jar
BLDSRC=$(BLDDIR)/src
BLDCLS=$(BLDDIR)/classes
CLLIST=$(BLDDIRFULL)/list$(TARGET).list
SRCDIR=$(TOPDIR)/src
TESTDIR=$(BLDDIR)/test

EXTERNAL_LIBS=0-external-libs
ANTLR_HOME=$(TOPDIR)
ANTLR_JAR_COMPLETE?=$(wildcard $(ANTLR_HOME)/$(EXTERNAL_LIBS)/antlr-*-complete.jar)
ANTLR_JAR_RUNTIME?=$(wildcard $(ANTLR_HOME)/$(EXTERNAL_LIBS)/antlr-runtime-*.jar)
ONEJAR?=$(wildcard $(TOPDIR)/$(EXTERNAL_LIBS)/one-jar-boot-*.jar)
CLASSPATH=.:$(BLDCLS):$(ANTLR_JAR_RUNTIME):$${CLASSPATH}
ANTLR=java -jar $(ANTLR_JAR_COMPLETE)

# files produced by antlr from a grammar file:
PJS=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%Parser.java,$(GRAMMAR))
PJS+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%Lexer.java,$(GRAMMAR))
PJS+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%BaseListener.java,$(GRAMMAR))
PJS+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%Listener.java,$(GRAMMAR))
PJS+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%BaseVisitor.java,$(GRAMMAR))
PJS+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%Visitor.java,$(GRAMMAR))
PJC=$(patsubst $(BLDSRC)/$(PKGDIR)/%.java,$(BLDCLS)/$(PKGDIR)/%.class,$(PJS))

# normal Java src files
NJS=$(wildcard $(SRCDIR)/$(PKGDIR)/*.java)
JAVA_SRC=$(PJS) $(NJS)
NJC=$(patsubst $(SRCDIR)/$(PKGDIR)/%.java,$(BLDCLS)/$(PKGDIR)/%.class,$(NJS))
JAVA_CLASSES=$(PJC) $(NJC)

.PRECIOUS: $(JAVA_SRC)

$(BLDCLS)/$(PKGDIR)/%.class: $(SRCDIR)/$(PKGDIR)/%.java makefile
	CLASSPATH=$(CLASSPATH) javac -d $(BLDCLS) --source-path $(SRCDIR) $(SRCDIR)/$(PKGDIR)/$*.java

$(BLDCLS)/$(PKGDIR)/%.class: $(BLDSRC)/$(PKGDIR)/%.java makefile
	CLASSPATH=$(CLASSPATH) javac -d $(BLDCLS) --source-path $(BLDSRC) $(BLDSRC)/$(PKGDIR)/$*.java

all:	compile

compile: $(JAVA_CLASSES) makefile

#	@echo Java src: $(NJS)
#	@echo Java src produced: $(PJS)
#	@echo Java classes: $(NJC)
#	@echo Java classes produced: $(PJC)

jar: $(TARGETJAR)

$(PJS): $(SRCDIR)/$(PKGDIR)/$(GRAMMAR).g4 makefile
	(cd $(SRCDIR); $(ANTLR) -visitor -o $(TOPDIR)/$(BLDSRC) -package $(PKG) $(PKGDIR)/$(GRAMMAR).g4)

$(CLLIST): $(JAVA_CLASSES) makefile
	@(cd $(BLDCLS); find -name '*.class' |sort -u) > $(CLLIST)
	wc -l $(CLLIST)

#	@(echo $(JAVA_CLASSES) | tr ' ' '\n' |sort -u) > $(CLLIST)2
#	wc -l $(CLLIST) $(CLLIST)2

$(THINJAR): $(CLLIST)
	-@cd $(BLDCLS); rm -f $(THINJAR)
	@cd $(BLDCLS); jar -c -f $(THINJAR) -e $(MAIN) @$(CLLIST)

$(TARGETJAR): $(THINJAR)
	-@rm -rf jar-build
	@mkdir -p jar-build/lib jar-build/main
	@cd jar-build; jar -xf $(ONEJAR)
	-@rm -rf jar-build/src
	@cp -p $(THINJAR) jar-build/main/
	@cp -p $(ANTLR_JAR_RUNTIME) jar-build/lib/
	@echo 'One-Jar-Main-Class: '$(PKG).$(MAIN) >> jar-build/boot-manifest.mf
	cd jar-build ; jar -cvfm $(TARGETJAR) boot-manifest.mf . > /dev/null 2>&1

$(TESTDIR):
	mkdir -p $(TESTDIR)

test1:  jar $(TESTDIR)
	(cd $(TESTDIR); java -jar $(TARGETJAR) < $(TOPDIR)/xcd-test-cases/aegis_deadlocking.xcd)

test:  jar $(TESTDIR)
	(cd $(TESTDIR); for f in $(TOPDIR)/xcd-test-cases/*.xcd ; do echo $$f ; java -jar $(TARGETJAR) <$$f  >/dev/null; if [ $$? != 0 ]; then break; fi; done)

clean:
	-rm -rf $(TARGETJAR) $(BLDDIR)/* $(CLLIST) $(THINJAR)
	-rm -rf jar-build
