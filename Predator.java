import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Predator extends Animal  {
    // Characteristics shared by all foxes (class variables).
    // The age at which a fox can start to breed.
    private int BREEDING_AGE = 10;
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

    private String PREY_ANIMAL_TYPE; // TODO: Change to Prey

    public int age;
    public int foodLevel;
    public boolean gender;

    public Predator(
        boolean randomAge, boolean gender, Location location,
        int BREEDING_AGE, int MAX_AGE, double BREEDING_PROBABILITY, int MAX_LITTER_SIZE, int PREY_FOOD_VALUE, String preyAnimalType,
        String category
    )
    {
        super(location, gender, category);
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
            if (potentialMate != null && potentialMate.getCategory().equals(getCategory())) {
                if (!potentialMate.getGender()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean sickAnimalInVicinity(Field nextFieldState) {
        List<Location> freeLocationsGeneric =
        nextFieldState.getLocationsInSpan(getLocation(), 2);
        for (Location loc : freeLocationsGeneric) {
            Animal potentialInfector = nextFieldState.getAnimalAt(loc);
            if (potentialInfector != null && potentialInfector.getIsSick()) {
                setSickness(true);
                age += 10;
                if (age > MAX_AGE) {
                    setDead();
                }
            }
        }
        return false;
    }

    public void act(Field currentField, Field nextFieldState, boolean isDay, String weather)
    {
        incrementAge();
        incrementHunger();
        determineSickness();
        sickAnimalInVicinity(nextFieldState);
        if (isAlive()) {
            if ("WILDFIRE".equals(weather) && rand.nextDouble() <= 0.2) {
                setDead();
            } else {
                if (!isDay) {
                    nextFieldState.placeAnimal(this, getLocation());
                    return;
                }
                List<Location> freeLocations = nextFieldState.getFreeAdjacentLocations(getLocation());
                if (!freeLocations.isEmpty() && gender && maleInVicinity(nextFieldState)) {
                    giveBirth(nextFieldState, freeLocations);
                }
                Location nextLocation = null;
                if (!"FOG".equals(weather)) {
                    // Move towards a source of food if found.
                    nextLocation = findFood(currentField);
                }
                if (nextLocation == null && !freeLocations.isEmpty()) {
                    // No food found - try to move to a free location.
                    nextLocation = freeLocations.remove(0);
                }
                // See if it was possible to move.
                if (nextLocation != null) {
                    setLocation(nextLocation);
                    Plant potentialPlant = currentField.getPlantAt(nextLocation);
                    if (potentialPlant != null) {
                        // System.out.println("PLANT AGE DECREMENTED");
                        potentialPlant.decrementAge();
                    }
                    nextFieldState.placeAnimal(this, nextLocation);
                } else {
                    // Overcrowding.
                    setDead();
                }
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
            if (potentialPrey != null && potentialPrey.getCategory().equals(getCategory())) {
                if (potentialPrey.isAlive()) {
                    System.out.println("PRED ATE FOOD");
                    if (potentialPrey.getIsSick()) {
                        setSickness(true);
                        age += 10;
                        if (age > MAX_AGE) {
                            setDead();
                        }
                    }
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
                // Use reflection to create a new instance of the subclass
                Predator young = new Predator(true, getRandomGender(), loc, BREEDING_AGE, MAX_AGE, BREEDING_PROBABILITY, MAX_LITTER_SIZE, PREY_FOOD_VALUE, PREY_ANIMAL_TYPE, getCategory());
                nextFieldState.placeAnimal(young, loc);

                System.out.println("PRED BIRTH");
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
