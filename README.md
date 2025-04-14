# 📘 RPAL Compiler Project: Lexical Analyzer, Parser & CSE Machine

## 📑 Table of Contents
- [📌 Introduction](#-introduction)
- [🧠 What is RPAL?](#-what-is-rpal)
- [🔍 What is a Lexical Analyzer?](#-what-is-a-lexical-analyzer)
- [🧩 What is a Parser?](#-what-is-a-parser)
- [📂 Project Structure](#-project-structure)
- [🧪 How to Run](#-how-to-run)
- [🔍 Java Files & Their Responsibilities](#-java-files--their-responsibilities)
- [📚 Sample Input and Output](#-sample-input-and-output)


---

## 📌 Introduction
This project is a mini compiler for the RPAL programming language developed for **CS 3513 - Programming Languages**. It includes a:
- Lexical Analyzer
- Parser
- Abstract Syntax Tree (AST) Generator
- Standardizer
- Control Stack Environment (CSE) Machine

It mimics the behavior of `rpal.exe` and supports command-line arguments to display AST, ST, or final output.

---

## 🧠 What is RPAL?
**RPAL (Right-reference Programming Algorithmic Language)** is a functional programming language used in academia to teach compiler construction. It emphasizes recursion, immutability, and the functional paradigm.

This project is centered around reading and evaluating RPAL programs by compiling them through different compiler phases.

---

## 🔍 What is a Lexical Analyzer?
A **Lexical Analyzer** (or scanner) is the first phase of a compiler that processes the input source code into tokens. These tokens are:
- Identifiers
- Operators
- Reserved keywords
- Constants

> 📁 Relevant file: `LexicalAnalyzer.java`

---

## 🧩 What is a Parser?
A **Parser** checks the sequence of tokens for grammatical structure according to a formal grammar. It builds an Abstract Syntax Tree (AST), which represents the syntactic structure of the code.

> 📁 Relevant file: `Parser.java`

---

## 📂 Project Structure
```bash
lex-analyzer-parser/
├── .idea/
├── out/
├── src/
│   ├── csemachine/
│   │   ├── CSEMachine.java
│   │   └── Environment.java
│   ├── lexical/
│   │   ├── LexicalAnalyzer.java
│   │   └── Token.java
│   ├── parser/
│   │   ├── Parser.java
│   │   └── Node.java
│   ├── standardizer/
│   │   └── Standardizer.java
│   ├── structures/
│   │   ├── Delta.java
│   │   ├── Eta.java
│   │   ├── Lambda.java
│   │   └── Tau.java
│   └── myrpal.java
├── Tests/
│   ├── testfile1
│   └── testfile2
├── .gitignore
├── .gitattributes
├── README
└── RPAL.iml
```

---

## 🧪 How to Run
### 🔧 Step-by-Step

1. **Clone the Repository**
```bash
git clone https://github.com/Vidurshika/lex-analyzer-parser.git
```
2. **Open in IntelliJ IDEA or another Java IDE**

3. **Navigate to the src directory**
```bash
cd lex-analyzer-parser/src
```
4. **Compile the Program**
```bash
javac myrpal.java lexical/*.java parser/*.java standardizer/*.java csemachine/*.java
```

### ▶️ Running the Program

#### If test files are in the `src` folder:

✅ To execute a program:
```bash
java myrpal file_name
```

🌲 To print Abstract Syntax Tree (AST):
```bash
java myrpal -ast file_name
```

🪵 To print Standardized Tree (ST):
```bash
java myrpal -st file_name
```

📄 To print file content:
```bash
java myrpal -l file_name
```

#### If test files are outside `src`:
```bash
java myrpal ../Tests/file_name
```

#### If file has an extension (e.g., `.txt`):
```bash
java myrpal ../Tests/file_name.txt
```


---

## 🔍 Java Files & Their Responsibilities

### 🔤 Lexical Analysis (📂 lexical)
- **LexicalAnalyzer.java**: Scans the RPAL source file and splits it into tokens.
- **Token.java**: Represents individual tokens with type and value.

### 🧱 Parsing (📂 parser)
- **Parser.java**: Builds an AST based on tokens from the lexical analyzer.
- **Node.java**: Represents a node in the AST.

### 🛠️ Standardizing (📂 standardizer)
- **Standardizer.java**: Converts AST into a Standardized Tree (ST) following specific transformation rules.

### ⚙️ CSE Execution (📂 csemachine)
- **CSEMachine.java**: Executes the standardized tree using the Control Stack Environment (CSE) machine model.
- **Environment.java**: Manages variable scopes and values during CSE execution.

### 📐 Structures (📂 structures)
- **Delta.java, Eta.java, Lambda.java, Tau.java**: Define internal representations for different types of tree nodes used during standardization and execution.

### 🧾 Main Class
- **myrpal.java**: Entry point of the program. Coordinates lexical analysis, parsing, standardization, and evaluation.


---

## 📚 Sample Input and Output
### 🔡 Input
```rpal
let Sum(A) = Psum (A,Order A )
where rec Psum (T,N) = N eq 0 -> 0 | Psum(T,N-1)+T N
in Print ( Sum (1,2,3,4,5) )
```

### 🌳 `-ast` Output
```
let
.function_form
..<ID:Sum>
..<ID:A>
..where
...gamma
....<ID:Psum>
....tau
.....<ID:A>
.....gamma
......<ID:Order>
......<ID:A>
...rec
....function_form
.....<ID:Psum>
.....,
......<ID:T>
......<ID:N>
.....->
......eq
.......<ID:N>
.......<INT:0>
......<INT:0>
......+
.......gamma
........<ID:Psum>
........tau
.........<ID:T>
.........-
..........<ID:N>
..........<INT:1>
.......gamma
........<ID:T>
........<ID:N>
.gamma
..<ID:Print>
..gamma
...<ID:Sum>
...tau
....<INT:1>
....<INT:2>
....<INT:3>
....<INT:4>
....<INT:5>
```

### 🧮 Output without switches
```
15
```
---

