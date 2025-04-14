# 💻 RPAL Lexical Analyzer and Parser Project

Welcome to the **RPAL Analyzer Project**! This README will walk you through the purpose, structure, and usage of the project, which includes a **Lexical Analyzer**, **Parser**, **Standardizer**, and **CSE Machine** for the **RPAL (Right-reference Programming Algorithmic Language)**.

---

## 📜 Table of Contents

1. [📘 What is RPAL?](#-what-is-rpal)
2. [🔍 Lexical Analyzer & Parser](#-lexical-analyzer--parser)
3. [📂 Project Structure](#-project-structure)
4. [📄 Java Files Explained](#-java-files-explained)
5. [⚙️ How to Run the Project](#how-to-run-the-project)
6. [🧪 Sample Input/Output](#-sample-inputoutput)

---

## 📘 What is RPAL?

RPAL stands for **Right-reference Programming Algorithmic Language**. It is a simplified functional programming language used primarily in academic settings to explore:

- Lexical and syntactic structures
- Abstract Syntax Trees (AST)
- Evaluation using Control Stack Environment (CSE) machines

The goal of this project is to implement tools to analyze and evaluate RPAL code without relying on external tools like lex/yacc.

---

## 🔍 Lexical Analyzer & Parser

### 🧠 Lexical Analyzer

The **Lexical Analyzer** processes an RPAL program and breaks it down into meaningful tokens like keywords, identifiers, literals, and symbols.

### 🧠 Parser

The **Parser** uses the tokens from the lexical analyzer to create an **Abstract Syntax Tree (AST)** based on RPAL grammar. This AST is further **standardized** before being passed to the **CSE Machine** for evaluation.

---

## 📂 Project Structure

```
lex-analyzer-parser
├── .idea
├── out
├── src
│   ├── csemachine
│   │   ├── CSEMachine.java
│   │   └── Environment.java
│   ├── lexical
│   │   ├── LexicalAnalyzer.java
│   │   └── Token.java
│   ├── parser
│   │   ├── Node.java
│   │   └── Parser.java
│   ├── standardizer
│   │   └── Standardizer.java
│   ├── structures
│   │   ├── Delta.java
│   │   ├── Eta.java
│   │   ├── Lambda.java
│   │   └── Tau.java
│   └── myrpal.java
├── Tests
│   ├── testfile1
│   └── testfile2
├── .gitignore
├── .gitattributes
├── RPAL.iml
└── README.md
```

---

## 📄 Java Files Explained

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


## ⚙️ How to Run the Project


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

## 🧪 Sample Input/Output

### 📝 Input
```
let Sum(A) = Psum (A,Order A )
where rec Psum (T,N) = N eq 0 -> 0 | Psum(T,N-1)+T N
in Print ( Sum (1,2,3,4,5) )
```

### 🌳 Output with `-ast`
```
let
.function_form ..<ID:Sum>
..<ID:A>
..where
...gamma
....<ID:Psum> ....tau
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
.gamma ..<ID:Print> ..gamma ...<ID:Sum> ...tau
....<INT:1> ....<INT:2> ....<INT:3> ....<INT:4>....<INT:5>
```

### 📤 Output without `-ast`
```
Output of the above program is:
15
```

---

