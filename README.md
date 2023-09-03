# (Legacy) PotoCraft BlockBrotection Plugin

This plugin is a old implementation with more features but also a few issues.
It supports placing and breaking blocks preventing other players from breaking them (except through an explosion or by trusted players).
Almost every feature is toggled through a configuration file (without needing to repackage the jar file).

Its issue is it suffers from HikariCP's pool exhaustion, which creates a deadlock and crashes the server given a not sufficiently large pool (I was using the not recommended 100, 150 amount).
At the time I didn't know how to fix it, as many do, but the solution is simple:
use an `java.util.concurrent.Executor` with the number of threads being at most the same amount of connections in the pool, so that all threads can have at least one connection available, preventing exhausting of the pool.
