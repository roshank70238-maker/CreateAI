export interface ChatMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
}

export interface ChatProviderResponse {
  reply: string;
  model: string;
  usage?: {
    promptTokens: number;
    completionTokens: number;
  };
}

export interface ChatProvider {
  id: string;
  name: string;
  isConfigured(): boolean;
  generateReply(messages: ChatMessage[], model?: string): Promise<ChatProviderResponse>;
}

export class GeminiChatProvider implements ChatProvider {
  id = 'gemini';
  name = 'Google Gemini';

  isConfigured(): boolean {
    const key = process.env.GEMINI_API_KEY;
    return !!key && key.trim() !== '' && key !== 'MY_GEMINI_API_KEY';
  }

  async generateReply(messages: ChatMessage[], model: string = 'gemini-3.5-flash'): Promise<ChatProviderResponse> {
    if (!this.isConfigured()) {
      throw new Error('Provider not configured. Please supply a valid GEMINI_API_KEY.');
    }

    const apiKey = process.env.GEMINI_API_KEY!;
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`;

    const contents = messages.map((m) => ({
      role: m.role === 'assistant' ? 'model' : 'user',
      parts: [{ text: m.content }]
    }));

    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ contents })
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Gemini API Error (${response.status}): ${errorText}`);
    }

    const data = await response.json();
    const reply = data?.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!reply) {
      throw new Error('No candidate content returned by model');
    }

    return {
      reply,
      model
    };
  }
}
