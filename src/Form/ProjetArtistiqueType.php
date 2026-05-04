<?php

namespace App\Form;

use App\Entity\ProjetArtistique;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Form\Extension\Core\Type\MoneyType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\File;

class ProjetArtistiqueType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('titre', TextType::class, [
                'required' => true,
                'attr' => ['pattern' => '^[^<>]+$'],
            ])
            ->add('description', TextareaType::class, [
                'required' => true,
                'attr' => ['pattern' => '^[^<>]+$'],
            ])
            ->add('objectifFinancier', MoneyType::class, [
                'required' => true,
                'currency' => 'USD',
            ])
            ->add('dateLimite', DateType::class, [
                'required' => true,
                'widget' => 'single_text',
            ])
            ->add('statut', ChoiceType::class, [
                'required' => true,
                'choices' => [
                    'En attente' => 'EN_ATTENTE',
                    'En cours' => 'EN_COURS',
                    'Finance' => 'FINANCE',
                    'Echec' => 'ECHEC',
                ],
            ])
            ->add('categorie', TextType::class, [
                'required' => true,
                'attr' => ['pattern' => '^[^<>]+$'],
            ])
            ->add('imageFile', FileType::class, [
                'label' => 'Project picture',
                'mapped' => false,
                'required' => false,
                'constraints' => [
                    new File([
                        'maxSize' => '5M',
                        'mimeTypes' => ['image/jpeg', 'image/png', 'image/webp', 'image/gif'],
                        'mimeTypesMessage' => 'Please upload a valid image (jpg, png, webp, gif).',
                    ]),
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => ProjetArtistique::class,
        ]);
    }
}
