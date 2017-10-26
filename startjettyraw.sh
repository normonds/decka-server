#echo "hello"
#JAVA_HOME='/java'
PATH="$PATH:/$JAVA_HOME/bin"
java -version $@
java -cp ./target/helloworld/WEB-INF/classes/*:./target/helloworld/WEB-INF/lib/* DeckaServer $@

#java -cp "./target/helloworld/WEB-INF/classes/:./target/helloworld/WEB-INF/lib/*" DeckaServer