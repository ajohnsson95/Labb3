import java.sql.*;
import java.util.Scanner;

public class Main {

    private static Scanner scanner = new Scanner(System.in);

    private static Connection connect() {
        String url = "jdbc:sqlite:C:\\Users\\ajohn\\SQLiteJava2023Datagrip";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    private static void printActions() {
        System.out.println("\nVälj:");
        System.out.println("""
                0  - Stäng av
                1  - Visa alla recept
                2  - Lägga till ett nytt recept
                3  - Uppdatera recept
                4  - Ta bort ett recept
                5  - Lägg till ingredienser i recept
                6  - Visa Join från bägge tabeller
                7  - Visa en lista över alla val.""");
    }

    public static void main(String[] args) {

        boolean quit = false;
        printActions();
        while(!quit) {
            System.out.println("\nVälj (7 för att visa val):");
            int action = scanner.nextInt();
            scanner.nextLine();

            switch (action) {
                case 0 -> {
                    System.out.println("\nStänger ner...");
                    quit = true;
                }
                case 1 -> selectAll();
                case 2 -> addRecipe();
                case 3 -> updateRecipe();
                case 4 -> deleteRecipe();
                case 5 -> addIngredientToRecipe();
                case 6 -> displayJoinedData();
                case 7 -> printActions();
            }
        }

    }
    private static void selectAll(){
        String sql = "SELECT * FROM Recept";

        try {
            Connection conn = connect();
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getInt("receptId") +  "\t" +
                        rs.getString("receptNamn") + "\t" +
                        rs.getString("beskrivning") + "\t" +
                        rs.getString("svårighetsgrad"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void addRecipe(){
        System.out.println("Skriv in receptet du vill lägga till med Namn, kort beskrivning och" +
                " svårighetsgrad med siffror 1-5");
        add(scanner.nextLine(), scanner.nextLine(), scanner.nextInt());
    }
    private static void add(String namn, String beskrivning, int difficulty) {
        String sql = "INSERT INTO Recept(receptNamn, beskrivning, svårighetsgrad) VALUES(?,?,?)";

        try{
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, namn);
            pstmt.setString(2, beskrivning);
            pstmt.setInt(3, difficulty);
            pstmt.executeUpdate();
            System.out.println("Du har lagt till receptet: " + namn);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void updateRecipe(){
        System.out.println("För att uppdatera ett recept mata in namnet på receptet du vill ha," +
                "en kort beskrivning om receptet, svårighetsgrad med siffror 1-5 och sedan vilket receptId som skall uppdateras");
        update(scanner.nextLine(), scanner.nextLine(), scanner.nextInt(), scanner.nextInt());
    }
    private static void update(String namn, String beskrivning, int difficulty, int receptId) {
        String sql = "UPDATE Recept SET receptNamn = ? , "
                + "beskrivning = ? , "
                + "svårighetsgrad = ? "
                + "WHERE receptId = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setString(1, namn);
            pstmt.setString(2, beskrivning);
            pstmt.setInt(3, difficulty);
            pstmt.setInt(4, receptId);
            // update
            pstmt.executeUpdate();
            System.out.println("Du har uppdaterat receptet " + namn);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void deleteRecipe(){
        System.out.println("Skriv in ett recept ID önksar att ta bort: ");
        int receptIdToDelete = scanner.nextInt();
        delete(receptIdToDelete);
        scanner.nextLine();
    }
    private static void delete(int id) {
        String sql = "DELETE FROM Recept WHERE receptId = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int deletedRows = pstmt.executeUpdate();

            if (deletedRows > 0) {
                System.out.println("Recept med ID " + id + " har nu tagits bort");
            } else {
                System.out.println("Hittar inget recept med recpetID: " + id);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void addIngredientToRecipe() {
        System.out.println("Välj recept för att lägga till ingredienser:");
        displayAllRecipes();

        int recipeId = scanner.nextInt();
        scanner.nextLine();

        boolean addingIngredients = true;
        while (addingIngredients) {
            System.out.println("Ange namnet på ingrediensen eller skriv 'sluta' för att avsluta:");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("sluta")) {
                addingIngredients = false;
            } else {
                String ingredientName = input;
                System.out.println("Ange mängden av ingrediensen:");
                String ingredientAmount = scanner.nextLine();

                addIngredient(recipeId, ingredientName, ingredientAmount);
            }
        }
    }
    private static void displayAllRecipes() {
        String sql = "SELECT receptId, receptNamn FROM Recept";

        try {
            Connection conn = connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                System.out.println(rs.getInt("receptId") + " - " + rs.getString("receptNamn"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void addIngredient(int receptId, String ingredientName, String ingredientAmount) {
        String sql = "INSERT INTO Ingredienser(ingrediensNamn, mängd, ingrediensReceptId) VALUES(?,?,?)";

        try {
            Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, ingredientName);
            pstmt.setString(2, ingredientAmount);
            pstmt.setInt(3, receptId);
            pstmt.executeUpdate();
            System.out.println("Ingrediensen har lagts till i receptet med ID: " + receptId);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void displayJoinedData() {
        String sql = "SELECT Recept.receptId, Recept.receptNamn, Recept.beskrivning, Recept.svårighetsgrad, " +
                "GROUP_CONCAT(ingredient.ingrediensNamn || ' - ' || ingredient.mängd, ', ') AS ingredients " +
                "FROM Recept " +
                "LEFT JOIN Ingredienser ingredient ON Recept.receptId = ingredient.ingrediensReceptId " +
                "GROUP BY Recept.receptId";

        try {
            Connection conn = connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                System.out.println("Recept ID: " + rs.getInt("receptId"));
                System.out.println("Recept Namn: " + rs.getString("receptNamn"));
                System.out.println("Beskrivning: " + rs.getString("beskrivning"));
                System.out.println("Svårighetsgrad: " + rs.getInt("svårighetsgrad"));
                System.out.println("Ingredienser: " + rs.getString("ingredients"));
                System.out.println("-----------------------------");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}