package project2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;

/*
    The StudentFakebookOracle class is derived from the FakebookOracle class and implements
    the abstract query functions that investigate the database provided via the <connection>
    parameter of the constructor to discover specific information.
*/
public final class StudentFakebookOracle extends FakebookOracle {
    // [Constructor]
    // REQUIRES: <connection> is a valid JDBC connection
    public StudentFakebookOracle(Connection connection) {
        oracle = connection;
    }

    @Override
    // Query 0
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the total number of users for which a birth month is listed
    //        (B) Find the birth month in which the most users were born
    //        (C) Find the birth month in which the fewest users (at least one) were born
    //        (D) Find the IDs, first names, and last names of users born in the month
    //            identified in (B)
    //        (E) Find the IDs, first names, and last name of users born in the month
    //            identified in (C)
    //
    // This query is provided to you completed for reference. Below you will find the appropriate
    // mechanisms for opening up a statement, executing a query, walking through results, extracting
    // data, and more things that you will need to do for the remaining nine queries
    public BirthMonthInfo findMonthOfBirthInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            // Step 1
            // ------------
            // * Find the total number of users with birth month info
            // * Find the month in which the most users were born
            // * Find the month in which the fewest (but at least 1) users were born
            ResultSet rst = stmt.executeQuery(
                    "SELECT COUNT(*) AS Birthed, Month_of_Birth " + // select birth months and number of uses with that birth month
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth IS NOT NULL " + // for which a birth month is available
                            "GROUP BY Month_of_Birth " + // group into buckets by birth month
                            "ORDER BY Birthed DESC, Month_of_Birth ASC"); // sort by users born in that month, descending; break ties by birth month

            int mostMonth = 0;
            int leastMonth = 0;
            int total = 0;
            while (rst.next()) { // step through result rows/records one by one
                if (rst.isFirst()) { // if first record
                    mostMonth = rst.getInt(2); //   it is the month with the most
                }
                if (rst.isLast()) { // if last record
                    leastMonth = rst.getInt(2); //   it is the month with the least
                }
                total += rst.getInt(1); // get the first field's value as an integer
            }
            BirthMonthInfo info = new BirthMonthInfo(total, mostMonth, leastMonth);

            // Step 2
            // ------------
            // * Get the names of users born in the most popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + mostMonth + " " + // born in the most popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addMostPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 3
            // ------------
            // * Get the names of users born in the least popular birth month
            rst = stmt.executeQuery(
                    "SELECT User_ID, First_Name, Last_Name " + // select ID, first name, and last name
                            "FROM " + UsersTable + " " + // from all users
                            "WHERE Month_of_Birth = " + leastMonth + " " + // born in the least popular birth month
                            "ORDER BY User_ID"); // sort smaller IDs first

            while (rst.next()) {
                info.addLeastPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }

            // Step 4
            // ------------
            // * Close resources being used
            rst.close();
            stmt.close(); // if you close the statement first, the result set gets closed automatically

