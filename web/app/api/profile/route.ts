import { NextRequest, NextResponse } from 'next/server';

export async function GET(req: NextRequest) {
  return NextResponse.json({
    user: {
      id: 'usr_dev_101',
      name: 'Creative Studio User',
      email: 'creator@createai.studio',
      plan: 'Free Plan'
    },
    usage: {
      dailyChatLimit: parseInt(process.env.DAILY_CHAT_LIMIT || '50', 10),
      dailyImageLimit: parseInt(process.env.DAILY_IMAGE_LIMIT || '5', 10),
      dailyVideoLimit: parseInt(process.env.DAILY_VIDEO_LIMIT || '2', 10),
      chatsToday: 0,
      imagesToday: 0,
      videosToday: 0
    }
  });
}
