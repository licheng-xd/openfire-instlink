INSERT INTO ofVersion (name, version) VALUES ('pubsub', 0);

-- pubsub_node
-- Used by: mod_pubsub
--
CREATE TABLE pubsub_node (
    `collectionowner` varchar(255) NOT NULL, KEY(`collectionowner`(255)),
    `object-sequence` BIGINT NOT NULL AUTO_INCREMENT, PRIMARY KEY(`object-sequence`),
    `creator` varchar(128) NOT NULL,
    `name` varchar(128),
    `type` varchar(64) NOT NULL,
    `description` TEXT ,
    `parent` varchar(128),
    
    `password` varchar(32),
    `nqos` varchar(32),
    `icqos` varchar(32),
    `deadline` INT,
    item_lifecycle INT
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 记录节点订阅信息
-- Used by: mod_pubsub
--
CREATE TABLE `subscriptions` (
    `collectionowner` varchar(255) NOT NULL, KEY(`collectionowner`(255)),
    `subscriber` varchar(128) NOT NULL, KEY(`subscriber`(128)),
    `nsqos` varchar(32),
    `isqos` varchar(32),
    `history_range` varchar(32),
    `deadline` INT,
    INDEX(`collectionowner`),
    FOREIGN KEY(`collectionowner`) REFERENCES `pubsub_node`(`collectionowner`) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- 记录用户订阅了的节点
-- Used by: mod_pubsub
CREATE TABLE `user_subscriptions` (
    `collectionowner` varchar(255) NOT NULL, KEY(`collectionowner`(255)),
    `node` varchar(128) NOT NULL, KEY(`node`(128)),
    `type` varchar(64) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;
    

-- 记录节点的历史信息
-- Used by: mod_pubsub
--
CREATE TABLE `history` (
    `collectionowner` varchar(255) NOT NULL, KEY(`collectionowner`(255)),
    `ID` varchar(128) NOT NULL, KEY(`ID`),
    `publisher` varchar(128) NOT NULL,
    `content` TEXT ,
    `time` varchar(128),
    `status` varchar(128),
    `handler` varchar(128),
    `subid` varchar(128) NOT NULL, KEY(`subid`),
    
    `type` varchar(32),
    `name` varchar(128),
    `body` TEXT,
    `iqos` varchar(32),
    `delay` INT,
    `deadline` INT,
    
    INDEX(`collectionowner`),
    FOREIGN KEY(`collectionowner`) REFERENCES `pubsub_node`(`collectionowner`) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- INSERT INTO `pubsub_node`(
--   `collectionowner`, `creator`, `name`, `type`, `description`, `history`, `offline`, `access_model`, `leaf`) --VALUES('root@yangshuai.csi', 'administrator@yangshuai.csi', 'root', 'normal', 'This is a root node. It always exists', --0, 0, 0, 0);

-- tp081023记录会议的离线消息
CREATE TABLE `offline`(
    `collectionowner` varchar(128) NOT NULL,KEY(`collectionowner`(128)),
    `node` varchar(128) NOT NULL,KEY(`node`(128)),
    `sequence` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY(`sequence`),
    `time` timestamp,
    `lifecycle` varchar(32),
    `xml` MEDIUMTEXT,
    `delay` int,
    `deadline` int) DEFAULT CHARSET=utf8;
    
-- 记录节点的主题信息
-- Used by: mod_pubsub
--
CREATE TABLE `subjects` (
    `collectionowner` varchar(255) NOT NULL, KEY(`collectionowner`(255)),
    `subid` varchar(128),
    `type` varchar(32),
    `name` varchar(128),
    INDEX(`collectionowner`),
    FOREIGN KEY(`collectionowner`) REFERENCES `pubsub_node`(`collectionowner`) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- pubsub_personal
-- Used by: sm: mod_pubsub
--
CREATE TABLE `pubsub_personal` (
    `collectionowner` varchar(255) NOT NULL, KEY(`collectionowner`(255)),
    `object-sequence` BIGINT NOT NULL AUTO_INCREMENT, PRIMARY KEY(`object-sequence`),
    `creator` varchar(128) NOT NULL,
    `name` varchar(128),
    `type` varchar(64) NOT NULL,
    `description` TEXT ,
    `parent` varchar(128),
    
    `password` varchar(32),
    `nqos` varchar(32),
    `icqos` varchar(32),
    `deadline` varchar(32),
    item_lifecycle varchar(32)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 记录个人节点的主题信息
-- Used by:sm: mod_pubsub
--
CREATE TABLE `personal_subjects` (
    `collectionowner` varchar(255) NOT NULL, KEY(`collectionowner`(255)),
    `subid` varchar(128),
    `type` varchar(32),
    `name` varchar(128),
    INDEX(`collectionowner`),
    FOREIGN KEY(`collectionowner`) REFERENCES `pubsub_personal`(`collectionowner`) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 记录个人节点的历史信息
-- Used by:sm: mod_pubsub
--
CREATE TABLE `personal_history` (
    `collectionowner` varchar(255) NOT NULL, KEY(`collectionowner`(255)),
    `ID` varchar(128) NOT NULL, KEY(`ID`),
    `publisher` varchar(128),
    `content` TEXT ,
    `time` varchar(128),
    `status` varchar(128),
    `handler` varchar(128),
    `subid` varchar(128) NOT NULL, KEY(`subid`),
    
    `type` varchar(32),
    `name` varchar(128),
    `body` TEXT,
    `iqos` varchar(32),
    `delay` INT,
    `deadline` INT,
    
    INDEX(`collectionowner`),
    FOREIGN KEY(`collectionowner`) REFERENCES `pubsub_personal`(`collectionowner`) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `pubsub_smallconf` (
  `collectionowner` varchar(255) NOT NULL,
  `object-sequence` bigint(20) NOT NULL auto_increment,
  `creator` varchar(128) NOT NULL,
  `name` varchar(128) default NULL,
  `type` varchar(64) NOT NULL,
  `description` text,
  `parent` varchar(128) default NULL,
  `password` varchar(32) default NULL,
  `nqos` varchar(32) default NULL,
  `icqos` varchar(32) default NULL,
  PRIMARY KEY  (`object-sequence`),
  KEY `collectionowner` (`collectionowner`)
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8;


CREATE TABLE `smallconf_subscriptions` (
  `collectionowner` varchar(255) NOT NULL,
  `subscriber` varchar(128) NOT NULL,
  `nsqos` varchar(32) default NULL,
  `isqos` varchar(32) default NULL,
  KEY `collectionowner` (`collectionowner`),
  KEY `subscriber` (`subscriber`),
  KEY `collectionowner_2` (`collectionowner`),
  CONSTRAINT `smallconf_subscriptions_ibfk_1` FOREIGN KEY (`collectionowner`) REFERENCES `pubsub_smallconf` (`collectionowner`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



