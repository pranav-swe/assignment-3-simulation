public class Grass extends Plant {
    public Grass(Location location, boolean randomAge) {
        super(location, randomAge, "Grass", 0, 1, 1000000, false);

        SPREADING_AGE = 0;
        SPREAD_PROBABILITY = 1;
        MAX_AGE = 1000000;
    }
}
