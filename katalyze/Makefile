# Find all the jar files in the lib directory, and string them together using
# colons (appropriate for giving to -classpath). Then append the current
# directory and src directory.
CLASSPATH=$(shell find lib/*.jar | tr '\n' ':')src:.

%.class : %.java
	javac -classpath $(CLASSPATH) $<

# make all the files except those in "tests", which aren't building right now
JAVAFILES = $(shell find src | grep -v tests | grep 'java$$')
CLASSFILES = $(JAVAFILES:.java=.class)

all: $(CLASSFILES)

clean:
	rm -f $$(find src | grep class$$)

.PHONY: clean all
