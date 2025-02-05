import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * A simple predator-prey simulator, based on a rectangular field containing 
 * rabbits and foxes.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 7.1
 */
public class Simulator
{
    
    // Constants representing configuration information for the simulation.
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 120;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 80;
    // The probability that a fox will be created in any given grid position.

    private HashMap<Class<? extends Animal>, Double> animalTypeCreationProbabilityMap = new HashMap<>();

    // The current state of the field.
    private Field field;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation.
    private final SimulatorView view;

    private boolean isDay = true;
    // 0 for day, 1 for night

    /**
     * Construct a simulation field with default size.
     */
    public Simulator()
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH);
    }
    
    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field. Must be greater than zero.
     * @param width Width of the field. Must be greater than zero.
     */
    public Simulator(int depth, int width)
    {
        if(width <= 0 || depth <= 0) {
            System.out.println("The dimensions must be >= zero.");
            System.out.println("Using default values.");
            depth = DEFAULT_DEPTH;
            width = DEFAULT_WIDTH;
        }
        
        field = new Field(depth, width);
        view = new SimulatorView(depth, width);

        setUpAnimalTypeCreationProbabilityMap();

        reset();
    }

    public void setUpAnimalTypeCreationProbabilityMap() {
        double ZEBRA_CREATION_PROBABILITY = 0.05;
        double LION_CREATION_PROBABILITY = 0.03;
        double GAZELLE_CREATION_PROBABILITY = 0.05;
        double HYENA_CREATION_PROBABILITY = 0.03;
        double CHEETAH_CREATION_PROBABILITY = 0.03;

        animalTypeCreationProbabilityMap.put(Zebra.class, ZEBRA_CREATION_PROBABILITY);
        animalTypeCreationProbabilityMap.put(Lion.class, LION_CREATION_PROBABILITY);
        animalTypeCreationProbabilityMap.put(Gazelle.class, GAZELLE_CREATION_PROBABILITY);
        animalTypeCreationProbabilityMap.put(Hyena.class, HYENA_CREATION_PROBABILITY);
        animalTypeCreationProbabilityMap.put(Cheetah.class, CHEETAH_CREATION_PROBABILITY);
    }
    
    /**
     * Run the simulation from its current state for a reasonably long 
     * period (4000 steps).
     */
    public void runLongSimulation()
    {
        simulate(700);
    }
    
    /**
     * Run the simulation for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     * @param numSteps The number of steps to run for.
     */
    public void simulate(int numSteps)
    {
        reportStats();
        for(int n = 1; n <= numSteps && field.isViable(); n++) {
            simulateOneStep();
            delay(50);         // adjust this to change execution speed
        }
    }

    private void changeDayOrNight() {
        isDay = !isDay;
    }
    
    /**
     * Run the simulation from its current state for a single step.
     * Iterate over the whole field updating the state of each fox and rabbit.
     */
    public void simulateOneStep()
    {
        changeDayOrNight();

        step++;
        // Use a separate Field to store the starting state of
        // the next step.
        Field nextFieldState = new Field(field.getDepth(), field.getWidth());


        List<Animal> animals = field.getAnimals();
        for (Animal anAnimal : animals) {
            anAnimal.act(field, nextFieldState, isDay);
        }
        
        // Replace the old state with the new one.
        field = nextFieldState;

        reportStats();
        view.showStatus(step, field);
    }
        
    /**
     * Reset the simulation to a starting position.
     */
    public void reset()
    {
        step = 0;
        populate();
        view.showStatus(step, field);
    }
    
    /**
     * Randomly populate the field with foxes and rabbits.
     */
    private void populate()
    {
        Random rand = Randomizer.getRandom();
        field.clear();
        for (int row = 0; row < field.getDepth(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {
                for (Class<? extends Animal> animalType : animalTypeCreationProbabilityMap.keySet()) {
                    double creationProbability = animalTypeCreationProbabilityMap.get(animalType);
                    if (rand.nextDouble() <= creationProbability) {
                        Location location = new Location(row, col);
                        try {
                            Animal animal = animalType.getDeclaredConstructor(
                                boolean.class, boolean.class, Location.class
                            ).newInstance(true, Animal.getRandomGender(), location);
                            field.placeAnimal(animal, location);
                            System.out.println(animal.getClass().getSimpleName() + " created at " + location);
                            break;
                        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Report on the number of each type of animal in the field.
     */
    public void reportStats()
    {
        System.out.println(field.fieldStats());
    }
    
    /**
     * Pause for a given time.
     * @param milliseconds The time to pause for, in milliseconds
     */
    private void delay(int milliseconds)
    {
        try {
            Thread.sleep(milliseconds);
        }
        catch(InterruptedException e) {
            // ignore
        }
    }
}
