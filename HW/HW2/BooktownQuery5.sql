SELECT Author_ID, First_Name, Last_Name
FROM Authors
WHERE Author_ID NOT IN ( 
  SELECT DISTINCT Author_ID FROM ( 
    SELECT Author_ID, Subject_ID
    FROM (
      SELECT Author_ID, Subject_ID
      FROM (
        SELECT DISTINCT S.Subject_ID
        FROM Subjects S, Authors A, Books B
        WHERE A.First_Name = 'J. K.' AND A.Last_Name = 'Rowling' 
        AND B.Author_ID = A.Author_ID AND B.Subject_ID = S.Subject_ID
      ) CROSS JOIN (
        SELECT DISTINCT Author_ID FROM Authors
      )
    )
    MINUS
    ( SELECT DISTINCT A.Author_ID, S.Subject_ID
      FROM Authors A, Books B, Subjects S
      WHERE A.Author_ID = B.Author_ID AND B.Subject_ID = S.Subject_ID
    ) 
  )
)
ORDER BY Last_Name ASC,
         Author_ID DESC;