public class Gazelle extends Prey {
    private static final int BREEDING_AGE = 2;
    private static final int MAX_AGE = 50;
    private static final double BREEDING_PROBABILITY = 0.5;
    private static final int MAX_LITTER_SIZE = 2;

    public Gazelle(boolean randomAge, boolean gender, Location location)
    {
        super(randomAge, gender, location, BREEDING_AGE, MAX_AGE, BREEDING_PROBABILITY, MAX_LITTER_SIZE);
    }

    @Override
    public String toString() {
        return "Gazelle{" +
                "age=" + age +
                ", alive=" + isAlive() +
                '}';
    }
}
