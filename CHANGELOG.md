Version 2.1.0
* Added more utility functions to various classes
* Added `WritableIntMap` abstract class to allow generic use of modifiable `IntMap` implementations (provided implementations extend this abstract class now)
* Added traceColor(...) method to `Tracer` class for tracing specific colors from an `IntMap` 

Version 2.0.0
* Changed Tracer to abstract class
* Refactored original algorithm to IntervalTracer
* Added PolylineTracer as an alternative algorithm for tracing
* **Breaking change to Tracer API:** `interval` now a constructor argument instead of a function parameter for the IntervalTracer

Version 1.1.0
* added methods to `BezierCurve` and `BezierShape` to support scaling the size of the traced path

Version 1.0.0
* Initial release
