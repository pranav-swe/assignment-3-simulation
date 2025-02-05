import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public abstract class Predator extends Animal  {
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
    private int PREY_FOOD_VALUE;
    // A shared random number generator to control breeding.
    private Random rand = Randomizer.getRandom();

    private Class<? extends Animal> PREY_ANIMAL_TYPE; // TODO: Change to Prey

    protected int age;
    protected int foodLevel;
    protected boolean gender;

    public Predator(
        boolean randomAge, boolean gender, Location location,
        int BREEDING_AGE, int MAX_AGE, double BREEDING_PROBABILITY, int MAX_LITTER_SIZE, int PREY_FOOD_VALUE, Class<? extends Animal> preyAnimalType // TODO: Change to Prey
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
        this.PREY_FOOD_VALUE = PREY_FOOD_VALUE;
        this.PREY_ANIMAL_TYPE = preyAnimalType;

        this.gender = gender;
        foodLevel = rand.nextInt(PREY_FOOD_VALUE);
    }

    public boolean maleInVicinity(Field nextFieldState) {
        List<Location> freeLocationsGeneric =
        nextFieldState.getLocationsInSpan(getLocation(), 5);
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
        incrementAge();
        incrementHunger();
        if (isAlive()) {
            List<Location> freeLocations = nextFieldState.getFreeAdjacentLocations(getLocation());
            if (!freeLocations.isEmpty() && gender && maleInVicinity(nextFieldState)) {
                giveBirth(nextFieldState, freeLocations);
            }
            // Move towards a source of food if found.
            Location nextLocation = findFood(currentField);
            if (nextLocation == null && !freeLocations.isEmpty()) {
                // No food found - try to move to a free location.
                nextLocation = freeLocations.remove(0);
            }
            // See if it was possible to move.
            if (nextLocation != null) {
                setLocation(nextLocation);
                nextFieldState.placeAnimal(this, nextLocation);
            }
            else {
                // Overcrowding.
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

    protected void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }

    protected Location findFood(Field field)
    {
        List<Location> adjacent = field.getAdjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        Location foodLocation = null;
        while (foodLocation == null && it.hasNext()) {
            Location loc = it.next();
            Animal potentialPrey = field.getAnimalAt(loc);
            if (PREY_ANIMAL_TYPE.isInstance(potentialPrey)) {
                if (potentialPrey.isAlive()) {
                    potentialPrey.setDead();
                    foodLevel = PREY_FOOD_VALUE;
                    foodLocation = loc;
                }
            }
        }
        return foodLocation;
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
