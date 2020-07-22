# CCompiler
C Compiler to Linux x64 assembly and to [LaserLang](https://github.com/Quintec/LaserLang) written in Java

## About
This is a simple c compiler written in Java. Based on the blog series [Writing a C Compiler](https://norasandler.com/2017/11/29/Write-a-Compiler.html) by Nora Sandler.
It currently supports a very small subset of C:
- Functions
- Local and Global variables
- All different types of binary/unary math and logic operators
- Flow control (Ifs, Loops)
- Integer types only (The return of void functions is also int for now)

The code quality is horrible, this is just about learning about how to write a compiler.

## LaserLang
There is also an option to compile to LaserLang, the option is a boolean variable in the main function.
When compiling for LaserLang, you can use the following functions for I/O:
```java
int putchar(int chr);
int putint(int i);
int getint();
```
* Note: You must declare the IO functions for them to be included in the generated code.

## Usage
Compile the source, and then
```shell
java -jar CCompiler.jar filename.c
```
Use the `-d` flag for debug output.
