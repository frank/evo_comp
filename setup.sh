#!/bin/sh
rm player24.class Population.class Child.class
javac -cp contest.jar player24.java Population.java Child.java
jar cmf MainClass.txt submission.jar player24.class Population.class Child.class
