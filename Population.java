

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by Joseph on 9/8/2018.
 */

public class Population {
    private Random _rnd;
    private ArrayList<Child> children = new ArrayList<>();
    private int populationSize;
    private int evals;
    private int maxEvals;
    private double TIME;
    private double stDevMultiplier;
    private String mutationType;
    private String parentSelectionType;
    private int numberOfParents;

   	static final String MAX = "max";
   	static final String BOLTZMAN = "boltzman";
   	static final String RANDOM ="random";

    public Population(Random rnd, int populationSize, double time, double stDevMultiplier, int maxEvals,
                      String mutationType, String parentSelectionType, int numberOfParents) {
        _rnd = rnd;
        this.populationSize = populationSize;
        this.maxEvals = maxEvals;
        this.mutationType = mutationType;
        this.TIME = time;
        this.stDevMultiplier = stDevMultiplier;
        this.parentSelectionType = parentSelectionType;
        this.numberOfParents = numberOfParents;
        for (int i = 0; i < populationSize; i++) {
            children.add(new Child(_rnd));
        }
        PrintProperties();
    }




    private void PrintProperties() {
        System.out.println("\nSimulation properties:");
        System.out.println("--------------------------------------------------------");
        System.out.println("Population size: " + populationSize);
        System.out.println("Maximum evaluations: " + maxEvals);
        System.out.println("Boltzman TIME variable: " + TIME);
        System.out.println("Mutation type: " + mutationType);
        if (mutationType.equals("Gaussian")) {
            System.out.println("Gaussian Standard Deviation: " + stDevMultiplier);
        }
        System.out.println("--------------------------------------------------------\n");
    }

    public Child[] SelectParents(){
        Child[] parents;
        switch (parentSelectionType) {
            case MAX:
                parents = SelectMaxParents();
            case BOLTZMAN:
                parents = SelectBoltzmannParents();
            default:
            parents = selectRandomParents();
        }
        return parents;
    }

    public Child[] SelectMaxParents() {
        if (children.size() < 2) {
            return new Child[]{children.get(0)};
        }
        Child[] maxparents = new Child[numberOfParents];
        for (int i = 0; i < numberOfParents; i++) maxparents[i] = children.get(i);
        return maxparents;
    }

    public Child[] SelectBoltzmannParents() {
        //https://www.rug.nl/research/portal/files/61756372/ICAART_2018_27.pdf
        if (children.size() < 2) {
            return new Child[]{children.get(0)};
        }

        double T = Math.max(0.5, TIME / (double) evals);
        double[] probabilities = new double[children.size()];

        double denom = 0;
        for (Child c : children) {
            denom += Math.exp(c.getFitness() / T);
        }

        for (int i = 0; i < children.size(); i++) {
            Child child = children.get(i);
            //devide by 10 to avoid scaling issues
            double numerator = Math.exp((child.getFitness()) / T);
            probabilities[i] = numerator / denom;
        }
        //System.out.println(Arrays.toString(probabilities));
        double cumsum = 0;
        double[] cumsumprobability = new double[children.size()];
        for (int idx = 0; idx < children.size(); idx++) {
            cumsum += probabilities[idx] * 100;
            cumsumprobability[idx] = cumsum;
        }

        Child[] parents = new Child[numberOfParents];

        for (int i = 0; i < numberOfParents; i++) {
            double randomValue = _rnd.nextDouble() * 100;
            get_child:
            {
                for (int idx = 0; idx < children.size(); idx++) {
                    if (randomValue < cumsumprobability[idx]) {
                        parents[i] = children.get(idx);
                        break get_child;
                    }
                }
                parents[i] = children.get(0);
            }

        }
        if (parents[0] == null || parents[1] == null) {
            System.out.println();
        }
        return parents;
    }

    public Child[] selectRandomParents(){
    	int[] parents_int = new int[numberOfParents];
    	Child[] parents = new Child[numberOfParents];

    	for(int i=0;i<numberOfParents;i++){
    		while(true){
    			int idx = _rnd.nextInt(children.size());
    			if(IntStream.of(parents_int).anyMatch(x -> x == idx));
    			else{parents_int[i]=idx;
    				break;
    			}
    		}
    	}
    	for(int i=0;i<numberOfParents;i++){
    		parents[i] = children.get(i);
    	}
    	return parents;
    }




    public Child CreateChild(Child[] parents) {
        // CrossOver
        Child child = UniformCrossover(parents);
        //Mutation
        switch (mutationType) {
            case "Uniform":
                child = SimpleRandomAdditionMutation(child);
            case "Gaussian":
                NormalDistMutation(child);
        }
        return child;
    }


    public Child UniformCrossover(Child[] parents) {

        //select random crossover points from all parents
        //we apply random crossover now
        double[] vals = new double[Child.VALUES_SIZE];
        for (int i = 0; i < Child.VALUES_SIZE; i++) {
            vals[i] = parents[_rnd.nextInt(parents.length)].getValues(i);
        }
        return new Child(vals, _rnd);
    }

    public Child SimpleRandomAdditionMutation(Child child) {
        //adds random mutation values at random locations
        _rnd.nextDouble();
        double[] vals = new double[Child.VALUES_SIZE];
        for (int i = 0; i < Child.VALUES_SIZE; i++) {
            if (_rnd.nextDouble() > 0.8) {
                double val = child.getValues(i) + child.random_bounded_value();
                double rebound = child.rebound(val);
                vals[i] = rebound;
            } else {
                vals[i] = child.getValues(i);
            }
        }
        return new Child(vals, _rnd);
    }

    public void NormalDistMutation(Child child) {
        Random rand = new Random();
        double evalPercentRemaining = ((double) evals - maxEvals) / maxEvals;
        double stDev = evalPercentRemaining * stDevMultiplier;
        for (int i = 1; i < child.getValuesSize(); i++) {
            double mutation = rand.nextGaussian() * stDev;
            double newValue = child.getValues(i) + mutation;
            newValue = child.rebound(newValue);
            child.setValues(i, newValue);
        }
    }

    public void AddChild(Child child) {

        int left, right, mid;
        left = 0;
        right = children.size();

        //mergesort
        while (left < right) {
            mid = (left + right) / 2;
            double result = child.getFitness() - children.get(mid).getFitness();
            if (result > 0) { //If e is lower
                right = mid;
            } else { //If e is higher
                left = mid + 1;
            }
        }
        if (children.size() < populationSize) children.add(left, child);
        else if (left < populationSize) children.set(left, child);//Drop everything after 1k
    }

    public void SetEvals(int evals) {
        this.evals = evals;
    }

}

