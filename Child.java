import java.util.Random;

/**
 * Created by Joseph on 9/8/2018.
 */


public class Child {
    private double[] _values;
    final static double MIN = -5.0;
    final static double MAX = 5.0;
    final static int VALUES_SIZE = 10;
    private Random _rnd;
    private double fitness = 0;

    public double random_bounded_value() {
        return MIN + (MAX - MIN) * _rnd.nextDouble();
    }

    public double rebound(double val) {
        return (val - MIN) % (MAX - MIN) + MIN;
    }

    public Child(Random rnd) {
        _rnd = rnd;
        _values = new double[10];
        for (int i = 0; i < 10; i++) {
            _values[i] = MIN + (MAX - MIN) * rnd.nextDouble();
        }
    }

    public Child(double values[], Random rnd) {
        this._rnd = rnd;
        _values = values;
    }

    public double[] getValues() {
        return _values;
    }

    public double getValues(int index) {
        return _values[index];
    }

    public int getValuesSize() {return VALUES_SIZE;}

    public void setValues(int index, double value) {
        _values[index] = value;
    }

    public void setValues(double[] values) {
        _values = values;
    }

    public Double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getMin() {
        return MIN;
    }

    public double getMax() {
        return MAX;
    }
}
