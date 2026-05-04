<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\Exception\TransportExceptionInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class TwilioSmsService
{
    public function __construct(
        private readonly HttpClientInterface $httpClient,
        private readonly string $accountSid,
        private readonly string $authToken,
        private readonly string $fromPhone,
        private readonly string $defaultReceiverPhone,
    ) {
    }

    public function sendInvestmentNotification(string $artistName, string $projectTitle, string $amount): bool
    {
        if ($this->accountSid === '' || $this->authToken === '' || $this->fromPhone === '' || $this->defaultReceiverPhone === '') {
            return false;
        }

        $message = sprintf(
            'Hello %s, you received a new investment of %s TND on project "%s".',
            $artistName,
            $amount,
            $projectTitle
        );

        $url = sprintf('https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json', $this->accountSid);

        try {
            $response = $this->httpClient->request('POST', $url, [
                'auth_basic' => [$this->accountSid, $this->authToken],
                'body' => [
                    'From' => $this->fromPhone,
                    'To' => $this->defaultReceiverPhone,
                    'Body' => $message,
                ],
            ]);

            return $response->getStatusCode() >= 200 && $response->getStatusCode() < 300;
        } catch (TransportExceptionInterface) {
            return false;
        }
    }
}
