<?php

namespace App\Form;

use App\Entity\Investissement;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\MoneyType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class InvestissementType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('montant', MoneyType::class, [
                'required' => true,
                'currency' => 'USD',
            ])
            ->add('moyenPaiement', ChoiceType::class, [
                'required' => true,
                'choices' => [
                    'Carte' => 'CARTE',
                    'Virement' => 'VIREMENT',
                    'Paypal' => 'PAYPAL',
                ],
            ])
            ->add('statut', ChoiceType::class, [
                'required' => true,
                'choices' => [
                    'En attente' => 'EN_ATTENTE',
                    'Valide' => 'VALIDE',
                    'Annule' => 'ANNULE',
                ],
            ])
            ->add('palier', TextType::class, [
                'required' => true,
                'attr' => ['pattern' => '^[^<>]+$'],
            ])
            ->add('messageSoutien', TextareaType::class, [
                'required' => true,
                'attr' => ['pattern' => '^[^<>]+$'],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Investissement::class,
        ]);
    }
}
