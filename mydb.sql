-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Feb 16, 2026 at 01:57 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `mydb`
--

-- --------------------------------------------------------

--
-- Table structure for table `investissement`
--

CREATE TABLE `investissement` (
  `id_investissement` int(11) NOT NULL,
  `id_investisseur` int(11) NOT NULL,
  `id_projet` int(11) NOT NULL,
  `montant` decimal(10,2) NOT NULL,
  `date_investissement` datetime DEFAULT current_timestamp(),
  `moyen_paiement` enum('CARTE','VIREMENT','PAYPAL') NOT NULL,
  `statut` enum('EN_ATTENTE','VALIDE','ANNULE') DEFAULT 'VALIDE',
  `palier` varchar(100) DEFAULT NULL,
  `message_soutien` varchar(255) DEFAULT NULL
) ;

-- --------------------------------------------------------

--
-- Table structure for table `projet_artistique`
--

CREATE TABLE `projet_artistique` (
  `id_projet` int(11) NOT NULL,
  `id_artiste` int(11) NOT NULL,
  `titre` varchar(150) NOT NULL,
  `description` text DEFAULT NULL,
  `objectif_financier` decimal(12,2) NOT NULL,
  `montant_collecte` decimal(12,2) DEFAULT 0.00,
  `date_creation` date NOT NULL,
  `date_limite` date DEFAULT NULL,
  `statut` enum('EN_ATTENTE','EN_COURS','FINANCE','ECHEC') DEFAULT 'EN_ATTENTE',
  `visibilite` tinyint(1) DEFAULT 1,
  `categorie` varchar(100) DEFAULT NULL
) ;

-- --------------------------------------------------------
--
-- Table structure for table `favori_projet`
--

CREATE TABLE `favori_projet` (
  `id_favori` int(11) NOT NULL,
  `id_investisseur` int(11) NOT NULL,
  `id_projet` int(11) NOT NULL,
  `date_favori` datetime DEFAULT current_timestamp()
) ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `investissement`
--
ALTER TABLE `investissement`
  ADD PRIMARY KEY (`id_investissement`),
  ADD KEY `fk_investissement_projet` (`id_projet`);

--
-- Indexes for table `projet_artistique`
--
ALTER TABLE `projet_artistique`
  ADD PRIMARY KEY (`id_projet`);

--
-- Indexes for table `favori_projet`
--
ALTER TABLE `favori_projet`
  ADD PRIMARY KEY (`id_favori`),
  ADD KEY `fk_favori_projet_projet` (`id_projet`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `investissement`
--
ALTER TABLE `investissement`
  MODIFY `id_investissement` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `projet_artistique`
--
ALTER TABLE `projet_artistique`
  MODIFY `id_projet` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `favori_projet`
--
ALTER TABLE `favori_projet`
  MODIFY `id_favori` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `investissement`
--
ALTER TABLE `investissement`
  ADD CONSTRAINT `fk_investissement_projet` FOREIGN KEY (`id_projet`) REFERENCES `projet_artistique` (`id_projet`) ON DELETE CASCADE;
--
-- Constraints for table `favori_projet`
--
ALTER TABLE `favori_projet`
  ADD CONSTRAINT `fk_favori_projet_projet` FOREIGN KEY (`id_projet`) REFERENCES `projet_artistique` (`id_projet`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
