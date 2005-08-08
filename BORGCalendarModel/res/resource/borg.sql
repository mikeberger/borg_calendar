-- phpMyAdmin SQL Dump
-- version 2.6.1-pl3
-- http://www.phpmyadmin.net
-- 
-- Host: localhost
-- Generation Time: Apr 29, 2005 at 06:36 PM
-- Server version: 4.1.7
-- PHP Version: 4.3.9
-- 
-- Database: `borg`
-- 

-- --------------------------------------------------------

-- 
-- Table structure for table `addresses`
-- 

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
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

-- 
-- Table structure for table `appointments`
-- 

CREATE TABLE `appointments` (
  `appt_date` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
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
  `frequency` varchar(10) default NULL,
  `todo` tinyint(4) NOT NULL default '0',
  `color` varchar(10) default NULL,
  `repeat` tinyint(4) NOT NULL default '0',
  `category` varchar(15) default NULL,
  `new` tinyint(4) NOT NULL default '0',
  `modified` tinyint(4) NOT NULL default '0',
  `deleted` tinyint(4) NOT NULL default '0',
  `alarm` char(1) default NULL,
  `reminders` text,
  PRIMARY KEY  (`appt_num`,`username`),
  KEY `todo` (`todo`,`username`),
  KEY `username` (`username`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

-- 
-- Table structure for table `options`
-- 

CREATE TABLE `options` (
  `name` varchar(10) NOT NULL default '',
  `username` varchar(25) NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY  (`name`,`username`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

-- 
-- Table structure for table `tasks`
-- 

CREATE TABLE `tasks` (
  `tasknum` int(11) NOT NULL default '0',
  `username` varchar(25) NOT NULL,
  `start_date` date NOT NULL default '0000-00-00',
  `due_date` date default NULL,
  `person_assigned` varchar(10) default NULL,
  `priority` varchar(10) default NULL,
  `state` varchar(10) NOT NULL default '',
  `type` varchar(10) NOT NULL default '',
  `description` text NOT NULL,
  `resolution` text,
  `todo_list` varchar(20) default NULL,
  `user_task1` varchar(25) default NULL,
  `user_task2` varchar(25) default NULL,
  `user_task3` varchar(25) default NULL,
  `user_task4` varchar(25) default NULL,
  `user_task5` varchar(25) default NULL,
  `category` varchar(10) default NULL,
  PRIMARY KEY  (`tasknum`,`username`),
  KEY `username` (`username`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

-- 
-- Table structure for table `users`
-- 

CREATE TABLE `users` (
  `username` varchar(25) NOT NULL default '',
  `password` varchar(25) NOT NULL default '',
  PRIMARY KEY  (`username`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
