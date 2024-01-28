# NFTFR Backend

## Setup

Aprire il progetto con intellij ed eseguirlo da codice sorgente. È necessario avere installato postgres ed aver impostato il [database](/database/README.md).

La backend si può controllare tramite il file `nftfr_config.json`, sotto `/src/main/resources`. Il contenuto del file è il seguente:

```json
{
  "dbPort": "5432",
  "dbName": "nftfr_ingsw",
  "dbUsername": "postgres",
  "dbPassword": "password_qui",
  "jwtSecret": "odAh38us0qj7coVBSfrvAEyKxJ2ecgqa8oPAPwvZi/c=",
  "nftImagePath": "C:\\Users\\user\\Documents\\Repos\\test_images"
}
```

- `dbPort`: porta da utilizzare per la connessione al database
- `dbName`: nome del database a cui connettersi
- `dbUsername`: username da utilizzare per la connessione al database
- `dbPassword`: password da utilizzare per la connessione al database
- `jwtSecret`: chiave segreta utilizzata per generare i token
- `nftImagePath`: percorso alla cartella dove vengono salvate le immagini degli NFT

Inoltre, nel file `application.properties` è possibile impostare la porta da utilizzare per il server (di default è `9001`).

## Organizzazione

La backend si divide in 3 parti.

### Persistenza

Il codice relativo all'interazione con il database, ne fanno parte DAO e DTO.

### Controller

Il codice relativo alle REST API, utilizzate dalla frontend, e alle servlet, utilizzare per implementare la sezione riservata agli amministratori, disponibile a `http://localhost:9001/admin`.

### Applicazione

Codice proprio dell'applicazione, implementa la logica per gestire i token, le immagini, il real time per le aste, etc.

## Note tecniche

- L'implementazione dei DAO fa uso di classi proxy per caricare dati in modo lazy.
- Le conversioni di valute sono implementate con l'ausilio delle API esterne [etherscan.io](https://etherscan.io/).
- La backend supporta più formati di immagini per NFT, che vengono automaticamente convertite in fase di upload.
- Le password nel database sono criptate per garantire sicurezza, e l'autenticazione per le REST API avviene tramite token JWS.
- Il sistema real time delle aste è implementato tramite Server Sent Events.