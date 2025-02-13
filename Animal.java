
/**
 * Common elements of foxes and rabbits.
 *
 * @author David J. Barnes and Michael Kölling
 * @version 7.0
 */
public abstract class Animal
{
    // Whether the animal is alive or not.
    private boolean alive;
    // The animal's position.
    private Location location;
    private boolean gender;
    private boolean isSick;
    private String category;

    /**
     * Constructor for objects of class Animal.
     * @param location The animal's location.
     */
    public Animal(Location location, boolean geneder, String category)
    {
        this.gender = geneder;
        this.alive = true;
        this.location = location;
        this.category = category;

        determineSickness();
    }

    public String getCategory() {
        return category;
    }

    public void determineSickness() {
        isSick = Math.random() < 0.3;
    }

    public static boolean getRandomGender() {
        return Math.random() < 0.5;
    }

    public boolean getGender() {
        return gender;
    }

    public void setSickness(boolean isSick) {
        this.isSick = isSick;
    }

    public boolean getIsSick() {
        return isSick;
    }

    /**
     * Act.
     * @param currentField The current state of the field.
     * @param nextFieldState The new state being built.
     */
    abstract public void act(Field currentField, Field nextFieldState, boolean isDay, String weather);
    
    /**
     * Check whether the animal is alive or not.
     * @return true if the animal is still alive.
     */
    public boolean isAlive()
    {
        return alive;
    }

    /**
     * Indicate that the animal is no longer alive.
     */
    protected void setDead()
    {
        alive = false;
        location = null;
    }
    
    /**
     * Return the animal's location.
     * @return The animal's location.
     */
    public Location getLocation()
    {
        return location;
    }
    
    /**
     * Set the animal's location.
     * @param location The new location.
     */
    protected void setLocation(Location location)
    {
        this.location = location;
    }
}
