import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.Properties;
import java.util.ArrayList;

public class player24 implements ContestSubmission {
    Random rnd_;
    ContestEvaluation evaluation_;
    private int evaluations_limit_;
    private ArrayList<Integer> foundNeighbor;

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


    public Child selectCandidateForEval(ArrayList<Child> history, Population pop, double F, double CR, Child[] donor, Child parent){
        double radiusEnd = 1.0;
        double radiusStart = 5.0;
        double radiusGradient = radiusStart-radiusEnd;
        double historyRadius =radiusGradient*pop.getEvals()/pop.getMaxEvals() + radiusEnd;

        int rolloutNumber = 10;
        Population rolloutPop = new Population(rnd_);
        boolean noHistory = false;
        for (int i = 0; i < rolloutNumber; i++){

            Child candidate = pop.CreateDifferentialChild(donor,parent,F,CR);
            double averageRadiusFitness = 0.0;
            double foundNum = 0;
            for (Child c : history) {
                if (candidate.getPythagoreanDistance(c.getValues()) < historyRadius) {
                    averageRadiusFitness += c.getFitness();
                    foundNum += 1.0;
                }
            }
            if (foundNum == 0){
                noHistory = true;
            }else{
                candidate.setFitness(averageRadiusFitness / foundNum);
            }
            rolloutPop.AddChild(candidate);
            if (noHistory){
                break;
            }
        }
        if (noHistory){
            foundNeighbor.add(0);
        }else{
            foundNeighbor.add(1);
        }
        Child highest;
        if (noHistory) {
            highest = rolloutPop.getChild(rolloutPop.getChildren().size()-1);
        }else{
            highest = rolloutPop.getChild(0);
            for (int i = 1; i < rolloutNumber; i++){
                System.out.println(rolloutPop.getChildren().size());
                if (highest.getFitness() <= rolloutPop.getChild(i).getFitness()){
                    highest = rolloutPop.getChild(i);
                }
            }
        }
        return highest;
    }



    public void run() {
        // Run your algorithm here
        double Fstd = 0.8;
        double CRstd = 0.1;
        ArrayList<Child> history = new ArrayList<Child>();

        // init population
        Population.populationSize = 90;
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

            double F = rnd_.nextGaussian()*Fstd + (double)Population.evals/(double)evaluations_limit_;
            while (F < 0.0 || F > 1.0){
                F = rnd_.nextGaussian()*Fstd + (double)Population.evals/(double)evaluations_limit_;
            }
            double CR = rnd_.nextGaussian()*CRstd + (double)Population.evals/(double)evaluations_limit_;
            while (CR < 0.0 || CR > 1.0){
                CR = rnd_.nextGaussian()*CRstd + (double)Population.evals/(double)evaluations_limit_;
            }
            for (int idx = 0; (idx < old_pop.children.size() ) && Population.evals<evaluations_limit_; idx++) {
                Child[] donor= old_pop.selectRandomParents(idx);
                Child parent = old_pop.getChild(idx);
//                Child child = pop.CreateDifferentialChild(donor,parent,F,CR);

                Child child = selectCandidateForEval(history, pop, F, CR, donor, parent);
                Double fitness = (double) evaluation_.evaluate(child.getValues());
                child.setFitness(fitness);
                history.add(child);
                Population.evals++;
                if(fitness>parent.getFitness()){mutantpopulation.AddChild(child);}
                else{mutantpopulation.AddChild(parent);}
            }
            generations.add(mutantpopulation);
        }
    }
}