# FE-F05 resumed emulator availability check

No app state was changed and no credentials or OTP were used.

Command: `/private/tmp/srastemsya-sdk.7DCrTs/platform-tools/adb devices -l`

Output:

```
List of devices attached
```

Command: `/private/tmp/srastemsya-sdk.7DCrTs/platform-tools/adb -s emulator-5554 get-state`

Output:

```
error: device 'emulator-5554' not found
```

Command: `/private/tmp/srastemsya-sdk.7DCrTs/platform-tools/adb -s emulator-5556 get-state`

Output:

```
error: device 'emulator-5556' not found
```

Result: the documented adb executable ran, but neither requested emulator is attached/available in this session. Functional acceptance remains blocked before app interaction. This is an environment/access blocker, not a product failure.
