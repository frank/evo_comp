import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.Properties;
import java.util.ArrayList;

public class player24 implements ContestSubmission {
    Random rnd_;
    ContestEvaluation evaluation_;
    private int evaluations_limit_;

    private boolean isSchaffer;
    private boolean isKatsuura;
    private boolean isBC;

    public player24() {
        rnd_ = new Random();
        isSchaffer = false;
        isKatsuura = false;
        isBC = false;
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

        if(!isMultimodal && !hasStructure){
            isBC = true;
        }else if(isMultimodal && hasStructure){
            isSchaffer = true;
        }else if(isMultimodal){
            isKatsuura = true;
        }
    }

    public void run() {
        // Run your algorithm here
        double Fstd;
        double F_end;
        double F_start;
        double CRstd;
        double CR_start;
        double CR_end;
        double CR;

        if (isKatsuura){
            Fstd = 0.5;
            F_start = 0.0;
            F_end = 0.8;
            CRstd = 0.15;
            CR_start = 0.0;
            CR_end = 1.0;
            Population.populationSize = 100;//137
        }else if(isSchaffer){
            Fstd = 5.0;
            F_start = 0.0;
            F_end = 1.0;
            CRstd = 0.0;
            CR_start = 0.0;
            CR_end = 1.0;

            Population.populationSize = 100;//48
        }else{
             Fstd = 5.0;
             F_start = 0.0;
             F_end = 1.0;
             CRstd = 0.0;
             CR_start = 0.0;
             CR_end = 1.0;
             Population.populationSize = 100;//24
        }

        // init population
        Population.maxEvals=evaluations_limit_;
        int sameplesize=2; // determines the amount of parents when useing uniform initialization

        ArrayList<Population> generations = new ArrayList<Population>();
        Population pop = new Population(rnd_);
        

        pop.initPopUniform(sameplesize);
//        pop.initPopRandom();


        pop.evalPopulation(evaluation_);
        generations.add(pop);
        boolean foundMax = false;
        while (Population.evals < evaluations_limit_) {
            Population mutantpopulation = new Population(rnd_);
            Population old_pop = generations.get(generations.size() - 1);

            double evalProgress = (double)Population.evals/(double)evaluations_limit_;

            double F = rnd_.nextGaussian()*Fstd*evalProgress + (F_end-F_start)*evalProgress + F_start;

             do{
                 CR = rnd_.nextGaussian()*CRstd*(1-evalProgress) + (CR_end-CR_start)*evalProgress + CR_start;
             }while (CR < 0.0 || CR > 1.0);
             for (int idx = 0; (idx < old_pop.children.size() ) && Population.evals<evaluations_limit_; idx++) {
                Child[] donor= old_pop.selectRandomParents(idx);
                Child parent = old_pop.getChild(idx);

                Child child = pop.CreateDifferentialChild(donor,parent,F,CR);
                Double fitness = (double) evaluation_.evaluate(child.getValues());
                child.setFitness(fitness);
                Population.evals++;
//                if (fitness >= 10.000){
//                    return;
//                }
                if(fitness>parent.getFitness()){mutantpopulation.AddChild(child);}
                else{mutantpopulation.AddChild(parent);}

            }
            generations.add(mutantpopulation);
        }
    }
}
