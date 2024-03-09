# Velocity Drain
This plugin prevents velocity from shutting down when it recieves `SIGINT` if players are still connected. It makes velocity wait until all players disconnect before shutting down. This is primarily meant for users hosting velocity in k8s, as it allows a rolling restart to be completed without any player interruptions.
