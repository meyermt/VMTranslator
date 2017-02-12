# VM Translator

This program will take an input file of HACK VM code and output an assembly code file that is ready to run in a HACK CPUEmulator.

## How to Run the Program

A pre-requisite to running this program is having Java 8 installed on whichever system it runs on. After ensuring it is installed, please follow these instructions for compiling and running the program.

Another requirement for running this project is that it be kept in the same folder structure as it exists. The commands below should be run from the root folder "MeyerMichaelProject<#>", and all sub-directories within that project must be maintained as-is.

1. Unzip the contents of the .zip file
2. Enter `javac -d bin src/main/java/com/meyermt/vm/*.java` from the project root directory to compile the program
3. Enter `java -cp bin com.meyermt.vm.Main <filename.vm>` from the project root directory to run the program. Please note that the file MUST have the ".vm" extension in order to be run through the program. You can use absolute or relative paths to specify the location of the input file.
4. The program will produce a file with an ".asm" extension in the same directory as the input file.
5. Load the output file into the CPUEmulator and enjoy.