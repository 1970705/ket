# World Map + Fog of War UI Design Specification

**Document Version**: 1.0
**Created**: 2026-02-17
**Designer**: compose-ui-designer
**Priority**: P0 - Core Experience

---

## 1. Design Overview

### 1.1 Current State Analysis
- **Current**: "Island Map" with isolated island cards in a LazyColumn
- **Problem**: Doesn't match "Wordland" branding, lacks exploration feel
- **Target**: Immersive world map with fog of war exploration

### 1.2 Design Goals
1. Create a sense of discovery and exploration
2. Match "Wordland" branding (a world, not isolated islands)
3. Support zoom/pan interactions for larger map
4. Implement fog of war that reveals with progress
5. Maintain 60fps performance on target devices

---

## 2. Visual Style Options

### Option A: Cartoon/Whimsical Style (RECOMMENDED for 10-year-olds)
```
Visual Characteristics:
- Bright, saturated colors
- Rounded, soft shapes
- Playful iconography
- Fantasy elements (floating islands, magical forests)

Color Palette:
- Sky: #87CEEB (Sky Blue) to #E0F6FF gradient
- Land: #90EE90 (Light Green), #DEB887 (Burlywood)
- Water: #1E90FF (Dodger Blue) with subtle waves
- Fog: #F0F0F0 with 60-80% opacity
- Unexplored: #696969 (Dim Gray) overlay
```

### Option B: Pixel Art Style
```
Visual Characteristics:
- Retro 8-bit/16-bit aesthetic
- Blocky, chunky shapes
- Limited color palette
- Nostalgic appeal

Best for: Indie game feel, but may feel dated
```

### Option C: Realistic/Modern Style
```
Visual Characteristics:
- Gradient backgrounds
- Smooth transitions
- Material Design 3 influence
- Clean, minimal icons

Best for: Professional appearance, but less "game-like"
```

**RECOMMENDATION**: Option A (Cartoon) for target age group (10 years)

---

## 3. World Structure Design

### Option A: Connected Continents (Full "Wordland" World)
```
Layout:
┌─────────────────────────────────────┐
│  [Forest]  [City]      [Ocean]     │
│     🌲        🏢          🌊        │
│                                       │
│      [Mountain]   [Castle]          │
│         ⛰️          🏰              │
│                                       │
│  [Village]  [Farm]     [Beach]      │
│     🏡         🚜          🏖️         │
└─────────────────────────────────────┘

Each region contains multiple islands/learning areas
Connected by paths/rivers
Zoom reveals sub-areas
```

### Option B: Wordland Archipelago (Rebranded Islands)
```
Layout: Clustered island groups in a vast ocean
┌─────────────────────────────────────┐
│         🌊  🌊  🌊  🌊              │
│      🏝️🏝️    🏝️  🏝️🏝️           │
│    Look    Make   Listen  Speak     │
│      Island  Lake  Valley  Hill     │
│                                       │
│         🌊  🌊  🌊  🌊              │
│           🏝️  🏝️  🏝️             │
│           (future islands)          │
│                                       │
│    ☁️☁️☁️ (foggy unexplored)☁️☁️☁️    │
└─────────────────────────────────────┘

Bridges/boats connect explored islands
Sailing ship icon shows player position
```

**RECOMMENDATION**: Start with Option B (Archipelago) as evolution of current design
- Easier to implement incrementally
- Maintains current "island" structure
- Can evolve to Option A later

---

## 4. Fog of War System

### 4.1 Fog States

| State | Visual | Interaction |
|-------|--------|-------------|
| **Unexplored** | Dark gray + cloud texture | Cannot click, shows "?" |
| **Partially Revealed** | Semi-transparent with hints | Shows region name, icon |
| **Explored** | Full color, visible | Can click, shows progress |
| **Completed** | Full color + star badge | Shows mastery % |

### 4.2 Fog Visual Design

```kotlin
// Fog overlay specification
FogOverlay(
    unexploredColor = Color(0xFF696969), // Dim Gray
    opacity = 0.7f, // Semi-transparent
    texture = FogTexture.CLOUDS, // Subtle cloud pattern
    edgeSoftness = 32.dp, // Gradient edges
    animation = FogAnimation.DRIFT // Slow floating movement
)
```

### 4.3 Reveal Animation

```
Animation Sequence:
1. User clicks region edge (tap to explore)
2. Fog dissipates from center outward (500ms)
3. Sparkle particles reveal region name
4. Region card expands with bounce effect
5. "Discovered!" toast appears
```

---

## 5. UI Component Specifications

### 5.1 WorldMapContainer

```kotlin
@Composable
fun WorldMapContainer(
    state: WorldMapUiState,
    onRegionClick: (RegionId) -> Unit,
    modifier: Modifier = Modifier
)
```

**Features**:
- Zoomable (0.5x to 2x)
- Pan-able with bounds
- Minimap in corner
- "You are here" marker

**Layout**:
```
┌─────────────────────────────────────┐
│  [Map Area - Zoom/Pan Content]     │
│                                     │
│        [Minimap] [Zoom Controls]    │
└─────────────────────────────────────┘
```

### 5.2 MapRegion Card

