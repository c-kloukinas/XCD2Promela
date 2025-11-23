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
BLDDIRFULL=$(TOPDIR)/$(BLDDIR)
JBLDDIR=$(BLDDIR)/jar-build
JBLDDIRFULL=$(TOPDIR)/$(JBLDDIR)
THINJAR=$(BLDDIRFULL)/$(TARGET)-thin.jar
BLDSRC=$(BLDDIR)/src
BLDCLS=$(BLDDIR)/classes
CLLIST=$(BLDDIRFULL)/list$(TARGET).list
SRCDIR=$(TOPDIR)/src
TESTDIR=$(BLDDIR)/test
TESTCASESDIR=$(TOPDIR)/xcd-test-cases
BACKUPDIR=$(TOPDIR)/y-ignore-me/z-keep-backups

JAVAC?=javac
JFLAGS?=-Xlint:unchecked

EXTERNAL_LIBS=0-external-libs
ANTLR_HOME=$(TOPDIR)
ANTLR_JAR_COMPLETE?=$(wildcard $(ANTLR_HOME)/$(EXTERNAL_LIBS)/antlr-*-complete.jar)
ANTLR_JAR_RUNTIME?=$(wildcard $(ANTLR_HOME)/$(EXTERNAL_LIBS)/antlr-runtime-*.jar)
ONEJAR?=$(wildcard $(TOPDIR)/$(EXTERNAL_LIBS)/one-jar-boot-*.jar)
CLASSPATH=.:$(BLDCLS):$(ANTLR_JAR_RUNTIME):$${CLASSPATH}
ANTLR=java -jar $(ANTLR_JAR_COMPLETE)

ALL_TESTS=$(wildcard $(TESTCASESDIR)/*.xcd)
ALL_TESTS_PASSED=$(patsubst $(TESTCASESDIR)/%.xcd,$(TESTDIR)/%.passed,$(ALL_TESTS))

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

.PHONY: all compile jar tests test1 test clean backup-incremental backup-full backupi backupf

$(BLDCLS)/$(PKGDIR)/%.class: $(SRCDIR)/$(PKGDIR)/%.java makefile
	CLASSPATH=$(CLASSPATH) $(JAVAC) $(JFLAGS) -d $(BLDCLS) --source-path $(SRCDIR) $(SRCDIR)/$(PKGDIR)/$*.java

$(BLDCLS)/$(PKGDIR)/%.class: $(BLDSRC)/$(PKGDIR)/%.java makefile
	CLASSPATH=$(CLASSPATH) $(JAVAC) $(JFLAGS) -d $(BLDCLS) --source-path $(BLDSRC) $(BLDSRC)/$(PKGDIR)/$*.java

$(TESTDIR)/%.passed: $(TESTCASESDIR)/%.xcd $(TARGETJAR)
	$(TOPDIR)/1-scripts/test-xcd $(TARGETJAR) $(TESTCASESDIR)/$*.xcd

all:	compile

check:
	echo ALL_TESTS=$(ALL_TESTS) 
	echo ALL_TESTS_PASSED=$(ALL_TESTS_PASSED) 

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
	@cd $(SRCDIR); jar -u -f $(THINJAR) resources

$(TARGETJAR): $(THINJAR)
	-@rm -rf $(JBLDDIRFULL)
	@mkdir -p $(JBLDDIRFULL)/lib $(JBLDDIRFULL)/main
	@cd $(JBLDDIRFULL); jar -xf $(ONEJAR)
	-@rm -rf $(JBLDDIRFULL)/src
	@cp -p $(THINJAR) $(JBLDDIRFULL)/main/
	@cp -p $(ANTLR_JAR_RUNTIME) $(JBLDDIRFULL)/lib/
	@echo 'One-Jar-Main-Class: '$(PKG).$(MAIN) >> $(JBLDDIRFULL)/boot-manifest.mf
	cd $(JBLDDIRFULL) ; jar -cvfm $(TARGETJAR) boot-manifest.mf . > /dev/null 2>&1

$(TESTDIR):
	mkdir -p $(TESTDIR)

test1:  $(TESTDIR)/aegis_deadlocking.passed

tests:	$(ALL_TESTS)
	-rm -f $(TESTDIR)/*.failed
	MAIN=$(MAIN) make -k $(ALL_TESTS_PASSED)

test:  tests

clean:
	-rm -rf $(TARGETJAR) $(JBLDDIRFULL) $(BLDDIRFULL)

backupf:	backup-full

backupi:	backup-incremental

backup-full:
	@sh 1-scripts/files-outside-build

backup-incremental:
	@sh 1-scripts/files-outside-build -n

