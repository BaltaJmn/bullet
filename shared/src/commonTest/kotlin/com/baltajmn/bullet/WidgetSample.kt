package com.baltajmn.bullet

/**
 * The one example of `widget.json` (docs/tecnico.md 4.2): `widgetState` of the diary in
 * `WidgetStateTest` is exactly this, and `tools/check-bobbinstore.swift` decodes it with
 * `BobbinStore.swift` (tests 12 and 36). It changes with every new field, on both sides.
 */
const val WIDGET_SAMPLE =
    """{"date":"2026-09-23","open":3,"done":2,"events":1,"month":"2026-09",""" +
        """"monthMask":"110110011101111011101010000000","reviewPending":true,"isPro":false,""" +
        """"cover":"sage","dayStartHour":4}"""
