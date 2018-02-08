CREATE SCHEMA IF NOT EXISTS `logdb` DEFAULT CHARACTER SET utf8 ;
USE `logdb` ;

CREATE TABLE IF NOT EXISTS `logdb`.`logs` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `copied_at` DATETIME default CURRENT_TIMESTAMP,
  `fileName` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;

CREATE USER IF NOT EXISTS 'admin'@'localhost' IDENTIFIED BY 'admin';

GRANT ALL ON `logdb`.* TO 'admin'@'localhost';