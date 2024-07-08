SELECT B.Title, E.Publication_Date, A.Author_ID, A.First_Name, A.Last_Name
FROM Books B, Authors A, Editions E, ( 
  SELECT DISTINCT B.Author_ID
  FROM Editions E, Books B
  WHERE TO_TIMESTAMP(E.Publication_Date, 'YYYY-MM-DD') >= TO_TIMESTAMP('2003-01-01', 'YYYY-MM-DD')
        AND TO_TIMESTAMP(E.Publication_Date, 'YYYY-MM-DD')  <= TO_TIMESTAMP('2008-12-31', 'YYYY-MM-DD') 
        AND B.Book_ID = E.Book_ID
) inner
WHERE A.Author_ID = inner.Author_ID AND B.Author_ID = inner.Author_ID AND E.Book_ID = B.Book_ID
ORDER BY 
  A.Author_ID ASC, 
  B.Title ASC,
  TO_TIMESTAMP(E.Publication_Date, 'YYYY-MM-DD') DESC;