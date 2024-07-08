// Query 1
// Find users who live in city "city".
// Return an array of user_ids. The order does not matter.

function find_user(city, dbname) {
    db = db.getSiblingDB(dbname);

    let results = [];
    // TODO: find all users who live in city
    // db.users.find(...);

    var cursor = db.users.find({ "hometown.city": city });

    // Iterate over the cursor and extract user ids
    cursor.forEach(function(doc) {
        results.push(doc.user_id);
    });

    return results;
}
