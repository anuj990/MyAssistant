# TwinMind Android - Take Home Assignment

This is my submission for the TwinMind Android Developer assignment. Built over 48 hours using Kotlin, Jetpack Compose, MVVM, Hilt, Room, and Coroutines.

## What I Built

A voice recording app that records audio in the background, splits it into 30-second chunks, transcribes each chunk, and generates a structured meeting summary.

### Features

**Recording**
- Foreground service keeps recording alive when app is in background
- Splits audio into 30-second chunks saved to local storage
- Persistent notification showing recording status

**Edge Cases Handled**
- Phone call detection — pauses recording on incoming/outgoing calls, resumes when call ends
- Audio focus loss — pauses when another app takes audio, resumes when focus returns
- Low storage check — checks before starting and between chunks, stops gracefully if storage runs out
- Microphone source changes — handles wired and bluetooth headset connect/disconnect
- Silence detection — warns after 10 seconds of no audio input

**Transcription**
- Uploads chunks to Gemini API as they complete
- Saves transcripts to Room database in correct order
- Retries failed chunks

**Summary**
- Sends full transcript to Gemini API
- Returns structured summary with Title, Summary, Action Items, Key Points
- Shows loading state while generating

## Tech Stack

- Kotlin + Jetpack Compose
- MVVM architecture
- Hilt for dependency injection
- Room for local database
- Retrofit + OkHttp for API calls
- Gemini API for transcription and summary
- WorkManager for background processing
- Coroutines + Flow

## Setup

1. Clone the repo
2. Get a Gemini API key from [aistudio.google.com](https://aistudio.google.com)
3. Add to `local.properties`:
```
GEMINI_API_KEY=your_key_here
```
4. Build and run

## Note on API

Gemini API integration is fully implemented. During development the free tier rate limit was hit, so the demo uses mock transcript data. To switch to real transcription uncomment the API call in `TranscriptRepository`. The summary generation uses real Gemini API calls.

## Project Structure
```
com.example.myassistant/
├── core/
│   ├── database/       — Room entities, DAOs, AppDatabase
│   ├── di/             — Hilt modules
│   ├── navigation/     — NavGraph, Screen routes
│   └── network/        — Retrofit, Gemini API
├── feature/
│   ├── recording/      — Service, Repository, ViewModel, UI
│   ├── transcript/     — Repository, ViewModel, UI
│   ├── summary/        — Repository, ViewModel, UI
│   └── dashboard/      — UI
└── ui/theme/
```

## APK

Debug APK is in the releases section of this repo.

## Screen Recording
