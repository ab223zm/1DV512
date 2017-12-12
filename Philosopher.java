import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Philosopher implements Runnable {
	
	private int id;
	
	private final ChopStick leftChopStick;
	private final ChopStick rightChopStick;
	
	private Random randomGenerator = new Random();
	
	private int numberOfEatingTurns = 0;
	private int numberOfThinkingTurns = 0;
	private int numberOfHungryTurns = 0;

	private double thinkingTime = 0;
	private double eatingTime = 0;
	private double hungryTime = 0;
	
	//help variables that will measure the hunger tiem of the philosophers
	private double startHunger;
	private double endHunger;
	
	//help variable that will only be true when a philosopher is done eating and that can't be changed more than once
	private volatile boolean fed = false;
	
	public Philosopher(int id, ChopStick leftChopStick, ChopStick rightChopStick, int seed) {
		this.id = id;
		this.leftChopStick = leftChopStick;
		this.rightChopStick = rightChopStick;
		
		/*
		 * set the seed for this philosopher. To differentiate the seed from the other philosophers, we add the philosopher id to the seed.
		 * the seed makes sure that the random numbers are the same every time the application is executed
		 * the random number is not the same between multiple calls within the same program execution 
		 
		 * NOTE
		 * In order to get the same average values use the seed 100, and set the id of the philosopher starting from 0 to 4 (0,1,2,3,4). 
		 * Each philosopher sets the seed to the random number generator as seed+id. 
		 * The seed for each philosopher is as follows:
		 * 	 	P0.seed = 100 + P0.id = 100 + 0 = 100
		 * 		P1.seed = 100 + P1.id = 100 + 1 = 101
		 * 		P2.seed = 100 + P2.id = 100 + 2 = 102
		 * 		P3.seed = 100 + P3.id = 100 + 3 = 103
		 * 		P4.seed = 100 + P4.id = 100 + 4 = 104
		 * Therefore, if the ids of the philosophers are not 0,1,2,3,4 then different random numbers will be generated.
		 */
		
		randomGenerator.setSeed(id+seed);
	}
	public int getId() {
		return id;
	}
	
	//help method to calculate all the averages by dividing the total time to the number of turns
	public double average(int turn, double time){
		if (turn==0)
			return 0.0;
		else
			return(time/turn);
	}
	
	public double getAverageThinkingTime() {
		double result = average(numberOfThinkingTurns,thinkingTime);
		return result;
	}

	public double getAverageEatingTime() {
		double result = average(numberOfEatingTurns,eatingTime);
		return result;
	}

	public double getAverageHungryTime() {
		double result = average(numberOfHungryTurns,hungryTime);
		return result;
	}
	
	public int getNumberOfThinkingTurns() {
		return numberOfThinkingTurns;
	}
	
	public int getNumberOfEatingTurns() {
		return numberOfEatingTurns;
	}
	
	public int getNumberOfHungryTurns() {
		return numberOfHungryTurns;
	}

	public double getTotalThinkingTime() {
		return thinkingTime;
	}

	public double getTotalEatingTime() {
		return eatingTime;
	}

	public double getTotalHungryTime() {
		return hungryTime;
	}

	@Override
	public void run(){
		//we start to measure the hunger time with the help of System time
		startHunger = System.currentTimeMillis();
		//while the philosophers have not eaten/are not done eating
		while (!fed) {
			//first the philosophers thinks
			think();
			//then the philosopher gets hungry 
			hungry();
			//if deadlock found, then exit the system
			if (leftChopStick.getQueueLength()>0)
				System.exit(-1);
			else
				//we need to use a try/catch statement in order to be able to lock the Chopsticks
				try {//try to pick up and lock the left Chopstick
					if(leftChopStick.myLock.tryLock(1, TimeUnit.MILLISECONDS)){
						//try to pick up and lock the other Chopstick
						if(rightChopStick.myLock.tryLock(2, TimeUnit.MILLISECONDS)){
							//we have to stop measuring the hunger
							endHunger = System.currentTimeMillis();
							//the hungry time is increased with the measured time
							hungryTime = hungryTime + (endHunger - startHunger);			
							//the philosopher can finally eat
							eat();
							//first he puts down the right Chopstick
							rightChopStick.myLock.unlock();
						} //then the philosopher puts down the left Chopstick as well
						leftChopStick.myLock.unlock();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	
	//help method to modify the variable fed, which will also indicate the end of the turn
	public void setFedStatus(boolean bool){
		fed = bool;
	}
		
	public void think(){
		//the thinking time is increased by a random number,generated with the random generator
		thinkingTime= thinkingTime + randomGenerator.nextInt(980)+10;
		//increasing the thinking turns as well 
		numberOfThinkingTurns++;
	}
	
	public void hungry(){
		//increasing only the hungry turns because we are already keeping track of the time 
		numberOfHungryTurns++;
	}
	
	public void eat(){
		//the eating time is increased by a random number,generated with the random generator
		eatingTime = eatingTime + randomGenerator.nextInt(980)+10;
		//increasing the hungry turns as well 
		numberOfEatingTurns++;
	}

	
	
}
