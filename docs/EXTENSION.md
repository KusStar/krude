# Krude Extension Protocol

JSON/JS based extension protocol for krude.

## Example

```ts
type I18N = {
  name?: string
  description?: string
  keywords?: string[]
  type?: string
}

type Extension = {
  id: string
  required?: string[]
  keywords?: string[]
  name: string
  description?: string
  type: "scheme"
  uri: string
  i18n?: {
    [key: string]: I18N & {
      uri?: string
    }
  }
} | {
  id: string
  required?: string[]
  keywords?: string[]
  name: string
  description?: string
  type: "intent"
  data: IntentData
  i18n?: {
    [key: string]: I18N & {
      data?: IntentData
    }
  }
}

type IntentData = {
  package: string
  class: string
  extras?: {
    [key: string]: string | number | boolean
  }
  flags?: number
  action?: string
}

type Main = Extension | Extension[] | string

type KrudeExtension = {
  name: string
  version: string
  krudeVersion: {
    min: string
    max: string
  }
  description?: string
  main: Main
}
```

Example:

```json
{
    "name": "krude-extension-example",
    "version": "1.0.0",
    "krudeVersion": {
      "min": "1.0.0",
      "max": "2.0.0"
    },
    "description": "Krude extensions example",
    "main": [
      {
        "required": ["com.tencent.mm"],
        "name": "微信-扫一扫",
        "description": "Wechat QR Scan",
        "type": "intent",
        "data": {
          "package": "com.tencent.mm",
          "class": "com.tencent.mm.ui.LauncherUI",
          "extra": {
            "LauncherUI.From.Scaner.Shortcut": true
          },
          "flags": 335544320,
          "action": "android.intent.action.VIEW"
        }
      },
      {
        "required": ["com.eg.android.AlipayGphone"],
        "name": "支付宝-扫一扫",
        "description": "Alipay QR Scan",
        "type": "scheme",
        "uri": "alipayqr://platformapi/startapp?saId=10000007"
      },
      {
        "name": "Email",
        "description": "Open mailto://",
        "type": "scheme",
        "uri": "mailto://",
      }
    ]
}
```

More at [Krude Extension Store](https://github.com/KusStar/krude-extensions?tab=readme-ov-file)
