ALTER TABLE appointments ADD (
	new tinyint(4) NOT NULL default '0',
	modified tinyint(4) NOT NULL default '0',
	deleted tinyint(4) NOT NULL default '0',
	alarm varchar(1) default NULL,
	reminders text
);

ALTER TABLE addresses ADD (
	new tinyint(4) NOT NULL default '0',
	modified tinyint(4) NOT NULL default '0',
	deleted tinyint(4) NOT NULL default '0'
);