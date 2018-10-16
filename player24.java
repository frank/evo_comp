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
        Population.populationSize = 90;
        int sameplesize=2;
        double time = 1000;
        double stDevMultiplier = 1.0;
        int numberOfParents = 3;

        String mutationType = Population.GAUSSIAN; // Set to 'Uniform' or 'Gaussian'
        String parentSelectionType = Population.RANDOM; // Boltzmann, Max
        double Fstd = 0.8;
        double CRstd = 0.1;

        // init population
        ArrayList<Population> generations = new ArrayList<Population>();
        Population pop = new Population(rnd_,stDevMultiplier, evaluations_limit_,
                mutationType, parentSelectionType, numberOfParents);
        pop.initPop(sameplesize);
        pop.evalPopulation(evaluation_);
        // pop.PrintProperties();
        generations.add(pop);

        int papa=0;
        // the actual code
        while (Population.evals < evaluations_limit_) {
            Population mutantpopulation = new Population(rnd_, stDevMultiplier, evaluations_limit_,
                    mutationType, parentSelectionType, numberOfParents);
            Population old_pop = generations.get(generations.size() - 1);

            double F = rnd_.nextGaussian()*Fstd + (double)Population.evals/(double)evaluations_limit_;
            while (F < 0.0 || F > 1.0){
                F = rnd_.nextGaussian()*Fstd + (double)Population.evals/(double)evaluations_limit_;
            }
            double CR = 0.76;
            while (CR < 0.0 || CR > 1.0){
                CR = rnd_.nextGaussian()*CRstd + (double)Population.evals/(double)evaluations_limit_;
            }
            for (int idx = 0; (idx < old_pop.children.size() ) && Population.evals<evaluations_limit_; idx++) {
                Child[] donor= old_pop.selectRandomParents(idx);
                Child parent = old_pop.getChild(idx);

                Child child = pop.CreateDifferentialChild(donor,parent,F,CR);
                Double fitness = (double) evaluation_.evaluate(child.getValues());
                child.setFitness(fitness);
                Population.evals++;

//              System.out.println("papa:"+parent.getFitness());
//              System.out.println("child:"+fitness);

                if(fitness>parent.getFitness()){mutantpopulation.AddChild(child);}
                else{mutantpopulation.AddChild(parent);papa++;}
            }
            generations.add(mutantpopulation);

			//F = -F-((papa-populationSize/2)/(double) populationSize);
            //System.out.println("papa added " + papa);
            //System.out.println("F: "+F);
//            if(papa==populationSize){
//                System.out.println("evals"+Population.evals);
//                System.out.println("quitting");
//                return;
//            }
            papa=0;
        }

    }
}
