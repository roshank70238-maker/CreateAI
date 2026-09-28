export interface ImageGenerationOptions {
  prompt: string;
  negativePrompt?: string;
  aspectRatio: string; // '1:1' | '16:9' | '9:16' | '4:3'
  imageSize: string; // '512x512' | '1024x1024' | '2048x2048'
  numImages: number;
}

export interface ImageGenerationResponse {
  imageUrls: string[];
  provider: string;
}

export interface ImageGenerationProvider {
  id: string;
  name: string;
  isConfigured(): boolean;
  generateImages(options: ImageGenerationOptions): Promise<ImageGenerationResponse>;
}

export class ImagenProviderAdapter implements ImageGenerationProvider {
  id = 'google-imagen';
  name = 'Google Imagen 3 / Gemini Image';

  isConfigured(): boolean {
    const key = process.env.IMAGE_GENERATION_API_KEY || process.env.GEMINI_API_KEY;
    return !!key && key.trim() !== '' && key !== 'MY_GEMINI_API_KEY';
  }

  async generateImages(options: ImageGenerationOptions): Promise<ImageGenerationResponse> {
    if (!this.isConfigured()) {
      throw new Error('Provider not configured. Please supply an IMAGE_GENERATION_API_KEY or configure Vertex AI.');
    }

    // In environments where image quota is pending, provide explicit adapter response
    throw new Error('Provider not configured: External Imagen endpoint credentials required.');
  }
}

export class CustomImageProviderAdapter implements ImageGenerationProvider {
  id = 'custom-image-provider';
  name = 'External Custom Image Provider';

  isConfigured(): boolean {
    return !!process.env.IMAGE_GENERATION_ENDPOINT;
  }

  async generateImages(options: ImageGenerationOptions): Promise<ImageGenerationResponse> {
    if (!this.isConfigured()) {
      throw new Error('Provider not configured. Set IMAGE_GENERATION_ENDPOINT in environment variables.');
    }

    const endpoint = process.env.IMAGE_GENERATION_ENDPOINT!;
    const apiKey = process.env.IMAGE_GENERATION_API_KEY || '';

    const res = await fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${apiKey}`
      },
      body: JSON.stringify(options)
    });

    if (!res.ok) {
      throw new Error(`Custom provider returned error code ${res.status}`);
    }

    const data = await res.json();
    return {
      imageUrls: data.images || [],
      provider: this.name
    };
  }
}
