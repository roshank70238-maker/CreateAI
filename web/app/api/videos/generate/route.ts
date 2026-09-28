import { NextRequest, NextResponse } from 'next/server';
import { VeoVideoProviderAdapter } from '@/lib/providers/video-provider';

const videoProvider = new VeoVideoProviderAdapter();

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const { prompt, sourceImageUrl, durationSeconds = 5, aspectRatio = '16:9' } = body;

    if (!prompt || typeof prompt !== 'string' || prompt.trim() === '') {
      return NextResponse.json(
        { error: 'Prompt is required' },
        { status: 400 }
      );
    }

    if (!videoProvider.isConfigured()) {
      return NextResponse.json(
        {
          error: 'Provider not configured',
          details: 'Video generation provider not configured. Please supply VIDEO_GENERATION_API_KEY in environment.'
        },
        { status: 503 }
      );
    }

    const job = await videoProvider.startGeneration({
      prompt,
      sourceImageUrl,
      durationSeconds,
      aspectRatio
    });

    return NextResponse.json(job);
  } catch (err: any) {
    const isUnconfigured = err.message?.includes('Provider not configured');
    return NextResponse.json(
      { error: err.message || 'Video generation failed' },
      { status: isUnconfigured ? 503 : 500 }
    );
  }
}
