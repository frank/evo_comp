import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.util.Arrays;
import java.util.Random;
import java.util.Properties;

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
        int evals = 0;
        // init population
        // calculate fitness
        System.out.println("test");
        Population pop = new Population(rnd_);
        while (evals < evaluations_limit_) {
            Child[] parents = pop.SelectBoltzmanParents(2,evals);
            //creating the child
            Child child = pop.UniformCrossover(parents);
            child = pop.SimpleRandomAdditionMutation(child);

            //calculating fitness
            Double fitness = (double) evaluation_.evaluate(child.getValues());
            child.setFitness(fitness);
            //System.out.println(child.getFitness());
            //System.out.println(Arrays.toString(child.getValues()));

            pop.AddChild(child);

            evals++;
            // Select survivors
        }

    }
}
