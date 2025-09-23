# MAIN?=TestGrammar
MAIN?=XCD2Promela
GRAMMAR=XCD
PKG=uk.ac.citystgeorges.XCD2Promela
PKGDIR=$(shell echo $(PKG) | tr . /)
TOPDIR=$(shell pwd)

TARGET=the$(MAIN)$(GRAMMAR)
TARGET=$(GRAMMAR)$(MAIN)
TARGET=$(MAIN)
BLDDIR=build
BLDSRC=$(BLDDIR)/src
BLDCLS=$(BLDDIR)/classes
CLLIST=$(BLDDIR)/list$(TARGET).list
SRCDIR=src

EXTERNAL_LIBS=0-external-libs
ANTLR_HOME=$(TOPDIR)
ANTLR_JAR_COMPLETE?=$(wildcard $(ANTLR_HOME)/$(EXTERNAL_LIBS)/antlr-*-complete.jar)
ANTLR_JAR_RUNTIME?=$(wildcard $(ANTLR_HOME)/$(EXTERNAL_LIBS)/antlr-runtime-*.jar)
ONEJAR?=$(wildcard $(TOPDIR)/$(EXTERNAL_LIBS)/one-jar-boot-*.jar)
CLASSPATH=.:$(BLDCLS):$(ANTLR_JAR_RUNTIME):$${CLASSPATH}
ANTLR=java -jar $(ANTLR_JAR_COMPLETE)

# files produced by antlr from a grammar file:
JAVA_SRC=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%BaseListener.java,$(GRAMMAR))
JAVA_SRC+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%Lexer.java,$(GRAMMAR))
JAVA_SRC+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%Listener.java,$(GRAMMAR))
JAVA_SRC+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%Parser.java,$(GRAMMAR))
JAVA_CLASSES=$(patsubst $(BLDSRC)/$(PKGDIR)/%.java,$(BLDCLS)/$(PKGDIR)/%.class,$(JAVA_SRC))

# normal Java src files
NJS=$(wildcard $(SRCDIR)/$(PKGDIR)/*.java)
JAVA_SRC+=$(NJS)
JAVA_CLASSES+=$(patsubst $(SRCDIR)/$(PKGDIR)/%.java,$(BLDCLS)/$(PKGDIR)/%.class,$(NJS))

.PRECIOUS: $(JAVA_SRC)

$(BLDSRC)/$(PKGDIR)/%.interp $(BLDSRC)/$(PKGDIR)/%.tokens $(BLDSRC)/$(PKGDIR)/%BaseListener.java $(BLDSRC)/$(PKGDIR)/%Lexer.interp $(BLDSRC)/$(PKGDIR)/%Lexer.java $(BLDSRC)/$(PKGDIR)/%Lexer.tokens $(BLDSRC)/$(PKGDIR)/%Listener.java $(BLDSRC)/$(PKGDIR)/%Parser.java: $(SRCDIR)/$(PKGDIR)/%.g4 makefile
	(cd $(SRCDIR); $(ANTLR) -o $(TOPDIR)/$(BLDSRC) -package $(PKG) $(PKGDIR)/$*.g4)

$(BLDCLS)/$(PKGDIR)/%.class: $(SRCDIR)/$(PKGDIR)/%.java makefile
	CLASSPATH=$(CLASSPATH) javac -d $(BLDCLS) --source-path $(SRCDIR) $(SRCDIR)/$(PKGDIR)/$*.java

$(BLDCLS)/$(PKGDIR)/%.class: $(BLDSRC)/$(PKGDIR)/%.java makefile
	CLASSPATH=$(CLASSPATH) javac -d $(BLDCLS) --source-path $(BLDSRC) $(BLDSRC)/$(PKGDIR)/$*.java

all:	test1

$(CLLIST): $(JAVA_CLASSES) makefile
	(cd $(BLDCLS); find -name '*.class' |sort -u) > $(CLLIST)
	tr ' ' '\n' < $(CLLIST) | wc -l

$(TARGET).jar: $(JAVA_CLASSES) $(CLLIST)
	-@cd $(BLDCLS); rm -f ../$(TARGET)-thin.jar
	cd $(BLDCLS); jar -c -f ../$(TARGET)-thin.jar -e $(MAIN) @../../$(CLLIST)
	-rm -rf jar-build
	mkdir -p jar-build/lib jar-build/main
	cd jar-build; jar -xf $(ONEJAR)
	-rm -rf jar-build/src
	cp -p $(BLDDIR)/$(TARGET)-thin.jar jar-build/main/
	cp -p $(ANTLR_JAR_RUNTIME) jar-build/lib/
	echo 'One-Jar-Main-Class: '$(PKG).$(MAIN) >> jar-build/boot-manifest.mf
	cd jar-build ; jar -cvfm ../$(TARGET).jar boot-manifest.mf .

test1:  $(TARGET).jar
	java -jar $(TARGET).jar < xcd-test-cases/aegis_deadlocking.xcd

test:  $(TARGET).jar
	for f in xcd-test-cases/*.xcd ; do echo $$f ; java -jar $(TARGET).jar <$$f  >/dev/null; if [ $$? != 0 ]; then break; fi; done

clean:
	-rm -rf $(TARGET).jar $(BLDDIR)/* $(CLLIST) $(BLDDIR)/$(TARGET)-thin.jar
	-rm -rf jar-build
