import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.Properties;
import java.util.ArrayList;

public class player24 implements ContestSubmission {
    Random rnd_;
    ContestEvaluation evaluation_;
    private int evaluations_limit_;

    public player24() {
        rnd_ = new Random();
    }

    public void setSeed(long seed) {
        // Set seed of algortihms random process
        rnd_.setSeed(seed);
    }

    //    public static void main(String[] args) {
    //        Population pop = new Population(new Random());
    //        Child child = new Child(new Random());
    //        System.out.println("SchaffersEvaluation");ConsertTestBox.main(new String[]{"-submission=player24", "-evaluation=SchaffersEvaluation", "-seed=1"});
    //        //System.out.println("KatsuuraEvaluation");ConsertTestBox.main(new String[]{"-submission=player24", "-evaluation=KatsuuraEvaluation", "-seed=1"});
    //        //System.out.println("BentCigarFunction");ConsertTestBox.main(new String[]{"-submission=player24", "-evaluation=BentCigarFunction", "-seed=1"});
    //
    //    }

    public void setEvaluation(ContestEvaluation evaluation) {
        // Set evaluation problem used in the run
        evaluation_ = evaluation;


        // Get evaluation properties
        Properties props = evaluation.getProperties();
        // Get evaluation limit
        evaluations_limit_ = Integer.parseInt(props.getProperty("Evaluations"));
        // Property keys depend on specific evaluation
        // E.g. double param = Double.parseDouble(props.getProperty("property_name"));
        boolean isMultimodal = Boolean.parseBoolean(props.getProperty("Multimodal"));
        boolean hasStructure = Boolean.parseBoolean(props.getProperty("Regular"));
        boolean isSeparable = Boolean.parseBoolean(props.getProperty("Separable"));

        // Do sth with property values, e.g. specify relevant settings of your algorithm
        if (isMultimodal) {
            // Do sth
        } else {
            // Do sth else
        }
    }



    public void run() {
        // Run your algorithm here
        int populationSize = 100;
        double time = 100;
        double stDevMultiplier = 1.0;
        int numberOfParents = 4;
        String mutationType = Population.GAUSSIAN; // Set to 'Uniform' or 'Gaussian'
        String parentSelectionType = Population.RANDOM; // Boltzmann, Max


        // init population
        ArrayList<Population> generations = new ArrayList<Population>();
        Population pop = new Population(rnd_, populationSize, time, stDevMultiplier, evaluations_limit_,
                mutationType, parentSelectionType, numberOfParents);
        pop.initPop();
        pop.evalPopulation(evaluation_);
        pop.PrintProperties();
        generations.add(pop);

        int papa=0;
        // the actual code
        while (Population.evals < evaluations_limit_) {
            Population mutantpopulation = new Population(rnd_, populationSize, time, stDevMultiplier, evaluations_limit_,
                    mutationType, parentSelectionType, numberOfParents);
            Population old_pop = generations.get(generations.size() - 1);

            for (int idx = 0; (idx < populationSize) && Population.evals<evaluations_limit_; idx++) {
                Child[] parents = old_pop.SelectParents();
                Child child = pop.CreateDifferentialChild(parents);
                Double fitness = (double) evaluation_.evaluate(child.getValues());
                child.setFitness(fitness);
                Population.evals++;
//
                System.out.println("papa:"+parents[0].getFitness());
                System.out.println("child:"+fitness);

                if(fitness>parents[0].getFitness()){mutantpopulation.AddChild(child);}
                else{mutantpopulation.AddChild(parents[0]);papa++;}
            }
            generations.add(mutantpopulation);
            System.out.println("papa added "+papa);
            papa=0;
        }

    }
}
