import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * This class collects and provides some statistical data on the state 
 * of a field. It is flexible: it will create and maintain a counter 
 * for any class of object that is found within the field.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 7.0
 */
public class FieldStats
{
    // Counters for each type of entity (fox, rabbit, etc.) in the simulation.
    private final Map<String, Counter> counters;

    private final Map<String, Color> colorMap;

    // Whether the counters are currently up to date.
    private boolean countsValid;

    /**
     * Construct a FieldStats object.
     */
    public FieldStats(Map<String, Color> colors)
    {
        // Set up a collection for counters for each type of animal that
        // we might find
        counters = new HashMap<>();
        colorMap = colors;
        countsValid = true;
    }

    /**
     * Get details of what is in the field.
     * @return A string describing what is in the field.
     */
    public String getPopulationDetails(Field field)
    {
        StringBuilder details = new StringBuilder("<div>");
        if(!countsValid) {
            generateCounts(field);
        }
        for(String key : counters.keySet()) {
            Counter info = counters.get(key);
            Color color = colorMap.get(info.getName());
            String colorString = color != null ? String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()) : "#000000"; // Default to black if color is null
            details.append(String.format("<span style='color:%s'>%s</span> : %d ", colorString, info.getName(), info.getCount()));
        }
        details.append("</div>");
        return details.toString();
    }
    
    /**
     * Invalidate the current set of statistics; reset all 
     * counts to zero.
     */
    public void reset()
    {
        countsValid = false;
        for(String key : counters.keySet()) {
            Counter count = counters.get(key);
            count.reset();
        }
    }

    /**
     * Increment the count for one class of animal.
     * @param animalClass The class of animal to increment.
     */
    public void incrementCount(String animalClass)
    {
        Counter count = counters.get(animalClass);
        if(count == null) {
            // We do not have a counter for this species yet.
            // Create one.
            count = new Counter(animalClass);
            counters.put(animalClass, count);
        }
        count.increment();
    }

    /**
     * Indicate that an animal count has been completed.
     */
    public void countFinished()
    {
        countsValid = true;
    }

    /**
     * Determine whether the simulation is still viable.
     * I.e., should it continue to run.
     * @return true If there is more than one species alive.
     */
    public boolean isViable(Field field)
    {
        return field.isViable();
    }
    
    /**
     * Generate counts of the number of foxes and rabbits.
     * These are not kept up to date as foxes and rabbits
     * are placed in the field, but only when a request
     * is made for the information.
     * @param field The field to generate the stats for.
     */
    private void generateCounts(Field field)
    {
        reset();
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Animal animal = field.getAnimalAt(new Location(row, col));
                if(animal != null) {
                    incrementCount(animal.getCategory());
                }
            }
        }
        countsValid = true;
    }
}
