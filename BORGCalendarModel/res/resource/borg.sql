CREATE TABLE `addresses` (
  `address_num` int(11) NOT NULL default '0',
  `username` varchar(25) NOT NULL,
  `first_name` varchar(25) default NULL,
  `last_name` varchar(25) default NULL,
  `nickname` varchar(25) default NULL,
  `email` varchar(50) default NULL,
  `screen_name` varchar(25) default NULL,
  `work_phone` varchar(25) default NULL,
  `home_phone` varchar(25) default NULL,
  `fax` varchar(25) default NULL,
  `pager` varchar(25) default NULL,
  `street` varchar(25) default NULL,
  `city` varchar(25) default NULL,
  `state` varchar(25) default NULL,
  `zip` varchar(25) default NULL,
  `country` varchar(25) default NULL,
  `company` varchar(25) default NULL,
  `work_street` varchar(25) default NULL,
  `work_city` varchar(25) default NULL,
  `work_state` varchar(25) default NULL,
  `work_zip` varchar(25) default NULL,
  `work_country` varchar(25) default NULL,
  `webpage` varchar(100) default NULL,
  `notes` text,
  `birthday` date default NULL,
  `new` tinyint(4) NOT NULL default '0',
  `modified` tinyint(4) NOT NULL default '0',
  `deleted` tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (`address_num`,`username`),
  KEY `username` (`username`)
);


CREATE TABLE `appointments` (
  `appt_date` timestamp NOT NULL default '0000-00-00 00:00:00',
  `appt_num` int(11) NOT NULL default '0',
  `username` varchar(25) NOT NULL,
  `duration` int(11) default NULL,
  `text` text NOT NULL,
  `skip_list` text,
  `next_todo` date default NULL,
  `vacation` tinyint(4) NOT NULL default '0',
  `holiday` tinyint(4) NOT NULL default '0',
  `private` tinyint(4) NOT NULL default '0',
  `times` int(11) NOT NULL default '0',
  `frequency` varchar(20) default NULL,
  `todo` tinyint(4) NOT NULL default '0',
  `color` varchar(10) default NULL,
   rpt tinyint(4) NOT NULL default '0',
  `category` varchar(15) default NULL,
  `new` tinyint(4) NOT NULL default '0',
  `modified` tinyint(4) NOT NULL default '0',
  `deleted` tinyint(4) NOT NULL default '0',
  `alarm` char(1) default NULL,
  `reminders` text,
  PRIMARY KEY  (`appt_num`,`username`),
  KEY `todo` (`todo`,`username`),
  KEY `username` (`username`)
);


CREATE TABLE `options` (
  `name` varchar(10) NOT NULL default '',
  `username` varchar(25) NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY  (`name`,`username`)
);


CREATE TABLE projects (
  id int(11)  NOT NULL default '0',
  username varchar(25) NOT NULL,
  start_date date NOT NULL default '0000-00-00',
  due_date date default NULL,
  description text NOT NULL,
  category varchar(10) default NULL,
  status varchar(10) NOT NULL default '',
  PRIMARY KEY  (id,username),
  KEY `username` (`username`)
);


CREATE TABLE `tasks` (
  `tasknum` int(11) NOT NULL default '0',
  `username` varchar(25) NOT NULL,
  `start_date` date NOT NULL default '0000-00-00',
  `due_date` date default NULL,
  `person_assigned` varchar(10) default NULL,
  `priority` tinyint(4) default '3',
  `state` varchar(10) NOT NULL default '',
  `type` varchar(10) NOT NULL default '',
  `description` text NOT NULL,
  `resolution` text,
  `category` varchar(10) default NULL,
  `close_date` date default NULL,
  project integer default NULL,
  PRIMARY KEY  (`tasknum`,`username`),
  FOREIGN KEY (project, username) REFERENCES projects ( id, username )
     ON DELETE CASCADE,
  KEY `username` (`username`)
);

CREATE TABLE subtasks (
  id int(11) NOT NULL default '0' ,
  username varchar(25) NOT NULL,
  create_date date NOT NULL default '0000-00-00',
  due_date date default NULL default '0000-00-00',
  close_date date default NULL default '0000-00-00',
  description text NOT NULL,
  task integer NOT NULL default '0',
  PRIMARY KEY  (id,username),
  KEY `username` (`username`),
  FOREIGN KEY (task, username) REFERENCES tasks ( tasknum, username )
     ON DELETE CASCADE
);


CREATE TABLE tasklog (
  id int(11) NOT NULL default '0',
  username varchar(25) NOT NULL,
  logtime datetime NOT NULL default '0000-00-00 00:00:00',
  description text NOT NULL,
  task int(11) NOT NULL default '0' ,
  PRIMARY KEY ( id, username ),
  KEY `username` (`username`),
  FOREIGN KEY (task, username) REFERENCES tasks ( tasknum, username )
     ON DELETE CASCADE
);



CREATE TABLE memos (
  memoname varchar(50) NOT NULL,
  username varchar(25) NOT NULL,
  memotext text,
  palmid int(11),
  new int(11) NOT NULL default '0' ,
  modified int(11) NOT NULL default '0' ,
  deleted int(11)  NOT NULL default '0' ,
  PRIMARY KEY  (memoname,username),
  KEY `username` (`username`)
);

