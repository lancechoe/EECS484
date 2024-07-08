SELECT SID 
FROM (
  SELECT S.SID
  FROM Students S, Courses C1, Courses C2, Courses C3, Enrollments E1, Enrollments E2, Enrollments E3
  WHERE S.SID = E1.SID AND S.SID = E2.SID AND S.SID = E3.SID AND E1.CID = C1.CID AND E2.CID = C2.CID AND E3.CID = C3.CID AND
        C1.C_Name = 'EECS442' AND C2.C_NAME = 'EECS445' AND C3.C_NAME = 'EECS492'
  UNION 
  SELECT S.SID
  FROM Students S, Courses C1, Courses C2, Enrollments E1, Enrollments E2
  WHERE S.SID = E1.SID AND S.SID = E2.SID AND E1.CID = C1.CID AND E2.CID = C2.CID AND
        C1.C_Name = 'EECS482' AND C2.C_NAME = 'EECS486'
  UNION 
  SELECT S.SID
  FROM Students S, Courses C1, Enrollments E1
  WHERE S.SID = E1.SID AND E1.CID = C1.CID AND C1.C_Name = 'EECS281'
) 
ORDER BY SID;