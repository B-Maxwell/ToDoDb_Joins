import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by MacbookStudioPro on 3/3/17.
 */
public class MainTest {

    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Main.createTables(conn);
        return conn;
    }

    @Test
    public void testInsertAndSelectItem() throws Exception {
        Connection conn = startConnection();
        Main.insertUser(conn, "Max");
        User user = Main.selectUser(conn, "Max");
        Main.insertItem(conn, "This is an item", user.id);
        ToDoItem item = Main.selectItem(conn, user.id, 1);
        conn.close();
        assertTrue(item != null);
    }


    @Test
    public void testSelectItems() throws Exception {
        Connection conn = startConnection();
        Main.insertUser(conn, "Joe");
        User user = Main.selectUser(conn, "Joe");
        Main.insertItem(conn, "This is item 1", user.id);
        Main.insertItem(conn, "This is item 2", user.id);
        Main.insertItem(conn, "This is item 3", user.id);
        ArrayList<ToDoItem> items = Main.selectItems(conn, user.id);
        conn.close();
        assertTrue(items.size() == 3);
    }

    @Test
    public void updateItem() throws Exception {
        Connection conn = startConnection();
        Main.insertUser(conn, "Helen");
        User user = Main.selectUser(conn, "Helen");
        Main.insertItem(conn, "This is my item", user.id);
        Main.insertItem(conn, "This is also my item", user.id);
        Main.insertItem(conn, "This is another item", user.id);
        Main.updateItem(conn, 2);
        ToDoItem item = Main.selectItem(conn, user.id, 2);
        assertTrue(item.isDone);
    }

    @Test
    public void deleteItem() throws Exception {
        Connection conn = startConnection();
        Main.insertUser(conn, "Bob");
        User user = Main.selectUser(conn, "Bob");
        Main.insertItem(conn, "This is my item", user.id);
        Main.insertItem(conn, "This is also my item", user.id);
        Main.insertItem(conn, "This is another item", user.id);
        Main.deleteItem(conn, 1);
        ArrayList<ToDoItem> items = Main.selectItems(conn, user.id);
        conn.close();
        assertTrue(items.size() == 2);
    }

}