-- 6.For flight number and date, find the number of availalbe seats (i.e. total plane capacity minus booked seats )
SELECT P.seats - F.num_sold FROM Flight F, Plane P, FlightInfo I WHERE F.fnum = I.flight_id AND I.flight_id = P.id AND F.fnum = 1;

-- 7. Count number of repairs per planes and list them in descending order.
SELECT plane.id, (SELECT COUNT(*) FROM Repairs R WHERE R.plane_id = plane.id) AS RepairCount FROM Plane ORDER BY RepairCount DESC;

-- 8. Count repairs per year and list them in ascending order.
SELECT EXTRACT(year FROM repair_date) AS Year, COUNT(*) FROM Repairs GROUP BY EXTRACT(year FROM repair_date) ORDER BY count ASC;

-- 9. Find how many passengers there are with a status (i.e. W,C,R) and list that number.
SELECT COUNT(*) FROM Reservation WHERE Reservation.fid = 1 AND Reservation.status = 'R';
