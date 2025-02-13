public class Cheetah extends Predator {
    private static final int BREEDING_AGE = 5;
    private static final int MAX_AGE = 50;
    private static final double BREEDING_PROBABILITY = 0.5;
    private static final int MAX_LITTER_SIZE = 2;
    private static final int PREY_FOOD_VALUE = 9;
    private static final String PREY_ANIMAL_TYPE = "test"; // TODO: Change to Prey

    public Cheetah(boolean randomAge, boolean gender, Location location, String category)
    {
        super(randomAge, gender, location, BREEDING_AGE, MAX_AGE, BREEDING_PROBABILITY, MAX_LITTER_SIZE, PREY_FOOD_VALUE, PREY_ANIMAL_TYPE, category);
    }
    
    @Override
    public String toString() {
        return "Cheetah{" +
                "age=" + age +
                ", foodLevel=" + foodLevel +
                ", alive=" + isAlive() +
                '}';
    }
}