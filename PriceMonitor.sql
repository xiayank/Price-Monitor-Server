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

 Date: 06/12/2017 11:37:29 AM
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `PriceMonitor`
-- ----------------------------
DROP TABLE IF EXISTS `PriceMonitor`;
CREATE TABLE `PriceMonitor` (
  `ProductId` varchar(255) NOT NULL,
  `Title` varchar(255) DEFAULT NULL,
  `OldPrice` double DEFAULT NULL,
  `NewPirce` double DEFAULT NULL,
  `Flag` tinyint(4) DEFAULT NULL,
  `Category` varchar(255) DEFAULT NULL,
  `URL` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ProductId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;
