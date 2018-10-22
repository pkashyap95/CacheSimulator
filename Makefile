#JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

# List all your .java files here
CLASSES = sim_cache.java 

default: classes
	@echo "my work is done here..."

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
