// Query 6
// Find the average friend count per user.
// Return a decimal value as the average user friend count of all users in the users collection.

function find_average_friendcount(dbname) {
    db = db.getSiblingDB(dbname);

    // TODO: calculate the average friend count

    let sum = 0;
    let total = 0;
    db.users.find().forEach(function(U) {
        sum += U.friends.length;
        ++total;
    });

    return sum/total;
}
