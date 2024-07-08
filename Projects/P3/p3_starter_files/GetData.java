import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;
import java.util.Vector;

import org.json.JSONObject;
import org.json.JSONArray;

public class GetData {

    static String prefix = "project3.";

    // You must use the following variable as the JDBC connection
    Connection oracleConnection = null;

    // You must refer to the following variables for the corresponding 
    // tables in your database
    String userTableName = null;
    String friendsTableName = null;
    String cityTableName = null;
    String currentCityTableName = null;
    String hometownCityTableName = null;

    // DO NOT modify this constructor
    public GetData(String u, Connection c) {
        super();
        String dataType = u;
        oracleConnection = c;
        userTableName = prefix + dataType + "_USERS";
        friendsTableName = prefix + dataType + "_FRIENDS";
        cityTableName = prefix + dataType + "_CITIES";
        currentCityTableName = prefix + dataType + "_USER_CURRENT_CITIES";
        hometownCityTableName = prefix + dataType + "_USER_HOMETOWN_CITIES";
    }

    @SuppressWarnings("unchecked")
    public JSONArray toJSON() throws SQLException {

        // This is the data structure to store all users' information
        JSONArray users_info = new JSONArray();

        try (Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

            Statement stmt2 = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            // Query to fetch user information
            ResultSet userResultSet = stmt.executeQuery(
                "SELECT User_ID, First_Name, Last_Name, Gender, Year_of_Birth, Month_of_Birth, Day_of_Birth " +
                "FROM " + userTableName
            );

            // Iterate through each user
            while (userResultSet.next()) {
                JSONObject user = new JSONObject();
                user.put("user_id", userResultSet.getInt(1));
                user.put("first_name", userResultSet.getString(2));
                user.put("last_name", userResultSet.getString(3));
                user.put("gender", userResultSet.getString(4));
                user.put("YOB", userResultSet.getInt(5));
                user.put("MOB", userResultSet.getInt(6));
                user.put("DOB", userResultSet.getInt(7));

                // Fetch current city information
                String queryCurrentCity = "SELECT CT.City_Name, CT.State_Name, CT.Country_Name " +
    				"FROM " + currentCityTableName + " C, " + cityTableName + " CT " +
    				"WHERE CT.City_ID = C.Current_City_ID AND C.User_ID = " + userResultSet.getInt(1);

                ResultSet currentCityResultSet = stmt2.executeQuery(queryCurrentCity);
                
                JSONObject current = new JSONObject();
                if (!currentCityResultSet.next()) {
                    user.put("current", current);
                }
                else {
                    current.put("city", currentCityResultSet.getString(1));
                    current.put("state", currentCityResultSet.getString(2));
                    current.put("country", currentCityResultSet.getString(3));
                    user.put("current", current);
                }
                currentCityResultSet.close();

                // Fetch hometown information
                String queryHometown = "SELECT C.City_Name, C.State_Name, C.Country_Name " +
                "FROM " + hometownCityTableName + " H, " + cityTableName + " C " +
                "WHERE C.City_ID = H.Hometown_City_ID AND H.User_ID = " + userResultSet.getInt(1);

                ResultSet hometownResultSet = stmt2.executeQuery(queryHometown);

                JSONObject hometown = new JSONObject();
                if (!hometownResultSet.next()) {
                    user.put("hometown", hometown);
                }
                else {
                    hometown.put("city", hometownResultSet.getString(1));
                    hometown.put("state", hometownResultSet.getString(2));
                    hometown.put("country", hometownResultSet.getString(3));
                    user.put("hometown", hometown);
                }
                hometownResultSet.close();

                // Fetch friends information
                String queryFriends = "SELECT F.USER2_ID " +
    				"FROM " + friendsTableName + " F " +
    				"WHERE F.USER1_ID = " + userResultSet.getInt(1);

                JSONArray friends = new JSONArray();
                ResultSet friendsResultSet = stmt2.executeQuery(queryFriends);
                while (friendsResultSet.next()){
                    friends.put(friendsResultSet.getInt(1));
                }
                user.put("friends", friends);
                friendsResultSet.close();

                users_info.put(user);
            }

            userResultSet.close();
            stmt2.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return users_info;
    }

    // This outputs to a file "output.json"
    // DO NOT MODIFY this function
    public void writeJSON(JSONArray users_info) {
        try {
            FileWriter file = new FileWriter(System.getProperty("user.dir") + "/output.json");
            file.write(users_info.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
