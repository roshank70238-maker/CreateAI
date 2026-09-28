import { NextRequest, NextResponse } from 'next/server';
import { ImagenProviderAdapter, CustomImageProviderAdapter } from '@/lib/providers/image-provider';

const defaultProvider = new ImagenProviderAdapter();
const customProvider = new CustomImageProviderAdapter();

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const { prompt, aspectRatio = '1:1', imageSize = '1024x1024', numImages = 1, negativePrompt } = body;

    if (!prompt || typeof prompt !== 'string' || prompt.trim() === '') {
      return NextResponse.json(
        { error: 'Prompt is required' },
        { status: 400 }
      );
    }

    const provider = customProvider.isConfigured() ? customProvider : defaultProvider;

    if (!provider.isConfigured()) {
      return NextResponse.json(
        {
          error: 'Provider not configured',
          details: 'Image generation provider is not configured. Set IMAGE_GENERATION_API_KEY in environment.'
        },
        { status: 503 }
      );
    }

    const result = await provider.generateImages({
      prompt,
      aspectRatio,
      imageSize,
      numImages,
      negativePrompt
    });

    return NextResponse.json(result);
  } catch (err: any) {
    const isUnconfigured = err.message?.includes('Provider not configured');
    return NextResponse.json(
      { error: err.message || 'Image generation failed' },
      { status: isUnconfigured ? 503 : 500 }
    );
  }
}
