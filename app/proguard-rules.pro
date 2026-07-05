# Room, WorkManager and Jsoup keep enough metadata via consumer rules bundled in their AARs.
# Keep data/entity classes intact for reflection-based Room mapping.
-keep class com.pctracker.data.db.entity.** { *; }
-keepattributes *Annotation*
