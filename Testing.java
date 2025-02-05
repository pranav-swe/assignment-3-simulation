import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Testing {
    private final int width = 20;
    private final int depth = 20;

    private static final Random rand = Randomizer.getRandom();

    public Testing() {}

    public List<Location> getAdjacentLocationsGeneric(Location location, int span)
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

            System.out.println("Locations fixed size : " + locations.size());
        }
        return locations;
    }

}