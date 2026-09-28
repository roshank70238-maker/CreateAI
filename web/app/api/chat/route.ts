import { NextRequest, NextResponse } from 'next/server';
import { GeminiChatProvider } from '@/lib/providers/chat-provider';

const chatProvider = new GeminiChatProvider();

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const { messages, model = 'gemini-3.5-flash' } = body;

    if (!messages || !Array.isArray(messages) || messages.length === 0) {
      return NextResponse.json(
        { error: 'Invalid payload: messages array is required' },
        { status: 400 }
      );
    }

    if (!chatProvider.isConfigured()) {
      return NextResponse.json(
        {
          error: 'Provider not configured',
          details: 'Please set GEMINI_API_KEY in server environment variables.'
        },
        { status: 503 }
      );
    }

    const response = await chatProvider.generateReply(messages, model);
    return NextResponse.json(response);
  } catch (err: any) {
    return NextResponse.json(
      { error: err.message || 'Internal server error in AI chat' },
      { status: 500 }
    );
  }
}
