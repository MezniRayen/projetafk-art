<?php

namespace App\Controller;

use App\Entity\Investissement;
use App\Entity\ProjetArtistique;
use App\Entity\User;
use App\Form\InvestissementType;
use App\Form\ProjetArtistiqueType;
use App\Form\UserType;
use App\Repository\InvestissementRepository;
use App\Repository\ProjetArtistiqueRepository;
use App\Repository\UserRepository;
use Dompdf\Dompdf;
use Dompdf\Options;
use Doctrine\DBAL\Connection;
use Doctrine\ORM\QueryBuilder;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\String\Slugger\SluggerInterface;

#[Route('/backoffice')]
final class BackofficeController extends AbstractController
{
    #[Route('', name: 'app_backoffice', methods: ['GET'])]
    public function dashboard(UserRepository $users, ProjetArtistiqueRepository $projects, InvestissementRepository $investments, Connection $connection): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $usersCount = (int) $users->createQueryBuilder('u')
            ->select('COUNT(u.id)')
            ->getQuery()
            ->getSingleScalarResult();

        $projectsCount = (int) $projects->createQueryBuilder('p')
            ->select('COUNT(p.id)')
            ->getQuery()
            ->getSingleScalarResult();

        $investmentsCount = (int) $investments->createQueryBuilder('i')
            ->select('COUNT(i.id)')
            ->getQuery()
            ->getSingleScalarResult();

        $totalRaised = (float) $projects->createQueryBuilder('p')
            ->select('COALESCE(SUM(p.montantCollecte), 0)')
            ->getQuery()
            ->getSingleScalarResult();

        $statusRows = $projects->createQueryBuilder('p')
            ->select('p.statut AS status, COUNT(p.id) AS total')
            ->groupBy('p.statut')
            ->getQuery()
            ->getArrayResult();

        $statusMap = ['EN_ATTENTE' => 0, 'EN_COURS' => 0, 'FINANCE' => 0, 'ECHEC' => 0];
        foreach ($statusRows as $row) {
            $status = (string) ($row['status'] ?? '');
            if (array_key_exists($status, $statusMap)) {
                $statusMap[$status] = (int) $row['total'];
            }
        }

        $monthRows = $connection->fetchAllAssociative(
            "SELECT DATE_FORMAT(date_investissement, '%Y-%m') AS ym, COALESCE(SUM(montant), 0) AS amount
             FROM investissement
             WHERE date_investissement >= :startDate
             GROUP BY ym
             ORDER BY ym ASC",
            [
                'startDate' => (new \DateTimeImmutable('first day of -5 months'))->setTime(0, 0)->format('Y-m-d H:i:s'),
            ],
            [
                'startDate' => \Doctrine\DBAL\ParameterType::STRING,
            ]
        );

        $monthSeries = $this->buildMonthSeries($monthRows, 6);

        $topProjects = $projects->createQueryBuilder('p')
            ->select('p.titre AS title, p.montantCollecte AS collected')
            ->orderBy('p.montantCollecte', 'DESC')
            ->setMaxResults(5)
            ->getQuery()
            ->getArrayResult();

        $topProjectLabels = array_map(static fn (array $row) => (string) $row['title'], $topProjects);
        $topProjectValues = array_map(static fn (array $row) => (float) $row['collected'], $topProjects);

