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

    public Child selectCandidateForEval(ArrayList<Child> history, Population pop, double F, double CR, Child[] donor, Child parent){
        double radiusEnd = 5.0;
        double radiusStart = 1.0;
        double radiusGradient = radiusStart-radiusEnd;
        double historyRadius =radiusGradient*pop.getEvals()/pop.getMaxEvals() + radiusEnd;
        int rolloutNumber = 10;
        Population rolloutPop = new Population(rnd_, 0, evaluations_limit_,
                null, null, 0);
        boolean noHistory = false;
        for (int i = 0; i < rolloutNumber; i++){

            Child candidate = pop.CreateDifferentialChild(donor,parent,F,CR);
            double averageRadiusFitness = 0.0;
            double foundNum = 0;
            for (Child c : history){
                if (candidate.getPythagoreanDistance(c.getValues()) < historyRadius){
                    averageRadiusFitness += c.getFitness();
                    foundNum += 1.0;
                }
                if (foundNum == 0){
                    noHistory = true;
                    break;
                }else{
                    candidate.setFitness(averageRadiusFitness / foundNum);
                }
            }
            rolloutPop.AddChild(candidate);
            if (noHistory){
                break;
            }
        }
        Child highest;
        if (noHistory) {
            highest = rolloutPop.getChild(rolloutPop.getChildren().size()-1);
        }else{
            highest = rolloutPop.getChild(0);
            for (int i = 1; i < rolloutNumber; i++){
                if (highest.getFitness() <= rolloutPop.getChild(1).getFitness()){
                    highest = rolloutPop.getChild(1);
                }
            }
        }
        return highest;
    }



    public void run() {
        // Run your algorithm here
        Population.populationSize = 120;
        int sameplesize=2;
        double time = 1000;
        double stDevMultiplier = 1.0;
        int numberOfParents = 3;

        ArrayList<Child> history = new ArrayList<>();

        String mutationType = Population.GAUSSIAN; // Set to 'Uniform' or 'Gaussian'
        String parentSelectionType = Population.RANDOM; // Boltzmann, Max
        double F = 0.4;
//        double CR = 0.7;

        // init population
        ArrayList<Population> generations = new ArrayList<Population>();
        Population pop = new Population(rnd_,stDevMultiplier, evaluations_limit_,
                mutationType, parentSelectionType, numberOfParents);
        pop.initPop(sameplesize);
        pop.evalPopulation(evaluation_);
        // pop.PrintProperties();
        generations.add(pop);

        history.addAll(pop.getChildren());

        int papa=0;
        // the actual code
        while (Population.evals < evaluations_limit_) {
            Population mutantpopulation = new Population(rnd_, stDevMultiplier, evaluations_limit_,
                    mutationType, parentSelectionType, numberOfParents);
            Population old_pop = generations.get(generations.size() - 1);

            F = rnd_.nextDouble();
            double CR = (double)Population.evals/(double)evaluations_limit_; 
            for (int idx = 0; (idx < old_pop.children.size() ) && Population.evals<evaluations_limit_; idx++) {
                Child[] donor= old_pop.selectRandomParents(idx);
                Child parent = old_pop.getChild(idx);
//                Child child = pop.CreateDifferentialChild(donor,parent,F,CR);

                Child child = selectCandidateForEval(history, pop, F, CR, donor, parent);
                Double fitness = (double) evaluation_.evaluate(child.getValues());
                child.setFitness(fitness);
                history.add(child);
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