            return info;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new BirthMonthInfo(-1, -1, -1);
        }
    }

    @Override
    // Query 1
    // -----------------------------------------------------------------------------------
    // GOALS: (A) The first name(s) with the most letters
    //        (B) The first name(s) with the fewest letters
    //        (C) The first name held by the most users
    //        (D) The number of users whose first name is that identified in (C)
    public FirstNameInfo findNameInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                FirstNameInfo info = new FirstNameInfo();
                info.addLongName("Aristophanes");
                info.addLongName("Michelangelo");
                info.addLongName("Peisistratos");
                info.addShortName("Bob");
                info.addShortName("Sue");
                info.addCommonName("Harold");
                info.addCommonName("Jessica");
                info.setCommonNameCount(42);
                return info;
            */

           // (A)
            FirstNameInfo info = new FirstNameInfo();
            ResultSet rst = stmt.executeQuery(
                "SELECT DISTINCT First_Name FROM " + UsersTable +
                " WHERE LENGTH(First_name) IN" + "(" +
                " SELECT MAX(LENGTH(First_name)) FROM " + UsersTable + ")" +
                " ORDER BY 1");

            while (rst.next()){
                info.addLongName(rst.getString(1));
            }

            // (B)
            rst = stmt.executeQuery(
                "SELECT DISTINCT First_Name FROM " + UsersTable +
                " WHERE LENGTH(First_name) IN" + "(" +
                " SELECT MIN(LENGTH(First_name)) FROM " + UsersTable + ")" +
                " ORDER BY 1");

            while (rst.next()){
                info.addShortName(rst.getString(1));
            }

            // (C)
            rst = stmt.executeQuery(
                "SELECT MAX(COUNT(First_name)) FROM " + UsersTable + 
                " GROUP BY First_Name");
            int numMostFirst = 0;
            while(rst.next()){
                numMostFirst = rst.getInt(1);
            }
            info.setCommonNameCount(numMostFirst);

            // (D)
            String query = String.format(
                "SELECT First_name FROM " + UsersTable + " GROUP BY First_name HAVING COUNT(*) = %d", numMostFirst);
            rst = stmt.executeQuery(query);
            while(rst.next()){
                info.addCommonName(rst.getString(1));
            }

            rst.close();
            stmt.close();

            return info;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new FirstNameInfo();
        }
    }

    @Override
    // Query 2
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users without any friends
    //
    // Be careful! Remember that if two users are friends, the Friends table only contains
    // the one entry (U1, U2) where U1 < U2.
    public FakebookArrayList<UserInfo> lonelyUsers() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(15, "Abraham", "Lincoln");
                UserInfo u2 = new UserInfo(39, "Margaret", "Thatcher");
                results.add(u1);
                results.add(u2);
            */

           // SQL query to find users without any friends
           ResultSet rst = stmt.executeQuery(
                "SELECT User_ID, First_Name, Last_Name " +
                "FROM " + UsersTable + " U " +
                "WHERE User_ID NOT IN ( " +
                "    SELECT User1_ID FROM " + FriendsTable + " " +
                "    UNION " +
                "    SELECT User2_ID FROM " + FriendsTable + " " +
                ") " +
                "ORDER BY User_ID");

        while (rst.next()) {
            UserInfo user = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
            results.add(user);
        }

        // Close resources
        rst.close();
        stmt.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 3
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users who no longer live
    //            in their hometown (i.e. their current city and their hometown are different)
    public FakebookArrayList<UserInfo> liveAwayFromHome() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(9, "Meryl", "Streep");
                UserInfo u2 = new UserInfo(104, "Tom", "Hanks");
                results.add(u1);
                results.add(u2);
            */
            
            ResultSet rst = stmt.executeQuery(
                "SELECT USER_ID, FIRST_NAME, LAST_NAME " + "FROM " + UsersTable + 
                " MINUS " + "SELECT U.USER_ID, " + "U.FIRST_NAME, " + "U.LAST_NAME " + "FROM " + 
                CurrentCitiesTable + " P1, " + HometownCitiesTable + " P2, " + UsersTable + 
                " U " + "WHERE P1.USER_ID = P2.USER_ID AND U.USER_ID = P1.USER_ID AND P1.CURRENT_CITY_ID = P2.HOMETOWN_CITY_ID ORDER BY 1"
            );

            while(rst.next()){
                results.add(new UserInfo(rst.getInt(1), rst.getString(2), rst.getString(3)));
            }

            rst.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 4
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, links, and IDs and names of the containing album of the top
    //            <num> photos with the most tagged users
    //        (B) For each photo identified in (A), find the IDs, first names, and last names
    //            of the users therein tagged
    public FakebookArrayList<TaggedPhotoInfo> findPhotosWithMostTags(int num) throws SQLException {
        FakebookArrayList<TaggedPhotoInfo> results = new FakebookArrayList<TaggedPhotoInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                PhotoInfo p = new PhotoInfo(80, 5, "www.photolink.net", "Winterfell S1");
                UserInfo u1 = new UserInfo(3901, "Jon", "Snow");
                UserInfo u2 = new UserInfo(3902, "Arya", "Stark");
                UserInfo u3 = new UserInfo(3903, "Sansa", "Stark");
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
                tp.addTaggedUser(u1);
                tp.addTaggedUser(u2);
                tp.addTaggedUser(u3);
                results.add(tp);
            */

            ResultSet rst = stmt.executeQuery(
                "SELECT P.PHOTO_ID, P.ALBUM_ID, A.ALBUM_NAME, P.PHOTO_LINK, U.USER_ID, U.FIRST_NAME, U.LAST_NAME "
                + "FROM " + PhotosTable + " P, " + AlbumsTable + " A, " + TagsTable + " T2, " + UsersTable
                + " U, " + "(SELECT * FROM((SELECT T1.TAG_PHOTO_ID, COUNT(T1.TAG_SUBJECT_ID) AS C FROM "
                + TagsTable + " T1 "
                + "GROUP BY T1.TAG_PHOTO_ID ORDER BY COUNT(T1.TAG_SUBJECT_ID) DESC, T1.TAG_PHOTO_ID) TMP) WHERE ROWNUM<= "
                + num + ")" + " TMP1 "
                + " WHERE P.PHOTO_ID=T2.TAG_PHOTO_ID AND P.ALBUM_ID=A.ALBUM_ID AND T2.TAG_SUBJECT_ID=U.USER_ID AND T2.TAG_PHOTO_ID=TMP1.TAG_PHOTO_ID "
                + " ORDER BY TMP1.C, P.PHOTO_ID, U.USER_ID");

            TaggedPhotoInfo tp = null;
            long p_id = 0;
            while (rst.next()) {
                if (p_id == rst.getLong(1)) {
                    UserInfo u = new UserInfo(rst.getLong(5), rst.getString(6), rst.getString(7));
                    tp.addTaggedUser(u);
                } else {
                    if (tp != null) {
                        results.add(tp);
                    }
                    p_id = rst.getLong(1);
                    PhotoInfo p = new PhotoInfo(rst.getLong(1), rst.getLong(2), rst.getString(4), rst.getString(3));
                    tp = new TaggedPhotoInfo(p);
                    UserInfo u = new UserInfo(rst.getLong(5), rst.getString(6), rst.getString(7));
                    tp.addTaggedUser(u);
                }
                if (rst.isLast()) {
                    results.add(tp);
                }
            }

            rst.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 5
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, last names, and birth years of each of the two
    //            users in the top <num> pairs of users that meet each of the following
    //            criteria:
    //              (i) same gender
    //              (ii) tagged in at least one common photo
    //              (iii) difference in birth years is no more than <yearDiff>
    //              (iv) not friends
    //        (B) For each pair identified in (A), find the IDs, links, and IDs and names of
    //            the containing album of each photo in which they are tagged together
    public FakebookArrayList<MatchPair> matchMaker(int num, int yearDiff) throws SQLException {
        FakebookArrayList<MatchPair> results = new FakebookArrayList<MatchPair>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(93103, "Romeo", "Montague");
                UserInfo u2 = new UserInfo(93113, "Juliet", "Capulet");
                MatchPair mp = new MatchPair(u1, 1597, u2, 1597);
                PhotoInfo p = new PhotoInfo(167, 309, "www.photolink.net", "Tragedy");
                mp.addSharedPhoto(p);
                results.add(mp);
            */

            ResultSet rst1 = stmt.executeQuery(
                    "SELECT N.USER1_ID, U1.FIRST_NAME, U1.LAST_NAME, U1.YEAR_OF_BIRTH,  " +
                            "N.USER2_ID, U2.FIRST_NAME, U2.LAST_NAME, U2.YEAR_OF_BIRTH FROM  " +
                            "( " +
                            "SELECT DISTINCT T1.TAG_SUBJECT_ID AS USER1_ID, T2.TAG_SUBJECT_ID AS USER2_ID, T1.TAG_PHOTO_ID AS PHOTO_ID FROM " + TagsTable + " T1 " +
                            "JOIN " + TagsTable + " T2 ON T1.TAG_PHOTO_ID = T2.TAG_PHOTO_ID AND T1.TAG_SUBJECT_ID < T2.TAG_SUBJECT_ID " +
                            ") N " +
                            "JOIN " + UsersTable + " U1 ON U1.USER_ID = N.USER1_ID " +
                            "JOIN " + UsersTable + " U2 ON U2.USER_ID = N.USER2_ID " +
                            "WHERE U1.GENDER = U2.GENDER " +
                            "AND NOT EXISTS (SELECT USER1_ID, USER2_ID FROM  " +
                            "( " +
                            "(SELECT USER1_ID, USER2_ID FROM " + FriendsTable + " WHERE USER1_ID < USER2_ID) " +
                            "UNION " +
                            "(SELECT USER2_ID AS USER1_ID, USER1_ID AS USER2_ID FROM " + FriendsTable + " WHERE USER1_ID > USER2_ID) " +
                            ") F " +
                            "WHERE F.USER1_ID = N.USER1_ID AND F.USER2_ID = N.USER2_ID " +
                            ") " +
                            "AND ABS(U1.YEAR_OF_BIRTH - U2.YEAR_OF_BIRTH) <= 2 " +
                            "ORDER BY N.USER1_ID ASC, N.USER2_ID ASC"
            );

            Statement stmt2 = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly);




            while(rst1.next())
            {

                UserInfo u1 = new UserInfo(rst1.getLong(1), rst1.getString(2),rst1.getString(3));
                UserInfo u2 = new UserInfo(rst1.getLong(5), rst1.getString(6),rst1.getString(7));
                MatchPair mp = new MatchPair(u1, rst1.getLong(4), u2, rst1.getLong(8));

                long uid1 = rst1.getLong(1);
                long uid2 = rst1.getLong(5);

                ResultSet rst2 = stmt2.executeQuery(
                        "SELECT P.PHOTO_ID, P.ALBUM_ID, P.PHOTO_LINK, A.ALBUM_NAME  FROM " + PhotosTable + " P " +
                                "JOIN " + AlbumsTable + " A ON A.ALBUM_ID = P.ALBUM_ID " +
                                "JOIN " + TagsTable + " T1 ON T1.TAG_PHOTO_ID = P.PHOTO_ID " +
                                "JOIN " + TagsTable + " T2 ON T2.TAG_PHOTO_ID = P.PHOTO_ID AND T1.TAG_SUBJECT_ID < T2.TAG_SUBJECT_ID " +
                                "WHERE T1.TAG_SUBJECT_ID = " + uid1 + " AND T2.TAG_SUBJECT_ID = " + uid2
                );

                while(rst2.next())
                {
                    mp.addSharedPhoto(new PhotoInfo(rst2.getLong(1), rst2.getLong(2), rst2.getString(3), rst2.getString(4)));
                }
                results.add(mp);
            }
        

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 6
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of each of the two users in
    //            the top <num> pairs of users who are not friends but have a lot of
    //            common friends
    //        (B) For each pair identified in (A), find the IDs, first names, and last names
    //            of all the two users' common friends
    public FakebookArrayList<UsersPair> suggestFriends(int num) throws SQLException {
        FakebookArrayList<UsersPair> results = new FakebookArrayList<UsersPair>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            Statement stmt2 = oracle.createStatement
            (FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly);
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(16, "The", "Hacker");
                UserInfo u2 = new UserInfo(80, "Dr.", "Marbles");
                UserInfo u3 = new UserInfo(192, "Digit", "Le Boid");
                UsersPair up = new UsersPair(u1, u2);
                up.addSharedFriend(u3);
                results.add(up);
            */

            stmt.executeUpdate(
                "CREATE VIEW Mutuals AS " +
                "SELECT A.U1 AS P1, A.U2 AS MUTUAL, B.U2 AS P2 FROM " +
                "(SELECT F.USER1_ID AS U1, F.USER2_ID AS U2 FROM " + FriendsTable + " F UNION " +
                "SELECT F2.USER2_ID AS U1, F2.USER1_ID AS U2 FROM " + FriendsTable + " F2) A, " +
                "(SELECT F3.USER1_ID AS U1, F3.USER2_ID AS U2 FROM " + FriendsTable + " F3 UNION " +
                "SELECT F4.USER2_ID AS U1, F4.USER1_ID AS U2 FROM " + FriendsTable + " F4) B " +
                "WHERE A.U2 = B.U1 AND A.U1 < B.U2 AND NOT EXISTS (SELECT * FROM " + FriendsTable +
                " WHERE USER1_ID = A.U1 AND B.U2 = USER2_ID)"
            );
            
            //Create view for mutual friends. one column is all of u1's friends 
            ResultSet rst = stmt.executeQuery(
                "SELECT * FROM " +
                "(SELECT COUNT(*) AS FREQ, P1, P2 FROM Mutuals GROUP BY P1, P2 ORDER BY FREQ DESC, P1 ASC, P2 ASC) " +
                "WHERE ROWNUM <= " + num
            );

            while(rst.next()){
                ResultSet rst2 = stmt2.executeQuery(
                    "SELECT U1.FIRST_NAME AS U1F, U1.LAST_NAME AS U1L, U2.FIRST_NAME AS U2F, U2.LAST_NAME AS U2L FROM " +
                    UsersTable + " U1, " + UsersTable + " U2 WHERE U1.USER_ID = " + rst.getInt(2) + " AND U2.USER_ID = " +
                    rst.getInt(3)
                );
                rst2.next();
                UserInfo u1 = new UserInfo(rst.getInt(2), rst2.getString(1), rst2.getString(2));
                UserInfo u2 = new UserInfo(rst.getInt(3), rst2.getString(3), rst2.getString(4));
                UsersPair up = new UsersPair(u1, u2);

                rst2 = stmt2.executeQuery(
                "SELECT U.USER_ID, U.FIRST_NAME, U.LAST_NAME FROM " +
                "((SELECT F.USER1_ID AS Friend FROM " + FriendsTable + " F WHERE F.USER2_ID = " + rst.getInt(2) +
                "UNION SELECT F2.USER2_ID AS Friend FROM " + FriendsTable + " F2 WHERE F2.USER1_ID = " + rst.getInt(2) + ") INTERSECT " +
                "(SELECT F3.USER1_ID AS Friend FROM " + FriendsTable + " F3 WHERE F3.USER2_ID = " + rst.getInt(3) +
                "UNION SELECT F4.USER2_ID AS Friend FROM " + FriendsTable + " F4 WHERE F4.USER1_ID = " + rst.getInt(3) + ")) X, " +
                UsersTable + " U WHERE U.USER_ID = X.Friend ORDER BY U.USER_ID ASC" 
                );

                while(rst2.next()){
                    UserInfo u3 = new UserInfo(rst2.getInt(1), rst2.getString(2), rst2.getString(3));
                    up.addSharedFriend(u3);
                }
                results.add(up);
                rst2.close();
            }
            stmt.executeUpdate("DROP VIEW Mutuals");

            rst.close();
            stmt2.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return results;
    }

    @Override
    // Query 7
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the name of the state or states in which the most events are held
    //        (B) Find the number of events held in the states identified in (A)
    public EventStateInfo findEventStates() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
             * EXAMPLE DATA STRUCTURE USAGE ============================================
             * EventStateInfo info = new EventStateInfo(50); info.addState("Kentucky");
             * info.addState("Hawaii"); info.addState("New Hampshire"); return info;
             */

            ResultSet rst = stmt.executeQuery("SELECT MAX(COUNT(C.STATE_NAME)) " + "FROM " + CitiesTable + " C, "
                    + EventsTable + " E " + "WHERE C.CITY_ID = E.EVENT_CITY_ID " + " GROUP BY C.STATE_NAME");

            int maxCount = 0;
            while (rst.next()) {
                maxCount = rst.getInt(1);
            }
            EventStateInfo info = new EventStateInfo(maxCount);

            rst = stmt.executeQuery("SELECT C.STATE_NAME FROM " + EventsTable + " E, " + CitiesTable + " C "
                    + " WHERE E.EVENT_CITY_ID = C.CITY_ID " + " GROUP BY C.STATE_NAME " + " HAVING COUNT(*)= "
                    + maxCount + " ORDER BY 1");

            while (rst.next()) {
                info.addState(rst.getString(1));
            }
            rst.close();
            stmt.close();
            return info;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new EventStateInfo(-1);
        }
    }

    @Override
    // Query 8
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the ID, first name, and last name of the oldest friend of the user
    //            with User ID <userID>
    //        (B) Find the ID, first name, and last name of the youngest friend of the user
    //            with User ID <userID>
    public AgeInfo findAgeInfo(long userID) throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
                FakebookOracleConstants.ReadOnly)) {
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo old = new UserInfo(12000000, "Galileo", "Galilei");
                UserInfo young = new UserInfo(80000000, "Neil", "deGrasse Tyson");
                return new AgeInfo(old, young);
            */

           // Step 1: Find the oldest friend
        ResultSet rstOldest = stmt.executeQuery(
                "SELECT F.User2_ID, U.First_Name, U.Last_Name " +
                "FROM " + FriendsTable + " F JOIN " + UsersTable + " U ON F.User2_ID = U.User_ID " +
                "WHERE F.User1_ID = " + userID + " " +
                "ORDER BY U.Year_of_Birth ASC, U.Month_of_Birth ASC, U.Day_of_Birth ASC, U.User_ID DESC " +
                "FETCH FIRST 1 ROWS ONLY");

        UserInfo oldestFriend = null;
        if (rstOldest.next()) {
            oldestFriend = new UserInfo(rstOldest.getLong(1), rstOldest.getString(2), rstOldest.getString(3));
        }

        // Step 2: Find the youngest friend
        ResultSet rstYoungest = stmt.executeQuery(
                "SELECT F.User2_ID, U.First_Name, U.Last_Name " +
                "FROM " + FriendsTable + " F JOIN " + UsersTable + " U ON F.User2_ID = U.User_ID " +
                "WHERE F.User1_ID = " + userID + " " +
                "ORDER BY U.Year_of_Birth DESC, U.Month_of_Birth DESC, U.Day_of_Birth DESC, U.User_ID DESC " +
                "FETCH FIRST 1 ROWS ONLY");

        UserInfo youngestFriend = null;
        if (rstYoungest.next()) {
            youngestFriend = new UserInfo(rstYoungest.getLong(1), rstYoungest.getString(2), rstYoungest.getString(3));
        }

        // Step 3: Close resources
        rstOldest.close();
        rstYoungest.close();
        stmt.close();

        // Step 4: Return the AgeInfo object
        if (oldestFriend == null || youngestFriend == null) {
            return new AgeInfo(new UserInfo(-1, "NOT FOUND", "NOT FOUND"), new UserInfo(-1, "NOT FOUND", "NOT FOUND"));
        }

        return new AgeInfo(oldestFriend, youngestFriend);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return new AgeInfo(new UserInfo(-1, "ERROR", "ERROR"), new UserInfo(-1, "ERROR", "ERROR"));
        }
    }

    @Override
    // Query 9
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find all pairs of users that meet each of the following criteria
    //              (i) same last name
    //              (ii) same hometown
    //              (iii) are friends
    //              (iv) less than 10 birth years apart
    public FakebookArrayList<SiblingInfo> findPotentialSiblings() throws SQLException {
        FakebookArrayList<SiblingInfo> results = new FakebookArrayList<SiblingInfo>("\n");

        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll,
        FakebookOracleConstants.ReadOnly)) {
        
            ResultSet rst = stmt.executeQuery(
                "SELECT U1.USER_ID AS U1_ID, U1.First_Name AS U1_FNAME, U1.Last_Name AS U1_LNAME, " +
                "U2.USER_ID AS U2_ID, U2.First_Name AS U2_FNAME, U2.Last_Name AS U2_LNAME " +
                "FROM " + UsersTable + " U1 " +
                "LEFT JOIN " + UsersTable + " U2 " +
                "ON U1.USER_ID < U2.USER_ID " +
                "LEFT JOIN " + HometownCitiesTable + " H1 " +
                "ON H1.USER_ID = U1.USER_ID " +
                "LEFT JOIN " + HometownCitiesTable + " H2 " +
                "ON H2.USER_ID = U2.USER_ID " +
                "LEFT JOIN " + FriendsTable + " F " +
                "ON F.USER1_ID = U1.USER_ID AND F.USER2_ID = U2.USER_ID " +
                "WHERE U1.USER_ID != U2.USER_ID " +
                "AND U1.Last_Name = U2.Last_Name " +
                "AND ABS(U1.YEAR_OF_BIRTH - U2.YEAR_OF_BIRTH) < 10 " +
                "AND H1.HOMETOWN_CITY_ID = H2.HOMETOWN_CITY_ID " +
                "AND F.USER1_ID = U1.USER_ID AND F.USER2_ID = U2.USER_ID " +
                "ORDER BY U1.USER_ID ASC, U2.USER_ID ASC");

            while (rst.next()) {
                long u1Id = rst.getLong(1);
                String u1FName = rst.getString(2);
                String u1LName = rst.getString(3);
                long u2Id = rst.getLong(4);
                String u2FName = rst.getString(5);
                String u2LName = rst.getString(6);
                
                UserInfo u1 = new UserInfo(u1Id, u1FName, u1LName);
                UserInfo u2 = new UserInfo(u2Id, u2FName, u2LName);
                SiblingInfo si = new SiblingInfo(u1, u2);
                results.add(si);
            }

            rst.close();
            stmt.close();

            return results;

    } catch (SQLException e) {
        System.err.println(e.getMessage());
    }

    return results;
    }

    // Member Variables
    private Connection oracle;
    private final String UsersTable = FakebookOracleConstants.UsersTable;
    private final String CitiesTable = FakebookOracleConstants.CitiesTable;
    private final String FriendsTable = FakebookOracleConstants.FriendsTable;
    private final String CurrentCitiesTable = FakebookOracleConstants.CurrentCitiesTable;
    private final String HometownCitiesTable = FakebookOracleConstants.HometownCitiesTable;
    private final String ProgramsTable = FakebookOracleConstants.ProgramsTable;
    private final String EducationTable = FakebookOracleConstants.EducationTable;
    private final String EventsTable = FakebookOracleConstants.EventsTable;
    private final String AlbumsTable = FakebookOracleConstants.AlbumsTable;
    private final String PhotosTable = FakebookOracleConstants.PhotosTable;
    private final String TagsTable = FakebookOracleConstants.TagsTable;
}
