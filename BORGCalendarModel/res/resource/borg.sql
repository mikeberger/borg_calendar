# This file is only needed by users playing with the 
# experimental support for MYSQL. It is used
# to create the BORG data tables in MYSQL.
# By default, BORG uses its own db files to store data
# and a separate database is not required. See file
# RDBMS.txt for more information. 
# The vast majority of users can ignore this file.
#
# Table structure for table `appointments`
#

DROP TABLE IF EXISTS appointments;
CREATE TABLE appointments (
  appt_date timestamp(14) NOT NULL,
  appt_num int(11) NOT NULL default '0',
  userid int(11) NOT NULL default '0',
  duration int(11) default NULL,
  text text NOT NULL,
  skip_list text,
  next_todo date default NULL,
  vacation tinyint(4) NOT NULL default '0',
  holiday tinyint(4) NOT NULL default '0',
  private tinyint(4) NOT NULL default '0',
  times int(11) NOT NULL default '0',
  frequency varchar(10) default NULL,
  todo tinyint(4) NOT NULL default '0',
  color varchar(10) default NULL,
  repeat tinyint(4) NOT NULL default '0',
  category varchar(15) default NULL,
  PRIMARY KEY  (appt_num,userid),
  KEY todo (todo,userid),
  KEY userid (userid)
) TYPE=MyISAM;
# --------------------------------------------------------

#
# Table structure for table `options`
#

DROP TABLE IF EXISTS options;
CREATE TABLE options (
  name varchar(10) NOT NULL default '',
  userid int(11) NOT NULL default '0',
  value text NOT NULL,
  PRIMARY KEY  (name,userid)
) TYPE=MyISAM;
# --------------------------------------------------------

#
# Table structure for table `tasks`
#

DROP TABLE IF EXISTS tasks;
CREATE TABLE tasks (
  tasknum int(11) NOT NULL default '0',
  userid int(11) NOT NULL default '0',
  start_date date NOT NULL default '0000-00-00',
  due_date date default NULL,
  person_assigned varchar(10) default NULL,
  priority varchar(10) default NULL,
  state varchar(10) NOT NULL default '',
  type varchar(10) NOT NULL default '',
  description text NOT NULL,
  resolution text,
  todo_list varchar(20) default NULL,
  user_task1 varchar(25) default NULL,
  user_task2 varchar(25) default NULL,
  user_task3 varchar(25) default NULL,
  user_task4 varchar(25) default NULL,
  user_task5 varchar(25) default NULL,
  category varchar(10) default NULL,
  PRIMARY KEY  (tasknum, userid),
  KEY userid (userid)
) TYPE=MyISAM;
# --------------------------------------------------------

#
# Table structure for table `users`
#

DROP TABLE IF EXISTS users;
CREATE TABLE users (
  username varchar(25) NOT NULL default '',
  userid int(11) NOT NULL default '0',
  password varchar(25) NOT NULL default '',
  PRIMARY KEY  (username)
) TYPE=MyISAM;
# --------------------------------------------------------

#
# Table structure for table `addresses`
#

DROP TABLE IF EXISTS addresses;
CREATE TABLE addresses (
  address_num int(11) NOT NULL default '0',
  userid int(11) NOT NULL default '0',
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
  notes text default NULL,
  birthday date default NULL,
  PRIMARY KEY  (address_num, userid),
  KEY userid (userid)
) TYPE=MyISAM;
