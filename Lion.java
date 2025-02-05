public class Lion extends Predator {
    private static final int BREEDING_AGE = 0;
    private static final int MAX_AGE = 150;
    private static final double BREEDING_PROBABILITY = 0.08;
    private static final int MAX_LITTER_SIZE = 2;
    private static final int PREY_FOOD_VALUE = 9;
    private static final Class<? extends Animal> PREY_ANIMAL_TYPE = Zebra.class; // TODO: Change to Prey

    public Lion(boolean randomAge, boolean gender, Location location)
    {
        super(randomAge, gender, location, BREEDING_AGE, MAX_AGE, BREEDING_PROBABILITY, MAX_LITTER_SIZE, PREY_FOOD_VALUE, PREY_ANIMAL_TYPE);
    }

    @Override
    public String toString() {
        return "Lion{" +
                "age=" + age +
                ", foodLevel=" + foodLevel +
                ", alive=" + isAlive() +
                ", gender=" + gender +
                ", location=" + getLocation() +
                '}';
    }
}