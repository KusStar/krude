# Krude Extension Protocol

JSON/JS based extension protocol for krude.

## Example

```ts
interface Extension {
  name: string
  description?: string
  type: "activity" | "scheme" | "script"
  uri: string
}

type Main = Extension | Extension[] | string
```

```json
{
    "name": "krude-extension-example",
    "version": "1.0.0",
    "description": "Krude extensions example",
    "main": [
      {
        "name": "Settings - WiFi",
        "description": "Open System Wifi",
        "type": "activity",
        "uri": "android.settings.WIFI_SETTINGS",
      },
      {
        "name": "Email",
        "description": "Open mailto://",
        "type": "scheme",
        "uri": "mailto://",
      },
      {
        "name": "Custom Extension Script",
        "description": "",
        "type": "script",
        "uri": "./index.js",
      }
    ],
}
```
