#!/bin/bash 

cd /Users/jzhang621/Documents/Berkeley/CS162/projects/Project3/src/edu/berkeley/cs162/

javac *.java 

if [ $? -eq 0 ]
then  
    cd /Users/jzhang621/Documents/Berkeley/CS162/projects/Project3/src
    if test $1 = "KVCache" ; then 
        echo "Results will be written to results.xml"
        java edu.berkeley.cs162.$1 > results.xml
    else 
        java edu.berkeley.cs162.$1
    fi
fi
