<?php

namespace App\Entity;

use App\Repository\ProjetArtistiqueRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: ProjetArtistiqueRepository::class)]
#[ORM\Table(name: 'projet_artistique')]
class ProjetArtistique
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'id_projet', type: Types::BIGINT)]
    private ?string $id = null;

    #[ORM\ManyToOne(targetEntity: User::class, inversedBy: 'projets')]
    #[ORM\JoinColumn(name: 'id_artiste', referencedColumnName: 'idUser', nullable: false, onDelete: 'CASCADE')]
    private ?User $artiste = null;

    #[ORM\Column(length: 150)]
    #[Assert\NotBlank]
    #[Assert\Length(max: 150)]
    #[Assert\Regex(pattern: '/^[^<>]+$/u', message: 'HTML tags are not allowed.')]
    private string $titre = '';

    #[ORM\Column(type: Types::TEXT)]
    #[Assert\NotBlank]
    #[Assert\Regex(pattern: '/^[^<>]+$/u', message: 'HTML tags are not allowed.')]
    private string $description = '';

    #[ORM\Column(name: 'objectif_financier', type: Types::DECIMAL, precision: 12, scale: 2)]
    #[Assert\NotBlank]
    #[Assert\Positive]
    private ?string $objectifFinancier = null;

    #[ORM\Column(name: 'montant_collecte', type: Types::DECIMAL, precision: 12, scale: 2, options: ['default' => 0])]
    private string $montantCollecte = '0.00';

    #[ORM\Column(name: 'date_creation', type: Types::DATE_MUTABLE)]
    #[Assert\NotNull]
    private ?\DateTimeInterface $dateCreation = null;

    #[ORM\Column(name: 'date_limite', type: Types::DATE_MUTABLE)]
    #[Assert\NotNull]
    private ?\DateTimeInterface $dateLimite = null;

    #[ORM\Column(length: 20, options: ['default' => 'EN_ATTENTE'])]
    #[Assert\Choice(choices: ['EN_ATTENTE', 'EN_COURS', 'FINANCE', 'ECHEC'])]
    private string $statut = 'EN_ATTENTE';

    #[ORM\Column(type: Types::BOOLEAN, options: ['default' => true])]
    private bool $visibilite = true;

    #[ORM\Column(length: 100)]
    #[Assert\NotBlank]
    #[Assert\Length(max: 100)]
    #[Assert\Regex(pattern: '/^[^<>]+$/u', message: 'HTML tags are not allowed.')]
    private string $categorie = '';

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $image = null;

    #[ORM\OneToMany(mappedBy: 'projet', targetEntity: Investissement::class, orphanRemoval: true)]
    private Collection $investissements;

    public function __construct()
    {
        $this->investissements = new ArrayCollection();
        $this->dateCreation = new \DateTime();
    }

    public function getId(): ?string
    {
        return $this->id;
    }

    public function getArtiste(): ?User
    {
        return $this->artiste;
    }

    public function setArtiste(?User $artiste): self
    {
        $this->artiste = $artiste;

        return $this;
    }

    public function getTitre(): string
    {
        return $this->titre;
    }

    public function setTitre(string $titre): self
    {
        $this->titre = $titre;

        return $this;
    }

    public function getDescription(): string
    {
        return $this->description;
    }

    public function setDescription(string $description): self
    {
        $this->description = $description;

        return $this;
    }

    public function getObjectifFinancier(): ?string
    {
        return $this->objectifFinancier;
    }

    public function setObjectifFinancier(?string $objectifFinancier): self
    {
        $this->objectifFinancier = $objectifFinancier;

        return $this;
    }

    public function getMontantCollecte(): string
    {
        return $this->montantCollecte;
    }

    public function setMontantCollecte(string $montantCollecte): self
    {
        $this->montantCollecte = $montantCollecte;

        return $this;
    }

    public function getDateCreation(): ?\DateTimeInterface
    {
        return $this->dateCreation;
    }

    public function setDateCreation(?\DateTimeInterface $dateCreation): self
    {
        $this->dateCreation = $dateCreation;

        return $this;
    }

    public function getDateLimite(): ?\DateTimeInterface
    {
        return $this->dateLimite;
    }

    public function setDateLimite(?\DateTimeInterface $dateLimite): self
    {
        $this->dateLimite = $dateLimite;

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

    public function isVisibilite(): bool
    {
        return $this->visibilite;
    }

    public function setVisibilite(bool $visibilite): self
    {
        $this->visibilite = $visibilite;

        return $this;
    }

    public function getCategorie(): string
    {
        return $this->categorie;
    }

    public function setCategorie(string $categorie): self
    {
        $this->categorie = $categorie;

        return $this;
    }

    public function getImage(): ?string
    {
        return $this->image;
    }

    public function setImage(?string $image): self
    {
        $this->image = $image;

        return $this;
    }

    /**
     * @return Collection<int, Investissement>
     */
    public function getInvestissements(): Collection
    {
        return $this->investissements;
    }
}
