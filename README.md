# Required Features

- [x] Contains at least 1 activity drawn using 2D graphics
- Works in real time
    - [x] Real time elements updated synchronously in each frame(fps)
    - [x] Interval elements updated synchronously (Timer) at predetermined interval steps
    - [ ] Asynchronous elements updated in threads (Swipe Executor Service)
- [x] Interactive, responds to user events
- [x] Perform parallel operations by creating at least one worker thread
- [x] Ensures that threads synchronize updates to a common state with built-in synchronization primitives (use mutexes)

# Additional Features
- At least 1 activity containing Standard GUI Components 
    - [x] MainActivity
    - [x] AuthorsActivity
- [x] Store User Record in SQLite
- [x] Notifications on Win
- [x] Vibrations on out of bounds
- [x] Screen always on (in GameView)
- [x] Using intent to pass data (input your name, then start, in MainActivity)

# Todos

- [ ] Sequence Diagram
- [ ] Make swiper executor service
- [ ] Documentation
- [ ] Feature List (Features Mapped to Requirements)
- [ ] Restructure code

# Features Design

## 2D Graphics and UI Elements

### UI Elements

### 2D Graphics

The `GameActivity` class has a `GameView` that renders the `Game` dynamically.

The `GameView` handles the rendering logic and the `Game` contains the game logic.

`Game` renders two components:

1. Board 

## Real time

## Interactive

## Thread and Synchronization

## Screen Always On

## Vibrations and Notifications

## Records in SQLite

## Data Flow

