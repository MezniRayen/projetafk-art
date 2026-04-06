<?php

namespace App\Controller;

use App\Entity\Investissement;
use App\Entity\ProjetArtistique;
use App\Entity\User;
use App\Form\InvestissementType;
use App\Form\ProjetArtistiqueType;
use App\Repository\InvestissementRepository;
use App\Repository\ProjetArtistiqueRepository;
use Dompdf\Dompdf;
use Dompdf\Options;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\String\Slugger\SluggerInterface;

final class FrontofficeController extends AbstractController
{
    #[Route('/', name: 'app_frontoffice', methods: ['GET'])]
    public function index(ProjetArtistiqueRepository $projetRepository): Response
    {
        return $this->render('frontoffice/index.html.twig', [
            'projects' => $projetRepository->findBy(['visibilite' => true], ['id' => 'DESC']),
        ]);
    }

    #[Route('/front/projects', name: 'app_front_projects', methods: ['GET'])]
    public function projects(Request $request, ProjetArtistiqueRepository $projetRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        $search = trim((string) $request->query->get('search', ''));
        $sort = $this->safeSort((string) $request->query->get('sort', 'id'), ['id', 'titre', 'categorie', 'statut', 'objectifFinancier', 'montantCollecte']);
        $dir = $this->safeDirection((string) $request->query->get('dir', 'DESC'));

        $qb = $projetRepository->createQueryBuilder('p')
            ->leftJoin('p.artiste', 'a')
            ->addSelect('a')
            ->andWhere('p.visibilite = :visible')
            ->setParameter('visible', true);

        if ($search !== '') {
            $qb
                ->andWhere('p.titre LIKE :q OR p.categorie LIKE :q OR p.statut LIKE :q OR a.nom LIKE :q OR a.prenom LIKE :q')
                ->setParameter('q', '%' . $search . '%');
        }
        $qb->orderBy('p.' . $sort, $dir);

        return $this->render('frontoffice/projects.html.twig', [
            'projects' => $qb->getQuery()->getResult(),
            'search' => $search,
            'sort' => $sort,
            'dir' => $dir,
        ]);
    }

    #[Route('/front/projects/export/pdf', name: 'app_front_projects_export_pdf', methods: ['GET'])]
    public function projectsExportPdf(Request $request, ProjetArtistiqueRepository $projetRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        $search = trim((string) $request->query->get('search', ''));
        $sort = $this->safeSort((string) $request->query->get('sort', 'id'), ['id', 'titre', 'categorie', 'statut', 'objectifFinancier', 'montantCollecte']);
        $dir = $this->safeDirection((string) $request->query->get('dir', 'DESC'));

        $qb = $projetRepository->createQueryBuilder('p')
            ->leftJoin('p.artiste', 'a')
            ->addSelect('a')
            ->andWhere('p.visibilite = :visible')
            ->setParameter('visible', true);

        if ($search !== '') {
            $qb
                ->andWhere('p.titre LIKE :q OR p.categorie LIKE :q OR p.statut LIKE :q OR a.nom LIKE :q OR a.prenom LIKE :q')
                ->setParameter('q', '%' . $search . '%');
        }
        $qb->orderBy('p.' . $sort, $dir);

        return $this->renderPdf('frontoffice/pdf/projects.html.twig', [
            'items' => $qb->getQuery()->getResult(),
            'title' => 'Frontoffice Projects Export',
        ], 'front-projects.pdf');
    }

