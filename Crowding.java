import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.Properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.text.NumberFormat;

/* 
	Class Crowding has a population, initialized in the player24 class.
	It has the ContestEvaluation object. 


*/

public class Crowding
{
	private ArrayList<Child[]> parents = new ArrayList<>();
	private ContestEvaluation evaluation_;
	private Population pop;
	private Random _rnd;

	public Crowding(Population pop, ContestEvaluation evaluation_, Random rnd)
	{	
		_rnd = rnd;
		this.pop = pop;
		this.evaluation_ = evaluation_;
	}

	//Interface: This function is called from the player24 class in the run() function
	public void nextGeneration()
	{
		//1. Create an ArrayList of parent pairs

		//2. 
	}	

	private void pairRandomParents()
	{

	}

	// private void 




}