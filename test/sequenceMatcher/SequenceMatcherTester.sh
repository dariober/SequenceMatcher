#!/bin/bash

# * Compile jar file SequenceMatcher.jar and put path in JAR variable.
# * Change dir to /.../SequenceMatcher/trunk/test/sequenceMatcher/

cd ~/svn_git/SequenceMatcher/trunk/test/sequenceMatcher/
JAR="/Users/berald01/Tritume/SequenceMatcher.jar"

java -jar $JAR -h

java -jar $JAR match -h

java -jar $JAR match -a ../seqs.fa

java -jar $JAR match -a ../seqs.fa -b ../seqs.fa