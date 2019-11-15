CREATE INDEX seats_index
ON Plane(seats);

CREATE INDEX planeid_index
ON Repairs(plane_id);

CREATE INDEX repairdate_index
ON Repairs(repair_date);

CREATE INDEX status_index
ON Reservation(status);
