# Makefile to compile myrpal.java

# Compiler
JAVAC = javac

# Source and Output
SRC = myrpal.java
CLASS = myrpal.class

# Default target
all: $(CLASS)

$(CLASS): $(SRC)
	$(JAVAC) $(SRC)

# Clean class files
clean:
	find . -name "*.class" -type f -delete
