import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import org.vu.contest.ContestEvaluation;

/**
 * Created by Joseph on 9/8/2018.
 */

public class Population {
    private Random _rnd;
    ArrayList<Child> children = new ArrayList<>();
    static int evals=0;
    private int maxEvals;
    static double TIME;
    private double stDevMultiplier;
    private String mutationType;
    private String parentSelectionType;
    private int numberOfParents;

   	static final String MAX = "max";
   	static final String BOLTZMAN = "boltzman";
   	static final String RANDOM ="random";
    public static final String GAUSSIAN = "gaussian";
    public static final String UNIFORM  = "uniform";
    public static int populationSize;

    public Population(Random rnd, double stDevMultiplier, int maxEvals,
                      String mutationType, String parentSelectionType, int numberOfParents) {
        _rnd = rnd; 
        this.maxEvals = maxEvals;
        this.mutationType = mutationType;
        this.stDevMultiplier = stDevMultiplier;
        this.parentSelectionType = parentSelectionType;
        this.numberOfParents = numberOfParents;
    }

    public void initPop(int samplesize){
    		double increment=10/(double)(samplesize+1);
        	double[] vals = new double[10];
        	for(int idx=0;idx<10;idx++){
        		vals[idx]=-5;            	
        	}
        	generate_kid(0,samplesize,increment,vals);

   		}


    public void generate_kid(int arr_idx,int max_increment,double increment,double[] vals){
    	if(arr_idx==10){
    		children.add(new Child(vals,_rnd));
    		return;
    	}


    	for(int i=0;i<max_increment;i++){
    		vals[arr_idx]+=increment;
    		generate_kid(arr_idx+1,max_increment,increment,Arrays.copyOf(vals, vals.length));
    	}

    }

    public void PrintProperties() {
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
                break;
            case BOLTZMAN:
                parents = SelectBoltzmannParents();
                break;
            default:
            	parents = selectRandomParents(0);
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

    public Child[] selectRandomParents(int not_idx){
    	int[] parents_int = new int[numberOfParents];
    	Child[] parents = new Child[numberOfParents];

    	for(int i=0;i<numberOfParents;i++){
    		while(true){
    			int idx = _rnd.nextInt(children.size());
    			if(idx==not_idx || IntStream.of(parents_int).anyMatch(x -> x == idx));
    			else{
    				parents_int[i]=idx;
    				break;
    			}
    		}

    	}
//        System.out.println(Arrays.toString(parents_int));
    	for(int i=0;i<numberOfParents;i++){
    		parents[i] = children.get(parents_int[i]);
    	}
    	return parents;
    }




    public Child CreateChild(Child[] parents) {
        // CrossOver
        Child child = UniformCrossover(parents);
        //Mutation
        switch (mutationType) {
            case UNIFORM:
                child = SimpleRandomAdditionMutation(child);
                break;
            case GAUSSIAN:
                child = NormalDistMutation(child);
                break;
        }
        return child;
    }

    public Child CreateDifferentialChild(Child[] donor, Child parent,double F,double RecombinationRate){
        int const_idx = _rnd.nextInt(10);
        Child x=donor[0];
        Child y=donor[1];
        Child z=donor[2];

    	double peturbation_v;
    	double[] mutant_v = new double[10];

    	//mutation
    	for(int idx=0;idx<10;idx++){
    		peturbation_v = F*(y.getValues(idx)-z.getValues(idx));
    		//System.out.println("peturbation " + peturbation_v);
    		mutant_v[idx] = Math.min(Child.MAX,Math.max(Child.MIN,   x.getValues(idx) - peturbation_v));
    	}
        //System.out.println(Arrays.toString(mutant_v));
    	//System.out.print("\n");
        //crossover
        double[] vals = new double[10];
        for(int idx=0;idx<10;idx++) {
            if (idx == const_idx || _rnd.nextDouble() <= RecombinationRate) {
                vals[idx] = mutant_v[idx];
            }else{
                vals[idx] = parent.getValues(idx);
            }
        }

    	return new Child(vals,_rnd);
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

    public Child NormalDistMutation(Child child) {
        Random rand =_rnd;
        double evalPercentRemaining = ((double) evals - maxEvals) / maxEvals;
        double[] vals = new double[10];
        double stDev = evalPercentRemaining * stDevMultiplier;
        for (int i = 1; i < child.getValuesSize(); i++) {
            double mutation = rand.nextGaussian() * stDev;
            double newValue = child.getValues(i) + mutation;
            newValue = child.rebound(newValue);
            vals[i]= newValue;
        }
        return new Child(vals,_rnd);
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



    public ArrayList<Child> getChildren()
    {
        return this.children;
    }

    public Child getChild(int idx){
        return children.get(idx);
    }


    public void Reinitialize(ContestEvaluation evaluation_,int amount,int limit){


    	 for(int i = populationSize-amount; i < populationSize && evals<limit;i++ ){
            children.set(i,new Child(_rnd));
            children.get(i).setFitness((double) evaluation_.evaluate(children.get(i).getValues()));
            evals++;
        }

    }


    public void evalPopulation(ContestEvaluation evaluation_)
    {   
        //Remember to increment evals!

        //1. For each child in population
        for(int i = 0; i < populationSize; i++){
            children.get(i).setFitness((double) evaluation_.evaluate(children.get(i).getValues()));
            evals++;
        }
    }

    public int getMaxEvals(){return maxEvals;}

    public int getEvals(){return evals;}
}