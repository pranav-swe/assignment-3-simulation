import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/pred-prey-sim-db";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "admin";

    public static void createTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, name VARCHAR(50), email VARCHAR(50))";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
            System.out.println("Table 'users' created!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> retrieveAllSimulations() {
        String selectSQL = "SELECT * FROM simulation";
        List<String> simulationDetails = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectSQL)) {
            while (resultSet.next()) {
                String simulationDetail = "Simulation ID: " + resultSet.getInt("id") + "\n" +
                                          "Simulation Name: " + resultSet.getString("simulation_name") + "\n";
                simulationDetails.add(simulationDetail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return simulationDetails;
    }

    public static void deleteSimulation(int id) {
        String deleteSimulationSQL = "DELETE FROM simulation WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(deleteSimulationSQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertSimulation(Map<String, Map<String, Object>> entityData) {
        String simulationName = entityData.get("simulationName").get("name").toString();

        String insertSimulationSQL = "INSERT INTO simulation (simulation_name) VALUES (?) RETURNING id";
        String insertAnimalSQL = "INSERT INTO animal (animal_type, animal_category, breeding_age, max_age, breeding_probability, max_offspring_size, food_value, food_type, spawn_probability) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        String insertPlantSQL = "INSERT INTO plant (plant_type, max_age, spreading_age, spreading_probability, is_poisonous, spawn_probability) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        String insertSimulationAnimalSQL = "INSERT INTO simulation_animal (simulation_id, animal_id) VALUES (?, ?)";
        String insertSimulationPlantSQL = "INSERT INTO simulation_plant (simulation_id, plant_id) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            connection.setAutoCommit(false);

            try (PreparedStatement simulationStmt = connection.prepareStatement(insertSimulationSQL);
                 PreparedStatement animalStmt = connection.prepareStatement(insertAnimalSQL);
                 PreparedStatement plantStmt = connection.prepareStatement(insertPlantSQL);
                 PreparedStatement simulationAnimalStmt = connection.prepareStatement(insertSimulationAnimalSQL);
                 PreparedStatement simulationPlantStmt = connection.prepareStatement(insertSimulationPlantSQL)) {

                // Insert simulation
                simulationStmt.setString(1, simulationName);
                ResultSet simulationRs = simulationStmt.executeQuery();
                simulationRs.next();
                int simulationId = simulationRs.getInt(1);

                // Insert animals
                for (Map.Entry<String, Map<String, Object>> entry : entityData.entrySet()) {
                    Map<String, Object> attributes = entry.getValue();
                    if (attributes.containsKey("animal_type")) {
                        animalStmt.setString(1, (String) attributes.get("animal_type"));
                        animalStmt.setString(2, (String) attributes.get("animal_category"));
                        animalStmt.setInt(3, (int) attributes.get("breeding_age"));
                        animalStmt.setInt(4, (int) attributes.get("max_age"));
                        animalStmt.setBigDecimal(5, BigDecimal.valueOf((double) attributes.get("breeding_probability")));
                        animalStmt.setInt(6, (int) attributes.get("max_offspring_size"));
                        animalStmt.setInt(7, (int) attributes.get("food_value"));
                        animalStmt.setString(8, (String) attributes.get("food_type"));
                        animalStmt.setBigDecimal(9, BigDecimal.valueOf((double) attributes.get("spawn_probability")));
                        ResultSet animalRs = animalStmt.executeQuery();
                        animalRs.next();
                        int animalId = animalRs.getInt(1);

                        // Insert into simulation_animal
                        simulationAnimalStmt.setInt(1, simulationId);
                        simulationAnimalStmt.setInt(2, animalId);
                        simulationAnimalStmt.executeUpdate();
                    }
                }

                // Insert plants
                for (Map.Entry<String, Map<String, Object>> entry : entityData.entrySet()) {
                    Map<String, Object> attributes = entry.getValue();
                    if (attributes.containsKey("plant_type")) {
                        plantStmt.setString(1, (String) attributes.get("plant_type"));
                        plantStmt.setInt(2, (int) attributes.get("max_age"));
                        plantStmt.setInt(3, (int) attributes.get("spreading_age"));
                        plantStmt.setBigDecimal(4, BigDecimal.valueOf((double) attributes.get("spreading_probability")));
                        plantStmt.setBoolean(5, Boolean.parseBoolean((String) attributes.get("is_poisonous")));
                        plantStmt.setBigDecimal(6, BigDecimal.valueOf((double) attributes.get("spawn_probability")));
                        ResultSet plantRs = plantStmt.executeQuery();
                        plantRs.next();
                        int plantId = plantRs.getInt(1);

                        // Insert into simulation_plant
                        simulationPlantStmt.setInt(1, simulationId);
                        simulationPlantStmt.setInt(2, plantId);
                        simulationPlantStmt.executeUpdate();
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Map<String, Object>> retrieveSimulationAnimalConfig(int id) {
        String[] animalFields = {
            "id", 
            "animal_type", 
            "animal_category", 
            "breeding_age", 
            "max_age", 
            "breeding_probability", 
            "max_offspring_size", 
            "food_value", 
            "food_type", 
            "spawn_probability"
        };
        String selectSQL = "SELECT * FROM animal WHERE id IN (SELECT animal_id FROM simulation_animal WHERE simulation_id = ?)";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setInt(1, id);
            ResultSet resultSet = pstmt.executeQuery();
            List<Map<String, Object>> animals = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> animal = new HashMap<>();
                for (String fieldName : animalFields) {
                    Object value = resultSet.getObject(fieldName);
                    animal.put(fieldName, value);
                }
                animals.add(animal);
            }
            return animals;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Map<String, Object>> retrieveSimulationPlantConfig(int id) {
        String[] plantFields = {
            "id", 
            "plant_type", 
            "max_age", 
            "spreading_age", 
            "spreading_probability", 
            "is_poisonous", 
            "spawn_probability"
        };
        
        String selectSQL = "SELECT * FROM plant WHERE id IN (SELECT plant_id FROM simulation_plant WHERE simulation_id = ?)";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setInt(1, id);
            ResultSet resultSet = pstmt.executeQuery();
            List<Map<String, Object>> plants = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> plant = new HashMap<>();
                for (String fieldName : plantFields) {
                    plant.put(fieldName, resultSet.getObject(fieldName));
                }
                plants.add(plant);
            }
            return plants;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void insertData(String name, String email) {
        String insertSQL = "INSERT INTO users (name, email) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.executeUpdate();
            System.out.println("Inserted data into 'users' table!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet retrieveData() {
        String selectSQL = "SELECT * FROM users";
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectSQL)) {
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getAllPlantAndAnimalTypes() {
        String plantTypeSQL = "SELECT plant_type FROM plant";
        String animalTypeSQL = "SELECT animal_type FROM animal";
        List<String> types = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             Statement plantStmt = connection.createStatement();
             Statement animalStmt = connection.createStatement();
             ResultSet plantResultSet = plantStmt.executeQuery(plantTypeSQL);
             ResultSet animalResultSet = animalStmt.executeQuery(animalTypeSQL)) {

            while (plantResultSet.next()) {
                types.add(plantResultSet.getString("plant_type"));
            }

            while (animalResultSet.next()) {
                types.add(animalResultSet.getString("animal_type"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return types;
    }
}
