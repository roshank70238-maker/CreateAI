import { NextRequest, NextResponse } from 'next/server';

export async function GET(req: NextRequest) {
  // In production, queries the PostgreSQL database by user_id
  return NextResponse.json({
    conversations: [],
    images: [],
    videos: [],
    message: 'History loaded successfully'
  });
}
