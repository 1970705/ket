# Audio Resources Specification

**Version**: 1.0
**Last Updated**: 2026-02-18
**Author**: education-specialist

---

## Overview

This document specifies all audio resources required for Phase 3 (Listening Mode) and enhanced gameplay experience in Wordland.

---

## Table of Contents

1. [Technical Specifications](#technical-specifications)
2. [KET Vocabulary Pronunciation](#ket-vocabulary-pronunciation)
3. [Game Sound Effects](#game-sound-effects)
4. [Background Music](#background-music)
5. [File Naming Convention](#file-naming-convention)
6. [Resource Sources](#resource-sources)
7. [Directory Structure](#directory-structure)

---

## Technical Specifications

### Audio Format Standards

| Specification | Value | Notes |
|--------------|-------|-------|
| Format | MP3 (primary), OGG (alternative) | MP3 for pronunciation, OGG for sound effects |
| Sample Rate | 44.1 kHz | CD quality |
| Bitrate | 128-192 kbps | Balance between quality and file size |
| Channels | Mono (pronunciation), Stereo (music) | |
| Max Duration | Pronunciation: 2s, SFX: 3s, BGM: 5min | |

### File Size Limits

| Type | Max Size | Target Size |
|------|----------|-------------|
| Pronunciation | 100 KB | 50-80 KB |
| Sound Effect | 150 KB | 30-80 KB |
| Background Music | 2 MB | 1-1.5 MB |

**Total Estimated APK Impact**: ~3-4 MB for all audio resources

---

## KET Vocabulary Pronunciation

### Priority: CRITICAL ⚡

30 KET vocabulary words from Look Island, organized by level.

### Level 1: Basic Observation Verbs (6 words)

| Word ID | Word | File Name | IPA |
|---------|------|-----------|-----|
| look_001 | look | `word_look_001.mp3` | /lʊk/ |
| look_002 | see | `word_see.mp3` | /siː/ |
| look_003 | watch | `word_watch.mp3` | /wɒtʃ/ |
| look_004 | eye | `word_eye.mp3` | /aɪ/ |
| look_005 | glass | `word_glass.mp3` | /glɑːs/ |
| look_006 | find | `word_find.mp3` | /faɪnd/ |

### Level 2: Colors and Light (6 words)

| Word ID | Word | File Name | IPA |
|---------|------|-----------|-----|
| look_007 | color | `word_color.mp3` | /ˈkʌlə(r)/ |
| look_008 | red | `word_red.mp3` | /red/ |
| look_009 | blue | `word_blue.mp3` | /bluː/ |
| look_010 | dark | `word_dark.mp3` | /dɑːk/ |
| look_011 | light | `word_light.mp3` | /laɪt/ |
| look_012 | bright | `word_bright.mp3` | /braɪt/ |

### Level 3: Movement and Gaze (6 words)

| Word ID | Word | File Name | IPA |
|---------|------|-----------|-----|
| look_013 | stare | `word_stare.mp3` | /steə(r)/ |
| look_014 | notice | `word_notice.mp3` | /ˈnəʊtɪs/ |
| look_015 | observe | `word_observe.mp3` | /əbˈzɜːv/ |
| look_016 | appear | `word_appear.mp3` | /əˈpɪə(r)/ |
| look_017 | view | `word_view.mp3` | /vjuː/ |
| look_018 | scene | `word_scene.mp3` | /siːn/ |

### Level 4: Looking Actions (6 words)

| Word ID | Word | File Name | IPA |
|---------|------|-----------|-----|
| look_019 | search | `word_search.mp3` | /sɜːtʃ/ |
| look_020 | check | `word_check.mp3` | /tʃek/ |
| look_021 | picture | `word_picture.mp3` | /ˈpɪktʃə(r)/ |
| look_022 | photo | `word_photo.mp3` | /ˈfəʊtəʊ/ |
| look_023 | camera | `word_camera.mp3` | /ˈkæmərə/ |
| look_024 | display | `word_display.mp3` | /dɪˈspleɪ/ |

### Level 5: Advanced Observation (6 words)

| Word ID | Word | File Name | IPA |
|---------|------|-----------|-----|
| look_025 | examine | `word_examine.mp3` | /ɪɡˈzæmɪn/ |
| look_026 | visible | `word_visible.mp3` | /ˈvɪzəbl/ |
| look_027 | mirror | `word_mirror.mp3` | /ˈmɪrə(r)/ |
| look_028 | focus | `word_focus.mp3` | /ˈfəʊkəs/ |
| look_029 | clear | `word_clear.mp3` | /klɪə(r)/ |
| look_030 | notice | `word_notice_2.mp3` | /ˈnəʊtɪs/ |

### Pronunciation Requirements

- **Dialect**: Standard British (UK) or American (US) English
- **Speed**: Normal speaking rate (~120-150 WPM)
- **Clarity**: High clarity, distinct pronunciation
- **Recording Quality**: No background noise, consistent volume
- **Format**: Single word only, no example sentences

---

## Game Sound Effects

### Priority: HIGH

### 1. Correct Answer Sound

| File Name | Description | Duration | Mood |
|-----------|-------------|----------|------|
| `sfx_correct.mp3` | Correct answer feedback | 0.5-1s | Positive, encouraging |

**Requirements**:
- Cheerful but not distracting
- Child-friendly tone
- Should evoke success without being overly celebratory

### 2. Incorrect Answer Sound

| File Name | Description | Duration | Mood |
|-----------|-------------|----------|------|
| `sfx_incorrect.mp3` | Wrong answer feedback | 0.5-1s | Gentle, non-punitive |

**Requirements**:
- Soft, mild tone
- Should not discourage the child
- Avoid harsh or jarring sounds

### 3. Achievement Unlock Sound

| File Name | Description | Duration | Mood |
|-----------|-------------|----------|------|
| `sfx_achievement.mp3` | Achievement earned | 1-2s | Celebratory, rewarding |

**Requirements**:
- Sense of accomplishment
- More elaborate than correct answer
- Could use ascending melody

### 4. Button Click Sound

| File Name | Description | Duration | Mood |
|-----------|-------------|----------|------|
| `sfx_click.mp3` | UI button tap | 0.1-0.3s | Clean, responsive |

**Requirements**:
- Very short and subtle
- Should not become annoying with repeated use
- Consistent across all buttons

### 5. Combo Sounds (Progressive)

| File Name | Description | Duration | Mood |
|-----------|-------------|----------|------|
| `sfx_combo_3.mp3` | 3 correct in a row | 0.5s | Encouraging |
| `sfx_combo_5.mp3` | 5 correct in a row | 0.7s | Exciting |
| `sfx_combo_10.mp3` | 10 correct in a row | 1s | Thrilling |

**Requirements**:
- Progressive intensity (3 < 5 < 10)
- Builds momentum feeling
- Rewards sustained attention

### 6. Level Complete Sound

| File Name | Description | Duration | Mood |
|-----------|-------------|----------|------|
| `sfx_level_complete.mp3` | Level finished | 2-3s | Victory, accomplishment |

**Requirements**:
- Sense of completion and achievement
- More elaborate than combo sounds
- Could use fanfare-style melody

### 7. Star Earned Sound

| File Name | Description | Duration | Mood |
|-----------|-------------|----------|------|
| `sfx_star.mp3` | Star awarded | 0.5s | Sparkle, magical |

**Requirements**:
- Light, magical feel
- Could use bell/chime tones

---

## Background Music

### Priority: LOW (Optional)

### 1. Main Menu Music

| File Name | Description | Duration | Mood |
|-----------|-------------|----------|------|
| `bgm_menu.mp3` | Home screen background | 2-3 min (loop) | Welcoming, cheerful |

**Requirements**:
- Light, upbeat melody
- Not distracting
- Seamless loop
- Child-friendly instrumentation

### 2. Gameplay Music

| File Name | Description | Duration | Mood |
|-----------|-------------|----------|------|
| `bgm_gameplay.mp3` | During game activities | 2-3 min (loop) | Focus, gentle |

**Requirements**:
- Subtle, non-intrusive
- Should not interfere with concentration
- Can have ambient or minimalist style
- Lower volume than pronunciation

### 3. Relaxation Music

| File Name | Description | Duration | Mood |
|-----------|-------------|----------|------|
| `bgm_relax.mp3` | Break/meditation time | 3-5 min (loop) | Calm, peaceful |

**Requirements**:
- Very soothing
- Slow tempo
- Natural sounds or soft instrumentation

---

## File Naming Convention

### Pattern

```
[type]_[name]_[variant].[extension]
```

### Types

| Prefix | Usage |
|--------|-------|
| `word_` | Vocabulary pronunciation |
| `sfx_` | Sound effect |
| `bgm_` | Background music |

### Examples

- `word_look_001.mp3` - Pronunciation of "look"
- `sfx_correct.mp3` - Correct answer sound
- `bgm_menu.mp3` - Main menu background music
- `sfx_combo_5.mp3` - 5-combo sound effect

### Rules

1. Use lowercase only
2. Use underscores as separators
3. No spaces in filenames
4. Use descriptive names
5. Include word ID for vocabulary

---

## Resource Sources

### Vocabulary Pronunciation

#### Free Online Dictionaries

| Source | URL | Quality | License |
|--------|-----|---------|---------|
| Cambridge Dictionary | dictionary.cambridge.org | ⭐⭐⭐⭐⭐ | Free for educational use |
| Oxford Learner's Dictionaries | oxfordlearnersdictionaries.com | ⭐⭐⭐⭐⭐ | Free for educational use |
| Merriam-Webster | merriam-webster.com | ⭐⭐⭐⭐ | Free for educational use |
| Google Pronunciation | google.com (search "pronounce [word]") | ⭐⭐⭐⭐ | Free |

**Note**: These sources provide audio that can be used for educational purposes under fair use, but verify terms of use.

#### TTS (Text-to-Speech) Tools

| Tool | Quality | Cost |
|------|---------|------|
| Google TTS API | ⭐⭐⭐ | Free tier available |
| Microsoft Azure TTS | ⭐⭐⭐⭐ | Paid with free tier |
| Amazon Polly | ⭐⭐⭐⭐ | Paid with free tier |
| iOS/macOS Built-in TTS | ⭐⭐⭐ | Free |

**Recommendation**: For MVP, use built-in TTS APIs. For production, consider professional recording.

### Sound Effects

#### Free Sound Effect Libraries

| Source | URL | License |
|--------|-----|---------|
| Freesound.org | freesound.org | CC0, CC-BY |
| ZapSplat | zapsplat.com | Free (with attribution) |
| Mixkit | mixkit.co | Free (no attribution) |
| Pixabay Sound Effects | pixabay.com/music/sound-effects/ | Free |

**Recommended Collections**:
- "UI Sounds" - Button clicks
- "Success Sounds" - Correct answers
- "Game Sounds" - Achievements, combos
- "Cartoon Sounds" - Child-friendly effects

### Background Music

#### Free Music Libraries

| Source | URL | License |
|--------|-----|---------|
| FreePD | freepd.com | CC0 (Public Domain) |
| Bensound | bensound.com | Free with attribution |
| Incompetech | incompetech.com | CC-BY |
| Purple Planet | purple-planet.com | Free with attribution |
| YouTube Audio Library | youtube.com/audiolibrary | Varies |

**Recommended Genres**:
- "Children's Music"
- "Upbeat"
- "Ambient"
- "Calm"

---

## Directory Structure

```
app/src/main/res/raw/
├── pronunciation/
│   ├── word_look_001.mp3
│   ├── word_see.mp3
│   ├── word_watch.mp3
│   ├── ... (27 more files)
│   └── word_clear.mp3
├── sfx/
│   ├── sfx_correct.mp3
│   ├── sfx_incorrect.mp3
│   ├── sfx_achievement.mp3
│   ├── sfx_click.mp3
│   ├── sfx_combo_3.mp3
│   ├── sfx_combo_5.mp3
│   ├── sfx_combo_10.mp3
│   ├── sfx_level_complete.mp3
│   └── sfx_star.mp3
└── bgm/
    ├── bgm_menu.mp3
    ├── bgm_gameplay.mp3
    └── bgm_relax.mp3
```

**Note**: Android's `res/raw/` directory doesn't support subdirectories. All files should be placed directly in `raw/` with appropriate naming prefixes:

```
app/src/main/res/raw/
├── word_look_001.mp3
├── word_see.mp3
├── ...
├── sfx_correct.mp3
├── sfx_incorrect.mp3
├── ...
├── bgm_menu.mp3
├── bgm_gameplay.mp3
└── bgm_relax.mp3
```

---

## Implementation Checklist

### Phase 1: Pronunciation (REQUIRED)

- [ ] Download 30 word pronunciation files
- [ ] Verify audio quality for each file
- [ ] Rename files according to naming convention
- [ ] Place files in `app/src/main/res/raw/`
- [ ] Test playback through Android MediaPlayer
- [ ] Document file sizes in a manifest

### Phase 2: Sound Effects (REQUIRED)

- [ ] Download 7 sound effect files
- [ ] Test each effect for appropriateness
- [ ] Rename files according to naming convention
- [ ] Place files in `app/src/main/res/raw/`
- [ ] Test playback in game context

### Phase 3: Background Music (OPTIONAL)

- [ ] Select 3 background music tracks
- [ ] Verify loop points are smooth
- [ ] Convert to MP3 if needed
- [ ] Rename files according to naming convention
- [ ] Place files in `app/src/main/res/raw/`
- [ ] Test loop playback

---

## Audio Quality Testing

### Testing Checklist

For each audio file:

- [ ] No background noise or static
- [ ] Consistent volume level across files
- [ ] Clear and intelligible (for pronunciation)
- [ ] Appropriate duration (not too long/short)
- [ ] No clipping or distortion
- [ ] Smooth loop (for background music)

### Volume Guidelines

| Type | Relative Volume |
|------|-----------------|
| Pronunciation | 100% (reference) |
| Sound Effects | 80-90% |
| Background Music | 30-40% |

---

## Copyright and Licensing

### License Tracking

Each audio source must be documented with:

| File | Source | License | Attribution Required |
|------|--------|---------|---------------------|
| word_look_001.mp3 | Cambridge | Fair Use | No |
| sfx_correct.mp3 | Freesound.org | CC0 | No |

### Compliance Notes

1. **COPPA Compliance**: No voice data collection or biometric analysis
2. **Educational Fair Use**: Pronunciation from dictionaries generally acceptable
3. **Open Source**: Prefer CC0 and public domain resources
4. **Attribution**: Keep records for CC-BY licensed content

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-02-18 | Initial specification |

---

## Next Steps

1. Human team member downloads audio files from listed sources
2. Files are renamed according to naming convention
3. Files are placed in `app/src/main/res/raw/`
4. See `AUDIO_IMPLEMENTATION_GUIDE.md` for integration code
