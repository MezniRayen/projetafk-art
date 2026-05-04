<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260406154548 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE investissement (id_investissement BIGINT AUTO_INCREMENT NOT NULL, montant NUMERIC(10, 2) NOT NULL, date_investissement DATETIME NOT NULL, moyen_paiement VARCHAR(20) NOT NULL, statut VARCHAR(20) DEFAULT \'VALIDE\' NOT NULL, palier VARCHAR(100) NOT NULL, message_soutien VARCHAR(255) NOT NULL, id_investisseur BIGINT NOT NULL, id_projet BIGINT NOT NULL, INDEX IDX_B8E64E01724A5452 (id_investisseur), INDEX IDX_B8E64E0176222944 (id_projet), PRIMARY KEY (id_investissement)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE projet_artistique (id_projet BIGINT AUTO_INCREMENT NOT NULL, titre VARCHAR(150) NOT NULL, description LONGTEXT NOT NULL, objectif_financier NUMERIC(12, 2) NOT NULL, montant_collecte NUMERIC(12, 2) DEFAULT 0 NOT NULL, date_creation DATE NOT NULL, date_limite DATE NOT NULL, statut VARCHAR(20) DEFAULT \'EN_ATTENTE\' NOT NULL, visibilite TINYINT DEFAULT 1 NOT NULL, categorie VARCHAR(100) NOT NULL, id_artiste BIGINT NOT NULL, INDEX IDX_493A09F7429A9C3F (id_artiste), PRIMARY KEY (id_projet)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE users (idUser BIGINT AUTO_INCREMENT NOT NULL, nom VARCHAR(50) NOT NULL, prenom VARCHAR(50) NOT NULL, email VARCHAR(100) NOT NULL, motDePasse VARCHAR(255) NOT NULL, role VARCHAR(20) DEFAULT \'INVESTISSEUR\' NOT NULL, dateCreation DATETIME NOT NULL, statut VARCHAR(20) DEFAULT \'ACTIF\' NOT NULL, UNIQUE INDEX UNIQ_1483A5E9E7927C74 (email), PRIMARY KEY (idUser)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('CREATE TABLE messenger_messages (id BIGINT AUTO_INCREMENT NOT NULL, body LONGTEXT NOT NULL, headers LONGTEXT NOT NULL, queue_name VARCHAR(190) NOT NULL, created_at DATETIME NOT NULL, available_at DATETIME NOT NULL, delivered_at DATETIME DEFAULT NULL, INDEX IDX_75EA56E0FB7336F0E3BD61CE16BA31DBBF396750 (queue_name, available_at, delivered_at, id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4');
        $this->addSql('ALTER TABLE investissement ADD CONSTRAINT FK_B8E64E01724A5452 FOREIGN KEY (id_investisseur) REFERENCES users (idUser) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE investissement ADD CONSTRAINT FK_B8E64E0176222944 FOREIGN KEY (id_projet) REFERENCES projet_artistique (id_projet) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE projet_artistique ADD CONSTRAINT FK_493A09F7429A9C3F FOREIGN KEY (id_artiste) REFERENCES users (idUser) ON DELETE CASCADE');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE investissement DROP FOREIGN KEY FK_B8E64E01724A5452');
        $this->addSql('ALTER TABLE investissement DROP FOREIGN KEY FK_B8E64E0176222944');
        $this->addSql('ALTER TABLE projet_artistique DROP FOREIGN KEY FK_493A09F7429A9C3F');
        $this->addSql('DROP TABLE investissement');
        $this->addSql('DROP TABLE projet_artistique');
        $this->addSql('DROP TABLE users');
        $this->addSql('DROP TABLE messenger_messages');
    }
}
