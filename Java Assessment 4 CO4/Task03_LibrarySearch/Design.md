# Task 3 – Campus Library Search layout

## Layout selection
- JFrame uses BorderLayout: title/search at NORTH, results at CENTER, actions/status at SOUTH.
- Header panel uses BorderLayout: title NORTH and search row SOUTH.
- Search row and action row use FlowLayout so controls keep preferred sizes.
- Results panel uses BorderLayout with JScrollPane in CENTER, so it receives the extra space when the window grows.
- GridLayout is not suitable for the whole screen because it gives components equal cells. CardLayout is useful for switching screens, not this single search layout.

## Nested structure
```text
JFrame [BorderLayout]
├── Header JPanel [BorderLayout] (NORTH)
│   ├── Title JLabel [NORTH]
│   └── Search JPanel [FlowLayout] [SOUTH]
├── Results JPanel [BorderLayout] (CENTER)
│   └── JScrollPane → JList
└── Bottom JPanel [BorderLayout] (SOUTH)
    ├── Buttons JPanel [FlowLayout] [CENTER]
    └── Status JLabel [SOUTH]
```

## Resize tests
Capture the same screen at small, medium and large dimensions. The center results region should grow/shrink; header and action controls should retain preferred sizes. Enter your observed dimensions and screenshots in `REPORT/ExperimentLog.md`.

## Alternative analysis
GridBagLayout can provide fine-grained weights and constraints but requires more constraint configuration. Absolute positioning is fragile: controls do not automatically adapt to font scaling, platform differences or resizing. Nested layout managers create a few extra JPanel objects (O(1) for this fixed screen); both designs lay out a fixed number c of components, so layout work is approximately O(c), while absolute positioning has less layout-manager calculation but poor adaptability. Container count is constant for this screen.
