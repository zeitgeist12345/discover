## Deploy a new azure function

```
(base) zeitgeist@zeitgeist-workstation:~/Documents/github/discover/deprecated/azure$ zip -r deploy.zip . -x *.git* -x *.vscode* -x local.settings.json

(base) zeitgeist@zeitgeist-workstation:~/Documents/github/discover/deprecated/azure$ func azure functionapp publish discover-api --build remote --nozip
```
