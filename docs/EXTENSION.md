# Krude Extension Protocol

JSON/JS based extension protocol for krude.

## Example

```ts
type Extension = {
  required?: string[]
  name: string
  description?: string
  type: "scheme"
  uri: string
} | {
  required?: string[]
  name: string
  description?: string
  type: "intent"
  data: {
    package: string
    class: string
    extras?: {
      [key: string]: string | number | boolean
    }
    flags?: number
    action?: string
  }
}

type Main = Extension | Extension[] | string
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
