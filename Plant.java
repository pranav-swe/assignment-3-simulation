import java.util.List;
import java.util.Random;

public class Plant {
    private boolean alive;
    private Location location;
    private int age;
    public int SPREADING_AGE; // or any appropriate value
    public double SPREAD_PROBABILITY;
    public int MAX_AGE;
    private String type;
    private boolean IS_POISONOUS;

    private Random rand = Randomizer.getRandom();

    public Plant(Location location, boolean randomAge, String type, int spreadingAge, double spreadProbability, int maxAge, boolean isPoisonous) {
        this.alive = true;
        this.location = location;
        this.type = type;

        SPREADING_AGE = spreadingAge;
        SPREAD_PROBABILITY = spreadProbability;
        MAX_AGE = maxAge;
        IS_POISONOUS = isPoisonous;

        if(randomAge) {
            age = rand.nextInt(2);
        }
        else {
            age = 0;
        }
    }

    public void decrementAge() {
        decrementAge(1);
    }

    public void decrementAge(int decrementAmount) {
        age -= decrementAmount;
        if (age <= 0) {
            setDead();
        }
    }

    public String getType() {
        return type;
    }

    public boolean getIsPoisonous() {
        return IS_POISONOUS;
    }

    public void incrementAge() {
        incrementAge(1);
    }

    public void incrementAge(int incrementAmount) {
        age += incrementAmount;
        if (age > MAX_AGE) {
            setDead();
        }
    }

    public void setDead() {
        alive = false;
        location = null;
    }

    public boolean isAlive() {
        return alive;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location)
    {
        this.location = location;
    }

    public void act(Field currentField, Field nextFieldState, boolean isDay, String weather)
    {
        incrementAge();
        if (isAlive()) {
            if ("WILDFIRE".equals(weather) && Math.random() < 0.2) {
                setDead();
            } else {
                List<Location> freeLocations = nextFieldState.getFreeAdjacentLocationsPlants(getLocation());
                if (!freeLocations.isEmpty() && age >= SPREADING_AGE) {
                    double spreadProbability = SPREAD_PROBABILITY;
                    if ("RAIN".equals(weather)) {
                        spreadProbability *= 2; // Exponential growth during rain
                    } else if ("DROUGHT".equals(weather)) {
                        spreadProbability *= 0.5; // Reduced growth during drought
                    }
                    for (Location loc : freeLocations) {
                        if (Math.random() < spreadProbability) {
                            Plant newPlant = spread(loc);
                            nextFieldState.placeAnimal(newPlant, loc);
                        }
                    }
                } else {
                    setDead();
                }
                nextFieldState.placeAnimal(this, getLocation());
            }
        }
    }

    protected Plant spread(Location location) {
        // System.out.println("PLANT SPREAD");
        return new Plant(location, true, getType(), SPREADING_AGE, SPREAD_PROBABILITY, MAX_AGE, IS_POISONOUS);
    }
}