```kotlin
@Composable
fun MapRegionCard(
    region: MapRegion,
    fogState: FogState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**States**:

**Unexplored**:
```
┌─────────────┐
│   ☁️  ☁️    │  Fog overlay
│    ???     │  Question mark
│  ☁️  ☁️    │  Cloud texture
└─────────────┘
```

**Partially Revealed**:
```
┌─────────────┐
│  🌲 Look    │  Region icon + name
│  Island     │  Faded content
│   [🔓]      │  Unlock button
└─────────────┘
```

**Explored**:
```
┌─────────────┐
│  🌲 Look    │  Full color
│  Island     │  Progress bar
│  ██████░░   │  60% mastery
└─────────────┘
```

**Completed**:
```
┌─────────────┐
│⭐🌲 Look⭐ │  Stars + full color
│  Island     │  Progress bar
│  ████████   │  100% mastery
└─────────────┘
```

### 5.3 Minimap

```kotlin
@Composable
fun Minimap(
    regions: List<MapRegion>,
    viewport: Rect,
    modifier: Modifier = Modifier
)
```

**Spec**:
- Size: 100dp x 100dp (expandable to 150dp)
- Position: Bottom-right corner
- Shows all regions as dots
- Viewport rectangle shows current view
- Tap to jump to region

---

## 6. Interaction Design

### 6.1 Tap to Explore

```
When user taps fogged area:
1. Check if adjacent to explored region (unlock condition)
2. If yes: Show "Explore?" confirmation dialog
3. If no: Show "Explore nearby areas first!" toast
4. On confirm: Play reveal animation, unlock region
```

### 6.2 Pinch to Zoom

```
- Two-finger pinch: 0.5x to 2x zoom
- Double tap: Reset to 1x
- Zoom centers on tap point
- Smooth animation (300ms)
```

### 6.3 Drag to Pan

```
- Single finger drag: Pan map
- Bounds checking: Cannot pan beyond map edges
- Momentum scroll: Decelerates after release
```

---

## 7. Themed Regions

### Region Theme Mapping

| Region | Theme | Vocabulary | Colors | Icon |
|--------|-------|------------|--------|------|
| Look Island | Forest/Observation | see, watch, tree, bird | Greens | 🌲 |
| Make Lake | Ocean/Craft | boat, fish, swim | Blues | 🌊 |
| Listen Valley | Mountain/Sound | echo, thunder, wind | Grays | ⛰️ |
| Speak Hill | Village/Talking | hello, ask, tell | Browns | 🏡 |
| Read Castle | Castle/Books | read, story, page | Purples | 🏰 |
| Write Farm | Farm/Creating | plant, grow, build | Yellows | 🚜 |

### Region Progression

```
Level 1: Look Island (Forest) - Basic observation
Level 2: Make Lake (Ocean) - Completed Look → Unlock
Level 3: Listen Valley (Mountain) - Completed Make → Unlock
Level 4: Speak Hill (Village) - Completed Listen → Unlock
Level 5: Read Castle (Castle) - Completed Speak → Unlock
Level 6: Write Farm (Farm) - Completed Read → Unlock
```

---

## 8. Animation Specifications

### 8.1 Fog Reveal Animation

```kotlin
// Animation spec
val fogRevealSpec = tween<Float>(
    durationMillis = 500,
    easing = FastOutSlowInEasing
)

// Staggered reveal for multiple regions
val staggerDelay = 100 // ms between each region
```

### 8.2 Region Selection

```kotlin
// Bounce effect on tap
val bounceSpec = spring<Float>(
    dampingRatio = 0.8f,
    stiffness = 300f
)
```

### 8.3 Ambient Fog Animation

```kotlin
// Fog drifts slowly
val infiniteTransition = rememberInfiniteTransition(label = "fog")
val fogOffset by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
        animation = tween(30000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    )
)
```

---

## 9. Technical Implementation Notes

### 9.1 Compose Components

```kotlin
// Core components needed
- ZoomableContainer: Wrapper for zoom/pan
- FogOverlay: Canvas drawing for fog effect
- RegionCard: Individual region display
- Minimap: Overview navigation
- MapControls: Zoom buttons, compass
```

### 9.2 Performance Considerations

- Use `drawWithCache` for fog texture
- Limit recomposition with stable keys
- Pre-render fog texture to bitmap
- Use `Modifier.graphicsLayer` for zoom/pan transforms

### 9.3 Data Model

```kotlin
data class WorldMap(
    val regions: List<MapRegion>,
    val fogState: FogStateMap,
    val playerPosition: Offset
)

data class MapRegion(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val position: Offset, // Normalized (0-1)
    val size: Size,      // Normalized
    val theme: RegionTheme,
    val fogState: FogState,
    val masteryPercentage: Float
)

enum class FogState {
    UNEXPLORED,
    PARTIALLY_REVEALED,
    EXPLORED,
    COMPLETED
}
```

---

## 10. Success Metrics

- User spends 2+ minutes exploring map on first visit
- 80% of users tap at least one fogged area
- Map zoom/pan usage > 50% of users
- Positive feedback on "discovery" feeling

---

## 11. Next Steps

1. **Immediate**: Create wireframe/mockup images
2. **Week 1**: Implement basic zoom/pan container
3. **Week 1**: Implement fog overlay system
4. **Week 2**: Add region cards and interactions
5. **Week 2**: Polish animations and transitions

---

**Status**: Ready for Review
**Awaiting**: Game designer input on visual style preference