        return $this->render('backoffice/dashboard.html.twig', [
            'usersCount' => $usersCount,
            'projectsCount' => $projectsCount,
            'investmentsCount' => $investmentsCount,
            'totalRaised' => $totalRaised,
            'projectStatusLabels' => ['En attente', 'En cours', 'Finance', 'Echec'],
            'projectStatusValues' => array_values($statusMap),
            'investmentMonths' => $monthSeries['labels'],
            'investmentMonthValues' => $monthSeries['values'],
            'topProjectLabels' => $topProjectLabels,
            'topProjectValues' => $topProjectValues,
        ]);
    }

    #[Route('/projects', name: 'app_backoffice_project_index', methods: ['GET'])]
    public function projects(Request $request, ProjetArtistiqueRepository $repository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $search = trim((string) $request->query->get('search', ''));
        $sort = $this->safeSort((string) $request->query->get('sort', 'id'), ['id', 'titre', 'categorie', 'statut', 'objectifFinancier', 'montantCollecte']);
        $dir = $this->safeDirection((string) $request->query->get('dir', 'DESC'));

        $qb = $repository->createQueryBuilder('p')
            ->leftJoin('p.artiste', 'a')
            ->addSelect('a');

        if ($search !== '') {
            $qb
                ->andWhere('p.titre LIKE :q OR p.categorie LIKE :q OR p.statut LIKE :q OR a.nom LIKE :q OR a.prenom LIKE :q')
                ->setParameter('q', '%' . $search . '%');
        }
        $qb->orderBy('p.' . $sort, $dir);

        return $this->render('backoffice/project_index.html.twig', [
            'projects' => $qb->getQuery()->getResult(),
            'search' => $search,
            'sort' => $sort,
            'dir' => $dir,
        ]);
    }

    #[Route('/projects/export/pdf', name: 'app_backoffice_project_export_pdf', methods: ['GET'])]
    public function projectExportPdf(Request $request, ProjetArtistiqueRepository $repository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $search = trim((string) $request->query->get('search', ''));
        $sort = $this->safeSort((string) $request->query->get('sort', 'id'), ['id', 'titre', 'categorie', 'statut', 'objectifFinancier', 'montantCollecte']);
        $dir = $this->safeDirection((string) $request->query->get('dir', 'DESC'));

        $qb = $repository->createQueryBuilder('p')
            ->leftJoin('p.artiste', 'a')
            ->addSelect('a');

        if ($search !== '') {
            $qb
                ->andWhere('p.titre LIKE :q OR p.categorie LIKE :q OR p.statut LIKE :q OR a.nom LIKE :q OR a.prenom LIKE :q')
                ->setParameter('q', '%' . $search . '%');
        }
        $qb->orderBy('p.' . $sort, $dir);

        return $this->renderPdf('backoffice/pdf/projects.html.twig', [
            'items' => $qb->getQuery()->getResult(),
            'title' => 'Projects Export',
        ], 'projects.pdf');
    }

    #[Route('/projects/new', name: 'app_backoffice_project_new')]
    public function projectNew(Request $request, EntityManagerInterface $entityManager, UserRepository $userRepository, SluggerInterface $slugger): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $project = new ProjetArtistique();
        $artist = $userRepository->findOneBy(['role' => 'USER', 'userType' => 'ARTIST']);
        if ($artist instanceof User) {
            $project->setArtiste($artist);
        }

        $form = $this->createForm(ProjetArtistiqueType::class, $project);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            /** @var UploadedFile|null $imageFile */
            $imageFile = $form->get('imageFile')->getData();
            if ($imageFile instanceof UploadedFile) {
                $project->setImage($this->uploadProjectImage($imageFile, $slugger));
            }

            if (!$project->getArtiste() instanceof User && $artist instanceof User) {
                $project->setArtiste($artist);
            }
            $entityManager->persist($project);
            $entityManager->flush();

            return $this->redirectToRoute('app_backoffice_project_index');
        }

        return $this->render('backoffice/form_page.html.twig', [
            'title' => 'Create project',
            'form' => $form,
        ]);
    }

    #[Route('/projects/{id}/edit', name: 'app_backoffice_project_edit')]
    public function projectEdit(ProjetArtistique $project, Request $request, EntityManagerInterface $entityManager, SluggerInterface $slugger): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $form = $this->createForm(ProjetArtistiqueType::class, $project);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            /** @var UploadedFile|null $imageFile */
            $imageFile = $form->get('imageFile')->getData();
            if ($imageFile instanceof UploadedFile) {
                $project->setImage($this->uploadProjectImage($imageFile, $slugger));
            }

            $entityManager->flush();

            return $this->redirectToRoute('app_backoffice_project_index');
        }

        return $this->render('backoffice/form_page.html.twig', [
            'title' => 'Edit project',
            'form' => $form,
        ]);
    }

    #[Route('/projects/{id}/delete', name: 'app_backoffice_project_delete', methods: ['POST'])]
    public function projectDelete(ProjetArtistique $project, Request $request, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        if ($this->isCsrfTokenValid('delete_project_' . $project->getId(), (string) $request->request->get('_token'))) {
            $entityManager->remove($project);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_backoffice_project_index');
    }

    #[Route('/investments', name: 'app_backoffice_investment_index', methods: ['GET'])]
    public function investments(Request $request, InvestissementRepository $repository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $search = trim((string) $request->query->get('search', ''));
        $sort = $this->safeSort((string) $request->query->get('sort', 'id'), ['id', 'montant', 'moyenPaiement', 'statut', 'dateInvestissement']);
        $dir = $this->safeDirection((string) $request->query->get('dir', 'DESC'));

        $qb = $repository->createQueryBuilder('i')
            ->leftJoin('i.investisseur', 'u')
            ->leftJoin('i.projet', 'p')
            ->addSelect('u', 'p');

        if ($search !== '') {
            $qb
                ->andWhere('u.nom LIKE :q OR u.prenom LIKE :q OR p.titre LIKE :q OR i.statut LIKE :q OR i.moyenPaiement LIKE :q')
                ->setParameter('q', '%' . $search . '%');
        }
        $qb->orderBy('i.' . $sort, $dir);

        return $this->render('backoffice/investment_index.html.twig', [
            'investments' => $qb->getQuery()->getResult(),
            'search' => $search,
            'sort' => $sort,
            'dir' => $dir,
        ]);
    }

    #[Route('/investments/export/pdf', name: 'app_backoffice_investment_export_pdf', methods: ['GET'])]
    public function investmentExportPdf(Request $request, InvestissementRepository $repository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $search = trim((string) $request->query->get('search', ''));
        $sort = $this->safeSort((string) $request->query->get('sort', 'id'), ['id', 'montant', 'moyenPaiement', 'statut', 'dateInvestissement']);
        $dir = $this->safeDirection((string) $request->query->get('dir', 'DESC'));

        $qb = $repository->createQueryBuilder('i')
            ->leftJoin('i.investisseur', 'u')
            ->leftJoin('i.projet', 'p')
            ->addSelect('u', 'p');

        if ($search !== '') {
            $qb
                ->andWhere('u.nom LIKE :q OR u.prenom LIKE :q OR p.titre LIKE :q OR i.statut LIKE :q OR i.moyenPaiement LIKE :q')
                ->setParameter('q', '%' . $search . '%');
        }
        $qb->orderBy('i.' . $sort, $dir);

        return $this->renderPdf('backoffice/pdf/investments.html.twig', [
            'items' => $qb->getQuery()->getResult(),
            'title' => 'Investments Export',
        ], 'investments.pdf');
    }

    #[Route('/investments/new/{id}', name: 'app_backoffice_investment_new')]
    public function investmentNew(ProjetArtistique $projet, Request $request, EntityManagerInterface $entityManager, UserRepository $userRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $investment = new Investissement();
        $investment->setProjet($projet);
        $investor = $userRepository->findOneBy(['role' => 'USER', 'userType' => 'INVESTOR']);
        if ($investor instanceof User) {
            $investment->setInvestisseur($investor);
        }

        $form = $this->createForm(InvestissementType::class, $investment);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            if (!$investment->getInvestisseur() instanceof User && $investor instanceof User) {
                $investment->setInvestisseur($investor);
            }
            $entityManager->persist($investment);
            $entityManager->flush();

            return $this->redirectToRoute('app_backoffice_investment_index');
        }

        return $this->render('backoffice/form_page.html.twig', [
            'title' => 'Create investment',
            'form' => $form,
        ]);
    }

    #[Route('/investments/{id}/edit', name: 'app_backoffice_investment_edit')]
    public function investmentEdit(Investissement $investment, Request $request, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $form = $this->createForm(InvestissementType::class, $investment);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('app_backoffice_investment_index');
        }

        return $this->render('backoffice/form_page.html.twig', [
            'title' => 'Edit investment',
            'form' => $form,
        ]);
    }

    #[Route('/investments/{id}/delete', name: 'app_backoffice_investment_delete', methods: ['POST'])]
    public function investmentDelete(Investissement $investment, Request $request, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        if ($this->isCsrfTokenValid('delete_investment_' . $investment->getId(), (string) $request->request->get('_token'))) {
            $entityManager->remove($investment);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_backoffice_investment_index');
    }

    private function safeSort(string $sort, array $allowed): string
    {
        return in_array($sort, $allowed, true) ? $sort : $allowed[0];
    }

    private function safeDirection(string $direction): string
    {
        $direction = strtoupper($direction);

        return in_array($direction, ['ASC', 'DESC'], true) ? $direction : 'DESC';
    }

    private function renderPdf(string $template, array $context, string $filename): Response
    {
        $options = new Options();
        $options->set('isRemoteEnabled', true);
        $dompdf = new Dompdf($options);
        $html = $this->renderView($template, $context);
        $dompdf->loadHtml($html);
        $dompdf->setPaper('A4', 'portrait');
        $dompdf->render();

        $response = new Response($dompdf->output());
        $response->headers->set('Content-Type', 'application/pdf');
        $response->headers->set('Content-Disposition', 'attachment; filename="' . $filename . '"');

        return $response;
    }

    private function uploadProjectImage(UploadedFile $imageFile, SluggerInterface $slugger): string
    {
        $originalFilename = pathinfo($imageFile->getClientOriginalName(), PATHINFO_FILENAME);
        $safeFilename = $slugger->slug($originalFilename)->lower();
        $extension = $imageFile->guessExtension() ?: 'bin';
        $newFilename = $safeFilename . '-' . uniqid('', true) . '.' . $extension;
        $targetDirectory = $this->getParameter('kernel.project_dir') . '/public/uploads/projects';

        try {
            $imageFile->move($targetDirectory, $newFilename);
        } catch (FileException) {
            throw new \RuntimeException('Unable to upload project image.');
        }

        return $newFilename;
    }

    /**
     * @param array<int, array{ym?: string, amount?: mixed}> $rows
     * @return array{labels: array<int, string>, values: array<int, float>}
     */
    private function buildMonthSeries(array $rows, int $months): array
    {
        $labels = [];
        $valuesByMonth = [];

        foreach ($rows as $row) {
            $monthKey = (string) ($row['ym'] ?? '');
            if ($monthKey !== '') {
                $valuesByMonth[$monthKey] = (float) ($row['amount'] ?? 0);
            }
        }

        $values = [];
        $start = new \DateTimeImmutable('first day of -' . ($months - 1) . ' months');
        for ($i = 0; $i < $months; ++$i) {
            $current = $start->modify('+' . $i . ' months');
            $key = $current->format('Y-m');
            $labels[] = $current->format('M Y');
            $values[] = $valuesByMonth[$key] ?? 0.0;
        }

        return ['labels' => $labels, 'values' => $values];
    }
}
