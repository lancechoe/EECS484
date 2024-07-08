// Query 2
// Unwind friends and create a collection called 'flat_users' where each document has the following schema:
// {
//   user_id:xxx
//   friends:xxx
// }
// Return nothing.

function unwind_friends(dbname) {
    db = db.getSiblingDB(dbname);

    // TODO: unwind friends
    db.users.aggregate([
        { $unwind: "$friends" },
        {
            $project: {
                _id: 0, // do not show the default _id
                user_id: "$user_id",
                friends: "$friends"
            }
        },
        { $out: "flat_users" } // output results to flat_users collection
    ]);
    
    return;
}
