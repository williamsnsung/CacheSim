
# Compilation Instructions
- This practical uses the GSON-2.10.1 library to parse JSONs, the JAR is included in this folder, otherwise you can download it here:
- - https://search.maven.org/artifact/com.google.code.gson/gson/2.10.1/jar
- GSON is a required dependency, therefore this JAR needs to be added to your classpath
- You can do this by executing the following code
export CLASSPATH=.:$(pwd)/gson-2.10.1.jar
- This will only work if you are in the working directory where the JAR is located, change $(pwd) to the path where the file is stored otherwise
- To compile, simply run 
javac *.java
- In the directory of the file
- To run the code, execute the following:
java -XX:+UseParallelGC -XX:+AlwaysPreTouch -Xmx1G -Xms1G Main <Path to JSON configuration> <Path to Trace File>
- Below is an example of this
java -XX:+UseParallelGC -XX:+AlwaysPreTouch -Xmx1G -Xms1G Main sample-inputs/l1l2l3.json trace-files/xz.out

