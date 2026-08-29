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
SRCDIRPLAIN=src
SRCDIR=$(TOPDIR)/$(SRCDIRPLAIN)
RESDIR=resources
# find normal files, exclude Emacs backups
RESOURCES=$(shell find $(SRCDIR)/$(RESDIR) -type f | grep -v '~$$')
TESTDIR=$(BLDDIR)/test
TESTCASESDIR=$(TOPDIR)/xcd-test-cases
BACKUPDIR=$(TOPDIR)/y-ignore-me/z-keep-backups
SCRIPTDIR=$(TOPDIR)/1-scripts

GRAMMARFULL=$(PKGDIR)/$(GRAMMAR).g4
# GRAMMARPARSER=$(patsubst %.g4,$(BLDSRC)/%Parser.java,$(GRAMMARFULL))
TARGETJAVA=$(SRCDIR)/$(PKGDIR)/$(TARGET).java
TARGETCLASS=$(patsubst $(SRCDIR)/%.java,$(BLDCLS)/%.class,$(TARGETJAVA))

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

# files produced by antlr from a grammar file (Produced Java Sources/Classes):
PJS=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%Parser.java,$(GRAMMAR))
PJS+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%Lexer.java,$(GRAMMAR))
PJS+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%BaseListener.java,$(GRAMMAR))
PJS+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%Listener.java,$(GRAMMAR))
PJS+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%BaseVisitor.java,$(GRAMMAR))
PJS+=$(patsubst %,$(BLDSRC)/$(PKGDIR)/%Visitor.java,$(GRAMMAR))
PJC=$(patsubst $(BLDSRC)/%.java,$(BLDCLS)/%.class,$(PJS))

# Normal Java Source/Class files
# NJS=$(wildcard $(SRCDIR)/$(PKGDIR)/*.java)
NJS=$(shell find $(SRCDIR) -name '*.java')
JAVA_SRC=$(PJS) $(NJS)
NJC=$(patsubst $(SRCDIR)/%.java,$(BLDCLS)/%.class,$(NJS))
NJCWITHOUTTARGETCLASS=$(patsubst $(TARGETCLASS),,$(NJC))

# all Java class files
JAVA_CLASSES=$(PJC) $(NJC)

# Define dependency tracking files ONLY for normal source
DEPS=$(patsubst %.class,%.d,$(NJC))
JDEPS=echo jdeps
JDEPS=jdeps

.PRECIOUS: $(JAVA_SRC)

.PHONY: all compile jar \
	tests test test1 \
	clean \
	backup-incremental backup-full backupi backupf \
	unused deps

all:	jar unused

# explicit empty rule for everything in the src directory.
$(SRCDIR)/%:	;

# explicit empty rule for the makefile itself as well.
makefile:	;

# explicit empty rule for dependencies - don't try to automatically re-create
# them (horrible looping if not present because make includes them and tries to
# update them each time).
%.d:	;

$(BLDCLS)%.class: $(SRCDIR)%.java
	@mkdir -p $(dir $@)
	CLASSPATH=$(CLASSPATH) \
	$(JAVAC) $(JFLAGS) -d $(BLDCLS) --source-path $(SRCDIR):$(BLDSRC) $<

$(BLDCLS)%.class: $(BLDSRC)%.java
	@mkdir -p $(dir $@)
	CLASSPATH=$(CLASSPATH) \
	$(JAVAC) $(JFLAGS) -d $(BLDCLS) --source-path $(BLDSRC):$(SRCDIR) $<

$(TESTDIR)/%.passed: $(TESTCASESDIR)/%.xcd $(TARGETJAR) $(SCRIPTDIR)/test-xcd makefile
	$(SCRIPTDIR)/test-xcd $(TARGETJAR) $(TESTCASESDIR)/$*.xcd

unused: $(NJS)
	@for f in $(NJS) ; do \
		b=`basename $$f .java` \
		; n=`grep $$b $(NJS) \
		| wc -l` \
		; if [ $$n = 1 ]; then \
			echo NOTE: $$f was NOT used \
		; fi \
	; done

check:
	echo ALL_TESTS=$(ALL_TESTS)
	echo ALL_TESTS_PASSED=$(ALL_TESTS_PASSED)

$(PJS): $(SRCDIR)/$(GRAMMARFULL)
	(cd $(SRCDIR); \
	$(ANTLR) -visitor -o $(TOPDIR)/$(BLDSRC) -package $(PKG) $(GRAMMARFULL))

$(NJC):	$(PJS)

# Just compile the target main class - the rest of NJS/PJS will be
# compiled implicitly as needed
#
# But first remake any stale class files that are not the target main
# class
$(TARGETCLASS): $(PJS) $(NJS) makefile
	@$(SCRIPTDIR)/remake $(NJCWITHOUTTARGETCLASS)
	CLASSPATH=$(CLASSPATH) \
	$(JAVAC) $(JFLAGS) -d $(BLDCLS) \
		--source-path $(SRCDIR):$(BLDSRC) \
		$(TARGETJAVA)

#	 | grep -v 'is up to date' \

# can create/update dependencies once you've (re)compiled
compile: $(TARGETCLASS) deps

#	@echo Java src: $(NJS)
#	@echo Java src produced: $(PJS)
#	@echo Java classes: $(NJC)
#	@echo Java classes produced: $(PJC)

jar: compile $(TARGETJAR)

