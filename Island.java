public class Island extends Population {

    private Random _rnd;
    private ArrayList<Child> children = new ArrayList<>();
    private int populationSize;
    static int evals = 0;
    private int maxEvals;
    private double TIME;
    private double stDevMultiplier;
    private String mutationType;
    private String parentSelectionType;
    private int numberOfParents;

}