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
    static public int maxEvals;
    public static int populationSize;
    public static int numberOfParents=3;
    public Population(Random rnd) {
        _rnd = rnd; 
        this.maxEvals = maxEvals;
    }

    public void initPopRandom(){
        for(int i = 0; i < populationSize; i++) {
            children.add(new Child(_rnd));
        }
    }

    public void initPopUniform(int samplesize){
        double increment=10/(double)(samplesize+1);
        double[] vals = new double[10];
        for(int idx=0;idx<10;idx++){
            vals[idx]=-5;

        }
        //uniform initialization
        if(samplesize>0)generate_kid(0,samplesize,increment,vals);
        //random initialization
        else{
            for(int i = 0;i<populationSize;i++ )children.add(new Child(_rnd));
   		}
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

        System.out.println("--------------------------------------------------------\n");
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
    	for(int i=0;i<numberOfParents;i++){
    		parents[i] = children.get(parents_int[i]);
    	}
    	return parents;
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
    		peturbation_v = F*(y.getValues(idx)-z.getValues(idx)) ;
    		mutant_v[idx] = Math.min(Child.MAX,Math.max(Child.MIN,   x.getValues(idx) - peturbation_v));
    	}
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

    public void evalPopulation(ContestEvaluation evaluation_){
        //reevaluate the complete population
        for(int i = 0; i < populationSize; i++){
            children.get(i).setFitness((double) evaluation_.evaluate(children.get(i).getValues()));
            evals++;
        }
    }
}