
import org.h2.tools.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    /*Track To-Do item.
    * Write a program that constantly loops
    * and provides an option to add, toggle, and list the to-do items
    */

    public static void insertItem(Connection conn, String text, int userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO items VALUES (NULL, ?, FALSE, ? )");
        stmt.setString(1, text);
        stmt.setInt(2, userId);
        stmt.execute();
    }

    public static ToDoItem selectItem(Connection conn, int userId, int itemId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM items INNER JOIN users ON items.user_id = ? WHERE items.id = ?");
        stmt.setInt(1, userId);
        stmt.setInt(2, itemId);
        ResultSet results = stmt.executeQuery();

        if (results.next()) {
            String text = results.getString("text");
            boolean isDone = results.getBoolean("is_done");
            int user = results.getInt("user_id");//GET items have to match database fields
            return new ToDoItem(itemId, text, isDone, user);
        }
        return null;
    }

    public static ArrayList<ToDoItem> selectItems(Connection conn, int userId) throws SQLException {
        ArrayList<ToDoItem> items = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM items INNER JOIN users ON items.user_id = users.id WHERE user_id = ?");
        stmt.setInt(1, userId);
        ResultSet results = stmt.executeQuery();

        while (results.next()) {
            int id = results.getInt("id");
            String text = results.getString("text");
            boolean isDone = results.getBoolean("is_done");
            int user = results.getInt("user_id");//GET items have to match database fields
            items.add(new ToDoItem(id, text, isDone, user));
        }
        return items;
    }

    public static void updateItem(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE items SET is_done = NOT is_done WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }

    public static void insertUser(Connection conn, String user) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES(NULL, ?); ");
        stmt.setString(1, user);
        stmt.execute();
    }

    public static User selectUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet result = stmt.executeQuery();
        if (result.next()) { //return selected user
            int id = result.getInt("id");
            return new User(id, name);
        }

        return null;
    }

    static void deleteItem(Connection conn, int itemNum) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM items WHERE id = ?");
        stmt.setInt(1, itemNum);
        stmt.execute();
    }

    static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS items (id IDENTITY, text VARCHAR, is_done BOOLEAN, user_id INT);");
        stmt.execute("CREATE  TABLE IF NOT EXISTS users(id IDENTITY , name VARCHAR );");

    }

    public static void main(String[] args) throws SQLException {

        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);


        Scanner scanner = new Scanner(System.in);

        //Prompt for login
        System.out.println("Please enter your name: ");
        String name = scanner.nextLine();

        //Select user with given login info
        User user = selectUser(conn, name);

        //Insert and select user if user doesn't exist
        if (user == null) {
            insertUser(conn, name);
            user = selectUser(conn,name);
        }

        //Prompt user to enter an option
        do {

            System.out.println("\n"+ user.name +", please select an option below:");
            System.out.println("1. Create to-do item");
            System.out.println("2. Update(Toggle) to-do item");
            System.out.println("3. List all to-do items");
            System.out.println("4. Delete item");
            System.out.println("5. Exit");

            Integer option = scanner.nextInt();
            scanner.nextLine(); //consumes new line leftover by nextInt


            switch (option) {

                case 1:
                    System.out.println("\nPlease enter your to-do item");
                    String text = scanner.nextLine();
                    insertItem(conn, text, user.id);
                    break;

                case 2:

                    System.out.println("\nPlease enter the number of the item that you would like to update/toggle: ");
                    int itemNum = Integer.parseInt(scanner.nextLine());
                    ToDoItem itemUpdate = selectItem(conn, user.id, itemNum); //todo checkuser's item
                    if (itemUpdate == null){
                        System.out.println("This item number was not found in your to-do list.");
                    } else {
                        updateItem(conn, itemNum);
                    }
                    break;

                case 3:
                    ArrayList<ToDoItem> items = selectItems(conn, user.id);
                    System.out.println("\nYou currently have " + items.size() + " items in your To-Do list");
                    System.out.println("\nDone?\tToDo ID#\tTodo Item");
                    System.out.println("==============================");
                    for (ToDoItem item : items) {
                        String checkbox = "[ ] ";
                        if (item.isDone) {
                            checkbox = "[X] ";
                        }
                        System.out.printf("%s\t%d\t\t%s\n", checkbox, item.id, item.text);

                    }
                    System.out.println("==============================\n");
                    break;

                case 4:
                    System.out.println("Please enter the id number of the item that you would like to delete: ");
                    Integer deleteNum = scanner.nextInt();
                    ToDoItem itemDel = selectItem(conn, user.id, deleteNum);
                    if (itemDel == null){
                        System.out.println("This item number was not found in your to-do list.");
                    } else {
                        deleteItem(conn, deleteNum);
                    }
                    break;

                case 5:
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid option");

            }

        } while (true);

    }
}
