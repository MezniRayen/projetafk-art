<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\Exception\ExceptionInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class GroqAiService
{
    public function __construct(
        private readonly HttpClientInterface $httpClient,
        private readonly string $apiKey,
        private readonly string $model,
    ) {
    }

    public function improveProjectDescription(string $draft, string $title = '', string $category = ''): ?string
    {
        if ($this->apiKey === '' || trim($draft) === '') {
            return null;
        }

        $systemPrompt = 'You are a creative writing assistant for crowdfunding artistic projects. Rewrite the user draft into a clear, persuasive, and professional project description. Keep it concise (120-220 words), avoid HTML tags, keep factual details from the draft, and improve clarity and structure.';

        $context = "Project title: {$title}\nCategory: {$category}\nDraft:\n{$draft}";

        try {
            $response = $this->httpClient->request('POST', 'https://api.groq.com/openai/v1/chat/completions', [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type' => 'application/json',
                ],
                'json' => [
                    'model' => $this->model,
                    'temperature' => 0.7,
                    'max_tokens' => 500,
                    'messages' => [
                        ['role' => 'system', 'content' => $systemPrompt],
                        ['role' => 'user', 'content' => $context],
                    ],
                ],
            ]);

            $data = $response->toArray(false);
            $content = $data['choices'][0]['message']['content'] ?? null;
            if (!is_string($content) || trim($content) === '') {
                return null;
            }

            return trim(strip_tags($content));
        } catch (ExceptionInterface) {
            return null;
        }
    }

    /**
     * @param array<int, array{role: string, content: string}> $messages
     */
    public function chat(array $messages, string $systemPrompt): ?string
    {
        if ($this->apiKey === '' || $messages === []) {
            return null;
        }

        try {
            $response = $this->httpClient->request('POST', 'https://api.groq.com/openai/v1/chat/completions', [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type' => 'application/json',
                ],
                'json' => [
                    'model' => $this->model,
                    'temperature' => 0.7,
                    'max_tokens' => 500,
                    'messages' => array_merge([
                        ['role' => 'system', 'content' => $systemPrompt],
                    ], $messages),
                ],
            ]);

            $data = $response->toArray(false);
            $content = $data['choices'][0]['message']['content'] ?? null;
            if (!is_string($content) || trim($content) === '') {
                return null;
            }

            return trim(strip_tags($content));
        } catch (ExceptionInterface) {
            return null;
        }
    }

    /**
     * Generate theme suggestions for artists based on current events.
     * @return array<int, string>
     */
    public function suggestProjectThemesFromEvents(): array
    {
        if ($this->apiKey === '') {
            return [];
        }

        $systemPrompt = 'You are a creative advisor for an artistic crowdfunding platform. Based on current national and international events, trends, and cultural moments, suggest 5 compelling and timely artistic project themes that artists could create. Provide themes that are diverse, relevant, and inspiring. Format each theme on a new line, starting with "- " and keep each theme concise (under 10 words).';

        $userMessage = 'Based on current events in ' . date('Y') . ', please suggest 5 artistic project themes that would resonate with investors and audiences right now.';

        try {
            $response = $this->httpClient->request('POST', 'https://api.groq.com/openai/v1/chat/completions', [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type' => 'application/json',
                ],
                'json' => [
                    'model' => $this->model,
                    'temperature' => 0.8,
                    'max_tokens' => 400,
                    'messages' => [
                        ['role' => 'system', 'content' => $systemPrompt],
                        ['role' => 'user', 'content' => $userMessage],
                    ],
                ],
            ]);

            $data = $response->toArray(false);
            $content = $data['choices'][0]['message']['content'] ?? null;
            if (!is_string($content) || trim($content) === '') {
                return [];
            }

            // Parse themes from bullet-point format
            $lines = explode("\n", trim(strip_tags($content)));
            $themes = [];
            foreach ($lines as $line) {
                $clean = trim($line, '- ');
                if ($clean !== '' && strlen($clean) > 3) {
                    $themes[] = $clean;
                }
            }

            return array_slice($themes, 0, 5);
        } catch (ExceptionInterface) {
            return [];
        }
    }
}