    #[Route('/front/projects/{id}', name: 'app_front_project_show', methods: ['GET'])]
    public function projectShow(ProjetArtistique $projet): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        return $this->render('frontoffice/project_show.html.twig', [
            'project' => $projet,
        ]);
    }

    #[Route('/front/my-projects', name: 'app_front_my_projects', methods: ['GET'])]
    public function myProjects(Request $request, ProjetArtistiqueRepository $projetRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ARTIST');

        /** @var User $user */
        $user = $this->getUser();

        $search = trim((string) $request->query->get('search', ''));
        $sort = $this->safeSort((string) $request->query->get('sort', 'id'), ['id', 'titre', 'categorie', 'statut', 'objectifFinancier', 'montantCollecte']);
        $dir = $this->safeDirection((string) $request->query->get('dir', 'DESC'));

        $qb = $projetRepository->createQueryBuilder('p')
            ->andWhere('p.artiste = :artist')
            ->setParameter('artist', $user);

        if ($search !== '') {
            $qb
                ->andWhere('p.titre LIKE :q OR p.categorie LIKE :q OR p.statut LIKE :q')
                ->setParameter('q', '%' . $search . '%');
        }
        $qb->orderBy('p.' . $sort, $dir);

        return $this->render('frontoffice/my_projects.html.twig', [
            'projects' => $qb->getQuery()->getResult(),
            'search' => $search,
            'sort' => $sort,
            'dir' => $dir,
        ]);
    }

    #[Route('/front/my-projects/export/pdf', name: 'app_front_my_projects_export_pdf', methods: ['GET'])]
    public function myProjectsExportPdf(Request $request, ProjetArtistiqueRepository $projetRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ARTIST');

        /** @var User $user */
        $user = $this->getUser();

        $search = trim((string) $request->query->get('search', ''));
        $sort = $this->safeSort((string) $request->query->get('sort', 'id'), ['id', 'titre', 'categorie', 'statut', 'objectifFinancier', 'montantCollecte']);
        $dir = $this->safeDirection((string) $request->query->get('dir', 'DESC'));

        $qb = $projetRepository->createQueryBuilder('p')
            ->andWhere('p.artiste = :artist')
            ->setParameter('artist', $user);

        if ($search !== '') {
            $qb
                ->andWhere('p.titre LIKE :q OR p.categorie LIKE :q OR p.statut LIKE :q')
                ->setParameter('q', '%' . $search . '%');
        }
        $qb->orderBy('p.' . $sort, $dir);

        return $this->renderPdf('frontoffice/pdf/my_projects.html.twig', [
            'items' => $qb->getQuery()->getResult(),
            'title' => 'My Projects Export',
        ], 'my-projects.pdf');
    }

    #[Route('/front/my-investments', name: 'app_front_my_investments', methods: ['GET'])]
    public function myInvestments(Request $request, InvestissementRepository $investissementRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_INVESTOR');

        /** @var User $user */
        $user = $this->getUser();

        $search = trim((string) $request->query->get('search', ''));
        $sort = $this->safeSort((string) $request->query->get('sort', 'id'), ['id', 'montant', 'moyenPaiement', 'statut', 'dateInvestissement']);
        $dir = $this->safeDirection((string) $request->query->get('dir', 'DESC'));

        $qb = $investissementRepository->createQueryBuilder('i')
            ->leftJoin('i.projet', 'p')
            ->addSelect('p')
            ->andWhere('i.investisseur = :investor')
            ->setParameter('investor', $user);

        if ($search !== '') {
            $qb
                ->andWhere('p.titre LIKE :q OR i.statut LIKE :q OR i.moyenPaiement LIKE :q')
                ->setParameter('q', '%' . $search . '%');
        }
        $qb->orderBy('i.' . $sort, $dir);

        return $this->render('frontoffice/my_investments.html.twig', [
            'investments' => $qb->getQuery()->getResult(),
            'search' => $search,
            'sort' => $sort,
            'dir' => $dir,
        ]);
    }

    #[Route('/front/my-investments/export/pdf', name: 'app_front_my_investments_export_pdf', methods: ['GET'])]
    public function myInvestmentsExportPdf(Request $request, InvestissementRepository $investissementRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_INVESTOR');

        /** @var User $user */
        $user = $this->getUser();

        $search = trim((string) $request->query->get('search', ''));
        $sort = $this->safeSort((string) $request->query->get('sort', 'id'), ['id', 'montant', 'moyenPaiement', 'statut', 'dateInvestissement']);
        $dir = $this->safeDirection((string) $request->query->get('dir', 'DESC'));

        $qb = $investissementRepository->createQueryBuilder('i')
            ->leftJoin('i.projet', 'p')
            ->addSelect('p')
            ->andWhere('i.investisseur = :investor')
            ->setParameter('investor', $user);

        if ($search !== '') {
            $qb
                ->andWhere('p.titre LIKE :q OR i.statut LIKE :q OR i.moyenPaiement LIKE :q')
                ->setParameter('q', '%' . $search . '%');
        }
        $qb->orderBy('i.' . $sort, $dir);

        return $this->renderPdf('frontoffice/pdf/my_investments.html.twig', [
            'items' => $qb->getQuery()->getResult(),
            'title' => 'My Investments Export',
        ], 'my-investments.pdf');
    }

    #[Route('/front/projet/new', name: 'app_front_project_new')]
    public function createProject(Request $request, EntityManagerInterface $entityManager, SluggerInterface $slugger): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ARTIST');

        $project = new ProjetArtistique();
        /** @var User $user */
        $user = $this->getUser();
        $project->setArtiste($user);

        $form = $this->createForm(ProjetArtistiqueType::class, $project);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            /** @var UploadedFile|null $imageFile */
            $imageFile = $form->get('imageFile')->getData();
            if ($imageFile instanceof UploadedFile) {
                $project->setImage($this->uploadProjectImage($imageFile, $slugger));
            }

            $project->setDateCreation(new \DateTime());
            $project->setMontantCollecte('0.00');
            $entityManager->persist($project);
            $entityManager->flush();

            $this->addFlash('success', 'Project created successfully.');

            return $this->redirectToRoute('app_front_my_projects');
        }

        return $this->render('frontoffice/project_form.html.twig', [
            'form' => $form,
            'title' => 'Create artistic project',
        ]);
    }

    #[Route('/front/projet/{id}/edit', name: 'app_front_project_edit')]
    public function editProject(ProjetArtistique $project, Request $request, EntityManagerInterface $entityManager, SluggerInterface $slugger): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ARTIST');

        /** @var User $user */
        $user = $this->getUser();
        $this->assertArtistOwnsProject($project, $user);

        $form = $this->createForm(ProjetArtistiqueType::class, $project);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            /** @var UploadedFile|null $imageFile */
            $imageFile = $form->get('imageFile')->getData();
            if ($imageFile instanceof UploadedFile) {
                $project->setImage($this->uploadProjectImage($imageFile, $slugger));
            }

            $entityManager->flush();
            $this->addFlash('success', 'Project updated successfully.');

            return $this->redirectToRoute('app_front_my_projects');
        }

        return $this->render('frontoffice/project_form.html.twig', [
            'form' => $form,
            'title' => 'Edit artistic project',
        ]);
    }

    #[Route('/front/projet/{id}/delete', name: 'app_front_project_delete', methods: ['POST'])]
    public function deleteProject(ProjetArtistique $project, Request $request, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ARTIST');

        /** @var User $user */
        $user = $this->getUser();
        $this->assertArtistOwnsProject($project, $user);

        $token = (string) $request->request->get('_token');
        if ($this->isCsrfTokenValid('delete_my_project_' . $project->getId(), $token)) {
            $entityManager->remove($project);
            $entityManager->flush();
            $this->addFlash('success', 'Project deleted successfully.');
        }

        return $this->redirectToRoute('app_front_my_projects');
    }

    #[Route('/front/projet/{id}/invest', name: 'app_front_project_invest')]
    public function invest(ProjetArtistique $projet, Request $request, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('ROLE_INVESTOR');

        $investissement = new Investissement();
        /** @var User $user */
        $user = $this->getUser();
        $investissement->setInvestisseur($user);
        $investissement->setProjet($projet);

        $form = $this->createForm(InvestissementType::class, $investissement);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $investissement->setDateInvestissement(new \DateTime());

            $currentCollected = (float) $projet->getMontantCollecte();
            $newAmount = (float) $investissement->getMontant();
            $projet->setMontantCollecte(number_format($currentCollected + $newAmount, 2, '.', ''));

            $entityManager->persist($investissement);
            $entityManager->flush();

            $this->addFlash('success', 'Investment registered.');

            return $this->redirectToRoute('app_front_my_investments');
        }

        return $this->render('frontoffice/invest_form.html.twig', [
            'form' => $form,
            'project' => $projet,
        ]);
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

    private function assertArtistOwnsProject(ProjetArtistique $project, User $user): void
    {
        if (!$project->getArtiste() || $project->getArtiste()->getId() !== $user->getId()) {
            throw $this->createAccessDeniedException('You can only manage your own projects.');
        }
    }
}
