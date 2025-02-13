import java.util.*;

/**
 * Represent a rectangular grid of field positions.
 * Each position is able to store a single animal/object.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 7.0
 */
public class Field
{
    // A random number generator for providing random locations.
    private static final Random rand = Randomizer.getRandom();
    
    // The dimensions of the field.
    private final int depth, width;
    // Animals mapped by location.
    private final Map<Location, Animal> field = new HashMap<>();
    // The animals.
    private final List<Animal> animals = new ArrayList<>();

    private final List<Plant> plants = new ArrayList<>();

    // Grass mapped by location.
    public final Map<Location, Plant> plantField = new HashMap<>();

    /**
     * Represent a field of the given dimensions.
     * @param depth The depth of the field.
     * @param width The width of the field.
     */
    public Field(int depth, int width)
    {
        this.depth = depth;
        this.width = width;
    }

    /**
     * Place an animal at the given location.
     * If there is already an animal at the location it will
     * be lost.
     * @param anAnimal The animal to be placed.
     * @param location Where to place the animal.
     */
    public void placeAnimal(Animal anAnimal, Location location)
    {
        assert location != null;
        Object other = field.get(location);
        if(other != null) {
            animals.remove(other);
        }
        field.put(location, anAnimal);
        animals.add(anAnimal);
    }

    // TODO: Change the naming of this. Its really bad. 
    public void placeAnimal(Plant plant, Location location)
    {
        // assert location != null;
        Object other = plantField.get(location);
        if(other != null) {
            plantField.remove(location);
        }
        plantField.put(location, plant);
        plants.add(plant);
    }
    
    /**
     * Return the animal at the given location, if any.
     * @param location Where in the field.
     * @return The animal at the given location, or null if there is none.
     */
    public Animal getAnimalAt(Location location)
    {
        return field.get(location);
    }

    public Plant getPlantAt(Location location)
    {
        return plantField.get(location);
    }

    /**
     * Get a shuffled list of the free adjacent locations.
     * @param location Get locations adjacent to this.
     * @return A list of free adjacent locations.
     */

    public List<Location> getFreeAdjacentLocations(Location location)
    {
        return getFreeLocationsInSpan(location, 1);
    }

    public List<Location> getFreeAdjacentLocationsPlants(Location location)
    {
        return getFreeLocationsInSpanPlant(location, 1);
    }

    public List<Location> getFreeLocationsInSpanPlant(Location location, int span)
    {
        List<Location> free = new LinkedList<>();
        List<Location> adjacent = getLocationsInSpan(location, span);
        for(Location next : adjacent) {
            Plant plant = plantField.get(next);
            if(plant == null) {
                free.add(next);
            }
            else if(!plant.isAlive()) {
                free.add(next);
            }
        }
        return free;
    }



    /**
     * Return a shuffled list of locations adjacent to the given one.
     * The list will not include the location itself.
     * All locations will lie within the grid.
     * @param location The location from which to generate adjacencies.
     * @return A list of locations adjacent to that given.
     */

    public List<Location> getAdjacentLocations(Location location)
    {
        return getLocationsInSpan(location, 1);
    }

    public List<Location> getFreeLocationsInSpan(Location location, int span)
    {
        List<Location> free = new LinkedList<>();
        List<Location> adjacent = getLocationsInSpan(location, span);
        for(Location next : adjacent) {
            Animal anAnimal = field.get(next);
            if(anAnimal == null) {
                free.add(next);
            }
            else if(!anAnimal.isAlive()) {
                free.add(next);
            }
        }
        return free;
    }

    public List<Location> getLocationsInSpan(Location location, int span)
    {
        // The list of locations to be returned.
        List<Location> locations = new ArrayList<>();
        if(location != null) {
            int row = location.row();
            int col = location.col();
            for(int roffset = -span; roffset <= span; roffset++) {
                int nextRow = row + roffset;
                if(nextRow >= 0 && nextRow < depth) {
                    for(int coffset = -span; coffset <= span; coffset++) {
                        int nextCol = col + coffset;
                        // Exclude invalid locations and the original location.
                        if(nextCol >= 0 && nextCol < width && (roffset != 0 || coffset != 0)) {
                            locations.add(new Location(nextRow, nextCol));
                        }
                    }
                }
            }
            
            // Shuffle the list. Several other methods rely on the list
            // being in a random order.
            Collections.shuffle(locations, rand);
        }
        return locations;
    }

    /**
     * Print out the number of foxes and rabbits in the field.
     */
    public String fieldStats() {
        Map<Class<? extends Animal>, Integer> animalTypeCountMap = new HashMap<>();
        for (Animal anAnimal : field.values()) {
            Class<? extends Animal> animalClass = anAnimal.getClass();
            Integer count = animalTypeCountMap.get(animalClass);
            if (count == null) {
                animalTypeCountMap.put(animalClass, 1);
            } else {
                animalTypeCountMap.put(animalClass, count + 1);
            }
        }

        String statsString = "";

        for (Class<? extends Animal> animalClass : animalTypeCountMap.keySet()) {
            Integer count = animalTypeCountMap.get(animalClass);
            statsString += String.format("%s : %d\n", animalClass.getSimpleName(), count);
        }

        return statsString;
    }

    /**
     * Empty the field.
     */
    public void clear()
    {
        field.clear();
    }

    /**
     * Return whether there is at least one rabbit and one fox in the field.
     * @return true if there is at least one rabbit and one fox in the field.
     */
    public boolean isViable()
    {
        return true;
        /* 
        boolean rabbitFound = false;
        boolean foxFound = false;
        Iterator<Animal> it = animals.iterator();
        while(it.hasNext() && ! (rabbitFound && foxFound)) {
            Animal anAnimal = it.next();
            if(anAnimal instanceof Rabbit rabbit) {
                if(rabbit.isAlive()) {
                    rabbitFound = true;
                }
            }
            else if(anAnimal instanceof Fox fox) {
                if(fox.isAlive()) {
                    foxFound = true;
                }
            }
        }
        return rabbitFound && foxFound;
        */
    }
    
    /**
     * Get the list of animals.
     */
    public List<Animal> getAnimals()
    {
        return animals;
    }

    public List<Plant> getPlants()
    {
        return plants;
    }

    /**
     * Return the depth of the field.
     * @return The depth of the field.
     */
    public int getDepth()
    {
        return depth;
    }
    
    /**
     * Return the width of the field.
     * @return The width of the field.
     */
    public int getWidth()
    {
        return width;
    }
}
