# NFTFR Backend

## Setup

La backend si può controllare tramite il file `nftfr_config.json`, sotto `\src\main\resources`. Il contenuto del file è il seguente:

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