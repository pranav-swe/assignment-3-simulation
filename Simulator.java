import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;

public class Simulator
{
    
    // Constants representing configuration information for the simulation.
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 120;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 80;
    // The probability that a fox will be created in any given grid position.

    private HashMap<Class<? extends Animal>, Double> animalTypeCreationProbabilityMap = new HashMap<>();

    private HashMap<Class<? extends Plant>, Double> plantTypeCreationProbabilityMap = new HashMap<>();

    // The current state of the field.
    private Field field;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation.
    private final SimulatorView view;

    private boolean isDay = true;

    private List<String> weatherTypes;
    // 0 for day, 1 for night

    private int simulationId;

    /**
     * Construct a simulation field with default size.
     */
    public Simulator(int simulationId)
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH, simulationId);
    }
    
    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field. Must be greater than zero.
     * @param width Width of the field. Must be greater than zero.
     */
    public Simulator(int depth, int width, int simulationId)
    {
        if(width <= 0 || depth <= 0) {
            System.out.println("The dimensions must be >= zero.");
            System.out.println("Using default values.");
            depth = DEFAULT_DEPTH;
            width = DEFAULT_WIDTH;
        }
        
        field = new Field(depth, width);
        view = new SimulatorView(depth, width);

        this.simulationId = simulationId;

        setUpAnimalTypeCreationProbabilityMap();

        setupWeather();

        reset();
    }

    public void setupWeather() {
        weatherTypes = new ArrayList();

        weatherTypes.add("RAIN");
        weatherTypes.add("FOG");
        weatherTypes.add("DROUGHT");
        weatherTypes.add("SAND_STORM");
        weatherTypes.add("WILD_FIRE");
        weatherTypes.add("NORMAL");
    }

    public void setUpAnimalTypeCreationProbabilityMap() {
        double ZEBRA_CREATION_PROBABILITY = 0.05;
        double LION_CREATION_PROBABILITY = 0.03;
        double GAZELLE_CREATION_PROBABILITY = 0.05;
        double HYENA_CREATION_PROBABILITY = 0.03;
        double CHEETAH_CREATION_PROBABILITY = 0.03;
        double PLANT_CREATION_PROBABILITY = 0.6;

        animalTypeCreationProbabilityMap.put(Zebra.class, ZEBRA_CREATION_PROBABILITY);
        animalTypeCreationProbabilityMap.put(Lion.class, LION_CREATION_PROBABILITY);
        animalTypeCreationProbabilityMap.put(Gazelle.class, GAZELLE_CREATION_PROBABILITY);
        animalTypeCreationProbabilityMap.put(Hyena.class, HYENA_CREATION_PROBABILITY);
        animalTypeCreationProbabilityMap.put(Cheetah.class, CHEETAH_CREATION_PROBABILITY);
        
        plantTypeCreationProbabilityMap.put(Grass.class, PLANT_CREATION_PROBABILITY);
    }
    
    /**
     * Run the simulation from its current state for a reasonably long 
     * period (4000 steps).
     */
    public void runLongSimulation()
    {
        simulate(700);
    }

    public void testSimDataAnimal() {
        System.out.println(DatabaseManager.retrieveSimulationAnimalConfig(simulationId));
    }

    public void testSimDataPlant() {
        System.out.println(DatabaseManager.retrieveSimulationPlantConfig(simulationId));
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

        String weather = getRandomWeather();

        System.out.println(weather);

        List<Animal> animals = field.getAnimals();
        for (Animal anAnimal : animals) {
            anAnimal.act(field, nextFieldState, isDay, weather);
        }

        List<Plant> plants = field.getPlants();
        for (Plant plant : plants) {
            plant.act(field, nextFieldState, isDay, weather);
        }
        
        // Replace the old state with the new one.
        field = nextFieldState;

        System.out.println("Plant Length: " + nextFieldState.plantField.size());

        reportStats();
        view.showStatus(step, field);
    }

    private String getRandomWeather() {
        Collections.shuffle(weatherTypes);
        return weatherTypes.get(0);
    }
        
    /**
     * Reset the simulation to a starting position.
     */
    public void reset()
    {
        step = 0;
        populate2();
        view.showStatus(step, field);
    }

    private void populate2() {
        Random rand = Randomizer.getRandom();
        field.clear();

        List<Map<String, Object>> animalData = DatabaseManager.retrieveSimulationAnimalConfig(simulationId);
        List<Map<String, Object>> plantData = DatabaseManager.retrieveSimulationPlantConfig(simulationId);

        for (int row = 0; row < field.getDepth(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {
                for (Map<String, Object> animalConfig : animalData) {
                    double creationProbability = ((BigDecimal) animalConfig.get("spawn_probability")).doubleValue();
                    if (rand.nextDouble() <= creationProbability) {

                        Location location = new Location(row, col);

                        String animal_type = (String) animalConfig.get("animal_type");
                        int breeding_age = (int) animalConfig.get("breeding_age");
                        int max_age = (int) animalConfig.get("max_age");
                        double breeding_probability = ((BigDecimal) animalConfig.get("breeding_probability")).doubleValue();
                        int max_litter_size = (int) animalConfig.get("max_offspring_size");
                        int food_value = (int) animalConfig.get("food_value");
                        String food_type = (String) animalConfig.get("food_type");

                        String animal_category = (String) animalConfig.get("animal_category");

                        if (animal_category.equals("PREY")) {
                            Prey animal = new Prey(true, Animal.getRandomGender(), location, breeding_age, max_age, breeding_probability, max_litter_size, food_value, food_type, animal_type);
                            field.placeAnimal(animal, location);
                        }
                        else if (animal_category.equals("PREDATOR")) {
                            Predator animal = new Predator(true, Animal.getRandomGender(), location, breeding_age, max_age, breeding_probability, max_litter_size, food_value, food_type, animal_type);
                            field.placeAnimal(animal, location);
                        }
                        System.out.println(animal_type + " created at " + location);
                        break;
                    }
                }
            }
        }




        for (int row = 0; row < field.getDepth(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {
                for (Map<String, Object> plantConfig : plantData) {
                    double creationProbability = ((BigDecimal) plantConfig.get("spawn_probability")).doubleValue();
                    if (rand.nextDouble() <= creationProbability) {

                        Location location = new Location(row, col);

                        String plant_type = (String) plantConfig.get("plant_type");
                        int spreading_age = (int) plantConfig.get("spreading_age");
                        int max_age = (int) plantConfig.get("max_age");
                        double spreading_probability = ((BigDecimal) plantConfig.get("spreading_probability")).doubleValue();
                        boolean is_poisonous = (boolean) plantConfig.get("is_poisonous");

                        Plant plant = new Plant(location, true, plant_type, spreading_age, spreading_probability, max_age, is_poisonous);
                        field.placeAnimal(plant, location);
                    }
                }
            }
        }






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
                                boolean.class, boolean.class, Location.class, String.class
                            ).newInstance(true, Animal.getRandomGender(), location, "test");
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

        for (int row = 0; row < field.getDepth(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {
                for (Class<? extends Plant> plantType : plantTypeCreationProbabilityMap.keySet()) {
                    double creationProbability = plantTypeCreationProbabilityMap.get(plantType);
                    if (rand.nextDouble() <= creationProbability) {
                        Location location = new Location(row, col);
                        try {
                            Plant plant = plantType.getDeclaredConstructor(
                                Location.class, boolean.class
                            ).newInstance(location, true);
                            field.placeAnimal(plant, location);
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

    public void collectEntityData() {
        Scanner scanner = new Scanner(System.in);
        Map<String, Map<String, Object>> entityData = new HashMap<>();
        boolean collecting = true;

        String simulationName = getStringInput(scanner, "Enter the name of the simulation: ");
        entityData.put("simulationName", Collections.singletonMap("name", simulationName));

        while (collecting) {
            System.out.println("Enter entity type (PREDATOR, PREY, PLANT) or 'submit' to save to database or 'clear' to clear all data: or 'view' to view entity data : ");
            String entityType = scanner.nextLine().toUpperCase();

            if (entityType.equals("SUBMIT")) {
                submitEntityData(entityData);
                collecting = false;
            } else if (entityType.equals("CLEAR")) {
                entityData.clear();
                System.out.println("All data cleared.");
                simulationName = getStringInput(scanner, "Enter the name of the simulation: ");
                entityData.put("simulationName", Collections.singletonMap("name", simulationName));
            } else if (entityType.equals("VIEW")) {
                System.out.println(entityData);
            } else {
                Map<String, Object> entityAttributes = new HashMap<>();
                switch (entityType) {
                    case "PREDATOR":
                        entityAttributes.put("animal_category", "PREDATOR");
                        entityAttributes.put("animal_type", getStringInput(scanner, "Enter ANIMAL_TYPE: "));
                        entityAttributes.put("breeding_age", getIntInput(scanner, "Enter BREEDING_AGE: "));
                        entityAttributes.put("max_age", getIntInput(scanner, "Enter MAX_AGE: "));
                        entityAttributes.put("breeding_probability", getDoubleInput(scanner, "Enter BREEDING_PROBABILITY: "));
                        entityAttributes.put("max_offspring_size", getIntInput(scanner, "Enter MAX_LITTER_SIZE: "));
                        entityAttributes.put("food_value", getIntInput(scanner, "Enter PREY_FOOD_VALUE: "));
                        entityAttributes.put("food_type", getStringInput(scanner, "Enter PREY_ANIMAL_TYPE: "));
                        entityAttributes.put("spawn_probability", getDoubleInput(scanner, "Enter SPAWN_PROBABILITY: "));
                        break;

                    case "PREY":
                        entityAttributes.put("animal_category", "PREY");
                        entityAttributes.put("animal_type", getStringInput(scanner, "Enter ANIMAL_TYPE: "));
                        entityAttributes.put("breeding_age", getIntInput(scanner, "Enter BREEDING_AGE: "));
                        entityAttributes.put("max_age", getIntInput(scanner, "Enter MAX_AGE: "));
                        entityAttributes.put("breeding_probability", getDoubleInput(scanner, "Enter BREEDING_PROBABILITY: "));
                        entityAttributes.put("max_offspring_size", getIntInput(scanner, "Enter MAX_LITTER_SIZE: "));
                        entityAttributes.put("food_value", getIntInput(scanner, "Enter PREY_FOOD_VALUE: "));
                        entityAttributes.put("food_type", getStringInput(scanner, "Enter PREY_ANIMAL_TYPE: "));
                        entityAttributes.put("spawn_probability", getDoubleInput(scanner, "Enter SPAWN_PROBABILITY: "));
                        break;

                    case "PLANT":
                        entityAttributes.put("plant_type", getStringInput(scanner, "Enter PLANT_TYPE: "));
                        entityAttributes.put("spreading_age", getIntInput(scanner, "Enter SPREADING_AGE: "));
                        entityAttributes.put("spreading_probability", getDoubleInput(scanner, "Enter SPREAD_PROBABILITY: "));
                        entityAttributes.put("max_age", getIntInput(scanner, "Enter MAX_AGE: "));
                        entityAttributes.put("spawn_probability", getDoubleInput(scanner, "Enter SPAWN_PROBABILITY: "));
                        entityAttributes.put("is_poisonous", getStringInput(scanner, "Enter IS_POISONOUS: "));
                        break;

                    default:
                        System.out.println("Invalid entity type.");
                        continue;
                }
                String key = UUID.randomUUID().toString();
                entityData.put(key, entityAttributes);
                System.out.println("Entity data collected with key: " + key);
            }
        }
    }

    public void viewAllSimulations() {
        List<String> resultSet = DatabaseManager.retrieveAllSimulations();
        for (String statement : resultSet) {
            System.out.println(statement);
        }
    }

    public void deleteSimulation() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the simulation ID to delete: ");
        int simulationId = scanner.nextInt();
        DatabaseManager.deleteSimulation(simulationId);
        System.out.println("Simulation with ID " + simulationId + " deleted.");
    }

    private int getIntInput(Scanner scanner, String prompt) {
        int input;
        while (true) {
            System.out.println(prompt);
            if (scanner.hasNextInt()) {
                input = scanner.nextInt();
                scanner.nextLine(); // consume newline
                break;
            } else {
                System.out.println("Invalid input. Please enter an integer.");
                scanner.next(); // consume invalid input
            }
        }
        return input;
    }

    private double getDoubleInput(Scanner scanner, String prompt) {
        double input;
        while (true) {
            System.out.println(prompt);
            if (scanner.hasNextDouble()) {
                input = scanner.nextDouble();
                scanner.nextLine(); // consume newline
                break;
            } else {
                System.out.println("Invalid input. Please enter a double.");
                scanner.next(); // consume invalid input
            }
        }
        return input;
    }

    private String getStringInput(Scanner scanner, String prompt) {
        System.out.println(prompt);
        return scanner.nextLine();
    }

    public void submitEntityData(Map<String, Map<String, Object>> entityData) {
        // Placeholder for database interaction
        System.out.println("Submitting entity data to database: " + entityData);
        DatabaseManager.insertSimulation(entityData);
    }

}
