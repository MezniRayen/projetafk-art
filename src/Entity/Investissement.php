<?php

namespace App\Entity;

use App\Repository\InvestissementRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: InvestissementRepository::class)]
#[ORM\Table(name: 'investissement')]
class Investissement
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_investissement', type: Types::BIGINT)]
    private ?string $id = null;

    #[ORM\ManyToOne(targetEntity: User::class, inversedBy: 'investissements')]
    #[ORM\JoinColumn(name: 'id_investisseur', referencedColumnName: 'idUser', nullable: false, onDelete: 'CASCADE')]
    private ?User $investisseur = null;

    #[ORM\ManyToOne(targetEntity: ProjetArtistique::class, inversedBy: 'investissements')]
    #[ORM\JoinColumn(name: 'id_projet', referencedColumnName: 'id_projet', nullable: false, onDelete: 'CASCADE')]
    private ?ProjetArtistique $projet = null;

    #[ORM\Column(type: Types::DECIMAL, precision: 10, scale: 2)]
    #[Assert\NotBlank]
    #[Assert\Positive]
    private ?string $montant = null;

    #[ORM\Column(name: 'date_investissement', type: Types::DATETIME_MUTABLE)]
    private \DateTimeInterface $dateInvestissement;

    #[ORM\Column(name: 'moyen_paiement', length: 20)]
    #[Assert\Choice(choices: ['CARTE', 'VIREMENT', 'PAYPAL'])]
    private string $moyenPaiement = 'CARTE';

    #[ORM\Column(length: 20, options: ['default' => 'VALIDE'])]
    #[Assert\Choice(choices: ['EN_ATTENTE', 'VALIDE', 'ANNULE'])]
    private string $statut = 'VALIDE';

    #[ORM\Column(length: 100)]
    #[Assert\NotBlank]
    #[Assert\Regex(pattern: '/^[^<>]*$/u', message: 'HTML tags are not allowed.')]
    private string $palier = '';

    #[ORM\Column(name: 'message_soutien', length: 255)]
    #[Assert\NotBlank]
    #[Assert\Regex(pattern: '/^[^<>]*$/u', message: 'HTML tags are not allowed.')]
    private string $messageSoutien = '';

    public function __construct()
    {
        $this->dateInvestissement = new \DateTime();
    }

    public function getId(): ?string
    {
        return $this->id;
    }

    public function getInvestisseur(): ?User
    {
        return $this->investisseur;
    }

    public function setInvestisseur(?User $investisseur): self
    {
        $this->investisseur = $investisseur;

        return $this;
    }

    public function getProjet(): ?ProjetArtistique
    {
        return $this->projet;
    }

    public function setProjet(?ProjetArtistique $projet): self
    {
        $this->projet = $projet;

        return $this;
    }

    public function getMontant(): ?string
    {
        return $this->montant;
    }

    public function setMontant(?string $montant): self
    {
        $this->montant = $montant;

        return $this;
    }

    public function getDateInvestissement(): \DateTimeInterface
    {
        return $this->dateInvestissement;
    }

    public function setDateInvestissement(\DateTimeInterface $dateInvestissement): self
    {
        $this->dateInvestissement = $dateInvestissement;

        return $this;
    }

    public function getMoyenPaiement(): string
    {
        return $this->moyenPaiement;
    }

    public function setMoyenPaiement(string $moyenPaiement): self
    {
        $this->moyenPaiement = $moyenPaiement;

        return $this;
    }

    public function getStatut(): string
    {
        return $this->statut;
    }

    public function setStatut(string $statut): self
    {
        $this->statut = $statut;

        return $this;
    }

    public function getPalier(): string
    {
        return $this->palier;
    }

    public function setPalier(string $palier): self
    {
        $this->palier = $palier;

        return $this;
    }

    public function getMessageSoutien(): string
    {
        return $this->messageSoutien;
    }

    public function setMessageSoutien(string $messageSoutien): self
    {
        $this->messageSoutien = $messageSoutien;

        return $this;
    }
}
