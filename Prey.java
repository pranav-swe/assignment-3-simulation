import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

public abstract class Prey extends Animal  {
    // Characteristics shared by all foxes (class variables).
    // The age at which a fox can start to breed.
    private int BREEDING_AGE;
    // The age to which a fox can live.
    private int MAX_AGE;
    // The likelihood of a fox breeding.
    private double BREEDING_PROBABILITY;
    // The maximum number of births.
    private int MAX_LITTER_SIZE;
    // The food value of a single rabbit. In effect, this is the
    // number of steps a fox can go before it has to eat again.
    // A shared random number generator to control breeding.

    private Random rand = Randomizer.getRandom();

    protected int age;
    protected int foodLevel;
    protected boolean gender;

    public Prey(
        boolean randomAge, boolean gender, Location location,
        int BREEDING_AGE, int MAX_AGE, double BREEDING_PROBABILITY, int MAX_LITTER_SIZE
    )
    {
        super(location, gender);
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        }
        else {
            age = 0;
        }

        this.BREEDING_AGE = BREEDING_AGE;
        this.MAX_AGE = MAX_AGE;
        this.BREEDING_PROBABILITY = BREEDING_PROBABILITY;
        this.MAX_LITTER_SIZE = MAX_LITTER_SIZE;

        this.gender = gender;
    }

    public boolean maleInVicinity(Field nextFieldState) {
        List<Location> freeLocationsGeneric =
        nextFieldState.getLocationsInSpan(getLocation(), 100);
        for (Location loc : freeLocationsGeneric) {
            Animal potentialMate = nextFieldState.getAnimalAt(loc);
            if (this.getClass().isInstance(potentialMate)) {
                if (!potentialMate.getGender()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void act(Field currentField, Field nextFieldState, boolean isDay)
    {
        // incrementAge();
        if (isAlive()) {
            List<Location> freeLocations = nextFieldState.getFreeAdjacentLocations(getLocation());
            if (!freeLocations.isEmpty() && gender && maleInVicinity(nextFieldState)) {
                giveBirth(nextFieldState, freeLocations);
            }
            if (!isDay) {
                nextFieldState.placeAnimal(this, getLocation());
            }
            else if (!freeLocations.isEmpty()) {
                Location nextLocation = freeLocations.get(0);
                setLocation(nextLocation);
                nextFieldState.placeAnimal(this, nextLocation);
            }
            else {
                setDead();
            }
        }
    }

    protected void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }

    protected void giveBirth(Field nextFieldState, List<Location> freeLocations)
    {
        int births = breed();
        if (births > 0) {
            for (int b = 0; b < births && ! freeLocations.isEmpty(); b++) {
                Location loc = freeLocations.remove(0);
                try {
                    // Use reflection to create a new instance of the subclass
                    Animal young = this.getClass().getDeclaredConstructor(boolean.class, boolean.class, Location.class)
                            .newInstance(false, Animal.getRandomGender(), loc);
                    nextFieldState.placeAnimal(young, loc);
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected int breed()
    {
        int births;
        if(canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        else {
            births = 0;
        }
        return births;
    }

    protected boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
}
