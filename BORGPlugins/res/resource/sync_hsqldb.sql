

CREATE CACHED TABLE syncmap (
  id integer NOT NULL,
  objtype varchar(25) NOT NULL,
  action varchar(25) NOT NULL,
  PRIMARY KEY  (id,objtype)
);
