java -XX:+UseParallelGC -XX:+AlwaysPreTouch -Xmx1G -Xms1G Main sample-inputs/l1l2l3.json trace-files/xz.out
export CLASSPATH=.:$(pwd)/gson-2.10.1.jar