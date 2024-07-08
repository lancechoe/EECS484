// Query 4
// Find user pairs (A,B) that meet the following constraints:
// i) user A is male and user B is female
// ii) their Year_Of_Birth difference is less than year_diff
// iii) user A and B are not friends
// iv) user A and B are from the same hometown city
// The following is the schema for output pairs:
// [
//      [user_id1, user_id2],
//      [user_id1, user_id3],
//      [user_id4, user_id2],
//      ...
//  ]
// user_id is the field from the users collection. Do not use the _id field in users.
// Return an array of arrays.

function suggest_friends(year_diff, dbname) {
    db = db.getSiblingDB(dbname);

    let pairs = [];
    // TODO: implement suggest friends

    db.users.find({"gender":"male"}).forEach(function(A){
        db.users.find({
        $and: [
        { "gender":"female" },
        { "hometown.city": A.hometown.city}
        ]
        }).forEach(function(B) {
            var diff = Math.abs(A.YOB - B.YOB);
            if(diff < year_diff){
                var isAFriendOfB = B.friends && B.friends.indexOf(A.user_id) != -1;
                var isBFriendOfA = A.friends && A.friends.indexOf(B.user_id) != -1;
                if (!isAFriendOfB && !isBFriendOfA) {
                    pairs.push([A.user_id, B.user_id]);
                }
            }
        });
    });

    return pairs;
}
