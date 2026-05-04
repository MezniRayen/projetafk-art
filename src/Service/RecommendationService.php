<?php

namespace App\Service;

use App\Entity\ProjetArtistique;
use App\Entity\User;
use App\Repository\ProjetArtistiqueRepository;

class RecommendationService
{
    public function __construct(private readonly ProjetArtistiqueRepository $projectRepository)
    {
    }

    /**
     * Return an array of ProjetArtistique recommended for the given user.
     * Simple heuristic: if user has favorites, score projects by keyword overlap
     * with favorites; otherwise rank public projects by description quality and presence of image.
     *
     * @return ProjetArtistique[]
     */
    public function getRecommendations(User $user, int $limit = 5): array
    {
        $favorites = $user->getFavoriteProjects()->toArray();

        $candidates = $this->projectRepository->createQueryBuilder('p')
            ->leftJoin('p.artiste', 'a')
            ->addSelect('a')
            ->andWhere('p.visibilite = :v')
            ->setParameter('v', true)
            ->getQuery()
            ->getResult();

        if ($favorites !== []) {
            $keywords = $this->collectKeywordsFromProjects($favorites);
            $scored = [];
            foreach ($candidates as $p) {
                if ($this->isSameAsAny($p, $favorites)) {
                    continue;
                }

                $score = $this->scoreByKeywordOverlap($p, $keywords);
                $score += $p->getImage() ? 0.5 : 0.0;
                $score += min(1.0, ($this->safeFloat($p->getMontantCollecte()) / max(1.0, $this->safeFloat($p->getObjectifFinancier()))) ) ;

                $scored[] = ['project' => $p, 'score' => $score];
            }

            usort($scored, static fn($a, $b) => $b['score'] <=> $a['score']);

            return array_slice(array_map(static fn($s) => $s['project'], $scored), 0, $limit);
        }

        // No favorites -> pick projects with longest descriptions and images, then by collected/goal
        usort($candidates, function (ProjetArtistique $a, ProjetArtistique $b) {
            $sa = strlen((string) $a->getDescription()) + ($a->getImage() ? 300 : 0) + (int) round($this->safeFloat($a->getMontantCollecte()) / max(1.0, $this->safeFloat($a->getObjectifFinancier())) * 100);
            $sb = strlen((string) $b->getDescription()) + ($b->getImage() ? 300 : 0) + (int) round($this->safeFloat($b->getMontantCollecte()) / max(1.0, $this->safeFloat($b->getObjectifFinancier())) * 100);

            return $sb <=> $sa;
        });

        return array_slice($candidates, 0, $limit);
    }

    /** @param ProjetArtistique[] $projects */
    private function collectKeywordsFromProjects(array $projects): array
    {
        $words = [];
        foreach ($projects as $p) {
            $text = strtolower($p->getTitre() . ' ' . $p->getDescription() . ' ' . $p->getCategorie());
            $tokens = preg_split('/[^a-z0-9]+/i', $text, -1, PREG_SPLIT_NO_EMPTY);
            foreach ($tokens as $t) {
                if (strlen($t) < 3) {
                    continue;
                }
                $words[$t] = ($words[$t] ?? 0) + 1;
            }
        }

        arsort($words);

        return array_keys(array_slice($words, 0, 40, true));
    }

    private function scoreByKeywordOverlap(ProjetArtistique $project, array $keywords): float
    {
        $text = strtolower($project->getTitre() . ' ' . $project->getDescription() . ' ' . $project->getCategorie());
        $score = 0.0;
        foreach ($keywords as $k) {
            if (stripos($text, $k) !== false) {
                $score += 1.0;
            }
        }

        // normalize by description length
        $len = max(1, strlen((string) $project->getDescription()));
        return $score / sqrt($len);
    }

    /** @param ProjetArtistique[] $list */
    private function isSameAsAny(ProjetArtistique $p, array $list): bool
    {
        foreach ($list as $l) {
            if ($l->getId() === $p->getId()) {
                return true;
            }
        }

        return false;
    }

    private function safeFloat($val): float
    {
        if ($val === null) {
            return 0.0;
        }

        return (float) $val;
    }
}
