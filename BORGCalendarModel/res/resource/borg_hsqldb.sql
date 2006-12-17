
-- 
-- Table structure for table addresses
-- 

CREATE CACHED TABLE addresses (
  address_num integer default '0' NOT NULL,
  username varchar(25) NOT NULL,
  first_name varchar(25) default NULL,
  last_name varchar(25) default NULL,
  nickname varchar(25) default NULL,
  email varchar(50) default NULL,
  screen_name varchar(25) default NULL,
  work_phone varchar(25) default NULL,
  home_phone varchar(25) default NULL,
  fax varchar(25) default NULL,
  pager varchar(25) default NULL,
  street varchar(25) default NULL,
  city varchar(25) default NULL,
  state varchar(25) default NULL,
  zip varchar(25) default NULL,
  country varchar(25) default NULL,
  company varchar(25) default NULL,
  work_street varchar(25) default NULL,
  work_city varchar(25) default NULL,
  work_state varchar(25) default NULL,
  work_zip varchar(25) default NULL,
  work_country varchar(25) default NULL,
  webpage varchar(100) default NULL,
  notes longvarchar,
  birthday date default NULL,
  new integer default '0' NOT NULL,
  modified integer default '0' NOT NULL,
  deleted integer default '0' NOT NULL,
  PRIMARY KEY  (address_num,username)
);

CREATE INDEX ADDR_USER ON addresses (username);
-- --------------------------------------------------------

-- 
-- Table structure for table appointments
-- 

CREATE CACHED TABLE appointments (
  appt_date datetime default '0000-00-00 00:00:00' NOT NULL,
  appt_num integer default '0' NOT NULL,
  username varchar(25) NOT NULL,
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
  category varchar(15) default NULL,
  new integer default '0' NOT NULL,
  modified integer default '0' NOT NULL,
  deleted integer default '0' NOT NULL,
  alarm char(1) default NULL,
  reminders longvarchar,
  PRIMARY KEY  (appt_num,username)
);

CREATE INDEX app_todo ON appointments (todo);
CREATE INDEX app_user ON appointments (username);
-- --------------------------------------------------------

-- 
-- Table structure for table options
-- 

CREATE CACHED TABLE options (
  name varchar(10) default '' NOT NULL,
  username varchar(25) NOT NULL,
  value longvarchar NOT NULL,
  PRIMARY KEY  (name,username)
);

CREATE CACHED TABLE projects (
  id integer default '0' NOT NULL,
  username varchar(25) NOT NULL,
  start_date date NOT NULL ,
  due_date date default NULL,
  description longvarchar NOT NULL,
  category varchar(10) default NULL,
  status varchar(10) default '' NOT NULL,
  PRIMARY KEY  (id,username)
);
CREATE INDEX project_user ON projects (username);

CREATE CACHED TABLE tasks (
  tasknum integer default '0' NOT NULL,
  username varchar(25) NOT NULL,
  start_date date NOT NULL ,
  due_date date default NULL,
  person_assigned varchar(10) default NULL,
  priority integer default '3' NOT NULL,
  state varchar(10) default '' NOT NULL,
  type varchar(10) default '' NOT NULL,
  description longvarchar NOT NULL,
  resolution longvarchar,
  category varchar(10) default NULL,
  close_date date default NULL,
  project integer default NULL,
  PRIMARY KEY  (tasknum,username),
  FOREIGN KEY (project, username) REFERENCES projects ( id, username )
     ON DELETE CASCADE
);
CREATE INDEX task_user ON tasks (username);



-- --------------------------------------------------------

-- 
-- Table structure for table subtasks
-- 

CREATE CACHED TABLE subtasks (
  id integer default '0' NOT NULL,
  username varchar(25) NOT NULL,
  create_date date NOT NULL ,
  due_date date default NULL,
  close_date date default NULL,
  description longvarchar NOT NULL,
  task integer default '0' NOT NULL,
  PRIMARY KEY  (id,username),
  FOREIGN KEY (task, username) REFERENCES tasks ( tasknum, username )
     ON DELETE CASCADE
);
CREATE INDEX subtask_user ON subtasks (username);


CREATE CACHED TABLE tasklog (
  id integer default '0' NOT NULL,
  username varchar(25) NOT NULL,
  logtime datetime default '0000-00-00 00:00:00' NOT NULL,
  description longvarchar NOT NULL,
  task integer default '0' NOT NULL,
  PRIMARY KEY ( id, username ),
  FOREIGN KEY (task, username) REFERENCES tasks ( tasknum, username )
     ON DELETE CASCADE
);
CREATE INDEX tasklog_user ON tasklog (username);

CREATE CACHED TABLE memos (
  memoname varchar(50) NOT NULL,
  username varchar(25) NOT NULL,
  memotext longvarchar,
  new integer default '0' NOT NULL,
  modified integer default '0' NOT NULL,
  deleted integer default '0' NOT NULL,
  PRIMARY KEY  (memoname,username)
);
CREATE INDEX memo_user ON memos (username);
