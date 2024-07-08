SELECT B.Title, inner.Total_Pages
FROM Books B,
( SELECT E.Book_ID, SUM(E.Pages) as Total_Pages
  FROM Editions E
  GROUP BY E.Book_ID
) inner
WHERE B.Book_ID = inner.Book_ID
ORDER BY inner.Total_Pages DESC;