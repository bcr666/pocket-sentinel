# Pocket Sentinel (Java)

A tiny console app that projects each bill pocket's balance to the next due date and flags **AT RISK** if the pocket will be short (below safety buffer).

## Build
```
mvn -q -DskipTests package
```

## Run
```
java -jar target/pocket-sentinel-0.1.0.jar pockets.json
```

Exit code is **1** if any pocket is at risk, **0** otherwise.

## JSON format (normalized pay schedule)
See `pockets.json` for an example.
