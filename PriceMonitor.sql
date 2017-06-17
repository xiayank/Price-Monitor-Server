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

 Date: 06/17/2017 15:11:36 PM
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
  `NewPrice` double DEFAULT NULL,
  `Reduced_Percentage` double DEFAULT NULL,
  `Category` varchar(255) DEFAULT NULL,
  `URL` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ProductId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;
