import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.Properties;

public class player24 implements ContestSubmission {
    Random rnd_;
    boolean _isMultimodal;
    boolean _isRegular;
    boolean _isSeparable;
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
            // do smth
        }
    }

    public void run() {
        // Run your algorithm here
        int evals = 0;
        int populationSize = 50;
        double time = 100;
        double stDevMultiplier = 3.0;
        int numberOfParents = 2;
        String mutationType = Population.GENE_GAUSSIAN; // Set to 'UNIFORM', 'GAUSSIAN', or 'GENE_GAUSSIAN'
        String parentSelectionType = Population.BOLTZMANN; // Boltzmann, Max

        // init population
        Population pop = new Population(rnd_, populationSize, time, stDevMultiplier, evaluations_limit_,
                                        mutationType, parentSelectionType, numberOfParents);
        //Create new random children
        pop.initPop();
        //Evaluate and set fitness for all children 
        pop.evalPopulation(evaluation_);
        //This function sorts all children based on fitness
        pop.sortOnFitness();

        //Crowding
        Crowding crowding = new Crowding(pop, evaluation_, rnd_);

        while (Population.evals < evaluations_limit_) {
            Child[] parents = pop.SelectParents();
//            pop.printChildren(parents);

            //creating the child
            Child child = pop.CreateChild(parents);
            //calculating fitness
            Double fitness = (double) evaluation_.evaluate(child.getValues());
            child.setFitness(fitness);
//            Child[] c = new Child[1];
//            c[0] = child;
//            pop.printChildren(c);

            //System.out.println(child.getFitness());
            //System.out.println(Arrays.toString(child.getValues()));

            pop.AddChild(child);
//            pop.printPopulation();
//            if (Population.evals == populationSize+ 200){
//                System.exit(0);
//            }

            Population.evals++;
            // Select survivors
        }
    }
}
