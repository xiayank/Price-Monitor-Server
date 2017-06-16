/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 50718
 Source Host           : localhost
 Source Database       : project

 Target Server Type    : MySQL
 Target Server Version : 50718
 File Encoding         : utf-8

 Date: 06/16/2017 11:25:18 AM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `Users`
-- ----------------------------
DROP TABLE IF EXISTS `Users`;
CREATE TABLE `Users` (
  `username` varchar(255) NOT NULL,
  `subscribe` varchar(255) DEFAULT NULL,
  `Email` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Records of `Users`
-- ----------------------------
BEGIN;
INSERT INTO `Users` VALUES ('Alex', 'Sports&Outdoors', 'yan.xia.cs@gmail.com'), ('Bob', 'Electronics&Computer', 'xiayan_cpp@qq.com');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
