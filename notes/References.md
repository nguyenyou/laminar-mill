┌─────────────────────────────────────────────────────────────┐
│                    REFERENCE FLOW                            │
└─────────────────────────────────────────────────────────────┘

Parent Observable                    Child Observable
┌──────────────┐                    ┌──────────────┐
│              │                    │              │
│  Parent      │◄───────────────────│  parent: P   │  (1) Child ALWAYS
│              │   Strong ref       │              │      holds parent
│              │   (permanent)      │              │
└──────────────┘                    └──────────────┘
       │                                    ▲
       │                                    │
       │ (2) Parent holds child             │
       │     ONLY when child                │
       │     is started (has                │
       │     observers)                     │
       │                                    │
       ▼                                    │
┌──────────────┐                           │
│ internal     │                           │
│ Observers:   │                           │
│ [child]      │───────────────────────────┘
│              │   Strong ref
└──────────────┘   (conditional - only when started)


LIFECYCLE:

1. Child created:
   - Child holds parent reference ✓
   - Parent does NOT hold child reference ✗

2. Child.addObserver() called:
   - Child calls parent.addInternalObserver(this)
   - Parent NOW holds child reference ✓

3. Child subscription killed:
   - Child calls parent.removeInternalObserver(this)
   - Parent removes child reference ✗
   - If child is unreachable, it can be GC'd