$(CLLIST): $(JAVA_CLASSES) makefile
	@(cd $(BLDCLS); find -name '*.class' |sort -u) > $(CLLIST)
	wc -l $(CLLIST)

#	@(echo $(JAVA_CLASSES) | tr ' ' '\n' |sort -u) > $(CLLIST)2
#	wc -l $(CLLIST) $(CLLIST)2

$(THINJAR): $(CLLIST) $(RESOURCES)
	-@cd $(BLDCLS); rm -f $(THINJAR)
	cd $(BLDCLS) \
	; jar -c -f $(THINJAR) -e $(MAIN) @$(CLLIST)
	cd $(SRCDIR) \
	; jar -u -f $(THINJAR) $(RESDIR)

$(TARGETJAR): $(THINJAR)
	-@rm -rf $(JBLDDIRFULL)
	@mkdir -p $(JBLDDIRFULL)/lib $(JBLDDIRFULL)/main
	@cd $(JBLDDIRFULL) \
	; jar -xf $(ONEJAR)
	-@rm -rf $(JBLDDIRFULL)/src
	@cp -p $(THINJAR) $(JBLDDIRFULL)/main/
	@cp -p $(ANTLR_JAR_RUNTIME) $(JBLDDIRFULL)/lib/
	@echo 'One-Jar-Main-Class: '$(PKG).$(MAIN) >> $(JBLDDIRFULL)/boot-manifest.mf
	cd $(JBLDDIRFULL) \
	; jar -cvfm $(TARGETJAR) boot-manifest.mf . > /dev/null 2>&1

$(TESTDIR):
	mkdir -p $(TESTDIR)

test1:  $(TESTDIR)/aegis_deadlocking.passed

FAILURES0=$(TESTDIR)/*.failed
tests:	jar $(ALL_TESTS)
	-rm -f $(FAILURES0)
	-MAIN=$(MAIN) make -k $(ALL_TESTS_PASSED)
	@export SUCCESSES=$(TESTDIR)/*.passed ; \
	if [ z"`ls $${SUCCESSES} 2> /dev/null`" != z ] ; then \
          echo PASSED: `ls $${SUCCESSES} | wc -l` ; \
	  egrep 'There were [^0]' $${SUCCESSES} ; \
	else \
	  echo 'NONE PASSED!' ; \
	fi
	@export FAILURES=$(TESTDIR)/*.failed ; \
	if [ z"`ls $${FAILURES} 2> /dev/null`" != z ] ; then \
	  echo FAILED: `ls $${FAILURES} | wc -l` ; \
	  ls $${FAILURES} ; \
	  egrep 'There were [^0]' $${FAILURES} ; \
	  exit 1 ; \
	else \
	  echo FAILED: 0 ; \
	fi

test:  tests

clean:
	-rm -rf $(TARGETJAR) $(JBLDDIRFULL) $(BLDDIRFULL)
	-ls -d */. \
	| while read f ; do \
		d=`dirname "${f}"` ; \
		if [ -d "${d}" -a ! -h "${d}" ] ; then \
			-find "${d}" -name '*~' -a \! -name z-stage-us~ -exec rm '{}' \; ; \
		fi ; \
	done

backupf:	backup-full

backupi:	backup-incremental

backup-full:
	@sh $(SCRIPTDIR)/files-outside-build

backup-incremental:
	@sh $(SCRIPTDIR)/files-outside-build -n

## This rule causes class files to be re-compiled all the time, because make
## tries to update what it includes.
## Instead, we have an explicit empty rule for %d and have target deps after the
## actual compilation.
# %.d: %.class

# In the JDEPS call below, which is from a %.d: %.class old rule, we have:
# depfile = $@
# classfile = $<
deps:	$(DEPS) makefile
	@for depfile in $(DEPS); do \
	    classfile=`dirname "$${depfile}"`/`basename "$${depfile}" .d`.class ; \
	    javafile=`dirname "$${depfile}"`/`basename "$${depfile}" .d`.java ; \
	    if [ -f "$${classfile}" ] ; then \
		 if [ ! -f "$${depfile}" -o "$${depfile}" -ot "$${classfile}" ] ; then \
		   echo 'COMMENT: Add self dependency' > /dev/null ; \
		   echo "$${classfile}": "$${javafile}" > "$${depfile}" ; \
		   echo 'COMMENT: No self dependency, to find independent classes' > /dev/null ; \
		   echo '' > "$${depfile}" ; \
	           $(JDEPS) -verbose:class -filter:none -cp $(BLDCLS) "$${classfile}" 2>/dev/null \
		     | grep '[-]> '"$(PKG)" \
		     | grep -v '\$$' \
		     | tr '\t' ' ' \
		     | sed -e 's/  */ /g' -e 's/^ //' -e 's/ classes *$$//' \
		     | sed -e 's|\.|\/|g' \
		           -e 's|^\([^ ]*\) -> \([^ ]*\)$$|$(SRCDIRPLAIN)/\2.java|' \
		     | while read f ; do \
			  if [ -f "$${f}" ] ; then \
			    echo "$${classfile}": "$${f}" ; \
			  fi ; \
		     done \
		     >> "$${depfile}" ; \
		     echo Updated "$${depfile}" ; \
	         fi ; \
            else \
	      rm -f "$${depfile}" ; \
	    fi ; \
	done

# Include generated dependency rules if they exist
-include $(DEPS)

