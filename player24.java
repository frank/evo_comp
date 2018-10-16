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
        double Fstd = 1;
        double CRstd = 0.0;
        double F_lowerB = 0.0;
        double F_upperB = 1.0;

        // init population
        Population.populationSize = 140;
        Population.maxEvals=evaluations_limit_;
        int sameplesize=2; // determines the amount of parents when useing uniform initialization

        ArrayList<Population> generations = new ArrayList<Population>();
        Population pop = new Population(rnd_);
        pop.initPop(sameplesize);
        pop.evalPopulation(evaluation_);
        generations.add(pop);
        while (Population.evals < evaluations_limit_) {
            Population mutantpopulation = new Population(rnd_);
            Population old_pop = generations.get(generations.size() - 1);

            double evalProgress = (double)Population.evals/(double)evaluations_limit_;
            double F = rnd_.nextGaussian()*Fstd*evalProgress + evalProgress;
            while (F < F_lowerB || F > F_upperB){
                F = rnd_.nextGaussian()*Fstd*evalProgress + evalProgress;
            }
            double CR = rnd_.nextGaussian()*CRstd + evalProgress;
            while (CR < 0.0 || CR > 1.0){
                CR = rnd_.nextGaussian()*CRstd + evalProgress;
            }
            for (int idx = 0; (idx < old_pop.children.size() ) && Population.evals<evaluations_limit_; idx++) {
                Child[] donor= old_pop.selectRandomParents(idx);
                Child parent = old_pop.getChild(idx);

                Child child = pop.CreateDifferentialChild(donor,parent,F,CR);
                Double fitness = (double) evaluation_.evaluate(child.getValues());
                child.setFitness(fitness);
                Population.evals++;
                if(fitness>parent.getFitness()){mutantpopulation.AddChild(child);}
                else{mutantpopulation.AddChild(parent);}
            }
            generations.add(mutantpopulation);
        }
    }
}
