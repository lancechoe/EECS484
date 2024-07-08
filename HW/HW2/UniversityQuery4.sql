CREATE VIEW StudentPairs AS
SELECT inner.SID1, inner.SID2
FROM (
    SELECT DISTINCT S1.SID AS SID1, S2.SID AS SID2 
    FROM Students S1, Students S2, Enrollments E1, Enrollments E2 
    WHERE
    S1.SID = E1.SID AND S1.SID != S2.SID AND S2.SID = E2.SID AND E1.CID = E2.CID AND S1.SID < S2.SID
    MINUS 
    SELECT DISTINCT M1.SID, M2.SID 
    FROM Members M1, Members M2, Enrollments E1, Enrollments E2 
    WHERE 
    M1.SID = E1.SID AND M2.SID = E2.SID AND M1.SID < M2.SID AND M1.PID = M2.PID AND E1.CID = E2.CID
) inner; 