javac -cp contest.jar player24.java Population.java Child.java
jar cmf MainClass.txt submission.jar player24.class Population.class Child.class                    
java -jar testrun.jar -submission=player24 -evaluation=BentCigarFunction -seed=1
