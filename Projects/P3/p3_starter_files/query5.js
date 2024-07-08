// Query 5
// Find the oldest friend for each user who has a friend. For simplicity,
// use only year of birth to determine age, if there is a tie, use the
// one with smallest user_id. You may find query 2 and query 3 helpful.
// You can create selections if you want. Do not modify users collection.
// Return a javascript object : key is the user_id and the value is the oldest_friend id.
// You should return something like this (order does not matter):
// {user1:userx1, user2:userx2, user3:userx3,...}

function oldest_friend(dbname) {
    db = db.getSiblingDB(dbname);

    let results = {};
    // TODO: implement oldest friends

    db.users.find().forEach(function(U){
        let yob = Number.MAX_SAFE_INTEGER;
        let uid = Number.MAX_SAFE_INTEGER;
        
        if(results.hasOwnProperty(U.user_id)){
            let overwrite = db.users.findOne({"user_id":results[U.user_id]});
            yob = overwrite.YOB;
            uid = overwrite.user_id;
        }
        let leng = U.friends.length;
        for (let i = 0; i < leng; ++i) {
            let temp = db.users.findOne({"user_id": U.friends[i]});
            if (temp.YOB < yob || (temp.YOB == yob && temp.user_id < uid)) {
                yob = temp.YOB;
                uid = temp.user_id;
            }
            results[U.user_id] = uid;
            if (results.hasOwnProperty(temp.user_id)){
                let check = db.users.findOne({"user_id":results[temp.user_id]});
                if(check.YOB > U.YOB || (check.YOB == U.YOB && check.user_id > U.user_id)){
                    results[temp.user_id] = U.user_id;
                }
            }
            else{
                results[temp.user_id] = U.user_id;
            }
        }
    });

    return results;
}
