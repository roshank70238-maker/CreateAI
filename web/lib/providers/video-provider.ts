export interface VideoGenerationOptions {
  prompt: string;
  sourceImageUrl?: string;
  durationSeconds: number; // 3, 5, 10
  aspectRatio: string; // '16:9' | '9:16' | '1:1'
}

export interface VideoGenerationJob {
  jobId: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'UNCONFIGURED';
  progressPercent: number;
  videoUrl?: string;
  errorMessage?: string;
}

export interface VideoGenerationProvider {
  id: string;
  name: string;
  isConfigured(): boolean;
  startGeneration(options: VideoGenerationOptions): Promise<VideoGenerationJob>;
  checkStatus(jobId: string): Promise<VideoGenerationJob>;
}

export class VeoVideoProviderAdapter implements VideoGenerationProvider {
  id = 'google-veo';
  name = 'Google Veo 3.1';

  isConfigured(): boolean {
    const key = process.env.VIDEO_GENERATION_API_KEY;
    return !!key && key.trim() !== '' && key !== 'MY_VEO_API_KEY';
  }

  async startGeneration(options: VideoGenerationOptions): Promise<VideoGenerationJob> {
    if (!this.isConfigured()) {
      throw new Error('Provider not configured. Set VIDEO_GENERATION_API_KEY in environment variables.');
    }

    throw new Error('Provider not configured: Google Veo enterprise access credentials required.');
  }

  async checkStatus(jobId: string): Promise<VideoGenerationJob> {
    throw new Error('Provider not configured.');
  }
}
