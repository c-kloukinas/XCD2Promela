GRAMMAR=XCD
MAIN=TestGrammar
PKG=uk.ac.citystgeorges.XCD2Promela
PKGDIR=$(shell echo $(PKG) | tr . /)
TOPDIR=$(shell pwd)

TARGET=the$(MAIN)$(GRAMMAR)
TARGET=$(GRAMMAR)$(MAIN)
BLDDIR=build
BLDSRC=$(BLDDIR)/src
BLDCLS=$(BLDDIR)/classes
CLLIST=$(BLDDIR)/list$(TARGET).list
SRCDIR=src

# files produced by antlr from a grammar file:
JAVA_SRC=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%BaseListener.java,$(GRAMMAR))
JAVA_SRC+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%Lexer.java,$(GRAMMAR))
JAVA_SRC+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%Listener.java,$(GRAMMAR))
JAVA_SRC+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%Parser.java,$(GRAMMAR))
JAVA_CLASSES=$(patsubst $(BLDSRC)/$(PKGDIR)/%.java,$(BLDCLS)/$(PKGDIR)/%.class,$(JAVA_SRC))

# normal Java src files
JAVA_SRC+=$(SRCDIR)/$(PKGDIR)/$(MAIN).java
JAVA_CLASSES+=$(BLDCLS)/$(PKGDIR)/$(MAIN).class

.PRECIOUS: $(JAVA_SRC)

EXTERNAL_LIBS=0-external-libs
# ANTLR_HOME=$(HOME)/bin
ANTLR_HOME=$(TOPDIR)
# ANTLR_JAR_COMPLETE?=$(ANTLR_HOME)/$(EXTERNAL_LIBS)/antlr-4.13.2-complete.jar
# ANTLR_JAR_RUNTIME?=$(ANTLR_HOME)/$(EXTERNAL_LIBS)/antlr-runtime-4.13.2.jar
ANTLR_JAR_COMPLETE?=$(shell echo $(ANTLR_HOME)/$(EXTERNAL_LIBS)/antlr-*-complete.jar)
ANTLR_JAR_RUNTIME?=$(shell echo $(ANTLR_HOME)/$(EXTERNAL_LIBS)/antlr-runtime-*.jar)
CLASSPATH=.:$(BLDCLS):$(ANTLR_JAR_RUNTIME):$${CLASSPATH}
ANTLR=java -jar $(ANTLR_JAR_COMPLETE)

#	$(ANTLR) -o $(BLDSRC) -package $(PKG) -Xexact-output-dir $(SRCDIR)/$(PKGDIR)/$*.g4
$(BLDSRC)/$(PKGDIR)/%.interp $(BLDSRC)/$(PKGDIR)/%.tokens $(BLDSRC)/$(PKGDIR)/%BaseListener.java $(BLDSRC)/$(PKGDIR)/%Lexer.interp $(BLDSRC)/$(PKGDIR)/%Lexer.java $(BLDSRC)/$(PKGDIR)/%Lexer.tokens $(BLDSRC)/$(PKGDIR)/%Listener.java $(BLDSRC)/$(PKGDIR)/%Parser.java: $(SRCDIR)/$(PKGDIR)/%.g4 makefile
	(cd $(SRCDIR); $(ANTLR) -o $(TOPDIR)/$(BLDSRC) -package $(PKG) $(PKGDIR)/$*.g4)

$(BLDCLS)/$(PKGDIR)/%.class: $(SRCDIR)/$(PKGDIR)/%.java makefile
	CLASSPATH=$(CLASSPATH) javac -d $(BLDCLS) --source-path $(SRCDIR) $(SRCDIR)/$(PKGDIR)/$*.java

$(BLDCLS)/$(PKGDIR)/%.class: $(BLDSRC)/$(PKGDIR)/%.java makefile
	CLASSPATH=$(CLASSPATH) javac -d $(BLDCLS) --source-path $(BLDSRC) $(BLDSRC)/$(PKGDIR)/$*.java

all:	$(TARGET).jar

#$(CLLIST): $(JAVA_CLASSES) makefile
	# (cd $(BLDCLS); echo '$(sort $(JAVA_CLASSES) $(wildcard $(GRAMMAR)Parser*.class))') > $(CLLIST)
$(CLLIST): $(JAVA_CLASSES) makefile
	(cd $(BLDCLS); find -name '*.class' |sort -u) > $(CLLIST)
	tr ' ' '\n' < $(CLLIST) | wc -l

# $(TARGET).jar: $(JAVA_CLASSES)
$(TARGET).jar: $(JAVA_CLASSES) $(CLLIST)
	-@cd $(BLDCLS); rm -f ../$(TARGET)-thin.jar
	cd $(BLDCLS); jar -c -f ../$(TARGET)-thin.jar -e $(MAIN) @../../$(CLLIST)
	-rm -rf jar-build
	mkdir -p jar-build/lib jar-build/main
	cd jar-build; jar -xf ../$(EXTERNAL_LIBS)/one-jar-boot-0.97.jar
	-rm -rf jar-build/src
	cp -p $(BLDDIR)/$(TARGET)-thin.jar jar-build/main/
	cp -p $(ANTLR_JAR_RUNTIME) jar-build/lib/
	echo 'One-Jar-Main-Class: '$(PKG).$(MAIN) >> jar-build/boot-manifest.mf
	cd jar-build ; jar -cvfm ../$(TARGET).jar boot-manifest.mf .

test:  $(TARGET).jar
	for f in xcd-test-cases/*.xcd ; do echo $$f ; java -jar $(TARGET).jar <$$f  >/dev/null; if [ $$? != 0 ]; then break; fi; done

clean:
	-rm -rf $(TARGET).jar $(BLDDIR)/* $(CLLIST) $(BLDDIR)/$(TARGET)-thin.jar
	-rm -rf jar-build
