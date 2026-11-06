# Appointment timezone migration policy

Appointment records now have a nullable `timezone` column containing a Java/IANA zone ID (for example, `America/New_York`). Fresh appointments use the timezone configured by the Java runtime. Date/time values in `appt_date` are not converted by the schema migration.

Older appointment rows have no reliable indication of their original timezone. The upgrade therefore adds the column as nullable and does not infer a timezone from the machine running the migration. By default, old rows remain `NULL` and their stored `appt_date` values are preserved.

If you know the original timezone of your legacy calendar and want to assign it to all rows missing a timezone, back up the database, then start BORG with the explicit JVM setting:

```text
-Dborg.appointment.legacyTimezone=America/New_York
```

Replace the example with the appropriate IANA zone ID (for example, `Europe/London`). The setting is applied to rows whose timezone is still null or empty, and can be supplied on a subsequent startup if you did not set it during the initial schema upgrade. Do not use abbreviations such as `EST`. For a desktop launch, add the option to the Java command line in the launch script; for an IDE, add it to the run configuration's VM options.

Without this setting, legacy timezone information remains unknown rather than being silently assigned the migration host's zone. This preserves the source date values but means that applications consuming those legacy records must define their own fallback interpretation until the user supplies a timezone.
