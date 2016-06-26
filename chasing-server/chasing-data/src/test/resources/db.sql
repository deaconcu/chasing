CREATE DATABASE `db_chasing` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `db_chasing`

CREATE TABLE `t_user` (
  `pk_id` bigint(20) NOT NULL,
  `name` varchar(16) NOT NULL DEFAULT '',
  `email` varchar(64) NOT NULL DEFAULT '',
  `phone` varchar(32) NOT NULL DEFAULT '',
  `register_time` datetime NOT NULL DEFAULT '2016-01-01 08:00:00',
  `register_ip` varchar(32) NOT NULL DEFAULT '',
  `login_time` datetime NOT NULL DEFAULT '2016-01-01 08:00:00',
  `last_login_ip` varchar(32) NOT NULL DEFAULT '',
  PRIMARY KEY (`pk_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
