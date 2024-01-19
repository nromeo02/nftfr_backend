# NFTFR Backend

## Setup

La backend si può controllare tramite il file `nftfr_config.json`, sotto `/src/main/resources`. Il contenuto del file è il seguente:

```json
{
  "dbPort" : "5432",
  "dbName" : "nftfr",
  "dbUsername" : "postgres",
  "dbPassword" : "password_qui",
  "jwtSecret" : "odAh38us0qj7coVBSfrvAEyKxJ2ecgqa8oPAPwvZi/c=",
  "nftImagePath" : "C:/Users/user/nft_images"
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

### Rest controller

Il codice relativo alle REST API, utilizzate dalla frontend.

### Servlet

Il codice relativo alla sezione riservata agli amministratori, implementata tramite thymeleaf ed accessibile sotto `http://localhost:9001/admin`.

### Note tecniche

- L'implementazione dei DAO fa uso di classi proxy per caricare dati in modo lazy.
- Le conversioni di valute sono implementate con l'ausilio delle API esterne [etherscan.io](https://etherscan.io/).
- La backend supporta più formati di immagini per NFT, che vengono automaticamente convertite in fase di upload.
- Le password nel database sono criptate per garantire sicurezza, e le interazioni con molte REST API avvengono tramite token JWS.