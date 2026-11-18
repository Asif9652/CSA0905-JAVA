# Task 4 – Event-handling forensics

## 4.1 Defect
The listener method is misspelled `mouseclicked`. Java's MouseListener/MouseAdapter callback is case-sensitive: `mouseClicked(MouseEvent)`. Therefore the intended callback is not overridden and is never invoked. The label remains unchanged because the code that increments and updates it is never called.

## 4.2 Failing interaction
After fixing the callback, click several times, minimise the window, then restore it. Dots drawn directly with `getGraphics()` disappear because the drawing is not part of the component's persistent paint state. The window system may repaint at any time.

## 4.3 Repair
Store click coordinates in a `List<Point>`, update the label and call `repaint()`. Override `paint(Graphics)` and redraw all stored points. The completed code does this.

## 4.4 Prevention
With `@Override`, misspelling `mouseClicked` as `mouseclicked` causes a compile-time error because no superclass method is overridden. This catches the defect before execution.

## Alternative: BufferedImage
A BufferedImage stores the drawing as width × height pixels. Each click draws once into the image; `paint` copies the image to the screen. Repainting is approximately O(width × height) for a full image copy (implementation/hardware dependent), rather than O(n) point draw operations for n points. Point-list storage is O(n); image storage is O(width × height × bytes-per-pixel). An image is often faster for many complex marks but uses more memory and needs resizing logic.
