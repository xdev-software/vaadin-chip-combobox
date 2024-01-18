## 4.1.1
* ⚠️ GroupId changed from ``com.xdev-software`` to ``software.xdev``
* Updated dependencies

## 4.1.0
* Use ``Set`` instead of ``Collection`` so that empty value detection works correctly #186
* Replaced ``Label`` with ``Span`` to remove deprecation warnings
* Added deprecation notice 

## 4.0.0
⚠️<i>This release contains breaking changes</i>

* Adds support for Vaadin 24+, drops support for Vaadin 23<br/>
  <i>If you are still using Vaadin 23, use the ``3.x`` versions.</i>
  * Requires Java 17+
* Updated dependencies

## 3.1.1
* Updated dependencies

## 3.1.0
* Added a clear-all button
* Implemented more Vaadin-Mixins for better customization
* Fixed the required indicator
  * The indicator of the "available items"-ComboBox is now only present when the wrapping ChipComboBox is empty
  * The indicator value is now correctly returned by ``isRequiredIndicatorVisible``
* Updated dependencies

## 3.0.1
* Updated dependencies
  * Vaadin 23.2

## 3.0.0
⚠️<i>This release contains breaking changes</i>

* Adds support for Vaadin 23+, drops support for Vaadin 14<br/>
  <i>If you are still using Vaadin 14, use the ``2.x`` versions.</i>
  * Requires Java 11+
* Updated dependencies
