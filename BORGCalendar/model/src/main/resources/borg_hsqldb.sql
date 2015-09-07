
-- 
-- Table structure for table addresses
-- 

CREATE CACHED TABLE addresses (
  address_num integer default '0' NOT NULL,
  first_name varchar(25) default NULL,
  last_name varchar(25) default NULL,
  nickname varchar(25) default NULL,
  email varchar(50) default NULL,
  screen_name varchar(25) default NULL,
  work_phone varchar(25) default NULL,
  home_phone varchar(25) default NULL,
  fax varchar(25) default NULL,
  pager varchar(25) default NULL,
  street varchar(40) default NULL,
  city varchar(25) default NULL,
  state varchar(25) default NULL,
  zip varchar(25) default NULL,
  country varchar(25) default NULL,
  company varchar(25) default NULL,
  work_street varchar(40) default NULL,
  work_city varchar(25) default NULL,
  work_state varchar(25) default NULL,
  work_zip varchar(25) default NULL,
  work_country varchar(25) default NULL,
  webpage varchar(100) default NULL,
  notes longvarchar,
  birthday date default NULL,
  cell_phone varchar(25) default NULL,
  PRIMARY KEY  (address_num)
);

-- --------------------------------------------------------

-- 
-- Table structure for table appointments
-- 

CREATE CACHED TABLE appointments (
  appt_date datetime default '1000-01-01 00:00:00' NOT NULL,
  appt_num integer default '0' NOT NULL,
  duration integer default NULL,
  text longvarchar NOT NULL,
  skip_list longvarchar,
  next_todo date default NULL,
  vacation integer default '0' NOT NULL,
  holiday integer default '0' NOT NULL,
  private integer default '0' NOT NULL,
  times integer default '0' NOT NULL,
  frequency varchar(20) default NULL,
  todo integer default '0' NOT NULL,
  color varchar(10) default NULL,
  rpt integer default '0' NOT NULL,
  category varchar(255) default NULL,
  reminders longvarchar,
  untimed char(1) default NULL,
  encrypted char(1) default NULL,
  repeat_until date default NULL,
  priority integer default '5' NOT NULL,
  create_time datetime default '1980-01-01 00:00:00' NOT NULL,
  lastmod datetime default '1980-01-01 00:00:00' NOT NULL,
  uid longvarchar,
  url longvarchar,

  PRIMARY KEY  (appt_num)
);

CREATE INDEX app_todo ON appointments (todo);

-- --------------------------------------------------------

-- 
-- Table structure for table options
-- 

CREATE CACHED TABLE options (
  name varchar(30) default '' NOT NULL,
  value longvarchar NOT NULL,
  PRIMARY KEY  (name)
);

CREATE CACHED TABLE projects (
  id integer default '0' NOT NULL,
  start_date date NOT NULL ,
  due_date date default NULL,
  description longvarchar NOT NULL,
  category varchar(255) default NULL,
  status varchar(10) default '' NOT NULL,
  parent integer default NULL,
  FOREIGN KEY (parent) REFERENCES projects ( id)
     ON DELETE CASCADE,
  PRIMARY KEY  (id)
);

CREATE CACHED TABLE tasks (
  tasknum integer default '0' NOT NULL,
  start_date date NOT NULL ,
  due_date date default NULL,
  person_assigned varchar(10) default NULL,
  priority integer default '3' NOT NULL,
  state varchar(10) default '' NOT NULL,
  type varchar(10) default '' NOT NULL,
  description longvarchar,
  resolution longvarchar,
  category varchar(255) default NULL,
  close_date date default NULL,
  project integer default NULL,
  summary longvarchar NOT NULL,
  create_time datetime default '1980-01-01 00:00:00' NOT NULL,
  lastmod datetime default '1980-01-01 00:00:00' NOT NULL,
  uid longvarchar,
  url longvarchar,
  PRIMARY KEY  (tasknum),
  FOREIGN KEY (project) REFERENCES projects ( id)
     ON DELETE CASCADE
);


CREATE CACHED TABLE subtasks (
  id integer default '0' NOT NULL,
  create_date date NOT NULL ,
  due_date date default NULL,
  close_date date default NULL,
  description longvarchar NOT NULL,
  task integer default '0' NOT NULL,
   create_time datetime default '1980-01-01 00:00:00' NOT NULL,
  lastmod datetime default '1980-01-01 00:00:00' NOT NULL,
  uid longvarchar,
  url longvarchar,
  PRIMARY KEY  (id),
  FOREIGN KEY (task) REFERENCES tasks ( tasknum)
     ON DELETE CASCADE
);


CREATE CACHED TABLE tasklog (
  id integer default '0' NOT NULL,
  logtime datetime default '1000-01-01 00:00:00' NOT NULL,
  description longvarchar NOT NULL,
  task integer default '0' NOT NULL,
  PRIMARY KEY ( id ),
  FOREIGN KEY (task) REFERENCES tasks ( tasknum )
     ON DELETE CASCADE
);

CREATE CACHED TABLE memos (
  memoname varchar(50) NOT NULL,
  memotext longvarchar,
  encrypted char(1) default NULL,
  PRIMARY KEY  (memoname)
);


CREATE CACHED TABLE links (
  id integer default '0' NOT NULL,
  linktype varchar(15) NOT NULL,
  ownerkey integer default '0' NOT NULL,
  ownertype varchar(15),
  path varchar(250),
  PRIMARY KEY  (id)
);

CREATE CACHED TABLE checklists (
  name varchar(50) NOT NULL,
  text longvarchar,
  PRIMARY KEY  (name)
);
